package com.example.api;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel REST DSL.
 */
@Component
public class RestConfig extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    restConfiguration()
        .component("servlet")
        .contextPath("/api")
        .bindingMode(RestBindingMode.json)
        .clientRequestValidation(false);
  }
}
