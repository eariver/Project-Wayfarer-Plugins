# V0.0.2 Redesign Work Continuation

Document ID: `GOV-CONTINUITY-001`  
Revision: C  
State: `IN_REVIEW`  
Updated: 2026-08-12 JST  
Maintainer: ChatGPT  
Reviewer: Project Owner  
Applicable Product: Plugin V0.0.2 redesign

## 1. Start here

This is the single living entry point for resuming the Project Wayfarer Plugin V0.0.2 redesign after a chat-session change, context loss, or work interruption.

Repository: `eariver/Project-Wayfarer-Plugins`  
Branch: `redesign/V0.0.2-swe1-3`  
Draft PR: `#18`  
Current process: `SWE.1 Software Requirements Analysis`  
Current gate: `SWE1_OWNER_REVIEW_IN_PROGRESS`  
Current review section: `MAIN`  
Next substantive review item: `CAN-MAIN-001 — Deployment and lifecycle`

Always fetch the latest PR head before using the references below. A previously reported commit SHA is an immutable checkpoint/reference, not proof of the current head.

## 2. Ordered resumption path

Read these documents in order:

1. [`STATUS.md`](STATUS.md)
   - current process, gate, Common/Core checkpoint result, provisional active requirement count, next clause, and prohibited downstream work;
2. [`REV-SWE1-002 — Joint Owner Review Log`](10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md)
   - reviewed CAN clauses, Owner determinations, checkpoint reset, and next clause;
3. [`DEC-REQ-002 — Common Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-002-common-requirement-review-corrections.md)
   - immutable rationale for the first Common review set;
4. [`DEC-REQ-004 — Common Requirement Review Corrections 006–010`](08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md)
   - immutable rationale for the second Common review set;
5. [`DEC-REQ-005 — Core Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-005-core-requirement-review-corrections-001-005.md)
   - immutable rationale for the completed Core review, including accepted-contract compatibility, public API boundaries, Waymark transaction allocation, ambiguity containment, and migration discipline;
6. [`DEC-REQ-003 — Review Consolidation and Session Continuity Policy`](08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md)
   - checkpoint cadence, consolidation rules, authority order, and session-resumption procedure;
7. [`SWE1-SRC-002 — Canonical Mainline Requirements`](01-swe1-software-requirements-analysis/SWE1-SRC-002-canonical-mainline-requirements.md)
   - current canonical source Revision C; Common and Core sections are integrated, later sections remain subject to their clause-by-clause review;
8. [`SWE1-INDEX-001 — SWE.1 Document Index`](01-swe1-software-requirements-analysis/SWE1-INDEX-001-document-index.md)
   - controlled SWE.1 document set and provisional 175-active-item count;
9. [`TRC-SWE1-001 — Source-to-Requirement Traceability`](07-traceability/TRC-SWE1-001-source-requirement-traceability.md)
   - current Common/Core-checkpoint source allocation, superseded-item disposition, and target propagation.

Follow further references from those documents rather than relying on chat history.

## 3. Current authoritative state

### 3.1 Reviewed clauses

The complete Common canonical section is jointly reviewed and integrated:

- `CAN-COM-001` through `CAN-COM-010` — correction directions approved and integrated under `DEC-REQ-002` / `DEC-REQ-004`.

The complete Core canonical section is jointly reviewed and integrated:

- `CAN-CORE-001` — correction direction approved with Owner refinement: preserve accepted V0.0.1 public contract, not implementation;
- `CAN-CORE-002` — correction direction approved;
- `CAN-CORE-003` — correction direction approved; V0.0.2 shared Waymark transaction contract allocated to Core;
- `CAN-CORE-004` — correction direction approved; duplicate Core `UNKNOWN` requirement superseded by deduplication;
- `CAN-CORE-005` — correction direction approved.

`CAN-MAIN-001` onward has not yet been jointly reviewed.

### 3.2 Integrated authority

`DEC-REQ-002`, `DEC-REQ-004`, and `DEC-REQ-005` have been integrated into the applicable checkpoint documents and remain immutable rationale/decision records. They are not unresolved overlays for the integrated Common/Core text.

The integrated Core checkpoint includes:

- `SWE1-SRC-002` Revision C;
- `SWE1-COMMON-001` Revision B (unchanged by Core review);
- `SWE1-CORE-001` Revision C;
- `SWE1-INDEX-001` Revision D;
- `TRC-SWE1-001` Revision D;
- `SWE1-VERIFY-001` Revision D;
- `REV-SWE1-002` Revision C;
- `SWE1-SRC-001` / Requirement Source Register current checkpoint revision;
- `STATUS.md` and this continuation record.

The provisional active Product requirement total is **175**:

```text
CAP: 64
CON: 58
IFC: 14
QLT: 39
```

`SWE1-CORE-001-CON-004` is a historical superseded identifier and is not part of the active count. It is not reused or renumbered; its behavior remains covered by Common `QLT-006`, Common `QLT-012`, and Core `QLT-001`.

A complete post-review automated identifier/source/count audit and full SWE.1 self-review are still pending before G1.

### 3.3 Accepted V0.0.1 baseline boundary

Core review explicitly established that V0.0.1 implementation code is not frozen. The compatibility baseline is the controlled accepted public contract and controlled accepted migration-history artifacts.

Before G1, the Project still must complete the accepted V0.0.1 public API/contract/migration inventory. Until that inventory exists, final PASS evidence for the affected Core compatibility/migration requirements cannot be established.

### 3.4 Known target-specific propagation, not silently resolved

The Common/Core checkpoints record but do not pre-approve these later target issues:

- fixed Main/Frontier backend-role and specific-Core wording in later target clauses conflicts with approved topology/shared-owner principles and must be resolved in the owning target review;
- Worlds Beyond target clauses still containing literal `frontier_iris` are subject to the approved configured gameplay-world direction during their later reviews;
- V0.0.2 shared Waymark transaction ownership is now allocated to Core, but Main/WB feature-specific eligibility, domain mutation, entitlement/delivery, compensation trigger, and target sequencing remain for their own reviews;
- `CAN-WB-014` must decide the exact pending-delivery versus refund/compensation policy after proven clear delivery failure;
- ISSUE-001 retains the exact health/status and recovery/re-enable behavior after the configured Worlds Beyond world becomes available.

### 3.5 Gate restriction

The following remain unauthorized:

- G1 approval;
- SWE.2 and SWE.3;
- construction or Product implementation;
- SWE.4 through SWE.6 execution;
- PR readiness transition, merge, tag, deployment, or release.

## 4. Repository-checkpoint policy and current cadence

During SWE.1 clause review, the Owner instructs repository reflection at either:

- a transition between logical CAN sections; or
- completion of five newly approved CAN clauses since the previous repository checkpoint.

The Owner's explicit instruction authorizes the write. Reaching a boundary by itself does not.

Completed checkpoints:

- Common checkpoint: explicitly authorized after `CAN-COM-006` through `CAN-COM-010` and Common → Core transition;
- Core checkpoint: explicitly authorized after `CAN-CORE-001` through `CAN-CORE-005` and Core → Main transition.

Current cadence after the Core checkpoint:

```text
NEWLY APPROVED CLAUSES SINCE CHECKPOINT:
  0 / 5

CURRENT LOGICAL SECTION:
  MAIN
```

At each later instructed checkpoint:

1. update the joint review log;
2. create/update the applicable approved correction decision record;
3. update `STATUS.md`;
4. update this `CONTINUATION.md`;
5. perform integrated consolidation when directed or needed to keep the approved overlay safely applicable.

## 5. Integrated consolidation package

When an integrated checkpoint is performed, update the affected package consistently:

- canonical source revision;
- affected SWE.1 requirement documents;
- document index;
- source traceability;
- verification intent;
- issues/conflicts where affected;
- source register where authority/revision metadata changes;
- requirement counts and superseded-item dispositions;
- review records;
- `STATUS.md`;
- this continuation document.

Decision records remain as immutable rationale after their content is integrated. Do not renumber approved identifiers silently; new items receive unused identifiers and superseded identifiers are retained historically without reuse.

## 6. Immediate next action

Continue the clause-by-clause Owner review with:

`CAN-MAIN-001 — Deployment and lifecycle`

Before explaining or changing it:

1. fetch PR `#18` and confirm the latest head;
2. reread this continuation chain, especially `STATUS.md`, `REV-SWE1-002`, `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`, and `DEC-REQ-003`;
3. inspect `CAN-MAIN-001` in `SWE1-SRC-002` Revision C and every derived requirement/disposition linked from `TRC-SWE1-001`;
4. distinguish already integrated Common/Core constraints from the still-unreviewed Main target wording;
5. specifically review the known conflict between fixed `Main backend` / required `Wayfarer_Core` wording and the approved topology/shared-capability ownership rules;
6. review exactly one canonical clause using the established sequence: canonical content, SWE.1 decomposition, purpose, over/under-specification/fixation/responsibility/future-extension analysis, corrections, disposition;
7. do not advance to another clause without Owner approval;
8. make no repository mutation until the Owner requests the next checkpoint or immediate correction.

## 7. Inconsistency stop rule

Stop substantive work and report the inconsistency when any of these disagree materially:

- this continuation document;
- `STATUS.md`;
- the current joint review log;
- approved decision records;
- integrated canonical/traceability state;
- PR head or branch identity.

Do not resolve such a conflict from memory or chat history alone. Explicitly documented deferred target conflicts are not stop-rule violations; they are items for the owning later clause review.

## 8. Minimal new-session instruction

A new session can be started with only this instruction:

> Open `docs/V0.0.2-redesign/CONTINUATION.md` from the latest head of PR #18 in `eariver/Project-Wayfarer-Plugins`, follow its ordered references, and resume the listed next action without advancing any gate.