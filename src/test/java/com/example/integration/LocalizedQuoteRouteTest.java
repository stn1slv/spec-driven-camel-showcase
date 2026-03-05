package com.example.integration;

import com.example.mapping.LocalizedQuoteRecords.LocalizedOrderQuote;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.apache.camel.http.base.HttpOperationFailedException;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizedQuoteRouteTest extends BaseIntegrationTest {

    @EndpointInject("mock:base-price")
    private MockEndpoint mockBasePrice;

    @EndpointInject("mock:exchange-rate")
    private MockEndpoint mockExchangeRate;

    @EndpointInject("mock:shippo")
    private MockEndpoint mockShippo;

    @BeforeEach
    public void setup() throws Exception {
        mockBasePrice.reset();
        mockExchangeRate.reset();
        mockShippo.reset();
    }

    @Test
    public void testHappyPath() throws Exception {
        mockBasePrice.whenAnyExchangeReceived(e -> e.getMessage().setBody("{\"productId\": 1, \"basePrice\": 100.0, \"currency\": \"USD\"}"));
        mockExchangeRate.whenAnyExchangeReceived(e -> e.getMessage().setBody("{\"base\": \"USD\", \"rates\": {\"EUR\": 0.95}}"));
        mockShippo.whenAnyExchangeReceived(e -> e.getMessage().setBody("{\"amount\": \"15.00\", \"estimated_days\": 3}"));

        ResponseEntity<LocalizedOrderQuote> response = restTemplate.getForEntity("/api/v1/products/1/localized-quote?targetCurrency=EUR&destinationZip=21000", LocalizedOrderQuote.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().productId());
        assertEquals(95.0f, response.getBody().convertedPrice());
        assertEquals("EUR", response.getBody().currency());
        assertEquals(15.0f, response.getBody().shippingCost());
        assertEquals(3, response.getBody().estimatedDays());
    }

    @Test
    public void testBadRequestInvalidCurrency() throws Exception {
        ResponseEntity<com.example.mapping.ProblemDetail> response = restTemplate.getForEntity("/api/v1/products/1/localized-quote?targetCurrency=euro&destinationZip=21000", com.example.mapping.ProblemDetail.class);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
    }

    @Test
    public void testDownstreamFailure() throws Exception {
        mockBasePrice.whenAnyExchangeReceived(e -> {
            throw new HttpOperationFailedException("http://baseprice", 500, "Server Error", null, null, "");
        });
        mockExchangeRate.whenAnyExchangeReceived(e -> e.getMessage().setBody("{\"base\": \"USD\", \"rates\": {\"EUR\": 0.95}}"));
        mockShippo.whenAnyExchangeReceived(e -> e.getMessage().setBody("{\"amount\": \"15.00\", \"estimated_days\": 3}"));

        ResponseEntity<com.example.mapping.ProblemDetail> response = restTemplate.getForEntity("/api/v1/products/1/localized-quote?targetCurrency=EUR&destinationZip=21000", com.example.mapping.ProblemDetail.class);

        assertEquals(502, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().status());
    }
}