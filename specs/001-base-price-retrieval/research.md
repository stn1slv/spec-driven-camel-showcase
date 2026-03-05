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
- **Decision**: Handle `HttpOperationFailedException` and other Camel errors inside `BasePriceRoute` using a route-scoped `onException` that maps them to a Spring `ProblemDetail` response (RFC 9457). A reusable `routeConfiguration("global-error")` is defined in `GlobalErrorRoute` for potential reuse, but is not currently applied to any routes.
- **Rationale**: Mandatory requirement in the feature specification (FR-008) to expose errors as RFC 9457-compliant `ProblemDetail` responses, while keeping error handling local to the base price retrieval flow and avoiding surprising cross-route side effects of a truly global handler.
- **Alternatives considered**: A single Camel-global `onException` and/or returning a simple JSON map. Rejected as they either violate the specification or introduce globally scoped behavior we do not currently need.

## Decision: Testing Strategy (Advice Once)
- **Decision**: Use `AdviceWith` to mock the `FakeStoreAPI` endpoint using the "Advice Once" pattern in `OptimizedRouteTest` style.
- **Rationale**: Mandatory requirement in the Constitution (Core Principle VIII) to avoid performance degradation from `@DirtiesContext`.
- **Alternatives considered**: `@DirtiesContext`. Rejected for performance reasons.
