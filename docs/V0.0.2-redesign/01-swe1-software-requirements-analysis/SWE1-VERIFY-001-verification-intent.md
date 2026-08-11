# SWE.1 Verification-intent Allocation

Document ID: `SWE1-VERIFY-001`  
Revision: C  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `VERIFY`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary requirement baseline: provisional SWE.1 target documents after integrated Common checkpoint

## 1. Purpose

Control the verification intent assigned to every provisional draft SWE.1 Product requirement without duplicating the full 176-row requirement inventory. The normative allocation for an individual requirement is its own `Verification intent` field. This document defines allocation rules, checkpoint completeness accounting, open-issue restrictions, and downstream obligations.

No SWE.4/SWE.5/SWE.6 execution is authorized by this document.

## 2. Verification levels

| Level | Intended use |
|---|---|
| `INSPECTION` | Static ownership, dependency, source, schema, packaging, migration, prohibition, and documentation claims |
| `ANALYSIS` | Mathematical properties, compatibility, architecture feasibility, and bounded-behavior arguments |
| `SWE.4` | Isolated policy, state machine, arithmetic, identity, serialization, concurrency, lifecycle, and error handling |
| `SWE.5` | Paper/platform execution contexts, persistence, adopted plugins, module/shared interfaces, lifecycle, transactions, delivery, and integration topology |
| `SWE.6` | Externally observable Main, Frontier, or Worlds Beyond behavior using representative client/runtime actions |
| `OWNER_REVIEW` | Usability or product-intent judgment not reducible to software pass/fail evidence alone |

A requirement may use more than one level when internal correctness and external behavior are independently material.

## 3. Allocation principles

1. Use the lowest level that can establish the obligation with sufficient confidence.
2. Do not repeat an internal property in runtime qualification when SWE.4/SWE.5 evidence is sufficient, unless the externally observable integration remains material.
3. Requirements involving platform event delivery/cancellation, inventory/item/block/world mutation, player motion, or GUI entry require at least SWE.5 when runtime integration is material; representative externally visible behavior may additionally require SWE.6.
4. Requirements involving MariaDB, Redis, MVI, Waymark providers, LeafGrapple, WorldGuard, WorldEdit/FAWE, external libraries, or module/runtime packaging require SWE.5 and/or inspection as applicable.
5. Arithmetic, state transition, saturation, operation/effect identity, replay, authorization, compensation, lifecycle-disposition, and race behavior require SWE.4 even when later integration evidence also applies.
6. Platform-execution-context and deferred-completion requirements verify against the authoritative contract of the adopted server platform rather than assuming one global main thread.
7. A requirement linked to an open issue retains provisional intent; executable cases cannot be baselined until the issue supplies the missing contract.
8. A target requirement that explicitly carries a known Common/target conflict may retain only provisional verification intent until its canonical target clause is jointly reviewed.
9. Later verification documents use process-appropriate domains, Software Unit/Runtime Target or Integration Topology metadata, and full upstream trace links. Matching domain names do not prove coverage.
10. `SWE1-COMMON-001-CON-010` capability ownership non-duplication is verified first through SWE.2/SWE.3 allocation/dependency assessment under `GOV-ENG-001`; integration verification is then assigned to the selected external or Project-owned boundary as applicable.

## 4. Checkpoint completeness accounting

Every provisional Product requirement in the integrated Common checkpoint package contains a non-empty `Verification intent` field.

| Requirement document | Requirements | Allocation status |
|---|---:|---|
| `SWE1-COMMON-001` | 30 | 30 allocated |
| `SWE1-CORE-001` | 14 | 14 allocated |
| `SWE1-MAIN-001` | 20 | 20 allocated |
| `SWE1-MAIN-002` | 27 | 27 allocated |
| `SWE1-MAIN-003` | 19 | 19 allocated |
| `SWE1-FRONTIER-001` | 14 | 14 allocated |
| `SWE1-WB-001` | 24 | 24 allocated |
| `SWE1-WB-002` | 28 | 28 allocated |
| **Total** | **176** | **176 allocated** |

```text
CHECKPOINT DOCUMENT INSPECTION:
  verification-intent fields retained/added for the integrated 176-item package

FULL POST-REVIEW AUTOMATED VERIFICATION-INTENT/IDENTIFIER AUDIT:
  PENDING BEFORE G1
```

The old 164-item automated self-review result is historical evidence and is not reused as proof for the 176-item package.

## 5. Provisional allocations affected by open issues

| Open issue | Affected requirements | Allocation consequence |
|---|---|---|
| `SWE1-ISSUE-001-ISSUE-001` | `SWE1-FRONTIER-001-CON-002` | Missing configured-world fail-closed direction is partly resolved; lifecycle/health and recovery/re-enable executable cases wait for the remaining contract |
| `SWE1-ISSUE-001-ISSUE-002` | `SWE1-MAIN-001-CON-007` | External-repair integration cases wait for the supported public/cancellable boundary |
| `SWE1-ISSUE-001-ISSUE-003` | `SWE1-COMMON-001-CON-005`, `SWE1-WB-001-IFC-001`, `SWE1-WB-001-CON-006` | Dependency/configuration cases wait for LeafGrapple API and safe-tier evidence |
| `SWE1-ISSUE-001-ISSUE-004` | `SWE1-WB-002-CAP-011` | WorldEdit/FAWE integration cases wait for the supported-hook boundary |
| `SWE1-ISSUE-001-ISSUE-005` | `SWE1-WB-002-CAP-005`, `SWE1-WB-002-CAP-006` | Persistence/reconciliation cases wait for material identity authority |
| `SWE1-ISSUE-001-ISSUE-006` | `SWE1-WB-002-CAP-015`, `SWE1-WB-002-CON-007` | Portal-denial qualification waits for the authoritative return path |
| `SWE1-ISSUE-001-ISSUE-007` | `SWE1-MAIN-003-CAP-006` | Reissue entry-route cases wait for invocation-context decision |
| `SWE1-ISSUE-001-ISSUE-008` | `SWE1-MAIN-003-IFC-001`, `SWE1-FRONTIER-001-IFC-002`, `SWE1-WB-002-IFC-001` | Permission cases wait for complete route-to-group allocation |
| `SWE1-ISSUE-001-ISSUE-009` | `SWE1-MAIN-002-CAP-015`, `SWE1-MAIN-002-QLT-006` | Crash-loss test oracle waits for a measurable maximum window |

## 6. Common-review propagation still awaiting target-clause review

The Common checkpoint intentionally records but does not silently settle later target-specific conflicts. Executable verification baselines for these items wait for their owning canonical target review:

- `SWE1-MAIN-001-CON-001` — old Main-backend/Core fixed wording versus approved topology/shared-ownership rules;
- `SWE1-FRONTIER-001-CON-001` — old Frontier-backend/Core fixed wording versus approved topology/shared-ownership rules;
- `SWE1-FRONTIER-001-CAP-001`, `SWE1-FRONTIER-001-CON-003` — configured Worlds Beyond world direction versus current target literal;
- literal-world references retained and marked in `SWE1-WB-002` pending their later `CAN-WB-*` reviews;
- concrete shared Waymark transaction ownership in Core/Main/WB requirements pending `CAN-CORE-003` and relevant target reviews;
- exact pending-delivery versus compensation priority in `CAN-WB-014` after a proven clear delivery failure.

## 7. Required downstream chain

Each applicable verification case shall trace through the approved design chain after the relevant gates are authorized:

```text
SWE.1 requirement
  -> SWE.2 architecture allocation
  -> SWE.3 detailed-design item
  -> SWE.3 implementation unit
  -> SWE.4 and/or SWE.5 case
  -> SWE.6 case where externally observable qualification is required
  -> execution evidence and review verdict
```

A SWE.4/SWE.5/SWE.6 case shall identify:

- full requirement and design IDs;
- verification target Software Unit or Integration Topology;
- runtime target and participating units;
- controlled environment/data/oracle;
- Product commit and artifact identity;
- result and retained evidence.

## 8. Current disposition

Verification intent is allocated for the provisional 176-item Common-checkpoint package but is not an approved verification baseline. Continued Owner clause review, target-conflict resolution, open issues, V0.0.1 inventory, Project consistency review, complete automated self-review, and G1 approval remain prerequisites for downstream executable verification design/execution.
