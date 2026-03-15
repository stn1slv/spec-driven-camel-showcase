package com.example.integration;

import com.example.mapping.BasePrice;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadTest extends BaseIntegrationTest {

    @EndpointInject("mock:fakestore")
    private MockEndpoint mockFakeStore;

    @BeforeEach
    public void setup() throws Exception {
        mockFakeStore.reset();
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
            ResponseEntity<BasePrice> response = restTemplate.getForEntity("/api/v1/products/1/base-price", BasePrice.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        
        long end = System.currentTimeMillis();
        long duration = end - start;
        
        mockFakeStore.assertIsSatisfied();
    }
}
