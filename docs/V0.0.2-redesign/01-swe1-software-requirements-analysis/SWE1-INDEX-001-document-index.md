# SWE.1 Target-domain Document Index

Document ID: `SWE1-INDEX-001`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 and later while items remain active  
Predecessor: Revision A

## 1. Purpose

Identify every controlled SWE.1 work product, its target or support domain, path, state, and contained
item range. This index is navigational and does not duplicate normative content.

## 2. Domain rule

Normative SWE.1 Product requirements use target-oriented domains:

```text
COMMON
CORE
MAIN
FRONTIER
WB
```

`RF` and `ADAPTER` are recognized possible target domains but have no normative document in the
current scope.

Support documents use `SRC`, `PLAN`, `INDEX`, `SCOPE`, `GLOSSARY`, `ISSUE`, and `VERIFY`. Downstream
SWE.2–SWE.5 domains will be concern-oriented and need not match these targets.

## 3. Controlled SWE.1 work products

| Document ID | Domain role | Title / purpose | Path | State | Item range / count |
|---|---|---|---|---|---|
| `SWE1-SRC-001` | Support / source | Requirement Source Register | `REQUIREMENT_SOURCES.md` | DRAFT_FOR_OWNER_REVIEW | Source records |
| `SWE1-SRC-002` | Support / source | Canonical positive-requirement source | `SWE1-SRC-002-canonical-mainline-requirements.md` | DRAFT_FOR_OWNER_REVIEW | 59 `CAN-*` source clauses |
| `SWE1-PLAN-001` | Support / plan | Analysis Plan and Execution Record | `SWE1_ANALYSIS_PLAN.md` | EXECUTED_AWAITING_OWNER_REVIEW | Process record |
| `SWE1-INDEX-001` | Support / index | This target-domain index | this file | DRAFT_FOR_OWNER_REVIEW | None |
| `SWE1-SCOPE-001` | Support / scope | Scope and Non-scope | `SWE1-SCOPE-001-scope-and-non-scope.md` | DRAFT_FOR_OWNER_REVIEW | Scope dispositions |
| `SWE1-GLOSSARY-001` | Support / glossary | Glossary and controlled terms | `SWE1-GLOSSARY-001-glossary.md` | DRAFT_FOR_OWNER_REVIEW | Definitions |
| `SWE1-COMMON-001` | Product / COMMON | Cross-target common requirements | `SWE1-COMMON-001-common-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CON-001`–`CON-008` (8), `IFC-001`–`IFC-002` (2), `QLT-001`–`QLT-008` (8) |
| `SWE1-CORE-001` | Product / CORE | Wayfarer_Core requirements | `SWE1-CORE-001-core-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-002` (2), `CON-001`–`CON-008` (8), `IFC-001`–`IFC-003` (3), `QLT-001`–`QLT-001` (1) |
| `SWE1-MAIN-001` | Product / MAIN | Lifecycle, authority, delivery | `SWE1-MAIN-001-authority-delivery-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-009` (9), `CON-001`–`CON-009` (9), `QLT-001`–`QLT-002` (2) |
| `SWE1-MAIN-002` | Product / MAIN | Progress, evolution, durability, checkpoint | `SWE1-MAIN-002-progress-durability-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-016` (16), `CON-001`–`CON-005` (5), `QLT-001`–`QLT-006` (6) |
| `SWE1-MAIN-003` | Product / MAIN | GUI, repair, reissue, admin, permission | `SWE1-MAIN-003-repair-reissue-admin-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-009` (9), `CON-001`–`CON-004` (4), `IFC-001`–`IFC-001` (1), `QLT-001`–`QLT-005` (5) |
| `SWE1-FRONTIER-001` | Product / FRONTIER | Runtime boundary, MVI, persistence, permission | `SWE1-FRONTIER-001-boundary-persistence-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-002` (2), `CON-001`–`CON-008` (8), `IFC-001`–`IFC-002` (2), `QLT-001`–`QLT-002` (2) |
| `SWE1-WB-001` | Product / WB | Loadout, permanent items, hook, navigation | `SWE1-WB-001-loadout-navigation-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-010` (10), `CON-001`–`CON-008` (8), `IFC-001`–`IFC-001` (1), `QLT-001`–`QLT-005` (5) |
| `SWE1-WB-002` | Product / WB | Launchpad, shop, portal, administration | `SWE1-WB-002-launchpad-shop-portal-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-016` (16), `CON-001`–`CON-007` (7), `IFC-001`–`IFC-001` (1), `QLT-001`–`QLT-004` (4) |
| `SWE1-ISSUE-001` | Support / issue | Open questions and conflicts | `SWE1-ISSUE-001-open-questions.md` | DRAFT_FOR_OWNER_REVIEW | `ISSUE-001`–`ISSUE-009` |
| `SWE1-VERIFY-001` | Support / verification | Verification-intent allocation | `SWE1-VERIFY-001-verification-intent.md` | DRAFT_FOR_OWNER_REVIEW | 164 allocations |

Cross-directory controlled work products:

| Document ID | Purpose | Path | State |
|---|---|---|---|
| `TRC-SWE1-001` | Source-to-requirement bidirectional traceability | `../07-traceability/TRC-SWE1-001-source-requirement-traceability.md` | DRAFT_FOR_OWNER_REVIEW |
| `REV-SWE1-001` | Complete SWE.1 package self-review | `../10-reviews-and-evidence/REV-SWE1-001-self-review.md` | COMPLETE_AWAITING_OWNER_REVIEW |
| `DEC-REQ-001` | Canonical source merge and analysis method | `../08-decisions/DEC-REQ-001-canonical-source-merge-and-swe1-analysis.md` | APPROVED_OWNER_DECISION |

## 4. Requirement ownership

- Common obligations applying identically across targets are owned by `SWE1-COMMON-001`.
- Core shared-service behavior is owned by `SWE1-CORE-001`.
- Main behavior is split across three `MAIN` documents to remain reviewable; it is not moved to
  architecture concern domains during SWE.1.
- Frontier plugin/runtime/MVI/persistence behavior is owned by `SWE1-FRONTIER-001`.
- Externally observable Worlds Beyond theme behavior is owned by `SWE1-WB-001` or `SWE1-WB-002`.
- Other documents reference full requirement IDs rather than copying normative statements.

## 5. Item summary

```text
Total draft Product requirements: 164

CAP: 64
CON: 57
IFC: 10
QLT: 33
```

## 6. Gate state

The document set has been decomposed and self-reviewed. It is not an approved baseline. G1 remains
`NOT_READY` until Owner review, issue resolution/acceptance, V0.0.1 baseline inventory, and Project
consistency review are complete.
