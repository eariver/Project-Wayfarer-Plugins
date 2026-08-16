# Core Requirement Review Corrections — CAN-CORE-001 through CAN-CORE-005

Document ID: `DEC-REQ-005`  
Revision: A  
State: `APPROVED`  
Date: 2026-08-12 JST  
Author: ChatGPT  
Approver: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Affected source: `SWE1-SRC-002` Revision B  
Affected SWE.1 documents: `SWE1-CORE-001`, `TRC-SWE1-001`, `SWE1-VERIFY-001`, `SWE1-INDEX-001`

## 1. Purpose

Record the Owner-approved correction directions resulting from joint review of `CAN-CORE-001` through `CAN-CORE-005` and control their integration at the Core-section repository checkpoint.

This decision follows the Common corrections in `DEC-REQ-002` and `DEC-REQ-004` and the checkpoint/session-continuity policy in `DEC-REQ-003`. It does not approve G1, SWE.2, SWE.3, implementation, verification execution, PR readiness, merge, tag, deployment, or release.

## 2. CAN-CORE-001 — accepted V0.0.1 Core contract compatibility

### 2.1 Approved correction

V0.0.1 compatibility protects the accepted external/public contract, not the V0.0.1 implementation. V0.0.1 implementation locations may be refactored, corrected, or replaced in V0.0.2, including defect correction, validation/error-handling improvement, internal dependency changes, threading/lifecycle redesign, and changes to undocumented behavior, provided the accepted public contract remains compatible.

The accepted V0.0.1 Core public contract is identified through the controlled baseline inventory. Compatibility covers accepted source use, binary linkage, and documented externally observable contract semantics. It does not freeze Core internal structure, implementation-specific dependencies, or undocumented implementation behavior.

A new public contract required by an approved capability allocated to Core shall extend the accepted baseline without incompatibly removing, renaming, retyping, or changing the documented semantics of accepted contract elements. Existing contract elements may be deprecated while remaining compatible.

A baseline-breaking change requires an explicit Owner-approved baseline-change decision identifying the affected contract surface and version/transition scope. Accepted contract elements outside that approved scope remain protected.

The accepted V0.0.1 Core migration baseline remains protected by the Common migration rules and `CAN-CORE-005`.

### 2.2 Required SWE.1 changes

- revise `SWE1-CORE-001-IFC-001` to **Accepted V0.0.1 public-contract backward compatibility**;
- revise `SWE1-CORE-001-IFC-002` to **Compatible additive Core contract extension**;
- add `CAN-CORE-001` as a source for `SWE1-CORE-001-CON-008` because the canonical clause explicitly protects the accepted migration baseline;
- remove `CAN-CORE-001` as a source for `SWE1-CORE-001-CAP-001` and `SWE1-CORE-001-CON-006`; those obligations remain valid from the approved Common topology/ownership decisions.

The complete V0.0.1 public-contract and migration inventory remains required before G1.

## 3. CAN-CORE-002 — stable public-contract abstraction and runtime type identity

### 3.1 Approved correction

The Core public contract shall be isolated from Core implementation details without freezing current implementation-library names into SWE.1. The normative boundary is implementation-specific type/resource leakage, not a permanent list containing HikariCP, Flyway, Lettuce, or another present implementation choice.

Raw internal resource or authority-access handles, including database/provider/client handles that would allow a consumer to bypass an approved capability boundary, shall not be exposed as the normal Core public contract.

Platform or external contract types are not categorically prohibited. They may form part of a Core public contract only when required by the approved capability contract and when their compatibility, lifecycle, ownership, and execution-context implications are acceptable. Implementation convenience alone is not sufficient reason to expose a runtime-specific type. Public-boundary inspection includes parameters, returns, generic arguments, callbacks, public DTO/record members, exceptions, and equivalent public contract members.

The class-identity requirement is result-oriented. Within one in-process Core public-contract compatibility domain, participating software units shall exchange contract types using compatible runtime type identity. SWE.1 does not mandate one API JAR placement, dependency scope, or class-loader mechanism. Intentionally isolated contract versions may coexist only when their type identities do not cross the same public-contract boundary and an approved compatibility/adapter boundary defines their interaction.

### 3.2 Required SWE.1 changes

- revise `SWE1-CORE-001-CON-001` to **Public-contract implementation isolation**;
- revise `SWE1-CORE-001-CON-002` to **Public-contract runtime type identity**.

No new Product requirement is required.

## 4. CAN-CORE-003 — V0.0.2 shared Waymark transaction contract and provider guarantee boundary

### 4.1 Approved correction

For V0.0.2, the compatibility-preserving shared Waymark transaction contract is explicitly allocated to Core. This is a V0.0.2 software allocation and does not establish Core as the permanent owner of every future shared transaction implementation. The Common shared-capability allocation rules continue to apply to later architecture evolution.

The accepted V0.0.1 public surface already includes `WayfarerServices.transactions()` and the `WayfarerTransactions` contract. V0.0.2 shall preserve that accepted contract subject to `CAN-CORE-001` while permitting internal implementation redesign.

Core owns the shared transaction concerns: provider-access boundary, logical operation/effect correlation, provider interaction disposition, shared financial-effect coordination, transaction inspection/audit support, and shared reconciliation mechanism. Feature-specific eligibility, paid-benefit semantics, feature-domain mutation, item entitlement, final delivery, and equivalent feature policy remain with the applicable feature owner unless explicitly reallocated by an approved requirement.

Waymark balance authority remains the approved economy provider. Wayfarer transaction records may authoritatively record Wayfarer's logical operation/effect disposition but do not replace provider balance authority and do not independently prove a feature-owned domain effect.

Provider evidence is interpreted only within guarantees actually supplied by the approved provider contract. Core shall not infer durable effect completion, provider-side effect lookup, external operation identity, atomic multi-effect behavior, or exactly-once semantics unless the provider contract supplies the corresponding guarantee. The earlier `durable Redis completion` wording is removed as an inappropriate implementation/provider-internal fixation.

Protected-operation identity, replay, `UNKNOWN`, compensation, and duplicate-effect behavior continue to follow the approved Common requirements.

### 4.2 Required SWE.1 changes

- revise `SWE1-CORE-001-CAP-002` to **V0.0.2 shared Waymark transaction contract**;
- revise `SWE1-CORE-001-IFC-003` to **External provider guarantee preservation**;
- retain `SWE1-CORE-001-QLT-001` as the Core specialization of Common protected-operation identity/replay semantics and clarify that a Core transaction disposition does not itself establish a feature-owned domain effect.

No new Product requirement is required.

## 5. CAN-CORE-004 — ambiguous Waymark effect containment and no manufactured provider semantics

### 5.1 Approved correction

The generic `UNKNOWN`, replay, duplicate-effect, and reconciliation rules are not redefined in Core. They remain controlled by the Common protected-operation requirements and the Core transaction-effect specialization.

A before/after Waymark balance observation or other aggregate provider state that is not contractually correlated to the exact provider effect shall not by itself establish that the effect succeeded, clearly failed without effect, or was not applied. Aggregate state may still be used for eligibility, diagnostics, or reconciliation context.

Core shall not access or mutate unsupported provider internals, create unsupported provider-side/cross-channel markers, or otherwise introduce a unilateral Wayfarer mechanism in order to claim effect lookup, provider-side operation identity, atomicity, exactly-once behavior, durable completion evidence, or another guarantee absent from the approved provider contract.

Wayfarer-owned operation/effect/audit/reconciliation records are permitted and required where applicable. They record Wayfarer's own knowledge/disposition and shall not by themselves strengthen the provider effect's authoritative guarantee. A stronger provider capability may be used when it is supplied by an approved supported provider contract.

### 5.2 Required SWE.1 changes

- revise `SWE1-CORE-001-CON-003` to **No aggregate-state inference as exact provider-effect proof**;
- supersede and remove `SWE1-CORE-001-CON-004 — No automatic UNKNOWN retry` because its full obligation is already covered by `SWE1-COMMON-001-QLT-006`, `SWE1-COMMON-001-QLT-012`, and `SWE1-CORE-001-QLT-001`;
- revise `SWE1-CORE-001-CON-005` to **No manufactured provider semantics**;
- retain explicit trace from `CAN-CORE-004` to the inherited Common/Core `UNKNOWN` and replay rules without creating another duplicate Product requirement.

The removal is an atomicity/deduplication correction, not a relaxation of behavior. Existing identifiers after `CON-004` are not renumbered.

## 6. CAN-CORE-005 — Core durable-schema evolution and accepted migration preservation

### 6.1 Approved correction

Core schema evolution is not limited to creation of a new Core capability. A Core-attributed migration may be required by an approved new or existing Core-owned durable capability, defect correction, integrity requirement, compatibility requirement, or controlled data/schema evolution.

A Core migration shall not be used to create, alter, or carry durable state owned by another Wayfarer durable-state owner merely because Core provides shared database or migration infrastructure.

The accepted V0.0.1 Core migrations identified by the controlled accepted-baseline inventory are immutable migration-history artifacts. They retain migration identity, ordering semantics, and byte-for-byte artifact content and shall not be modified, deleted, reordered, reused, or repurposed. Later corrections/evolution use a new Core-owned migration identity.

This requirement does not permanently fix Flyway or another migration framework, resource directory, executor, namespace/table convention, or physical history-store mechanism, provided accepted migration history and the supported V0.0.1 upgrade path remain compatible.

### 6.2 Required SWE.1 changes

- revise `SWE1-CORE-001-CON-007` to **Core-owned schema evolution justification**;
- revise `SWE1-CORE-001-CON-008` to **Accepted Core migration artifact immutability**;
- retain the Core-specific byte-for-byte strengthening for controlled accepted V0.0.1 migration artifacts;
- add `CAN-CORE-001` to the reverse source trace of `CON-008`.

Common empty-install/upgrade compatibility and schema fail-closed requirements are reused rather than duplicated as additional Core requirements.

## 7. Requirement-count effect

The integrated Common checkpoint contained 176 provisional Product requirements. Core review does not add new atomic requirements. It removes one redundant Core requirement:

- supersede/remove `SWE1-CORE-001-CON-004 — No automatic UNKNOWN retry` because equivalent and stronger obligations already exist in approved Common/Core requirements.

The provisional integrated total after Core review is therefore **175** Product requirements:

- CAP: 64
- CON: 58
- IFC: 14
- QLT: 39

`SWE1-CORE-001` contains 13 active normative items: CAP 2, CON 7, IFC 3, QLT 1. Identifier `SWE1-CORE-001-CON-004` remains a historical superseded identifier and is not reused or renumbered.

## 8. Checkpoint and downstream obligations

This decision completes Owner review of the Core canonical section (`CAN-CORE-001` through `CAN-CORE-005`). The Owner explicitly authorized repository checkpoint reflection after `CAN-CORE-005`, simultaneously satisfying five approved clauses since the Common checkpoint and the Core → Main logical section transition.

At this checkpoint, the approved Core corrections shall be integrated into the canonical source, Core requirements, index, traceability, verification-intent allocation, review log, source/status/continuation records, and requirement counts.

The next substantive canonical review item after checkpoint integration is `CAN-MAIN-001 — Deployment and lifecycle`.

All later gate restrictions remain unchanged.