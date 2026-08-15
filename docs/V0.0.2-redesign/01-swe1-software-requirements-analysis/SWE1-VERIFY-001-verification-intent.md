# SWE.1 Verification-intent Allocation

Document ID: `SWE1-VERIFY-001`  
Revision: F  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-15 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `VERIFY`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary requirement baseline: provisional SWE.1 target documents after integrated review through `CAN-MAIN-010`

## 1. Purpose

Control verification intent assigned to every provisional active SWE.1 Product requirement without duplicating the full 176-row requirement inventory. The normative allocation for each active requirement remains its own `Verification intent` field. This document defines allocation rules, checkpoint completeness accounting, superseded-item handling, open-issue restrictions, and downstream obligations.

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
15. Main progress-world verification uses exact configured identity membership. Similar-name/dimension/environment heuristics and implicit substitution shall be negative cases.
16. Qualifying progress verification treats one completed eligible player-mined physical block break as exactly one progress event. Natural, player-placed, generated/plugin-created, Silk-Touch-re-placed, and repeated eligible mining are representative allowed provenance cases.
17. Nonqualifying-removal verification may use explosion, piston, command, editor, and plugin-direct removal as representative cases without making those products Product dependencies.
18. Progress numeric verification is representation-independent: no test oracle shall require Java `long`, `1000` internal units, or `Long.MAX_VALUE`; it shall establish logical determinism, monotonic positive addition, no wrap/corruption, and operability at the selected supported maximum if bounded.
19. Uniform-progress verification shall confirm the same configured increment for representative stone, ore, rare block, generated, placed, and re-placed qualifying blocks; material/ore/rarity multipliers are not expected behavior.

## 4. Checkpoint completeness accounting

Every active provisional Product requirement in the integrated package contains a non-empty `Verification intent` field.

| Requirement document | Active requirements | Allocation status |
|---|---:|---|
| `SWE1-COMMON-001` | 30 | 30 allocated |
| `SWE1-CORE-001` | 13 | 13 allocated; historical `CON-004` superseded |
| `SWE1-MAIN-001` | 22 | 22 allocated; historical `CON-008`/`CON-009` superseded |
| `SWE1-MAIN-002` | 26 | 26 allocated; historical `CAP-006` superseded |
| `SWE1-MAIN-003` | 19 | 19 allocated |
| `SWE1-FRONTIER-001` | 14 | 14 allocated |
| `SWE1-WB-001` | 24 | 24 allocated |
| `SWE1-WB-002` | 28 | 28 allocated |
| **Total** | **176** | **176 allocated** |

```text
HISTORICAL SUPERSEDED IDENTIFIERS:
  SWE1-CORE-001-CON-004
  SWE1-MAIN-001-CON-008
  SWE1-MAIN-001-CON-009
  SWE1-MAIN-002-CAP-006

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
| `SWE1-ISSUE-001-ISSUE-007` | `SWE1-MAIN-003-CAP-006` | Reissue invocation context pending |
| `SWE1-ISSUE-001-ISSUE-008` | Main/Frontier/WB permission IFCs | Complete route-to-group allocation pending |
| `SWE1-ISSUE-001-ISSUE-009` | `SWE1-MAIN-002-CAP-015`, `QLT-006` | Crash-loss test oracle waits for measurable maximum window |

The V0.0.1 public-contract/Core migration inventories remain source prerequisites for final Core verification baselining rather than new issue records.

## 6. Reviewed propagation and later target work still pending

The reviewed Main 006–010 checkpoint resolves the prior CAN-MAIN-006 death-drop conflict. Later owning-clause work still includes:

- `CAN-MAIN-011` onward, including material/enchantment evolution and later durability/reconciliation semantics;
- `CAN-MAIN-016`: reissue shall not rely on global proof of current physical absence; authority rotation invalidates prior physical instances; paid reissue remains strictly more expensive than applicable repair rather than using the stale exact formula;
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

Verification intent is allocated for the provisional **176-active-item** package through `CAN-MAIN-010`, but it is not an approved verification baseline. Continued Owner review from `CAN-MAIN-011`, issue resolution, V0.0.1 inventory, Project consistency review, final automated audit/self-review, and explicit G1 approval remain prerequisites for downstream verification design/execution.
