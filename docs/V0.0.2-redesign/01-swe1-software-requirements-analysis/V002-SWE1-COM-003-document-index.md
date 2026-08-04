# SWE.1 Domain Document Index

Document ID: `V002-SWE1-COM-003`  
Revision: A  
State: `DRAFT`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Applicable Product: Plugin V0.0.2 redesign  
Predecessor authority: `V002-GOV-COM-002`

## 1. Purpose

Identify every controlled SWE.1 work product, its owning domain, state, path, and normative-item range.
This index is navigational and does not duplicate normative requirements.

## 2. Current and reserved documents

| Document ID | Domain | Title / purpose | Path | State | Normative item range |
|---|---|---|---|---|---|
| `V002-SWE1-COM-001` | COM | Requirement Source Register | `REQUIREMENT_SOURCES.md` | DRAFT; header normalization pending | Source records only |
| `V002-SWE1-COM-002` | COM | SWE.1 Analysis Plan | `SWE1_ANALYSIS_PLAN.md` | DRAFT; header normalization pending | Process obligations only |
| `V002-SWE1-COM-003` | COM | SWE.1 Domain Document Index | this file | DRAFT | None |
| `V002-SWE1-COM-004` | COM | Scope and Non-scope | reserved | NOT_CREATED | `...-REQ-*` as applicable |
| `V002-SWE1-COM-005` | COM | Glossary and controlled states | reserved | NOT_CREATED | Definitions; no duplicated product requirements |
| `V002-SWE1-COM-006` | COM | Cross-plugin and repository-wide requirements | reserved | NOT_CREATED | `V002-SWE1-COM-006-REQ-*` |
| `V002-SWE1-CORE-001` | CORE | Wayfarer_Core requirements | reserved | NOT_CREATED | `V002-SWE1-CORE-001-REQ-*` |
| `V002-SWE1-MAIN-001` | MAIN | Wayfarer_Main and Growth Tool requirements | reserved | NOT_CREATED | `V002-SWE1-MAIN-001-REQ-*` |
| `V002-SWE1-FRONT-001` | FRONT | Wayfarer_Frontier shared requirements | reserved | NOT_CREATED | `V002-SWE1-FRONT-001-REQ-*` |
| `V002-SWE1-WB-001` | WB | Worlds Beyond requirements | reserved pending scope confirmation | NOT_CREATED | `V002-SWE1-WB-001-REQ-*` |
| `V002-SWE1-RF-001` | RF | Ruined Frontier integration requirements | reserved pending scope confirmation | NOT_CREATED | `V002-SWE1-RF-001-REQ-*` |
| `V002-SWE1-INT-001` | INT | Interface, platform, adopted-plugin, and V0.0.1 compatibility requirements | reserved | NOT_CREATED | `V002-SWE1-INT-001-REQ-*` |
| `V002-SWE1-DATA-001` | DATA | Persistence, migration, identity, transaction, cache, and audit requirements | reserved | NOT_CREATED | `V002-SWE1-DATA-001-REQ-*` |
| `V002-SWE1-SEC-001` | SEC | Permission, authority, and security requirements | reserved | NOT_CREATED | `V002-SWE1-SEC-001-REQ-*` |
| `V002-SWE1-QLT-001` | QLT | Quality, timing, failure, recovery, and observability requirements | reserved | NOT_CREATED | `V002-SWE1-QLT-001-REQ-*` |
| `V002-SWE1-OPS-001` | OPS | Configuration, lifecycle, operations, and release-facing requirements | reserved | NOT_CREATED | `V002-SWE1-OPS-001-REQ-*` |
| `V002-SWE1-COM-007` | COM | Open Questions and Conflicts | reserved | NOT_CREATED | Risk/decision references only |
| `V002-SWE1-COM-008` | COM | Verification Intent Allocation | reserved | NOT_CREATED | Requirement-to-level allocation |

## 3. Domain ownership rules

- Requirements are placed in the document that owns their normative responsibility.
- Product-domain documents reference, rather than duplicate, DATA, SEC, QLT, OPS, and INT
  requirements.
- A requirement applying identically to multiple plugins is owned by COM unless another cross-cutting
  domain is more specific.
- A cross-module interaction is owned by INT; each participating product document identifies its
  allocation without creating a second requirement.
- WB and RF documents are created only when SWE.1 confirms that their behavior belongs to V0.0.2.

## 4. Normalization actions

Before G1 review:

1. update `REQUIREMENT_SOURCES.md` header to `V002-SWE1-COM-001`;
2. update `SWE1_ANALYSIS_PLAN.md` header to `V002-SWE1-COM-002`;
3. prefix new filenames with their document IDs;
4. list exact first and last normative item identifiers after each document is populated;
5. remove any reserved document that SWE.1 proves is outside V0.0.2 scope while preserving its
   reservation/disposition history.
