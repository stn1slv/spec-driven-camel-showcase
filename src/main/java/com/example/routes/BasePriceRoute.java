package com.example.routes;

import com.example.api.AppConfig;
import com.example.mapping.ProblemDetail;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route for retrieving base product prices with local error handling.
 */
@Component
public class BasePriceRoute extends RouteBuilder {

  private final AppConfig appConfig;

  public BasePriceRoute(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @Override
  public void configure() throws Exception {
    // Local error handling for connectivity issues, taking precedence over global config.
    onException(SocketTimeoutException.class, TimeoutException.class, ConnectException.class)
        .handled(true)
        .process(exchange -> {
          int statusCode = 503;
          String title = "Service Unavailable";
          exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);

          String uriStr = (String) exchange.getProperty("originalUri");
          if (uriStr == null) {
            uriStr = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
          }
          URI uri = (uriStr != null) ? URI.create(uriStr) : null;

          ProblemDetail problem = new ProblemDetail(
              URI.create("/problems/service-unavailable"),
              title,
              statusCode,
              "Downstream service is temporarily unavailable",
              uri
          );

          exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
          exchange.getMessage().setBody(problem);
        });

    from("direct:base-price-route")
        .routeId("base-price-route")
        .routeConfigurationId("global-error")
        .process(exchange -> {
          Integer productId = exchange.getIn().getHeader("productId", Integer.class);
          if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
          }
        })
        .setProperty("originalUri", header(Exchange.HTTP_URI))
        .removeHeaders("CamelHttp*")
        .removeHeader("Accept-Encoding")
        .circuitBreaker().id("fakestore")
        .toD(appConfig.fakestore().baseUrl() + "/products/${header.productId}"
            + "?connectionTimeout=5000&socketTimeout=5000")
        .unmarshal().json()
        .to("jsonata:mapping/base-price.jsonata")
        .end()
        .removeHeader("Content-Encoding")
        .removeHeader("Transfer-Encoding")
        .removeHeader("Server");
  }
}
