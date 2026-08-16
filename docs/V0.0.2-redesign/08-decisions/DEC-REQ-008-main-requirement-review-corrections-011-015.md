# Main Requirement Review Corrections 011–015

Document ID: `DEC-REQ-008`  
Revision: A  
State: `APPROVED_OWNER_DECISION`  
Date: 2026-08-16 JST  
Prepared by: ChatGPT  
Reviewer / approver: Project Owner  
Applicable process: SWE.1 Software Requirements Analysis  
Applicable Product: Plugin V0.0.2 redesign

## 1. Decision

The Project Owner approved the correction directions produced by the clause-by-clause joint review of `CAN-MAIN-011` through `CAN-MAIN-015`.

This decision is the controlling rationale for the integrated checkpoint covering material/enchantment evolution, configuration reconciliation, Broken-state continuity, the owner management interface, and owner-paid full repair.

The corrections preserve Product-visible semantics while removing unnecessary permanent fixation of balance values, implementation mechanisms, physical metadata details, and duplicated protected-operation rules.

## 2. CAN-MAIN-011 — Configured material and enchantment evolution

Approved direction:

- The V0.0.2 base-material progression remains `Wood → Stone → Iron → Diamond` and begins at Wood.
- Progress-based automatic material evolution does not extend beyond Diamond unless a later approved requirement adds another tier.
- `100`, `400`, and `1200` are the V0.0.2 initial/supplied material-threshold defaults, not permanent untunable Product constants.
- The post-Diamond definition `800 + 200n + 40n²`, beginning at `n=1`, is an increment from the preceding evolution threshold, not an absolute cumulative threshold; it is the initial/supplied default schedule.
- The initial/supplied repeating post-Diamond mapping is `Efficiency → Unbreaking → Efficiency → Unbreaking → Fortune`.
- The V0.0.2 conceptual progression family is limited to Efficiency, Unbreaking, and Fortune unless later approved requirements extend it.
- Effective caps are approved configuration. Initial/supplied defaults remain Efficiency 10, Unbreaking 10, Fortune 5, and Silk Touch 1.
- Reaching an effective cap does not stop cumulative progress, evolution count, or conceptual enchantment progression.
- Default branch remains `FORTUNE`.
- In `FORTUNE`, the capped conceptual Fortune value is applied and Silk Touch is not applied.
- In `SILK_TOUCH`, Silk Touch I is applied and Fortune is not applied to the physical item, while conceptual Fortune progression continues.
- Returning to `FORTUNE` reapplies the then-current conceptual Fortune value subject to the effective cap.
- Authorized administration may select `FORTUNE` or `SILK_TOUCH`; ordinary player-paid branch switching remains outside V0.0.2 scope.
- Thresholds, increment parameters/mapping, and effective caps are approved configuration rather than permanent numeric Product constants.

Derived requirement action:

- revise `SWE1-MAIN-002-CAP-007` as configured base-material evolution;
- revise `SWE1-MAIN-002-CAP-008` as configured post-Diamond evolution thresholds;
- add `SWE1-MAIN-002-CAP-017 — Configured post-Diamond enchantment mapping`;
- revise `SWE1-MAIN-002-CON-003` for configured effective caps with conceptual continuation;
- revise `SWE1-MAIN-002-CAP-009` for branch-dependent effective enchantment projection.

## 3. CAN-MAIN-012 — Deterministic evolution evaluation and configuration reconciliation

Approved direction:

- One evaluation/reconciliation uses one internally consistent approved evolution-configuration snapshot and does not mix dependent values from different configuration states.
- Cumulative progress is unchanged by configuration reconciliation.
- Derived material tier, conceptual evolution state, effective enchantments, and next-threshold state are recalculated from the effective approved configuration and current authoritative branch.
- Promotion and demotion are allowed as derived outcomes of a configuration change.
- Configuration reconciliation alone does not change logical tool identity, owner, lifecycle, delivery state, active branch, current physical issuance identity, or epoch.
- SWE.1 does not require eager global scanning/reconciliation of all persisted or physically existing tools; eager/lazy/load-time strategies remain downstream design choices.
- Reconciliation alone does not repair the tool and does not revive `BROKEN` to `ACTIVE`.
- When reconciliation changes the material of a current authorized `ACTIVE` physical pickaxe, the Minecraft-authoritative remaining-durability fraction is preserved across the new material capacity, subject to discrete quantization and retaining at least one remaining point.
- No separate authoritative durability value is invented when the current physical representation is unavailable.
- Within normal progression, an accepted qualifying progress addition that crosses one or more configured material/post-Diamond evolution thresholds produces one progression-triggered full-durability restoration.
- A progress addition that crosses no evolution threshold produces no progression-triggered durability restoration.
- Explicit repair/reissue restoration remains governed by those owning requirements.
- Evaluation algorithm, configuration snapshot representation, reconciliation timing strategy, and physical-item discovery mechanism remain downstream design choices.

Derived requirement action:

- revise `SWE1-MAIN-002-QLT-003` as deterministic bounded evolution evaluation;
- revise `SWE1-MAIN-002-CAP-010` as configuration-driven derived-state reconciliation;
- revise `SWE1-MAIN-002-CON-004` as no repair or revival from reconciliation;
- revise `SWE1-MAIN-002-CAP-011` as physical-durability preservation across reconciliation material change;
- revise `SWE1-MAIN-002-CAP-012` as progression-triggered full-durability recovery;
- add `SWE1-MAIN-002-QLT-007 — Internally consistent evolution configuration`.

## 4. CAN-MAIN-013 — Terminal durability and Broken-state continuity

Approved direction:

- Terminal durability does not destroy the logical Growth Tool or silently lose managed authority.
- Unless the same qualifying operation receives an applicable progression-triggered full-durability recovery, terminal durability transitions the logical lifecycle `ACTIVE → BROKEN` and produces the Broken representation.
- If the same qualifying operation also crosses an evolution threshold, the progression-triggered full recovery is applied before deciding whether terminal Broken conversion remains applicable; if recovery leaves the tool valid above terminal state, it remains `ACTIVE`.
- Broken conversion preserves logical identity, owner, tool type, cumulative progress, delivery state, branch, current physical issuance identity, and epoch.
- Broken conversion is not reissue, authority rotation, or a new delivery entitlement.
- The Broken representation must be distinguishable from an active Growth Pickaxe and retain sufficient supported managed identity to resolve the same current logical authority.
- `GRAY_DYE` is the V0.0.2 initial/supplied default Broken presentation, not authority and not a permanent physical identity contract.
- A `BROKEN` representation does not function as an active Growth Pickaxe and cannot satisfy an operation requiring `ACTIVE` lifecycle state.
- Detailed progress, repair, external-modification, and GUI rules remain governed by their owning requirements.
- An authorized Broken Tool remains eligible for the applicable management interface; the exact gesture is owned by CAN-MAIN-014.
- Once established, Broken state remains durably recoverable across supported lifecycle interruption/restart and is not implicitly reverted, reissued, rotated, or erased by restart.
- Event priority, temporary raw `ItemStack` retention, synchronous database writes, and a specific checkpoint implementation are not prescribed here.

Derived requirement action:

- revise `SWE1-MAIN-002-CAP-013` as terminal durability to Broken transition;
- revise `SWE1-MAIN-002-CAP-014` as Broken representation and authority continuity;
- revise `SWE1-MAIN-002-CON-005` as Broken lifecycle operation boundary;
- revise `SWE1-MAIN-002-QLT-004` as durable Broken-state continuity.

## 5. CAN-MAIN-014 — Owner management interface

Approved direction:

- The required player entry route is a main-hand air right-click, not targeting a block/entity, with the current authorized Growth Tool or Broken Tool.
- The corresponding off-hand interaction does not open the management GUI.
- This required route does not prohibit another later approved management entry route.
- Entry reuses current logical-owner/current-physical-authority validation; non-owner possession, stale issuance/epoch, malformed identity, or other failed authority does not grant owner management access.
- The GUI presents sufficient current state to understand lifecycle, Growth Pickaxe material tier, cumulative progress, conceptual evolution state/count, next threshold or defined no-next-threshold state, effective enchantments, active branch, applicable current physical durability or Broken state, repair availability/preview, and material conceptual/effective differences caused by caps or branch projection.
- For a Broken representation, presented material means the derived Growth Pickaxe material tier rather than the Broken presentation material.
- The GUI provides access to the applicable Repair operation and Help/Status information.
- Opening the GUI or viewing status/repair preview is not itself repair authorization, Waymark debit, or another protected state-changing effect.
- Exact wording, inventory size, slot assignment, decorative item choice, display name, lore, and equivalent presentation details are not fixed Product semantics; required information/actions remain identifiable and operable.
- Permission-node allocation remains for CAN-MAIN-018 and is not silently approved here.
- `SWE1-MAIN-003-QLT-001` remains active for protected financial-operation replay safety but CAN-MAIN-014 is removed from that requirement's source allocation; its controlling sources remain CAN-MAIN-015/CAN-MAIN-016/Common protected-operation semantics.

Derived requirement action:

- revise `SWE1-MAIN-003-CAP-001` as owner management GUI entry;
- revise `SWE1-MAIN-003-CAP-002` as management state presentation;
- revise `SWE1-MAIN-003-CAP-003` as management operation access;
- revise `SWE1-MAIN-003-QLT-004` as presentation-independent management usability;
- remove CAN-MAIN-014 from `SWE1-MAIN-003-QLT-001` source allocation without deleting that requirement.

## 6. CAN-MAIN-015 — Owner-paid full repair and protected transaction

Approved direction:

- Player repair remains full-repair only in V0.0.2; partial player repair is not offered.
- An `ACTIVE` current authorized Growth Pickaxe is player-repair eligible only below maximum durability.
- A maximum-durability active tool is not repairable, presents a 0 WM repair charge, and does not begin a debit operation.
- A current authorized `BROKEN` tool is eligible for Broken repair.
- Missing, stale, revoked, non-owner, unresolved, or otherwise non-current physical representations are not repaired through this operation.
- Repair pricing comes from current approved repair-pricing configuration and authoritative current state.
- Initial/supplied default pricing remains:
  - base full-repair cost `ceil(100 × (1 + evolution_count × 0.08))`;
  - active repair `ceil(base_full_repair_cost × max(0.25, missing_durability_ratio))`;
  - broken repair `base_full_repair_cost + 100 + evolution_count × 5`.
- These values are initial/default balance values and may be adjusted through approved configuration.
- ACTIVE missing-durability ratio comes from Minecraft-authoritative physical durability.
- Before any protected financial effect, the owner receives an explicit quote and explicitly confirms it.
- Current authority, lifecycle, physical state, applicable evolution state, pricing configuration, and quote validity are revalidated before debit.
- If a change alters eligibility or quoted amount, a stale confirmation does not silently charge a different amount; a new quote/confirmation is required.
- Repair uses the V0.0.2 Core-provided shared Waymark transaction contract and applicable Common protected-operation identity/replay/UNKNOWN/compensation semantics.
- Repair benefit proceeds only after debit success is proven.
- Successful ACTIVE repair preserves logical identity, owner, progress, delivery state, branch, issuance, and epoch while restoring current physical durability to maximum.
- Successful BROKEN repair transitions the same logical tool `BROKEN → ACTIVE`, preserves owner/progress/delivery/branch/issuance/epoch, reconstructs the current authorized active pickaxe representation from current approved evolution state, and restores maximum durability.
- Repair is not reissue, authority rotation, or a new delivery entitlement.
- Repair success is not reported until required logical and physical repaired state for the same current authority is established.
- Ambiguous/partial repair benefit remains `UNKNOWN` and is reconciled without blind retry or automatic compensation; compensation still requires the Common proven-debit/proven-no-benefit conditions.
- Transaction coordinator, lock mechanism, quote/session representation, database layout, and cross-authority atomicity mechanism remain downstream design choices.

Derived requirement action:

- revise `SWE1-MAIN-003-CAP-004` as configured full-repair pricing;
- revise `SWE1-MAIN-003-CON-001` as no paid repair at full durability;
- revise `SWE1-MAIN-003-CAP-005` as confirmed repair transaction admission/execution;
- add `SWE1-MAIN-003-CAP-010 — Successful full-repair result`;
- revise `SWE1-MAIN-003-QLT-005` as repair-benefit completion and ambiguous partial-state containment;
- mark `SWE1-MAIN-003-QLT-002 — Repair compensation on proven clear failure` as `SUPERSEDED_BY_DEDUPLICATION`, because its generic compensation rule is already controlled by Common protected-operation requirements and the remaining Repair-specific ambiguity rule;
- retain `SWE1-MAIN-003-QLT-001` for repair/reissue replay safety, with CAN-MAIN-014 removed from its source allocation.

## 7. Active requirement accounting

Before this review set, the live provisional active total after approved CAN-MAIN-010 was 176.

CAN-MAIN-011 adds one CAP; CAN-MAIN-012 adds one QLT; CAN-MAIN-013 and CAN-MAIN-014 are count-neutral; CAN-MAIN-015 adds one CAP and supersedes one duplicate QLT.

Integrated active total after CAN-MAIN-015:

```text
TOTAL: 178
CAP:   67
CON:   58
IFC:   14
QLT:   39
```

New active identifiers introduced by this checkpoint:

- `SWE1-MAIN-002-CAP-017`
- `SWE1-MAIN-002-QLT-007`
- `SWE1-MAIN-003-CAP-010`

New historical superseded identifier disposition introduced by this checkpoint:

- `SWE1-MAIN-003-QLT-002` — superseded by deduplication; identifier retained and never reused.

The complete post-review automated identifier/source/count audit remains required before G1.

## 8. Review and checkpoint state

`CAN-MAIN-011` through `CAN-MAIN-015` have completed their owning joint review. This decision does not approve `CAN-MAIN-016` or later clauses.

The next owning clause is:

`CAN-MAIN-016 — Player-paid reissue`

Repository-checkpoint cadence is reset to `0 / 5` after integration of this approved five-clause set.

No G1 approval, SWE.2/SWE.3 advancement, Product implementation, PR Ready transition, merge to main, tag, deployment, or release is authorized by this decision.
