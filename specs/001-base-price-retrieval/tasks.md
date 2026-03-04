---
description: "Task list for integration implementation"
---

# Tasks: Base Price Retrieval

**Input**: Design documents from `/specs/001-base-price-retrieval/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by flow/story to enable independent implementation and testing.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create project structure per implementation plan
- [x] T002 Initialize `pom.xml` with Spring Boot 3.5.11, Apache Camel 4.14.5, and required dependencies (`servlet`, `http`, `jackson`, `test-spring-junit5`, `resilience4j`)
- [x] T003 [P] Configure `application.yml` and typed `@ConfigurationProperties` for external URLs, context path, and Resilience4j settings

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY flow can be implemented

**⚠️ CRITICAL**: No flow implementation can begin until this phase is complete

- [x] T004 Configure Camel REST DSL to use servlet component with json binding in `src/main/java/com/example/api/RestConfig.java`
- [x] T005 [P] Setup global `onException` handler for RFC 9457 Problem Details formatting in `src/main/java/com/example/routes/GlobalErrorRoute.java`

**Checkpoint**: Foundation ready - flow implementation can now begin

---

## Phase 3: Flow 1 - Base Price Retrieval (Priority: P1) 🎯 MVP

**Goal**: Retrieve and format product base prices from FakeStoreAPI, exposing a REST endpoint.

**Independent Test**: Send a GET request to `/v1/products/1/base-price` and verify that the response contains `productId: 1`, a numeric `basePrice`, and `currency: "USD"`.

### Tests for Flow 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T006 [P] [Flow1] Create Camel Route integration test utilizing "Advice Once" pattern (no `@DirtiesContext`) in `src/test/java/com/example/integration/BasePriceRouteTest.java`

### Implementation for Flow 1

- [x] T007 [P] [Flow1] Define Source Product and Target Base Price Java records in `src/main/java/com/example/mapping/`
- [x] T008 [P] [Flow1] Define ProblemDetail Java record for error representation in `src/main/java/com/example/mapping/ProblemDetail.java`
- [x] T009 [Flow1] Define REST DSL endpoint `GET /v1/products/{productId}/base-price` with positive integer validation for `productId` (FR-002) in `src/main/java/com/example/api/BasePriceApi.java`
- [x] T010 [Flow1] Implement data mapping processor from FakeStore to BasePrice in `src/main/java/com/example/mapping/BasePriceMapper.java`
- [x] T011 [Flow1] Implement main Camel route orchestrating FakeStoreAPI call using Resilience4j circuit breaker in `src/main/java/com/example/routes/BasePriceRoute.java`
- [x] T012 [Flow1] Add ingress and egress header sanitization (strip `Accept-Encoding`, `Content-Encoding`, `Server`) in `src/main/java/com/example/routes/BasePriceRoute.java`
- [x] T013 [Flow1] Implement specific error handling (404, 504, 502 Bad Gateway) with explicit `.throwException()` and status assignments in `src/main/java/com/example/routes/BasePriceRoute.java`

**Checkpoint**: At this point, Flow 1 should be fully functional and testable independently

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple flows

- [x] T014 [P] Expose and test the defined OpenAPI specifications from `contracts/`
- [x] T015 Verify timeout and connection configurations on all Camel components (`connectionTimeout`, `socketTimeout`)
- [x] T016 Audit all boundaries for missing header sanitization
- [x] T017 Verify all tests use "Advice Once" and don't trigger context recreation
- [x] T018 Conduct load/performance testing to verify 5 TPS throughput and < 50ms processing overhead (SC-003, SC-004)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all flows
- **Flows (Phase 3+)**: Depend on Foundational phase completion.
- **Polish (Final Phase)**: Depends on all desired flows being complete

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, mapping records, error models, and initial test setup in Phase 3 can run in parallel.
