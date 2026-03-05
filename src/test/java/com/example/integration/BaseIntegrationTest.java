package com.example.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.fakestore.base-url=http://mock-fakestore.com",
    "app.baseprice.base-url=http://mock-baseprice.com",
    "app.exchangerate.base-url=https://mock-exchange-rate.com",
    "app.shippo.base-url=https://mock-shippo.com"
})
@UseAdviceWith
public abstract class BaseIntegrationTest {

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected TestRestTemplate restTemplate;

    @BeforeEach
    public void baseSetup() throws Exception {
        if (!camelContext.getStatus().isStarted()) {
            // Advise Base Price Route
            AdviceWith.adviceWith(camelContext, "base-price-route", a -> {
                a.interceptSendToEndpoint("http://mock-fakestore.com*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:fakestore");
            });

            // Advise Localized Quote Sub-routes
            AdviceWith.adviceWith(camelContext, "call-base-price", a -> {
                a.interceptSendToEndpoint("http://mock-baseprice.com*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:base-price");
            });
            AdviceWith.adviceWith(camelContext, "call-exchange-rate", a -> {
                a.interceptSendToEndpoint("https://mock-exchange-rate.com*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:exchange-rate");
            });
            AdviceWith.adviceWith(camelContext, "call-shippo", a -> {
                a.interceptSendToEndpoint("https://mock-shippo.com*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:shippo");
            });
            
            camelContext.start();
        }
    }
}