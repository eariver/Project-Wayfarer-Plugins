# Project Wayfarer Plugin V0.0.2 Redesign

This directory is the authoritative entry point for the V0.0.2 redesign.

## Development premise

- Plugin V0.0.1 is treated as the accepted baseline for this redesign.
- V0.0.1 redesign is outside the current scope.
- When SWE.1 analysis identifies a V0.0.1 defect or an interface that prevents a valid V0.0.2
  design, record it as a baseline dependency issue. Do not silently redesign V0.0.1.
- The failed or incomplete V0.0.2 implementation on PR #14 is retained as evidence and an analysis
  input. It is not a design authority and is not the implementation baseline for the redesign.

## Process model

The redesign follows a tailored V-model aligned to SWE.1 through SWE.6.

| Process | Project activity | Primary executor | Independent review / approval |
|---|---|---|---|
| SWE.1 | Software Requirements Analysis | ChatGPT | Owner |
| SWE.2 | Software Architectural Design | ChatGPT | Owner |
| SWE.3 design | Software Detailed Design | ChatGPT | Owner |
| SWE.3 construction | Unit construction from approved detailed design; implementation/design consistency record | Codex | ChatGPT, then Owner gate where required |
| SWE.4 | Software Unit Verification | Codex | ChatGPT |
| SWE.5 | Software Integration and Integration Test | Codex | ChatGPT |
| SWE.6 | Software Qualification Test | Codex | ChatGPT, then Owner acceptance |

This is a Project Wayfarer process tailoring. The repository does not claim formal Automotive SPICE
assessment or certification solely from using these process names.

## Reading order

1. `STATUS.md`
2. `00-governance/REDESIGN_CHARTER.md`
3. `00-governance/V_MODEL_PROCESS_AND_ROLES.md`
4. `00-governance/DOCUMENT_CONTROL_AND_GATES.md`
5. the active phase directory named by `STATUS.md`
6. the current approved work order, when implementation or verification is authorized

## Directory model

```text
docs/V0.0.2-redesign/
├─ README.md
├─ STATUS.md
├─ 00-governance/
├─ 01-swe1-software-requirements-analysis/
├─ 02-swe2-software-architectural-design/
├─ 03-swe3-software-detailed-design/
├─ 04-swe4-software-unit-verification/
├─ 05-swe5-software-integration-test/
├─ 06-swe6-software-qualification-test/
├─ 07-traceability/
├─ 08-decisions/
├─ 09-work-orders/
└─ 10-reviews-and-evidence/
```

Directories are created as their first controlled artifact is approved or prepared. Empty directory
placeholders are not authority.

## Authority rule

A conversation, mutable branch URL, local file, or uncommitted runtime observation is not sufficient
project authority. Requirements, designs, decisions, work orders, reviews, and evidence summaries
needed to continue the work must be committed to this repository and referenced by immutable commit
SHA or release tag.

## Current activity

SWE.1 Software Requirements Analysis is active. No V0.0.2 Product implementation, Candidate-9
remediation, runtime acceptance continuation, PR readiness transition, merge, tag, or release is
authorized by this redesign directory until the corresponding gates are approved.
