# V0.0.2 SWE.1 Scope and Non-scope

Document ID: `SWE1-SCOPE-001`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `SCOPE`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A

## 1. Purpose

Define the software scope analyzed by the current SWE.1 baseline. This document allocates scope; it
does not duplicate the normative statements owned by target requirement documents.

## 2. In-scope target work products

### COMMON

- artifact placement and dependency direction;
- data-authority and threading constraints;
- transaction, migration, lifecycle, and audit quality;
- V0.0.1 upgrade compatibility applicable across modules.

Owning requirements: `SWE1-COMMON-001`.

### CORE

- V0.0.1-compatible shared Core services;
- additive API/capability extension;
- approved Waymark transaction boundary;
- Core migration discipline.

Owning requirements: `SWE1-CORE-001`.

### MAIN

- Main-only lifecycle;
- one logical Growth Pickaxe per player;
- physical identity and owner/epoch authority;
- initial and pending delivery;
- transfer and death restrictions;
- progress, weights, evolution, durability, Broken state, checkpointing;
- management GUI, full repair, player-paid reissue;
- administration, debug, and approved permission groups.

Owning requirements:
- `SWE1-MAIN-001`
- `SWE1-MAIN-002`
- `SWE1-MAIN-003`

### FRONTIER

- Frontier-only lifecycle;
- exact Worlds Beyond world recognition;
- MVI non-ownership;
- Frontier persistence ownership;
- approved permission groups.

Owning requirements: `SWE1-FRONTIER-001`.

### WORLDS BEYOND

- first safe-entry loadout and typed pending delivery;
- permanent traversal-item identity, restrictions, and durable death recovery;
- Elytra, LeafGrapple integration, Navigation;
- Launchpad item, placement, use, lifecycle, protection, reconciliation;
- Frontier shop;
- vanilla portal denial;
- theme administration.

Owning requirements:
- `SWE1-WB-001`
- `SWE1-WB-002`

## 3. Explicit software non-scope

The following are not requirements of this SWE.1 baseline:

### Main

- Axe or Shovel growth tools;
- player-paid Fortune/Silk Touch branch switching;
- Netherite upgrade;
- rankings, evolution rewards, special abilities, or cosmetics;
- cross-server Growth Tool use;
- prohibition of ordinary vanilla tools.

### Frontier / Worlds Beyond

- full Waystone lifecycle, placement, discovery, or teleport;
- Waystone Placement Tool sales;
- Ruined Frontier gameplay;
- EliteMobs gameplay;
- MVI profile implementation or switching;
- Gate implementation or Gate lifecycle;
- Iris world generation;
- Frontier resource-pack build or delivery;
- a separate EliteMobs-MVI adapter without later explicit authorization;
- a generic Portal/Gate/Waystone/System-Structure launchpad-exclusion subsystem;
- discovery of arbitrary pressure plates that have no durable Launchpad authority;
- an in-world return structure.

## 4. Excluded process and roadmap material

The following content from the two source files is deliberately not part of the software-requirement
baseline:

- previous PR #14 implementation status;
- current/next Codex work lists;
- previous candidate, pre-client, CI, and headless status;
- implementation anchor and branch state;
- prior version roadmap and future-version scheduling;
- prior release sequencing and Project Roadmap order completion;
- stable-release publication procedure and artifact handoff procedure;
- detailed test execution lists and test repetition policy;
- AMD-012 candidate-versus-stable process distinction.

These may inform later work orders or verification planning only after the SWE.1–SWE.3 baselines are
approved.

## 5. Conditional and deferred target domains

### RF

No normative `SWE1-RF-*` document is created for this baseline because Ruined Frontier gameplay is
outside scope.

### ADAPTER

No normative `SWE1-ADAPTER-*` document is created. A separate adapter remains prohibited unless a
later authoritative decision explicitly requires it.

### Waystone

No Waystone target document is created. The current requirement is only that incomplete Waystone
actions remain unavailable and that current Frontier persistence does not take ownership of ordinary
inventory.

## 6. Scope blockers

The open issues in `SWE1-ISSUE-001` do not broaden scope. They identify details that must be resolved
or explicitly accepted as blockers before G1 PASS. A resolution that materially changes the scope
requires an updated scope revision and upstream impact review.
