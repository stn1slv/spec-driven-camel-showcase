# Quickstart: Base Price Retrieval

## Run the Application
```bash
mvn spring-boot:run
```
The server starts on port `8080` (default Spring Boot port).

## Test the API
Retrieve base price for product ID 1:
```bash
curl -X GET "http://localhost:8080/api/v1/products/1/base-price"
```
Response:
```json
{
  "productId": 1,
  "basePrice": 109.95,
  "currency": "USD"
}
```

## Run Tests
Execute the unit and integration tests:
```bash
mvn test
```

## Quality Gates
Run checkstyle and spotbugs:
```bash
mvn checkstyle:check spotbugs:check
```
