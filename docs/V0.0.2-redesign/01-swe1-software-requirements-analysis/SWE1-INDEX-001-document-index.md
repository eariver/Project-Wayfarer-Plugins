# SWE.1 Target-domain Document Index

Document ID: `SWE1-INDEX-001`  
Revision: A  
State: `DRAFT`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 and later while documents remain active  
Predecessor: `V002-SWE1-COM-003` — superseded before G1

## 1. Purpose

Identify every controlled SWE.1 work product, the target or support domain it owns, its state, path,
and normative-item range. This index is navigational and does not duplicate normative content.

## 2. SWE.1 domain rule

Normative Product requirements use target-oriented domains:

```text
COMMON
CORE
MAIN
FRONTIER
WB
RF
ADAPTER
```

Process-support documents may use `SRC`, `PLAN`, `INDEX`, `SCOPE`, `GLOSSARY`, `ISSUE`, and `VERIFY`.
These support domains do not imply downstream architecture domains.

SWE.2 through SWE.5 will define separate concern-oriented domain dictionaries. Traceability maps each
SWE.1 target requirement to the appropriate downstream concerns.

## 3. Current and reserved documents

| Document ID | Domain role | Title / purpose | Path | State | Normative item range |
|---|---|---|---|---|---|
| `SWE1-SRC-001` | Support / source | Requirement Source Register | `REQUIREMENT_SOURCES.md` | DRAFT | Source records only |
| `SWE1-PLAN-001` | Support / plan | SWE.1 Analysis Plan | `SWE1_ANALYSIS_PLAN.md` | DRAFT | Process obligations only |
| `SWE1-INDEX-001` | Support / index | SWE.1 Target-domain Document Index | this file | DRAFT | None |
| `SWE1-SCOPE-001` | Support / scope | Scope and Non-scope | reserved | NOT_CREATED | Scope dispositions; no duplicated Product requirements |
| `SWE1-GLOSSARY-001` | Support / glossary | Glossary and controlled states | reserved | NOT_CREATED | Definitions |
| `SWE1-COMMON-001` | Product target | Cross-target common requirements | reserved | NOT_CREATED | `...-CAP-*`, `...-CON-*`, `...-IFC-*`, `...-QLT-*` |
| `SWE1-CORE-001` | Product target | Wayfarer_Core requirements | reserved | NOT_CREATED | `...-CAP-*`, `...-CON-*`, `...-IFC-*`, `...-QLT-*` |
| `SWE1-MAIN-001` | Product target | Main / Wayfarer_Main / Growth Tool requirements | reserved | NOT_CREATED | `...-CAP-*`, `...-CON-*`, `...-IFC-*`, `...-QLT-*` |
| `SWE1-FRONTIER-001` | Product target | Frontier shared requirements | reserved | NOT_CREATED | `...-CAP-*`, `...-CON-*`, `...-IFC-*`, `...-QLT-*` |
| `SWE1-WB-001` | Product target | Worlds Beyond requirements | reserved pending scope confirmation | NOT_CREATED | semantic SWE.1 items only |
| `SWE1-RF-001` | Product target | Ruined Frontier integration requirements | reserved pending scope confirmation | NOT_CREATED | semantic SWE.1 items only |
| `SWE1-ADAPTER-001` | Conditional Product target | Separate adapter requirements | reserved only if approved decision requires an adapter | NOT_CREATED | semantic SWE.1 items only |
| `SWE1-ISSUE-001` | Support / issue | Open Questions, Conflicts, and Baseline Dependencies | reserved | NOT_CREATED | `RISK-*` and `ISSUE-*` only |
| `SWE1-VERIFY-001` | Support / verification intent | Requirement Verification-intent Allocation | reserved | NOT_CREATED | Allocation records only |

## 4. Requirement ownership rules

- Each normative Product obligation is owned by the target to which it applies.
- A Main requirement about permissions remains in the `MAIN` document during SWE.1; it is not moved to
  a `PERMISSION` domain until SWE.2 architecture allocation.
- A Frontier requirement about inventory isolation remains in the appropriate `FRONTIER`, `WB`, or
  `RF` target document during SWE.1; downstream design may map it to `INVENTORY`, `STATE`, or
  `AUTHORITY` concerns.
- `COMMON` is used only for one genuinely shared obligation. Similar but observably different target
  behavior receives separate target-owned items.
- `WB`, `RF`, and `ADAPTER` documents are created only after SWE.1 confirms their V0.0.2 scope.
- Other documents reference full IDs rather than copying normative statements.

## 5. Normative item types

SWE.1 Product documents use:

```text
CAP  required capability or observable behavior
CON  required constraint or prohibition
IFC  external or inter-product interface obligation
QLT  quality, reliability, security, recovery, performance, or operability obligation
```

Examples:

```text
SWE1-MAIN-001-CAP-001
SWE1-MAIN-001-CON-002
SWE1-FRONTIER-001-IFC-003
SWE1-COMMON-001-QLT-004
```

The retired `REQ` type and every `V002-*` prefix are prohibited.

## 6. Normalization actions before G1

1. retain `SWE1-SRC-001` and `SWE1-PLAN-001` as version-independent support-document IDs;
2. replace every reference to `V002-SWE1-COM-003` with `SWE1-INDEX-001`;
3. remove or supersede every reserved global cross-cutting SWE.1 domain such as `DATA`, `SEC`, `QLT`,
   or `OPS`; allocate those concerns downstream after target requirements are approved;
4. create target documents only after scope disposition;
5. populate exact item ranges after each document is drafted;
6. complete source-to-target requirement traceability before G1 review.

## 7. Downstream allocation preview

This index does not reserve SWE.2 through SWE.6 domains. Later phase indexes may create concern domains
such as `PERMISSION`, `INVENTORY`, `AUTHORITY`, `STATE`, `PERSISTENCE`, `TRANSACTION`, or
`GAMEPLAY-FLOW` based on the approved upstream items.

A target requirement may allocate to several concern documents, and one concern document may satisfy
requirements from several targets. The traceability matrix, not this index, controls that relationship.