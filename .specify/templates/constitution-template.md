 [PROJECT_NAME] Constitution

## Core Principles

### I. Contract-Driven Development
All integrations must be defined by a clear contract (OpenAPI for synchronous, AsyncAPI for asynchronous) before any implementation begins. The code must strictly match the contract.

### II. Design for Failure (NON-NEGOTIABLE)
Assume network components and downstream systems will experience downtime. Implement appropriate timeouts, automated retries, and circuit breakers.

### III. Idempotency is Mandatory
For asynchronous messaging and retry scenarios, the receiving system must be able to process the same payload multiple times without causing duplicate records or data corruption.

### IV. Protect System Limits
Always respect the rate limits and API quotas of external and COTS systems (e.g., Salesforce, SAP) to prevent account lockouts and degraded performance. Use batching where necessary.

### V. Never Lose Data
If a payload cannot be processed after all automated retries are exhausted, it must be routed to a Dead Letter Queue (DLQ) or an error database for manual review.

## Architecture Standards

[Specify required integration patterns, e.g., standard error response format, logging formats, correlation ID propagation]

## Quality Gates

[Specify testing requirements, e.g., Contract tests must pass, end-to-end flow must be verified in lower environments]

## Governance

All Pull Requests must verify compliance with these integration principles. Any deviations (e.g., skipping a DLQ for non-critical logging events) must be explicitly documented and approved.

**Version**: [CONSTITUTION_VERSION] | **Ratified**: [RATIFICATION_DATE] | **Last Amended**: [LAST_AMENDED_DATE]
