# SWE.1 Verification-intent Allocation

Document ID: `SWE1-VERIFY-001`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `VERIFY`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary requirement baseline: SWE.1 target documents Revision A

## 1. Purpose

Control the verification intent assigned to every draft SWE.1 requirement without duplicating the
full 164-row requirement inventory. The normative allocation for an individual requirement is its
own `Verification intent` field. This document defines allocation rules, completeness accounting,
open-issue restrictions, and downstream obligations.

## 2. Verification levels

| Level | Intended use |
|---|---|
| `INSPECTION` | Static ownership, dependency, source, schema, packaging, migration, prohibition, and documentation claims |
| `ANALYSIS` | Mathematical properties, compatibility, architecture feasibility, and bounded-behavior arguments |
| `SWE.4` | Isolated policy, state machine, arithmetic, identity, serialization, concurrency, and error handling |
| `SWE.5` | Paper events, persistence, adopted plugins, module interfaces, lifecycle, transactions, delivery, and integration topology |
| `SWE.6` | Externally observable Main, Frontier, or Worlds Beyond behavior using representative client/runtime actions |
| `OWNER_REVIEW` | Usability or product-intent judgment not reducible to software pass/fail evidence alone |

A requirement may use more than one level when internal correctness and external behavior are
independently material.

## 3. Allocation principles

1. Use the lowest level that can establish the obligation with sufficient confidence.
2. Do not repeat an internal property in runtime qualification when SWE.4/SWE.5 evidence is
   sufficient, unless the externally observable integration remains material.
3. Requirements involving Paper event delivery, cancellation, inventory mutation, player motion, or
   GUI entry require at least SWE.5; representative externally visible behavior may additionally
   require SWE.6.
4. Requirements involving MariaDB, Redis, MVI, Waymark providers, LeafGrapple, WorldGuard,
   WorldEdit/FAWE, or module packaging require SWE.5 or inspection as applicable.
5. Arithmetic, state transition, saturation, idempotency, authorization, and race behavior require
   SWE.4 even when later integration evidence also applies.
6. A requirement linked to an open issue retains provisional intent; executable cases cannot be
   baselined until the issue supplies the missing contract.
7. Later verification documents use process-appropriate domains, Software Unit/Runtime Target or
   Integration Topology metadata, and full upstream trace links. Matching domain names do not prove
   coverage.

## 4. Completeness accounting

All 164 draft target requirements contain a non-empty `Verification intent` field.

| Requirement document | Requirements | Allocation status |
|---|---:|---|
| `SWE1-COMMON-001` | 18 | 18 allocated |
| `SWE1-CORE-001` | 14 | 14 allocated |
| `SWE1-MAIN-001` | 20 | 20 allocated |
| `SWE1-MAIN-002` | 27 | 27 allocated |
| `SWE1-MAIN-003` | 19 | 19 allocated |
| `SWE1-FRONTIER-001` | 14 | 14 allocated |
| `SWE1-WB-001` | 24 | 24 allocated |
| `SWE1-WB-002` | 28 | 28 allocated |
| **Total** | **164** | **164 allocated** |

Automated self-review found:

```text
MISSING VERIFICATION INTENT:
  0

REQUIREMENTS WITHOUT TARGET DOCUMENT OWNERSHIP:
  0

OPEN-ISSUE REQUIREMENTS WITHOUT ISSUE LINKS:
  0
```

## 5. Provisional allocations affected by open issues

| Open issue | Affected requirements | Allocation consequence |
|---|---|---|
| `SWE1-ISSUE-001-ISSUE-001` | `SWE1-COMMON-001-QLT-003`, `SWE1-FRONTIER-001-CON-002` | Lifecycle/health integration and qualification cases wait for the missing-world contract |
| `SWE1-ISSUE-001-ISSUE-002` | `SWE1-MAIN-001-CON-007` | External-repair integration cases wait for the supported public/cancellable boundary |
| `SWE1-ISSUE-001-ISSUE-003` | `SWE1-COMMON-001-CON-005`, `SWE1-WB-001-IFC-001`, `SWE1-WB-001-CON-006` | Dependency/configuration cases wait for LeafGrapple API and safe-tier evidence |
| `SWE1-ISSUE-001-ISSUE-004` | `SWE1-WB-002-CAP-011` | WorldEdit/FAWE integration cases wait for the supported-hook boundary |
| `SWE1-ISSUE-001-ISSUE-005` | `SWE1-WB-002-CAP-005`, `SWE1-WB-002-CAP-006` | Persistence/reconciliation cases wait for material identity authority |
| `SWE1-ISSUE-001-ISSUE-006` | `SWE1-WB-002-CAP-015`, `SWE1-WB-002-CON-007` | Portal-denial qualification waits for the authoritative return path |
| `SWE1-ISSUE-001-ISSUE-007` | `SWE1-MAIN-003-CAP-006` | Reissue entry-route cases wait for invocation-context decision |
| `SWE1-ISSUE-001-ISSUE-008` | `SWE1-MAIN-003-IFC-001`, `SWE1-FRONTIER-001-IFC-002`, `SWE1-WB-002-IFC-001` | Permission cases wait for complete route-to-group allocation |
| `SWE1-ISSUE-001-ISSUE-009` | `SWE1-MAIN-002-CAP-015`, `SWE1-MAIN-002-QLT-006` | Crash-loss test oracle waits for a measurable maximum window |

## 6. Required downstream chain

Each applicable verification case shall trace through the approved design chain:

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

## 7. Current disposition

Verification intent is complete for Owner review but is not an approved verification baseline.
Open issues, V0.0.1 inventory, Project consistency review, and G1 approval remain prerequisites for
downstream executable verification design.
