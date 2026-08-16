# V0.0.2 Redesign Work Continuation

Document ID: `GOV-CONTINUITY-001`  
Revision: F  
State: `IN_REVIEW`  
Updated: 2026-08-16 JST  
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
Next substantive review item: `CAN-MAIN-016 — Player-paid reissue`

Always fetch the latest PR head before using a previously reported commit SHA.

## 2. Ordered resumption path

Read these documents in order:

1. [`STATUS.md`](STATUS.md) — current gate, count, checkpoint, next clause, and restrictions.
2. [`REV-SWE1-002 — Joint Owner Review Log`](10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md) — Owner dispositions through CAN-MAIN-015.
3. [`DEC-REQ-006 — Main Requirement Review Corrections 001–005`](08-decisions/DEC-REQ-006-main-requirement-review-corrections-001-005.md).
4. [`DEC-REQ-007 — Main Requirement Review Corrections 006–010`](08-decisions/DEC-REQ-007-main-requirement-review-corrections-006-010.md).
5. [`DEC-REQ-008 — Main Requirement Review Corrections 011–015`](08-decisions/DEC-REQ-008-main-requirement-review-corrections-011-015.md).
6. [`DEC-REQ-002`](08-decisions/DEC-REQ-002-common-requirement-review-corrections.md), [`DEC-REQ-004`](08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md), and [`DEC-REQ-005`](08-decisions/DEC-REQ-005-core-requirement-review-corrections-001-005.md) for integrated Common/Core authority.
7. [`DEC-REQ-003 — Review Consolidation and Session Continuity Policy`](08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md).
8. [`SWE1-SRC-002 — Canonical Mainline Requirements`](01-swe1-software-requirements-analysis/SWE1-SRC-002-canonical-mainline-requirements.md) Revision F.
9. [`SWE1-INDEX-001`](01-swe1-software-requirements-analysis/SWE1-INDEX-001-document-index.md) Revision G.
10. [`TRC-SWE1-001`](07-traceability/TRC-SWE1-001-source-requirement-traceability.md) Revision G.
11. [`SWE1-VERIFY-001`](01-swe1-software-requirements-analysis/SWE1-VERIFY-001-verification-intent.md) Revision G.

## 3. Current authoritative state

### 3.1 Reviewed/integrated clauses

- Common: `CAN-COM-001` through `CAN-COM-010` complete.
- Core: `CAN-CORE-001` through `CAN-CORE-005` complete.
- Main: `CAN-MAIN-001` through `CAN-MAIN-015` complete.
- Next: `CAN-MAIN-016`.

The current canonical source is Revision F. `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`, `DEC-REQ-006`, `DEC-REQ-007`, and `DEC-REQ-008` are integrated controlling Owner decisions.

### 3.2 Provisional active requirement count

```text
TOTAL: 178
CAP:   67
CON:   58
IFC:   14
QLT:   39
```

Historical superseded identifiers retained without reuse:

- `SWE1-CORE-001-CON-004`
- `SWE1-MAIN-001-CON-008`
- `SWE1-MAIN-001-CON-009`
- `SWE1-MAIN-002-CAP-006`
- `SWE1-MAIN-003-QLT-002`

New active identifiers from the CAN-MAIN-011–015 checkpoint:

- `SWE1-MAIN-002-CAP-017`
- `SWE1-MAIN-002-QLT-007`
- `SWE1-MAIN-003-CAP-010`

A complete post-review automated identifier/source/count/verification-intent audit remains pending before G1.

### 3.3 Main authority and physical-item boundary

- Main owns logical Growth Tool authority/state; MariaDB is durable authority for logical state.
- Minecraft owns current physical item state including current durability/damage.
- Physical identity is resolved against current logical authority and current issuance/epoch.
- Physical possession/storage/drop/death/item lifecycle does not transfer logical ownership.
- Non-owner possession is allowed; non-owner Growth Tool use/progress is denied.
- Delivery failure and ordinary post-delivery item lifecycle are distinct: no fallback delivery drop, but ordinary later drops/storage/death behavior are allowed.
- Durability restoration and enchantment modification are Wayfarer-controlled; V0.0.2 anvil/grindstone processing including rename is prohibited.

### 3.4 Reviewed progress/evolution/reconciliation model

- Progress worlds are exact configured identities; V0.0.2 defaults are `resource`, `resource_nether`, `resource_end`.
- One completed qualifying player-mined `minecraft:mineable/pickaxe` block with the current authorized active Growth Pickaxe grants progress exactly once; natural/placed/generated/re-placed/repeated eligible mining is allowed.
- Progress numeric representation is not fixed to an internal scale/type/maximum; positive progress remains deterministic, monotonic, overflow-safe, and operable at any selected supported maximum.
- Each qualifying break uses one uniform configured positive logical increment; V0.0.2 default is `1.00`; block/category/ore/rarity weighting is withdrawn.
- Base material sequence is Wood → Stone → Iron → Diamond; thresholds `100/400/1200` are V0.0.2 initial/default configuration.
- Post-Diamond `800 + 200n + 40n²` is the initial/default increment from the preceding evolution threshold; mapping/caps are approved configurable defaults.
- `FORTUNE`/`SILK_TOUCH` affect effective enchantment projection without erasing conceptual Fortune progression.
- Evolution/reconciliation uses one internally consistent approved configuration snapshot.
- Reconciliation preserves cumulative progress and authoritative identity/owner/lifecycle/delivery/branch/issuance/epoch; derived state may promote/demote.
- Reconciliation alone does not repair/revive; material-change reconciliation preserves Minecraft-authoritative remaining-durability fraction where available.
- A real qualifying progress addition crossing one or more evolution thresholds produces one progression-triggered full recovery.

### 3.5 Reviewed Broken/management/Repair model

- Terminal durability does not destroy the logical Growth Tool. Same-operation evolution recovery is applied before terminal Broken decision; otherwise the same authority transitions `ACTIVE → BROKEN`.
- Broken conversion is not reissue, epoch rotation, or a new delivery entitlement. `GRAY_DYE` is initial/default Broken presentation only.
- BROKEN cannot satisfy ACTIVE-only operations and remains durably recoverable across lifecycle interruption/restart.
- Required owner management entry is current-authority main-hand air right-click without block/entity target; off-hand does not open the GUI.
- GUI status distinguishes logical/derived/effective state from physical presentation and does not fix exact wording/layout/slots/name/lore.
- Viewing status/repair preview is non-committing and causes no protected financial/domain effect.
- Player Repair is full-repair only. Pricing formulas are initial/default approved configuration.
- Repair requires explicit current quote and confirmation; changed state/price invalidates stale confirmation rather than silently charging another amount.
- Repair benefit proceeds only after proven debit success through Core's approved V0.0.2 shared Waymark transaction contract.
- Successful ACTIVE/BROKEN repair preserves the same current authority; BROKEN repair returns the same issuance/epoch to ACTIVE at maximum durability and is not reissue.
- Partial/ambiguous Repair benefit remains `UNKNOWN` until authorized reconciliation; generic compensation uses Common rules.

### 3.6 Known CAN-MAIN-016 propagation

`CAN-MAIN-016` must be corrected during its owning review:

- paid reissue safety must not depend on global proof that the current physical instance is absent across all permitted possession/storage/world contexts;
- successful authority rotation establishes a new physical issuance/current epoch and makes prior physical instances stale/unusable;
- successful reissue is fully repaired;
- paid reissue price remains strictly above the applicable repair price for the same logical tool/configuration, but the old exact `broken_repair_cost + full_repair_cost` formula is not fixed;
- pending-delivery no-charge retry remains a distinct obligation and is not paid reissue;
- allowed invocation context remains open issue `SWE1-ISSUE-001-ISSUE-007`.

### 3.7 Gate restriction

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
- Main `CAN-MAIN-011`–`015` checkpoint.

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

`CAN-MAIN-016 — Player-paid reissue`

Before disposition:

1. confirm latest PR #18 head;
2. inspect CAN-MAIN-016 and derived `SWE1-MAIN-003-CAP-006`, `CAP-007`, `CAP-008`, `CON-002`, `QLT-003`, and the reissue portion of `QLT-001`;
3. apply `DEC-REQ-006` §7.2: reissue safety is authority rotation, not global proof of physical absence;
4. preserve CAN-MAIN-004 pending-delivery free retry as separate from paid reissue;
5. compare reissue pricing against the now-reviewed configurable Repair pricing and retain only the approved strict-more-expensive invariant unless the Owner chooses another explicit formula;
6. preserve Common/Core protected-operation `UNKNOWN`, replay, compensation, and provider-guarantee boundaries;
7. resolve or retain open issue `SWE1-ISSUE-001-ISSUE-007` for allowed player invocation context without guessing;
8. review exactly one clause and do not advance without Owner approval;
9. do not mutate the repository again until the next Owner-directed checkpoint/immediate correction.

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
