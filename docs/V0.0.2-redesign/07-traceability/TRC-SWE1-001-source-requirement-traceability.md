# SWE.1 Source-to-Requirement Traceability

Document ID: `TRC-SWE1-001`  
Revision: G  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Traceability scope: `SWE1-SRC-002` Revision F to the 178 provisional active SWE.1 Product requirements after integrated Common, Core, and Main review through CAN-MAIN-015  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded

## 1. Purpose

Provide bidirectional traceability from the 59 canonical source clauses to the provisional 178 active SWE.1 Product requirements after integration of the Owner-approved Common, Core, and Main corrections through `CAN-MAIN-015`. Forward traceability is controlled here; reverse traceability is controlled by each active requirement's `Source` field, with explicit disposition for historical superseded identifiers.

This checkpoint revision integrates `CAN-MAIN-011` through `CAN-MAIN-015`, adds three active requirements (`SWE1-MAIN-002-CAP-017`, `SWE1-MAIN-002-QLT-007`, `SWE1-MAIN-003-CAP-010`), and supersedes duplicate `SWE1-MAIN-003-QLT-002`. It does not claim the complete automated identifier/source/count audit has already been rerun; that remains mandatory before G1.

## 2. Source-clause forward traceability

| Canonical clause | Downstream requirement(s) or disposition |
|---|---|
| `CAN-COM-001` | `SWE1-COMMON-001-CON-001`; `SWE1-CORE-001-CAP-001`; `SWE1-FRONTIER-001-CON-001` (known target conflict carried to Frontier review) |
| `CAN-COM-002` | `SWE1-COMMON-001-CON-002`; `SWE1-COMMON-001-CON-007`; `SWE1-COMMON-001-CON-008` |
| `CAN-COM-003` | `SWE1-COMMON-001-IFC-001`; `SWE1-COMMON-001-CON-009`; `SWE1-COMMON-001-IFC-003`; `SWE1-COMMON-001-IFC-004`; `SWE1-COMMON-001-IFC-005`; `SWE1-FRONTIER-001-IFC-001`; reviewed Main durability-authority specialization in `SWE1-MAIN-001-CON-002` |
| `CAN-COM-004` | `SWE1-COMMON-001-CON-003`; `SWE1-COMMON-001-CON-004`; `SWE1-COMMON-001-CON-005`; `SWE1-CORE-001-CON-006`; `SWE1-FRONTIER-001-CON-004` |
| `CAN-COM-005` | `SWE1-COMMON-001-QLT-001`; `SWE1-COMMON-001-QLT-002`; `SWE1-COMMON-001-QLT-009`; `SWE1-MAIN-001-CAP-005`; `SWE1-MAIN-001-CAP-006`; `SWE1-WB-002-QLT-004` |
| `CAN-COM-006` | `SWE1-COMMON-001-QLT-003`; `SWE1-COMMON-001-QLT-004`; `SWE1-COMMON-001-QLT-010`; `SWE1-COMMON-001-QLT-011`; `SWE1-MAIN-001-QLT-001`; `SWE1-MAIN-001-QLT-002`; `SWE1-FRONTIER-001-CON-002`; `SWE1-FRONTIER-001-CAP-001` |
| `CAN-COM-007` | `SWE1-COMMON-001-QLT-005`; `SWE1-COMMON-001-QLT-006`; `SWE1-COMMON-001-QLT-012`; `SWE1-COMMON-001-QLT-013`; `SWE1-CORE-001-QLT-001`; `SWE1-MAIN-001-CAP-005`; `SWE1-MAIN-001-CAP-007`; `SWE1-MAIN-001-CAP-009`; `SWE1-MAIN-003-QLT-001`; `SWE1-MAIN-003-CAP-005`; `SWE1-MAIN-003-QLT-005`; `SWE1-MAIN-003-CAP-008`; `SWE1-MAIN-003-QLT-003`; `SWE1-WB-002-QLT-003`; historical `SWE1-MAIN-003-QLT-002` superseded by deduplication |
| `CAN-COM-008` | `SWE1-COMMON-001-IFC-002`; `SWE1-COMMON-001-IFC-006`; `SWE1-COMMON-001-CON-006`; `SWE1-COMMON-001-QLT-007`; `SWE1-CORE-001-CON-008`; `SWE1-FRONTIER-001-CON-006`; `SWE1-FRONTIER-001-QLT-002` |
| `CAN-COM-009` | `SWE1-COMMON-001-QLT-008`; `SWE1-COMMON-001-QLT-014`; `SWE1-MAIN-001-CAP-009`; `SWE1-WB-002-CAP-010`; `SWE1-WB-002-CAP-016` |
| `CAN-COM-010` | `SWE1-COMMON-001-CON-010`; concrete capability/dependency/API selection and authoritative-reference evidence are governed by `GOV-ENG-001` in SWE.2/SWE.3 |
| `CAN-CORE-001` | `SWE1-CORE-001-IFC-001`; `SWE1-CORE-001-IFC-002`; `SWE1-CORE-001-CON-008` |
| `CAN-CORE-002` | `SWE1-CORE-001-CON-001`; `SWE1-CORE-001-CON-002` |
| `CAN-CORE-003` | `SWE1-CORE-001-CAP-002`; `SWE1-CORE-001-IFC-003`; `SWE1-CORE-001-QLT-001`; reviewed Main financial-capability dependency specialization in `SWE1-MAIN-001-QLT-001`; reviewed Main repair use in `SWE1-MAIN-003-CAP-005` |
| `CAN-CORE-004` | `SWE1-CORE-001-CON-003`; `SWE1-CORE-001-CON-005`; inherited `UNKNOWN`/replay obligations use `SWE1-COMMON-001-QLT-006`, `SWE1-COMMON-001-QLT-012`, and `SWE1-CORE-001-QLT-001`; historical `SWE1-CORE-001-CON-004` superseded by deduplication |
| `CAN-CORE-005` | `SWE1-CORE-001-CON-007`; `SWE1-CORE-001-CON-008` |
| `CAN-FRONTIER-001` | `SWE1-FRONTIER-001-CON-001`; `SWE1-FRONTIER-001-CON-002` |
| `CAN-FRONTIER-002` | `SWE1-FRONTIER-001-CAP-001`; `SWE1-FRONTIER-001-CON-003` |
| `CAN-FRONTIER-003` | `SWE1-FRONTIER-001-IFC-001`; `SWE1-FRONTIER-001-CON-004`; `SWE1-FRONTIER-001-CON-005` |
| `CAN-FRONTIER-004` | `SWE1-FRONTIER-001-CON-004`; `SWE1-FRONTIER-001-CAP-002`; `SWE1-FRONTIER-001-CON-006`; `SWE1-FRONTIER-001-QLT-001`; `SWE1-FRONTIER-001-QLT-002` |
| `CAN-FRONTIER-005` | `SWE1-FRONTIER-001-IFC-002`; `SWE1-FRONTIER-001-CON-007`; `SWE1-FRONTIER-001-CON-008`; `SWE1-WB-002-IFC-001` |
| `CAN-MAIN-001` | `SWE1-MAIN-001-CON-001`; `SWE1-MAIN-001-QLT-001` |
| `CAN-MAIN-002` | `SWE1-MAIN-001-CAP-001`; `SWE1-MAIN-001-CAP-002`; `SWE1-MAIN-001-CAP-010`; `SWE1-MAIN-001-CAP-011`; `SWE1-MAIN-001-CON-002` |
| `CAN-MAIN-003` | `SWE1-MAIN-001-CAP-003`; `SWE1-MAIN-001-CON-003`; `SWE1-MAIN-001-CAP-004`; `SWE1-MAIN-001-CON-004` |
| `CAN-MAIN-004` | `SWE1-MAIN-001-CAP-005`; `SWE1-MAIN-001-CAP-006`; `SWE1-MAIN-001-CAP-007`; `SWE1-MAIN-001-CON-005`; `SWE1-MAIN-001-CAP-009` |
| `CAN-MAIN-005` | `SWE1-MAIN-001-CAP-004`; `SWE1-MAIN-001-CAP-008`; `SWE1-MAIN-001-CON-006`; `SWE1-MAIN-001-CON-007`; `SWE1-MAIN-001-CON-010`; `SWE1-MAIN-001-CON-011` |
| `CAN-MAIN-006` | `SWE1-MAIN-001-CON-005`; `SWE1-MAIN-001-CON-007`; historical `SWE1-MAIN-001-CON-008` superseded by Owner correction; historical `SWE1-MAIN-001-CON-009` superseded by deduplication |
| `CAN-MAIN-007` | `SWE1-MAIN-002-CAP-001`; `SWE1-MAIN-002-CON-001` |
| `CAN-MAIN-008` | `SWE1-MAIN-002-CAP-002`; `SWE1-MAIN-002-CAP-003`; `SWE1-MAIN-002-CON-002` |
| `CAN-MAIN-009` | `SWE1-MAIN-002-CAP-004`; `SWE1-MAIN-002-QLT-001`; `SWE1-MAIN-002-QLT-002`; stale `Long.MAX_VALUE` wording in later unreviewed clauses is constrained by `DEC-REQ-007` until owning review |
| `CAN-MAIN-010` | `SWE1-MAIN-002-CAP-005`; historical `SWE1-MAIN-002-CAP-006` superseded because material/ore weighting was withdrawn |
| `CAN-MAIN-011` | `SWE1-MAIN-002-CAP-007`; `SWE1-MAIN-002-CAP-008`; `SWE1-MAIN-002-CAP-017`; `SWE1-MAIN-002-CON-003`; `SWE1-MAIN-002-CAP-009` |
| `CAN-MAIN-012` | `SWE1-MAIN-002-QLT-003`; `SWE1-MAIN-002-QLT-007`; `SWE1-MAIN-002-CAP-010`; `SWE1-MAIN-002-CON-004`; `SWE1-MAIN-002-CAP-011`; `SWE1-MAIN-002-CAP-012` |
| `CAN-MAIN-013` | `SWE1-MAIN-002-CAP-013`; `SWE1-MAIN-002-CAP-014`; `SWE1-MAIN-002-CON-005`; `SWE1-MAIN-002-QLT-004` |
| `CAN-MAIN-014` | `SWE1-MAIN-003-CAP-001`; `SWE1-MAIN-003-CAP-002`; `SWE1-MAIN-003-CAP-003`; `SWE1-MAIN-003-QLT-004` |
| `CAN-MAIN-015` | `SWE1-MAIN-003-CAP-003`; `SWE1-MAIN-003-QLT-001`; `SWE1-MAIN-003-CAP-004`; `SWE1-MAIN-003-CON-001`; `SWE1-MAIN-003-CAP-005`; `SWE1-MAIN-003-CAP-010`; `SWE1-MAIN-003-QLT-005`; historical `SWE1-MAIN-003-QLT-002` superseded by deduplication |
| `CAN-MAIN-016` | `SWE1-MAIN-003-QLT-001`; `SWE1-MAIN-003-CAP-006`; `SWE1-MAIN-003-CAP-007`; `SWE1-MAIN-003-CAP-008`; `SWE1-MAIN-003-CON-002`; `SWE1-MAIN-003-QLT-003`; current physical-absence and exact-price wording requires correction under `DEC-REQ-006` §7.2 during owning review |
| `CAN-MAIN-017` | `SWE1-MAIN-001-QLT-002`; `SWE1-MAIN-002-CAP-015`; `SWE1-MAIN-002-QLT-005`; `SWE1-MAIN-002-QLT-006`; reviewed Broken continuity `SWE1-MAIN-002-QLT-004` constrains later persistence wording |
| `CAN-MAIN-018` | `SWE1-MAIN-003-CAP-009`; `SWE1-MAIN-003-IFC-001`; `SWE1-MAIN-003-CON-003`; `SWE1-MAIN-003-CON-004` |
| `CAN-MAIN-019` | `SWE1-MAIN-002-CAP-016` |
| `CAN-SCOPE-001` | `SWE1-MAIN-002-CAP-009` plus non-scope disposition in `SWE1-SCOPE-001` |
| `CAN-SCOPE-002` | `SWE1-FRONTIER-001-CON-002`; `SWE1-WB-001-CON-007`; `SWE1-WB-002-CON-006`; `SWE1-WB-002-CON-007`; remaining exclusions in `SWE1-SCOPE-001` |
| `CAN-SCOPE-003` | Scope disposition in `SWE1-SCOPE-001` §5; no ADAPTER target requirement document is created. |
| `CAN-SCOPE-004` | Process/non-software disposition in `SWE1-SCOPE-001` §4. |
| `CAN-WB-001` | `SWE1-WB-001-CAP-001`; `SWE1-WB-001-CON-001` |
| `CAN-WB-002` | `SWE1-FRONTIER-001-QLT-001`; `SWE1-WB-001-CAP-002`; `SWE1-WB-001-CAP-003`; `SWE1-WB-001-CAP-004`; `SWE1-WB-001-QLT-001`; `SWE1-WB-001-QLT-002` |
| `CAN-WB-003` | `SWE1-WB-001-CAP-005`; `SWE1-WB-001-CON-002`; `SWE1-WB-001-CAP-006` |
| `CAN-WB-004` | `SWE1-FRONTIER-001-QLT-001`; `SWE1-WB-001-CON-003`; `SWE1-WB-001-CAP-007`; `SWE1-WB-001-CAP-008`; `SWE1-WB-001-QLT-003`; `SWE1-WB-001-CON-004` |
| `CAN-WB-005` | `SWE1-WB-001-CAP-009` |
| `CAN-WB-006` | `SWE1-COMMON-001-CON-005`; `SWE1-WB-001-IFC-001`; `SWE1-WB-001-CON-005`; `SWE1-WB-001-CON-006`; `SWE1-WB-001-QLT-004` |
| `CAN-WB-007` | `SWE1-WB-001-CAP-010`; `SWE1-WB-001-CON-007`; `SWE1-WB-001-CON-008`; `SWE1-WB-001-QLT-005` |
| `CAN-WB-008` | `SWE1-WB-002-CAP-001`; `SWE1-WB-002-CON-001`; `SWE1-WB-002-CAP-002`; `SWE1-WB-002-CAP-003` |
| `CAN-WB-009` | `SWE1-WB-002-CAP-002`; `SWE1-WB-002-CAP-004`; `SWE1-WB-002-CON-002`; `SWE1-WB-002-QLT-001` |
| `CAN-WB-010` | `SWE1-WB-002-CAP-005`; `SWE1-WB-002-CAP-006`; `SWE1-WB-002-CON-003` |
| `CAN-WB-011` | `SWE1-WB-002-CAP-007`; `SWE1-WB-002-CAP-008`; `SWE1-WB-002-QLT-002`; `SWE1-WB-002-CAP-009` |
| `CAN-WB-012` | `SWE1-WB-002-CAP-010`; `SWE1-WB-002-CON-004`; `SWE1-WB-002-CAP-011` |
| `CAN-WB-013` | `SWE1-WB-002-CAP-012`; `SWE1-WB-002-CAP-013`; `SWE1-WB-002-CON-005`; `SWE1-WB-002-QLT-004` |
| `CAN-WB-014` | `SWE1-WB-002-CAP-014`; `SWE1-WB-002-CON-006`; `SWE1-WB-002-QLT-003` |
| `CAN-WB-015` | `SWE1-WB-002-CAP-015`; `SWE1-WB-002-CON-007` |
| `CAN-WB-016` | `SWE1-WB-002-CAP-016`; `SWE1-WB-002-IFC-001` |

## 3. Reverse-traceability control

The active requirement-owned `Source` field is the authoritative reverse link from each requirement to its canonical clause(s), amendment(s), and controlling review decision where applicable. Historical superseded identifiers are excluded from the active reverse-trace set but retain explicit replacement/disposition records.

At this integrated Main CAN-MAIN-011–015 checkpoint:

```text
PROVISIONAL ACTIVE PRODUCT REQUIREMENT COUNT:
  178

ACTIVE REQUIREMENT TYPES:
  CAP 67
  CON 58
  IFC 14
  QLT 39

SWE1-MAIN-001 ACTIVE REQUIREMENTS:
  22

SWE1-MAIN-002 ACTIVE REQUIREMENTS:
  28

SWE1-MAIN-003 ACTIVE REQUIREMENTS:
  19

COMMON SOURCE CLAUSES REVIEWED/INTEGRATED:
  CAN-COM-001 through CAN-COM-010

CORE SOURCE CLAUSES REVIEWED/INTEGRATED:
  CAN-CORE-001 through CAN-CORE-005

MAIN SOURCE CLAUSES REVIEWED/INTEGRATED:
  CAN-MAIN-001 through CAN-MAIN-015

NEW ACTIVE IDENTIFIERS THIS CHECKPOINT:
  SWE1-MAIN-002-CAP-017
  SWE1-MAIN-002-QLT-007
  SWE1-MAIN-003-CAP-010

NEW HISTORICAL SUPERSEDED IDENTIFIER THIS CHECKPOINT:
  SWE1-MAIN-003-QLT-002

FULL POST-CHECKPOINT AUTOMATED REVERSE-TRACE AUDIT:
  PENDING BEFORE G1
```

The prior automated count results remain historical evidence only. Before G1, the complete package self-review shall parse every active `Source` field, verify canonical identifiers, detect active requirements without source, detect canonical clauses without downstream requirement/disposition, verify superseded dispositions, and verify identifier uniqueness/counts.

## 4. Amendment traceability

| Amendment | Direct downstream effect |
|---|---|
| `AMD-001` | `SWE1-WB-002-CON-003`; `SWE1-WB-002-CAP-008` |
| `AMD-002` | `SWE1-WB-002-CAP-015`; `SWE1-WB-002-CON-007` |
| `AMD-003` | `SWE1-WB-002-CAP-006` and issue `SWE1-ISSUE-001-ISSUE-005` |
| `AMD-004` | `SWE1-WB-002-CAP-004`; `SWE1-WB-002-CON-002`; scope non-scope |
| `AMD-005` | `SWE1-WB-002-CON-005` |
| `AMD-006` | Death-drop suppression portion superseded by `DEC-REQ-007`; replacement coverage in Main `CON-007`; no-implicit-respawn-replacement coverage in Main `CON-005`; other AMD-006-derived later requirements remain subject to owning review |
| `AMD-007` | `SWE1-MAIN-003-CAP-006`–`CAP-008`; `SWE1-MAIN-003-CON-002`; exact current-absence and pricing semantics require owning CAN-MAIN-016 correction under `DEC-REQ-006` §7.2 |
| `AMD-008` | `SWE1-WB-001-CON-003`; `SWE1-WB-001-CAP-007`; `SWE1-WB-001-CAP-008`; `SWE1-WB-001-QLT-003` |
| `AMD-009` | Concrete `Long.MAX_VALUE` rule superseded by `DEC-REQ-007`; representation-independent safety now covered by `SWE1-MAIN-002-QLT-001` / `QLT-002` |
| `AMD-010` | `SWE1-MAIN-003-QLT-004`; `SWE1-WB-001-QLT-005` |
| `AMD-011` | `SWE1-MAIN-003-IFC-001`; `SWE1-FRONTIER-001-IFC-002`; `SWE1-WB-002-IFC-001` |
| `AMD-012` | Excluded as process/release-stage material; no Product requirement generated. |

## 5. Joint-review decision traceability

| Decision | Integrated effect |
|---|---|
| `DEC-REQ-002` | Common `CAN-COM-001`–`005`; topology/shared ownership, authority split, external-state boundaries, execution-context rules; added Common `CON-009`, `IFC-003`–`IFC-005`, `QLT-009` |
| `DEC-REQ-004` | Common `CAN-COM-006`–`010`; lifecycle, protected-operation safety, migration ownership, audit/data minimization, capability reuse; added Common `QLT-010`–`QLT-014`, `IFC-006`, `CON-010` |
| `DEC-REQ-005` | Core `CAN-CORE-001`–`005`; accepted-contract compatibility, public API abstraction/type identity, V0.0.2 Core Waymark transaction allocation/provider guarantee boundary, ambiguity/side-channel constraints, Core schema evolution and accepted migration immutability; superseded duplicate Core `CON-004` |
| `DEC-REQ-006` | Main `CAN-MAIN-001`–`005`; topology-independent allocation, logical/Minecraft physical authority split, atomic lifecycle/delivery/branch state, physical identity resolution, same-entitlement delivery, possession/storage neutrality, owner-only use, controlled durability/enchantment modification, anvil/grindstone prohibition; added Main `CAP-010`, `CAP-011`, `CON-010`, `CON-011` |
| `DEC-REQ-007` | Main `CAN-MAIN-006`–`010`; ordinary death/item-lifecycle neutrality, configured exact progress worlds, qualifying player-mined/provenance-neutral progress, representation-independent numeric safety, uniform per-break progress; superseded Main `CON-008`, `CON-009`, and `CAP-006`; superseded concrete AMD-009 `Long.MAX_VALUE` rule |
| `DEC-REQ-008` | Main `CAN-MAIN-011`–`015`; configured material/enchantment evolution, internally consistent reconciliation, same-authority Broken transition, owner management interface, configured quote-confirmed protected full repair; added Main `CAP-017`, `QLT-007`, Main GUI/repair `CAP-010`; superseded duplicate Main repair `QLT-002` |

## 6. Current disposition

Traceability is updated through `CAN-MAIN-015` and remains draft. It is not an approved G1 baseline. Review resumes at `CAN-MAIN-016 — Player-paid reissue`; remaining Main, Frontier, Worlds Beyond, Scope, and issue-specific clauses remain subject to continued Owner review, and the complete automated source/identifier/recount audit is required after all joint-review corrections are consolidated.
