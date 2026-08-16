# SWE.1 Joint Owner Review Log

Document ID: `REV-SWE1-002`  
Revision: F  
State: `IN_REVIEW`  
Date: 2026-08-16 JST  
Reviewers: Project Owner and ChatGPT  
Reviewed source: `SWE1-SRC-002` Revision F  
Reviewed derived documents: current draft SWE.1 package on `redesign/V0.0.2-swe1-3`

## 1. Purpose

Track the clause-by-clause joint review of the canonical requirement source and its derived SWE.1 requirements. An item is not a G1 approval merely because its correction direction is approved here. All approved corrections must be consolidated, traced, and self-reviewed before the requirements baseline can be approved.

## 2. Review progress

| Canonical clause | Review disposition | Controlling correction |
|---|---|---|
| `CAN-COM-001`–`CAN-COM-005` | Correction directions approved | `DEC-REQ-002` |
| `CAN-COM-006`–`CAN-COM-010` | Correction directions approved | `DEC-REQ-004` |
| `CAN-CORE-001`–`CAN-CORE-005` | Correction directions approved; Core CON-004 deduplicated | `DEC-REQ-005` |
| `CAN-MAIN-001`–`CAN-MAIN-005` | Correction directions approved | `DEC-REQ-006` |
| `CAN-MAIN-006`–`CAN-MAIN-010` | Correction directions approved; death-drop/numeric/material-weighting corrections integrated | `DEC-REQ-007` |
| `CAN-MAIN-011` | Correction direction approved; configured/default evolution values and branch projection | `DEC-REQ-008` §2 |
| `CAN-MAIN-012` | Correction direction approved; internally consistent reconciliation and durability semantics | `DEC-REQ-008` §3 |
| `CAN-MAIN-013` | Correction direction approved; same-authority Broken lifecycle transition | `DEC-REQ-008` §4 |
| `CAN-MAIN-014` | Correction direction approved; owner management interface / presentation independence | `DEC-REQ-008` §5 |
| `CAN-MAIN-015` | Correction direction approved; quote-confirmed protected full repair / QLT-002 deduplicated | `DEC-REQ-008` §6 |
| `CAN-MAIN-016` onward | Not yet jointly reviewed | Previously recorded mandatory propagation where applicable |

The Common and Core canonical sections and Main `CAN-MAIN-001` through `CAN-MAIN-015` are reviewed and integrated. The next substantive review item is `CAN-MAIN-016 — Player-paid reissue`.

## 3. Key Owner determinations

### 3.1 Common/Core determinations retained

The integrated Common/Core decisions remain controlling: topology independence, public shared-contract access, authority separation, player-state boundaries, platform-authorized execution contexts, capability-scoped lifecycle, protected-operation identity/replay/UNKNOWN semantics, durable-domain migration ownership, audit/data minimization, reuse-first traceability, V0.0.1 accepted-contract compatibility, Core public-contract isolation/type identity, V0.0.2 Core Waymark transaction allocation, provider-guarantee containment, and immutable accepted Core migration history.

### 3.2 Main authority, delivery, possession, and progress through CAN-MAIN-010

Main owns logical Growth Tool authority with MariaDB as durable authority; Minecraft owns current physical item state including current durability/damage. Physical metadata/possession is not authority and is resolved against current logical authority.

Logical entitlement and physical delivery are distinct. Inventory-full delivery retains the same pending entitlement without world-drop fallback. After delivery, ordinary possession/storage/drop/death/item lifecycle remains permitted and does not transfer logical ownership. Only the logical owner may use/progress the Growth Tool.

Durability restoration and enchantment modification are Wayfarer-controlled; V0.0.2 anvil/grindstone processing including rename is prohibited. Progress-world membership is exact configured identity. Qualifying progress is one completed eligible player-mined break, provenance-neutral and repeatable. Progress numeric representation is implementation-independent and overflow-safe. Each qualifying break receives one uniform configured positive logical increment, default `1.00`; material/ore weighting is withdrawn.

### 3.3 Configured material/enchantment evolution

The V0.0.2 material sequence remains Wood → Stone → Iron → Diamond. Material thresholds `100/400/1200`, post-Diamond increment definition `800 + 200n + 40n²`, the repeating Efficiency/Unbreaking/Efficiency/Unbreaking/Fortune mapping, and effective caps are initial/supplied configuration defaults rather than permanent untunable Product constants.

The post-Diamond formula is an increment from the preceding evolution threshold. Conceptual progress/evolution continues after effective caps. `FORTUNE` is the default branch. In `SILK_TOUCH`, Silk Touch I is effective and physical Fortune is suppressed while conceptual Fortune progression continues; returning to `FORTUNE` reapplies current conceptual Fortune subject to cap. Ordinary player-paid branch switching remains out of scope.

### 3.4 Configuration reconciliation

Evolution evaluation uses one internally consistent approved configuration snapshot. Configuration changes do not rewrite cumulative progress or authoritative identity/owner/lifecycle/delivery/branch/issuance/epoch. Derived material/evolution/effective-enchantment state may promote or demote.

Global eager scanning of all Growth Tools is not required. Reconciliation alone does not repair or revive BROKEN. When a current authorized ACTIVE physical pickaxe changes material, Minecraft-authoritative remaining-durability fraction is mapped to the new capacity with discrete quantization and at least one remaining point. No separate authoritative damage value is manufactured when the physical representation is unavailable.

A real qualifying progress addition that crosses one or more evolution thresholds produces one progression-triggered full recovery; no crossing produces no progression-triggered recovery. Explicit Repair/Reissue remain governed separately.

### 3.5 Broken-state continuity

Terminal durability does not destroy the logical Growth Tool. A same-operation evolution-triggered recovery is applied before terminal Broken conversion. Otherwise the same logical/current physical authority transitions `ACTIVE → BROKEN` without reissue, epoch rotation, or a new delivery entitlement.

The Broken representation is distinguishable from an active pickaxe and resolves to the same current authority. `GRAY_DYE` is the V0.0.2 supplied/default Broken presentation, not authority. BROKEN cannot satisfy ACTIVE-only Growth Tool operations but ordinary possession/lifecycle remains governed by earlier Main rules. Broken state remains durably recoverable across restart/lifecycle interruption without implicit revival/replacement.

### 3.6 Owner management interface

The required player entry route is main-hand air right-click without targeting a block/entity while holding the current authorized Growth Tool/Broken Tool; corresponding off-hand interaction does not open the GUI. Non-owner possession/stale/malformed authority does not grant owner management access.

The GUI presents lifecycle, derived Growth Pickaxe material, cumulative/conceptual evolution, next threshold or no-next-threshold state, effective enchantments, branch, applicable current physical durability or Broken state, repair availability/preview, and meaningful conceptual/effective differences. Exact wording, inventory size, slots, decorative items, name, and lore are not fixed acceptance semantics.

Opening/viewing management state or repair preview does not itself authorize repair, debit Waymark, or produce another protected state-changing effect. Permission-node allocation remains for CAN-MAIN-018 review. CAN-MAIN-014 was removed from the source allocation of protected financial UI replay `SWE1-MAIN-003-QLT-001`.

### 3.7 Owner-paid full repair

Player Repair remains full-repair only. Current authorized ACTIVE below maximum durability and current authorized BROKEN are eligible; fully durable ACTIVE displays 0 WM and starts no debit. Missing/stale/revoked/non-owner/unresolved physical representations are not repaired through this operation.

The original repair formulas are V0.0.2 supplied/default pricing configuration, not permanent untunable constants. ACTIVE price uses Minecraft-authoritative missing-durability ratio.

Protected Repair requires explicit quote and explicit confirmation; authority/lifecycle/physical state/evolution/pricing/quote validity are revalidated before debit. A changed price/eligibility invalidates the stale confirmation rather than silently charging a new amount. The repair benefit proceeds only after debit success is proven through the Core-provided V0.0.2 Waymark transaction contract.

Successful ACTIVE repair preserves identity/owner/progress/delivery/branch/issuance/epoch and restores full durability. Successful BROKEN repair transitions the same authority `BROKEN → ACTIVE`, reconstructs the current active representation from current approved evolution state, and restores full durability. Repair is not reissue/authority rotation/new delivery entitlement.

Repair is not reported successful until required logical and physical repaired state is established. Partial/ambiguous repair benefit remains `UNKNOWN` and is reconciled without blind retry or automatic refund. Generic compensation remains controlled by Common requirements; duplicate `SWE1-MAIN-003-QLT-002` is superseded by deduplication.

## 4. Checkpoint history

- Common checkpoint: `CAN-COM-006`–`010`, integrated under `DEC-REQ-002`/`DEC-REQ-004`.
- Core checkpoint: `CAN-CORE-001`–`005`, integrated under `DEC-REQ-005`.
- Main checkpoint 1: `CAN-MAIN-001`–`005`, integrated under `DEC-REQ-006`.
- Main checkpoint 2: `CAN-MAIN-006`–`010`, integrated under `DEC-REQ-007`.
- Main checkpoint 3: `CAN-MAIN-011`–`015`, integrated under `DEC-REQ-008` after five newly approved clauses.

## 5. Package impact

The current provisional active Product requirement total after Main `CAN-MAIN-011`–`015` integration is **178**:

```text
CAP: 67
CON: 58
IFC: 14
QLT: 39
TOTAL ACTIVE: 178
```

New active identifiers this checkpoint:

- `SWE1-MAIN-002-CAP-017`
- `SWE1-MAIN-002-QLT-007`
- `SWE1-MAIN-003-CAP-010`

New historical superseded identifier:

- `SWE1-MAIN-003-QLT-002` — generic Repair compensation duplicated Common protected-operation semantics.

Previously superseded historical IDs remain traceable and are not reused: `SWE1-CORE-001-CON-004`, `SWE1-MAIN-001-CON-008`, `SWE1-MAIN-001-CON-009`, and `SWE1-MAIN-002-CAP-006`.

The complete post-review automated identifier/source/count audit and full SWE.1 self-review remain required before G1.

## 6. Checkpoint cadence after Main-011–015 integration

```text
NEWLY APPROVED CLAUSES SINCE CHECKPOINT:
  0 / 5

CURRENT SECTION:
  MAIN

NEXT REVIEW ITEM:
  CAN-MAIN-016 — Player-paid reissue
```

A later logical-section transition or five newly approved clauses creates a checkpoint candidate; repository mutation still requires explicit Owner instruction.

## 7. Gate state

```text
JOINT REVIEW:
  COMMON SECTION COMPLETE
  CORE SECTION COMPLETE
  MAIN CAN-MAIN-001–015 INTEGRATED
  MAIN REVIEW CONTINUES AT CAN-MAIN-016

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 AND LATER:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / VERIFICATION EXECUTION / PR READY / MERGE TO MAIN / TAG / DEPLOY / RELEASE:
  NOT AUTHORIZED BY THIS CHECKPOINT
```
