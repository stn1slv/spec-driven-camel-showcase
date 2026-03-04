# Research: Base Price Retrieval

## Decision: Technology Stack Alignment
- **Decision**: Java 17, Spring Boot 3.5.11, Apache Camel 4.14.5.
- **Rationale**: Requested by the user and aligns with the project's development guidelines.
- **Alternatives considered**: None (user-specified).

## Decision: Declarative Mapping via JSONata
- **Decision**: Use `camel-jsonata` for data transformation.
- **Rationale**: Mandated by project guidelines for JSON-to-JSON transformations to reduce imperative Java boilerplate. Bypasses intermediate POJO mapping.
- **Alternatives considered**: Java `Processor` (BasePriceMapper). Rejected in favor of declarative mapping.

## Decision: Error Handling (RFC 9457)
- **Decision**: Implement a global `onException` handler that maps `HttpOperationFailedException` and other errors to a `ProblemDetail` object (standard in Spring Boot 3+ / RFC 9457).
- **Rationale**: Mandatory requirement in the feature specification (FR-008).
- **Alternatives considered**: Simple JSON map. Rejected as it violates the specification.

## Decision: Testing Strategy (Advice Once)
- **Decision**: Use `AdviceWith` to mock the `FakeStoreAPI` endpoint using the "Advice Once" pattern in `OptimizedRouteTest` style.
- **Rationale**: Mandatory requirement in the Constitution (Core Principle VIII) to avoid performance degradation from `@DirtiesContext`.
- **Alternatives considered**: `@DirtiesContext`. Rejected for performance reasons.
