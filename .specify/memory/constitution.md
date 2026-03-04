<!--
Sync Impact Report:
- Version change: 1.0.0 → 1.1.0
- List of modified principles:
    - Added VI. Dynamic Expression Safety
    - Added VII. Surgical Header Management
    - Added VIII. Optimized Testing Discipline
- Added sections: Architecture Standards (Header Sanitization, Typed Configuration, Dependency Rigor)
- Removed sections: None
- Templates requiring updates:
    - ✅ .specify/templates/plan-template.md (Updated Constitution Check examples)
    - ✅ .specify/templates/tasks-template.md (Added header sanitization and testing optimization tasks)
- Follow-up TODOs: None
-->
# intg-ssd-demo Constitution

## Core Principles

### I. Contract-Driven Development
All integrations must be defined by a clear contract (OpenAPI for synchronous, AsyncAPI for asynchronous) before any implementation begins. The code must strictly match the contract.

### II. Design for Failure (NON-NEGOTIABLE)
Assume network components and downstream systems will experience downtime. Implement appropriate timeouts, automated retries, and circuit breakers.

### III. Idempotency is Mandatory
For asynchronous messaging and retry scenarios, the receiving system must be able to process the same payload multiple times without causing duplicate records or data corruption.

### IV. Protect System Limits
Always respect the rate limits and API quotas of external systems to prevent account lockouts and degraded performance. Use batching where necessary.

### V. Never Lose Data
If a payload cannot be processed after all automated retries are exhausted, it must be routed to a Dead Letter Queue (DLQ) or an error database for manual review.

### VI. Dynamic Expression Safety
When throwing exceptions in Camel routes, always pass the exception class and the message string separately (e.g., `.throwException(MyException.class, "ID ${header.id}")`). Never pass an already instantiated exception, as it prevents Camel from evaluating dynamic Simple expressions.

### VII. Surgical Header Management
Always sanitize ingress and egress headers. Strip `Accept-Encoding` before calling external APIs to avoid unhandled compression. Strip `Content-Encoding`, `Transfer-Encoding`, and `Server` from responses before returning them to the caller. Explicitly set `Exchange.HTTP_RESPONSE_CODE` inside processors within `onException` blocks to ensure correct status code propagation.

### VIII. Optimized Testing Discipline
Avoid `@DirtiesContext` in unit tests as it severely degrades performance. Use the "Advice Once" pattern: manually reset mocks in `@BeforeEach` and only apply `AdviceWith` if the `CamelContext` is not already started.

## Architecture Standards

### Header Sanitization
Every route interacting with external HTTP services MUST include a sanitization step for both request (ingress) and response (egress) headers to prevent content negotiation traps and security leaks.

### Typed Configuration
Prefer Java `record`-based `@ConfigurationProperties` for dynamic route evaluation. Avoid using `simple("${properties:...}")` or `@Value` inside `.choice()` blocks to ensure reliable property resolution during route startup.

### Dependency Rigor
Explicitly include `camel-servlet-starter` in the `pom.xml` whenever exposing REST APIs via the Camel Servlet component in Spring Boot.

## Quality Gates

- **Advice Once Pattern**: All Camel unit tests must implement the Advice Once pattern to maintain isolation without context recreation.
- **Contract Compliance**: All integration flows must pass contract tests against the defined OpenAPI/AsyncAPI specifications.
- **Header Sanitization**: Code reviews must verify that `Accept-Encoding` and other sensitive headers are stripped at system boundaries.

## Governance

All Pull Requests must verify compliance with these integration principles. Any deviations must be explicitly documented and approved. The constitution is the source of truth for all architectural decisions.

**Version**: 1.1.0 | **Ratified**: 2026-03-03 | **Last Amended**: 2026-03-04
