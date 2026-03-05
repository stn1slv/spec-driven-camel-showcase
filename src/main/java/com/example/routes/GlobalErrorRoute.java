package com.example.routes;

import com.example.mapping.ProblemDetail;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class GlobalErrorRoute extends RouteConfigurationBuilder {

    @Override
    public void configuration() throws Exception {
        routeConfiguration()
            .onException(Exception.class)
            .handled(true)
            .process(exchange -> {
                Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                int statusCode = 500;
                String title = "Internal Server Error";
                
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
                
                String uri = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                
                ProblemDetail problem = new ProblemDetail(
                        URI.create("/problems/internal-server-error"),
                        title,
                        statusCode,
                        cause != null ? cause.getMessage() : "Unknown error",
                        uri != null ? URI.create(uri) : null
                );
                
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
                exchange.getMessage().setBody(problem);
            });
    }
}
