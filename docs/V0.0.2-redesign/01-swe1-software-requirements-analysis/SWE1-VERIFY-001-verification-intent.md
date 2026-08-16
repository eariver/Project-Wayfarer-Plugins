# SWE.1 Verification-intent Allocation

Document ID: `SWE1-VERIFY-001`  
Revision: G  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `VERIFY`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary requirement baseline: provisional SWE.1 target documents after integrated review through `CAN-MAIN-015`

## 1. Purpose

Control verification intent assigned to every provisional active SWE.1 Product requirement without duplicating the full 178-row requirement inventory. The normative allocation for each active requirement remains its own `Verification intent` field. This document defines allocation rules, checkpoint completeness accounting, superseded-item handling, open-issue restrictions, and downstream obligations.

No SWE.4/SWE.5/SWE.6 execution is authorized by this document.

## 2. Verification levels

| Level | Intended use |
|---|---|
| `INSPECTION` | Static ownership, dependency, source, schema, packaging, migration, prohibition, and documentation claims |
| `ANALYSIS` | Mathematical properties, compatibility, architecture feasibility, and bounded-behavior arguments |
| `SWE.4` | Isolated policy, state machine, arithmetic, identity, concurrency, lifecycle, and error handling |
| `SWE.5` | Platform execution, persistence, adopted plugins, shared interfaces, lifecycle, transactions, inventory/item/world behavior, and integration topology |
| `SWE.6` | Externally observable Main, Frontier, or Worlds Beyond behavior using representative client/runtime actions |
| `OWNER_REVIEW` | Product-intent/usability judgment not reducible to software pass/fail evidence alone |

## 3. Allocation principles

1. Use the lowest verification level sufficient to establish the obligation with confidence.
2. Do not duplicate internal evidence at higher levels unless externally observable integration remains material.
3. Platform event/cancellation, inventory/item/block/world mutation, player motion, or GUI entry require at least SWE.5 where runtime integration is material; representative Product behavior may additionally require SWE.6.
4. MariaDB, Redis, MVI, Waymark, LeafGrapple, WorldGuard, WorldEdit/FAWE, external libraries, or packaging boundaries require SWE.5 and/or inspection as applicable.
5. Arithmetic, state transition, protected-operation identity/replay, authorization, compensation, lifecycle disposition, race behavior, and numeric-boundary safety require SWE.4 even when integration evidence also applies.
6. Execution-context requirements verify against the authoritative adopted-platform contract, not a presumed global main thread.
7. Requirements linked to unresolved issues retain provisional intent until the missing contract is supplied or explicitly accepted.
8. A superseded identifier has no independent executable verification allocation; its replacement/disposition is verified by inspection and final identifier/count audit.
9. `SWE1-COMMON-001-CON-010` reuse/ownership non-duplication is verified through SWE.2/SWE.3 allocation/dependency assessment under `GOV-ENG-001` before selected-boundary integration evidence.
10. Accepted V0.0.1 Core compatibility/migration claims verify against the controlled accepted public-contract and migration inventories rather than current implementation presence.
11. Waymark provider verification distinguishes provider-authoritative evidence, Wayfarer logical operation/effect disposition, and feature-owned domain effects.
12. Main Growth Tool verification distinguishes Main-owned logical state from Minecraft-owned physical item state; current durability/damage is not an independently authoritative Main database value.
13. Ordinary Growth Tool possession/storage/death/item-lifecycle behavior may succeed for owner or non-owner while owner-only Growth Tool use/progress remains denied to non-owners.
14. V0.0.2 modification-control verification tests prohibited effects/routes, including anvil/grindstone processing and rename, rather than imposing blanket possession restrictions.
15. Main progress-world verification uses exact configured identity membership. Similar-name/dimension/environment heuristics and implicit substitution are negative cases.
16. Qualifying progress verification treats one completed eligible player-mined physical block break as exactly one progress event. Natural, player-placed, generated/plugin-created, Silk-Touch-re-placed, and repeated eligible mining are representative allowed provenance cases.
17. Nonqualifying-removal verification may use explosion, piston, command, editor, and plugin-direct removal as representative cases without making those products Product dependencies.
18. Progress numeric verification is representation-independent: no test oracle requires Java `long`, `1000` internal units, or `Long.MAX_VALUE`; it establishes logical determinism, monotonic positive addition, no wrap/corruption, and operability at the selected supported maximum if bounded.
19. Uniform-progress verification confirms the same configured increment for representative stone, ore, rare block, generated, placed, and re-placed qualifying blocks; material/ore/rarity multipliers are not expected behavior.
20. Material/enchantment evolution verification separates fixed V0.0.2 Product sequence/family semantics from approved configurable defaults. Thresholds, post-Diamond increment parameters/mapping, and effective caps verify both initial defaults and accepted configuration changes.
21. Post-Diamond threshold verification treats `800 + 200n + 40n²` as the initial/default increment from the preceding evolution threshold, not an absolute threshold.
22. Branch verification distinguishes conceptual Fortune progression from effective physical enchantments: Silk Touch suppresses effective Fortune without erasing conceptual Fortune progression; returning to Fortune reapplies current conceptual Fortune subject to cap.
23. Configuration-reconciliation verification uses one internally consistent approved evolution snapshot and confirms cumulative progress, logical identity, owner, lifecycle, delivery, branch, issuance, and epoch are not changed solely by reconciliation. Promotion/demotion of derived evolution state is allowed.
24. Reconciliation material-change verification obtains current durability from Minecraft physical authority and preserves remaining-durability fraction to representable precision; no separate authoritative damage value is manufactured when the physical item is unavailable.
25. Progression-triggered full recovery verifies threshold crossing, not merely any progress addition. A same-operation evolution recovery is resolved before terminal Broken conversion.
26. Broken-state verification treats `BROKEN` as a same-logical-authority/same-issuance/same-epoch lifecycle transition. `GRAY_DYE` is only the initial/default presentation and is not an authority oracle.
27. Management-interface verification distinguishes the derived Growth Pickaxe material from Broken presentation material, conceptual from effective enchantments, and applicable current physical durability from Broken lifecycle state. Exact slot/layout/text/lore is not a functional oracle.
28. Management GUI entry requires current owner/current authority; non-owner physical possession or stale identity does not grant owner management access. CAN-MAIN-018 permission-node allocation is not assumed until reviewed.
29. Repair verification treats preview/status as non-committing. A protected repair requires an explicit current quote and explicit confirmation; a state/config change that alters eligibility/price makes the old confirmation stale and must not silently charge a different amount.
30. Full-repair pricing verifies the current approved pricing configuration and the initial/supplied default formulas; the formulas are not permanent untunable Product constants.
31. Repair sequencing verifies no repair benefit proceeds before proven debit success. A maximum-durability ACTIVE tool displays 0 WM and begins no debit operation.
32. Successful ACTIVE and BROKEN repair verify preservation of logical identity/owner/progress/delivery/branch/current issuance/epoch; BROKEN repair returns the same authority to ACTIVE at maximum durability and is not reissue/authority rotation/new delivery.
33. Repair ambiguity verification does not report success until required logical and physical repaired state for the same authority is established. Partial/ambiguous benefit remains `UNKNOWN`; compensation relies on the Common proven-debit/proven-no-benefit rule rather than duplicated Main compensation semantics.

## 4. Checkpoint completeness accounting

Every active provisional Product requirement in the integrated package contains a non-empty `Verification intent` field.

| Requirement document | Active requirements | Allocation status |
|---|---:|---|
| `SWE1-COMMON-001` | 30 | 30 allocated |
| `SWE1-CORE-001` | 13 | 13 allocated; historical `CON-004` superseded |
| `SWE1-MAIN-001` | 22 | 22 allocated; historical `CON-008`/`CON-009` superseded |
| `SWE1-MAIN-002` | 28 | 28 allocated; historical `CAP-006` superseded |
| `SWE1-MAIN-003` | 19 | 19 allocated; historical `QLT-002` superseded |
| `SWE1-FRONTIER-001` | 14 | 14 allocated |
| `SWE1-WB-001` | 24 | 24 allocated |
| `SWE1-WB-002` | 28 | 28 allocated |
| **Total** | **178** | **178 allocated** |

```text
HISTORICAL SUPERSEDED IDENTIFIERS:
  SWE1-CORE-001-CON-004
  SWE1-MAIN-001-CON-008
  SWE1-MAIN-001-CON-009
  SWE1-MAIN-002-CAP-006
  SWE1-MAIN-003-QLT-002

AMD-009 CONCRETE LONG.MAX_VALUE RULE:
  SUPERSEDED BY DEC-REQ-007 / REPRESENTATION-INDEPENDENT NUMERIC SAFETY

FULL POST-REVIEW AUTOMATED VERIFICATION-INTENT/IDENTIFIER AUDIT:
  PENDING BEFORE G1
```

## 5. Provisional allocations affected by open issues

| Open issue | Affected requirements | Allocation consequence |
|---|---|---|
| `SWE1-ISSUE-001-ISSUE-001` | `SWE1-FRONTIER-001-CON-002` | Configured-world lifecycle/health and recovery/re-enable executable cases wait for remaining contract |
| `SWE1-ISSUE-001-ISSUE-002` | `SWE1-MAIN-001-CON-010` | Product modification policy is fixed; exact supported external public/cancellable integration boundary remains open |
| `SWE1-ISSUE-001-ISSUE-003` | Common external-boundary / WB hook requirements | LeafGrapple API and safe-tier evidence pending |
| `SWE1-ISSUE-001-ISSUE-004` | `SWE1-WB-002-CAP-011` | Supported WorldEdit/FAWE protection boundary pending |
| `SWE1-ISSUE-001-ISSUE-005` | `SWE1-WB-002-CAP-005`, `CAP-006` | Launchpad physical-material identity/reconciliation pending |
| `SWE1-ISSUE-001-ISSUE-006` | `SWE1-WB-002-CAP-015`, `CON-007` | Portal-denial qualification waits for authoritative return path |
| `SWE1-ISSUE-001-ISSUE-007` | `SWE1-MAIN-003-CAP-006` | Reissue invocation context pending; CAN-MAIN-016 owning review still pending |
| `SWE1-ISSUE-001-ISSUE-008` | Main/Frontier/WB permission IFCs | Complete route-to-group allocation pending |
| `SWE1-ISSUE-001-ISSUE-009` | `SWE1-MAIN-002-CAP-015`, `QLT-006` | Crash-loss test oracle waits for measurable maximum window |

The V0.0.1 public-contract/Core migration inventories remain source prerequisites for final Core verification baselining rather than new issue records.

## 6. Reviewed propagation and later target work still pending

The reviewed Main 011–015 checkpoint resolves the prior evolution/reconciliation/Broken/management/repair draft ambiguities. Later owning-clause work still includes:

- `CAN-MAIN-016`: reissue shall not rely on global proof of current physical absence; authority rotation invalidates prior physical instances; paid reissue remains strictly more expensive than applicable repair rather than using the stale exact formula; pending-delivery free retry remains distinct;
- `CAN-MAIN-017`: persistence/checkpoint/session timing and crash-loss wording must be reviewed against already approved Broken-state durability and Common lifecycle/threading rules;
- `CAN-MAIN-018`: administrative capability and permission allocation, including the management-use permission boundary;
- `CAN-MAIN-019`: normal durability behavior under Minecraft physical authority and the already reviewed evolution/repair/Broken exceptions;
- Frontier fixed-backend/specific-Core wording versus Common topology/shared-owner rules;
- configured Worlds Beyond gameplay-world direction versus current literal target wording;
- later Main/WB feature-specific transaction, entitlement, delivery, and compensation semantics;
- `CAN-WB-014` pending-delivery versus compensation priority after proven clear delivery failure.

## 7. Required downstream chain

After applicable gates are authorized, each verification case traces:

```text
SWE.1 requirement
  -> SWE.2 architecture allocation
  -> SWE.3 detailed-design item / implementation unit
  -> SWE.4 and/or SWE.5 case
  -> SWE.6 case where externally observable qualification is required
  -> execution evidence and review verdict
```

Cases shall identify full upstream IDs, target software unit/integration topology, controlled environment/data/oracle, Product commit/artifact identity, result, and retained evidence.

## 8. Current disposition

Verification intent is allocated for the provisional **178-active-item** package through `CAN-MAIN-015`, but it is not an approved verification baseline. Continued Owner review from `CAN-MAIN-016`, issue resolution, V0.0.1 inventory, Project consistency review, final automated audit/self-review, and explicit G1 approval remain prerequisites for downstream verification design/execution.
