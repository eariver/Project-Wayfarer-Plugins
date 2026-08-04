# V0.0.2 SWE.1 Software Requirements Analysis Plan

Document ID: `SWE1-PLAN-001`  
Revision: B  
State: `DRAFT`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 and later while requirements remain active

## 1. Objective

Produce an approved, implementation-independent, testable, and traceable Software Requirements
baseline for Plugin V0.0.2 while treating V0.0.1 as the accepted baseline.

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
- Use target-oriented SWE.1 domains. Do not pre-allocate requirements into SWE.2/SWE.3 concern
  domains during requirements analysis.
- Keep all controlled document and item IDs independent of Product version.

## 3. SWE.1 target domains

The initial SWE.1 requirement-target domains are:

| Domain | Target |
|---|---|
| `COMMON` | Obligations genuinely shared by multiple Wayfarer targets |
| `CORE` | Wayfarer_Core externally required behavior and constraints |
| `MAIN` | Main server and Wayfarer_Main / Growth Tool behavior |
| `FRONTIER` | Frontier server and Wayfarer_Frontier shared behavior |
| `WB` | Worlds Beyond theme behavior, when confirmed in V0.0.2 scope |
| `RF` | Ruined Frontier integration behavior, when confirmed in V0.0.2 scope |
| `ADAPTER` | A separately approved adapter Product, only when a decision requires one |

Support documents may use process-support domains such as `SOURCE`, `PLAN`, `INDEX`, `SCOPE`,
`GLOSSARY`, `ISSUE`, and `VERIFY`. These documents do not own Product behavior unless they contain an
explicitly identified normative item.

SWE.2 through SWE.5 will define their own concern-oriented domains. Their domain names do not need to
match this table.

## 4. Planned work products

| Document ID | Purpose |
|---|---|
| `SWE1-SRC-001` | Source provenance, authority, versions, and applicability |
| `SWE1-PLAN-001` | SWE.1 method and completion criteria |
| `SWE1-INDEX-001` | SWE.1 target-domain document index |
| `SWE1-SCOPE-001` | Exact V0.0.2 boundaries and deferred items |
| `SWE1-GLOSSARY-001` | Controlled terminology and state definitions |
| target-domain documents | Normative capabilities, constraints, interfaces, and quality obligations |
| `SWE1-ISSUE-001` | Open questions, conflicts, and baseline dependency issues |
| `SWE1-VERIFY-001` | Verification-intent allocation |
| traceability documents under `07-traceability/` | Source-to-requirement and downstream allocation records |
| review documents under `10-reviews-and-evidence/` | G1 review record and recommendation |

The phase document index is authoritative for the final document set. No document reservation alone
adds a feature to V0.0.2 scope.

## 5. Requirement item types

Normative SWE.1 items use semantic types rather than the retired generic `REQ` type:

| Type | Meaning |
|---|---|
| `CAP` | Required capability or observable behavior |
| `CON` | Required constraint or prohibition |
| `IFC` | External or inter-product interface obligation |
| `QLT` | Quality, reliability, security, recovery, performance, or operability obligation |

Example:

```text
SWE1-MAIN-001-CAP-003
SWE1-MAIN-001-CON-004
SWE1-FRONTIER-001-IFC-002
SWE1-COMMON-001-QLT-006
```

The ID does not contain `V002`. Version applicability is an item attribute.

## 6. Requirement quality criteria

Each software requirement must be:

- uniquely identified;
- necessary for the approved scope;
- traceable to one or more sources or Owner decisions;
- stated as required software behavior or constraint;
- singular enough to verify;
- unambiguous within the glossary;
- feasible under known platform and baseline constraints;
- free of unapproved class, method, algorithm, or architecture prescription;
- assigned a verification intent;
- assigned a priority and release applicability;
- linked to conflicts, assumptions, or dependencies where applicable.

## 7. Requirement attributes

Each normative SWE.1 entry includes:

```text
Full item ID
Item type
Title
Normative statement
Rationale
Source(s)
Introduced Product version
Applicable Product version or version range
Priority
Preconditions / triggering condition
Required observable result
Failure or denial result where applicable
Verification intent
Dependencies
Assumptions
Open decision / conflict reference
Lifecycle state
Supersedes / superseded by
```

## 8. Analysis sequence

### Step 1 — Source baseline

- verify current Project and Plugin source versions;
- retrieve machine-readable locks;
- inventory V0.0.1 public contracts and migrations;
- identify superseded or conflicting sources;
- freeze the SWE.1 source baseline by immutable SHA.

### Step 2 — Scope and target decomposition

Decompose candidate V0.0.2 scope by externally meaningful target:

- cross-target common obligations;
- Wayfarer_Core;
- Main / Wayfarer_Main / Growth Tool;
- Frontier shared behavior;
- Worlds Beyond, when in scope;
- Ruined Frontier integration, when in scope;
- a conditional adapter only when separately approved;
- explicit non-scope and later-release items.

Do not separate SWE.1 requirements into implementation concerns such as inventory, commands,
permissions, persistence, or state machines. Those become downstream SWE.2/SWE.3 allocations.

### Step 3 — Requirement derivation

For each source statement:

1. determine authority and current applicability;
2. distinguish stakeholder intent from software-level behavior;
3. assign the owning SWE.1 target domain;
4. derive one or more singular `CAP`, `CON`, `IFC`, or `QLT` items;
5. identify required interfaces and baseline dependencies without designing them;
6. record ambiguity, conflict, or missing Product decision;
7. assign verification intent without writing the detailed test procedure.

### Step 4 — Consistency and feasibility analysis

Check:

- internal requirement conflicts;
- target ownership overlap;
- conflict with MVI, Waymark, Paper, or third-party authority;
- V0.0.1 compatibility impact;
- feasibility of threading, persistence, lifecycle, permission, and failure obligations;
- scope creep into later Project orders;
- requirements that cannot be qualified objectively;
- requirements that improperly prescribe a SWE.2/SWE.3 solution.

### Step 5 — Requirements review

Present to the Owner:

- proposed target-domain scope;
- all unresolved gameplay decisions;
- baseline changes, if any;
- requirements with high implementation or operational risk;
- requirements changed or rejected relative to the prior V0.0.2 document;
- complete source-to-requirement traceability;
- a preview of downstream concern allocations where useful, clearly marked non-normative;
- G1 verdict recommendation.

SWE.2 begins only after Owner approval and `STATUS.md` records G1 PASS.

## 9. Verification-intent levels

SWE.1 assigns the lowest sufficient verification level:

```text
ANALYSIS
SWE.4_UNIT_VERIFICATION
SWE.5_INTEGRATION_TEST
SWE.6_QUALIFICATION_TEST
INSPECTION
```

Multiple levels may apply. SWE.6 qualification documents will again be organized primarily by
Product target, while SWE.4/SWE.5 may use unit and integration concern domains.

## 10. Known initial risk areas

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

This is an analysis checklist, not a domain allocation, approved scope, or design.

## 11. SWE.1 completion criteria

SWE.1 is ready for G1 review when:

- all applicable authoritative sources are registered and immutable;
- scope and non-scope are explicit;
- the SWE.1 target-domain dictionary is approved;
- all normative items use version-independent full IDs and semantic item types;
- all requirements meet the quality criteria or carry a documented exception;
- every requirement has source traceability and verification intent;
- all source statements in scope are dispositioned;
- conflicts and open decisions are resolved or remain explicit blockers;
- V0.0.1 dependencies are identified;
- no architecture or detailed-design assumption is masquerading as a requirement;
- the Owner can approve the baseline without consulting chat history.