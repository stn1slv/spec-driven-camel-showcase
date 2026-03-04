# intg-ssd-demo Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-03

## Active Technologies

- **Java**: 17+ (LTS)
- **Spring Boot**: 3.5.11
- **Apache Camel**: 4.14.5

## Commands

- `mvn clean test`: Run all tests.
- `mvn spring-boot:run`: Start the application locally.
- `mvn checkstyle:check spotbugs:check`: Run quality gates.

## Known Issues & Gotchas

### ⚠️ [Camel Simple Expressions in `.throwException()`]

**Issue:** Exception message variables (e.g., `${header.id}`) are not evaluated, resulting in literal strings in the error response.
**Root Cause:** Passing an instantiated exception `new MyException("ID ${header.id}")` does not trigger Camel's dynamic expression evaluation.
**Prevention Rule:** Always pass the exception class and the message string separately: `.throwException(MyException.class, "ID ${header.id}")`.

### ⚠️ [HTTP Component Response Codes in `onException` Unit Tests]

**Issue:** Unit tests expecting a specific HTTP status code (e.g., 404) fail, returning 500 instead.
**Root Cause:** Setting the HTTP response code outside the `process` block (using `.setHeader(...)` at the end of the `onException` chain) fails to propagate back to the `ProducerTemplate` in `CamelSpringBootTest` mocks.
**Prevention Rule:** Explicitly set the HTTP response code directly on the `exchange.getMessage()` inside the `process` block within `onException`: `exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);`.

### ⚠️ [Camel REST Servlet Dependency Missing]

**Issue:** Spring Boot application fails to start with `FailedToStartRouteException: No bean could be found in the registry for: servlet of type: org.apache.camel.spi.RestApiConsumerFactory`.
**Root Cause:** Missing the explicit dependency for the servlet component when `restConfiguration().component("servlet")` is used.
**Prevention Rule:** Ensure `camel-servlet-starter` is included in `pom.xml` whenever exposing REST APIs via the servlet component in Spring Boot.

### ⚠️ [Spring Boot Properties vs Camel Simple]

**Issue:** `IllegalArgumentException: Property with key [...] not found` when using `simple("${properties:...}")`.
**Root Cause:** Camel's property placeholder component may fail to resolve properties defined only via Spring's `@Value` annotation or if the property context isn't fully synchronized.
**Prevention Rule:** To evaluate application properties dynamically in a `.choice()`, prefer using typed `@ConfigurationProperties` records directly via `constant(config.myProperty())` rather than relying on `@Value` or `simple("${properties:...}")`.

### ⚠️ [Content Negotiation / Encoding Traps]

**Issue:** Clients receive unreadable responses, e.g., `ContentDecodingError: ('Received response with content-encoding: zstd...')`.
**Root Cause:** Camel propagates ingress headers (like `Accept-Encoding`) to downstream systems, and then forwards the downstream's compressed response (like `Content-Encoding: zstd`) back to the caller without uncompressing it.
**Prevention Rule:** Always sanitize ingress and egress headers. Strip `Accept-Encoding` before calling external APIs, and strip `Content-Encoding`, `Transfer-Encoding`, and `Server` from the response before returning to the caller.

### ⚠️ [Unit Test Performance and Isolation (@DirtiesContext)]

**Issue:** Unit tests run very slowly and may experience context-related errors when re-advising routes.
**Root Cause:** Overuse of `@DirtiesContext` which forces context recreation. Re-advising a route that has already been started in a shared context can cause conflicts.
**Prevention Rule:** Avoid `@DirtiesContext`. Use the **"Advice Once"** pattern: manually reset mocks in `@BeforeEach` and only apply `AdviceWith` if the `CamelContext` is not already started. Ensure mocks are reset to maintain isolation while reusing the context.

