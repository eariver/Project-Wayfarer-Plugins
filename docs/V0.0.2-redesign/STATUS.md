# V0.0.2 Redesign Status

Updated: 2026-08-12 JST  
Branch: `redesign/V0.0.2-swe1-3`  
Draft PR: `#18`  
Single continuation entry point: [`CONTINUATION.md`](CONTINUATION.md)  
Baseline: Plugin V0.0.1 on `main`  
Current process: `SWE.1 Software Requirements Analysis`  
Current activity: clause-by-clause joint Owner review  
Current gate: `SWE1_OWNER_REVIEW_IN_PROGRESS`

## Executive status

```text
V0.0.1 BASELINE:
  ASSUMED ACCEPTED FOR INITIAL V0.0.2 ANALYSIS / COMPLETE CONTRACT+MIGRATION INVENTORY PENDING

PR #14 / LEGACY IMPLEMENTATION:
  FROZEN REFERENCE / NOT A REQUIREMENT OR DESIGN AUTHORITY

CANONICAL SOURCE:
  REVISION C / COMMON AND CORE SECTIONS INTEGRATED

INITIAL SWE.1 DECOMPOSITION:
  COMPLETE AS DRAFT

JOINT OWNER REVIEW:
  COMMON SECTION COMPLETE
  CORE SECTION COMPLETE
  NEXT: CAN-MAIN-001 — Deployment and lifecycle

OWNER-APPROVED REVIEW CORRECTIONS:
  CAN-COM-001–005: DEC-REQ-002 / INTEGRATED
  CAN-COM-006–010: DEC-REQ-004 / INTEGRATED
  CAN-CORE-001–005: DEC-REQ-005 / INTEGRATED

SESSION CONTINUITY AND CONSOLIDATION POLICY:
  APPROVED IN DEC-REQ-003

PROVISIONAL ACTIVE SWE.1 REQUIREMENT COUNT:
  175 AFTER INTEGRATED CORE CHECKPOINT
  CAP 64 / CON 58 / IFC 14 / QLT 39
  HISTORICAL CORE CON-004 SUPERSEDED / NOT REUSED

OPEN ISSUES:
  9 RECORDS
  ISSUE-001 PARTIALLY RESOLVED BY COMMON REVIEW; RECOVERY/RE-ENABLE DETAIL REMAINS OPEN

CHECKPOINT CADENCE:
  CORE CHECKPOINT COMPLETE
  0 / 5 NEWLY APPROVED CLAUSES SINCE CHECKPOINT

INITIAL SELF-REVIEW SNAPSHOT:
  HISTORICAL / SUPERSEDED AS CURRENT EVIDENCE BY OWNER-REVIEW CORRECTIONS

FULL POST-REVIEW AUTOMATED IDENTIFIER/SOURCE/COUNT AUDIT:
  PENDING BEFORE G1

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 THROUGH SWE.6:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / CANDIDATE / PR READY / MERGE / TAG / DEPLOY / RELEASE:
  NOT AUTHORIZED
```

## Session-resumption rule

A new chat session or recovered context shall begin from [`CONTINUATION.md`](CONTINUATION.md) on the latest PR #18 head and follow its ordered references. Conversation history is supplementary only.

The continuation document must be updated at every Owner-directed repository checkpoint together with this status document.

## Current controlling review records

- `CONTINUATION.md`
  - single living entry point for session resumption;
  - ordered references, current next action, checkpoint state, and stop rules.
- `08-decisions/DEC-REQ-002-common-requirement-review-corrections.md`
  - immutable rationale for Owner-approved `CAN-COM-001` through `CAN-COM-005` corrections;
  - integrated into the current canonical/Common package.
- `08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md`
  - immutable rationale for Owner-approved `CAN-COM-006` through `CAN-COM-010` corrections;
  - integrated into the current canonical/Common package.
- `08-decisions/DEC-REQ-005-core-requirement-review-corrections-001-005.md`
  - immutable rationale for Owner-approved `CAN-CORE-001` through `CAN-CORE-005` corrections;
  - integrated into Canonical Revision C and Core Revision C.
- `08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md`
  - Owner-approved consolidation cadence and session-continuity policy.
- `10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md`
  - Common and Core joint review are complete;
  - next clause is `CAN-MAIN-001`.

## Integrated Common-section results

### CAN-COM-001 — topology independence

- Capability ownership is separate from physical-server placement.
- Fixed server names/backend roles are not general runtime prerequisites.
- Actual configuration/schema/capability/context prerequisites can fail closed.

### CAN-COM-002 — shared ownership/dependency direction

- Shared access uses public contracts of approved shared owners rather than permanently forcing every shared capability into Core.
- Feature plugins do not depend on one another's internals; layering remains acyclic.

### CAN-COM-003 — authority separation

- MariaDB: Wayfarer durable business authority.
- Redis: non-authoritative coordination/cache/etc.
- MVI: assigned normal player-state authority.
- Waymark provider: balance authority behind approved shared contract.
- Minecraft runtime: physical runtime-state authority.
- Explicit logical/physical dual authority is allowed with feature-owned reconciliation.

### CAN-COM-004 — external-state and player-state boundaries

- Unsupported private/internal external state is not a normal contract.
- Wayfarer does not become general/long-term player inventory/profile authority.
- Future explicitly approved transactional cross-context transfer is not prohibited but is not V0.0.2 scope.
- Non-public API use requires an explicit Owner-approved isolated exception.
- Shared-foundation semantic neutrality is generalized.

### CAN-COM-005 — execution-context safety

- Use platform-authorized execution contexts rather than assuming one global main thread.
- No blocking external/durable I/O on tick-critical/region-critical contexts.
- Deferred completion revalidates mutable preconditions before protected mutation.

### CAN-COM-006 — lifecycle/fail-closed

- Fail-closed is capability-scoped by default.
- Prerequisite loss closes new operation admission/effects.
- Accepted operations receive finite completed/rejected/compensated/recoverable-unresolved disposition.
- Prior-lifecycle completions cannot mutate current/disabled state without current recognition and revalidation.
- Listener/command registration, async flush, and generation counters are not SWE.1 mechanisms.

### CAN-COM-007 — protected operation safety

- Stable logical operation/effect identity exists before first protected effect.
- Replay reuses existing operation/effect disposition and prevents duplicate protected effects.
- `UNKNOWN` is neither success nor clear failure and cannot automatically drive retry/compensation/benefit/authority changes.
- Compensation requires proven debit success plus proven downstream no-benefit/clear failure and has its own effect identity.

### CAN-COM-008 — migration ownership

- Durable-state owner, not table prefix, defines schema ownership.
- Migration sequence/applied state is independently attributable per owner.
- Accepted/supported migration history is immutable; later changes use new forward migrations.
- Empty installation and V0.0.1 upgrade must preserve/transform accepted durable state without destructive automatic reset.

### CAN-COM-009 — audit/data minimization

- Protected outcomes retain retrievable, correlatable audit evidence.
- Required evidence survives normal restart for the applicable retention period.
- Audit is not stronger authority than provider/domain evidence.
- Secret/credential/token storage is prohibited and unrelated personal/runtime state is minimized.

### CAN-COM-010 — reuse/ownership non-duplication

- Reuse-first remains a SWE.1 software constraint so it cannot disappear downstream.
- Generic capability is not duplicated in Project-owned code when an approved external/platform capability adequately satisfies requirements/constraints without unacceptable risk.
- Custom ownership remains permitted for an identified unmet requirement/constraint or material risk reduction.
- Exact dependency/API/version/adapter/reference selection remains under `GOV-ENG-001` in SWE.2/SWE.3.

## Integrated Core-section results

### CAN-CORE-001 — accepted V0.0.1 contract compatibility

- Preserve the controlled accepted external/public contract, not the V0.0.1 implementation.
- Compatibility covers source use, binary linkage, and documented externally observable semantics.
- Internal defects, structure, dependencies, validation, threading/lifecycle implementation, and undocumented behavior may be corrected/redesigned.
- Breaking exceptions must be explicitly scoped; V0.0.1 contract/migration inventory remains required before G1.

### CAN-CORE-002 — public-contract abstraction and type identity

- Public contracts do not expose implementation-specific types or raw internal authority/resource handles merely for convenience.
- Platform/external contract types are allowed when genuinely required and compatible with lifecycle/ownership/execution constraints.
- Required result is compatible runtime type identity within an in-process contract domain, not a fixed JAR/class-loader layout.

### CAN-CORE-003 — V0.0.2 shared Waymark transaction contract

- V0.0.2 shared Waymark transaction contract is explicitly allocated to Core, consistent with the accepted V0.0.1 Core-facing transaction surface.
- Core owns shared transaction coordination/provider interaction/inspection/reconciliation, not feature-specific eligibility/domain mutation/entitlement/final delivery.
- Waymark balance authority remains the provider; Wayfarer transaction records own only Wayfarer's logical operation/effect disposition.
- Provider evidence is interpreted only within supported provider guarantees.

### CAN-CORE-004 — ambiguity and no manufactured provider semantics

- Core reuses Common `UNKNOWN`/replay rules rather than duplicating them.
- Balance or aggregate provider state is not uncorrelated proof of one exact effect.
- Unsupported provider internals/side channels cannot manufacture stronger guarantees.
- Wayfarer operation/audit/reconciliation records remain valid but do not strengthen provider authority.
- `SWE1-CORE-001-CON-004` is superseded by deduplication; no behavior is relaxed.

### CAN-CORE-005 — Core schema evolution and migration history

- Core migrations may support approved correction/integrity/compatibility/evolution of Core-owned durable state, not only new capabilities.
- Core migrations do not carry another owner's durable domain.
- Controlled accepted V0.0.1 Core migration identity/order/byte content is immutable; later changes use new migration identities.
- Future migration framework/resource/executor details remain design choices subject to compatibility.

## Known propagation carried to later target review

The Common/Core checkpoints do not silently approve later target clauses. Current follow-ups include:

- `CAN-MAIN-001`: fixed Main-backend/specific-Core wording versus approved topology/shared-owner direction;
- `CAN-FRONTIER-001`: fixed Frontier-backend/specific-Core wording versus Common direction;
- `CAN-FRONTIER-002` and later WB clauses: literal `frontier_iris` wording versus the Owner-approved configurable Worlds Beyond gameplay-world direction;
- Main/WB financial clauses must use the reviewed V0.0.2 Core transaction boundary while retaining their own feature eligibility/domain/delivery/compensation semantics;
- `CAN-WB-014`: exact typed-pending-delivery versus refund/compensation priority after a proven clear delivery failure;
- `SWE1-ISSUE-001-ISSUE-001`: missing configured-world health/status and recovery/re-enable lifecycle detail.

## SWE.1 repository-checkpoint cadence

During clause-by-clause review, the Owner instructs repository reflection at either:

- transition between logical CAN sections; or
- completion of five newly approved CAN clauses since the previous repository checkpoint.

The Owner's explicit instruction controls the write. Reaching a boundary does not independently authorize mutation.

The 2026-08-12 Core checkpoint has reset the counter to `0 / 5`. The current logical section is MAIN.

## Required work before G1

1. Continue joint Owner review from `CAN-MAIN-001 — Deployment and lifecycle`.
2. Continue clause-by-clause review across Main, Frontier, Worlds Beyond, and Scope without silently resolving target-specific conflicts from Common/Core review.
3. Resolve or explicitly disposition the nine issue records, including remaining ISSUE-001 recovery/re-enable semantics.
4. Complete the accepted V0.0.1 public API/contract/migration inventory.
5. Reconcile controlled Project consistency inputs and runtime locks without silently adding behavior.
6. After all joint-review corrections are consolidated, rerun complete automated identifier/source/count/verification-intent checks and a full SWE.1 self-review.
7. Present the corrected complete package for explicit G1 Owner approval.

## Authority restriction

No document in the current branch authorizes SWE.2, SWE.3 construction, SWE.4 through SWE.6 execution, Product implementation, Candidate remediation, PR readiness transition, merge, tag, deployment, or release. Only the Owner may approve the next gate.