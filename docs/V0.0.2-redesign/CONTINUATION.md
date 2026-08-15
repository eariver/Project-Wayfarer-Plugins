# V0.0.2 Redesign Work Continuation

Document ID: `GOV-CONTINUITY-001`  
Revision: E  
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
Next substantive review item: `CAN-MAIN-011 — Material and enchantment evolution`

Always fetch the latest PR head before using a previously reported commit SHA.

## 2. Ordered resumption path

Read these documents in order:

1. [`STATUS.md`](STATUS.md) — current gate, count, checkpoint, next clause, and restrictions.
2. [`REV-SWE1-002 — Joint Owner Review Log`](10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md) — Owner dispositions through CAN-MAIN-010.
3. [`DEC-REQ-006 — Main Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-006-main-requirement-review-corrections-001-005.md).
4. [`DEC-REQ-007 — Main Requirement Review Corrections 006–010`](08-decisions/DEC-REQ-007-main-requirement-review-corrections-006-010.md).
5. [`DEC-REQ-002`](08-decisions/DEC-REQ-002-common-requirement-review-corrections.md), [`DEC-REQ-004`](08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md), and [`DEC-REQ-005`](08-decisions/DEC-REQ-005-core-requirement-review-corrections-001-005.md) for integrated Common/Core authority.
6. [`DEC-REQ-003 — Review Consolidation and Session Continuity Policy`](08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md).
7. [`SWE1-SRC-002 — Canonical Mainline Requirements`](01-swe1-software-requirements-analysis/SWE1-SRC-002-canonical-mainline-requirements.md) Revision E.
8. [`SWE1-INDEX-001`](01-swe1-software-requirements-analysis/SWE1-INDEX-001-document-index.md) Revision F.
9. [`TRC-SWE1-001`](07-traceability/TRC-SWE1-001-source-requirement-traceability.md) Revision F.
10. [`SWE1-VERIFY-001`](01-swe1-software-requirements-analysis/SWE1-VERIFY-001-verification-intent.md) Revision F.

## 3. Current authoritative state

### 3.1 Reviewed/integrated clauses

- Common: `CAN-COM-001` through `CAN-COM-010` complete.
- Core: `CAN-CORE-001` through `CAN-CORE-005` complete.
- Main: `CAN-MAIN-001` through `CAN-MAIN-010` complete.
- Next: `CAN-MAIN-011`.

The current canonical source is Revision E. `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`, `DEC-REQ-006`, and `DEC-REQ-007` are integrated controlling Owner decisions.

### 3.2 Provisional active requirement count

```text
TOTAL: 176
CAP:   65
CON:   58
IFC:   14
QLT:   39
```

Historical superseded identifiers retained without reuse:

- `SWE1-CORE-001-CON-004`
- `SWE1-MAIN-001-CON-008`
- `SWE1-MAIN-001-CON-009`
- `SWE1-MAIN-002-CAP-006`

A complete post-review automated identifier/source/count/verification-intent audit remains pending before G1.

### 3.3 Main authority and physical-item boundary

- Main owns logical Growth Tool authority/state; MariaDB is durable authority for logical state.
- Minecraft owns current physical item state including current durability/damage.
- Physical identity is resolved against current logical authority and current issuance/epoch.
- Physical possession/storage/drop/death/item lifecycle does not transfer logical ownership.
- Non-owner possession is allowed; non-owner Growth Tool use/progress is denied.
- Delivery failure and ordinary post-delivery item lifecycle are distinct: no fallback delivery drop, but ordinary later drops/storage/death behavior are allowed.
- Durability restoration and enchantment modification are Wayfarer-controlled; V0.0.2 anvil/grindstone processing including rename is prohibited.

### 3.4 Reviewed progress model through CAN-MAIN-010

- Progress worlds are exact members of approved configuration. Initial V0.0.2 defaults: `resource`, `resource_nether`, `resource_end`.
- Similar-name/dimension/environment heuristics and implicit replacement-world adoption are prohibited.
- One completed qualifying player-mined `minecraft:mineable/pickaxe` block with the current authorized active Growth Pickaxe grants progress exactly once under applicable Survival/Adventure rules.
- Natural, player-placed, generated/plugin-created, Silk-Touch-re-placed, and repeated eligible mining are allowed; block provenance history is not required solely for progress.
- Non-player removal and nonqualifying/cancelled mining do not grant progress.
- Progress numeric representation is not fixed to `1000` internal units, Java `long`, or `Long.MAX_VALUE`; positive progress must remain monotonic, deterministic, overflow-safe, and operable at any selected supported maximum.
- `AMD-009` concrete `Long.MAX_VALUE` rule is superseded by `DEC-REQ-007`.
- Every qualifying break uses one uniform configured positive logical increment; V0.0.2 default is `1.00`.
- Block/category weights and ore/rarity multipliers are withdrawn.

### 3.5 Known later propagation

`CAN-MAIN-016` must still be corrected during its owning review:

- no global proof that the current physical instance is absent is required for paid reissue safety;
- successful authority rotation makes prior physical instances stale/unusable;
- new physical reissue is fully repaired;
- paid reissue price remains strictly above the applicable repair price, but the old exact formula is not fixed;
- pending-delivery no-charge retry remains distinct.

Later durability/reconciliation clauses must be reviewed against Minecraft current-durability authority and the representation-independent progress model.

Frontier/WB configured-world, topology/shared-owner, transaction/delivery, and open-issue propagation remains pending in their owning reviews.

### 3.6 Gate restriction

The following remain unauthorized by the current reviewed package:

- G1 approval;
- SWE.2/SWE.3;
- Product implementation;
- SWE.4–SWE.6 execution;
- PR Ready transition;
- merge to `main`, tag, deployment, or release.

## 4. Repository-checkpoint cadence

Completed checkpoints:

- Common checkpoint.
- Core checkpoint.
- Main `CAN-MAIN-001`–`005` checkpoint.
- Main `CAN-MAIN-006`–`010` checkpoint.

Current cadence:

```text
NEWLY APPROVED CLAUSES SINCE CHECKPOINT:
  0 / 5
CURRENT LOGICAL SECTION:
  MAIN
```

A later section transition or five newly approved clauses creates a checkpoint candidate; repository mutation still requires explicit Owner instruction.

## 5. Immediate next action

Review exactly:

`CAN-MAIN-011 — Material and enchantment evolution`

Before disposition:

1. confirm latest PR #18 head;
2. inspect CAN-MAIN-011 and its derived `SWE1-MAIN-002-CAP-007`, `CAP-008`, `CON-003`, and `CAP-009`;
3. distinguish Product-visible material/enchantment evolution from fixed formulas/cycles that may be balance/configuration values;
4. check consistency with uniform per-break progress and representation-independent numeric semantics;
5. check branch ownership/administrative selection against already reviewed logical branch state;
6. review exactly one clause and do not advance without Owner approval;
7. do not mutate the repository again until the next Owner-directed checkpoint/immediate correction.

## 6. Work required before G1

- finish remaining Main, Frontier, Worlds Beyond, and Scope clause review;
- resolve/accept all open issues;
- complete accepted V0.0.1 public API/contract/migration inventory;
- reconcile controlled Project consistency/runtime-lock inputs;
- run complete final automated source/identifier/count/verification-intent audit and full SWE.1 self-review;
- obtain explicit Owner G1 approval.

## 7. Inconsistency stop rule

If `CONTINUATION.md`, `STATUS.md`, `REV-SWE1-002`, approved decisions, canonical/traceability state, or PR head materially disagree, stop substantive review and report the inconsistency rather than guessing from chat history.

## 8. Minimal new-session instruction

> Open `docs/V0.0.2-redesign/CONTINUATION.md` from the latest head of PR #18 in `eariver/Project-Wayfarer-Plugins`, follow its ordered references, and resume the listed next action without advancing any gate.
