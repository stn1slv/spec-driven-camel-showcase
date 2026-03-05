# Implementation Plan: Localized Quote

**Branch**: `002-localized-quote` | **Date**: March 5, 2026 | **Spec**: [Link to Spec](./spec.md)
**Input**: Feature specification from `/specs/002-localized-quote/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implement a synchronous composite API to generate a localized order quote. The integration layer will use a Scatter-Gather pattern to orchestrate requests in parallel to the Base Price, ExchangeRate, and Shippo APIs. The system will handle downstream unavailability with standard 5xx errors and provide 4xx responses for invalid input paths (unsupported currency or unserviceable zip codes). The final response is an aggregated quote with a converted base price and calculated shipping constraints.

## Technical Context

**Language/Version**: Java 17
**Spring Boot Version**: 3.5.11
**Apache Camel Version**: 4.14.5
**Other Dependencies**: `camel-servlet-starter` (REST), `camel-jackson` (Mapping), `camel-http` (Downstream Calls)
**Transport**: HTTPS (REST API)
**Testing**: JUnit 5, Camel Test Spring Boot ("Advice Once" Pattern)
**Integration Type**: Synchronous API Composite (Scatter-Gather)
**Performance Goals**: Minimize latency via parallel downstream calls (`multicast().parallelProcessing()`).
**Constraints**: Fail-fast on downstream failures (no retries for connectivity exceptions), fail-fast on malformed downstream payloads.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Contract-Driven**: Is the OpenAPI/AsyncAPI spec defined and linked? (Yes, defined in `contracts/openapi.yaml`)
- [x] **Failure Design**: Are timeouts, retries, and circuit breakers planned for all external calls? (Yes, explicitly configured timeouts; spec dictates NO retries on failures to fail-fast with 5xx).
- [x] **Dynamic Safety**: Does error handling use `.throwException(Class, String)` to support Simple expressions? (Yes, error handling strategy requires this).
- [x] **Header Sanitization**: Is ingress/egress header stripping planned for all HTTP boundaries? (Yes, `Accept-Encoding` and other problematic headers will be stripped to prevent content negotiation traps).
- [x] **Testing Strategy**: Does the test plan follow the "Advice Once" pattern to avoid `@DirtiesContext`? (Yes).

## Project Structure

### Documentation (this feature)

```text
specs/002-localized-quote/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (OpenAPI/AsyncAPI definitions)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── api/                 # REST configuration & Camel REST DSL
│   │       ├── routes/              # Main RouteBuilder & Sub-routes
│   │       └── mapping/             # Java Records / DTOs
│   └── resources/
│       └── application.yml          # Typed configuration for downstream URIs
tests/
└── test/
    └── java/
        └── com/example/
            └── integration/         # Advice Once tests
```

**Structure Decision**: Using the Synchronous API Integration structure, heavily relying on RouteBuilders and Java records for intermediate DTOs during aggregation.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
| --- | --- | --- |
| None | N/A | N/A |