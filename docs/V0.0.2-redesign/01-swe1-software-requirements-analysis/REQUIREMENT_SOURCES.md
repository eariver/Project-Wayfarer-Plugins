# V0.0.2 Requirement Source Register

Document ID: `SWE1-SRC-001`  
Revision: E  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-15 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Baseline premise: Plugin V0.0.1 accepted

## 1. Purpose

Identify the controlled inputs used for the current V0.0.2 SWE.1 analysis and distinguish product authority from implementation evidence, planning material, review decisions, and later consistency sources.

## 2. Authority classes

| Class | Meaning |
|---|---|
| A | Explicit Owner decision controlling current product intent or analysis/review method |
| B | Owner-provided mainline product requirement source |
| C | Accepted V0.0.1 public contract or immutable baseline constraint |
| D | Project/Concept consistency input not used to silently add requirements in this analysis |
| E | Prior implementation, test, roadmap, or delta material that is reference-only except for an explicitly selected Owner-amendment subset |
| DERIVED | Controlled document produced by applying registered sources and approved review decisions under the approved method |

Conflict priority:

1. committed explicit Owner decision;
2. current approved Project source of truth or runtime lock where it controls the applicable field;
3. Owner-provided mainline requirement source;
4. accepted V0.0.1 public contract and immutable migration;
5. Concept/consistency input;
6. prior implementation/test/roadmap evidence;
7. engineering preference.

Within the current SWE.1 review, `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`, and `DEC-REQ-006` are explicit Owner decisions controlling the Common/Core/reviewed-Main corrections integrated into `SWE1-SRC-002` Revision D and affected derived requirements.

## 3. Controlling source set for this SWE.1 decomposition and review

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

### SRC-OWNER-002 — Joint Owner Common-review corrections

```text
Authority class:
  A

Decision records:
  DEC-REQ-002 — CAN-COM-001 through CAN-COM-005
  DEC-REQ-004 — CAN-COM-006 through CAN-COM-010

Integration checkpoint:
  2026-08-11 JST
```

Controls:

- the approved semantics of the complete Common canonical section;
- approved atomic decomposition that produced 176 provisional Product requirements at the Common checkpoint;
- direct propagation into affected Core/Main/Frontier/WB drafts while preserving unreviewed target-clause conflicts for later review;
- reuse/ownership non-duplication as a SWE.1 constraint with concrete engineering selection governed by `GOV-ENG-001`.

### SRC-OWNER-003 — Joint Owner Core-review corrections

```text
Authority class:
  A

Decision record:
  DEC-REQ-005 — CAN-CORE-001 through CAN-CORE-005

Integration checkpoint:
  2026-08-12 JST
```

Controls:

- V0.0.1 compatibility protects the controlled accepted public contract rather than freezing V0.0.1 implementation code;
- Core public-contract abstraction and in-process runtime type-identity requirements;
- V0.0.2 allocation of the compatibility-preserving shared Waymark transaction contract to Core without transferring feature-specific domain authority;
- provider evidence/ambiguity constraints without manufactured provider semantics;
- Core-owned schema-evolution justification and byte-for-byte immutability of controlled accepted V0.0.1 Core migration artifacts;
- superseding redundant `SWE1-CORE-001-CON-004` without weakening the inherited Common/Core `UNKNOWN` and replay obligations;
- provisional active Product requirement count becomes 175.

### SRC-OWNER-004 — Joint Owner Main-review corrections 001–005

```text
Authority class:
  A

Decision record:
  DEC-REQ-006 — CAN-MAIN-001 through CAN-MAIN-005

Integration checkpoint:
  2026-08-15 JST
```

Controls:

- Main capability allocation independent of historical backend naming and capability-scoped lifecycle prerequisites;
- Main ownership of the logical Growth Tool domain with MariaDB as durable logical-state authority and Minecraft physical item state as current durability authority;
- atomic logical lifecycle/delivery/branch state dimensions and removal of persistence-mechanism fields from SWE.1;
- physical-item identity as a persistent machine-readable reference resolved against current logical authority rather than an exact PDC field schema;
- logical entitlement and physical delivery as distinct effects with same-entitlement pending retry and no fallback world-drop delivery;
- owner-bound use without possession/storage binding, permitting ordinary drop/pickup/storage including non-owner possession while preserving logical ownership;
- Wayfarer-exclusive durability restoration/enchantment modification, plus V0.0.2 anvil/grindstone processing prohibition including rename;
- mandatory propagation to the still-unreviewed CAN-MAIN-006 death-drop and CAN-MAIN-016 reissue clauses;
- provisional active Product requirement count becomes 179.

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

- base product behavior, compatibility, authority, lifecycle, Main, Frontier, Worlds Beyond, and non-scope intent;
- source algorithm/procedure wording is translated into implementation-independent SWE.1 obligations where possible;
- test procedures, release workflow, roadmap sequencing, and handoff lists are not automatically software requirements.

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

- AMD-001 through AMD-011 as dispositioned by `SWE1-SRC-002` §3 and later explicit Owner decisions;
- safe-entry delivery-outcome notification detail where it clarifies the base pending-delivery requirement.

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

Revision:
  D

Path:
  docs/V0.0.2-redesign/01-swe1-software-requirements-analysis/
  SWE1-SRC-002-canonical-mainline-requirements.md

Current Git blob SHA at Main-001–005 checkpoint:
  PENDING POST-CHECKPOINT METADATA REFRESH

Revision A SHA-256 retained as historical identity:
  A04C1DBA6FE0D9568C51CE2D2F7FE591F0598C3B92A1EDD4B47AFF779F9A9121

Revision D controlled content SHA-256:
  PENDING FINAL SWE.1 CONSOLIDATION / HASH REFRESH BEFORE G1

Authority class:
  DERIVED

State:
  DRAFT_FOR_OWNER_REVIEW
```

`SWE1-SRC-002` remains the primary canonical source for decomposed SWE.1 Product requirements. Requirement `Source` fields may additionally name the Owner decision that controls a reviewed correction or `GOV-ENG-001` where the requirement explicitly links to engineering-governance evidence; those references do not replace canonical provenance.

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

Use in this analysis is limited to the accepted-baseline premise represented in the canonical source and to later controlled inventory of accepted public contracts/migrations. A full V0.0.1 public-interface and migration inventory remains required before G1 PASS; no internal V0.0.1 implementation behavior is silently imported as a requirement.

Core review explicitly reinforces this boundary: implementation locations may be changed or corrected in V0.0.2, while controlled accepted public-contract compatibility and controlled accepted migration-history artifacts remain protected.

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

Core review observed the existing V0.0.1 public surface including `WayfarerServices.transactions()` and `WayfarerTransactions` as relevant inventory candidates, but the complete accepted API/semantic/migration inventory is still pending and shall not be inferred solely from implementation presence.

## 5. Registered Project consistency inputs

The following remain registered for later consistency and feasibility review. Their content was not used to silently add or change the original 164-requirement decomposition. The current provisional active 179-item count results from explicit Owner-approved Common/Core/Main review decisions, not from silent import of these consistency sources.

| Source ID | Repository path | Observed blob SHA | Class |
|---|---|---|---|
| SRC-PROJ-001 | `docs/09-roadmap.md` | `8481727c6b2e339785f3f652c00048fa4443bb52` | D |
| SRC-PROJ-002 | `docs/06-acceptance-tests.md` | `d838eedfb0f3411746a9b99a2f0c63943fa76efd` | D |
| SRC-PROJ-003 | `docs/12-permission-model.md` | `a486c0cfc625c0f420854fbc36aa25f0079d5376` | D/A by controlled field |
| SRC-PROJ-004 | `docs/10-waymark-economy.md` | `b25b462a7056feb4113066603d50f8a3035d6b2e` | D/A by controlled field |
| SRC-PROJ-005 | `docs/14-frontier-v0.1.0-scope.md` | `8cb28384fa0c8a65f665ba65fda3a8b64e96ac9d` | D/A by controlled field |
| SRC-PROJ-006 | `docs/15-frontier-runtime-lock.md` | `e78abf0f5cc4efc2ba64ac2397c6e109c956e2ec` | D/A by controlled field |

Machine-readable Frontier locks, `versions.yml`, `plugin-manifest.yml`, and current Concept blobs remain pending content/hash review before G1.

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
- assess implementation reuse only after SWE.3 is authorized;
- derive risk and review checklists.

Prohibited use:

- infer product intent from code;
- treat a passing test or current behavior as a requirement;
- import the old implementation roadmap into the redesigned lifecycle.

## 7. Source actions remaining before G1

- complete V0.0.1 public API, documented public-contract semantics, and immutable migration inventory;
- retrieve and hash the applicable machine-readable Frontier locks;
- inspect current Project manifests for compatibility/placement conflicts;
- run a consistency review of the draft SWE.1 baseline against registered Project sources without adding behavior silently;
- resolve or accept all issues in `SWE1-ISSUE-001`;
- finish joint Owner review of remaining Main/Frontier/WB/Scope clauses;
- after final consolidation, calculate/record the controlled Revision D-or-later canonical content SHA-256 and rerun the complete source/identifier/count/verification-intent self-review;
- obtain explicit G1 Owner approval of the complete canonical and decomposed SWE.1 package.