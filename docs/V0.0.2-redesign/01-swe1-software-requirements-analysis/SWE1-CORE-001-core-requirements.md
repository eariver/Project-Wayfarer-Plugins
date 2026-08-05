# Wayfarer_Core Software Requirements

Document ID: `SWE1-CORE-001`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `CORE`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A  
Contained normative items: CAP: 2, CON: 8, IFC: 3, QLT: 1

## 1. Purpose

Define the shared Core capabilities and compatibility constraints required by the Main and Frontier targets without allocating target gameplay to Core.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-CORE-001-CAP-001 — Shared Core availability

**Normative statement:** Wayfarer_Core shall provide the shared runtime services required by approved Main and Frontier capabilities and shall be deployable on both Main and Frontier without target-specific gameplay ownership.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001; §4 CAN-COM-001  
**Rationale:** Provides a single shared foundation while preserving gameplay-plugin ownership.  
**Precondition / trigger:** When either gameplay plugin resolves a required Core service.  
**Required observable result:** The compatible service is available or the dependent capability fails closed without Core taking over target gameplay.  
**Verification intent:** SWE.5 Main-Core and Frontier-Core integration tests.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-001 — V0.0.1 public API compatibility

**Normative statement:** The accepted V0.0.1 Core public API shall remain source- and binary-compatible for existing consumers unless a separate Owner-approved baseline change explicitly authorizes incompatibility.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001  
**Rationale:** Protects the accepted baseline and parallel upgrade path.  
**Precondition / trigger:** When current artifacts are compiled and loaded against the accepted V0.0.1 contract.  
**Required observable result:** Existing public symbols and behavior required by accepted consumers remain usable.  
**Verification intent:** API compatibility analysis, binary compatibility tooling, and SWE.5 class-loading integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-002 — Additive Core extension

**Normative statement:** A Core capability needed by Main or Frontier shall be introduced through an additive public contract with explicit capability discovery and version compatibility.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001  
**Rationale:** Allows extension without silently changing baseline semantics.  
**Precondition / trigger:** When a new shared capability is required.  
**Required observable result:** Consumers can determine capability availability and fail closed when the required version/capability is absent.  
**Verification intent:** SWE.4 contract verification and SWE.5 mixed-version integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-001 — Public API implementation isolation

**Normative statement:** Core public API shall not expose Bukkit/Paper runtime objects, JDBC connections, HikariCP, Flyway, Lettuce, or other implementation-specific persistence/client types.

**Source:** SWE1-SRC-002 §5 CAN-CORE-002  
**Rationale:** Prevents consumers from coupling to runtime and storage implementation.  
**Precondition / trigger:** At public API definition and publication.  
**Required observable result:** Published signatures contain only approved domain/API types.  
**Verification intent:** Static API inspection and dependency analysis.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-002 — Single API class identity

**Normative statement:** Wayfarer API classes shall not be independently bundled into multiple runtime artifacts in a manner that creates duplicate class identity.

**Source:** SWE1-SRC-002 §5 CAN-CORE-002  
**Rationale:** Prevents service-resolution and type-cast failures.  
**Precondition / trigger:** At artifact assembly and runtime loading.  
**Required observable result:** The runtime resolves one compatible API class identity for all modules.  
**Verification intent:** Packaging inspection and SWE.5 class-loading integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CAP-002 — Waymark transaction service

**Normative statement:** Core shall expose the approved Waymark transaction boundary used by Main repair/reissue and Frontier shop operations.

**Source:** SWE1-SRC-002 §5 CAN-CORE-003  
**Rationale:** Centralizes shared economy semantics and prevents direct provider access.  
**Precondition / trigger:** When a target plugin requests an approved debit, refund, or reconciliation-capable operation.  
**Required observable result:** The request is executed through Core and returns an explicit supported disposition.  
**Verification intent:** SWE.4 service-contract verification and SWE.5 provider integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-003 — Provider acceptance semantics

**Normative statement:** Core shall represent provider acceptance only as evidence that the provider accepted the invocation and shall not claim durable completion, external effect lookup, unconditional exactly-once behavior, or external atomic operation identity when the provider does not supply those guarantees.

**Source:** SWE1-SRC-002 §5 CAN-CORE-003  
**Rationale:** Prevents overstating external guarantees.  
**Precondition / trigger:** When translating provider responses into Wayfarer transaction outcomes.  
**Required observable result:** Returned status and documentation preserve the provider's actual guarantee boundary.  
**Verification intent:** Interface inspection and failure-mode integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-003 — No balance-difference success proof

**Normative statement:** Core shall not infer transaction success solely from a before/after balance difference.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004  
**Rationale:** Concurrent balance changes make difference-based proof unsafe.  
**Precondition / trigger:** When provider completion is ambiguous or queried indirectly.  
**Required observable result:** The operation remains `UNKNOWN` or uses an explicit provider-supported proof; balance delta alone does not terminally succeed it.  
**Verification intent:** SWE.4 outcome-policy verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-004 — No automatic UNKNOWN retry

**Normative statement:** Core shall not automatically re-invoke a Waymark provider operation whose outcome is `UNKNOWN`.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004  
**Rationale:** Avoids duplicate debit/refund.  
**Precondition / trigger:** When an operation returns or is recovered as `UNKNOWN`.  
**Required observable result:** No second provider effect is automatically attempted; manual reconciliation remains possible.  
**Verification intent:** SWE.4 replay-policy verification and SWE.5 ambiguous-provider integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-005 — No provider side channel

**Normative statement:** Core shall not create a Wayfarer-specific RedisEconomy side channel or mutate provider internals to manufacture stronger transaction semantics.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004  
**Rationale:** Preserves provider ownership and upgrade safety.  
**Precondition / trigger:** When transaction guarantees are insufficient.  
**Required observable result:** The limitation is represented explicitly rather than bypassed through provider-internal state.  
**Verification intent:** Source/dependency inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-QLT-001 — Transaction idempotency support

**Normative statement:** The Core transaction boundary shall accept or produce stable operation identity sufficient for target modules to recognize replay and prevent duplicate provider effects.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; §5 CAN-CORE-003  
**Rationale:** Enables end-to-end duplicate-effect prevention.  
**Precondition / trigger:** On initial execution and subsequent replay of the same logical operation.  
**Required observable result:** The provider invocation occurs no more than allowed by the stored operation disposition.  
**Verification intent:** SWE.4 unit verification and SWE.5 transaction integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-006 — Core gameplay non-ownership

**Normative statement:** Core shall not own Main Growth Tool behavior, Worlds Beyond traversal behavior, launchpad behavior, target GUI behavior, or target command semantics.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; §5 CAN-CORE-001  
**Rationale:** Maintains dependency direction and target autonomy.  
**Precondition / trigger:** During architecture allocation and implementation.  
**Required observable result:** Target gameplay remains in the target plugin and Core exposes only shared contracts.  
**Verification intent:** SWE.2 allocation review and source inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-007 — Core migration necessity

**Normative statement:** A new Core migration shall be introduced only for a new Core-owned durable capability and shall not be added merely because Main or Frontier needs target-owned persistence.

**Source:** SWE1-SRC-002 §5 CAN-CORE-005  
**Rationale:** Prevents target schema leakage into Core.  
**Precondition / trigger:** When persistence changes are proposed.  
**Required observable result:** The migration changes only `wf_core_*` structures and has an identified Core requirement.  
**Verification intent:** Migration ownership inspection and SWE.5 migration integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-008 — Accepted Core migration preservation

**Normative statement:** All applied V0.0.1 Core migrations shall remain byte-for-byte unchanged.

**Source:** SWE1-SRC-002 §5 CAN-CORE-005  
**Rationale:** Protects accepted database history.  
**Precondition / trigger:** At any Core schema evolution.  
**Required observable result:** Prior migration checksums remain identical.  
**Verification intent:** Checksum verification and inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
