package com.example.routes;

import com.example.api.AppConfig;
import com.example.mapping.ProblemDetail;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class BasePriceRoute extends RouteBuilder {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void configure() throws Exception {
        
        onException(HttpOperationFailedException.class)
            .handled(true)
            .process(exchange -> {
                HttpOperationFailedException cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                int statusCode = cause.getStatusCode();
                
                if (statusCode == 404) {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                } else if (statusCode == 500) {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 502); 
                    statusCode = 502;
                } else if (statusCode == 504) {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 504);
                } else {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
                }
                
                String uri = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                if (uri == null) {
                    uri = exchange.getProperty("originalUri", String.class);
                }
                
                ProblemDetail problem = new ProblemDetail(
                        URI.create("/problems/downstream-error"),
                        "Downstream Error",
                        statusCode,
                        "Error from downstream system: " + cause.getMessage(),
                        uri != null ? URI.create(uri) : null
                );
                
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
                exchange.getMessage().setBody(problem);
            });

        onException(IllegalArgumentException.class)
            .handled(true)
            .process(exchange -> {
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                String uri = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                if (uri == null) {
                    uri = exchange.getProperty("originalUri", String.class);
                }
                
                ProblemDetail problem = new ProblemDetail(
                        URI.create("/problems/bad-request"),
                        "Bad Request",
                        400,
                        exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage(),
                        uri != null ? URI.create(uri) : null
                );
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
                exchange.getMessage().setBody(problem);
            });

        from("direct:base-price-route")
            .routeId("base-price-route")
            .process(exchange -> {
                String productIdStr = exchange.getIn().getHeader("productId", String.class);
                try {
                    int productId = Integer.parseInt(productIdStr);
                    if (productId <= 0) {
                        throw new IllegalArgumentException("productId must be a positive integer");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("productId must be an integer");
                }
            })
            .setProperty("originalUri", header(Exchange.HTTP_URI))
            .removeHeaders("CamelHttp*")
            .removeHeader("Accept-Encoding")
            .circuitBreaker().id("fakestore")
                .toD(appConfig.baseUrl() + "/products/${header.productId}?connectionTimeout=5000&socketTimeout=5000")
                .unmarshal().json()
                .to("jsonata:mapping/base-price.jsonata")
            .onFallback()
                .process(exchange -> {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 503);
                    String productId = exchange.getIn().getHeader("productId", String.class);
                    ProblemDetail problem = new ProblemDetail(
                            URI.create("/problems/service-unavailable"),
                            "Service Unavailable",
                            503,
                            "The downstream product service is temporarily unavailable. Please try again later.",
                            productId != null ? URI.create("/v1/products/" + productId + "/base-price") : null
                    );
                    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
                exchange.getMessage().setBody(problem);
                })
            .end()
            .removeHeader("Content-Encoding")
            .removeHeader("Transfer-Encoding")
            .removeHeader("Server");
    }
}
