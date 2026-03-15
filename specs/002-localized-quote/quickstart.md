# Quickstart: Localized Quote

## Local Development

### Prerequisites
- Java 17
- Maven

### Running the Application

1. Start the application locally using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

2. The application will start on `http://localhost:8080`.

### Manual Testing

You can test the endpoint using `curl`. Note that this assumes mock endpoints or accessible downstream APIs are configured in `application.yml`.

```bash
curl -X GET "http://localhost:8080/v1/products/1/localized-quote?targetCurrency=EUR&destinationZip=21000"
```

Expected output (assuming Base Price is USD 100, exchange rate is 0.95, and shipping is 15.00):
```json
{
  "productId": 1,
  "convertedPrice": 95.00,
  "currency": "EUR",
  "shippingCost": 15.00,
  "estimatedDays": 3
}
```

### Quality Checks
Run tests and static analysis:
```bash
./mvnw clean test checkstyle:check spotbugs:check
```