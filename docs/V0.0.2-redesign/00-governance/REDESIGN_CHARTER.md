# V0.0.2 Redesign Charter

Status: `PREPARED_FOR_OWNER_CONFIRMATION`  
Date: 2026-08-05 JST

## 1. Purpose

Redesign Plugin V0.0.2 from requirements through qualification using a controlled V-model so that
repository work products, not chat history or inferred implementation behavior, are sufficient to
continue, review, and audit the work.

## 2. Baseline premise

Plugin V0.0.1 is treated as complete and accepted for the purpose of V0.0.2 requirements analysis.
The redesign must preserve V0.0.1 public contracts, migrations, release artifacts, and established
runtime boundaries unless SWE.1 identifies a blocking baseline dependency issue.

A baseline dependency issue does not automatically authorize V0.0.1 redesign. It must be documented
with impact, alternatives, and an explicit Owner decision.

## 3. Scope

The redesign covers the V0.0.2 software scope that is approved through SWE.1, including the required
extensions to Wayfarer_Core and the Main and Frontier plugin behavior selected for V0.0.2.

The exact functional scope is not inherited automatically from the current PR #14 implementation.
It is re-established from authoritative Project and Plugin sources during SWE.1.

## 4. Existing V0.0.2 implementation

PR #14, Candidate-1 through Candidate-8, their tests, defects, and evidence are retained as:

- lessons learned;
- candidate requirement and risk inputs;
- reusable implementation candidates subject to design conformance review;
- negative evidence showing where prior requirements, design, or verification were insufficient.

They are not requirements authority, architectural authority, or proof that a behavior belongs in the
redesigned V0.0.2.

No Candidate-9 remediation or continuation of Candidate-8 formal acceptance is authorized while the
redesign remains before the applicable implementation and verification gates.

## 5. Roles

### Project Owner

The Owner decides product intent, scope, unresolved gameplay behavior, risk acceptance, gate approval,
and release authorization.

### ChatGPT

ChatGPT authors and maintains:

- SWE.1 Software Requirements Analysis;
- SWE.2 Software Architectural Design;
- the detailed-design portion of SWE.3;
- cross-process traceability and design verification intent;
- Codex work orders;
- independent reviews of implementation/design consistency and SWE.4 through SWE.6 evidence;
- gate recommendations to the Owner.

### Codex

Codex performs only repository changes and executions authorized by an approved work order:

- unit construction from approved SWE.3 detailed design;
- tracked implementation-to-detailed-design consistency reporting;
- SWE.4 Software Unit Verification;
- SWE.5 Software Integration and Integration Test;
- SWE.6 Software Qualification Test;
- evidence collection and factual execution reporting.

Codex does not own product interpretation, architecture, detailed behavior, acceptance criteria, or
unapproved test expansion.

## 6. Design-first rule

No production implementation begins before approval of:

1. the relevant SWE.1 requirements baseline;
2. the relevant SWE.2 architecture baseline;
3. the relevant SWE.3 detailed-design baseline;
4. the implementation work order and required consistency-record format.

A discovered design gap is reported as `DESIGN_BLOCKED`; it is resolved by an updated tracked design,
not by Codex inference.

## 7. Repository authority

The repository must contain all information needed to continue the work:

- current phase and gate;
- source provenance;
- requirements and open questions;
- architecture and detailed design;
- decisions and deviations;
- implementation work orders;
- implementation/design consistency records;
- verification specifications and evidence summaries;
- independent reviews;
- release-readiness state.

Local raw evidence may remain untracked when it contains secrets, logs, binaries, worlds, databases,
or personal identifiers, but the repository must contain a sanitized evidence index and conclusion
referenced to immutable Product and environment identities.

## 8. Quality principles

- Requirements are implementation-independent, uniquely identified, testable, and traceable.
- Architecture allocates every approved requirement or explicitly records why allocation is not
  applicable.
- Detailed design specifies behavior at a level where Codex does not need to invent semantics.
- Java standard APIs, Paper/Spigot/Bukkit APIs, adopted plugin boundaries, and acceptable external
  libraries are evaluated before Project-owned implementation is designed.
- Functionality adequately supplied by an approved platform API or external library is not
  reimplemented without a controlled justification.
- External dependencies must have an acceptable maintenance profile, compatibility, license,
  packaging, and failure-boundary assessment.
- Designs that rely on platform or library behavior link the authoritative official references used
  to validate that behavior, with particular attention to SWE.3 event, lifecycle, threading, item,
  inventory, persistence, and cancellation semantics.
- Implementation changes are traceable to detailed-design identifiers.
- Verification is derived from requirements and design, not from code structure alone.
- Runtime and Client tests cover only behavior that cannot be sufficiently established at lower test
  levels.
- A passed test count does not substitute for requirements coverage or design conformance.
- Deviations, limitations, and unresolved decisions are explicit.

The controlling cross-plugin engineering policy is:

`00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md`

## 9. Completion condition

The redesign is complete only when:

- SWE.1 through SWE.6 gates are approved;
- bidirectional traceability has no unexplained gap for the release scope;
- implementation/design consistency is independently reviewed;
- qualification evidence supports all applicable software requirements;
- known limitations and open decisions are accepted by the Owner;
- release readiness is approved separately from test execution.
