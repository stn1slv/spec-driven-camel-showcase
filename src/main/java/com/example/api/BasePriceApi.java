package com.example.api;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.model.rest.RestParamType;

@Component
public class BasePriceApi extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        rest("/v1/products")
            .get("/{productId}/base-price")
            .description("Retrieve the base price of a product in USD")
            .param().name("productId").type(RestParamType.path).dataType("integer").required(true).endParam()
            .to("direct:base-price-route");
    }
}
