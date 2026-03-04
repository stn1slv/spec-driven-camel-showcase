# Implementation Plan: Base Price Retrieval

**Branch**: `001-base-price-retrieval` | **Date**: 2026-03-04 | **Spec**: [specs/001-base-price-retrieval/spec.md]
**Input**: Feature specification from `/specs/001-base-price-retrieval/spec.md`

## Summary
Retrieve and format product base prices from FakeStoreAPI. The integration exposes a REST endpoint via Camel REST DSL and maps the catalog price to a standardized internal format in USD.

## Technical Context

**Language/Version**: Java 17
**Spring Boot Version**: 3.5.11
**Apache Camel Version**: 4.14.5
**Other Dependencies**: `camel-servlet-starter`, `camel-http-starter`, `camel-jackson-starter`, `camel-test-spring-junit5`, `camel-resilience4j`
**Transport**: HTTPS (JSON over HTTP)
**Testing**: JUnit 5, MockEndpoint (Advice Once pattern)
**Integration Type**: Synchronous API
**Performance Goals**: 5 TPS sustained, < 50ms internal overhead
**Constraints**: RFC 9457 (Problem Details) error handling, No caching, Resilience4j Circuit Breaker required for FakeStoreAPI

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Contract-Driven**: OpenAPI specs for sender and receiver are defined in `specs/001-base-price-retrieval/contracts/`.
- [x] **Failure Design**: Timeouts, retries, and Resilience4j circuit breakers are planned using `onException` and `ProblemDetail`.
- [x] **Dynamic Safety**: Error handling will use `.throwException(Class, String)` to support Simple expressions.
- [x] **Header Sanitization**: `Accept-Encoding` and other ingress/egress headers will be stripped at boundaries.
- [x] **Testing Strategy**: The test plan follows the "Advice Once" pattern to avoid `@DirtiesContext`.

## Project Structure

### Documentation (this feature)

```text
specs/001-base-price-retrieval/
├── plan.md              # This file
├── research.md          # Research on tech stack and error handling
├── data-model.md        # Mapping of FakeStore product to Internal price
├── quickstart.md        # Commands to run and test
├── contracts/           # OpenAPI definitions for Sender and Receiver
└── tasks.md             # Task list (to be created by /speckit.tasks)
```

### Source Code (repository root)

```text
# Using Option 1: Synchronous API Integration
src/main/java/com/example/
├── api/                 # REST DSL configuration
├── routes/              # Camel RouteBuilders (BasePriceRoute)
├── mapping/             # Product to BasePrice mapping
└── clients/             # FakeStoreAPI endpoint configuration

src/test/java/com/example/
├── contract/            # Contract tests (if applicable)
└── integration/         # Advice Once integration tests
```

**Structure Decision**: Standard Maven/Spring Boot structure with Camel-specific packages for routes and mapping.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
| --- | --- | --- |
| Missing DLQ for failed retries (Principle V) | This is a synchronous HTTP integration serving frontend clients. A DLQ is asynchronous and breaks the synchronous request/response cycle expected by the caller. | We reject a DLQ here because the calling system (E-commerce platform) needs an immediate failure response (e.g., 502/504) to handle the error in its own UI, rather than having the request silently queued for manual review. |
