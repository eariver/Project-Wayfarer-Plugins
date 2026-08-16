# Main Requirement Review Corrections — CAN-MAIN-006 through CAN-MAIN-010

Document ID: `DEC-REQ-007`  
Revision: A  
State: `APPROVED`  
Date: 2026-08-15 JST  
Author: ChatGPT  
Approver: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Affected source: `SWE1-SRC-002` Revision D  
Affected SWE.1 documents: `SWE1-MAIN-001`, `SWE1-MAIN-002`, `TRC-SWE1-001`, `SWE1-VERIFY-001`, `SWE1-INDEX-001`

## 1. Purpose

Record the Owner-approved correction directions resulting from joint review of `CAN-MAIN-006` through `CAN-MAIN-010` and control their integration at the second Main repository checkpoint.

This decision does not approve G1, SWE.2/SWE.3, Product implementation, verification execution, PR readiness, merge to `main`, tag, deployment, or release.

## 2. CAN-MAIN-006 — death, respawn, and physical-item lifecycle neutrality

Wayfarer_Main shall not suppress or specially redirect ordinary supported Growth Tool/Broken Tool death or entity/item lifecycle drops merely because the item is managed. Death, respawn, physical loss, destruction, or failure to observe the delivered physical representation does not by itself change logical ownership, delivery state, current physical issuance, or authority epoch and does not create a replacement entitlement.

Respawn alone does not cause automatic Growth Tool restoration. Replacement of a previously delivered item occurs only through an applicable authorized recovery/reissue operation. SWE.1 does not prescribe temporary raw `ItemStack` retention or event-list manipulation.

`SWE1-MAIN-001-CON-008` is superseded because its death-drop suppression policy is withdrawn and the approved drop neutrality is covered by `SWE1-MAIN-001-CON-007`. `SWE1-MAIN-001-CON-009` is superseded by deduplication because no-implicit-replacement semantics are covered by `SWE1-MAIN-001-CON-005`.

## 3. CAN-MAIN-007 — configured exact progress-world boundary

Growth Tool progress is eligible only when the current platform world identity exactly matches an entry in the approved progress-world configuration. The initial V0.0.2 supplied/default allowlist contains `resource`, `resource_nether`, and `resource_end`.

World eligibility shall not be inferred from prefix/suffix/substring similarity, dimension/environment alone, historical naming convention, or another unapproved heuristic. Absence of one configured world does not implicitly substitute another world and does not by itself invalidate other independently valid configured progress worlds.

`SWE1-MAIN-002-CAP-001` and `CON-001` are revised accordingly; no new requirement is added.

## 4. CAN-MAIN-008 — qualifying player-mined block progress

One completed qualifying player-caused block break grants the applicable Growth Tool progress exactly once when the world is eligible, the block is classified by the adopted Minecraft tag contract as `minecraft:mineable/pickaxe`, the physical tool used for the mining action resolves to the player's current authorized `ACTIVE` Growth Pickaxe, and the Survival/Adventure completion rule is satisfied.

Creative/Spectator activity, denied/cancelled mining that does not complete a qualifying break, and non-player-mining block removal do not grant progress. Product requirements do not depend on specific editor/plugin names for this exclusion.

Block provenance does not affect eligibility. Naturally generated, player-placed, generator/plugin-created, and Silk-Touch-collected-and-re-placed blocks remain eligible when all other conditions are met. Repeated mining is not prohibited by this requirement and Wayfarer is not required to maintain block-placement/provenance history solely for Growth Tool progress.

`SWE1-MAIN-002-CAP-002`, `CAP-003`, and `CON-002` are revised without changing the requirement count.

## 5. CAN-MAIN-009 — deterministic cumulative progress and numeric-boundary safety

Cumulative progress remains one logical non-negative quantity whose accumulation, durable persistence/reload, threshold comparison, presentation, and applicable reconciliation are deterministic and do not depend on one prescribed physical numeric encoding.

An accepted positive addition shall not reduce progress or cause wraparound, negative overflow, corruption, or undefined behavior. If the selected representation is bounded, reaching its supported maximum produces a defined safe state and subsequent additions remain safe. Applicable threshold/evolution, presentation, persistence/reload, and reconciliation behavior remains defined at the supported maximum.

The prior `1.000 = 1000 internal units`, Java `long`, and `Long.MAX_VALUE` fixation is removed from SWE.1. The earlier `AMD-009` concrete `Long.MAX_VALUE` saturation rule is superseded by this Owner-approved representation-independent overflow-safety requirement.

`SWE1-MAIN-002-CAP-004`, `QLT-001`, and `QLT-002` are revised. Later requirements that mention `Long.MAX_VALUE` shall interpret the approved boundary as any supported maximum until their owning clause review removes the stale implementation-specific wording.

## 6. CAN-MAIN-010 — uniform qualifying-break progress increment

Every qualifying block break under `CAN-MAIN-008` contributes the same configured positive logical progress increment. The initial V0.0.2 supplied/default increment is `1.00` logical progress per qualifying break.

Progress does not vary by block material, category, ore classification, rarity, provenance, generation source, or equivalent block-specific characteristic. Block-specific base weights, ore multipliers, rarity multipliers, and equivalent material-dependent progression modifiers are withdrawn unless a later approved requirement explicitly reintroduces such behavior.

`SWE1-MAIN-002-CAP-005` is revised to a uniform configurable progress increment. `SWE1-MAIN-002-CAP-006` is superseded by Owner correction because ore multipliers are no longer part of the Product requirement. The historical identifier is not reused.

## 7. Requirement-count effect

The prior checkpoint contained 179 provisional active Product requirements.

This review supersedes three active items without adding a new item:

- `SWE1-MAIN-001-CON-008` — death-drop suppression withdrawn;
- `SWE1-MAIN-001-CON-009` — no-automatic-respawn-restoration deduplicated into `CON-005`;
- `SWE1-MAIN-002-CAP-006` — ore multipliers withdrawn.

The provisional active total becomes **176**:

- CAP: 65
- CON: 58
- IFC: 14
- QLT: 39

Historical superseded identifiers remain traceable and are not reused or renumbered.

## 8. Checkpoint and next review item

The Owner approved `CAN-MAIN-006` through `CAN-MAIN-010`, satisfying five newly approved clauses since the preceding checkpoint and explicitly requested repository reflection.

The next substantive canonical review item is `CAN-MAIN-011 — Material and enchantment evolution`.

All existing gate restrictions remain unchanged unless the Owner separately and explicitly advances those gates.