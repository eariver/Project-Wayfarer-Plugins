# Project Wayfarer Plugin V0.0.2 Redesign

This directory is the authoritative entry point for the V0.0.2 redesign.

## Development premise

- Plugin V0.0.1 is treated as the accepted baseline for initial analysis.
- V0.0.1 redesign is outside the current scope.
- PR #14 is retained only as defect/risk/reference evidence.
- Product intent is derived from controlled source documents and Owner decisions, not from prior code.

## Process model

The work follows the Project-tailored V-model:

| Process | Primary executor | Review / approval |
|---|---|---|
| SWE.1 Software Requirements Analysis | ChatGPT | Project Owner |
| SWE.2 Software Architectural Design | ChatGPT | Project Owner |
| SWE.3 Detailed Design | ChatGPT | Project Owner |
| SWE.3 Unit Construction and design-consistency record | Codex | ChatGPT |
| SWE.4 Unit Verification | Codex | ChatGPT |
| SWE.5 Integration and Integration Test | Codex | ChatGPT |
| SWE.6 Qualification Test | Codex | ChatGPT and Project Owner |

No downstream process starts before its predecessor gate is approved.

## Current state

SWE.1 source merge, target requirement decomposition, traceability, verification intent, and ChatGPT
self-review are complete. The package is awaiting joint Project Owner / ChatGPT review.

G1 is not approved. SWE.2 and Product implementation are not authorized.

See `STATUS.md` for exact counts, open issues, and the next action.

## Reading order for the current review

1. `STATUS.md`
2. `00-governance/REDESIGN_CHARTER.md`
3. `00-governance/V_MODEL_PROCESS_AND_ROLES.md`
4. `00-governance/DOCUMENT_CONTROL_AND_GATES.md`
5. `00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md`
6. `00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md`
7. `01-swe1-software-requirements-analysis/SWE1-INDEX-001-document-index.md`
8. `01-swe1-software-requirements-analysis/SWE1-SRC-002-canonical-mainline-requirements.md`
9. `01-swe1-software-requirements-analysis/SWE1-SCOPE-001-scope-and-non-scope.md`
10. target requirement documents in index order
11. `01-swe1-software-requirements-analysis/SWE1-ISSUE-001-open-questions.md`
12. `07-traceability/TRC-SWE1-001-source-requirement-traceability.md`
13. `10-reviews-and-evidence/REV-SWE1-001-self-review.md`

## Source authority

The original Owner-provided input files are identified by filename and SHA-256 in
`01-swe1-software-requirements-analysis/source-snapshots/SOURCE_INPUT_MANIFEST.md`.
The normalized repository-controlled source used for decomposition is `SWE1-SRC-002`.

The controlled merge decision is:

`08-decisions/DEC-REQ-001-canonical-source-merge-and-swe1-analysis.md`

The direct source for decomposed SWE.1 requirements is `SWE1-SRC-002`.

## Identifier and domain rules

- Product-version tokens are not placed in requirement/design IDs.
- SWE.1 and SWE.6 use target-oriented domains.
- SWE.2–SWE.5 use process-appropriate concern, unit, flow, or topology domains.
- Cross-process coverage is explicit; matching domain names are never assumed to establish traceability.
- Production code will trace to approved SWE.3 items through Javadoc and focused inline comments as
  governed by `GOV-TRACE-001`.

## Engineering policy

All Wayfarer-owned plugins may use suitable Java, Paper/Spigot/Bukkit, adopted-plugin, and approved
external-library capabilities. Equivalent mature capabilities must not be unnecessarily reimplemented.
SWE.2/SWE.3 designs must cite authoritative version-appropriate references.

See:

`00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md`

## Repository authority rule

Chat history, mutable branch URLs, local files, and uncommitted observations are supplementary only.
Controlled requirements, designs, decisions, work orders, reviews, and evidence needed to continue
the project must be committed and referenced by immutable commit SHA or release tag.
