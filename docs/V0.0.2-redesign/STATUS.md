# V0.0.2 Redesign Status

Updated: 2026-08-15 JST  
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
  REVISION E / COMMON + CORE + MAIN CAN-MAIN-001–010 INTEGRATED

JOINT OWNER REVIEW:
  COMMON SECTION COMPLETE
  CORE SECTION COMPLETE
  MAIN CAN-MAIN-001–010 COMPLETE / INTEGRATED
  NEXT: CAN-MAIN-011 — Material and enchantment evolution

OWNER-APPROVED REVIEW CORRECTIONS:
  CAN-COM-001–005: DEC-REQ-002 / INTEGRATED
  CAN-COM-006–010: DEC-REQ-004 / INTEGRATED
  CAN-CORE-001–005: DEC-REQ-005 / INTEGRATED
  CAN-MAIN-001–005: DEC-REQ-006 / INTEGRATED
  CAN-MAIN-006–010: DEC-REQ-007 / INTEGRATED

PROVISIONAL ACTIVE SWE.1 REQUIREMENT COUNT:
  176
  CAP 65 / CON 58 / IFC 14 / QLT 39

HISTORICAL SUPERSEDED IDENTIFIERS:
  SWE1-CORE-001-CON-004
  SWE1-MAIN-001-CON-008
  SWE1-MAIN-001-CON-009
  SWE1-MAIN-002-CAP-006

CHECKPOINT CADENCE:
  MAIN CAN-MAIN-006–010 CHECKPOINT COMPLETE
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
- `DEC-REQ-007`: Main `CAN-MAIN-006`–`010` review, including death/item-lifecycle neutrality, configured progress worlds, provenance-neutral/repeated mining, representation-independent numeric safety, and uniform progress.
- `REV-SWE1-002`: joint Owner review log through `CAN-MAIN-010`.
- `TRC-SWE1-001` Revision F: source-to-requirement traceability for 176 active Product requirements.

## Integrated Main results through CAN-MAIN-010

### Authority, delivery, possession, and modification

- Main capability deployment is configuration/allocation driven rather than bound to a historical backend name.
- MariaDB is authority for Main logical Growth Tool state; Minecraft is authority for current physical item state including durability/damage.
- Logical lifecycle, delivery, and branch states are separate.
- Physical persistent identity is resolved against current logical authority; presentation/possession alone is not authority.
- Initial logical entitlement and physical delivery are separate effects; failed delivery remains the same durable pending entitlement and does not use fallback world drop.
- Owner binding restricts Growth Tool use/progress, not ordinary possession/storage.
- Owner and non-owner may ordinarily possess, drop, pick up, and store the item without changing logical ownership.
- Durability restoration and enchantment modification are Wayfarer-controlled; anvil/grindstone processing including rename is prohibited in V0.0.2.

### Death/respawn and physical lifecycle

- Wayfarer does not suppress ordinary player/entity death or item-lifecycle drops merely because the item is managed.
- Death, respawn, physical loss/destruction, or non-observation does not create a new entitlement, rotate authority, or change logical ownership/delivery status.
- Respawn alone does not restore a previously delivered Growth Tool; replacement uses an authorized recovery/reissue flow.
- Historical Main `CON-008` and `CON-009` are superseded and not reused.

### Progress worlds and qualifying mining

- Progress uses exact membership in an approved configured world allowlist; V0.0.2 defaults are `resource`, `resource_nether`, and `resource_end`.
- Similar-name, dimension/environment, or historical naming heuristics do not adopt substitute worlds.
- One completed qualifying player-mined pickaxe-tag block grants progress exactly once.
- Natural, player-placed, generator/plugin-created, Silk-Touch-re-placed, and repeated eligible mining are permitted; block provenance tracking is not required solely for progress.
- Creative/Spectator, non-completing cancelled/denied mining, and non-player removal do not grant progress.

### Progress value and balance

- Progress numeric semantics are representation-independent; `1000` internal units, Java `long`, and `Long.MAX_VALUE` are not Product contracts.
- Positive progress is monotonic and cannot wrap/corrupt; bounded maximum states remain defined and operable.
- `AMD-009` concrete `Long.MAX_VALUE` rule is superseded by `DEC-REQ-007`.
- Every qualifying break receives the same configured positive logical increment; V0.0.2 default is `1.00`.
- Block/category weights, ore multipliers, and rarity bonuses are withdrawn. Historical Main `CAP-006` is superseded and not reused.

## Known later Main propagation

`CAN-MAIN-016` remains unreviewed. Its owning review must preserve the earlier Owner direction that paid reissue safety does not depend on globally proving physical-item absence; successful authority rotation invalidates old physical instances, reissued items are fully repaired, and reissue pricing remains strictly above applicable repair without preserving the stale exact formula.

Later Main durability/reconciliation clauses must also be reviewed against the already approved rule that current durability is Minecraft physical authority and against the representation-independent progress model.

## Repository checkpoint cadence

The Owner directs repository reflection after a logical section transition or five newly approved CAN clauses. The second Main checkpoint (`CAN-MAIN-006`–`010`) is now integrated and the counter is reset to `0 / 5`.

## Required work before G1

1. Continue joint Owner review at `CAN-MAIN-011 — Material and enchantment evolution`.
2. Complete remaining Main, Frontier, Worlds Beyond, and Scope clause review.
3. Resolve or explicitly disposition all open issues.
4. Complete the accepted V0.0.1 public API/contract/migration inventory.
5. Reconcile controlled Project consistency inputs/runtime locks without silently importing behavior.
6. Run the complete post-review source/identifier/count/verification-intent audit and full SWE.1 self-review.
7. Present the corrected complete package for explicit G1 Owner approval.

## Authority restriction

This checkpoint records Owner-approved clause corrections and repository reflection only. It does not itself constitute G1 approval or authorization of SWE.2+, Product implementation, PR Ready transition, merge to `main`, tag, deployment, or release.
