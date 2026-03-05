package com.example.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
  public record ExchangeRateResponse(
      String base,
      Map<String, Float> rates
  ) {
    /**
     * Constructor with defensive copy.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Defensive copy used")
    public ExchangeRateResponse(String base, Map<String, Float> rates) {
      this.base = base;
      this.rates = rates != null ? Map.copyOf(rates) : null;
    }

    /**
     * Accessor with defensive copy.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Defensive copy used")
    @Override
    public Map<String, Float> rates() {
      return rates != null ? Map.copyOf(rates) : null;
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
