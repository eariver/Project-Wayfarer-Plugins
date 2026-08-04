# V0.0.2 SWE.1 Software Requirements Analysis Plan

Document ID: `SWE1-PLAN-001`  
Revision: A  
State: `DRAFT`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Applicable Product: Plugin V0.0.2 redesign

## 1. Objective

Produce an approved, implementation-independent, testable, and traceable Software Requirements
Specification for Plugin V0.0.2 while treating V0.0.1 as the accepted baseline.

## 2. Analysis principles

- Derive requirements from registered authority, not from PR #14 implementation behavior.
- Separate stakeholder intent, software requirements, architecture constraints, detailed design, and
  verification procedures.
- Preserve exact Project terminology where a source defines a term or boundary.
- Convert vague expectations into measurable requirements only when the source or Owner decision
  supports the measurement.
- Record uncertainty instead of inventing behavior.
- Identify requirements that apply to V0.0.2 separately from later V0.0.x or Project V0.1.0 work.
- Treat V0.0.1 public interfaces, immutable migrations, and released behavior as compatibility inputs.
- Use prior V0.0.2 defects to improve completeness and verification intent, not to create requirements
  retroactively.

## 3. Planned work products

| Document | Purpose |
|---|---|
| `REQUIREMENT_SOURCES.md` | Source provenance, authority, versions, and applicability |
| `SCOPE_AND_NON_SCOPE.md` | Exact V0.0.2 boundaries and deferred items |
| `GLOSSARY.md` | Controlled terminology and state definitions |
| `STAKEHOLDER_REQUIREMENTS_ANALYSIS.md` | Source statements and derived software relevance |
| `SOFTWARE_REQUIREMENTS_SPECIFICATION.md` | Approved SWR requirements |
| `INTERFACE_AND_COMPATIBILITY_REQUIREMENTS.md` | V0.0.1, Project Runtime, and third-party integration constraints |
| `QUALITY_AND_FAILURE_REQUIREMENTS.md` | Threading, performance, safety, security, persistence, recovery, observability |
| `OPEN_QUESTIONS_AND_CONFLICTS.md` | Owner decisions and blockers |
| `VERIFICATION_INTENT.md` | Required verification level and observable result per SWR |
| `../07-traceability/SWE1_REQUIREMENTS_TRACEABILITY.md` | Source-to-requirement bidirectional traceability |
| `../10-reviews-and-evidence/SWE1_REQUIREMENTS_REVIEW.md` | Independent review record and G1 recommendation |

The final document split may be consolidated when that improves readability, but all listed content
must remain present and traceable.

## 4. Requirement quality criteria

Each software requirement must be:

- uniquely identified;
- necessary for the approved scope;
- traceable to one or more sources or Owner decisions;
- stated as required software behavior or constraint;
- singular enough to verify;
- unambiguous within the glossary;
- feasible under known platform and baseline constraints;
- free of unapproved class, method, or algorithm prescription;
- assigned a verification intent;
- assigned a priority and release applicability;
- linked to conflicts, assumptions, or dependencies where applicable.

## 5. Requirement attributes

Each `SWR-*` entry will include:

```text
ID
Title
Statement
Rationale
Source(s)
Applicability
Priority
Preconditions / triggering condition
Required observable result
Failure or denial result where applicable
Verification intent
Dependencies
Assumptions
Open decision / conflict reference
Status
```

## 6. Analysis sequence

### Step 1 — Source baseline

- verify current Project and Plugin source versions;
- retrieve machine-readable locks;
- inventory V0.0.1 public contracts and migrations;
- identify superseded or conflicting sources;
- freeze the SWE.1 source baseline by immutable SHA.

### Step 2 — Scope decomposition

Decompose candidate V0.0.2 scope into:

- common repository and release constraints;
- Wayfarer_Core extensions;
- Wayfarer_Main;
- Wayfarer_Frontier shared behavior;
- Worlds Beyond behavior;
- external integrations;
- cross-cutting quality requirements;
- explicit non-scope and later-release items.

No function is included solely because PR #14 implemented it.

### Step 3 — Requirement derivation

For each source statement:

1. determine authority and current applicability;
2. distinguish stakeholder intent from software-level behavior;
3. derive one or more verifiable software requirements;
4. identify required interfaces and baseline dependencies;
5. record ambiguity, conflict, or missing product decision;
6. assign verification intent without writing the detailed test procedure.

### Step 4 — Consistency and feasibility analysis

Check:

- internal requirement conflicts;
- Main/Frontier/Core ownership overlap;
- conflict with MVI, Waymark, Paper, or third-party authority;
- V0.0.1 compatibility impact;
- threading and persistence feasibility;
- lifecycle and restart consistency;
- permission and failure semantics;
- scope creep into later Project orders;
- requirements that cannot be qualified objectively.

### Step 5 — Requirements review

Present to the Owner:

- proposed scope;
- all unresolved gameplay decisions;
- baseline changes, if any;
- requirements with high implementation or operational risk;
- requirements changed or rejected relative to the prior V0.0.2 document;
- complete source-to-requirement traceability;
- G1 verdict recommendation.

SWE.2 begins only after Owner approval and `STATUS.md` records G1 PASS.

## 7. Verification-intent levels

SWE.1 assigns the lowest sufficient verification level:

```text
ANALYSIS
SWE.4_UNIT_VERIFICATION
SWE.5_INTEGRATION_TEST
SWE.6_QUALIFICATION_TEST
INSPECTION
```

Multiple levels may apply when a requirement contains both internal correctness and externally
observable behavior. Runtime or Client qualification is not required when lower-level evidence is
sufficient, unless a source explicitly requires operational proof.

## 8. Known initial risk areas

The following areas require explicit requirements rather than reliance on implementation convention:

- Player event entry conditions and cancellation semantics;
- use-permission enforcement on every gameplay route;
- logical authority versus physical ItemStack identity;
- progress and vanilla durability ordering;
- evolution, Broken conversion, and repair boundaries;
- inventory movement, death, reconnect, and stale Epoch behavior;
- asynchronous persistence without Main-thread I/O;
- disable/restart stale callback prevention;
- transaction idempotency and unknown outcomes;
- MVI ownership and Theme item isolation;
- exact World and Portal fail-closed behavior;
- external Plugin version and capability boundaries;
- audit and sanitized output.

This list is an analysis checklist, not an approved scope or design.

## 9. SWE.1 completion criteria

SWE.1 is ready for G1 review when:

- all applicable authoritative sources are registered and immutable;
- scope and non-scope are explicit;
- all requirements meet the quality criteria or carry a documented exception;
- every requirement has source traceability and verification intent;
- all source statements in scope are dispositioned;
- conflicts and open decisions are resolved or remain explicit blockers;
- V0.0.1 dependencies are identified;
- no implementation or detailed-design assumption is masquerading as a requirement;
- the Owner can approve the baseline without consulting chat history.
