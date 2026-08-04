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

Establish approved, domain-separated V0.0.2 Software Requirements documents and bidirectional
requirements traceability before any architecture or implementation decision.

Every controlled document receives a stable document ID. Every normative requirement or design item
uses a full identifier concatenated from the owning document ID and local item ID.

## Current work products

| Work product | State | Authority |
|---|---|---|
| Redesign entry point | Prepared | `README.md` |
| Redesign charter | Prepared for Owner confirmation | `00-governance/REDESIGN_CHARTER.md` |
| V-model process and roles | Prepared for Owner confirmation | `00-governance/V_MODEL_PROCESS_AND_ROLES.md` |
| Document control and gates | Prepared for Owner confirmation | `V002-GOV-COM-001` / `00-governance/DOCUMENT_CONTROL_AND_GATES.md` |
| Domain document and linked identifier model | Owner-approved | `V002-GOV-COM-002` / `00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md` |
| External library and reference policy | Owner-approved | `00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md` |
| Legacy V0.0.2 freeze | Approved baseline | `00-governance/LEGACY_V002_FREEZE.md` |
| V-model role decision | Approved baseline | `08-decisions/DEC-PROC-001-v-model-role-allocation.md` |
| External library reuse decision | Approved baseline | `08-decisions/DEC-ENG-001-external-library-reuse-and-reference-policy.md` |
| Domain/identifier decision | Approved baseline | `08-decisions/DEC-DOC-001-domain-document-and-linked-identifier-model.md` |
| SWE.1 source register | Draft; header normalization pending | target `V002-SWE1-COM-001` |
| SWE.1 analysis plan | Draft; header normalization pending | target `V002-SWE1-COM-002` |
| SWE.1 domain document index | Draft | `V002-SWE1-COM-003` |
| Domain-separated software requirements | Not started | reserved in `V002-SWE1-COM-003` |
| Requirements traceability matrix | Not started | planned under `07-traceability/` |

## Immediate next actions

1. Normalize the existing SWE.1 source register and analysis-plan headers to the assigned document
   identifiers.
2. Retrieve and hash all remaining authoritative V0.0.2 input sources from the Project and Plugin
   repositories.
3. Inventory V0.0.1 public API, contracts, migrations, and release handoff constraints.
4. Classify each source statement by authority, applicability, conflict status, and release scope.
5. Confirm the final V0.0.2 domain-document set and remove or disposition reserved documents that are
   outside scope.
6. Extract singular software requirements into their owning domain documents using full linked IDs,
   for example `V002-SWE1-MAIN-001-REQ-001`.
7. Derive applicable reuse, maintainability, dependency-selection, and official-reference constraints
   without prematurely selecting concrete libraries during SWE.1.
8. Define verification intent and complete bidirectional traceability using full identifiers only.
9. Record ambiguities, conflicts, missing decisions, and V0.0.1 baseline dependency issues.
10. Present the SWE.1 baseline for Owner review before SWE.2 begins.

## Stop conditions

Stop SWE.1 and request an Owner decision when:

- two equal- or higher-authority sources require incompatible behavior;
- a required gameplay behavior is undefined and cannot be derived without product invention;
- V0.0.2 requires a V0.0.1 interface change that would invalidate the accepted baseline premise;
- scope cannot be bounded between V0.0.2 and a later release;
- a requirement cannot be stated in a verifiable and implementation-independent form;
- a normative item cannot be assigned an unambiguous owning domain and document.

## Continuation rule

A new ChatGPT session or Codex task must read this file first. Work may continue only from the
current process and gate recorded here. Chat history is supplementary context, not process authority.
