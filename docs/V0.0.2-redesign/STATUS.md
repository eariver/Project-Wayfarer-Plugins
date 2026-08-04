# V0.0.2 Redesign Status

Updated: 2026-08-05 JST  
Branch: `redesign/V0.0.2-swe1-3`  
Draft PR: `#18`  
Baseline: Plugin V0.0.1 on `main`  
Current process: `SWE.1 Software Requirements Analysis`  
Current gate: `SWE1_OWNER_REVIEW_PENDING`

## Executive status

```text
V0.0.1 BASELINE:
  ASSUMED ACCEPTED FOR INITIAL V0.0.2 ANALYSIS

V0.0.1 REDESIGN:
  OUT OF CURRENT SCOPE

PR #14 / LEGACY IMPLEMENTATION:
  FROZEN REFERENCE / NOT A REQUIREMENT OR DESIGN AUTHORITY

CANONICAL SOURCE MERGE:
  COMPLETE / SELF-REVIEWED / OWNER REVIEW PENDING

SWE.1 DECOMPOSITION:
  COMPLETE / 164 DRAFT REQUIREMENTS

SWE.1 OPEN ISSUES:
  9

SWE.1 SELF-REVIEW:
  PASS FOR OWNER REVIEW

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2:
  NOT AUTHORIZED

SWE.3:
  NOT AUTHORIZED

SWE.4 THROUGH SWE.6:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / CANDIDATE / RELEASE:
  NOT AUTHORIZED
```

## Completed requested work

### Step 1 — Canonical positive-requirement source

Completed:

- retained the Owner-provided mainline requirement source as the base;
- applied only the approved clarification/contradiction-resolution subset of the delta register;
- excluded prior implementation state, test state, roadmap, and release sequencing;
- committed raw source snapshots and the normalized canonical source.

Canonical source:

```text
Document:
  SWE1-SRC-002

SHA-256:
  A04C1DBA6FE0D9568C51CE2D2F7FE591F0598C3B92A1EDD4B47AFF779F9A9121
```

### Step 2 — SWE.1 decomposition

Completed target documents:

- `SWE1-COMMON-001`
- `SWE1-CORE-001`
- `SWE1-MAIN-001`
- `SWE1-MAIN-002`
- `SWE1-MAIN-003`
- `SWE1-FRONTIER-001`
- `SWE1-WB-001`
- `SWE1-WB-002`

Totals:

```text
CAP: 64
CON: 57
IFC: 10
QLT: 33
TOTAL: 164
```

Supporting work products:

- scope and non-scope;
- glossary;
- nine-item open-issue register;
- verification-intent allocation;
- source-to-requirement bidirectional traceability.

### Step 3 — Self-review

`REV-SWE1-001` completed.

Automated and manual review found and corrected:

- missing explicit normal durability behavior;
- incomplete MVI group wording;
- missing Launchpad expiration timestamp basis;
- missing Frontier schema extensibility constraint;
- four potentially compound requirements;
- one terminology typo.

Final automated consistency result:

```text
DUPLICATE REQUIREMENT IDS:
  0

UNTRACED REQUIREMENTS:
  0

UNEXPLAINED CANONICAL CLAUSES:
  0

MISSING VERIFICATION INTENT:
  0

LEGACY / VERSION-PREFIXED REQUIREMENT IDS:
  0
```

## Current work-product entry points

1. `README.md`
2. `SWE1-INDEX-001-document-index.md`
3. `SWE1-SRC-002-canonical-mainline-requirements.md`
4. target requirement documents listed in the index
5. `SWE1-ISSUE-001-open-questions.md`
6. `../07-traceability/TRC-SWE1-001-source-requirement-traceability.md`
7. `../10-reviews-and-evidence/REV-SWE1-001-self-review.md`

## Open issues before G1

1. Missing `frontier_iris` enablement behavior.
2. Supported external Growth Tool repair boundary.
3. LeafGrapple 1.0.2 API and safe configuration.
4. WorldEdit/FAWE Launchpad protection boundary.
5. Launchpad material identity across configuration changes.
6. Authoritative return path under portal denial.
7. Player-paid Growth Tool reissue invocation context.
8. Complete command-to-permission group allocation.
9. Maximum normal-progress loss window.

Additional baseline work before G1:

- V0.0.1 public API/contract/migration inventory;
- machine-readable Frontier lock and Project-manifest consistency review;
- Owner review and correction of the full package.

## Immediate next action

Conduct the joint Project Owner / ChatGPT review requested by the Owner.

The review should proceed in this order:

1. canonical source and amendment disposition;
2. scope/non-scope;
3. Common and Core requirements;
4. Main requirements;
5. Frontier and Worlds Beyond requirements;
6. open issues;
7. verification intent and traceability;
8. G1 disposition.

## Continuation rule

A new ChatGPT session or Codex task must read this file and `SWE1-INDEX-001` first. No downstream
design, implementation, build candidate, runtime test, merge, tag, or release is authorized while G1
is not approved.
