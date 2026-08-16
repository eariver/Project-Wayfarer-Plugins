# Legacy V0.0.2 Implementation Freeze

Document ID: `GOV-FREEZE-001`  
Revision: A  
State: `APPROVED_BASELINE`  
Date: 2026-08-05 JST  
Authority: Project Owner decision to redesign V0.0.2

## Frozen reference

```text
Repository:
  eariver/Project-Wayfarer-Plugins

Legacy PR:
  #14

Legacy branch:
  feature/V0.0.2-main-frontier

Legacy branch HEAD at redesign start:
  0f06ee9bfeaf54e2ff9cd4af53114662e3988861

Last Product candidate:
  Candidate-8 Product HEAD
  698c387dfe2e86de8e48ea59a80f35f14c728e2a
```

## Status

- Candidate-8 formal Client Acceptance is discontinued for the legacy implementation.
- The prepared pre-remediation defect sweep is superseded by the redesign decision and must not be
  started or continued.
- Candidate-9 is not started.
- The legacy branch is not a design, implementation, test, release, or merge authority.
- The legacy branch and PR remain available as read-only evidence unless the Owner separately
  authorizes archival or closure operations.

## Permitted use

The frozen implementation may be inspected only to:

- identify defects and risk patterns;
- locate missing or ambiguous requirements;
- compare prior behavior against the new approved requirements and design;
- evaluate code for selective reuse after the relevant SWE.3 design is approved;
- ensure SWE.4 through SWE.6 cover previously missed behavior.

## Prohibited use

- do not continue legacy formal acceptance;
- do not patch Candidate-8 in place;
- do not create Candidate-9 from the legacy design;
- do not treat old tests as the new verification specification;
- do not merge PR #14 as the redesigned V0.0.2 Product;
- do not infer requirements from existing implementation behavior;
- do not reuse code before detailed-design conformance is assessed.

## Evidence preservation

Existing tracked reports and local ignored evidence should be preserved under their existing retention
rules. No raw JAR, log, world, database, secret, screenshot set, or personal identifier is added to the
redesign branch. The new requirements analysis may cite immutable legacy commits and sanitized tracked
reports.
