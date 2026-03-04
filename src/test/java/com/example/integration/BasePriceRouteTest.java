package com.example.integration;

import com.example.mapping.BasePrice;
import com.example.mapping.ProblemDetail;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.fakestore.base-url=http://mock-fakestore.com"
})
@UseAdviceWith
public class BasePriceRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @EndpointInject("mock:fakestore")
    private MockEndpoint mockFakeStore;

    @BeforeEach
    public void setup() throws Exception {
        mockFakeStore.reset();
        
        if (!camelContext.getStatus().isStarted()) {
            AdviceWith.adviceWith(camelContext, "base-price-route", a -> {
                a.interceptSendToEndpoint("http://mock-fakestore.com*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:fakestore");
            });
            camelContext.start();
        }
    }

    @AfterEach
    public void tearDown() {
        mockFakeStore.reset();
    }

    @Test
    public void testSuccessRetrieval() throws Exception {
        mockFakeStore.expectedMessageCount(1);
        mockFakeStore.whenAnyExchangeReceived(e -> {
            e.getMessage().setBody("{\"id\":1,\"title\":\"Backpack\",\"price\":109.95,\"description\":\"...\",\"category\":\"men\",\"image\":\"http\",\"rating\":{\"rate\":3.9,\"count\":120}}");
            e.getMessage().setHeader(org.apache.camel.Exchange.HTTP_RESPONSE_CODE, 200);
        });

        ResponseEntity<BasePrice> response = restTemplate.getForEntity("/v1/products/1/base-price", BasePrice.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().productId());
        assertEquals(109.95, response.getBody().basePrice());
        assertEquals("USD", response.getBody().currency());

        mockFakeStore.assertIsSatisfied();
    }

    @Test
    public void testProductNotFound() throws Exception {
        mockFakeStore.expectedMessageCount(1);
        mockFakeStore.whenAnyExchangeReceived(e -> {
            throw new org.apache.camel.http.base.HttpOperationFailedException("http://mock-fakestore.com/products/99", 404, "Not Found", null, null, "");
        });

        ResponseEntity<ProblemDetail> response = restTemplate.getForEntity("/v1/products/99/base-price", ProblemDetail.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        
        mockFakeStore.assertIsSatisfied();
    }
}
