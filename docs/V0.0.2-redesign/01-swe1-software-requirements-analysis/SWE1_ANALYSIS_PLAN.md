# V0.0.2 SWE.1 Software Requirements Analysis Plan and Execution Record

Document ID: `SWE1-PLAN-001`  
Revision: C  
State: `EXECUTED_AWAITING_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Baseline premise: Plugin V0.0.1 accepted

## 1. Objective

Create an implementation-independent, testable, target-oriented SWE.1 baseline from the Owner-provided
mainline requirement source and the approved clarification subset of the prior delta register.

## 2. Controlling method

1. Treat the mainline requirement source as the base.
2. Apply only Owner-approved requirement clarifications or contradiction resolutions from the delta
   register.
3. Do not import current implementation, test status, code work lists, or previous roadmap.
4. Produce one canonical positive-requirement source before decomposition.
5. Decompose by SWE.1 target domain rather than by implementation concern.
6. Assign full linked identifiers with semantic item types `CAP`, `CON`, `IFC`, and `QLT`.
7. Record unresolved behavior, feasibility, or measurable quality bounds in `SWE1-ISSUE-001`.
8. Assign verification intent and bidirectional source traceability.
9. Perform a complete self-review before requesting Owner review.

Method authority: `DEC-REQ-001`.

## 3. Work products produced

| Document ID | Purpose |
|---|---|
| `SWE1-SRC-001` | Source authority and provenance register |
| `SWE1-SRC-002` | Canonical positive-requirement source before decomposition |
| `SWE1-PLAN-001` | Analysis method and execution record |
| `SWE1-INDEX-001` | Target-domain work-product index |
| `SWE1-SCOPE-001` | In-scope and non-scope disposition |
| `SWE1-GLOSSARY-001` | Controlled terminology |
| `SWE1-COMMON-001` | Cross-target common requirements |
| `SWE1-CORE-001` | Wayfarer_Core requirements |
| `SWE1-MAIN-001` | Main lifecycle, authority, delivery, and owner-binding requirements |
| `SWE1-MAIN-002` | Main progress, evolution, durability, and checkpoint requirements |
| `SWE1-MAIN-003` | Main GUI, repair, paid reissue, admin, and permission requirements |
| `SWE1-FRONTIER-001` | Frontier boundary, MVI, persistence, and permission requirements |
| `SWE1-WB-001` | Worlds Beyond loadout, permanent item, hook, and navigation requirements |
| `SWE1-WB-002` | Worlds Beyond launchpad, shop, portal, and administration requirements |
| `SWE1-ISSUE-001` | Open questions, conflicts, and feasibility dependencies |
| `SWE1-VERIFY-001` | Verification-intent allocation |
| `TRC-SWE1-001` | Bidirectional canonical-source-to-requirement traceability |
| `REV-SWE1-001` | ChatGPT self-review of the complete SWE.1 package |

No `RF` or `ADAPTER` normative target document is created because those targets are outside or
conditionally prohibited in the current scope.

## 4. Requirement quality criteria applied

Every draft Product requirement was checked for:

- unique, version-independent full identifier;
- one target owner and one semantic item type;
- one assessable obligation;
- explicit source provenance;
- implementation-independent statement where the source allowed it;
- trigger/precondition and observable result;
- verification intent;
- priority, dependencies, assumptions, issue linkage, and state;
- no dependence on PR #14 implementation status;
- no use of phase-redundant item types such as `REQ`, `ARC`, `UV`, `IV`, or `QV`.

## 5. Decomposition result

```text
Draft target requirements:
  164

By target document:
  SWE1-COMMON-001     18
  SWE1-CORE-001       14
  SWE1-MAIN-001       20
  SWE1-MAIN-002       27
  SWE1-MAIN-003       19
  SWE1-FRONTIER-001   14
  SWE1-WB-001         24
  SWE1-WB-002         28

By semantic type:
  CAP  64
  CON  57
  IFC  10
  QLT  33

Open issues:
  9
```

## 6. Phase-boundary decisions

The following source wording was intentionally not copied as a SWE.1 implementation prescription:

- non-recursive threshold generation and binary search were translated to deterministic bounded
  full-range threshold evaluation;
- event class, priority, and cancellation-registration details are deferred to SWE.3 and must cite
  Paper references;
- command tree syntax, class/package structure, SQL implementation, cache structure, and GUI slot
  layout are not SWE.1 design;
- source test lists became verification intent, not executable test procedures;
- release, PR, artifact, and roadmap instructions remain governance/work-order concerns.

External fixed interfaces that are product constraints remain explicit, including exact world names,
PDC identity purpose, LeafGrapple 1.0.2, permission node names, MariaDB/Redis/MVI authority, and
Waymark provider semantics.

## 7. Execution completion

The canonical source, target requirements, scope, glossary, issue register, verification intent, and
traceability were generated and subjected to the automated and manual checks recorded in
`REV-SWE1-001`.

## 8. Current gate disposition

The package is ready for Owner review, not for G1 PASS.

Blocking work before G1 recommendation:

- Owner review of canonical source and amendment selection;
- resolution or explicit blocker acceptance for the nine open issues;
- V0.0.1 public API/migration inventory;
- Project source/runtime-lock consistency review;
- correction of any findings from the joint Owner/ChatGPT review.

SWE.2 and Product implementation remain unauthorized.
