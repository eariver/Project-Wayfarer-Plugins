# SWE.1 Target-domain Document Index

Document ID: `SWE1-INDEX-001`  
Revision: C  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 and later while items remain active  
Predecessor: Revision B

## 1. Purpose

Identify every controlled SWE.1 work product, its target or support domain, path, state, and contained item range. This index is navigational and does not duplicate normative content.

## 2. Domain rule

Normative SWE.1 Product requirements use target-oriented domains:

```text
COMMON
CORE
MAIN
FRONTIER
WB
```

`RF` and `ADAPTER` are recognized possible target domains but have no normative document in the current scope.

Support documents use `SRC`, `PLAN`, `INDEX`, `SCOPE`, `GLOSSARY`, `ISSUE`, and `VERIFY`. Downstream SWE.2–SWE.5 domains will be concern-oriented and need not match these targets.

## 3. Controlled SWE.1 work products

| Document ID | Domain role | Title / purpose | Path | State | Item range / count |
|---|---|---|---|---|---|
| `SWE1-SRC-001` | Support / source | Requirement Source Register | `REQUIREMENT_SOURCES.md` | DRAFT_FOR_OWNER_REVIEW | Source records |
| `SWE1-SRC-002` | Support / source | Canonical positive-requirement source | `SWE1-SRC-002-canonical-mainline-requirements.md` | DRAFT_FOR_OWNER_REVIEW | 59 `CAN-*` source clauses; Common section integrated as Revision B |
| `SWE1-PLAN-001` | Support / plan | Analysis Plan and Execution Record | `SWE1_ANALYSIS_PLAN.md` | EXECUTED_AWAITING_OWNER_REVIEW | Process record |
| `SWE1-INDEX-001` | Support / index | This target-domain index | this file | DRAFT_FOR_OWNER_REVIEW | None |
| `SWE1-SCOPE-001` | Support / scope | Scope and Non-scope | `SWE1-SCOPE-001-scope-and-non-scope.md` | DRAFT_FOR_OWNER_REVIEW | Scope dispositions |
| `SWE1-GLOSSARY-001` | Support / glossary | Glossary and controlled terms | `SWE1-GLOSSARY-001-glossary.md` | DRAFT_FOR_OWNER_REVIEW | Definitions |
| `SWE1-COMMON-001` | Product / COMMON | Cross-target common requirements | `SWE1-COMMON-001-common-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CON-001`–`CON-010` (10), `IFC-001`–`IFC-006` (6), `QLT-001`–`QLT-014` (14); 30 total |
| `SWE1-CORE-001` | Product / CORE | Wayfarer_Core requirements | `SWE1-CORE-001-core-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-002` (2), `CON-001`–`CON-008` (8), `IFC-001`–`IFC-003` (3), `QLT-001` (1); 14 total |
| `SWE1-MAIN-001` | Product / MAIN | Lifecycle, authority, delivery | `SWE1-MAIN-001-authority-delivery-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-009` (9), `CON-001`–`CON-009` (9), `QLT-001`–`QLT-002` (2); 20 total |
| `SWE1-MAIN-002` | Product / MAIN | Progress, evolution, durability, checkpoint | `SWE1-MAIN-002-progress-durability-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-016` (16), `CON-001`–`CON-005` (5), `QLT-001`–`QLT-006` (6); 27 total |
| `SWE1-MAIN-003` | Product / MAIN | GUI, repair, reissue, admin, permission | `SWE1-MAIN-003-repair-reissue-admin-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-009` (9), `CON-001`–`CON-004` (4), `IFC-001` (1), `QLT-001`–`QLT-005` (5); 19 total |
| `SWE1-FRONTIER-001` | Product / FRONTIER | Runtime boundary, MVI, persistence, permission | `SWE1-FRONTIER-001-boundary-persistence-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-002` (2), `CON-001`–`CON-008` (8), `IFC-001`–`IFC-002` (2), `QLT-001`–`QLT-002` (2); 14 total |
| `SWE1-WB-001` | Product / WB | Loadout, permanent items, hook, navigation | `SWE1-WB-001-loadout-navigation-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-010` (10), `CON-001`–`CON-008` (8), `IFC-001` (1), `QLT-001`–`QLT-005` (5); 24 total |
| `SWE1-WB-002` | Product / WB | Launchpad, shop, portal, administration | `SWE1-WB-002-launchpad-shop-portal-requirements.md` | DRAFT_FOR_OWNER_REVIEW | `CAP-001`–`CAP-016` (16), `CON-001`–`CON-007` (7), `IFC-001` (1), `QLT-001`–`QLT-004` (4); 28 total |
| `SWE1-ISSUE-001` | Support / issue | Open questions and conflicts | `SWE1-ISSUE-001-open-questions.md` | DRAFT_FOR_OWNER_REVIEW | `ISSUE-001`–`ISSUE-009`; ISSUE-001 partially resolved |
| `SWE1-VERIFY-001` | Support / verification | Verification-intent allocation | `SWE1-VERIFY-001-verification-intent.md` | DRAFT_FOR_OWNER_REVIEW | 176 allocations after Common checkpoint |

Cross-directory controlled work products:

| Document ID | Purpose | Path | State |
|---|---|---|---|
| `TRC-SWE1-001` | Source-to-requirement bidirectional traceability | `../07-traceability/TRC-SWE1-001-source-requirement-traceability.md` | DRAFT_FOR_OWNER_REVIEW |
| `REV-SWE1-001` | Complete SWE.1 package self-review snapshot | `../10-reviews-and-evidence/REV-SWE1-001-self-review.md` | SUPERSEDED_AS_CURRENT_EVIDENCE_BY_OWNER_REVIEW |
| `REV-SWE1-002` | Joint Owner clause review log | `../10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md` | IN_REVIEW |
| `DEC-REQ-001` | Canonical source merge and analysis method | `../08-decisions/DEC-REQ-001-canonical-source-merge-and-swe1-analysis.md` | APPROVED |
| `DEC-REQ-002` | Common review corrections CAN-COM-001–005 | `../08-decisions/DEC-REQ-002-common-requirement-review-corrections.md` | APPROVED / INTEGRATED AS RATIONALE |
| `DEC-REQ-003` | Review consolidation/session-continuity policy | `../08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md` | APPROVED |
| `DEC-REQ-004` | Common review corrections CAN-COM-006–010 | `../08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md` | APPROVED / INTEGRATED AS RATIONALE |

## 4. Requirement ownership

- Common obligations applying across Wayfarer-owned shared/feature capabilities are owned by `SWE1-COMMON-001` without implying one fixed physical topology.
- Capabilities currently allocated to Core are owned by `SWE1-CORE-001`; later Core review may refine this allocation without overriding approved Common ownership rules.
- Main behavior is split across three `MAIN` documents to remain reviewable; it is not moved to architecture concern domains during SWE.1.
- Frontier plugin/runtime/MVI/persistence behavior is owned by `SWE1-FRONTIER-001`, with known topology/world-identifier propagation explicitly carried to later target review.
- Externally observable Worlds Beyond theme behavior is owned by `SWE1-WB-001` or `SWE1-WB-002`.
- Other documents reference full requirement IDs rather than copying normative statements.

## 5. Item summary

```text
Provisional draft Product requirements after integrated Common checkpoint: 176

CAP: 64
CON: 59
IFC: 14
QLT: 39
```

The increase from the original 164-item snapshot is entirely due to Owner-approved atomic decomposition and the retained software-level capability-reuse constraint. A complete automated recount/self-review remains required before G1.

## 6. Gate state

The Common section has been jointly reviewed and integrated, but the complete SWE.1 baseline is not approved. G1 remains `NOT_READY` until Core/Main/Frontier/WB/Scope joint review, issue resolution/acceptance, V0.0.1 baseline inventory, Project consistency review, and the final complete SWE.1 self-review are complete.
