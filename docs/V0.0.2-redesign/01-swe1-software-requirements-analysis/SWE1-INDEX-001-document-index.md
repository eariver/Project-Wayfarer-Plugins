# SWE.1 Target-domain Document Index

Document ID: `SWE1-INDEX-001`  
Revision: G  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 and later while items remain active  
Predecessor: Revision F

## 1. Purpose

Identify every controlled SWE.1 work product, its target/support domain, path, state, and contained item range. This index is navigational and does not duplicate normative content.

## 2. Domain rule

Normative SWE.1 Product requirements use `COMMON`, `CORE`, `MAIN`, `FRONTIER`, and `WB` target domains. `RF` and `ADAPTER` remain recognized possible domains with no normative document in current scope. Support documents use `SRC`, `PLAN`, `INDEX`, `SCOPE`, `GLOSSARY`, `ISSUE`, and `VERIFY`.

## 3. Controlled SWE.1 work products

| Document ID | Domain role | Title / purpose | Path | State | Active item range / count |
|---|---|---|---|---|---|
| `SWE1-SRC-001` | Support / source | Requirement Source Register | `REQUIREMENT_SOURCES.md` | DRAFT_FOR_OWNER_REVIEW | Source records; checkpoint revision pending/synchronized with DEC-REQ-008 |
| `SWE1-SRC-002` | Support / source | Canonical positive-requirement source | `SWE1-SRC-002-canonical-mainline-requirements.md` | DRAFT_FOR_OWNER_REVIEW | 59 `CAN-*` clauses; Common, Core, Main `CAN-MAIN-001`–`015` integrated as Revision F |
| `SWE1-PLAN-001` | Support / plan | Analysis Plan and Execution Record | `SWE1_ANALYSIS_PLAN.md` | EXECUTED_AWAITING_OWNER_REVIEW | Process record |
| `SWE1-INDEX-001` | Support / index | This target-domain index | this file | DRAFT_FOR_OWNER_REVIEW | None |
| `SWE1-SCOPE-001` | Support / scope | Scope and Non-scope | `SWE1-SCOPE-001-scope-and-non-scope.md` | DRAFT_FOR_OWNER_REVIEW | Scope dispositions |
| `SWE1-GLOSSARY-001` | Support / glossary | Glossary and controlled terms | `SWE1-GLOSSARY-001-glossary.md` | DRAFT_FOR_OWNER_REVIEW | Definitions |
| `SWE1-COMMON-001` | Product / COMMON | Cross-target common requirements | `SWE1-COMMON-001-common-requirements.md` | DRAFT_FOR_OWNER_REVIEW | CON 10 / IFC 6 / QLT 14 = 30 active |
| `SWE1-CORE-001` | Product / CORE | Wayfarer_Core requirements | `SWE1-CORE-001-core-requirements.md` | DRAFT_FOR_OWNER_REVIEW | CAP 2 / CON 7 / IFC 3 / QLT 1 = 13 active; historical `CON-004` superseded |
| `SWE1-MAIN-001` | Product / MAIN | Lifecycle, authority, delivery, possession/lifecycle and modification policy | `SWE1-MAIN-001-authority-delivery-requirements.md` | DRAFT_FOR_OWNER_REVIEW | CAP 11 / CON 9 / QLT 2 = 22 active; historical `CON-008`, `CON-009` superseded |
| `SWE1-MAIN-002` | Product / MAIN | Progress, evolution, durability, checkpoint | `SWE1-MAIN-002-progress-durability-requirements.md` | DRAFT_FOR_OWNER_REVIEW | CAP 16 / CON 5 / QLT 7 = 28 active; historical `CAP-006` superseded |
| `SWE1-MAIN-003` | Product / MAIN | GUI, repair, reissue, admin, permission | `SWE1-MAIN-003-repair-reissue-admin-requirements.md` | DRAFT_FOR_OWNER_REVIEW | CAP 10 / CON 4 / IFC 1 / QLT 4 = 19 active; historical `QLT-002` superseded |
| `SWE1-FRONTIER-001` | Product / FRONTIER | Runtime boundary, MVI, persistence, permission | `SWE1-FRONTIER-001-boundary-persistence-requirements.md` | DRAFT_FOR_OWNER_REVIEW | 14 active |
| `SWE1-WB-001` | Product / WB | Loadout, permanent items, hook, navigation | `SWE1-WB-001-loadout-navigation-requirements.md` | DRAFT_FOR_OWNER_REVIEW | 24 active |
| `SWE1-WB-002` | Product / WB | Launchpad, shop, portal, administration | `SWE1-WB-002-launchpad-shop-portal-requirements.md` | DRAFT_FOR_OWNER_REVIEW | 28 active |
| `SWE1-ISSUE-001` | Support / issue | Open questions and conflicts | `SWE1-ISSUE-001-open-questions.md` | DRAFT_FOR_OWNER_REVIEW | ISSUE-001–009 |
| `SWE1-VERIFY-001` | Support / verification | Verification-intent allocation | `SWE1-VERIFY-001-verification-intent.md` | DRAFT_FOR_OWNER_REVIEW | 178 provisional active allocations after Main CAN-MAIN-011–015 checkpoint |

Cross-directory controlled work products:

| Document ID | Purpose | Path | State |
|---|---|---|---|
| `TRC-SWE1-001` | Source-to-requirement bidirectional traceability | `../07-traceability/TRC-SWE1-001-source-requirement-traceability.md` | DRAFT_FOR_OWNER_REVIEW / Revision G |
| `REV-SWE1-001` | Complete SWE.1 package self-review snapshot | `../10-reviews-and-evidence/REV-SWE1-001-self-review.md` | SUPERSEDED_AS_CURRENT_EVIDENCE_BY_OWNER_REVIEW |
| `REV-SWE1-002` | Joint Owner clause review log | `../10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md` | IN_REVIEW / checkpoint synchronized through CAN-MAIN-015 |
| `DEC-REQ-001` | Canonical source merge and analysis method | `../08-decisions/DEC-REQ-001-canonical-source-merge-and-swe1-analysis.md` | APPROVED |
| `DEC-REQ-002` | Common review corrections CAN-COM-001–005 | `../08-decisions/DEC-REQ-002-common-requirement-review-corrections.md` | APPROVED / INTEGRATED |
| `DEC-REQ-003` | Review consolidation/session-continuity policy | `../08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md` | APPROVED |
| `DEC-REQ-004` | Common review corrections CAN-COM-006–010 | `../08-decisions/DEC-REQ-004-common-requirement-review-corrections-006-010.md` | APPROVED / INTEGRATED |
| `DEC-REQ-005` | Core review corrections CAN-CORE-001–005 | `../08-decisions/DEC-REQ-005-core-requirement-review-corrections-001-005.md` | APPROVED / INTEGRATED |
| `DEC-REQ-006` | Main review corrections CAN-MAIN-001–005 | `../08-decisions/DEC-REQ-006-main-requirement-review-corrections-001-005.md` | APPROVED / INTEGRATED |
| `DEC-REQ-007` | Main review corrections CAN-MAIN-006–010 | `../08-decisions/DEC-REQ-007-main-requirement-review-corrections-006-010.md` | APPROVED / INTEGRATED |
| `DEC-REQ-008` | Main review corrections CAN-MAIN-011–015 | `../08-decisions/DEC-REQ-008-main-requirement-review-corrections-011-015.md` | APPROVED / INTEGRATED |

## 4. Requirement ownership and reviewed Main boundary

- Common obligations remain in `SWE1-COMMON-001` without implying fixed physical topology.
- Core owns the V0.0.2 shared Waymark transaction contract and preserves accepted V0.0.1 public contract/migration history, not implementation structure.
- Main requirements distinguish Main-owned logical Growth Tool state from Minecraft-owned physical item state.
- Physical possession/storage/death/item lifecycle does not transfer logical ownership or create replacement entitlement.
- Progress through CAN-MAIN-010 uses configured exact worlds, provenance-neutral/repeated eligible mining, representation-independent numeric safety, and one uniform configured increment.
- CAN-MAIN-011–013 now treat material/enchantment values as approved configuration/defaults where appropriate, preserve cumulative progress as authority during reconciliation, and define Broken as a same-authority lifecycle transition.
- CAN-MAIN-014 defines the owner management interface without fixing presentation layout or pre-approving permission allocation.
- CAN-MAIN-015 defines quote-confirmed protected full repair, including same-authority BROKEN→ACTIVE repair and Repair-specific UNKNOWN containment.
- CAN-MAIN-016 and later Main, Frontier, WB, and Scope requirements remain subject to owning-clause review.

## 5. Item summary

```text
Provisional active Product requirements after integrated Main CAN-MAIN-011–015 checkpoint: 178

CAP: 67
CON: 58
IFC: 14
QLT: 39
```

Historical superseded identifiers are retained and not reused:

- `SWE1-CORE-001-CON-004`
- `SWE1-MAIN-001-CON-008`
- `SWE1-MAIN-001-CON-009`
- `SWE1-MAIN-002-CAP-006`
- `SWE1-MAIN-003-QLT-002`

New active identifiers introduced by the CAN-MAIN-011–015 checkpoint:

- `SWE1-MAIN-002-CAP-017`
- `SWE1-MAIN-002-QLT-007`
- `SWE1-MAIN-003-CAP-010`

The complete automated recount/source/identifier/verification-intent audit remains required before G1.

## 6. Gate state

Common, Core, and Main through `CAN-MAIN-015` are jointly reviewed and integrated, but the complete SWE.1 baseline is not approved. G1 remains `NOT_READY` until remaining Main/Frontier/WB/Scope review, issue resolution/acceptance, V0.0.1 baseline inventory, Project consistency review, and final complete SWE.1 self-review are complete.

Next substantive review item: `CAN-MAIN-016 — Player-paid reissue`.
