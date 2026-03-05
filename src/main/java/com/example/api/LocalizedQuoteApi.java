package com.example.api;

import com.example.mapping.LocalizedQuoteRecords.LocalizedOrderQuote;
import com.example.mapping.ProblemDetail;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * REST API definition for localized quotes.
 */
@Component
public class LocalizedQuoteApi extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    rest("/v1")
        .get("/products/{productId}/localized-quote")
        .description("Get a complete quote including currency conversion and shipping")
        .outType(LocalizedOrderQuote.class)
        .responseMessage().code(200).message("Localized quote generated successfully")
        .endResponseMessage()
        .responseMessage().code(400).message("Bad Request").responseModel(ProblemDetail.class)
        .endResponseMessage()
        .responseMessage().code(422).message("Unprocessable Entity")
        .responseModel(ProblemDetail.class).endResponseMessage()
        .responseMessage().code(500).message("Internal Server Error")
        .responseModel(ProblemDetail.class).endResponseMessage()
        .responseMessage().code(503).message("Service Unavailable")
        .responseModel(ProblemDetail.class).endResponseMessage()
        .responseMessage().code(504).message("Gateway Timeout")
        .responseModel(ProblemDetail.class).endResponseMessage()
        .to("direct:localized-quote");
  }
}
