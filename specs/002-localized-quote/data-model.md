# Data Model: Localized Quote

## Entities

### Product Quote Request
This entity represents the incoming parameters from the E-commerce platform.

- **Fields**:
  - `productId` (Integer): The unique identifier for the product.
  - `targetCurrency` (String): The 3-letter ISO 4217 currency code for conversion (e.g., "EUR").
  - `destinationZip` (String): The postal code for shipping cost calculation.

- **Validation Rules**:
  - `targetCurrency` must be exactly 3 uppercase letters.
  - All fields are required.

### Localized Order Quote
This entity represents the final aggregated response sent back to the E-commerce platform.

- **Fields**:
  - `productId` (Integer): The original product identifier.
  - `convertedPrice` (Number / Float): The base price converted to the target currency, formatted to 2 decimals.
  - `currency` (String): The target currency code.
  - `shippingCost` (Number / Float): The cost of shipping based on the destination zip code, formatted to 2 decimals.
  - `estimatedDays` (Integer): The estimated delivery time in days.

### Intermediate DTOs (For Internal Aggregation)
To support the Scatter-Gather pattern, we need structures to hold the results of the parallel downstream calls:
- **BasePriceResponse**: Holds `basePrice` (Float) and `currency` (String, usually USD).
- **ExchangeRateResponse**: Holds the mapping of target currency to its multiplier rate (Float).
- **ShippingQuoteResponse**: Holds the `amount` (String to be parsed to Float) and `estimated_days` (Integer).