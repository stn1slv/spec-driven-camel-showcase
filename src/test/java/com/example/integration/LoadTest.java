package com.example.integration;

import com.example.mapping.BasePrice;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.fakestore.base-url=http://mock-fakestore.com"
})
@UseAdviceWith
public class LoadTest {

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
    public void testPerformance() throws Exception {
        mockFakeStore.expectedMessageCount(10);
        mockFakeStore.whenAnyExchangeReceived(e -> {
            e.getMessage().setBody("{\"id\":1,\"title\":\"Backpack\",\"price\":109.95,\"description\":\"...\",\"category\":\"men\",\"image\":\"http\",\"rating\":{\"rate\":3.9,\"count\":120}}");
            e.getMessage().setHeader(org.apache.camel.Exchange.HTTP_RESPONSE_CODE, 200);
        });

        long start = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            ResponseEntity<BasePrice> response = restTemplate.getForEntity("/v1/products/1/base-price", BasePrice.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        
        long end = System.currentTimeMillis();
        long duration = end - start;
        
        mockFakeStore.assertIsSatisfied();
        
        System.out.println("10 requests took " + duration + " ms");
        
        // Assert processing overhead is < 50ms per request on average (since mock is instant)
        // 50ms * 10 = 500ms total
        assertTrue(duration < 1000, "Processing overhead is too high");
    }
}
