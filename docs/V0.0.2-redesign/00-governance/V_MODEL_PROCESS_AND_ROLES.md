# V-model Process and Role Allocation

Status: `PREPARED_FOR_OWNER_CONFIRMATION`  
Date: 2026-08-05 JST

## 1. Tailored lifecycle

Project Wayfarer Plugin V0.0.2 uses a V-model aligned to SWE.1 through SWE.6. The process names are
used to structure engineering work products and reviews. This repository does not claim formal
Automotive SPICE assessment or certification solely from this tailoring.

## 2. Process allocation

| Process | Purpose in this project | Author / executor | Reviewer | Approval authority |
|---|---|---|---|---|
| SWE.1 | Derive, analyze, specify, and baseline software requirements | ChatGPT | Owner | Owner |
| SWE.2 | Define and baseline software architecture and interface allocation | ChatGPT | Owner | Owner |
| SWE.3 design | Define software detailed design, unit behavior, interfaces, state transitions, and construction constraints | ChatGPT | Owner | Owner |
| SWE.3 construction | Implement approved units and produce implementation/design consistency evidence | Codex | ChatGPT | Owner when the gate affects implementation continuation |
| SWE.4 | Verify software units against detailed design and unit-verification criteria | Codex | ChatGPT | ChatGPT gate recommendation; Owner may override explicitly |
| SWE.5 | Integrate units and verify interfaces and integrated behavior | Codex | ChatGPT | ChatGPT gate recommendation; Owner may override explicitly |
| SWE.6 | Qualify the software against the approved software requirements | Codex | ChatGPT | Owner acceptance |

## 3. SWE.1 Software Requirements Analysis

ChatGPT must produce at least:

- source and authority register;
- scope and non-scope definition;
- glossary and domain terminology;
- stakeholder-to-software requirement derivation;
- uniquely identified functional and non-functional requirements;
- interface and compatibility requirements;
- failure, security, performance, persistence, and observability requirements;
- verification intent for every requirement;
- conflicts, assumptions, open decisions, and V0.0.1 dependency issues;
- requirements traceability baseline.

SWE.1 output must not prescribe classes or implementation algorithms unless a higher-authority source
mandates a specific technical constraint.

## 4. SWE.2 Software Architectural Design

ChatGPT must allocate approved requirements to architectural elements and define:

- module and component boundaries;
- static and dynamic architecture;
- runtime topology;
- public and internal interfaces;
- data ownership and persistence boundaries;
- threading and scheduling model;
- failure containment and recovery strategy;
- permission and trust boundaries;
- external integration boundaries;
- architecture-level verification criteria;
- requirement-to-architecture traceability.

Architecture must not be reverse-derived from PR #14 as an assumed solution. Existing code may be
considered only after the required architecture is established.

## 5. SWE.3 Software Detailed Design

### 5.1 ChatGPT-owned detailed design

ChatGPT must define each implementation unit with sufficient precision that Codex does not need to
invent behavior. Applicable design elements include:

- unit responsibility and boundaries;
- inputs, outputs, preconditions, and postconditions;
- state machines and transition guards;
- event priority and cancellation policy;
- command and permission behavior;
- item identity and physical-state rules;
- data structures, schemas, and mapping rules;
- synchronous and asynchronous call sequences;
- error and fallback semantics;
- logs, audit events, and sanitized Player output;
- lifecycle and restart behavior;
- unit-verification obligations;
- design identifiers used by implementation traceability.

### 5.2 Codex-owned unit construction

Codex implements only the approved detailed design. Each delivery must include a tracked consistency
record with:

- detailed-design identifier;
- production file and implementation symbol;
- test file and verification symbol where applicable;
- implementation status;
- mechanical adjustments;
- deviations or `NONE`;
- unresolved design questions;
- commit identity.

A deviation is not accepted because it compiles or passes tests. It requires an updated design or an
explicit deviation approval before the implementation gate passes.

## 6. SWE.4 Software Unit Verification

Codex performs the approved unit verification. ChatGPT reviews:

- whether tests verify the detailed design rather than mirror the implementation;
- positive, negative, boundary, and fail-closed coverage required by the design;
- determinism and isolation;
- absence of false evidence from excessive mocking or unexercised event entry points;
- requirement and design traceability;
- failures, skipped tests, limitations, and environment differences.

## 7. SWE.5 Software Integration and Integration Test

Codex integrates units in the approved order and executes interface-focused tests. ChatGPT reviews:

- integration sequence and configuration identity;
- interface contracts and data flow;
- threading, persistence, lifecycle, and dependency boundaries;
- error propagation and recovery;
- cross-module isolation;
- regression impact on V0.0.1 compatibility;
- integration-test traceability and evidence.

## 8. SWE.6 Software Qualification Test

Codex executes qualification tests derived from the SWE.1 requirements baseline. ChatGPT reviews:

- qualification environment identity;
- Product artifact identity;
- requirement coverage;
- applicability and justified exclusions;
- Client and runtime observations;
- defects and unresolved limitations;
- evidence sufficiency;
- final qualification recommendation.

Qualification is not release authorization. The Owner separately approves acceptance and release.

## 9. Prohibited role substitutions

- Codex must not fill a requirements, architecture, or detailed-design gap by inference.
- ChatGPT must not treat Codex implementation notes as approved design without independent analysis.
- Test success must not be used to retroactively define requirements.
- Existing code must not override an approved design.
- Owner approval must not be inferred from silence or from continuation of unrelated work.

## 10. Change control

A change after a baseline gate must identify:

- affected requirement or design IDs;
- reason and source;
- upstream and downstream impact;
- work products requiring revision;
- implementation and verification impact;
- regression scope;
- approval required before continuation.
