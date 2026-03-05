# Tasks: Localized Quote

**Input**: Design documents from `/specs/002-localized-quote/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml

**Organization**: Tasks are grouped by flow/story to enable independent implementation and testing.

## Format: `[ID] [P?] [Flow/Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which flow this task belongs to (e.g., Flow1)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and base configuration for downstream systems.

- [x] T001 Update `src/main/resources/application.yml` with downstream URIs for ExchangeRate API and Shippo API.
- [x] T002 [P] Create typed configuration properties `src/main/java/com/example/api/AppConfig.java` to include ExchangeRate and Shippo endpoints.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY flow can be implemented.

- [x] T003 Update global error handling in `src/main/java/com/example/routes/GlobalErrorRoute.java` to handle timeouts, connectivity exceptions, and payload validation failures mapping to 503/504, 400/422, and map malformed/missing downstream responses to a 500 Internal Server Error with warning logs.
- [x] T004 Configure Circuit Breakers (Resilience4j) for the downstream API calls to satisfy the Design for Failure constitution principle.

**Checkpoint**: Error handling mechanisms and circuit breakers are robust.

---

## Phase 3: Flow 1 - Generate Localized Order Quote (Priority: P1) 🎯 MVP

**Goal**: Implement the synchronous composite API to calculate a localized quote based on Base Price, Exchange Rate, and Shipping API.

**Independent Test**: Can be tested by invoking the localized-quote endpoint with a valid product ID, currency, and destination zip, expecting a fully mapped `LocalizedOrderQuote`. Includes testing error mappings for invalid inputs and downstream failures.

### Tests for Flow 1 ⚠️

- [x] T005 [P] [Flow1] Create Camel Route integration test using Advice Once pattern in `src/test/java/com/example/integration/LocalizedQuoteRouteTest.java` covering the happy path, Bad Requests (400/422), and Downstream Failures (5xx).

### Implementation for Flow 1

- [x] T006 [P] [Flow1] Create DTO records (`LocalizedOrderQuote`, `ExchangeRateResponse`, `ShippingQuoteResponse`) in `src/main/java/com/example/mapping/LocalizedQuoteRecords.java`.
- [x] T007 [Flow1] Define the REST endpoint in `src/main/java/com/example/api/LocalizedQuoteApi.java` mapped to `/v1/products/{productId}/localized-quote`.
- [x] T008 [Flow1] Create the Camel route `src/main/java/com/example/routes/LocalizedQuoteRoute.java` using the `multicast().parallelProcessing()` Scatter-Gather pattern and circuit breakers.
- [x] T009 [Flow1] Add payload aggregation logic to combine the responses from Base Price, Exchange Rate, and Shippo APIs, explicitly multiplying the base price by the exchange rate.
- [x] T010 [Flow1] Implement parameter validation (Currency format, missing params) inside the route and map to 400 Bad Request.
- [x] T011 [Flow1] Apply header sanitization (e.g. `Accept-Encoding`) before each downstream call and prior to the client response.

**Checkpoint**: At this point, Flow 1 should be fully functional and testable independently.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect the application as a whole.

- [x] T012 [P] Ensure timeouts are explicitly set on HTTP component calls in the route.
- [x] T013 [P] Verify `LocalizeQuoteRouteTest` passes without recreating the Spring context (`@DirtiesContext` must not be used).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Must run first to ensure properties are available.
- **Foundational (Phase 2)**: Depends on Setup, blocks the actual Flow implementation.
- **Flows (Phase 3+)**: Depends on Phase 2. Core implementation sequence: DTOs -> Endpoint -> Route -> Aggregation.
- **Polish (Final Phase)**: Runs last to verify rules.

### Parallel Opportunities

- The creation of DTO records and testing scaffold can occur in parallel to REST endpoint definition.
- Typed configurations and error route updates can be implemented in parallel.

### Implementation Strategy
The initial MVP will focus entirely on Flow 1. Once all tasks under Phase 3 are complete, the endpoint will be functionally complete and satisfy the requirements.
