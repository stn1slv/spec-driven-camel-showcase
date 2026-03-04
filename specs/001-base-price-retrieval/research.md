# Research: Base Price Retrieval

## Decision: Technology Stack Alignment
- **Decision**: Java 17, Spring Boot 3.5.11, Apache Camel 4.14.5.
- **Rationale**: Requested by the user and aligns with the project's development guidelines.
- **Alternatives considered**: None (user-specified).

## Decision: API Exposure via Camel REST DSL (Servlet Component)
- **Decision**: Use Camel REST DSL with the `servlet` component.
- **Rationale**: The Constitution (Architecture Standards) explicitly requires `camel-servlet-starter` for REST APIs. It provides a standardized integration path within Spring Boot.
- **Alternatives considered**: Spring `@RestController`. Rejected to maintain integration logic entirely within Camel's domain.

## Decision: Error Handling (RFC 9457)
- **Decision**: Implement a global `onException` handler that maps `HttpOperationFailedException` and other errors to a `ProblemDetail` object (standard in Spring Boot 3+ / RFC 9457).
- **Rationale**: Mandatory requirement in the feature specification (FR-008).
- **Alternatives considered**: Simple JSON map. Rejected as it violates the specification.

## Decision: Testing Strategy (Advice Once)
- **Decision**: Use `AdviceWith` to mock the `FakeStoreAPI` endpoint using the "Advice Once" pattern in `OptimizedRouteTest` style.
- **Rationale**: Mandatory requirement in the Constitution (Core Principle VIII) to avoid performance degradation from `@DirtiesContext`.
- **Alternatives considered**: `@DirtiesContext`. Rejected for performance reasons.
