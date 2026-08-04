# SWE.1 Complete Package Self-review

Document ID: `REV-SWE1-001`  
Revision: A  
State: `COMPLETE_AWAITING_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Reviewer: ChatGPT  
Review type: Independent author self-review before joint Owner review  
Reviewed branch: `redesign/V0.0.2-swe1-3`  
Applicable Product: Plugin V0.0.2 redesign

## 1. Review objective

Review every SWE.1 work product produced from the two Owner-provided source files before presenting the
package for joint Owner/ChatGPT review.

The review checks:

- source-merge fidelity;
- exclusion of implementation status and prior roadmap;
- target-domain decomposition;
- requirement singularity, clarity, feasibility, and observability;
- identifier correctness;
- issue capture;
- verification intent;
- bidirectional traceability;
- phase-boundary discipline;
- consistency across all documents.

## 2. Reviewed work products

| Document | Result |
|---|---|
| `SWE1-SRC-001` | PASS as draft source register |
| `SWE1-SRC-002` | PASS as draft canonical positive-requirement source |
| `SWE1-PLAN-001` | PASS as executed analysis record |
| `SWE1-INDEX-001` | PASS |
| `SWE1-SCOPE-001` | PASS |
| `SWE1-GLOSSARY-001` | PASS |
| `SWE1-COMMON-001` | PASS as draft; no open item directly owned |
| `SWE1-CORE-001` | PASS as draft |
| `SWE1-MAIN-001` | PASS as draft; cites external-repair issue |
| `SWE1-MAIN-002` | PASS as draft; cites checkpoint-bound issue |
| `SWE1-MAIN-003` | PASS as draft; cites reissue-context and permission-allocation issues |
| `SWE1-FRONTIER-001` | PASS as draft; cites missing-world and permission issues |
| `SWE1-WB-001` | PASS as draft; cites LeafGrapple issue |
| `SWE1-WB-002` | PASS as draft; cites launchpad identity, protection, return-path, and permission issues |
| `SWE1-ISSUE-001` | PASS; nine open issues are explicit |
| `SWE1-VERIFY-001` | PASS; all requirements allocated |
| `TRC-SWE1-001` | PASS; no unexplained clause or untraced requirement |

`PASS as draft` means suitable for Owner review, not approved baseline status.

## 3. Source merge review

### 3.1 Base-source fidelity

The mainline requirement file remains the base source. Product behavior retained includes:

- Core compatibility and Waymark semantics;
- deployment, dependency, authority, threading, lifecycle, migration, and audit boundaries;
- complete Main Growth Tool vertical behavior;
- Frontier/MVI boundary;
- Worlds Beyond loadout, traversal integration, Launchpad, shop, and non-scope.

### 3.2 Delta-register restriction

Only requirement clarification or contradiction-resolution content was applied.

Applied:

- AMD-001 through AMD-011 according to `SWE1-SRC-002` §3;
- safe-entry delivery-result notification detail that directly clarifies the original pending-delivery
  requirement.

Excluded:

- implementation state and code anchors;
- CI/headless/client-test claims;
- prior Codex work lists;
- previous implementation and release roadmap;
- future-version scheduling;
- AMD-012;
- implementation-derived behavior that was not an Owner requirement.

Result: PASS.

### 3.3 Canonical source integrity

```text
Mainline source SHA-256:
  2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F

Delta register SHA-256:
  A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1

Canonical source SHA-256:
  A04C1DBA6FE0D9568C51CE2D2F7FE591F0598C3B92A1EDD4B47AFF779F9A9121
```

The canonical source contains 59 unique `CAN-*` source clauses. Source clauses are provenance anchors,
not decomposed requirements.

Result: PASS.

## 4. Automated consistency review

Automated checks executed against all generated target requirement records and Markdown files:

| Check | Result |
|---|---|
| Requirement ID uniqueness | PASS — 164 unique IDs |
| Version token in requirement IDs | PASS — none |
| Allowed target documents | PASS |
| Allowed semantic item types | PASS — `CAP`, `CON`, `IFC`, `QLT` only |
| Legacy/redundant types `REQ`, `ARC`, `UV`, `IV`, `QV` | PASS — none |
| Required requirement attributes populated | PASS |
| Canonical source reference on every requirement | PASS — 164/164 |
| Referenced canonical clause exists | PASS |
| Referenced issue exists | PASS |
| Requirement appears in owning document | PASS |
| Requirement appears in verification allocation | PASS — 164/164 |
| Requirement appears in reverse traceability | PASS — 164/164 |
| Canonical source-clause forward disposition | PASS — 59/59 |
| Unexplained canonical source clauses | PASS — 0 |
| Implementation-status token in normative statements | PASS — none |
| Duplicate requirement IDs | PASS — none |
| Compound-statement heuristic after correction | PASS — no remaining warning |

Requirement totals:

```text
CAP: 64
CON: 57
IFC: 10
QLT: 33
TOTAL: 164
```

## 5. Manual requirement-quality review

### 5.1 Target ownership

SWE.1 remains target-oriented:

- `COMMON`
- `CORE`
- `MAIN`
- `FRONTIER`
- `WB`

Permission, inventory, transaction, durability, and lifecycle are not incorrectly promoted to
SWE.1 domains. Those become concern domains only in later processes.

Result: PASS.

### 5.2 Phase-boundary discipline

The following source mechanisms were not copied as premature detailed design:

- non-recursive threshold loop and binary search;
- exact Paper listener class, priority, and `ignoreCancelled`;
- class/package layout;
- SQL/repository implementation;
- cache structure;
- command-tree implementation;
- GUI slot layout;
- executable test steps.

They were translated into observable, bounded, or interface-level SWE.1 obligations. Fixed external
interfaces and approved exact identifiers remain explicit.

Result: PASS.

### 5.3 Corrections made during self-review

The first decomposition draft was corrected before this review was closed:

1. Added explicit normal authorized durability consumption
   (`SWE1-MAIN-002-CAP-016`) so ordinary durability cannot be silently suppressed.
2. Restored the full MVI group-set context and exact `frontier_iris → worlds_beyond` ownership.
3. Added the explicit Launchpad expiration timestamp basis.
4. Added forward-extensible Frontier persistence without activating incomplete Waystone authority.
5. Split four potentially compound requirements:
   - dependency direction;
   - Main pending-delivery behavior;
   - clear repair compensation versus ambiguous outcome;
   - Launchpad expiration basis versus scheduler/thread/restart behavior.
6. Corrected one terminology typo.
7. Confirmed that exact presentation choices are not functional acceptance obligations while required
   information/actions remain observable.

Result: PASS after correction.

### 5.4 Source coverage

```text
Canonical clauses allocated to Product requirements:
  57

Canonical clauses explicitly dispositioned to scope/governance:
  2

Unexplained canonical clauses:
  0

Requirements without source provenance:
  0
```

`CAN-COM-010` is governed by `GOV-ENG-001`; it is not duplicated as a Product requirement.
`CAN-SCOPE-003` is a scope disposition prohibiting an unapproved adapter.

Result: PASS.

## 6. Open issues and blockers

Nine issues remain open:

1. Missing `frontier_iris` enablement behavior.
2. Supported external Growth Tool repair boundary.
3. LeafGrapple 1.0.2 API and safe configuration.
4. WorldEdit/FAWE protection support boundary.
5. Launchpad physical-material identity across configuration changes.
6. Authoritative return path under portal denial.
7. Player-paid reissue invocation context.
8. Complete command-to-permission group allocation.
9. Maximum normal-progress loss window.

These are not concealed as implementation choices. They are carried by full issue IDs and linked from
affected requirements.

Before SWE.2, the project must also finalize the previously discussed process-specific allocation
model for:

- owning software unit and repository module in SWE.2–SWE.4;
- runtime target/activation context;
- integration topology in SWE.5.

That governance detail does not change the current target-oriented SWE.1 requirement IDs, but it must
be approved before the SWE.2 index and architecture documents are baselined.

## 7. Baseline and external consistency work still required

The self-review does not claim completion of:

- full V0.0.1 public API/contract/migration inventory;
- machine-readable Frontier lock review;
- current Project manifest and Concept consistency review;
- feasibility proof for external-plugin issues;
- Owner approval of the canonical amendment selection;
- G1 approval.

No requirement was silently changed using those unreviewed sources.

## 8. Self-review verdict

```text
DOCUMENT COMPLETENESS:
  PASS FOR OWNER REVIEW

SOURCE MERGE:
  PASS

IDENTIFIER / DOMAIN MODEL:
  PASS

REQUIREMENT QUALITY:
  PASS AFTER RECORDED CORRECTIONS

SOURCE TRACEABILITY:
  PASS

VERIFICATION INTENT:
  PASS

OPEN ISSUES:
  9 / EXPLICIT

G1 REQUIREMENTS BASELINE:
  NOT READY

RECOMMENDED NEXT STATE:
  JOINT OWNER / CHATGPT REVIEW
```

The package is suitable for the requested joint review. SWE.2, implementation, candidate creation, and
verification execution remain unauthorized.
