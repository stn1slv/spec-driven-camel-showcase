# Research: Localized Quote

## Technical Context Unknowns Resolution

### Decision: Camel `multicast` for Parallel Orchestration (Scatter-Gather)
- **Rationale**: The specification requires calling the Base Price service, ExchangeRate service, and Shipping service in parallel to minimize overall latency (FR-003). Camel's `multicast().parallelProcessing()` is the standard pattern for this. A timeout must be configured to satisfy the "fail fast" constraint on unavailability.
- **Alternatives considered**: Sequential processing via consecutive `toD()` calls (rejected due to latency overhead and direct violation of FR-003).

### Decision: Exception Handling & Status Codes
- **Rationale**: The spec mandates immediate failure with 504/503 for downstream timeouts/unavailability, and 400/422 for unsupported currency or unserviceable zip codes. We will use Camel's `onException` clauses for `HttpOperationFailedException` and connect/timeout exceptions to map these to the required standard error responses.
- **Alternatives considered**: Checking HTTP response codes using `.choice()` blocks (rejected as it violates Camel's internal error handling best practices; exceptions should be caught instead).

### Decision: Typed Configuration for Downstream URIs
- **Rationale**: The project constitution and best practices emphasize typed `@ConfigurationProperties` for reliable route evaluation over `@Value` or simple properties. Downstream URIs for Base Price, Exchange Rate, and Shippo will be externalized this way.
- **Alternatives considered**: Hardcoded URIs (rejected as inflexible and non-configurable per environment).

### Decision: Java Records for Data Modeling
- **Rationale**: For integration DTOs and intermediate states within the scatter-gather aggregation strategy, Java 17 Records provide built-in immutability and conciseness.
- **Alternatives considered**: Standard POJOs with Lombok (rejected in favor of native Java 17 records which align with modern guidelines).