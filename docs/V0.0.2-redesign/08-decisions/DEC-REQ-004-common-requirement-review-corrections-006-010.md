# Common Requirement Review Corrections — CAN-COM-006 through CAN-COM-010

Document ID: `DEC-REQ-004`  
Revision: A  
State: `APPROVED`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Approver: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Affected source: `SWE1-SRC-002` Revision A  
Affected SWE.1 documents: `SWE1-COMMON-001`, `SWE1-CORE-001`, `SWE1-MAIN-001`, `SWE1-MAIN-003`, `SWE1-FRONTIER-001`, `SWE1-WB-002`

## 1. Purpose

Record the Owner-approved correction directions resulting from joint review of `CAN-COM-006` through `CAN-COM-010` and control their integration at the Common-section repository checkpoint.

This decision continues the correction model established by `DEC-REQ-002`. It does not approve G1, SWE.2, SWE.3, implementation, verification execution, PR readiness, merge, tag, deployment, or release.

## 2. CAN-COM-006 — capability lifecycle and fail-closed behavior

### 2.1 Approved correction

Fail-closed behavior is capability-scoped by default. A gameplay capability is available only while every mandatory prerequisite assigned to that capability remains valid. Mandatory prerequisites may include approved configuration, compatible schema state, required approved shared/external capability contracts, and target-specific world or content-context conditions.

A fixed physical server identity, historical backend role, or specific shared-provider implementation is not a general prerequisite unless an approved target requirement makes it an actual external contract.

When a mandatory prerequisite becomes invalid, or when a capability/plugin begins disablement or replacement, the affected capability becomes unavailable before accepting any new operation or producing any new capability-owned state change. Whole-plugin disablement is required only when the failed prerequisite applies to all runtime capabilities or a target requirement explicitly requires it.

For operations already accepted before admission closure, lifecycle handling shall produce a finite disposition: completed, safely rejected without protected effect, compensated, or explicitly unresolved but durably recoverable. Incomplete durable work shall not be silently discarded and shutdown/replacement shall not wait indefinitely.

Completion from an earlier or disabled lifecycle instance shall not mutate current or disabled runtime state unless the current lifecycle explicitly recognizes the operation as still valid. Applicable mutable preconditions are then revalidated under the asynchronous-completion requirement from `CAN-COM-005`.

The requirement specifies product/lifecycle results, not listener registration, command registration, asynchronous flush, runtime-generation counters, or another particular implementation mechanism.

### 2.2 Required SWE.1 decomposition

- revise `SWE1-COMMON-001-QLT-003` to capability-prerequisite availability;
- revise `SWE1-COMMON-001-QLT-004` to capability unavailability and operation-admission closure;
- add `SWE1-COMMON-001-QLT-010` — bounded accepted-operation disposition;
- add `SWE1-COMMON-001-QLT-011` — prior-lifecycle completion containment.

`SWE1-MAIN-001-QLT-001` shall specialize Main capability prerequisites without fixed backend-role or Core-implementation naming. `SWE1-MAIN-001-QLT-002` shall specialize dirty-session disposition without re-specifying the generic lifecycle mechanics.

### 2.3 Frontier propagation

The Owner determined that a Worlds Beyond capability may fail closed when its configured target world is absent and that, where all Frontier gameplay depends on that world, whole-plugin disablement is sufficient. The literal world name is not to become a Common requirement; later Frontier review shall express the target condition using a configurable Worlds Beyond gameplay-world identifier. Exact recovery/re-enable behavior remains a Frontier-specific review item.

## 3. CAN-COM-007 — protected-operation identity, replay, and ambiguous outcomes

### 3.1 Approved correction

Protected financial, compensation, durable-entitlement/delivery, and authority-rotation operations require a stable logical operation identity and recoverable operation disposition established before the first protected effect is invoked or committed.

Protected effects are distinguishable by effect kind and are correlated to the logical operation and any available provider transaction reference. Replay of an accepted logical operation resolves to the existing operation and stored effect dispositions instead of creating a new operation or duplicate protected effect. A stale request that was not previously accepted and no longer satisfies current target-specific prerequisites is rejected before protected effect.

Wayfarer prevents duplicate protected effects caused by its own invocation, replay, recovery, and reconciliation behavior without claiming guarantees stronger than the external provider contract. Durable-delivery retry may continue the same established entitlement only when final delivery has not been established; it shall not create a second entitlement or final delivered copy.

When a protected effect cannot be proven as success or clear no-effect failure, the effect is `UNKNOWN`. `UNKNOWN` is neither success nor clear failure and does not authorize automatic retry, automatic compensation, success-dependent continuation, paid-benefit completion, delivery-entitlement creation, or authority rotation. Resolution requires authoritative evidence or authorized reconciliation linked to the exact operation/effect and shall not re-invoke the uncertain effect.

A paid benefit is finalized only after debit success is proven. Automatic refund/compensation requires proven debit success plus proven absence or clear failure of the downstream paid benefit. Compensation has its own stable effect identity and follows the same replay and `UNKNOWN` rules.

### 3.2 Required SWE.1 decomposition

- revise `SWE1-COMMON-001-QLT-005` — stable protected-operation identity;
- revise `SWE1-COMMON-001-QLT-006` — ambiguous protected-effect containment;
- add `SWE1-COMMON-001-QLT-012` — replay and duplicate-effect containment;
- add `SWE1-COMMON-001-QLT-013` — paid-benefit sequencing and compensation containment.

Target propagation:

- `SWE1-CORE-001-QLT-001` shall require a stable operation/effect identity contract and prohibit re-invoking a success/`UNKNOWN` provider effect;
- `SWE1-MAIN-003-QLT-002` shall apply compensation only to clear downstream failure after proven debit success;
- `SWE1-MAIN-003-QLT-003` shall cover one debit, one authority rotation/current physical authority, and one delivery entitlement per reissue operation;
- `SWE1-MAIN-003-QLT-005` shall express debit, repair-commit, and compensation ambiguity at effect level;
- `SWE1-WB-002-QLT-003` shall use the Common protected-operation rules while preserving target-specific quote/product revalidation and one-entitlement semantics.

The exact priority between pending delivery and refund for `CAN-WB-014` remains deferred to the later target-clause review; delivery and refund must not both become successful outcomes for the same paid entitlement.

## 4. CAN-COM-008 — durable-schema ownership and migration compatibility

### 4.1 Approved correction

Each Wayfarer-owned durable-state domain has one assigned software owner. Schema objects and schema evolution remain attributable to that owner, and one owner shall not create, alter, drop, or repurpose schema objects owned by another owner.

Each durable-state owner has an independently identifiable migration sequence and applied-migration state. Table prefix, database/schema namespace, migration-resource location, migration executor, and physical migration-history storage are downstream design choices except where the accepted baseline already fixes an identifier/artifact that must remain compatible.

A migration included in an accepted Product baseline, or that may already have been applied to a supported installation, retains its migration identity, ordering semantics, and content. Later schema evolution uses a new forward migration. `Forward-only` does not prohibit transactional rollback of an uncommitted migration; it prohibits rewriting accepted migration history to express a later schema change.

The current schema must be constructible from an empty database and upgradeable from the accepted V0.0.1 baseline without destructive automatic reset or silent loss of accepted durable state. Controlled data transformation is permitted when it preserves the defined authority and resulting state semantics. Failure to establish required schema compatibility keeps affected capabilities fail-closed under `CAN-COM-006`.

### 4.2 Required SWE.1 decomposition

- revise `SWE1-COMMON-001-IFC-002` — durable-schema ownership isolation;
- add `SWE1-COMMON-001-IFC-006` — owner-attributable migration sequence;
- revise `SWE1-COMMON-001-CON-006` — accepted migration immutability and forward evolution;
- revise `SWE1-COMMON-001-QLT-007` — empty-install and supported-baseline upgrade compatibility.

`SWE1-FRONTIER-001-CON-006` shall express Frontier durable-state ownership without making `wf_frontier_*` a Common/SWE.1 architectural mechanism. Core-specific V0.0.1 migration preservation remains subject to `CAN-CORE-005` review.

The complete accepted V0.0.1 migration inventory remains required before G1.

## 5. CAN-COM-009 — auditable protected outcomes and sensitive-data safety

### 5.1 Approved correction

Designated security-sensitive, financial, durable-delivery, recovery, administrative, reconciliation, and later explicitly designated protected outcomes require retrievable audit evidence sufficient to correlate the operation/event, applicable actor and subject, occurrence time, recorded disposition, and operationally relevant reason/classification.

For `CAN-COM-007` protected operations, audit evidence shall correlate with the applicable logical operation/effect identities. Evidence needed for supported inspection/reconciliation remains retrievable across normal runtime restart for the applicable operational retention period. Existing durable transaction/domain/reconciliation records may satisfy this requirement; a duplicate dedicated audit store is not mandatory.

Audit evidence is not authority for a stronger domain/provider/physical effect than the corresponding authoritative source proves. `UNKNOWN` remains `UNKNOWN` unless resolved through the approved authoritative-evidence or reconciliation path.

Audit and diagnostic evidence apply data minimization. Secrets, raw credentials, authentication tokens, and equivalently protected values are not recorded. Personal/incidental runtime data is retained only when necessary for approved attribution, correlation, disposition analysis, recovery, or reconciliation. Stable actor/subject identifiers may be retained when required for those purposes.

### 5.2 Required SWE.1 decomposition

- revise `SWE1-COMMON-001-QLT-008` — retrievable protected-outcome audit evidence;
- add `SWE1-COMMON-001-QLT-014` — audit and diagnostic data minimization.

## 6. CAN-COM-010 — capability reuse and ownership non-duplication

### 6.1 Approved correction

The reuse-first principle remains represented in SWE.1 as a software-level ownership constraint so it cannot disappear from the SWE.1 → SWE.2 → SWE.3 trace chain.

Wayfarer-owned software shall not take unnecessary ownership of a generic capability when an approved Java/platform/adopted-plugin/external-library contract can adequately satisfy the applicable functional, compatibility, lifecycle, threading, security, licensing, operational, and maintainability requirements.

Project-owned implementation remains permitted when an identified approved requirement/design constraint cannot be adequately satisfied externally, or when Project ownership materially reduces relevant Product/integration risk.

The exact capability allocation, dependency/library selection, API contract, version, adapter boundary, alternatives assessment, and authoritative version-appropriate references are engineering/design controls governed by `GOV-ENG-001` in SWE.2/SWE.3 rather than being fixed by SWE.1.

### 6.2 Required SWE.1 decomposition

Add one Common software constraint: **Capability ownership non-duplication**.

The earlier conversational placeholder `SWE1-COMMON-001-CON-009` is not used because `DEC-REQ-002` already assigns that identifier to Redis non-authoritative coordination. To preserve identifier uniqueness and the no-renumbering rule in `DEC-REQ-003`, the integrated requirement is:

- `SWE1-COMMON-001-CON-010` — capability ownership non-duplication.

Compliance evidence shall use the capability/dependency assessment and authoritative references required by `GOV-ENG-001`.

## 7. Requirement-count effect

The prior approved overlay through `CAN-COM-005` produced a provisional 169 SWE.1 Product requirements. This decision adds seven atomic items:

- two additional lifecycle items under `CAN-COM-006`;
- two additional protected-operation items under `CAN-COM-007`;
- one additional migration-ownership item under `CAN-COM-008`;
- one additional audit/data-minimization item under `CAN-COM-009`;
- one capability-ownership non-duplication item under `CAN-COM-010`.

The provisional integrated total is therefore **176** Product requirements:

- CAP: 64
- CON: 59
- IFC: 14
- QLT: 39

## 8. Checkpoint and downstream obligations

This decision completes Owner review of the Common canonical section (`CAN-COM-001` through `CAN-COM-010`). At the Owner-directed repository checkpoint, ChatGPT shall consolidate `DEC-REQ-002` and this decision into the current canonical/Common package and update affected target/support documents consistently.

The next substantive canonical review item after checkpoint integration is `CAN-CORE-001 — V0.0.1 compatibility`.

All later gate restrictions remain unchanged.