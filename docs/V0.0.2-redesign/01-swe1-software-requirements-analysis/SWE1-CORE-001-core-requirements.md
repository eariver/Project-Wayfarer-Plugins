# Wayfarer_Core Software Requirements

Document ID: `SWE1-CORE-001`  
Revision: C  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-12 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `CORE`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision C  
Controlling review decisions: `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`  
Contained active normative items: CAP: 2, CON: 7, IFC: 3, QLT: 1; 13 total

## 1. Purpose

Define the shared capabilities currently allocated to Wayfarer_Core and the Core compatibility constraints required by approved dependent capabilities without allocating target gameplay semantics to Core.

Revision C integrates the complete joint Owner review of `CAN-CORE-001` through `CAN-CORE-005`. V0.0.1 compatibility protects accepted public contracts and immutable migration-history artifacts, not the V0.0.1 implementation itself.

## 2. Requirement interpretation rules

- Each active item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, packaging mechanism, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- Common requirements do not permanently force all future shared capability ownership into Core; this document controls only capabilities explicitly allocated to Core for the applicable Product baseline.
- Superseded identifiers are not reused or renumbered.

## 3. Active requirements

### SWE1-CORE-001-CAP-001 — Shared Core service availability independent of backend naming

**Normative statement:** For shared services explicitly allocated to Wayfarer_Core, Core shall provide the approved compatible service to any runtime containing an approved dependent Wayfarer capability without requiring a historical Main/Frontier backend name and without taking ownership of the dependent feature's gameplay semantics.

**Source:** SWE1-SRC-002 §4 CAN-COM-001; DEC-REQ-002 §2  
**Rationale:** Preserves approved Core service allocation while separating service ownership from physical backend naming.  
**Precondition / trigger:** When an approved dependent capability resolves a Core-owned shared service.  
**Required observable result:** The compatible Core-owned service is available when its actual prerequisites are met, or the dependent capability fails closed; backend naming alone does not decide availability.  
**Verification intent:** SWE.2 allocation inspection and SWE.5 dependent-capability/Core integration across approved topologies.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-001 — Accepted V0.0.1 public-contract backward compatibility

**Normative statement:** The accepted V0.0.1 Core public contract identified by the controlled baseline inventory shall remain backward-compatible for conforming V0.0.1 consumers with respect to accepted source use, binary linkage, and documented externally observable contract semantics, except for an explicitly scoped Owner-approved baseline-breaking change. Core internal implementation structure, implementation-specific dependencies, and undocumented implementation behavior are not part of this compatibility obligation.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001; DEC-REQ-005 §2  
**Rationale:** Protects accepted external compatibility while allowing V0.0.1 implementation defects, internal design, dependencies, validation, threading, lifecycle handling, and undocumented behavior to be corrected or redesigned.  
**Precondition / trigger:** When a V0.0.2 Core artifact is used by a consumer conforming to the controlled accepted V0.0.1 public contract.  
**Required observable result:** Accepted source use and binary linkage remain usable and documented contract semantics remain compatible outside any explicitly approved breaking-change scope.  
**Verification intent:** Controlled baseline inventory inspection, source/API compatibility analysis, binary compatibility tooling, documented-contract comparison, and SWE.5 class-loading/integration where behavior is runtime-dependent.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** Complete accepted V0.0.1 public-contract inventory will be established before G1.  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-002 — Compatible additive Core contract extension

**Normative statement:** A new public contract element shall be added to Core only for an approved capability allocated to Core and shall not incompatibly remove, rename, retype, or change the documented semantics of an accepted V0.0.1 public-contract element. A consumer that requires a newer Core contract may treat absence of that required compatible contract as an unmet capability prerequisite; this requirement does not require an older Core implementation to provide forward compatibility for a newer consumer.

**Source:** SWE1-SRC-002 §5 CAN-CORE-001; DEC-REQ-005 §2  
**Rationale:** Allows Core contract growth without treating additive syntax alone as proof of backward compatibility or requiring impossible forward compatibility from an older Core.  
**Precondition / trigger:** When a new public contract is proposed for a capability allocated to Core.  
**Required observable result:** Accepted V0.0.1 contract elements remain compatible; a newer consumer fails closed when its genuinely required newer contract is absent rather than assuming the older Core can provide it.  
**Verification intent:** API/source/binary compatibility inspection, SWE.4 capability-prerequisite policy verification, and SWE.5 mixed-version integration where supported.  
**Priority:** `MUST`  
**Dependencies:** SWE1-CORE-001-IFC-001; SWE1-COMMON-001-QLT-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-001 — Public-contract implementation isolation

**Normative statement:** A Core-owned public contract shall not expose Core implementation-specific types or raw internal resource/authority-access handles through public signatures or public contract data. An external or platform-specific type may be exposed only when it is required by the approved capability contract and acceptable within the defined compatibility, lifecycle, ownership, and execution-context constraints.

**Source:** SWE1-SRC-002 §5 CAN-CORE-002; DEC-REQ-005 §3  
**Rationale:** Prevents consumers from coupling to Core storage/client/migration/execution implementations or bypassing approved authority boundaries while not categorically prohibiting a legitimate platform/external contract type.  
**Precondition / trigger:** At definition or publication of any Core public parameter, return, generic argument, callback, public DTO/record member, public exception, or equivalent contract member.  
**Required observable result:** The public contract contains only approved contract types; no raw internal resource/authority handle or implementation-only type is exposed merely for implementation convenience.  
**Verification intent:** SWE.3 static API-surface inspection, dependency analysis, and authority-boundary review.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-003; SWE1-COMMON-001-IFC-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-002 — Public-contract runtime type identity

**Normative statement:** Within each in-process Core public-contract compatibility domain, participating software units shall exchange contract types using compatible runtime type identity. Packaging and class-loading shall not cause independent duplicate contract definitions to cross that boundary or produce service-resolution, type-check, casting, callback, or equivalent interoperability failure.

**Source:** SWE1-SRC-002 §5 CAN-CORE-002; DEC-REQ-005 §3  
**Rationale:** Expresses the required interoperability result without mandating one API artifact placement, dependency scope, or class-loader design. Intentionally isolated contract versions remain possible behind an approved compatibility/adapter boundary.  
**Precondition / trigger:** At assembly/loading of software units that exchange an in-process Core public contract.  
**Required observable result:** Contract provider and consumer resolve compatible runtime types across the shared boundary; intentionally isolated versions do not exchange their incompatible identities directly.  
**Verification intent:** SWE.3 packaging/class-loader design inspection and SWE.5 service-resolution/type-identity integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-CORE-001-CON-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CAP-002 — V0.0.2 shared Waymark transaction contract

**Normative statement:** For V0.0.2, Core shall provide the compatibility-preserving shared Waymark transaction contract for approved Wayfarer capabilities requiring financial effects, compensation, transaction inspection, or reconciliation. The contract shall coordinate shared transaction concerns without making Core authoritative for feature-specific paid-benefit eligibility, domain mutation, entitlement, final delivery, or other feature policy.

**Source:** SWE1-SRC-002 §5 CAN-CORE-003; DEC-REQ-005 §4  
**Rationale:** Preserves the accepted V0.0.1 Core-facing transaction surface and centralizes V0.0.2 shared financial-safety mechanics while keeping feature semantics with feature owners. This allocation does not establish permanent future Core ownership.  
**Precondition / trigger:** When an approved V0.0.2 dependent capability requests a supported financial effect, compensation, transaction inspection, or reconciliation operation.  
**Required observable result:** The request uses the approved Core shared contract and receives a supported Wayfarer transaction disposition without transferring feature-domain authority to Core.  
**Verification intent:** SWE.2 ownership/allocation inspection, SWE.4 service-contract verification, and SWE.5 provider/dependent-capability integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-004; SWE1-CORE-001-CON-006; SWE1-CORE-001-IFC-001  
**Assumptions:** The accepted V0.0.1 transaction public surface is included in the pending controlled baseline inventory.  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-IFC-003 — External provider guarantee preservation

**Normative statement:** The Core Waymark transaction contract shall classify provider evidence only according to guarantees supplied by the approved provider contract. It shall not infer durable effect completion, provider-side effect lookup capability, provider-side operation identity, atomic multi-effect behavior, or exactly-once semantics unless the provider contract explicitly supplies the corresponding guarantee.

**Source:** SWE1-SRC-002 §5 CAN-CORE-003; DEC-REQ-005 §4  
**Rationale:** Prevents Wayfarer from interpreting provider evidence as either stronger or categorically weaker than the actual supported provider contract and separates Wayfarer operation disposition from Waymark balance authority.  
**Precondition / trigger:** When provider evidence is translated into a Wayfarer transaction/effect disposition or used during reconciliation.  
**Required observable result:** The resulting disposition stays within the provider guarantee boundary; Wayfarer transaction records do not replace provider balance authority or independently prove a feature-owned domain effect.  
**Verification intent:** Provider-contract inspection, SWE.4 evidence-classification verification, and SWE.5 provider failure/reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-004; SWE1-CORE-001-CAP-002  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-003 — No aggregate-state inference as exact provider-effect proof

**Normative statement:** Core shall not classify an individual Waymark provider effect as successful, clearly failed without effect, or not applied solely from a before/after balance difference or other aggregate provider state that is not contractually correlated to that exact effect. Aggregate state may be used for eligibility, diagnostics, or reconciliation context without serving as exact-effect proof.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004; DEC-REQ-005 §5  
**Rationale:** Concurrent or unrelated balance changes make aggregate state unsafe as proof of one exact effect while still permitting legitimate balance-dependent product behavior and diagnostic use.  
**Precondition / trigger:** When an exact provider effect outcome is ambiguous, reconstructed, or inspected indirectly.  
**Required observable result:** Exact-effect disposition is based only on contractually valid correlated evidence; uncorrelated aggregate state does not terminally classify the effect.  
**Verification intent:** SWE.4 outcome/evidence policy verification and SWE.5 concurrent-provider/reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-006; SWE1-CORE-001-IFC-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-005 — No manufactured provider semantics

**Normative statement:** Core shall not use unsupported provider internals, unsupported provider-side state, or another Wayfarer-created side channel to establish provider-effect guarantees stronger than those supplied by the approved provider contract. Wayfarer-owned operation, effect, audit, and reconciliation records may retain Wayfarer's own knowledge/disposition but shall not independently prove or strengthen the provider effect.

**Source:** SWE1-SRC-002 §5 CAN-CORE-004; DEC-REQ-005 §5  
**Rationale:** Prevents unsupported coupling and false guarantees without prohibiting the durable Wayfarer records required for identity, replay, audit, and reconciliation. A stronger provider capability remains usable when supplied through an approved supported contract.  
**Precondition / trigger:** When the currently approved provider guarantees are insufficient for a desired transaction semantic or when reconciliation evidence is evaluated.  
**Required observable result:** Provider limitations remain explicit; no unsupported internal/side-channel marker is promoted to provider authority, while legitimate Wayfarer operation records remain available.  
**Verification intent:** SWE.3 dependency/integration-boundary inspection, SWE.4 evidence-authority verification, and SWE.5 provider integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-003; SWE1-COMMON-001-IFC-004; SWE1-CORE-001-IFC-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-QLT-001 — Protected transaction-effect identity contract

**Normative statement:** For a protected provider operation handled by the Core transaction boundary, Core shall operate on the stable logical operation/effect identity established before provider effect, correlate provider invocation/reference/result with that identity, and return the existing effect disposition on replay. A provider effect already proven successful or recorded as `UNKNOWN` shall not be automatically re-invoked for the same effect identity. A Core transaction disposition shall not by itself establish a feature-owned domain effect.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; §5 CAN-CORE-003; DEC-REQ-004 §3; DEC-REQ-005 §4–§5  
**Rationale:** Specializes Common protected-operation identity and replay containment at the provider boundary while preserving the separate authority of feature-owned effects.  
**Precondition / trigger:** Initial provider invocation, replay, restart recovery, reconciliation inspection, or later resolution of the same protected effect.  
**Required observable result:** Provider interaction remains correlated with one logical operation/effect; replay returns or advances the established disposition without automatic duplicate invocation, and Core's record is not used as independent proof of another owner's domain effect.  
**Verification intent:** SWE.4 contract/idempotency/authority verification and SWE.5 provider transaction/recovery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-006; SWE1-COMMON-001-QLT-012; SWE1-CORE-001-IFC-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-006 — Shared-foundation semantic neutrality

**Normative statement:** A capability allocated to Core as shared foundation shall not own policy or gameplay semantics that apply only to a specific feature domain unless an approved architecture allocation explicitly assigns that responsibility to Core; Core may provide reusable mechanisms and contracts without embedding feature-specific decisions.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; DEC-REQ-002 §5.4  
**Rationale:** Prevents a shared Core boundary from silently absorbing feature-specific semantics while preserving reusable shared mechanisms.  
**Precondition / trigger:** During capability allocation, API design, or shared implementation design.  
**Required observable result:** Feature-specific policy remains with its approved feature owner unless explicitly reallocated; Core contracts expose shared mechanisms rather than hidden feature decisions.  
**Verification intent:** SWE.2 allocation review and SWE.3 API/ownership inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-002; SWE1-COMMON-001-CON-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-007 — Core-owned schema evolution justification

**Normative statement:** A schema migration attributed to Core shall correspond to an approved change, correction, integrity requirement, or compatibility requirement of a durable-state domain allocated to Core. Core shall not introduce migration-owned persistence for a durable domain allocated to another owner.

**Source:** SWE1-SRC-002 §5 CAN-CORE-005; DEC-REQ-005 §6  
**Rationale:** Permits legitimate evolution and correction of existing Core-owned durable state while preventing feature persistence from leaking into Core merely because Core provides shared database/migration infrastructure.  
**Precondition / trigger:** When a Core-attributed persistence/schema change is proposed.  
**Required observable result:** Every Core migration has an identified approved Core-owned durable-state justification and does not create/alter another owner's schema domain.  
**Verification intent:** SWE.2 durable-owner allocation inspection, SWE.3 migration-design inspection, and SWE.5 migration integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-002; SWE1-COMMON-001-IFC-006  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-CORE-001-CON-008 — Accepted Core migration artifact immutability

**Normative statement:** Each accepted V0.0.1 Core migration identified in the controlled baseline inventory shall retain its migration identity, ordering semantics, and byte-for-byte artifact content and shall not be deleted, reordered, reused, or repurposed. Subsequent Core-owned schema correction or evolution shall use a new migration identity.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; §5 CAN-CORE-001; §5 CAN-CORE-005; DEC-REQ-004 §4; DEC-REQ-005 §2 and §6  
**Rationale:** Treats accepted migration artifacts as installation-history compatibility contracts rather than ordinary V0.0.1 implementation code.  
**Precondition / trigger:** At any Core schema evolution, migration-framework change, upgrade-path design, or controlled baseline comparison.  
**Required observable result:** Controlled accepted V0.0.1 migration identities/order/content remain identical, and every later Core-owned change is represented by a new migration identity.  
**Verification intent:** Controlled accepted-migration inventory, byte/hash/checksum comparison, ordering/identity inspection, and SWE.5 V0.0.1-to-current upgrade integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-006; SWE1-COMMON-001-QLT-007  
**Assumptions:** Complete accepted V0.0.1 migration inventory will be established before G1.  
**Open issue / conflict:** None beyond the pending controlled inventory required before G1.  
**State:** `DRAFT`

## 4. Superseded item disposition

| Historical requirement ID | Disposition | Replacement / retained obligation |
|---|---|---|
| `SWE1-CORE-001-CON-004 — No automatic UNKNOWN retry` | `SUPERSEDED_BY_DEDUPLICATION` | Generic `UNKNOWN` containment: `SWE1-COMMON-001-QLT-006`; replay/duplicate-effect containment: `SWE1-COMMON-001-QLT-012`; Core provider-effect specialization: `SWE1-CORE-001-QLT-001` |

The superseded identifier is historical only, is excluded from the active Product-requirement count, and shall not be reused for another obligation.