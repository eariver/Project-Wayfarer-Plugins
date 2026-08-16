# V0.0.2 Redesign Status

Updated: 2026-08-16 JST  
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
  FROZEN REFERENCE / NOT REQUIREMENT OR DESIGN AUTHORITY

CANONICAL SOURCE:
  REVISION F / COMMON + CORE + MAIN CAN-MAIN-001–015 INTEGRATED

JOINT OWNER REVIEW:
  COMMON SECTION COMPLETE
  CORE SECTION COMPLETE
  MAIN CAN-MAIN-001–015 COMPLETE / INTEGRATED
  NEXT: CAN-MAIN-016 — Player-paid reissue

OWNER-APPROVED REVIEW CORRECTIONS:
  CAN-COM-001–005: DEC-REQ-002 / INTEGRATED
  CAN-COM-006–010: DEC-REQ-004 / INTEGRATED
  CAN-CORE-001–005: DEC-REQ-005 / INTEGRATED
  CAN-MAIN-001–005: DEC-REQ-006 / INTEGRATED
  CAN-MAIN-006–010: DEC-REQ-007 / INTEGRATED
  CAN-MAIN-011–015: DEC-REQ-008 / INTEGRATED

PROVISIONAL ACTIVE SWE.1 REQUIREMENT COUNT:
  178
  CAP 67 / CON 58 / IFC 14 / QLT 39

HISTORICAL SUPERSEDED IDENTIFIERS:
  SWE1-CORE-001-CON-004
  SWE1-MAIN-001-CON-008
  SWE1-MAIN-001-CON-009
  SWE1-MAIN-002-CAP-006
  SWE1-MAIN-003-QLT-002

CHECKPOINT CADENCE:
  MAIN CAN-MAIN-011–015 CHECKPOINT COMPLETE
  0 / 5 NEWLY APPROVED CLAUSES SINCE CHECKPOINT

OPEN ISSUES:
  9 RECORDS
  ISSUE-001 PARTIALLY RESOLVED; CONFIGURED-WORLD RECOVERY/RE-ENABLE DETAIL REMAINS OPEN
  ISSUE-002 REMAINS OPEN FOR EXACT SUPPORTED EXTERNAL REPAIR/MODIFICATION BOUNDARY

FULL POST-REVIEW AUTOMATED IDENTIFIER/SOURCE/COUNT AUDIT:
  PENDING BEFORE G1

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 THROUGH SWE.6:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / PR READY / MERGE TO MAIN / TAG / DEPLOY / RELEASE:
  NOT AUTHORIZED BY CURRENT G1 STATE
```

## Current controlling review records

- `DEC-REQ-002` / `DEC-REQ-004`: complete Common review.
- `DEC-REQ-005`: complete Core review.
- `DEC-REQ-006`: Main `CAN-MAIN-001`–`005` review and later reissue propagation.
- `DEC-REQ-007`: Main `CAN-MAIN-006`–`010` review.
- `DEC-REQ-008`: Main `CAN-MAIN-011`–`015` review covering configured evolution/reconciliation, Broken continuity, owner management, and protected full repair.
- `REV-SWE1-002`: joint Owner review log through `CAN-MAIN-015`.
- `TRC-SWE1-001` Revision G: source-to-requirement traceability for 178 active Product requirements.

## Integrated Main results through CAN-MAIN-015

### Authority, delivery, possession, modification, and progress

- Main capability deployment is configuration/allocation driven rather than bound to a historical backend name.
- MariaDB is authority for Main logical Growth Tool state; Minecraft is authority for current physical item state including durability/damage.
- Physical possession/storage/drop/death does not transfer logical ownership or create replacement entitlement.
- Owner-only Growth Tool use/progress is distinct from ordinary owner/non-owner physical possession.
- Durability restoration and enchantment modification are Wayfarer-controlled; V0.0.2 anvil/grindstone processing including rename is prohibited.
- Progress worlds use exact configured identity membership; defaults are `resource`, `resource_nether`, `resource_end`.
- Qualifying player-mined progress is provenance-neutral/repeatable and grants one uniform configured increment per completed eligible break; default increment is `1.00`.
- Progress numeric semantics are representation-independent and overflow/max-boundary safe.

### Evolution and reconciliation

- Material progression remains Wood → Stone → Iron → Diamond; thresholds `100/400/1200` are initial/default configuration.
- Post-Diamond `800 + 200n + 40n²` is an increment from the preceding threshold; cycle/caps are configurable approved defaults.
- Conceptual progression continues beyond effective caps.
- `FORTUNE` / `SILK_TOUCH` project different effective enchantments without erasing conceptual Fortune progression.
- One evolution evaluation/reconciliation uses one internally consistent approved configuration snapshot.
- Reconciliation preserves cumulative progress and authoritative identity/owner/lifecycle/delivery/branch/issuance/epoch; derived state may promote/demote.
- Reconciliation alone does not repair/revive. Material-change reconciliation preserves Minecraft-authoritative remaining-durability fraction where a current physical item is available.
- Actual qualifying progress crossing one or more evolution thresholds produces one progression-triggered full-durability recovery.

### Broken state

- Terminal durability preserves the logical/current authority and normally transitions `ACTIVE → BROKEN`; same-operation progression recovery is resolved first.
- Broken conversion is not reissue, epoch rotation, or a new delivery entitlement.
- `GRAY_DYE` is the initial/default Broken presentation, not authority.
- BROKEN cannot satisfy ACTIVE-only operations and remains durably recoverable across lifecycle interruption/restart.

### Owner management and Repair

- Required owner management entry is current-authority main-hand air right-click without block/entity target; off-hand does not open the GUI.
- The GUI presents logical/derived/effective state, applicable physical durability or Broken state, and repair availability/preview without fixing exact layout/text/lore.
- Viewing GUI/status/preview is non-committing and causes no protected financial/domain effect.
- Player Repair is full-repair only. Pricing formulas are V0.0.2 initial/default approved configuration rather than permanent constants.
- Repair requires explicit quote and confirmation; changed eligibility/price invalidates the stale confirmation rather than silently charging another amount.
- Repair benefit proceeds only after proven debit success through the Core-provided shared Waymark transaction contract.
- Successful ACTIVE/BROKEN repair preserves the same logical/current physical authority; BROKEN repair returns the same issuance/epoch to ACTIVE at full durability and is not reissue.
- Partial/ambiguous Repair benefit remains `UNKNOWN` until authorized reconciliation. Generic compensation uses Common rules; duplicate Main `QLT-002` is superseded.

## Known later Main propagation

`CAN-MAIN-016` remains unreviewed. Its owning review must preserve the earlier Owner direction that paid reissue safety does not depend on globally proving physical-item absence; successful authority rotation invalidates old physical instances, reissued items are fully repaired, and reissue pricing remains strictly above applicable repair without preserving the stale exact formula. Pending-delivery no-charge retry remains distinct.

`CAN-MAIN-017`–`019` remain unreviewed and must respect already integrated Common/Main authority, Broken, Repair, and physical-durability semantics.

## Repository checkpoint cadence

The Owner directs repository reflection after a logical section transition or five newly approved CAN clauses. The third Main checkpoint (`CAN-MAIN-011`–`015`) is integrated and the counter is reset to `0 / 5`.

## Required work before G1

1. Continue joint Owner review at `CAN-MAIN-016 — Player-paid reissue`.
2. Complete remaining Main, Frontier, Worlds Beyond, and Scope clause review.
3. Resolve or explicitly disposition all open issues.
4. Complete the accepted V0.0.1 public API/contract/migration inventory.
5. Reconcile controlled Project consistency inputs/runtime locks without silently importing behavior.
6. Run the complete post-review source/identifier/count/verification-intent audit and full SWE.1 self-review.
7. Present the corrected complete package for explicit G1 Owner approval.

## Authority restriction

This checkpoint records Owner-approved clause corrections and repository reflection only. It does not itself constitute G1 approval or authorization of SWE.2+, Product implementation, PR Ready transition, merge to `main`, tag, deployment, or release.
