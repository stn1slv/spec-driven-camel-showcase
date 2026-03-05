package com.example.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application configuration properties.
 */
@ConfigurationProperties(prefix = "app")
public record AppConfig(
    Fakestore fakestore,
    ExchangeRate exchangerate,
    Shippo shippo,
    BasePrice baseprice
) {
  /**
   * Fakestore API configuration.
   */
  public record Fakestore(String baseUrl) {}

  /**
   * Exchange Rate API configuration.
   */
  public record ExchangeRate(String baseUrl) {}

  /**
   * Shippo API configuration.
   */
  public record Shippo(String baseUrl) {}

  /**
   * Base Price API configuration.
   */
  public record BasePrice(String baseUrl) {}
}
