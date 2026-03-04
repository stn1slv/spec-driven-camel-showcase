# spec-driven-camel-showcase

> **A demonstration of Spec-Driven Development (SDD) patterns using Apache Camel and Spring Boot.**

This repository serves as a reference implementation for "Contract-First" integration workflows. It demonstrates how to use specifications (such as OpenAPI or AsyncAPI) as the single source of truth to drive the implementation, validation, and testing of integration routes.

---

### ⚠️ Disclaimer
**This project is a non-production demo.** It is intended for educational purposes and proof-of-concept demonstrations. It has not been hardened for production environments and should not be used in a live setting.

### 🎯 Overview
The core philosophy of this showcase is to minimize "integration drift" by ensuring that the implementation remains strictly aligned with the agreed-upon contract.

**Key Patterns Demonstrated:**
*   **Contract-First Workflow**: Designing specifications before writing integration logic.
*   **Code Generation**: Generating boilerplate and DTOs from specifications to ensure consistency.
*   **Edge Validation**: Automated validation of messages against the contract at the entry points.
*   **Type-Safe Orchestration**: Utilizing modern Java and Camel features for robust, predictable data handling.

### 🛠️ Technology Stack
*   **Integration Engine**: Apache Camel
*   **Application Framework**: Spring Boot
*   **Metadata/Specs**: OpenAPI, AsyncAPI
*   **Build System**: Maven

### 🏷️ Showcase Workflow & Tags

This repository is designed to be an interactive tutorial. The commit history is organized chronologically using Git tags to demonstrate the Spec-Driven Development (SDD) process step-by-step. 

You can checkout specific tags to see the exact state of the project at various stages of an integration's lifecycle.

#### Global Repository Tags
These tags represent the foundational state of the entire repository before specific integration cases are implemented.

* **[`00-initial`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/00-initial)**: The starting point. Contains only the repository structure, Spec-Kit templates, and scripts.
* **[`01-constitution`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/01-constitution)**: The baseline architecture. Contains the core Spring Boot/Camel skeleton and global guidelines (e.g., [`GEMINI.md`](./GEMINI.md)) generated during the constitution phase.

#### Case-Specific Progress Tags
As integration cases are developed (e.g., [`i.001`](./docs/i.001.md), `i.002`), their progress is tracked using namespaced tags mapping to the SDD phases.

For example, the workflow for case **[`i.001`](./docs/i.001.md)** follows:
* **[`i.001/02-specify`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/i.001/02-specify)**: The functional specification ([`docs/i.001.md`](./docs/i.001.md)) is defined.
* **[`i.001/03-clarify`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/i.001/03-clarify)**: (Optional) Q&A and clarifications are documented.
* **[`i.001/04-plan`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/i.001/04-plan)**: The technical design and implementation plan are created.
* **[`i.001/05-tasks`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/i.001/05-tasks)**: The implementation tasks are broken down.
* **[`i.001/06-analyze`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/i.001/06-analyze)**: (Optional) Implementation analysis results.
* **[`i.001/07-implement`](https://github.com/stn1slv/spec-driven-camel-showcase/tree/i.001/07-implement)**: The completed, working code (Camel routes, tests, etc.) for this specific case.

*Tip: Use `git tag -l "i.001/*"` to view the progress of a specific integration case.*

#### Commit Conventions
This project follows the [Conventional Commits](https://www.conventionalcommits.org/) format. 
* **Global changes** (like the constitution) use standard types without scopes (e.g., `build: generate project constitution...`).
* **Case-specific changes** include the case ID in the scope (e.g., `docs(i.001): add functional specification...` or `feat(i.001): implement Base Price Retrieval interface`) to clearly delineate exactly where the work was applied.