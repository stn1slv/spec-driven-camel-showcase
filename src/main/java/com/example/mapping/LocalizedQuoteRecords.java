package com.example.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data records for localized quotes and downstream API responses.
 */
public final class LocalizedQuoteRecords {

  private LocalizedQuoteRecords() {
    // Utility class
  }

  /**
   * Final aggregated quote response.
   */
  public record LocalizedOrderQuote(
      Integer productId,
      Float convertedPrice,
      String currency,
      Float shippingCost,
      Integer estimatedDays
  ) {}

  /**
   * Response from Base Price API.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record BasePriceResponse(
      Integer productId,
      Float basePrice,
      String currency
  ) {}

  /**
   * Response from Exchange Rate API.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class ExchangeRateResponse {
    private final String base;
    private final Map<String, Float> rates;

    /**
     * Constructor for ExchangeRateResponse.
     *
     * @param base Base currency.
     * @param rates Map of exchange rates.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Defensive copy used")
    public ExchangeRateResponse(
        @JsonProperty("base") String base,
        @JsonProperty("rates") Map<String, Float> rates) {
      this.base = base;
      this.rates = rates != null ? Collections.unmodifiableMap(new HashMap<>(rates)) : null;
    }

    public String base() {
      return base;
    }

    /**
     * Returns an unmodifiable view of the rates map.
     *
     * @return Map of exchange rates.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Unmodifiable view returned")
    public Map<String, Float> rates() {
      return rates;
    }
  }

  /**
   * Response from Shipping API.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ShippingQuoteResponse(
      String amount,
      @JsonProperty("estimated_days")
      Integer estimatedDays
  ) {}
}
