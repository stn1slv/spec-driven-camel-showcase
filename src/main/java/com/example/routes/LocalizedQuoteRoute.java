package com.example.routes;

import com.example.api.AppConfig;
import com.example.mapping.LocalizedQuoteRecords.BasePriceResponse;
import com.example.mapping.LocalizedQuoteRecords.ExchangeRateResponse;
import com.example.mapping.LocalizedQuoteRecords.LocalizedOrderQuote;
import com.example.mapping.LocalizedQuoteRecords.ShippingQuoteResponse;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

/**
 * Route for generating localized product quotes.
 */
@Component
public class LocalizedQuoteRoute extends RouteBuilder {

  private final AppConfig appConfig;

  public LocalizedQuoteRoute(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @Override
  public void configure() throws Exception {
    from("direct:localized-quote")
        .routeId("localized-quote-route")
        .routeConfigurationId("global-error")
        .process(exchange -> {
          String targetCurrency = exchange.getIn().getHeader("targetCurrency", String.class);
          String destinationZip = exchange.getIn().getHeader("destinationZip", String.class);
          Integer productId = exchange.getIn().getHeader("productId", Integer.class);

          if (productId == null) {
            throw new IllegalArgumentException("productId is required");
          }
          if (targetCurrency == null || !targetCurrency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("targetCurrency must be a valid 3-letter code");
          }
          if (destinationZip == null || destinationZip.trim().isEmpty()) {
            throw new IllegalArgumentException("destinationZip is required");
          }
        })
        .removeHeaders("CamelHttp*")
        .removeHeader("Accept-Encoding")
        .multicast(new QuoteAggregationStrategy())
        .parallelProcessing()
        .timeout(10000)
        .shareUnitOfWork()
        .stopOnException()
        .to("direct:call-base-price", "direct:call-exchange-rate", "direct:call-shippo")
        .end()
        .process(exchange -> {
          @SuppressWarnings("unchecked")
          Map<String, Object> results = exchange.getIn().getBody(Map.class);

          BasePriceResponse basePrice = (BasePriceResponse) results.get("basePrice");
          ExchangeRateResponse exchangeRate = (ExchangeRateResponse) results.get("exchangeRate");
          ShippingQuoteResponse shipping = (ShippingQuoteResponse) results.get("shipping");
          String targetCurrency = exchange.getIn().getHeader("targetCurrency", String.class);

          if (basePrice == null || exchangeRate == null || shipping == null) {
            throw new IllegalStateException("Missing one or more downstream responses");
          }

          Map<String, Float> rates = exchangeRate.rates();
          Float rate = rates != null ? rates.get(targetCurrency) : null;
          if (rate == null) {
            throw new HttpOperationFailedException("http://exchangerate", 422,
                "Unsupported currency", null, null, "");
          }

          float convertedVal = basePrice.basePrice() * rate;
          Float convertedPrice = Math.round(convertedVal * 100.0f) / 100.0f;
          float shipVal = Float.parseFloat(shipping.amount());
          Float shipCost = Math.round(shipVal * 100.0f) / 100.0f;

          LocalizedOrderQuote quote = new LocalizedOrderQuote(
              basePrice.productId(),
              convertedPrice,
              targetCurrency,
              shipCost,
              shipping.estimatedDays()
          );
          exchange.getIn().setBody(quote);
        })
        .removeHeaders("*", "CamelHttpResponseCode");

    from("direct:call-base-price")
        .routeId("call-base-price")
        .routeConfigurationId("global-error")
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .circuitBreaker().id("baseprice").inheritErrorHandler(true)
          .toD(appConfig.baseprice().baseUrl()
              + "/v1/products/${header.productId}/base-price"
              + "?connectionTimeout=5000&socketTimeout=5000")
          .unmarshal().json(org.apache.camel.model.dataformat.JsonLibrary.Jackson,
              BasePriceResponse.class)
        .end();

    from("direct:call-exchange-rate")
        .routeId("call-exchange-rate")
        .routeConfigurationId("global-error")
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .circuitBreaker().id("exchangerate").inheritErrorHandler(true)
          .toD(appConfig.exchangerate().baseUrl()
              + "/v4/latest/USD?connectionTimeout=5000&socketTimeout=5000")
          .unmarshal().json(org.apache.camel.model.dataformat.JsonLibrary.Jackson,
              ExchangeRateResponse.class)
        .end();

    from("direct:call-shippo")
        .routeId("call-shippo")
        .routeConfigurationId("global-error")
        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
        .process(exchange -> {
          String zip = exchange.getIn().getHeader("destinationZip", String.class);
          exchange.getIn().setBody(String.format("{\"address_to\": {\"zip\": \"%s\"}}", zip));
          exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
        })
        .circuitBreaker().id("shippo").inheritErrorHandler(true)
          .toD(appConfig.shippo().baseUrl()
              + "/shipments/?connectionTimeout=5000&socketTimeout=5000")
          .unmarshal().json(org.apache.camel.model.dataformat.JsonLibrary.Jackson,
              ShippingQuoteResponse.class)
        .end();
  }

  private static class QuoteAggregationStrategy implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
      Object body = newExchange.getIn().getBody();
      Map<String, Object> results;

      if (oldExchange == null) {
        results = new HashMap<>();
        oldExchange = newExchange;
      } else {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = oldExchange.getIn().getBody(Map.class);
        results = map;
      }

      if (body instanceof BasePriceResponse) {
        results.put("basePrice", body);
      } else if (body instanceof ExchangeRateResponse) {
        results.put("exchangeRate", body);
      } else if (body instanceof ShippingQuoteResponse) {
        results.put("shipping", body);
      }

      oldExchange.getIn().setBody(results);
      return oldExchange;
    }
  }
}
