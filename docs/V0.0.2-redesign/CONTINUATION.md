# V0.0.2 Redesign Work Continuation

Document ID: `GOV-CONTINUITY-001`  
Revision: D  
State: `IN_REVIEW`  
Updated: 2026-08-15 JST  
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
Next substantive review item: `CAN-MAIN-006 — Death behavior`

Always fetch the latest PR head before using the references below. A previously reported commit SHA is an immutable checkpoint/reference, not proof of the current head.

## 2. Ordered resumption path

Read these documents in order:

1. [`STATUS.md`](STATUS.md)
   - current process, gate, Main checkpoint result, provisional active requirement count, next clause, and prohibited downstream work;
2. [`REV-SWE1-002 — Joint Owner Review Log`](10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md)
   - reviewed CAN clauses, Owner determinations, checkpoint reset, later propagation, and next clause;
3. [`DEC-REQ-002 — Common Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-002-common-requirement-review-corrections.md)
   - immutable rationale for the first Common review set;
4. [`DEC-REQ-004 — Common Requirement Review Corrections 006–010`](08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md)
   - immutable rationale for the second Common review set;
5. [`DEC-REQ-005 — Core Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-005-core-requirement-review-corrections-001-005.md)
   - immutable rationale for the completed Core review;
6. [`DEC-REQ-006 — Main Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-006-main-requirement-review-corrections-001-005.md)
   - immutable rationale for reviewed Main capability lifecycle, Growth Tool logical/physical authority, delivery, possession/storage, and controlled modification semantics; also records mandatory later propagation to CAN-MAIN-006 and CAN-MAIN-016;
7. [`DEC-REQ-003 — Review Consolidation and Session Continuity Policy`](08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md)
   - checkpoint cadence, consolidation rules, authority order, and session-resumption procedure;
8. [`SWE1-SRC-002 — Canonical Mainline Requirements`](01-swe1-software-requirements-analysis/SWE1-SRC-002-canonical-mainline-requirements.md)
   - current canonical source Revision D; Common, Core, and Main CAN-MAIN-001–005 are integrated; later clauses remain subject to their owning reviews;
9. [`SWE1-INDEX-001 — SWE.1 Document Index`](01-swe1-software-requirements-analysis/SWE1-INDEX-001-document-index.md)
   - controlled SWE.1 document set and provisional 179-active-item count;
10. [`TRC-SWE1-001 — Source-to-Requirement Traceability`](07-traceability/TRC-SWE1-001-source-requirement-traceability.md)
   - current source allocation, superseded-item disposition, reviewed Main mapping, and explicit later-clause conflicts.

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

The first Main checkpoint is jointly reviewed and integrated:

- `CAN-MAIN-001` — topology-independent capability allocation and capability-scoped prerequisites;
- `CAN-MAIN-002` — Main logical Growth Tool authority, independent lifecycle/delivery/branch state, and Minecraft authority for current physical durability;
- `CAN-MAIN-003` — persistent physical representation identity resolved against current logical authority without fixing an exact PDC layout;
- `CAN-MAIN-004` — separate logical-entitlement/physical-delivery effects, same-entitlement pending retry, no fallback world-drop delivery, ordinary post-delivery drop unaffected;
- `CAN-MAIN-005` — logical-owner-only Growth Tool use, ordinary owner/non-owner possession/drop/pickup/storage permitted, Wayfarer-controlled durability restoration/enchantment modification, anvil/grindstone processing prohibited for V0.0.2 including rename.

`CAN-MAIN-006` and later clauses have not yet completed their owning review.

### 3.2 Integrated authority

`DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`, and `DEC-REQ-006` have been integrated into the applicable checkpoint documents and remain immutable rationale/decision records. They are not unresolved overlays for the reviewed clauses.

The integrated Main CAN-MAIN-001–005 checkpoint includes:

- `SWE1-SRC-002` Revision D;
- `SWE1-MAIN-001` Revision C;
- `SWE1-INDEX-001` Revision E;
- `TRC-SWE1-001` Revision E;
- `SWE1-VERIFY-001` Revision E;
- `REV-SWE1-002` Revision D;
- `SWE1-SRC-001` / Requirement Source Register Revision E;
- `DEC-REQ-006` Revision A;
- `STATUS.md` and this continuation record.

The provisional active Product requirement total is **179**:

```text
CAP: 66
CON: 60
IFC: 14
QLT: 39
```

`SWE1-MAIN-001` contains 24 provisional active requirements after the reviewed atomic split: CAP 11, CON 11, QLT 2. Newly introduced identifiers at this checkpoint are `SWE1-MAIN-001-CAP-010`, `CAP-011`, `CON-010`, and `CON-011`.

`SWE1-CORE-001-CON-004` remains a historical superseded identifier and is not part of the active count. It is not reused or renumbered; its behavior remains covered by Common `QLT-006`, Common `QLT-012`, and Core `QLT-001`.

A complete post-review automated identifier/source/count audit and full SWE.1 self-review are still pending before G1.

### 3.3 Reviewed Main authority boundary

For the Growth Tool:

- Main owns logical Growth Tool authority/state;
- MariaDB is durable authority for that logical state;
- Minecraft is authoritative for current physical item state, including current durability/damage;
- physical possession or storage does not transfer logical ownership;
- non-owner possession is allowed but non-owner Growth Tool use/progress is denied;
- reissue authority rotation may invalidate old physical instances without finding/deleting every old item.

The logical record therefore does not maintain a separate authoritative current-damage value. Physical durability can be inspected and changed only through the applicable Minecraft/platform state and approved Wayfarer-controlled operations.

### 3.4 Accepted V0.0.1 baseline boundary

Core review established that V0.0.1 implementation code is not frozen. The compatibility baseline is the controlled accepted public contract and controlled accepted migration-history artifacts.

Before G1, the Project still must complete the accepted V0.0.1 public API/contract/migration inventory. Until that inventory exists, final PASS evidence for the affected Core compatibility/migration requirements cannot be established.

### 3.5 Known later-clause propagation, not silently approved

The Main checkpoint records later Owner determinations without approving their owning clauses:

- `CAN-MAIN-006`: current death-drop suppression conflicts with reviewed Main possession/drop neutrality. The owning review must permit ordinary Growth Tool/Broken Tool player/entity death or despawn drop behavior. The separate no-raw-ItemStack / no-automatic-respawn-restoration semantics remain to be reviewed rather than assumed.
- `CAN-MAIN-016`: paid reissue safety must not depend on proving that the current physical item is absent from every permitted player/storage/world context. Successful reissue authority rotation shall make older physical instances stale/unusable. A reissued item is fully repaired. Paid reissue remains strictly more expensive than applicable repair, but the current exact `broken_repair_cost + full_repair_cost` formula is no longer fixed. Pending-delivery free retry remains a distinct obligation.

Other previously carried target propagation remains:

- fixed Frontier backend-role and specific-Core wording conflicts with approved topology/shared-owner principles and must be resolved in Frontier review;
- Worlds Beyond clauses containing literal `frontier_iris` remain subject to the approved configured gameplay-world direction during their later reviews;
- Main/WB feature-specific eligibility, domain mutation, entitlement/delivery, compensation trigger, and target sequencing remain with their owning clauses even though Core owns the V0.0.2 shared Waymark transaction contract;
- `CAN-WB-014` must decide exact pending-delivery versus refund/compensation policy after proven clear delivery failure;
- ISSUE-001 retains exact health/status and recovery/re-enable behavior after the configured Worlds Beyond world becomes available.

### 3.6 Gate restriction

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
- Core checkpoint: explicitly authorized after `CAN-CORE-001` through `CAN-CORE-005` and Core → Main transition;
- Main checkpoint 001–005: explicitly authorized after `CAN-MAIN-001` through `CAN-MAIN-005` reached five newly approved clauses.

Current cadence after the Main 001–005 checkpoint:

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
- requirement counts and superseded/conflict dispositions;
- review records;
- `STATUS.md`;
- this continuation document.

Decision records remain immutable rationale after their content is integrated. Do not renumber approved identifiers silently; new items receive unused identifiers and superseded identifiers are retained historically without reuse.

## 6. Immediate next action

Continue the clause-by-clause Owner review with:

`CAN-MAIN-006 — Death behavior`

Before explaining or changing it:

1. fetch PR `#18` and confirm the latest head;
2. reread this continuation chain, especially `STATUS.md`, `REV-SWE1-002`, `DEC-REQ-006`, and `DEC-REQ-003`;
3. inspect `CAN-MAIN-006` in `SWE1-SRC-002` Revision D and every derived requirement/disposition linked from `TRC-SWE1-001`;
4. treat the existing death-drop suppression as an explicit unreviewed conflict, not as approved behavior: `DEC-REQ-006` §7.1 requires ordinary Growth Tool/Broken Tool player/entity death/despawn drops not to be prohibited;
5. separately review the remaining current CAN-MAIN-006 statements that a raw in-memory ItemStack is not retained for respawn restoration and that the item is not automatically restored on respawn; do not infer their disposition solely from the death-drop decision;
6. preserve the reviewed Main authority model: physical possession/drop/storage is not logical ownership, and current physical durability belongs to Minecraft item state;
7. review exactly this one canonical clause using the established sequence: canonical content, SWE.1 decomposition, purpose, over/under-specification/fixation/responsibility/future-extension analysis, corrections, disposition;
8. do not advance to `CAN-MAIN-007` or silently resolve `CAN-MAIN-016` without Owner approval;
9. make no repository mutation until the Owner requests the next checkpoint or immediate correction.

## 7. Inconsistency stop rule

Stop substantive work and report the inconsistency when any of these disagree materially:

- this continuation document;
- `STATUS.md`;
- the current joint review log;
- approved decision records;
- integrated canonical/traceability state;
- PR head or branch identity.

Do not resolve such a conflict from memory or chat history alone. Explicitly documented deferred target conflicts, including CAN-MAIN-006 and CAN-MAIN-016 propagation from `DEC-REQ-006`, are not stop-rule violations; they are items for the owning later clause review.

## 8. Minimal new-session instruction

A new session can be started with only this instruction:

> Open `docs/V0.0.2-redesign/CONTINUATION.md` from the latest head of PR #18 in `eariver/Project-Wayfarer-Plugins`, follow its ordered references, and resume the listed next action without advancing any gate.