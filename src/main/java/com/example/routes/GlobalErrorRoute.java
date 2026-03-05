package com.example.routes;

import com.example.mapping.ProblemDetail;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

/**
 * Global fallback error handling using named RouteConfiguration.
 */
@Component
public class GlobalErrorRoute extends RouteConfigurationBuilder {

  @Override
  public void configuration() throws Exception {
    routeConfiguration("global-error")
        .onException(HttpOperationFailedException.class)
        .handled(true)
        .process(exchange -> {
          HttpOperationFailedException cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
              HttpOperationFailedException.class);
          int statusCode = cause.getStatusCode();
          int mappedStatus = (statusCode == 404) ? 404
              : (statusCode == 400 || statusCode == 422) ? statusCode : 502;

          String uriStr = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
          URI uri = (uriStr != null) ? URI.create(uriStr) : null;

          exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, mappedStatus);
          ProblemDetail problem = new ProblemDetail(
              URI.create("/problems/downstream-error"),
              "Downstream Error",
              mappedStatus,
              "Downstream error: " + cause.getMessage(),
              uri
          );
          exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
          exchange.getMessage().setBody(problem);
        })
        .end()
        .onException(Exception.class)
        .handled(true)
        .process(exchange -> {
          Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

          // Find the most relevant exception in the cause chain
          HttpOperationFailedException httpErr = null;
          IllegalArgumentException validationErr = null;
          SocketTimeoutException socketTimeoutErr = null;
          TimeoutException concurrentTimeoutErr = null;
          ConnectException connectErr = null;

          Throwable current = cause;
          while (current != null) {
            if (current instanceof HttpOperationFailedException) {
              httpErr = (HttpOperationFailedException) current;
            }
            if (current instanceof IllegalArgumentException) {
              validationErr = (IllegalArgumentException) current;
            }
            if (current instanceof SocketTimeoutException) {
              socketTimeoutErr = (SocketTimeoutException) current;
            }
            if (current instanceof TimeoutException) {
              concurrentTimeoutErr = (TimeoutException) current;
            }
            if (current instanceof ConnectException) {
              connectErr = (ConnectException) current;
            }
            current = current.getCause();
          }

          int statusCode = 500;
          String title = "Internal Server Error";
          String detail = cause != null ? cause.getMessage() : "Unknown error";
          String type = "/problems/internal-server-error";

          if (validationErr != null) {
            statusCode = 400;
            title = "Bad Request";
            detail = validationErr.getMessage();
            type = "/problems/bad-request";
          } else if (httpErr != null) {
            int httpStatus = httpErr.getStatusCode();
            if (httpStatus == 404) {
              statusCode = 404;
              title = "Not Found";
              type = "/problems/not-found";
            } else if (httpStatus == 400 || httpStatus == 422) {
              statusCode = httpStatus;
              title = "Unprocessable Entity";
              type = "/problems/unprocessable-entity";
            } else {
              statusCode = 502;
              title = "Bad Gateway";
              type = "/problems/bad-gateway";
            }
            detail = "Downstream error: " + httpErr.getMessage();
          } else if (socketTimeoutErr != null || concurrentTimeoutErr != null) {
            statusCode = 504;
            title = "Gateway Timeout";
            detail = "Timeout waiting for downstream response";
            type = "/problems/gateway-timeout";
          } else if (connectErr != null) {
            statusCode = 503;
            title = "Service Unavailable";
            detail = "Downstream service is unavailable";
            type = "/problems/service-unavailable";
          }

          String uriStr = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
          URI uri = (uriStr != null) ? URI.create(uriStr) : null;

          exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
          ProblemDetail problem = new ProblemDetail(
              URI.create(type),
              title,
              statusCode,
              detail,
              uri
          );
          exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/problem+json");
          exchange.getMessage().setBody(problem);
        });
  }
}
