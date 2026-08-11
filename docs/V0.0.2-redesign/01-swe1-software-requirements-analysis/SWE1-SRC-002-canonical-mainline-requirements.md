# Project Wayfarer Plugin Mainline Canonical Requirements Source

Document ID: `SWE1-SRC-002`  
Revision: C  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-12 JST  
Author: ChatGPT  
Reviewer: Project Owner  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Predecessor sources:
- `Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED.md`
- `Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register.md`
Controlling joint-review decisions:
- `DEC-REQ-002` — approved Common corrections for `CAN-COM-001` through `CAN-COM-005`
- `DEC-REQ-004` — approved Common corrections for `CAN-COM-006` through `CAN-COM-010`
- `DEC-REQ-005` — approved Core corrections for `CAN-CORE-001` through `CAN-CORE-005`

## 1. Purpose and authority

This document is the normalized positive-requirement source used before SWE.1 requirement decomposition. It combines the mainline requirement source with only those later Owner decisions that clarified a requirement or resolved a contradiction in that source.

Revision C integrates the completed joint Owner review of the Common (`CAN-COM-001` through `CAN-COM-010`) and Core (`CAN-CORE-001` through `CAN-CORE-005`) canonical sections. Main, Frontier, Worlds Beyond, and Scope clauses remain subject to their own clause-by-clause review; conflicting target-specific wording identified for later propagation is not silently changed before that review.

This document deliberately excludes:

- PR, branch, commit, CI, candidate, and implementation-status information;
- statements whose authority is only the behavior of the abandoned PR #14 implementation;
- the previous implementation sequence, pre-client roadmap, release roadmap, and next-action plan;
- test-execution history and claims that a behavior was already implemented;
- future-version scheduling and version-number reservations;
- AMD-012, because it distinguishes candidate preparation from stable release rather than changing software behavior.

Source clause identifiers beginning with `CAN-` are provenance anchors only. They are not SWE.1 software-requirement identifiers.

## 2. Source provenance

| Source | SHA-256 | Use in this canonical source |
|---|---|---|
| Mainline requirement source | `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F` | Base authority for product behavior and constraints |
| Requirement/implementation delta register | `A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1` | Only explicit Owner requirement amendments and clarifications; implementation and roadmap content excluded |
| Owner instruction dated 2026-08-05 | Repository decision `DEC-REQ-001` | Controls the merge and analysis method |
| Common joint review through CAN-COM-005 | Repository decision `DEC-REQ-002` | Controls integrated Common corrections 001–005 |
| Common joint review through CAN-COM-010 | Repository decision `DEC-REQ-004` | Controls integrated Common corrections 006–010 |
| Core joint review through CAN-CORE-005 | Repository decision `DEC-REQ-005` | Controls integrated Core corrections 001–005 |

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

The delta register's proposed code changes, current implementation descriptions, test plans, unresolved implementation gates, and release sequencing are not imported as requirements.

## 4. Common system boundaries

### CAN-COM-001 — Capability ownership and deployment-topology independence

Wayfarer-owned software separates shared and feature capability ownership from physical server placement. A fixed server identity, server name, or historical Project backend role is not a general prerequisite for software operation merely because the current integration topology places a capability on one server.

Artifact placement and capability composition are controlled by approved integration/deployment configuration. A capability may become unavailable when its actual functional prerequisites are absent, including required configuration, schema compatibility, approved capability contracts, or required world/content context. The software shall not unnecessarily prevent future server consolidation, division, or co-location of multiple Wayfarer capabilities.

### CAN-COM-002 — Dependency direction and shared-contract access

Feature capabilities consume Wayfarer-owned shared capabilities only through public contracts of software units explicitly assigned shared ownership. A feature plugin does not depend on another feature plugin's internal implementation.

A software unit assigned shared-foundation ownership does not acquire a feature-specific dependency that reverses the approved shared-to-feature layering, and the Wayfarer software dependency graph remains acyclic.

### CAN-COM-003 — State authority and access boundaries

Authority, ownership, and access mechanism are distinct:

- Wayfarer-owned durable business state uses MariaDB as its durable authority unless a later approved requirement assigns another authority.
- Redis may provide cache, locks, coordination, pub/sub, messaging, or equivalent transient assistance but is not the sole durable authority for Wayfarer-owned business state; recoverable cached state is reconstructable from its authority.
- MVI remains authoritative for normal player-state domains assigned to it, and Wayfarer does not silently substitute its own persistence for that authority.
- Waymark balance authority remains the approved economy provider; Wayfarer feature capabilities use an approved shared transaction/provider contract rather than provider-internal state.
- Minecraft owns runtime world, block, entity, inventory-item, and related physical state; Wayfarer accesses or mutates that state through approved platform/adopted-plugin contracts.
- A feature may combine Wayfarer-owned logical authority with Minecraft-owned physical state when the owning feature defines mismatch detection and reconciliation.

### CAN-COM-004 — Prohibited ownership and unsupported access

Wayfarer-owned software does not use another product's private database, undocumented storage, internal cache, implementation-specific state, or equivalent unsupported internals as a normal integration contract. Supported public APIs, events, provider contracts, platform-visible state, and explicitly approved adapters may be used within their documented guarantee boundaries.

Wayfarer does not act as the general or long-term authority for normal player inventory/profile state. A later explicitly approved cross-context item-transfer capability may temporarily capture, persist, transform, and redeliver approved item classes through a controlled transaction without becoming a general inventory-storage service; such a capability requires its own approved scope, requirements, duplication/loss controls, audit, compensation, and reconciliation.

Unsupported/non-public APIs are not adopted as Product dependencies without an explicit Owner-approved exception that fixes the compatibility range, necessity, isolation boundary, failure behavior, and limitation. Approved exceptions are isolated behind replaceable adapters and fail closed outside the approved compatibility range.

A software unit assigned shared-foundation ownership does not own policy or gameplay semantics that apply only to a specific feature domain unless an approved architecture allocation explicitly assigns that responsibility to the shared unit. Shared units may provide reusable mechanisms/contracts without embedding feature-specific decisions.

### CAN-COM-005 — Threading and execution-context boundary

Minecraft runtime state and event-control operations are accessed or mutated only from execution contexts authorized by the adopted server-platform contract; the requirement does not assume one global main thread when the platform defines a more specific model.

Blocking or completion-waiting database, Redis, filesystem, network, or durable-audit I/O does not execute on tick-critical or region-critical server execution contexts. Bounded in-memory enqueueing alone is not treated as blocking durable I/O.

After asynchronous/deferred work completes, the software returns to an execution context authorized for the affected runtime state and revalidates every mutable precondition required by the operation before applying a protected mutation, including applicable lifecycle validity, subject identity, online state, location/content context, and item/authority identity.

### CAN-COM-006 — Capability lifecycle and fail-closed behavior

Each gameplay capability is available only while every mandatory prerequisite assigned to that capability remains valid. Mandatory prerequisites may include approved configuration, compatible schema state, required approved shared/external capability contracts, and target-specific world or content-context conditions. A fixed physical server identity, historical backend role, or specific shared-provider implementation is not a general prerequisite unless a target requirement makes it an actual external contract.

When a mandatory prerequisite becomes invalid, or when a capability/plugin begins disablement or replacement, the affected capability becomes unavailable before accepting any new capability operation or producing any new capability-owned state change. Other capabilities may remain available only while their own prerequisites remain valid. Whole-plugin unavailability is required when the failed prerequisite applies to all of that plugin's runtime capabilities or when a target requirement explicitly requires it.

Every operation already accepted before admission closure reaches, within a finite lifecycle deadline, one of: completed; safely rejected without protected effect; compensated; or explicitly unresolved but durably recoverable. The software does not wait indefinitely and does not silently discard incomplete durable work.

A completion originating from an earlier or disabled lifecycle instance does not mutate current or disabled runtime state unless the current lifecycle explicitly recognizes the operation as still valid. All applicable asynchronous-completion preconditions are then revalidated before mutation.

### CAN-COM-007 — Protected-operation identity, replay, and ambiguous outcomes

Each protected financial, compensation, durable item-entitlement/delivery, and authority-rotation operation establishes a stable logical operation identity and recoverable operation disposition before its first protected effect is invoked or committed. Each protected effect is distinguishable by effect kind and correlated with the logical operation and any available provider transaction reference.

Replay of an accepted operation resolves to the existing operation and recorded effect dispositions; it does not create a new logical operation or duplicate protected effect. A stale request that was not previously accepted and no longer satisfies its current target-specific prerequisites is rejected before protected effect. Wayfarer prevents duplicate effects caused by its own invocation, replay, recovery, and reconciliation behavior without claiming guarantees stronger than the external provider contract. A durable item-delivery retry may continue only the same established entitlement when prior final delivery has not been established.

When a protected effect cannot be proven as success or clear no-effect failure, that effect is represented as `UNKNOWN`. `UNKNOWN` is neither success nor clear failure and does not authorize automatic retry, automatic compensation, success-dependent continuation, paid-benefit completion, delivery-entitlement creation, or authority rotation. Resolution requires authoritative evidence or authorized reconciliation linked to the exact operation/effect and does not re-invoke the uncertain effect.

A paid benefit is finalized only after debit success is proven. Automatic refund/compensation requires proven debit success plus proven absence or clear failure of the downstream paid benefit. Compensation has its own stable effect identity and follows the same duplicate-effect and `UNKNOWN` rules.

### CAN-COM-008 — Durable-schema ownership and migration compatibility

Each Wayfarer-owned durable-state domain has one explicitly assigned software owner. Schema objects and schema evolution remain attributable to that owner, and one owner does not create, alter, drop, or repurpose schema objects owned by another owner.

Each durable-state owner has an independently identifiable migration sequence and applied-migration state. Physical database namespace, table naming, migration-resource location, migration executor, and migration-history storage are downstream architecture/design choices except where an accepted baseline fixes an identifier or artifact that must remain compatible.

A migration included in an accepted Product baseline, or that may already have been applied to a supported installation, retains its migration identity, ordering semantics, and content. Later schema evolution uses a new forward migration rather than modifying, deleting, reusing, or repurposing accepted migration history.

The current required schema is constructible from an empty database and upgradeable from the accepted V0.0.1 database baseline without destructive automatic reset or silent loss of accepted durable state. Controlled data transformation is permitted when it preserves defined authority and resulting-state semantics. Failure to establish required schema compatibility leaves affected capabilities unavailable under `CAN-COM-006`.

### CAN-COM-009 — Auditable protected outcomes and sensitive-data safety

Wayfarer-owned software retains retrievable audit evidence for designated security-sensitive, financial, durable-delivery, recovery, administrative, reconciliation, and later explicitly designated protected outcomes. Evidence is sufficient to correlate the operation/event, applicable actor and subject, occurrence time, recorded disposition, and operationally relevant reason/classification; protected operations are correlatable with their logical operation/effect identities.

Evidence needed for supported inspection or reconciliation remains retrievable across normal runtime restart for the applicable operational retention period. Existing durable transaction, domain, or reconciliation records may satisfy this requirement; a duplicate dedicated audit store is not required.

Audit evidence is not treated as proof of a stronger domain/provider/physical effect than the corresponding authoritative source establishes. An `UNKNOWN` outcome remains `UNKNOWN` unless resolved by authoritative evidence or authorized reconciliation.

Audit and diagnostic evidence applies data minimization. Secret material, raw credentials, authentication tokens, and equivalently protected values are not recorded. Personal or incidental runtime data is retained only when necessary for approved attribution, correlation, disposition analysis, recovery, or reconciliation; stable actor/subject identifiers may be retained when required for those purposes.

### CAN-COM-010 — Capability reuse and ownership non-duplication

Wayfarer-owned software does not take unnecessary ownership of a generic capability when an approved Java/platform/adopted-plugin/external-library contract can adequately satisfy the applicable functional, compatibility, lifecycle, threading, security, licensing, operational, and maintainability requirements.

Project-owned implementation of an equivalent capability remains permitted when an identified approved requirement or design constraint cannot be adequately satisfied by the available external capability, or when Project ownership materially reduces relevant Product/integration risk.

The exact capability allocation, dependency/library selection, API contract, version, adapter boundary, alternatives assessment, and authoritative version-appropriate references are controlled under `GOV-ENG-001` during SWE.2/SWE.3.

## 5. Wayfarer_Core requirements source

### CAN-CORE-001 — Accepted V0.0.1 Core contract compatibility

The accepted V0.0.1 Core public contract identified by the controlled accepted-baseline inventory remains backward-compatible. A conforming V0.0.1 consumer remains able to use the accepted public contract without incompatible source-use, binary-linkage, or documented behavioral-contract change.

Compatibility applies to accepted public contract surfaces and their documented guarantees. It does not freeze Core internal implementation structure, implementation-specific dependencies, or undocumented implementation behavior. V0.0.1 implementation locations may be refactored, corrected, or replaced when the accepted contract remains compatible.

When an approved capability is explicitly allocated to Core and requires a new public contract, that contract extends the accepted baseline without incompatibly removing, renaming, retyping, or changing the documented semantics of accepted contract elements. Existing elements may be deprecated while remaining compatible.

A baseline-breaking change is permitted only through an explicit Owner-approved baseline-change decision identifying the affected contract surface and version/transition scope. Accepted contract elements outside that approved scope remain subject to compatibility requirements.

The accepted V0.0.1 Core migration baseline remains protected by the Common migration-compatibility requirements and `CAN-CORE-005`.

### CAN-CORE-002 — Stable public-contract abstraction and runtime type identity

Core-owned public contracts use stable contract-owned types or explicitly approved external/platform contract types and do not expose Core implementation-specific persistence, migration, cache, provider-client, execution, or other internal implementation types or raw internal resource/authority-access handles.

An external or platform-specific type may form part of a Core public contract only when that type is required by the approved capability contract and its compatibility, lifecycle, ownership, and execution-context implications are acceptable. Implementation convenience alone is not sufficient reason to expose an internal or runtime-specific type.

Within one in-process Core public-contract compatibility domain, participating software units resolve compatible runtime type identity for the contract types they exchange. Packaging or class-loading does not introduce independent duplicate definitions that cause service resolution, type checking, casting, callback, or equivalent contract failure.

Intentionally isolated contract versions may coexist only when their type identities do not cross the same public-contract boundary and an approved compatibility or adapter boundary defines their interaction.

### CAN-CORE-003 — V0.0.2 shared Waymark transaction contract and provider guarantee boundary

For V0.0.2, Core owns and provides the compatibility-preserving shared Waymark transaction contract used by approved Wayfarer capabilities requiring financial effects, compensation, inspection, or reconciliation. This V0.0.2 allocation does not make Core the permanent owner of every future shared transaction implementation and does not override the Common shared-capability allocation rules.

The Core transaction contract owns shared transaction coordination, operation/effect correlation, provider interaction disposition, and shared reconciliation support. Feature-specific eligibility, paid-benefit semantics, domain mutation, item entitlement, delivery, and equivalent feature policy remain authoritative in the applicable feature owner unless explicitly reallocated by an approved requirement.

Waymark balance authority remains the approved economy provider. Core interprets provider evidence only within guarantees supplied by the approved provider contract and does not infer durable effect completion, provider-side effect lookup, external operation identity, atomic multi-effect execution, or exactly-once semantics unless the provider contract explicitly supplies that guarantee.

Wayfarer-owned transaction records may authoritatively record Wayfarer's logical operation and effect dispositions, but they do not replace provider balance authority or independently prove a feature-owned domain effect. Protected-operation identity, replay, `UNKNOWN`, compensation, and duplicate-effect behavior follow the Common protected-operation requirements.

### CAN-CORE-004 — Ambiguous Waymark effect containment and no manufactured provider semantics

When the outcome of an individual Waymark provider effect is not established by evidence guaranteed for that effect by the approved provider contract, the Core transaction boundary preserves the applicable Common `UNKNOWN`, replay, and reconciliation semantics.

A before/after balance observation, balance difference, or other aggregate provider state that is not contractually correlated to the exact provider effect does not by itself establish that the effect succeeded, clearly failed without effect, or was not applied. Such state may be used for eligibility, diagnostics, or reconciliation context without being promoted to exact-effect proof.

Core does not access or mutate unsupported provider internals, create an unsupported provider-side or cross-channel marker, or otherwise introduce a Wayfarer-specific mechanism in order to claim effect lookup, provider-side operation identity, atomicity, exactly-once behavior, durable completion evidence, or another guarantee that the approved provider contract does not supply.

Wayfarer-owned operation, effect, audit, and reconciliation records are permitted and required where applicable, but they represent Wayfarer's own knowledge and disposition and do not by themselves strengthen the authoritative guarantee of the provider effect. A stronger provider capability may be used when it is supplied through an approved supported provider contract and is interpreted only within its documented guarantee boundary.

### CAN-CORE-005 — Core durable-schema evolution and accepted migration preservation

Schema evolution attributed to Core is introduced only for an approved change, correction, integrity requirement, or compatibility requirement of a durable-state domain allocated to Core. A Core migration is not used to create, alter, or carry durable state owned by another Wayfarer durable-state owner merely because Core provides shared database or migration infrastructure.

The accepted V0.0.1 Core migrations identified by the controlled accepted-baseline inventory retain their migration identity, ordering semantics, and exact artifact content. They are not modified, deleted, reordered, reused, or repurposed after acceptance or possible application to a supported installation.

A later correction or schema evolution uses a new Core-owned migration identity. This requirement does not fix the future migration framework, resource location, executor, namespace convention, or physical migration-history mechanism, provided that the accepted migration history and supported V0.0.1 upgrade path remain compatible.

## 6. Wayfarer_Main requirements source

### CAN-MAIN-001 — Deployment and lifecycle

Wayfarer_Main activates only on the Main backend, requires Wayfarer_Core, validates its dependencies and schema before activation, and fails closed when a mandatory prerequisite is unavailable.

### CAN-MAIN-002 — Logical Growth Tool authority

MariaDB owns one logical `PICKAXE` per `owner_uuid + tool_type`.

The logical record supports:

- tool status: `ACTIVE`, `BROKEN`, `REVOKED`;
- delivery status: `DELIVERED`, `PENDING`;
- active branch: `FORTUNE`, `SILK_TOUCH`;
- stable identity, owner, epoch, cumulative fixed-point progress, stored damage, schema version, optimistic-lock version, timestamps, and checkpoint timestamp.

### CAN-MAIN-003 — Physical identity

A physical Growth Tool or Broken Tool carries PDC sufficient to identify item type, physical instance, logical tool, owner, tool type, epoch, schema, and display revision. Display name, lore, and material are not sufficient identity.

Unknown type/schema, malformed identity, wrong owner, wrong logical tool, and stale epoch are denied.

### CAN-MAIN-004 — Initial asynchronous delivery

On Main join, the plugin asynchronously reads or race-safely creates the logical record, then revalidates the player's online state before main-thread delivery.

A new player receives one tool. Rejoin does not duplicate it. Inventory-full delivery does not drop the item; it remains pending, the player is notified, the reason is audited, and retry is possible on a later join or authorized administrative action. A delivered record is not automatically reissued.

### CAN-MAIN-005 — Owner binding and inventory restrictions

Only the current owner may use or progress the current physical instance. Manual drop, other-player pickup, container storage, anvil, grindstone, smithing, crafting repair, same-tool combination, Mending, item-frame/armor-stand placement, and other supported transfer/repair paths are denied. Supported external repair integration must not bypass the same authority.

### CAN-MAIN-006 — Death behavior

The Growth Tool or Broken Tool is removed from death drops. It is not stored as a raw in-memory ItemStack for automatic respawn restoration, and it is not automatically restored on respawn. The logical tool record remains authoritative.

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

Player-placed, generator-created, re-placed Silk Touch ore, and normally broken plugin-generated blocks are eligible. Creative, Spectator, cancelled breaks, explosions, pistons, commands, WorldEdit/FAWE removal, and other non-player-break removal are not eligible.

### CAN-MAIN-009 — Progress representation and saturation

`1.000` progress equals `1000` internal integer units. Positive addition saturates at `Long.MAX_VALUE`, never wraps negative, and becomes a no-op after saturation. Threshold evaluation, GUI display, checkpointing, and configuration reconciliation must remain defined at the saturated value. Detailed per-addition overflow audit is not required.

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

Material progression is Wood → Stone → Iron → Diamond at cumulative progress 100, 400, and 1200. After Diamond, the initial increment for enchantment evolution number `n`, beginning at `n=1`, is `800 + 200n + 40n²`.

The repeating cycle is Efficiency, Unbreaking, Efficiency, Unbreaking, Fortune. Effective caps are:

- Efficiency 10
- Unbreaking 10
- Fortune 5
- Silk Touch 1

Conceptual level, progress, and evolution count continue after effective caps. Default branch is `FORTUNE`; authorized administration may select `FORTUNE` or `SILK_TOUCH`.

### CAN-MAIN-012 — Threshold and configuration reconciliation

Threshold evaluation is deterministic and supports the full valid progress range. Configuration is applied as an internally consistent revision.

On configuration reconciliation:

- cumulative progress is unchanged;
- material, enchantments, and evolution count are recomputed;
- promotion and demotion are permitted;
- reconciliation alone does not repair the item;
- active durability ratio is preserved across material change, with at least one durability point;
- a broken tool remains broken.

Only a real progress addition that increases evolution count restores the item to full durability.

### CAN-MAIN-019 — Normal durability behavior

An authorized active Growth Pickaxe follows the approved Paper/vanilla nonterminal durability result during ordinary use. Managed-item handling does not suppress normal durability loss merely because the item is authorized. The explicit exceptions are an evolution-triggered full recovery, denied or cancelled use, controlled repair/reissue, and terminal-damage conversion to the Broken state.

### CAN-MAIN-013 — Broken state

Before vanilla item disappearance at terminal durability, the physical representation becomes `GRAY_DYE` with logical status `BROKEN`.

Identity, owner, tool type, epoch, progress, branch, and schema remain associated. A broken tool cannot mine, progress, or use external repair; it remains owner-bound, can open the management GUI, survives restart, and is checkpointed as a critical state.

### CAN-MAIN-014 — Management GUI

With the authorized Growth Tool or Broken Tool in the main hand, an air right-click that does not target a block or entity opens the management GUI. Off-hand use does not open it.

The GUI exposes status, material, evolution, cumulative and next-threshold progress, enchantments, branch, durability, repair preview, and configuration clamp information, and provides Repair and Help/Status actions. Exact language, layout, slot assignment, item name, and lore are not normative acceptance obligations for this scope, but the required information and actions must be clear.

### CAN-MAIN-015 — Full repair and transaction behavior

Only full repair is offered.

- Full repair base: `ceil(100 × (1 + evolution_count × 0.08))`
- Active repair: `ceil(full_repair_cost × max(0.25, missing_durability_ratio))`
- Broken repair: `full_repair_cost + 100 + evolution_count × 5`

A fully durable active item is not repaired and costs 0 WM. Repair uses the Core transaction boundary, explicit confirmation, transaction identity, idempotency, player/tool serialization, no duplicate debit/refund, compensation for a clear downstream failure, and manual reconciliation for `UNKNOWN`.

### CAN-MAIN-016 — Player-paid reissue

A player may request a replacement for a missing Growth Tool through an explicit quote and confirm flow.

The reissue price is:

`broken_repair_cost + full_repair_cost`

A successful paid reissue:

- preserves the logical tool, cumulative progress, and active branch;
- produces a new physical `item_instance_id`;
- increments the epoch and invalidates old physical items;
- sets the logical tool to `ACTIVE`;
- delivers a fully repaired physical item immediately or through typed pending delivery.

If an authorized current physical item or an existing pending delivery exists, paid reissue is rejected before debit and the player is directed to the free delivery-retry path. Replay, double confirmation, or `UNKNOWN` must not cause duplicate debit or duplicate authority rotation.

### CAN-MAIN-017 — Session and checkpoint behavior

Normal progress may accumulate in a session cache. Critical changes, including evolution, broken, repair, reissue, and authorized administrative modification, are persisted promptly.

At minimum, periodic checkpoint, quit, and disable trigger a bounded asynchronous flush. Same-player operations are serialized. Obsolete callbacks are rejected. A crash may lose no more than the documented normal-progress checkpoint window.

### CAN-MAIN-018 — Administration and permissions

Required administrative capabilities include inspect, grant, administrative reissue, repair, branch, revoke, reconcile, and pending-delivery retry.

Approved permission groups are:

- `wayfarer.main.use`
- `wayfarer.main.admin.read`
- `wayfarer.main.admin.delivery`
- `wayfarer.main.admin.modify`
- `wayfarer.main.admin.reconcile`
- `wayfarer.main.debug`

An optional umbrella node may grant groups, but each command or gameplay handler directly enforces the applicable group. Debug actions are disabled by default and require both configuration enablement and `wayfarer.main.debug`.

## 7. Wayfarer_Frontier shared requirements source

### CAN-FRONTIER-001 — Deployment and lifecycle

Wayfarer_Frontier activates only on the Frontier backend, requires Wayfarer_Core, is not placed on Main or Lobby, and does not generate Worlds Beyond worlds.

### CAN-FRONTIER-002 — Exact Worlds Beyond boundary

The only Worlds Beyond gameplay world is exact `frontier_iris`. Similar names, unknown worlds, all Nether worlds, and all End worlds are excluded. Initial loadout, theme-bound item use, navigation, launchpad, shop, portal policy, and future theme behavior fail closed outside the exact world.

### CAN-FRONTIER-003 — MVI authority

The Project MVI group set is `neutral`, `worlds_beyond`, and `guild`. `frontier_iris` belongs only to the `worlds_beyond` MVI group. Wayfarer_Frontier does not save or restore normal player state, switch MVI profiles, duplicate Gate/respawn/reconnect transitions, or share profiles with Main. It owns only its typed item identity, pending delivery, launchpad, shop, and future explicitly approved domains.

### CAN-FRONTIER-004 — Frontier persistence

Frontier durable state covers typed traversal identities, initial and pending delivery, launchpad authority/history, shop pending delivery, and necessary placement/transaction records. It does not store raw normal inventory or raw player profiles. Its migration ownership must permit later forward-only addition of explicitly approved Frontier domains without making incomplete Waystone scaffolding a current runtime authority.

### CAN-FRONTIER-005 — Frontier permissions

Approved permission groups are:

- `wayfarer.frontier.use`
- `wayfarer.frontier.admin.read`
- `wayfarer.frontier.admin.delivery`
- `wayfarer.frontier.admin.launchpad`
- `wayfarer.frontier.admin.reconcile`
- `wayfarer.frontier.debug`

An optional umbrella node may grant groups, but each handler directly enforces the applicable group. Debug actions require both configuration enablement and the debug permission.

## 8. Worlds Beyond requirements source

### CAN-WB-001 — Initial safe-entry loadout

On the first safe entry into exact `frontier_iris`, the player receives:

Permanent items:
- one unbreakable, owner-bound Elytra;
- one owner-bound authentic LeafGrapple hook configured without durability and without entity/player hooking;
- one owner-bound Navigation item.

Consumable item:
- two Launchpads, granted only as the initial allocation.

Repeated entry does not duplicate delivered items.

### CAN-WB-002 — Safe-entry delivery

Delivery record retrieval/creation is asynchronous. Before item mutation, the player is revalidated as online and still inside exact `frontier_iris`.

Inventory-full or capability-unavailable delivery does not drop an item. The undelivered typed item remains pending. The player receives an actionable notification. Conflict or unknown outcomes do not auto-retry and direct the player to administrative review. Offline or theme-left outcomes preserve pending state without attempting player notification. Sanitized audit/console reporting is used only as necessary and must not duplicate existing audit semantics.

### CAN-WB-003 — Permanent item authority

Permanent traversal items are owner-bound, theme-bound, and identified by MariaDB plus PDC. Other players cannot use, equip, pick up, or store them. Manual drop and container storage are denied. Reissue invalidates stale epochs when authority is rotated.

### CAN-WB-004 — Permanent item death recovery

Elytra, Grappling Hook, and Navigation item are removed from death drops. Death creates durable typed pending delivery rather than preserving a raw ItemStack in process memory.

On a later exact Worlds Beyond safe entry, the item is reconstructed and delivered free of charge while preserving the same logical identity, physical instance identity, and epoch. Restart between death and redelivery must not lose the pending obligation or create a duplicate.

Launchpads, rockets, and other consumables do not receive this free permanent-item recovery.

### CAN-WB-005 — Elytra behavior

The issued Elytra is unbreakable, owner-bound, usable only in exact `frontier_iris`, and compatible with natural transition from hook or launchpad movement into gliding.

### CAN-WB-006 — LeafGrapple integration

LeafGrapple version `1.0.2` is the adopted capability. The integration creates the authentic hook, checks version and required capabilities, applies Wayfarer owner/theme identity, and verifies a configuration with durability disabled and entity/player hooking disabled.

Wayfarer does not reimplement hook projectile behavior, pull physics, or LeafGrapple cooldown calculation. Unsupported or unsafe integration fails closed. Any non-public dependency is isolated behind a version adapter and recorded as a limitation.

### CAN-WB-007 — Navigation

The Navigation item opens a theme GUI with at least Shop, Loadout, and Help. When Waystone is absent, Discovery, Teleport, and the Waystone Placement Tool are hidden, disabled, or clearly unavailable and cannot succeed. Navigation operations are rejected outside exact `frontier_iris`.

Exact language and layout are not normative acceptance obligations for this scope.

### CAN-WB-008 — Launchpad item and initial balance

A Launchpad item has typed identity including item type, physical instance, definition, and schema. It is a normal theme consumable and may be lost on death. Remaining uses are not stored on the unplaced item. A successful placement consumes exactly one item; a failed placement consumes none.

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

The initial physical block is `LIGHT_WEIGHTED_PRESSURE_PLATE`. Placement requires exact `frontier_iris`, a solid supporting top surface, air at the target, no liquid, world-border inclusion, a loaded chunk, spawn exclusion, WorldGuard permission, and no active launchpad overlap.

Dedicated generic Portal, Gate, Waystone, or System-Structure exclusion is not a V0.0.2 requirement. The item, durable record, and physical block must use a compensatable flow that prevents silent double consumption or authoritative orphaning.

### CAN-WB-010 — Launchpad creation and live configuration

Durable authority records creation identity, definition/schema, placement location, placer, successful use count, maximum uses at creation, creation/last-use/expiration timestamps, state, and optimistic lock.

Maximum uses and expiration-related authority are creation-time durable values. Launch velocity, cooldown, and auto-Elytra behavior use the current approved configuration at use time. Placement yaw is not authoritative for launch direction; an orientation field, if retained for compatibility, is non-authoritative metadata.

### CAN-WB-011 — Launchpad use

Stepping on an active launchpad triggers public use unless the player is sneaking. Launch direction is derived from the user's current look direction at activation, combined with the configured horizontal and vertical launch behavior.

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

Any player may normally break an active launchpad. A successful break yields no item, removes the physical block and active authority once, and is auditable. A cancelled or duplicate break does not remove authority twice.

Active launchpad positions are protected, through supported platform/plugin contracts, from explosion, fire/burn, fluid, piston, entity block change, falling block, block spread, tree/mushroom growth, structure generation, supported WorldEdit/FAWE editing, and mob griefing.

### CAN-WB-013 — Launchpad expiration and reconciliation

Expiration is based on `last_used_at` after at least one successful use and otherwise on `created_at`. Expiration candidates are searched asynchronously and physical mutation occurs on the main thread. Restart catch-up is supported. Removal for maximum uses, expiration, successful player break, authorized administration, or reconciliation is idempotent.

A durable launchpad record whose physical block is missing is detectable and reconcilable. The product is not required to discover an arbitrary `LIGHT_WEIGHTED_PRESSURE_PLATE` with no durable launchpad record or to treat it as a launchpad; such a block remains an ordinary physical block.

### CAN-WB-014 — Frontier shop

Inside exact `frontier_iris`, the shop sells:

- Launchpad ×1 for 30 WM;
- Flight Duration 3, non-explosive Firework Rocket ×1 for 200 WM.

Waystone Placement Tool is not sold while Waystone is unavailable, and any direct request is rejected before debit.

Shop operations use the Core transaction boundary, transaction identity, idempotency, duplicate-click protection, typed pending delivery or refund on clear delivery failure, audit, and `UNKNOWN` handling.

### CAN-WB-015 — Portal denial

Vanilla portal traversal from or within exact `frontier_iris` is denied. This requirement does not authorize Wayfarer_Frontier to implement world generation, Gate lifecycle, or an in-world return structure.

### CAN-WB-016 — Administration

Required administrative capabilities include loadout inspection/reissue, delivery inspection/retry, launchpad inspection/removal/reconciliation, transaction inspection, and audit reference, controlled by the Frontier permission groups in `CAN-FRONTIER-005`.

## 9. Explicit non-scope

### CAN-SCOPE-001 — Main non-scope

This source does not require Axe, Shovel, player-paid Fortune/Silk branch switching, Netherite upgrade, ranking, evolution rewards, abilities, cosmetics, cross-server Growth Tool use, or a ban on vanilla tools.

### CAN-SCOPE-002 — Frontier/Worlds Beyond non-scope

This source does not require full Waystone lifecycle, discovery/teleport behavior, Waystone Placement Tool sales, Ruined Frontier gameplay, EliteMobs gameplay, MVI profile implementation, Gate implementation, Iris world generation, Frontier resource-pack build/delivery, or a separate EliteMobs-MVI adapter.

### CAN-SCOPE-003 — Conditional adapter prohibition

A separate `Wayfarer_Frontier_EliteMobsMVI` adapter is not created or released unless a later authoritative decision explicitly requires it.

### CAN-SCOPE-004 — Process material excluded from software source

Build procedure, test repetition policy, PR status, release sequencing, stable-tag publication, artifact handoff, Project runtime deployment, roadmap order completion, and future version allocation are governed separately and are not software behavior clauses in this canonical source.

## 10. Source-level unresolved questions

The following matters remain intentionally unresolved and must be carried into SWE.1 issue analysis:

1. For the configured Worlds Beyond gameplay world, exact health/status and recovery/re-enable behavior after the world is absent at Wayfarer_Frontier enablement. The Owner has approved whole-plugin fail-closed behavior as sufficient when all Frontier gameplay depends on that missing configured world; literal `frontier_iris` configuration and recovery semantics remain for Frontier-clause review.
2. Exact supported external-repair integration boundary for Growth Tool.
3. Exact public/non-public LeafGrapple 1.0.2 API and a deployable safe tier/configuration.
4. Supported WorldEdit/FAWE protection boundary and the treatment of tools that bypass public hooks.
5. Durable identification of an existing launchpad if the configured physical material changes.
6. The authoritative non-portal return mechanism available while vanilla portals are denied.
7. The allowed invocation context for player-paid Growth Tool reissue.
8. Exact mapping of administrative commands to the approved permission groups.
9. Whether a documented normal-progress checkpoint-loss bound must be fixed at five minutes or remain configurable with a declared maximum.