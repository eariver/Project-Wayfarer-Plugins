# V0.0.2 Requirement Source Register

Document ID: `SWE1-SRC-001`  
Revision: G  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Baseline premise: Plugin V0.0.1 accepted

## 1. Purpose

Identify the controlled inputs used for the current V0.0.2 SWE.1 analysis and distinguish Product authority from implementation evidence, planning material, review decisions, and later consistency sources.

## 2. Authority classes

| Class | Meaning |
|---|---|
| A | Explicit Owner decision controlling current Product intent or analysis/review method |
| B | Owner-provided mainline Product requirement source |
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

Within the current SWE.1 review, `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-005`, `DEC-REQ-006`, `DEC-REQ-007`, and `DEC-REQ-008` are explicit Owner decisions controlling the Common/Core/reviewed-Main corrections integrated into `SWE1-SRC-002` Revision F and affected derived requirements.

## 3. Controlling source set for this SWE.1 decomposition and review

### SRC-OWNER-001 — Canonical merge and SWE.1 execution instruction

```text
Authority class:
  A
Decision record:
  DEC-REQ-001 — Canonical source merge and SWE.1 analysis
Owner instruction date:
  2026-08-05 JST
```

Controls use of the mainline requirement document as the base, selective use of explicit Owner amendments/clarifications from the delta register, exclusion of implementation/roadmap status as Product authority, canonicalization, SWE.1 decomposition, issue/conflict capture, and package self-review before Owner baseline approval.

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

Controls the complete reviewed Common section, including topology independence, shared ownership/dependency direction, authority/access boundaries, execution-context safety, lifecycle, protected operations, migration ownership, audit/data minimization, and reuse/ownership non-duplication.

### SRC-OWNER-003 — Joint Owner Core-review corrections

```text
Authority class:
  A
Decision record:
  DEC-REQ-005 — CAN-CORE-001 through CAN-CORE-005
Integration checkpoint:
  2026-08-12 JST
```

Controls accepted V0.0.1 public-contract compatibility rather than implementation freeze, Core public-contract abstraction/type identity, V0.0.2 Core Waymark transaction allocation/provider guarantee boundary, ambiguity containment, Core schema evolution, accepted migration immutability, and superseding redundant historical `SWE1-CORE-001-CON-004`.

### SRC-OWNER-004 — Joint Owner Main-review corrections 001–005

```text
Authority class:
  A
Decision record:
  DEC-REQ-006 — CAN-MAIN-001 through CAN-MAIN-005
Integration checkpoint:
  2026-08-15 JST
```

Controls topology-independent Main capability allocation, logical Growth Tool authority, Minecraft current-durability authority, persistent physical identity resolved against logical authority, distinct entitlement/delivery effects, ordinary possession/storage neutrality, owner-only use, Wayfarer-controlled durability/enchantment modification, V0.0.2 anvil/grindstone prohibition, and required later reissue propagation.

### SRC-OWNER-005 — Joint Owner Main-review corrections 006–010

```text
Authority class:
  A
Decision record:
  DEC-REQ-007 — CAN-MAIN-006 through CAN-MAIN-010
Integration checkpoint:
  2026-08-15 JST
```

Controls ordinary death/item-lifecycle neutrality, configured exact progress-world membership, qualifying/provenance-neutral/repeated player-mined progress, representation-independent progress numeric safety, uniform configurable progress increment, withdrawal of material/ore weighting, and associated historical superseded identifiers.

### SRC-OWNER-006 — Joint Owner Main-review corrections 011–015

```text
Authority class:
  A
Decision record:
  DEC-REQ-008 — CAN-MAIN-011 through CAN-MAIN-015
Integration checkpoint:
  2026-08-16 JST
```

Controls:

- fixed V0.0.2 material sequence with configurable/default material thresholds;
- post-Diamond threshold increments and configurable/default enchantment mapping/caps;
- conceptual/effective branch semantics for `FORTUNE` and `SILK_TOUCH`;
- internally consistent evolution-configuration evaluation and non-destructive reconciliation;
- preservation of Minecraft-authoritative durability ratio during reconciliation material changes;
- progression-triggered full recovery only on actual threshold crossing;
- same-authority terminal `ACTIVE → BROKEN` transition and default `GRAY_DYE` presentation without material-as-authority;
- owner management GUI entry/state semantics without fixed presentation layout or premature permission allocation;
- quote-confirmed configurable full-repair pricing and Core transaction use;
- same-authority successful ACTIVE/BROKEN full repair and Repair-specific partial-state `UNKNOWN` containment;
- new active identifiers `SWE1-MAIN-002-CAP-017`, `SWE1-MAIN-002-QLT-007`, and `SWE1-MAIN-003-CAP-010`;
- superseding duplicate `SWE1-MAIN-003-QLT-002` without identifier reuse;
- provisional active Product requirement count becoming 178.

### SRC-MAINLINE-001 — Mainline Main/Frontier requirement source

```text
Logical source name:
  Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED.md
SHA-256:
  2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F
Authority class:
  B
```

Use: base Product behavior, compatibility, authority, lifecycle, Main, Frontier, Worlds Beyond, and non-scope intent. Source algorithm/procedure wording is translated into implementation-independent SWE.1 obligations where possible.

### SRC-DELTA-001 — Requirement/implementation delta register

```text
Logical source name:
  Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register.md
SHA-256:
  A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1
Authority class:
  E generally
  A only for the explicit Owner amendment/clarification subset selected by SRC-OWNER-001 and later Owner review decisions
```

Applied/controlled subset is AMD-001 through AMD-011 as dispositioned by `SWE1-SRC-002` §3 and later explicit Owner decisions. `AMD-006` is superseded in part by `DEC-REQ-007`; `AMD-009` is superseded by `DEC-REQ-007`. `AMD-012` remains excluded as process/release-stage material.

Implementation/test status, PR/branch/CI state, candidate identity, prior implementation sequence, release roadmap, and future-version scheduling are not Product requirement authority.

### SRC-CANON-001 — Canonical positive-requirement source

```text
Document ID:
  SWE1-SRC-002
Revision:
  F
Path:
  docs/V0.0.2-redesign/01-swe1-software-requirements-analysis/
  SWE1-SRC-002-canonical-mainline-requirements.md
Revision A SHA-256 retained as historical identity:
  A04C1DBA6FE0D9568C51CE2D2F7FE591F0598C3B92A1EDD4B47AFF779F9A9121
Revision F controlled content SHA-256:
  PENDING FINAL SWE.1 CONSOLIDATION / HASH REFRESH BEFORE G1
Authority class:
  DERIVED
State:
  DRAFT_FOR_OWNER_REVIEW
```

`SWE1-SRC-002` remains the primary canonical source for decomposed SWE.1 Product requirements. Requirement `Source` fields may additionally name controlling Owner decisions or engineering-governance evidence where applicable.

## 4. Accepted V0.0.1 baseline sources

### SRC-PLUG-001 — V0.0.1 Work Order and Design Specification

Repository path: `docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md`  
Observed main blob SHA: `92fbd7c8a0e657862128bf371a357734f01e9a9f`  
Authority class: C

Use is limited to the accepted-baseline premise and later controlled inventory of accepted public contracts/migrations. Internal V0.0.1 implementation behavior is not silently imported as a requirement.

### SRC-PLUG-002 — Published V0.0.1 contracts and migrations

Scope includes `docs/contracts/`, `docs/architecture/`, `docs/adr/`, `docs/handoff/V0.0.1/`, `libraries/wayfarer-api/`, and immutable V0.0.1 migrations. Authority class C. Status remains `PENDING_INVENTORY_AND_INTERFACE_REVIEW`.

The complete accepted API/semantic/migration inventory remains required before G1.

## 5. Registered Project consistency inputs

These remain registered for later consistency/feasibility review and are not silently imported as Product behavior:

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

`SRC-REF-001` — frozen PR #14 / branch `feature/V0.0.2-main-frontier` remains class E reference-only evidence. It may identify prior defects/verification omissions or later implementation-reuse candidates, but shall not define Product intent or import the old implementation roadmap.

## 7. Current source-package accounting and actions remaining before G1

Current provisional active Product requirement count after integrated review through `CAN-MAIN-015` is **178**:

```text
CAP: 67
CON: 58
IFC: 14
QLT: 39
```

Before G1:

- finish joint Owner review from `CAN-MAIN-016` through remaining Main, Frontier, Worlds Beyond, and Scope clauses;
- resolve or explicitly accept all open issues;
- complete V0.0.1 public API/documented contract/immutable migration inventory;
- retrieve/hash applicable machine-readable runtime locks and inspect Project manifests;
- reconcile registered Project consistency inputs without silently adding behavior;
- calculate/record the final canonical content SHA-256;
- rerun complete source/identifier/count/verification-intent audit and full SWE.1 self-review;
- obtain explicit G1 Owner approval of the complete package.
