package com.example.integration;

import com.example.mapping.BasePrice;
import com.example.mapping.ProblemDetail;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class BasePriceRouteTest extends BaseIntegrationTest {

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
    public void testSuccessRetrieval() throws Exception {
        mockFakeStore.expectedMessageCount(1);
        mockFakeStore.whenAnyExchangeReceived(e -> {
            e.getMessage().setBody("{\"id\":1,\"title\":\"Backpack\",\"price\":109.95,\"description\":\"...\",\"category\":\"men\",\"image\":\"http\",\"rating\":{\"rate\":3.9,\"count\":120}}");
            e.getMessage().setHeader(org.apache.camel.Exchange.HTTP_RESPONSE_CODE, 200);
        });

        ResponseEntity<BasePrice> response = restTemplate.getForEntity("/api/v1/products/1/base-price", BasePrice.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().productId());
        assertEquals(109.95, response.getBody().basePrice(), 0.0001);
        assertEquals("USD", response.getBody().currency());

        mockFakeStore.assertIsSatisfied();
    }

    @Test
    public void testProductNotFound() throws Exception {
        mockFakeStore.expectedMessageCount(1);
        mockFakeStore.whenAnyExchangeReceived(e -> {
            throw new org.apache.camel.http.base.HttpOperationFailedException("http://mock-fakestore.com/products/99", 404, "Not Found", null, null, "");
        });

        ResponseEntity<ProblemDetail> response = restTemplate.getForEntity("/api/v1/products/99/base-price", ProblemDetail.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        
        mockFakeStore.assertIsSatisfied();
    }

    @Test
    public void testInvalidProductIdValidation() throws Exception {
        mockFakeStore.expectedMessageCount(0);

        ResponseEntity<ProblemDetail> response = restTemplate.getForEntity("/api/v1/products/0/base-price", ProblemDetail.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());

        mockFakeStore.assertIsSatisfied();
    }

    @Test
    public void testCircuitBreakerFallbackServiceUnavailable() throws Exception {
        mockFakeStore.expectedMinimumMessageCount(1);
        mockFakeStore.whenAnyExchangeReceived(e -> {
            throw new java.util.concurrent.TimeoutException("Simulated timeout failure");
        });

        ResponseEntity<ProblemDetail> response = restTemplate.getForEntity("/api/v1/products/1/base-price", ProblemDetail.class);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());

        mockFakeStore.assertIsSatisfied();
    }
}
