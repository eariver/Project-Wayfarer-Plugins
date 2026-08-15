# SWE.1 Joint Owner Review Log

Document ID: `REV-SWE1-002`  
Revision: E  
State: `IN_REVIEW`  
Date: 2026-08-15 JST  
Reviewers: Project Owner and ChatGPT  
Reviewed source: `SWE1-SRC-002` Revision E  
Reviewed derived documents: current draft SWE.1 package on `redesign/V0.0.2-swe1-3`

## 1. Purpose

Track the clause-by-clause joint review of the canonical requirement source and its derived SWE.1 requirements. An item is not a G1 approval merely because its correction direction is approved here. All approved corrections must be consolidated, traced, and self-reviewed before the requirements baseline can be approved.

## 2. Review progress

| Canonical clause | Review disposition | Controlling correction |
|---|---|---|
| `CAN-COM-001`–`CAN-COM-005` | Correction directions approved | `DEC-REQ-002` |
| `CAN-COM-006`–`CAN-COM-010` | Correction directions approved | `DEC-REQ-004` |
| `CAN-CORE-001`–`CAN-CORE-005` | Correction directions approved; Core CON-004 deduplicated | `DEC-REQ-005` |
| `CAN-MAIN-001` | Correction direction approved | `DEC-REQ-006` §2 |
| `CAN-MAIN-002` | Correction direction approved with later Owner durability-authority refinement | `DEC-REQ-006` §3 |
| `CAN-MAIN-003` | Correction direction approved | `DEC-REQ-006` §4 |
| `CAN-MAIN-004` | Correction direction approved with fallback-delivery refinement | `DEC-REQ-006` §5 |
| `CAN-MAIN-005` | Correction direction approved with possession/storage and modification refinements | `DEC-REQ-006` §6 |
| `CAN-MAIN-006` | Correction direction approved; death-drop suppression withdrawn and respawn replacement deduplicated | `DEC-REQ-007` §2 |
| `CAN-MAIN-007` | Correction direction approved; configured exact world allowlist | `DEC-REQ-007` §3 |
| `CAN-MAIN-008` | Correction direction approved with Owner refinement permitting repeated/provenance-neutral mining | `DEC-REQ-007` §4 |
| `CAN-MAIN-009` | Correction direction approved; `Long.MAX_VALUE`/fixed-point fixation removed | `DEC-REQ-007` §5 |
| `CAN-MAIN-010` | Correction direction approved; block/ore weighting withdrawn | `DEC-REQ-007` §6 |
| `CAN-MAIN-011` onward | Not yet jointly reviewed | None except previously recorded mandatory propagation where applicable |

The Common and Core canonical sections and Main `CAN-MAIN-001` through `CAN-MAIN-010` are reviewed and integrated. The next substantive review item is `CAN-MAIN-011 — Material and enchantment evolution`.

## 3. Key Owner determinations

### 3.1 Common/Core determinations retained

The integrated Common/Core decisions remain controlling: topology independence, public shared-contract access, authority separation, player-state boundaries, platform-authorized execution contexts, capability-scoped lifecycle, protected-operation identity/replay/UNKNOWN semantics, durable-domain migration ownership, audit/data minimization, reuse-first traceability, V0.0.1 accepted-contract compatibility, Core public-contract isolation/type identity, V0.0.2 Core Waymark transaction allocation, provider-guarantee containment, and immutable accepted Core migration history.

### 3.2 Main capability lifecycle and logical/physical authority

Main capability availability follows approved deployment allocation and actual capability prerequisites, not a historical backend name or universal Core-plugin presence. Main owns logical Growth Tool state with MariaDB as durable authority; Minecraft remains authority for current physical item state including durability/damage.

Logical lifecycle, delivery, and branch are independent state dimensions. Physical persistent metadata is a reference/credential resolved against current logical authority rather than an independent authority source.

### 3.3 Delivery and ordinary possession remain distinct

Logical entitlement and physical delivery are separate effects. Inventory-full or equivalent delivery failure retains the same pending entitlement and cannot use world-drop fallback. After delivery, however, ordinary player/entity drop, pickup, storage, transfer, death/item-lifecycle drop, loss, destruction, and despawn are not prohibited merely because the item is managed.

Physical possession, including non-owner possession, does not transfer logical ownership. Only the logical owner may use/progress the Growth Tool.

### 3.4 Growth Tool modification boundary

Durability restoration and enchantment-state modification are Wayfarer-controlled. V0.0.2 prohibits processing a managed Growth Tool/Broken Tool through an anvil or grindstone, including anvil rename. Crafting repair/combination, Mending, smithing/equivalent transformation, and supported external paths may not bypass controlled repair/evolution semantics.

### 3.5 Death/respawn is not a special free-recovery lifecycle

The prior death-drop suppression requirement is withdrawn. Death, respawn, ordinary loss/destruction, or non-observation does not automatically change logical ownership, delivery status, physical issuance, or authority epoch and does not create a replacement entitlement. Respawn alone does not restore a Growth Tool; replacement uses the applicable authorized recovery/reissue flow.

Historical `SWE1-MAIN-001-CON-008` is superseded by Owner correction and historical `CON-009` is superseded by deduplication into `CON-005`.

### 3.6 Progress worlds are configured exact identities

Progress-world eligibility uses exact membership in approved configuration rather than permanent hard-coded Product world names. V0.0.2 defaults remain `resource`, `resource_nether`, and `resource_end`. Similar-name/prefix/suffix/dimension heuristics do not implicitly adopt another world, and one unavailable configured world does not invalidate other independently valid configured worlds.

### 3.7 Progress is qualifying player mining, provenance-neutral, exactly once

One completed qualifying player-caused break grants progress exactly once when the world, Minecraft pickaxe-minable tag, current authorized active Growth Pickaxe, and Survival/Adventure completion rules are satisfied. Creative/Spectator, denied/cancelled non-completing mining, and non-player removal do not grant progress.

Block provenance is not tracked for eligibility: natural, player-placed, generator/plugin-created, Silk-Touch-collected-and-re-placed, and repeated eligible mining remain allowed. Editor/plugin names are verification examples, not Product dependencies.

### 3.8 Progress numeric semantics are representation-independent

The former `1.000 = 1000 internal units`, Java `long`, and `Long.MAX_VALUE` fixation is removed. Positive progress remains monotonic and must not wrap negative, corrupt, or become undefined. Any selected bounded maximum must be safe and operable for threshold/evolution, presentation, persistence/reload, and reconciliation. `AMD-009` concrete `Long.MAX_VALUE` saturation is superseded by this broader Owner-approved safety rule.

### 3.9 Progress weighting is uniform

The Owner withdrew block-specific base weights, ore multipliers, rarity multipliers, and equivalent material-dependent progress modifiers. Every qualifying break receives the same configured positive logical progress increment. The V0.0.2 supplied/default increment is `1.00` logical progress per qualifying break.

This preserves player choice between branch mining, cobblestone generators, ordinary excavation, and repeated eligible mining instead of steering progression by block rarity. Historical `SWE1-MAIN-002-CAP-006` is superseded and not reused.

## 4. Checkpoint history

- Common checkpoint: `CAN-COM-006`–`010`, integrated under `DEC-REQ-002`/`DEC-REQ-004`.
- Core checkpoint: `CAN-CORE-001`–`005`, integrated under `DEC-REQ-005`.
- Main checkpoint 1: `CAN-MAIN-001`–`005`, integrated under `DEC-REQ-006`.
- Main checkpoint 2: `CAN-MAIN-006`–`010`, integrated under `DEC-REQ-007` after five newly approved clauses.

## 5. Package impact

The current provisional active Product requirement total after Main `CAN-MAIN-006`–`010` integration is **176**:

```text
CAP: 65
CON: 58
IFC: 14
QLT: 39
TOTAL ACTIVE: 176
```

This checkpoint supersedes:

- `SWE1-MAIN-001-CON-008` — death-drop suppression withdrawn;
- `SWE1-MAIN-001-CON-009` — respawn-only replacement rule deduplicated into `CON-005`;
- `SWE1-MAIN-002-CAP-006` — ore multipliers withdrawn.

Historical IDs remain traceable and are not reused. The prior historical `SWE1-CORE-001-CON-004` remains superseded as before.

The complete post-review automated identifier/source/count audit and full SWE.1 self-review remain required before G1.

## 6. Checkpoint cadence after Main-006–010 integration

```text
NEWLY APPROVED CLAUSES SINCE CHECKPOINT:
  0 / 5

CURRENT SECTION:
  MAIN

NEXT REVIEW ITEM:
  CAN-MAIN-011 — Material and enchantment evolution
```

A later logical-section transition or five newly approved clauses creates a checkpoint candidate; repository mutation still requires explicit Owner instruction.

## 7. Gate state

```text
JOINT REVIEW:
  COMMON SECTION COMPLETE
  CORE SECTION COMPLETE
  MAIN CAN-MAIN-001–010 INTEGRATED
  MAIN REVIEW CONTINUES AT CAN-MAIN-011

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 AND LATER:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / VERIFICATION EXECUTION / PR READY / MERGE TO MAIN / TAG / DEPLOY / RELEASE:
  NOT AUTHORIZED BY THIS CHECKPOINT
```
