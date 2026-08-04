# Project Wayfarer Plugin Mainline Canonical Requirements Source

Document ID: `SWE1-SRC-002`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Predecessor sources:
- `Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED.md`
- `Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register.md`

## 1. Purpose and authority

This document is the normalized positive-requirement source used before SWE.1 requirement
decomposition. It combines the mainline requirement source with only those later Owner decisions that
clarified a requirement or resolved a contradiction in that source.

This document deliberately excludes:

- PR, branch, commit, CI, candidate, and implementation-status information;
- statements whose authority is only the behavior of the abandoned PR #14 implementation;
- the previous implementation sequence, pre-client roadmap, release roadmap, and next-action plan;
- test-execution history and claims that a behavior was already implemented;
- future-version scheduling and version-number reservations;
- AMD-012, because it distinguishes candidate preparation from stable release rather than changing
  software behavior.

Source clause identifiers beginning with `CAN-` are provenance anchors only. They are not SWE.1
software-requirement identifiers.

## 2. Source provenance

| Source | SHA-256 | Use in this canonical source |
|---|---|---|
| Mainline requirement source | `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F` | Base authority for product behavior and constraints |
| Requirement/implementation delta register | `A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1` | Only explicit Owner requirement amendments and clarifications; implementation and roadmap content excluded |
| Owner instruction dated 2026-08-05 | Repository decision `DEC-REQ-001` | Controls the merge and analysis method |

## 3. Applied amendment disposition

| Amendment | Disposition | Canonical effect |
|---|---|---|
| AMD-001 | Applied | Launchpad launch direction uses the current user's look direction, not placement yaw |
| AMD-002 | Applied | Vanilla portal traversal is denied inside `frontier_iris` |
| AMD-003 | Applied | Durable launchpad creation state is limited; velocity, cooldown, and auto-Elytra use current configuration |
| AMD-004 | Applied as current-scope clarification only | Dedicated generic Gate/Portal/System-Structure placement exclusion is not required; no later roadmap is adopted here |
| AMD-005 | Applied as current-scope clarification only | A physical pressure plate without durable launchpad authority need not be identified as a launchpad |
| AMD-006 | Applied | Growth Tool is removed from death drops and is not restored automatically on respawn |
| AMD-007 | Applied | Player-paid Growth Tool reissue is added with an explicit quote/confirm flow and defined price |
| AMD-008 | Applied | Permanent Worlds Beyond items use durable pending delivery after death |
| AMD-009 | Applied | Positive progress addition saturates at `Long.MAX_VALUE` |
| AMD-010 | Applied only as a present requirement clarification | Exact language, layout, name, and lore are not V0.0.2 functional acceptance obligations |
| AMD-011 | Applied | Main and Frontier permissions use the approved medium-grained groups |
| AMD-012 | Excluded | Process/release-stage clarification, not a software-behavior change |

The delta register's proposed code changes, current implementation descriptions, test plans, unresolved
implementation gates, and release sequencing are not imported as requirements.

## 4. Common system boundaries

### CAN-COM-001 — Runtime artifacts and placement

Wayfarer-owned runtime software is separated into `Wayfarer_Core`, `Wayfarer_Main`, and
`Wayfarer_Frontier`.

- `Wayfarer_Core` is deployed on Main and Frontier.
- `Wayfarer_Main` is deployed only on Main.
- `Wayfarer_Frontier` is deployed only on Frontier.
- No new Wayfarer-owned gameplay plugin is deployed to Lobby for this scope.

### CAN-COM-002 — Dependency direction

`Wayfarer_Main` and `Wayfarer_Frontier` may depend on the public API of `Wayfarer_Core`.
`Wayfarer_Core` must not depend on either gameplay plugin. Main and Frontier must not depend on each
other, and circular dependencies are prohibited.

### CAN-COM-003 — Data authority

| Domain | Authority |
|---|---|
| Wayfarer-owned durable data | MariaDB |
| Cache, lock, pub/sub, and messaging assistance | Redis |
| Normal Frontier player state | MVI |
| Waymark operations | Wayfarer_Core's approved provider/transaction boundary |
| Physical blocks, entities, and structures | World files |
| Runtime events, items, worlds, and mutations | Minecraft/Paper |

### CAN-COM-004 — Prohibited ownership and access

The plugins must not:

- manipulate RedisEconomy internal keys;
- update MVI, mcMMO, or EliteMobs internal databases;
- store normal inventory, armor, offhand, Ender Chest, XP, health, food, or MVI profiles in Wayfarer
  MariaDB;
- transfer normal items between Main and Frontier;
- expose unsupported internal APIs as product contracts;
- place Main- or Frontier-specific gameplay ownership in Core.

### CAN-COM-005 — Threading boundary

Minecraft/Paper objects and event cancellation are accessed or mutated on the server main thread.
JDBC, Redis I/O, audit writes, queries, checkpoint persistence, and expiration-candidate searches are
performed asynchronously. Synchronous database or Redis I/O on the main thread is prohibited.

### CAN-COM-006 — Lifecycle and fail-closed behavior

Before gameplay listeners and commands become active, each plugin validates required configuration,
Core capability, schema compatibility, runtime role, and required external capability. A required
dependency or capability failure prevents the affected gameplay from becoming available.

During disable, new operations stop, bounded asynchronous flush is attempted, and callbacks from an
obsolete runtime generation must not mutate the new or disabled runtime.

### CAN-COM-007 — Transaction safety

Financial and durable-delivery operations use explicit operation or transaction identity and must
prevent duplicate debit, duplicate refund, duplicate item delivery, and stale replay. An ambiguous
provider result is represented as `UNKNOWN`; it is not automatically retried or treated as success.

### CAN-COM-008 — Migration ownership

Core, Main, and Frontier own separate table prefixes, migration locations, and migration histories.

| Module | Table prefix |
|---|---|
| Core | `wf_core_*` |
| Main | `wf_main_*` |
| Frontier | `wf_frontier_*` |

Migrations are forward-only. Applied migrations are immutable. Empty-database installation and
upgrade from the accepted V0.0.1 baseline must be supported. A module must not create or modify tables
owned by another module.

### CAN-COM-009 — Audit and operational safety

Security-sensitive, financial, delivery, recovery, administrative, and reconciliation outcomes are
auditable without exposing secrets, raw credentials, or unnecessary personal/runtime data.

### CAN-COM-010 — External library reuse

Detailed design must use suitable Java, Paper/Spigot/Bukkit, adopted-plugin, or approved external
library capabilities instead of reimplementing equivalent functionality. The selected API behavior
must be justified by authoritative version-appropriate references under `GOV-ENG-001`.

## 5. Wayfarer_Core requirements source

### CAN-CORE-001 — V0.0.1 compatibility

The accepted V0.0.1 public API and applied migrations are preserved. New APIs required by Main or
Frontier are additive and compatible unless the Owner explicitly approves a baseline-breaking change.

### CAN-CORE-002 — Public API boundary

Core public API must not expose Bukkit/Paper runtime objects, JDBC connections, HikariCP, Flyway,
Lettuce, or another implementation-specific persistence/client type. API classes must not be bundled
in multiple runtime artifacts in a way that creates class-identity conflicts.

### CAN-CORE-003 — Waymark transaction boundary

Core provides the approved Waymark provider/transaction boundary used by Main repair/reissue and
Frontier shop operations. Provider acceptance does not prove durable Redis completion, external
effect lookup, unconditional exactly-once behavior, or external atomic operation identity.

### CAN-CORE-004 — Ambiguous Waymark outcomes

Balance difference is not used as proof of operation success. `UNKNOWN` is not automatically retried
or completed. No Wayfarer-specific side channel is added to RedisEconomy to manufacture stronger
semantics than the provider offers.

### CAN-CORE-005 — Core migration discipline

A new Core migration is added only when a new Core-owned durable capability requires it. Existing
V0.0.1 migration files remain unchanged.

## 6. Wayfarer_Main requirements source

### CAN-MAIN-001 — Deployment and lifecycle

Wayfarer_Main activates only on the Main backend, requires Wayfarer_Core, validates its dependencies
and schema before activation, and fails closed when a mandatory prerequisite is unavailable.

### CAN-MAIN-002 — Logical Growth Tool authority

MariaDB owns one logical `PICKAXE` per `owner_uuid + tool_type`.

The logical record supports:

- tool status: `ACTIVE`, `BROKEN`, `REVOKED`;
- delivery status: `DELIVERED`, `PENDING`;
- active branch: `FORTUNE`, `SILK_TOUCH`;
- stable identity, owner, epoch, cumulative fixed-point progress, stored damage, schema version,
  optimistic-lock version, timestamps, and checkpoint timestamp.

### CAN-MAIN-003 — Physical identity

A physical Growth Tool or Broken Tool carries PDC sufficient to identify item type, physical
instance, logical tool, owner, tool type, epoch, schema, and display revision. Display name, lore, and
material are not sufficient identity.

Unknown type/schema, malformed identity, wrong owner, wrong logical tool, and stale epoch are denied.

### CAN-MAIN-004 — Initial asynchronous delivery

On Main join, the plugin asynchronously reads or race-safely creates the logical record, then
revalidates the player's online state before main-thread delivery.

A new player receives one tool. Rejoin does not duplicate it. Inventory-full delivery does not drop
the item; it remains pending, the player is notified, the reason is audited, and retry is possible on
a later join or authorized administrative action. A delivered record is not automatically reissued.

### CAN-MAIN-005 — Owner binding and inventory restrictions

Only the current owner may use or progress the current physical instance. Manual drop, other-player
pickup, container storage, anvil, grindstone, smithing, crafting repair, same-tool combination,
Mending, item-frame/armor-stand placement, and other supported transfer/repair paths are denied.
Supported external repair integration must not bypass the same authority.

### CAN-MAIN-006 — Death behavior

The Growth Tool or Broken Tool is removed from death drops. It is not stored as a raw in-memory
ItemStack for automatic respawn restoration, and it is not automatically restored on respawn. The
logical tool record remains authoritative.

### CAN-MAIN-007 — Progress worlds

Progress is available only in exact worlds:

- `resource`
- `resource_nether`
- `resource_end`

Main worlds, similarly named worlds, unknown worlds, and all other worlds are excluded.

### CAN-MAIN-008 — Eligible progress event

A successful player block break adds progress exactly once when:

- the block is tagged `minecraft:mineable/pickaxe`;
- the current main-hand item is the authorized active Growth Pickaxe;
- owner, tool, and epoch match;
- the event is not cancelled;
- Survival is used, or Adventure results in an actual successful block break.

Player-placed, generator-created, re-placed Silk Touch ore, and normally broken plugin-generated
blocks are eligible. Creative, Spectator, cancelled breaks, explosions, pistons, commands,
WorldEdit/FAWE removal, and other non-player-break removal are not eligible.

### CAN-MAIN-009 — Progress representation and saturation

`1.000` progress equals `1000` internal integer units. Positive addition saturates at
`Long.MAX_VALUE`, never wraps negative, and becomes a no-op after saturation. Threshold evaluation,
GUI display, checkpointing, and configuration reconciliation must remain defined at the saturated
value. Detailed per-addition overflow audit is not required.

### CAN-MAIN-010 — Progress weights

The following initial configurable defaults apply:

| Block/category | Progress |
|---|---:|
| Cobblestone | 0.25 |
| Cobbled Deepslate | 0.35 |
| Stone/Granite/Diorite/Andesite/Tuff/Calcite | 1.00 |
| Netherrack/Blackstone/Basalt | 1.00 |
| Deepslate/End Stone | 1.25 |
| Obsidian/Crying Obsidian | 2.00 |
| Undefined pickaxe-tag block | 1.00 |

| Ore group | Multiplier |
|---|---:|
| Coal/Nether Quartz | 1.50 |
| Copper | 1.60 |
| Redstone | 1.75 |
| Iron/Nether Gold | 2.00 |
| Lapis | 2.10 |
| Gold | 2.50 |
| Diamond | 3.50 |
| Emerald/Ancient Debris | 4.00 |

### CAN-MAIN-011 — Material and enchantment evolution

Material progression is Wood → Stone → Iron → Diamond at cumulative progress 100, 400, and 1200.
After Diamond, the initial increment for enchantment evolution number `n`, beginning at `n=1`, is
`800 + 200n + 40n²`.

The repeating cycle is Efficiency, Unbreaking, Efficiency, Unbreaking, Fortune. Effective caps are:

- Efficiency 10
- Unbreaking 10
- Fortune 5
- Silk Touch 1

Conceptual level, progress, and evolution count continue after effective caps. Default branch is
`FORTUNE`; authorized administration may select `FORTUNE` or `SILK_TOUCH`.

### CAN-MAIN-012 — Threshold and configuration reconciliation

Threshold evaluation is deterministic and supports the full valid progress range. Configuration is
applied as an internally consistent revision.

On configuration reconciliation:

- cumulative progress is unchanged;
- material, enchantments, and evolution count are recomputed;
- promotion and demotion are permitted;
- reconciliation alone does not repair the item;
- active durability ratio is preserved across material change, with at least one durability point;
- a broken tool remains broken.

Only a real progress addition that increases evolution count restores the item to full durability.

### CAN-MAIN-019 — Normal durability behavior

An authorized active Growth Pickaxe follows the approved Paper/vanilla nonterminal durability result
during ordinary use. Managed-item handling does not suppress normal durability loss merely because the
item is authorized. The explicit exceptions are an evolution-triggered full recovery, denied or
cancelled use, controlled repair/reissue, and terminal-damage conversion to the Broken state.

### CAN-MAIN-013 — Broken state

Before vanilla item disappearance at terminal durability, the physical representation becomes
`GRAY_DYE` with logical status `BROKEN`.

Identity, owner, tool type, epoch, progress, branch, and schema remain associated. A broken tool cannot
mine, progress, or use external repair; it remains owner-bound, can open the management GUI, survives
restart, and is checkpointed as a critical state.

### CAN-MAIN-014 — Management GUI

With the authorized Growth Tool or Broken Tool in the main hand, an air right-click that does not
target a block or entity opens the management GUI. Off-hand use does not open it.

The GUI exposes status, material, evolution, cumulative and next-threshold progress, enchantments,
branch, durability, repair preview, and configuration clamp information, and provides Repair and
Help/Status actions. Exact language, layout, slot assignment, item name, and lore are not normative
acceptance obligations for this scope, but the required information and actions must be clear.

### CAN-MAIN-015 — Full repair and transaction behavior

Only full repair is offered.

- Full repair base: `ceil(100 × (1 + evolution_count × 0.08))`
- Active repair: `ceil(full_repair_cost × max(0.25, missing_durability_ratio))`
- Broken repair: `full_repair_cost + 100 + evolution_count × 5`

A fully durable active item is not repaired and costs 0 WM. Repair uses the Core transaction boundary,
explicit confirmation, transaction identity, idempotency, player/tool serialization, no duplicate
debit/refund, compensation for a clear downstream failure, and manual reconciliation for `UNKNOWN`.

### CAN-MAIN-016 — Player-paid reissue

A player may request a replacement for a missing Growth Tool through an explicit quote and confirm
flow.

The reissue price is:

`broken_repair_cost + full_repair_cost`

A successful paid reissue:

- preserves the logical tool, cumulative progress, and active branch;
- produces a new physical `item_instance_id`;
- increments the epoch and invalidates old physical items;
- sets the logical tool to `ACTIVE`;
- delivers a fully repaired physical item immediately or through typed pending delivery.

If an authorized current physical item or an existing pending delivery exists, paid reissue is
rejected before debit and the player is directed to the free delivery-retry path. Replay, double
confirmation, or `UNKNOWN` must not cause duplicate debit or duplicate authority rotation.

### CAN-MAIN-017 — Session and checkpoint behavior

Normal progress may accumulate in a session cache. Critical changes, including evolution, broken,
repair, reissue, and authorized administrative modification, are persisted promptly.

At minimum, periodic checkpoint, quit, and disable trigger a bounded asynchronous flush. Same-player
operations are serialized. Obsolete callbacks are rejected. A crash may lose no more than the
documented normal-progress checkpoint window.

### CAN-MAIN-018 — Administration and permissions

Required administrative capabilities include inspect, grant, administrative reissue, repair, branch,
revoke, reconcile, and pending-delivery retry.

Approved permission groups are:

- `wayfarer.main.use`
- `wayfarer.main.admin.read`
- `wayfarer.main.admin.delivery`
- `wayfarer.main.admin.modify`
- `wayfarer.main.admin.reconcile`
- `wayfarer.main.debug`

An optional umbrella node may grant groups, but each command or gameplay handler directly enforces the
applicable group. Debug actions are disabled by default and require both configuration enablement and
`wayfarer.main.debug`.

## 7. Wayfarer_Frontier shared requirements source

### CAN-FRONTIER-001 — Deployment and lifecycle

Wayfarer_Frontier activates only on the Frontier backend, requires Wayfarer_Core, is not placed on
Main or Lobby, and does not generate Worlds Beyond worlds.

### CAN-FRONTIER-002 — Exact Worlds Beyond boundary

The only Worlds Beyond gameplay world is exact `frontier_iris`. Similar names, unknown worlds, all
Nether worlds, and all End worlds are excluded. Initial loadout, theme-bound item use, navigation,
launchpad, shop, portal policy, and future theme behavior fail closed outside the exact world.

### CAN-FRONTIER-003 — MVI authority

The Project MVI group set is `neutral`, `worlds_beyond`, and `guild`.
`frontier_iris` belongs only to the `worlds_beyond` MVI group. Wayfarer_Frontier does not save or restore
normal player state, switch MVI profiles, duplicate Gate/respawn/reconnect transitions, or share
profiles with Main. It owns only its typed item identity, pending delivery, launchpad, shop, and
future explicitly approved domains.

### CAN-FRONTIER-004 — Frontier persistence

Frontier durable state covers typed traversal identities, initial and pending delivery, launchpad
authority/history, shop pending delivery, and necessary placement/transaction records. It does not
store raw normal inventory or raw player profiles. Its migration ownership must permit later
forward-only addition of explicitly approved Frontier domains without making incomplete Waystone
scaffolding a current runtime authority.

### CAN-FRONTIER-005 — Frontier permissions

Approved permission groups are:

- `wayfarer.frontier.use`
- `wayfarer.frontier.admin.read`
- `wayfarer.frontier.admin.delivery`
- `wayfarer.frontier.admin.launchpad`
- `wayfarer.frontier.admin.reconcile`
- `wayfarer.frontier.debug`

An optional umbrella node may grant groups, but each handler directly enforces the applicable group.
Debug actions require both configuration enablement and the debug permission.

## 8. Worlds Beyond requirements source

### CAN-WB-001 — Initial safe-entry loadout

On the first safe entry into exact `frontier_iris`, the player receives:

Permanent items:
- one unbreakable, owner-bound Elytra;
- one owner-bound authentic LeafGrapple hook configured without durability and without entity/player
  hooking;
- one owner-bound Navigation item.

Consumable item:
- two Launchpads, granted only as the initial allocation.

Repeated entry does not duplicate delivered items.

### CAN-WB-002 — Safe-entry delivery

Delivery record retrieval/creation is asynchronous. Before item mutation, the player is revalidated as
online and still inside exact `frontier_iris`.

Inventory-full or capability-unavailable delivery does not drop an item. The undelivered typed item
remains pending. The player receives an actionable notification. Conflict or unknown outcomes do not
auto-retry and direct the player to administrative review. Offline or theme-left outcomes preserve
pending state without attempting player notification. Sanitized audit/console reporting is used only
as necessary and must not duplicate existing audit semantics.

### CAN-WB-003 — Permanent item authority

Permanent traversal items are owner-bound, theme-bound, and identified by MariaDB plus PDC. Other
players cannot use, equip, pick up, or store them. Manual drop and container storage are denied.
Reissue invalidates stale epochs when authority is rotated.

### CAN-WB-004 — Permanent item death recovery

Elytra, Grappling Hook, and Navigation item are removed from death drops. Death creates durable typed
pending delivery rather than preserving a raw ItemStack in process memory.

On a later exact Worlds Beyond safe entry, the item is reconstructed and delivered free of charge
while preserving the same logical identity, physical instance identity, and epoch. Restart between
death and redelivery must not lose the pending obligation or create a duplicate.

Launchpads, rockets, and other consumables do not receive this free permanent-item recovery.

### CAN-WB-005 — Elytra behavior

The issued Elytra is unbreakable, owner-bound, usable only in exact `frontier_iris`, and compatible
with natural transition from hook or launchpad movement into gliding.

### CAN-WB-006 — LeafGrapple integration

LeafGrapple version `1.0.2` is the adopted capability. The integration creates the authentic hook,
checks version and required capabilities, applies Wayfarer owner/theme identity, and verifies a
configuration with durability disabled and entity/player hooking disabled.

Wayfarer does not reimplement hook projectile behavior, pull physics, or LeafGrapple cooldown
calculation. Unsupported or unsafe integration fails closed. Any non-public dependency is isolated
behind a version adapter and recorded as a limitation.

### CAN-WB-007 — Navigation

The Navigation item opens a theme GUI with at least Shop, Loadout, and Help. When Waystone is absent,
Discovery, Teleport, and the Waystone Placement Tool are hidden, disabled, or clearly unavailable and
cannot succeed. Navigation operations are rejected outside exact `frontier_iris`.

Exact language and layout are not normative acceptance obligations for this scope.

### CAN-WB-008 — Launchpad item and initial balance

A Launchpad item has typed identity including item type, physical instance, definition, and schema.
It is a normal theme consumable and may be lost on death. Remaining uses are not stored on the
unplaced item. A successful placement consumes exactly one item; a failed placement consumes none.

Initial configurable defaults:

| Parameter | Value |
|---|---:|
| Shop price | 30 WM |
| Purchase amount | 1 |
| Initial free amount | 2 |
| Maximum successful uses | 3 |
| Expiration | 30 days without successful use |
| Horizontal velocity | 2.5 |
| Vertical velocity | 1.2 |
| Cooldown | 2 seconds |
| Auto Elytra | true |
| Maximum active per player | 0 (unlimited) |

### CAN-WB-009 — Launchpad placement

The initial physical block is `LIGHT_WEIGHTED_PRESSURE_PLATE`. Placement requires exact
`frontier_iris`, a solid supporting top surface, air at the target, no liquid, world-border inclusion,
a loaded chunk, spawn exclusion, WorldGuard permission, and no active launchpad overlap.

Dedicated generic Portal, Gate, Waystone, or System-Structure exclusion is not a V0.0.2 requirement.
The item, durable record, and physical block must use a compensatable flow that prevents silent
double consumption or authoritative orphaning.

### CAN-WB-010 — Launchpad creation and live configuration

Durable authority records creation identity, definition/schema, placement location, placer, successful
use count, maximum uses at creation, creation/last-use/expiration timestamps, state, and optimistic
lock.

Maximum uses and expiration-related authority are creation-time durable values. Launch velocity,
cooldown, and auto-Elytra behavior use the current approved configuration at use time. Placement yaw
is not authoritative for launch direction; an orientation field, if retained for compatibility, is
non-authoritative metadata.

### CAN-WB-011 — Launchpad use

Stepping on an active launchpad triggers public use unless the player is sneaking. Launch direction is
derived from the user's current look direction at activation, combined with the configured horizontal
and vertical launch behavior.

A successful launch:

- acquires the launchpad use claim/lock;
- passes cooldown and safe-launch checks;
- prevents block embedding;
- applies auto-Elytra behavior when enabled;
- increments successful use count exactly once;
- updates `last_used_at` and extends expiration;
- removes the launchpad at the maximum-use boundary.

The feature is unavailable outside exact `frontier_iris`.

### CAN-WB-012 — Launchpad break and environmental protection

Any player may normally break an active launchpad. A successful break yields no item, removes the
physical block and active authority once, and is auditable. A cancelled or duplicate break does not
remove authority twice.

Active launchpad positions are protected, through supported platform/plugin contracts, from explosion,
fire/burn, fluid, piston, entity block change, falling block, block spread, tree/mushroom growth,
structure generation, supported WorldEdit/FAWE editing, and mob griefing.

### CAN-WB-013 — Launchpad expiration and reconciliation

Expiration is based on `last_used_at` after at least one successful use and otherwise on
`created_at`. Expiration candidates are searched asynchronously and physical mutation occurs on the
main thread. Restart catch-up is supported. Removal for maximum uses, expiration, successful player break,
authorized administration, or reconciliation is idempotent.

A durable launchpad record whose physical block is missing is detectable and reconcilable. The product
is not required to discover an arbitrary `LIGHT_WEIGHTED_PRESSURE_PLATE` with no durable launchpad
record or to treat it as a launchpad; such a block remains an ordinary physical block.

### CAN-WB-014 — Frontier shop

Inside exact `frontier_iris`, the shop sells:

- Launchpad ×1 for 30 WM;
- Flight Duration 3, non-explosive Firework Rocket ×1 for 200 WM.

Waystone Placement Tool is not sold while Waystone is unavailable, and any direct request is rejected
before debit.

Shop operations use the Core transaction boundary, transaction identity, idempotency, duplicate-click
protection, typed pending delivery or refund on clear delivery failure, audit, and `UNKNOWN` handling.

### CAN-WB-015 — Portal denial

Vanilla portal traversal from or within exact `frontier_iris` is denied. This requirement does not
authorize Wayfarer_Frontier to implement world generation, Gate lifecycle, or an in-world return
structure.

### CAN-WB-016 — Administration

Required administrative capabilities include loadout inspection/reissue, delivery inspection/retry,
launchpad inspection/removal/reconciliation, transaction inspection, and audit reference, controlled
by the Frontier permission groups in `CAN-FRONTIER-005`.

## 9. Explicit non-scope

### CAN-SCOPE-001 — Main non-scope

This source does not require Axe, Shovel, player-paid Fortune/Silk branch switching, Netherite
upgrade, ranking, evolution rewards, abilities, cosmetics, cross-server Growth Tool use, or a ban on
vanilla tools.

### CAN-SCOPE-002 — Frontier/Worlds Beyond non-scope

This source does not require full Waystone lifecycle, discovery/teleport behavior, Waystone Placement
Tool sales, Ruined Frontier gameplay, EliteMobs gameplay, MVI profile implementation, Gate
implementation, Iris world generation, Frontier resource-pack build/delivery, or a separate
EliteMobs-MVI adapter.

### CAN-SCOPE-003 — Conditional adapter prohibition

A separate `Wayfarer_Frontier_EliteMobsMVI` adapter is not created or released unless a later
authoritative decision explicitly requires it.

### CAN-SCOPE-004 — Process material excluded from software source

Build procedure, test repetition policy, PR status, release sequencing, stable-tag publication,
artifact handoff, Project runtime deployment, roadmap order completion, and future version allocation
are governed separately and are not software behavior clauses in this canonical source.

## 10. Source-level unresolved questions

The following matters remain intentionally unresolved and must be carried into SWE.1 issue analysis:

1. Required behavior when `frontier_iris` does not exist when Wayfarer_Frontier enables.
2. Exact supported external-repair integration boundary for Growth Tool.
3. Exact public/non-public LeafGrapple 1.0.2 API and a deployable safe tier/configuration.
4. Supported WorldEdit/FAWE protection boundary and the treatment of tools that bypass public hooks.
5. Durable identification of an existing launchpad if the configured physical material changes.
6. The authoritative non-portal return mechanism available while vanilla portals are denied.
7. The allowed invocation context for player-paid Growth Tool reissue.
8. Exact mapping of administrative commands to the approved permission groups.
9. Whether a documented normal-progress checkpoint-loss bound must be fixed at five minutes or remain
   configurable with a declared maximum.
