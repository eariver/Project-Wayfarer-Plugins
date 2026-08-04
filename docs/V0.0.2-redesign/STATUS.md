# V0.0.2 Redesign Status

Updated: 2026-08-05 JST  
Branch: `redesign/V0.0.2-swe1-3`  
Draft PR: `#18`  
Baseline: Plugin V0.0.1 on `main`  
Current process: `SWE.1 Software Requirements Analysis`  
Current gate: `SWE1_REQUIREMENTS_BASELINE_NOT_READY`

## Executive status

```text
V0.0.1 BASELINE:
  ASSUMED ACCEPTED FOR V0.0.2 REQUIREMENTS ANALYSIS

V0.0.1 REDESIGN:
  OUT OF CURRENT SCOPE

PR #14 / CANDIDATE-8:
  FROZEN REFERENCE / NOT A DESIGN AUTHORITY

PR #18:
  OPEN / DRAFT / DESIGN AUTHORITY WORKSTREAM

CANDIDATE-9:
  NOT STARTED / NOT AUTHORIZED

SWE.1:
  ACTIVE

SWE.2:
  NOT STARTED

SWE.3 DETAILED DESIGN:
  NOT STARTED

SWE.3 UNIT CONSTRUCTION:
  NOT AUTHORIZED

SWE.4 THROUGH SWE.6:
  NOT AUTHORIZED

MERGE / TAG / RELEASE:
  NOT AUTHORIZED
```

## Active objective

Establish approved, target-domain V0.0.2 Software Requirements documents and bidirectional
traceability before any architecture or implementation decision.

Controlled IDs are version-independent. Each SWE process defines domains suited to that process:

- SWE.1 and SWE.6 are normally Product-target oriented;
- SWE.2 through SWE.5 are normally architecture, implementation, unit, or integration-concern
  oriented;
- cross-process relationships are explicit trace links, not matching domain names.

## Current work products

| Work product | State | Authority |
|---|---|---|
| Redesign entry point | Prepared | `README.md` |
| Redesign charter | Prepared for Owner confirmation | `00-governance/REDESIGN_CHARTER.md` |
| V-model process and roles | Prepared for Owner confirmation | `00-governance/V_MODEL_PROCESS_AND_ROLES.md` |
| Document control and gates | Prepared for Owner confirmation | `GOV-CONTROL-001` / `00-governance/DOCUMENT_CONTROL_AND_GATES.md` |
| Phase-specific domain, identifier, and source traceability model | Owner-approved | `GOV-TRACE-001` / `00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md` |
| External library and reference policy | Owner-approved | `GOV-ENG-001` / `00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md` |
| Legacy V0.0.2 freeze | Approved baseline | `00-governance/LEGACY_V002_FREEZE.md` |
| V-model role decision | Approved baseline | `08-decisions/DEC-PROC-001-v-model-role-allocation.md` |
| External library reuse decision | Approved baseline | `08-decisions/DEC-ENG-001-external-library-reuse-and-reference-policy.md` |
| Initial domain/identifier decision | Superseded | `08-decisions/DEC-DOC-001-domain-document-and-linked-identifier-model.md` |
| Corrected domain/source-traceability decision | Approved baseline | `08-decisions/DEC-DOC-002-process-specific-domain-and-source-traceability-model.md` |
| SWE.1 source register | Draft | `SWE1-SRC-001` / `01-swe1-software-requirements-analysis/REQUIREMENT_SOURCES.md` |
| SWE.1 analysis plan | Draft Revision B | `SWE1-PLAN-001` / `01-swe1-software-requirements-analysis/SWE1_ANALYSIS_PLAN.md` |
| SWE.1 target-domain document index | Draft | `SWE1-INDEX-001` / `01-swe1-software-requirements-analysis/SWE1-INDEX-001-document-index.md` |
| Target-domain software requirements | Not started | reserved in `SWE1-INDEX-001` |
| Requirements traceability matrix | Not started | planned under `07-traceability/` |

## Identifier policy now in force

```text
Document ID:
  <PROCESS>-<DOMAIN>-<DOCUMENT_NUMBER>

Normative item ID:
  <DOCUMENT_ID>-<SEMANTIC_ITEM_TYPE>-<ITEM_NUMBER>

Version token:
  prohibited in IDs

SWE.1 item types:
  CAP / CON / IFC / QLT

SWE.2 item types:
  CMP / IFC / FLOW / STM / DAT / DEP / CON

SWE.3 item types:
  DD / REF / IMP

SWE.4-SWE.6 item types:
  CASE / PROC / ENV / DATA / ORCL / EVID / REF
```

The process-redundant types `REQ`, `ARC`, `UV`, `IV`, and `QV` are retired.

## SWE.3 construction traceability requirement

Future production classes and methods implementing normative SWE.3 behavior must list applicable full
SWE.3 `DD` IDs in Javadoc. Focused inline trace comments are required only for distinct code regions
inside a method that map to items from multiple detailed-design documents.

A deterministic source scan must generate the implementation-to-detailed-design consistency report and
reject missing or invalid mappings before G3C can pass.

## Immediate next actions

1. Complete the immutable SWE.1 source baseline.
2. Inventory V0.0.1 public API, contracts, migrations, and release handoff constraints.
3. Confirm V0.0.2 scope and the final SWE.1 target-domain document set.
4. Classify each source statement by authority, target, applicability, conflict status, and release
   scope.
5. Extract singular `CAP`, `CON`, `IFC`, and `QLT` items into their owning target documents.
6. Derive applicable reuse, maintainability, dependency-selection, and official-reference constraints
   without selecting concrete architecture during SWE.1.
7. Define verification intent and complete bidirectional source-to-requirement traceability.
8. Record ambiguities, conflicts, missing decisions, and V0.0.1 baseline dependency issues.
9. Present the SWE.1 baseline for Owner review before SWE.2 begins.

## Stop conditions

Stop SWE.1 and request an Owner decision when:

- two equal- or higher-authority sources require incompatible behavior;
- a required gameplay behavior is undefined and cannot be derived without Product invention;
- V0.0.2 requires a V0.0.1 interface change that would invalidate the accepted baseline premise;
- scope cannot be bounded between V0.0.2 and a later release;
- a requirement cannot be stated in a verifiable and implementation-independent form;
- a normative item cannot be assigned an unambiguous SWE.1 Product target.

## Continuation rule

A new ChatGPT session or Codex task must read this file first. Work may continue only from the
current process and gate recorded here. Chat history is supplementary context, not process authority.