# Wayfarer_Core Software Requirements

Document ID: `SWE1-CORE-001`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `CORE`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision B  
Controlling Common review decisions: `DEC-REQ-002`, `DEC-REQ-004`  
Contained normative items: CAP: 2, CON: 8, IFC: 3, QLT: 1

## 1. Purpose

Define the shared capabilities currently allocated to Wayfarer_Core and the Core compatibility constraints required by approved dependent capabilities without allocating target gameplay semantics to Core.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- Common requirements do not permanently force all future shared capability ownership into Core; this document controls only capabilities explicitly allocated to Core by the applicable Core clauses/design baseline.

## 3. Requirements

### SWE1-CORE-001-CAP-001 — Shared Core service availability independent of backend naming

**Normative statement:** For shared services explicitly allocated to Wayfarer_Core, Core shall provide the approved compatible service to any runtime containing an approved dependent Wayfarer capability without requiring a historical Main/Frontier backend name and without taking ownership of the dependent feature's gameplay semantics.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001; §4 CAN-COM-001; DEC-REQ-002 §2  
**Rationale:** Preserves the current Core allocation while separating service ownership from physical backend naming.  
**Precondition / trigger:** When an approved dependent capability resolves a Core-owned shared service.  
**Required observable result:** The compatible Core-owned service is available when its actual prerequisites are met, or the dependent capability fails closed; backend naming alone does not decide availability.  
**Verification intent:** SWE.2 allocation inspection and SWE.5 dependent-capability/Core integration across approved topologies.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-001  
**Assumptions:** None  
**Open issue / conflict:** Exact shared ownership under CAN-CORE-001/CAN-CORE-003 remains subject to the upcoming Core-clause Owner review.  
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
**Assumptions:** Complete accepted V0.0.1 public-contract inventory will be established before G1.  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-002 — Additive Core extension

**Normative statement:** A Core capability needed by an approved dependent capability shall be introduced through an additive public contract with explicit capability discovery and version compatibility unless an Owner-approved baseline-breaking change authorizes otherwise.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001  
**Rationale:** Allows Core extension without silently changing accepted baseline semantics.  
**Precondition / trigger:** When a new shared capability is proposed for Core ownership.  
**Required observable result:** Consumers can determine capability availability and fail closed when the required compatible version/capability is absent.  
**Verification intent:** SWE.4 contract verification and SWE.5 mixed-version integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** Exact additive/breaking compatibility policy remains subject to CAN-CORE-001 Owner review.  
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
**Required observable result:** The runtime resolves one compatible API class identity for all consumers.  
**Verification intent:** Packaging inspection and SWE.5 class-loading integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CAP-002 — Waymark transaction service

**Normative statement:** Core shall expose the approved Waymark transaction boundary used by currently allocated Main repair/reissue and Frontier shop operations.

**Source:** SWE1-SRC-002 §5 CAN-CORE-003  
**Rationale:** Centralizes the currently allocated shared economy semantics and prevents direct feature access to provider internals.  
**Precondition / trigger:** When an approved dependent capability requests a supported debit, refund/compensation, or reconciliation-capable operation.  
**Required observable result:** The request is handled through the approved Core contract and returns an explicit supported disposition.  
**Verification intent:** SWE.4 service-contract verification and SWE.5 provider/dependent-capability integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-004  
**Assumptions:** None  
**Open issue / conflict:** Concrete shared transaction ownership remains subject to CAN-CORE-003 Owner review.  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-003 — Provider acceptance semantics

**Normative statement:** Core shall represent provider acceptance only as evidence that the provider accepted the invocation and shall not claim durable completion, external effect lookup, unconditional exactly-once behavior, or external atomic operation identity when the provider does not supply those guarantees.

**Source:** SWE1-SRC-002 §5 CAN-CORE-003  
**Rationale:** Prevents overstating external guarantees.  
**Precondition / trigger:** When translating provider responses into Wayfarer transaction outcomes.  
**Required observable result:** Returned status and documentation preserve the provider's actual guarantee boundary.  
**Verification intent:** Interface inspection and SWE.5 failure-mode provider integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-004  
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
**Dependencies:** SWE1-COMMON-001-QLT-006  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-004 — No automatic UNKNOWN retry

**Normative statement:** Core shall not automatically re-invoke a Waymark provider effect whose outcome is `UNKNOWN`.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004; §4 CAN-COM-007  
**Rationale:** Avoids duplicate debit/refund/compensation where the provider effect may already have occurred.  
**Precondition / trigger:** When a provider effect returns or is recovered as `UNKNOWN`.  
**Required observable result:** No second uncertain provider effect is automatically attempted; authorized reconciliation remains possible.  
**Verification intent:** SWE.4 replay-policy verification and SWE.5 ambiguous-provider integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-006  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-005 — No provider side channel

**Normative statement:** Core shall not create a Wayfarer-specific RedisEconomy side channel or mutate provider internals to manufacture stronger transaction semantics.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004  
**Rationale:** Preserves provider ownership and upgrade safety.  
**Precondition / trigger:** When provider guarantees are insufficient for a requested transaction semantic.  
**Required observable result:** The limitation is represented explicitly rather than bypassed through provider-internal state.  
**Verification intent:** Source/dependency inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-003; SWE1-COMMON-001-IFC-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-QLT-001 — Protected transaction-effect identity contract

**Normative statement:** For a protected provider operation handled by the Core transaction boundary, Core shall operate on the stable logical operation/effect identity established before provider effect, correlate provider invocation/reference/result with that identity, and return the existing effect disposition on replay. A provider effect already proven successful or recorded as `UNKNOWN` shall not be automatically re-invoked for the same effect identity.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; §5 CAN-CORE-003; DEC-REQ-004 §3  
**Rationale:** Specializes Common protected-operation identity and replay containment at the provider boundary.  
**Precondition / trigger:** Initial provider invocation, replay, restart recovery, or reconciliation inspection of the same protected effect.  
**Required observable result:** Provider interaction remains correlated with one logical operation/effect; replay returns the established disposition and does not create an automatic second successful/uncertain effect.  
**Verification intent:** SWE.4 contract/idempotency verification and SWE.5 provider transaction/recovery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-006; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** Exact Core transaction contract remains subject to CAN-CORE-003 review.  
**State:** `DRAFT`

### SWE1-CORE-001-CON-006 — Shared-foundation semantic neutrality

**Normative statement:** A capability allocated to Core as shared foundation shall not own policy or gameplay semantics that apply only to a specific feature domain unless an approved architecture allocation explicitly assigns that responsibility to Core; Core may provide reusable mechanisms and contracts without embedding feature-specific decisions.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; §5 CAN-CORE-001; DEC-REQ-002 §5.4  
**Rationale:** Prevents current feature names from becoming the permanent definition of the shared boundary while preserving reusable shared mechanisms.  
**Precondition / trigger:** During capability allocation, API design, or shared implementation design.  
**Required observable result:** Feature-specific policy remains with its approved feature owner unless explicitly reallocated; Core contracts expose shared mechanisms rather than hidden feature decisions.  
**Verification intent:** SWE.2 allocation review and SWE.3 API/ownership inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-002; SWE1-COMMON-001-CON-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-007 — Core migration necessity

**Normative statement:** A new Core migration shall be introduced only for a durable capability allocated to Core and shall not be added merely because another feature needs persistence owned outside Core.

**Source:** SWE1-SRC-002 §5 CAN-CORE-005  
**Rationale:** Prevents target schema leakage into Core.  
**Precondition / trigger:** When a Core persistence change is proposed.  
**Required observable result:** Every Core migration has an identified Core-owned durable-state requirement and does not alter another owner's schema objects.  
**Verification intent:** Migration ownership inspection and SWE.5 migration integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-002  
**Assumptions:** None  
**Open issue / conflict:** Exact Core namespace/table-prefix implications remain subject to CAN-CORE-005 review and accepted V0.0.1 migration inventory.  
**State:** `DRAFT`

### SWE1-CORE-001-CON-008 — Accepted Core migration preservation

**Normative statement:** All accepted V0.0.1 Core migration files shall remain byte-for-byte unchanged.

**Source:** SWE1-SRC-002 §5 CAN-CORE-005; §4 CAN-COM-008  
**Rationale:** Protects accepted database history.  
**Precondition / trigger:** At any Core schema evolution.  
**Required observable result:** Prior accepted Core migration content/checksums remain identical and later change is introduced by a new migration.  
**Verification intent:** Accepted-migration inventory/checksum verification and inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-006  
**Assumptions:** Complete accepted V0.0.1 migration inventory will be established before G1.  
**Open issue / conflict:** Exact accepted inventory remains pending.  
**State:** `DRAFT`
