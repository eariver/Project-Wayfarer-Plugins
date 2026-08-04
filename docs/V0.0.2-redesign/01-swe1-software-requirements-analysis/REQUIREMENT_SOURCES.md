# V0.0.2 Requirement Source Register

Document ID: `SWE1-SRC-001`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Baseline premise: Plugin V0.0.1 accepted

## 1. Purpose

Identify the controlled inputs used for the current V0.0.2 SWE.1 analysis and distinguish product
authority from implementation evidence, planning material, and later consistency sources.

## 2. Authority classes

| Class | Meaning |
|---|---|
| A | Explicit Owner decision controlling current product intent or analysis method |
| B | Owner-provided mainline product requirement source |
| C | Accepted V0.0.1 public contract or immutable baseline constraint |
| D | Project/Concept consistency input not used to silently add requirements in this analysis |
| E | Prior implementation, test, roadmap, or delta material that is reference-only except for an explicitly selected Owner-amendment subset |
| DERIVED | Controlled document produced by applying registered sources under an approved method |

Conflict priority:

1. committed explicit Owner decision;
2. current approved Project source of truth or runtime lock;
3. Owner-provided mainline requirement source;
4. accepted V0.0.1 public contract and immutable migration;
5. Concept/consistency input;
6. prior implementation/test/roadmap evidence;
7. engineering preference.

## 3. Controlling source set for this SWE.1 decomposition

### SRC-OWNER-001 — Canonical merge and SWE.1 execution instruction

```text
Authority class:
  A

Decision record:
  docs/V0.0.2-redesign/08-decisions/
  DEC-REQ-001-canonical-source-merge-and-swe1-analysis.md

Owner instruction date:
  2026-08-05 JST
```

Controls:

- use the mainline requirement document as the base;
- use the delta register only for later Owner clarifications and contradiction resolutions;
- ignore prior implementation status and roadmap material;
- create one canonical positive-requirement source;
- decompose it to SWE.1;
- record ambiguities and conflicts;
- self-review all created SWE.1 work products before Owner review.

### SRC-MAINLINE-001 — Mainline Main/Frontier requirement source

```text
Uploaded filename:
  Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED(1).md

Logical source name:
  Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED.md

Input manifest:
  docs/V0.0.2-redesign/01-swe1-software-requirements-analysis/source-snapshots/
  SOURCE_INPUT_MANIFEST.md

SHA-256:
  2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F

Authority class:
  B
```

Use:

- base product behavior, compatibility, authority, lifecycle, Main, Frontier, Worlds Beyond, and
  non-scope intent;
- source algorithm or procedure wording is translated into implementation-independent SWE.1
  obligations where possible;
- test procedures, release workflow, roadmap sequencing, and handoff lists are not automatically
  software requirements.

### SRC-DELTA-001 — Requirement/implementation delta register

```text
Uploaded filename:
  Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register(1).md

Logical source name:
  Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register.md

Input manifest:
  docs/V0.0.2-redesign/01-swe1-software-requirements-analysis/source-snapshots/
  SOURCE_INPUT_MANIFEST.md

SHA-256:
  A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1

Authority class:
  E generally
  A only for the Owner-amendment/clarification subset selected by SRC-OWNER-001
```

Applied subset:

- AMD-001 through AMD-011 as dispositioned by `SWE1-SRC-002` §3;
- safe-entry delivery-outcome notification detail where it clarifies the base pending-delivery
  requirement.

Explicitly excluded:

- current implementation and test status;
- PR/branch/commit/CI/candidate identity;
- `CURRENT_DONE`, `NEXT_CODEX`, `CLIENT_TEST`, and similar status assertions;
- prior code work list and previous implementation sequence;
- prior release, candidate, or Project roadmap;
- future-version scheduling;
- AMD-012.

### SRC-CANON-001 — Canonical positive-requirement source

```text
Document ID:
  SWE1-SRC-002

Path:
  docs/V0.0.2-redesign/01-swe1-software-requirements-analysis/
  SWE1-SRC-002-canonical-mainline-requirements.md

SHA-256:
  A04C1DBA6FE0D9568C51CE2D2F7FE591F0598C3B92A1EDD4B47AFF779F9A9121

Authority class:
  DERIVED

State:
  DRAFT_FOR_OWNER_REVIEW
```

This is the sole direct source document used by the decomposed SWE.1 target requirements. It preserves
provenance to `SRC-MAINLINE-001`, `SRC-DELTA-001`, and `SRC-OWNER-001`.

## 4. Accepted V0.0.1 baseline sources

### SRC-PLUG-001 — V0.0.1 Work Order and Design Specification

```text
Repository:
  eariver/Project-Wayfarer-Plugins

Path:
  docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md

Observed main blob SHA:
  92fbd7c8a0e657862128bf371a357734f01e9a9f

Authority class:
  C
```

Use in this analysis is limited to the accepted-baseline premise already represented in the
canonical source. A full V0.0.1 public-interface and migration inventory remains required before G1
PASS; no internal V0.0.1 implementation behavior was imported as a requirement.

### SRC-PLUG-002 — Published V0.0.1 contracts and migrations

```text
Scope:
  docs/contracts/
  docs/architecture/
  docs/adr/
  docs/handoff/V0.0.1/
  libraries/wayfarer-api/
  immutable V0.0.1 migrations

Authority class:
  C

Status:
  PENDING_INVENTORY_AND_INTERFACE_REVIEW
```

## 5. Registered Project consistency inputs

The following remain registered for later consistency and feasibility review. Their content was not
used to silently add or change the 164 requirements produced in this execution.

| Source ID | Repository path | Observed blob SHA | Class |
|---|---|---|---|
| SRC-PROJ-001 | `docs/09-roadmap.md` | `8481727c6b2e339785f3f652c00048fa4443bb52` | D |
| SRC-PROJ-002 | `docs/06-acceptance-tests.md` | `d838eedfb0f3411746a9b99a2f0c63943fa76efd` | D |
| SRC-PROJ-003 | `docs/12-permission-model.md` | `a486c0cfc625c0f420854fbc36aa25f0079d5376` | D/A by controlled field |
| SRC-PROJ-004 | `docs/10-waymark-economy.md` | `b25b462a7056feb4113066603d50f8a3035d6b2e` | D/A by controlled field |
| SRC-PROJ-005 | `docs/14-frontier-v0.1.0-scope.md` | `8cb28384fa0c8a65f665ba65fda3a8b64e96ac9d` | D/A by controlled field |
| SRC-PROJ-006 | `docs/15-frontier-runtime-lock.md` | `e78abf0f5cc4efc2ba64ac2397c6e109c956e2ec` | D/A by controlled field |

Machine-readable Frontier locks, `versions.yml`, `plugin-manifest.yml`, and current Concept blobs
remain pending content/hash review before G1.

## 6. Prior implementation reference

### SRC-REF-001 — Frozen PR #14 and Candidate evidence

```text
Repository:
  eariver/Project-Wayfarer-Plugins

PR:
  #14

Frozen branch:
  feature/V0.0.2-main-frontier

Authority class:
  E
```

Permitted use:

- identify prior defects and verification omissions;
- assess implementation reuse only after SWE.3;
- derive risk and review checklists.

Prohibited use:

- infer product intent from code;
- treat a passing test or current behavior as a requirement;
- import the old implementation roadmap into the redesigned lifecycle.

## 7. Source actions remaining before G1

- complete V0.0.1 public API, contract, and immutable migration inventory;
- retrieve and hash the applicable machine-readable Frontier locks;
- inspect current Project manifests for compatibility/placement conflicts;
- run a consistency review of the draft SWE.1 baseline against registered Project sources without
  adding behavior silently;
- resolve or accept all issues in `SWE1-ISSUE-001`;
- obtain Owner approval of `SWE1-SRC-002` and the decomposed target requirements.
