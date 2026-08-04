# V0.0.2 Requirement Source Register

Document ID: `SWE1-SRC-001`  
Revision: A  
State: `DRAFT`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Applicable Product: Plugin V0.0.2 redesign  
Baseline premise: Plugin V0.0.1 accepted

## 1. Purpose

Identify the authoritative inputs from which V0.0.2 software requirements will be derived. Source
registration does not mean that every statement in a source becomes a V0.0.2 software requirement.
Applicability, authority, conflict, implementation independence, and verification feasibility are
analyzed separately.

## 2. Authority classes

| Class | Meaning |
|---|---|
| A | Explicit Owner-approved Project source of truth or approved lock |
| B | Current Project planning or acceptance source controlling release intent |
| C | Accepted V0.0.1 Plugin baseline contract |
| D | Concept or detailed-design input that must not override A through C |
| E | Prior V0.0.2 requirement, implementation, test, or defect evidence; reference only |

When sources conflict, the priority is:

1. explicit Owner decision committed to a repository;
2. current Project source-of-truth documents and approved runtime locks;
3. current Project release scope, roadmap, and acceptance documents;
4. accepted V0.0.1 Plugin public contracts and immutable migrations;
5. current Concept inputs;
6. prior V0.0.2 documents and implementation evidence;
7. engineering preference.

## 3. Registered Project sources

### SRC-PROJ-001 — Project Roadmap

```text
Repository: eariver/Project_Wayfarer
Path: docs/09-roadmap.md
Branch observed: main
Blob SHA: 8481727c6b2e339785f3f652c00048fa4443bb52
Authority class: B
```

Primary relevance:

- dependency order for Core, Main, Frontier, shared Frontier foundation, and later integration;
- V0.1.0 release-blocker relationship;
- separation between completed baseline and planned work;
- prohibition on assuming that a document alone authorizes implementation.

### SRC-PROJ-002 — Project Acceptance Tests

```text
Repository: eariver/Project_Wayfarer
Path: docs/06-acceptance-tests.md
Branch observed: main
Blob SHA: d838eedfb0f3411746a9b99a2f0c63943fa76efd
Authority class: B
```

Primary relevance:

- risk-proportionate verification policy;
- Wayfarer_Main and Frontier release-blocking behavior;
- current stated expectations for delivery, identity, progress, evolution, durability, repair,
  persistence, MVI isolation, Traversal, Launchpad, shop, Waystone, and portal boundaries;
- identification of statements that may need clarification because current acceptance wording can
  contain design assumptions or prior decisions that must be revalidated during SWE.1.

### SRC-PROJ-003 — Permission Model

```text
Repository: eariver/Project_Wayfarer
Path: docs/12-permission-model.md
Branch observed: main
Blob SHA: a486c0cfc625c0f420854fbc36aa25f0079d5376
Authority class: A
```

Primary relevance:

- Project LuckPerms source of truth;
- OP-independent operation;
- General Player, Builder, and Admin authority separation;
- Phase 1A baseline and Phase 1B dependency;
- requirement that V0.0.2 permission nodes be explicit enough to support the later final allowlist.

### SRC-PROJ-004 — Waymark Economy

```text
Repository: eariver/Project_Wayfarer
Path: docs/10-waymark-economy.md
Branch observed: main
Blob SHA: b25b462a7056feb4113066603d50f8a3035d6b2e
Authority class: A
```

Primary relevance:

- shared Waymark balance and non-item transfer role;
- RedisEconomy/Vault integration boundary;
- prohibition on direct RedisEconomy key access;
- Main repair and Frontier shop transaction expectations;
- idempotency, double-charge prevention, refund, reconcile, and audit requirements.

### SRC-PROJ-005 — Frontier V0.1.0 Scope

```text
Repository: eariver/Project_Wayfarer
Path: docs/14-frontier-v0.1.0-scope.md
Branch observed: main
Blob SHA: 8cb28384fa0c8a65f665ba65fda3a8b64e96ac9d
Authority class: A
```

Primary relevance:

- authoritative Frontier release scope;
- MVI ownership of normal Frontier Player State;
- cross-backend and cross-theme item/state isolation;
- Wayfarer_Core and Wayfarer_Frontier responsibilities;
- Worlds Beyond MVP scope and Ruined Frontier boundary;
- prohibition on reimplementing MVI, Gate, EliteMobs, BetterStructures, or unrelated Plugin
  functionality.

### SRC-PROJ-006 — Frontier Runtime Lock

```text
Repository: eariver/Project_Wayfarer
Path: docs/15-frontier-runtime-lock.md
Branch observed: main
Blob SHA: e78abf0f5cc4efc2ba64ac2397c6e109c956e2ec
Authority class: A
Approval: FRONTIER-LOCK-20260726-003
```

Primary relevance:

- fixed Artifact, World ID, MVI group, portal, runtime, Resource Pack, and persistence boundaries;
- exact `frontier_iris` single-Overworld rule;
- LeafGrapple and Iris scope;
- MVI state ownership;
- Wayfarer_Main absence from Frontier;
- unknown-World fail-closed behavior.

### SRC-PROJ-007 — Machine-readable Frontier locks

```text
Repository: eariver/Project_Wayfarer
Paths:
  config/frontier-lock/artifact-lock.yml
  config/frontier-lock/world-id-lock.yml
  config/frontier-lock/runtime-boundary-lock.yml
  config/frontier-lock/resource-pack-input-lock.yml
  config/frontier-lock/persistence-authority-lock.yml
Authority class: A
Status: PENDING_CONTENT_REVIEW
```

These machine-readable files take precedence over prose where the Runtime Lock declares them the
source of truth. Exact blob SHAs and applicable values must be recorded before Frontier requirements
are baselined.

### SRC-PROJ-008 — Project version and plugin manifests

```text
Repository: eariver/Project_Wayfarer
Paths:
  versions.yml
  plugin-manifest.yml
Authority class: A/B by field
Status: PENDING_CONTENT_REVIEW
```

Use only fields applicable to V0.0.2 compatibility, placement, version, or locked external dependency
identity. Runtime-installed state must not be confused with future V0.0.2 requirements.

## 4. Registered Plugin baseline sources

### SRC-PLUG-001 — V0.0.1 Work Order and Design Specification

```text
Repository: eariver/Project-Wayfarer-Plugins
Path: docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md
Branch observed: main
Blob SHA: 92fbd7c8a0e657862128bf371a357734f01e9a9f
Authority class: C
```

Primary relevance:

- accepted repository, module, dependency, build, threading, persistence, migration, and public
  contract baseline;
- V0.0.1 responsibilities that V0.0.2 may extend but must not silently invalidate;
- source authority priority and referenced Concept inputs.

This document contains both requirement and design content. SWE.1 extracts only the accepted baseline
constraints and interfaces required for V0.0.2 compatibility. New V0.0.2 detailed design must not be
inherited from it without SWE.2/SWE.3 analysis.

### SRC-PLUG-002 — V0.0.1 published contracts and migrations

```text
Repository: eariver/Project-Wayfarer-Plugins
Scope:
  docs/contracts/
  docs/architecture/
  docs/adr/
  docs/handoff/V0.0.1/
  libraries/wayfarer-api/
  immutable V0.0.1 migrations
Authority class: C
Status: PENDING_INVENTORY_AND_INTERFACE_REVIEW
```

The SWE.1 output must identify the exact V0.0.1 public interfaces and persisted data constraints that
V0.0.2 depends on. Internal V0.0.1 implementation is not automatically a software requirement.

## 5. Concept inputs

The following Project Concept files are registered as class D inputs. Exact current blob SHAs must be
retrieved before use:

```text
concepts/plugins/Project_Wayfarer_Plugin_Concept_v0.0.3.md
concepts/plugins/main/Project_Wayfarer_Growth_Tool_Concept_v0.0.5.md
concepts/plugins/frontier/Project_Wayfarer_Worlds_Beyond_Plugin_Concept_v0.0.4.md
concepts/plugins/frontier/Project_Wayfarer_Ruined_Frontier_Integration_Decision_Concept_v0.0.2.md
concepts/frontier/Frontier_Server_Specification_V0.0.5.md
concepts/frontier/Worlds_Beyond_Specification_V0.0.6.md
concepts/frontier/Ruined_Frontier_Specification_V0.0.5.md
```

Concept content may elaborate intent but cannot override current Project source-of-truth documents or
an approved Runtime Lock.

## 6. Prior V0.0.2 reference inputs

### SRC-REF-001 — Prior V0.0.2 Main/Frontier requirements document

```text
Repository: eariver/Project-Wayfarer-Plugins
Path:
  docs/requirements/main-server/Project-Wayfarer-V0.1.0/V0.0.2/
  Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements.md
Observed Product-era commit: 698c387dfe2e86de8e48ea59a80f35f14c728e2a
Authority class: E
```

Use as a coverage checklist and source-provenance aid only. Every requirement must be re-derived and
revalidated. The document mixed requirements, design decisions, implementation instructions, and
verification policy, so it is not adopted as the new SWE.1 baseline.

### SRC-REF-002 — PR #14 implementation and Candidate evidence

```text
Repository: eariver/Project-Wayfarer-Plugins
PR: #14
Frozen branch: feature/V0.0.2-main-frontier
Latest redesign-decision-era commit: 0f06ee9bfeaf54e2ff9cd4af53114662e3988861
Authority class: E
```

Use only for:

- defect and risk discovery;
- identifying missing or ambiguous requirements;
- identifying reusable implementation candidates after SWE.3;
- checking that verification addresses previously missed runtime paths.

Do not derive Product intent from implementation behavior alone.

## 7. Owner decisions to register

Owner decisions made during this redesign must be added under `08-decisions/` with stable `DEC-*`
identifiers. Conversation statements are not baseline authority until committed.

Current decisions to formalize:

```text
DEC-PROC-001:
  V0.0.1 is assumed complete for initial V0.0.2 requirements analysis.

DEC-PROC-002:
  The lifecycle follows a tailored V-model aligned to SWE.1 through SWE.6.

DEC-PROC-003:
  ChatGPT owns SWE.1, SWE.2, and SWE.3 detailed design.

DEC-PROC-004:
  Codex owns unit construction from approved SWE.3 design and must provide an
  implementation-to-detailed-design consistency record.

DEC-PROC-005:
  Codex executes SWE.4 through SWE.6; ChatGPT independently reviews those work products and results.
```

## 8. Pending source actions

- retrieve and hash all machine-readable Frontier lock files;
- inventory V0.0.1 public contracts, migrations, and handoff records;
- retrieve current Concept blob SHAs and classify applicable statements;
- inspect `versions.yml` and `plugin-manifest.yml` for applicable baseline constraints;
- search for later Owner decisions that supersede any registered source;
- identify whether PR #14 introduced requirement interpretations that were never approved upstream.
