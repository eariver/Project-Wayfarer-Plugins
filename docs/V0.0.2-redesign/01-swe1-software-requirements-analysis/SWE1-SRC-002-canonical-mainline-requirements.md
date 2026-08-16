# Project Wayfarer Plugin Mainline Canonical Requirements Source

Document ID: `SWE1-SRC-002`  
Revision: F  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
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
- `DEC-REQ-006` — approved Main corrections for `CAN-MAIN-001` through `CAN-MAIN-005`
- `DEC-REQ-007` — approved Main corrections for `CAN-MAIN-006` through `CAN-MAIN-010`
- `DEC-REQ-008` — approved Main corrections for `CAN-MAIN-011` through `CAN-MAIN-015`

## 1. Purpose and authority

This document is the normalized positive-requirement source used before SWE.1 requirement decomposition. It combines the mainline requirement source with only those later Owner decisions that clarified a requirement or resolved a contradiction in that source.

Revision F integrates the completed joint Owner review of the Common (`CAN-COM-001` through `CAN-COM-010`), Core (`CAN-CORE-001` through `CAN-CORE-005`), and Main through `CAN-MAIN-015`. `CAN-MAIN-016` and later Main clauses, Frontier, Worlds Beyond, and Scope remain subject to their own clause-by-clause review; conflicting later wording identified for required propagation is not silently changed before that owning review.

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
| Main joint review through CAN-MAIN-005 | Repository decision `DEC-REQ-006` | Controls integrated Main corrections 001–005 and mandatory propagation recorded for later Main review |
| Main joint review through CAN-MAIN-010 | Repository decision `DEC-REQ-007` | Controls integrated Main corrections 006–010, including withdrawal of death-drop suppression, numeric encoding fixation, and material-dependent progress weighting |
| Main joint review through CAN-MAIN-015 | Repository decision `DEC-REQ-008` | Controls configured evolution/reconciliation, Broken-state continuity, management interface, and protected full-repair corrections |

## 3. Applied amendment disposition

| Amendment | Disposition | Canonical effect |
|---|---|---|
| AMD-001 | Applied | Launchpad launch direction uses the current user's look direction, not placement yaw |
| AMD-002 | Applied | Vanilla portal traversal is denied inside `frontier_iris` |
| AMD-003 | Applied | Durable launchpad creation state is limited; velocity, cooldown, and auto-Elytra use current configuration |
| AMD-004 | Applied as current-scope clarification only | Dedicated generic Gate/Portal/System-Structure placement exclusion is not required; no later roadmap is adopted here |
| AMD-005 | Applied as current-scope clarification only | A physical pressure plate without durable launchpad authority need not be identified as a launchpad |
| AMD-006 | Superseded in part by `DEC-REQ-007` | Player/entity death/item-lifecycle drop suppression is withdrawn; respawn alone still does not create implicit replacement, now generalized under CAN-MAIN-006 / existing delivery semantics |
| AMD-007 | Applied to still-unreviewed CAN-MAIN-016 subject to later Owner propagation | Player-paid Growth Tool reissue remains required; DEC-REQ-006 records revised authority-rotation and relative-pricing direction for CAN-MAIN-016 review |
| AMD-008 | Applied | Permanent Worlds Beyond items use durable pending delivery after death |
| AMD-009 | Superseded by `DEC-REQ-007` | Concrete `Long.MAX_VALUE` saturation is replaced by representation-independent monotonic/overflow-safe maximum-boundary semantics |
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

### CAN-MAIN-001 — Main capability deployment and lifecycle

Wayfarer_Main capabilities are enabled only where they are allocated by approved integration/deployment configuration and shall not use a historical `Main` backend identity, physical server name, or equivalent topology label as a general runtime prerequisite.

Each Main capability is available only while the mandatory prerequisites assigned to that capability are valid, including applicable approved configuration, compatible Main-owned schema state, required approved shared or external capability contracts, and capability-specific gameplay/content context.

Mandatory dependencies are capability-scoped. Absence or incompatibility of a contract required by only one Main capability makes that capability unavailable but does not by itself disable unrelated Main capabilities whose own prerequisites remain valid.

For V0.0.2, a Main financial capability that uses the shared Waymark transaction contract requires the compatible Core-provided transaction contract allocated by `CAN-CORE-003`; this does not make the presence of Wayfarer_Core a universal prerequisite for every Main capability.

Prerequisite loss, operation-admission closure, accepted-operation disposition, and stale lifecycle completion follow the Common lifecycle requirements. This clause does not prescribe whole-plugin disablement, listener registration, startup ordering, or another specific lifecycle mechanism.

### CAN-MAIN-002 — Logical Growth Tool authority and durable state

Wayfarer_Main owns the logical Growth Tool domain. MariaDB is the durable authority for Main-owned logical Growth Tool state under the Common durable-state authority requirements.

For each owner and approved Growth Tool type, at most one logical Growth Tool authority may exist. The only Growth Tool type approved in V0.0.2 is `PICKAXE`; this uniqueness rule does not add other tool types to current scope.

The logical Growth Tool state distinguishes:

- tool lifecycle status: `ACTIVE`, `BROKEN`, or `REVOKED`;
- delivery status: `DELIVERED` or `PENDING`;
- active branch: `FORTUNE` or `SILK_TOUCH`.

Durable logical authority retains the semantic state required for logical continuity across supported restart, including stable logical identity, owner identity, approved tool type, current authority epoch, cumulative progress, and the applicable lifecycle, delivery, and branch states.

Current physical durability/damage is authoritative Minecraft physical item state under `CAN-COM-003`; it is not maintained as a separate authoritative current-damage value in the Main logical Growth Tool record.

This requirement does not prescribe a physical table layout, per-record schema-version field, optimistic-lock counter, creation/update timestamps, checkpoint-timestamp field, or another concurrency/persistence mechanism. Schema compatibility, checkpoint behavior, concurrency correctness, and auditability remain governed by their applicable requirements.

### CAN-MAIN-003 — Physical representation identity and logical-authority validation

A physical Growth Tool or Broken Tool is a Minecraft runtime representation of Main-owned logical Growth Tool authority and does not become authoritative from its item metadata or possession alone.

Each managed physical representation shall carry supported persistent machine-readable identity information sufficient to correlate it with the applicable logical Growth Tool authority and current physical issuance. The identity semantics shall distinguish the managed item class, stable logical tool identity, physical instance/issuance identity, current authority epoch, and a supported identity-format discriminator sufficient for safe interpretation.

Owner identity, approved tool type, or other logical-domain attributes may be carried redundantly in physical metadata but remain authoritative only through the current logical Growth Tool state. The requirement does not prescribe an exact PDC key set, metadata layout, serialization representation, or redundant-field set.

Material, display name, lore, enchantments, presentation revision, visual similarity, or equivalent player-visible/mutable attributes shall not by themselves establish managed-item authority. Presentation metadata may exist but does not strengthen physical authority.

Before a managed physical representation produces an operation requiring current tool authority, its identity shall be resolved and validated against the applicable current logical authority. Unsupported or malformed identity, unresolved or mismatched logical authority, subject-owner mismatch, invalid current physical issuance where applicable, and stale epoch shall fail closed.

The exact persistent-item metadata mechanism may be selected during SWE.2/SWE.3 using an approved platform contract; use of Paper PDC remains permissible but is not fixed here as the Product-level encoding contract.

### CAN-MAIN-004 — Initial logical entitlement and durable delivery

When a player first reaches an eligible Main Growth Tool capability entry condition, Main shall resolve the player's existing logical Growth Tool authority or establish the initial logical `PICKAXE` authority when absent, subject to the approved uniqueness requirement. Durable resolution shall not block a runtime execution context on which such I/O is prohibited, and concurrent or replayed entry shall not create duplicate logical authority or duplicate initial delivery entitlement.

Physical delivery is a distinct effect from logical-entitlement establishment. Before an outstanding initial or pending delivery entitlement mutates player inventory, Main shall revalidate the applicable current lifecycle, player, capability-context, logical-authority, and delivery prerequisites and perform the mutation only from an execution context authorized by the adopted platform contract.

If a still-valid delivery entitlement cannot be safely completed, including because the player's inventory lacks capacity, Main shall not create or use a world-item drop as a means of completing or bypassing that delivery. The outstanding delivery entitlement remains durably pending/recoverable under the applicable lifecycle and protected-operation rules. This fallback-delivery restriction does not prohibit ordinary player-initiated or Minecraft entity/death drop behavior after delivery has completed.

When a pending outcome occurs while the player is reachable, the player receives an actionable notification. The pending outcome and operationally relevant reason/classification remain correlated with retrievable evidence. A later eligible entry or authorized administrative retry may continue the same outstanding delivery entitlement without Waymark debit, logical-authority duplication, authority rotation, or duplicate delivery.

Re-entry or failure merely to observe a physical item shall not cause automatic reissue when the current delivery state is `DELIVERED`. Recovery or replacement of a previously delivered item uses only the applicable authorized recovery/reissue requirements.

### CAN-MAIN-005 — Owner-bound use and controlled tool modification

A Growth Tool remains logically bound to its current owner regardless of which player, inventory, storage container, item entity, or other supported Minecraft possession context currently contains its physical representation. Physical possession, transfer, pickup, drop, or storage does not transfer or change logical ownership.

Only the current logical owner may use a current authorized Growth Tool as a Growth Tool, gain Growth Tool progress, or perform another owner-authorized Growth Tool operation. A non-owner may physically possess, carry, pick up, drop, store, or transfer the item but does not thereby gain Growth Tool authority.

Wayfarer_Main shall not prohibit ordinary supported physical possession, inventory/storage transfer, pickup, or player/entity drop solely because the item is a Growth Tool. This includes ordinary player inventory, chest, Ender Chest, Shulker Box, world item, and equivalent supported Minecraft storage/transfer behavior.

Growth Tool durability restoration and Growth Tool enchantment-state modification, including enchantment addition, increase, removal, reduction, transfer, or replacement, occur only through operations authorized by Wayfarer_Main under the applicable Growth Tool requirements. Ordinary durability loss remains governed by the applicable Minecraft/Paper durability requirements.

A Growth Tool shall not be processed through an anvil or grindstone in V0.0.2. This prohibition includes repair, combination, enchantment modification or transfer, grindstone enchantment removal, and anvil renaming. Crafting repair/combination, Mending-based durability restoration, smithing or equivalent transformation that bypasses approved Growth Tool evolution, and supported external modification paths shall likewise not alter the managed Growth Tool outside the applicable Wayfarer-controlled operation.

Physical movement or possession does not rotate Growth Tool authority. When an authorized reissue later rotates the current physical authority, prior physical instances may remain physically present but become stale and cannot be used as an authorized Growth Tool under the applicable epoch/current-authority requirements.

### CAN-MAIN-006 — Death, respawn, and physical-item lifecycle neutrality

Wayfarer_Main shall not suppress, remove, replace, or otherwise specially redirect an ordinary supported Minecraft Growth Tool or Broken Tool drop solely because it results from player death or another applicable entity/item lifecycle event. Ordinary Minecraft drop, pickup, storage, loss, destruction, and despawn behavior remains subject to the physical-state authority and possession rules established by `CAN-COM-003` and `CAN-MAIN-005`.

Death, respawn, physical-item loss, destruction, or failure to observe the delivered physical representation does not by itself change logical ownership, delivery status, current physical-instance identity, or authority epoch and does not create a new delivery or reissue entitlement.

Respawn does not itself cause automatic Growth Tool restoration or replacement. A previously delivered physical Growth Tool that is no longer available is replaced only through an applicable authorized recovery/reissue operation.

This clause does not prescribe temporary raw `ItemStack` retention, event-list manipulation, inventory scanning, physical-item tracking, or another implementation mechanism.

### CAN-MAIN-007 — Configured exact progress-world boundary

Growth Tool progress is available only in worlds whose platform world identity exactly matches an entry in the approved Growth Tool progress-world configuration.

The initial V0.0.2 supplied/default progress-world allowlist contains:

- `resource`
- `resource_nether`
- `resource_end`

A world outside the approved exact allowlist does not grant Growth Tool progress. Wayfarer_Main shall not infer eligibility from a similar world name, prefix/suffix/substring match, dimension or environment alone, historical naming convention, or another unapproved heuristic.

Absence or unavailability of one configured progress world does not cause another world to be adopted implicitly and does not by itself make other independently valid configured progress worlds ineligible. Configuration validity and capability-prerequisite behavior follow the applicable Main/Common lifecycle requirements.

This clause fixes the approved exact-membership policy and V0.0.2 initial defaults, not permanent physical world names for later deployments.

### CAN-MAIN-008 — Qualifying player-mined block progress

A completed qualifying player-caused block break grants Growth Tool progress exactly once when:

- the current world is eligible under the approved progress-world requirements;
- the broken block is classified by the adopted Minecraft block-tag contract as `minecraft:mineable/pickaxe`;
- the physical tool used for the mining action resolves to the player's current authorized `ACTIVE` Growth Pickaxe; and
- the player is in Survival, or is in Adventure and the player mining action actually completes the block break.

A denied or cancelled mining attempt that does not complete the qualifying block break does not grant progress. Creative and Spectator activity does not grant Growth Tool progress.

Block eligibility does not depend on block provenance or placement history. An otherwise eligible naturally generated, player-placed, generator/plugin-created, or Silk-Touch-collected-and-re-placed block remains eligible. Repeated eligible mining is not prohibited, and this requirement does not require Wayfarer to maintain block-placement or block-provenance authority solely for Growth Tool progress.

Block removal that is not the result of a qualifying player mining action does not grant Growth Tool progress. Explosion, piston, command, editor, plugin-direct, and equivalent non-player-mining removal are representative excluded cases and do not establish separate Product dependencies.

Multiple observations or callbacks associated with the same completed physical block break shall not cause more than one progress addition.

### CAN-MAIN-009 — Deterministic cumulative progress and numeric-boundary safety

Growth Tool cumulative progress is maintained as one logical non-negative progress quantity. Its accumulation, durable persistence and reload, threshold comparison, user-visible representation, and configuration reconciliation shall preserve the approved progress semantics deterministically without representation-dependent drift or divergence.

An accepted positive progress addition shall not decrease cumulative progress and shall not cause numeric wraparound, negative overflow, corruption, or undefined behavior.

The implementation may use a bounded or unbounded numeric representation. If the selected representation has a supported maximum cumulative value, reaching that boundary shall produce a defined bounded state; subsequent positive additions shall not wrap, corrupt the value, or make the Growth Tool unusable merely because that numeric boundary has been reached.

Threshold/evolution determination, status presentation, durable persistence/reload, and configuration reconciliation shall remain defined at any supported maximum progress state.

This requirement does not prescribe `1000` internal units per logical progress point, Java `long`, `Long.MAX_VALUE`, or another physical numeric encoding.

### CAN-MAIN-010 — Uniform qualifying-break progress increment

Every qualifying block break established by `CAN-MAIN-008` contributes the same configured positive logical Growth Tool progress increment.

The initial V0.0.2 supplied/default increment is `1.00` logical progress per qualifying break.

Progress increment does not vary according to block material, block category, ore classification, rarity, provenance, generation source, or equivalent block-specific characteristic. Wayfarer_Main shall not apply block-specific base weights, ore multipliers, rarity multipliers, or equivalent material-dependent progression modifiers unless a later approved requirement explicitly introduces such behavior.

The configured uniform increment shall be a valid positive logical progress quantity. Configuration validity and fail-closed behavior follow the applicable Main/Common capability-prerequisite requirements.

The logical increment does not prescribe an internal numeric encoding or storage scale; numeric representation and boundary safety follow `CAN-MAIN-009`.

### CAN-MAIN-011 — Configured material and enchantment evolution

A Growth Pickaxe begins with the Wood base material and progresses through the V0.0.2 base-material sequence Wood → Stone → Iron → Diamond as cumulative progress reaches the applicable approved material thresholds. Progress-based automatic material evolution does not extend beyond Diamond unless a later approved requirement adds another material tier.

The initial V0.0.2 supplied/default cumulative material thresholds are:

- Stone: `100`
- Iron: `400`
- Diamond: `1200`

After Diamond, enchantment evolution occurs at successive cumulative thresholds formed by adding a configured positive increment for each post-Diamond enchantment evolution number `n`, beginning at `n=1`, to the preceding evolution threshold. The initial V0.0.2 supplied/default increment definition is `800 + 200n + 40n²`.

Post-Diamond evolution applies conceptual enchantment steps according to the approved V0.0.2 evolution mapping. The initial supplied/default repeating mapping is Efficiency → Unbreaking → Efficiency → Unbreaking → Fortune. The V0.0.2 conceptual enchantment progression set is limited to Efficiency, Unbreaking, and Fortune unless a later approved requirement extends it.

Effective enchantment application is capped by approved configuration. The initial V0.0.2 supplied/default caps are Efficiency 10, Unbreaking 10, Fortune 5, and Silk Touch 1. Reaching an effective cap does not stop cumulative progress, evolution count, or conceptual enchantment progression.

The initial branch is `FORTUNE`. In the `FORTUNE` branch, the current capped conceptual Fortune level is applied and Silk Touch is not applied. In the `SILK_TOUCH` branch, Silk Touch I is applied and Fortune is not applied to the physical item; Fortune-oriented conceptual progression nevertheless continues. Returning to `FORTUNE` reapplies the then-current conceptual Fortune level subject to its effective cap.

V0.0.2 permits an authorized administrative operation to select `FORTUNE` or `SILK_TOUCH`. Ordinary player-paid branch switching remains outside V0.0.2 scope.

Evolution thresholds, post-Diamond increment parameters/mapping, and effective caps are approved configuration rather than permanent numeric Product constants. Their validation and reconciliation behavior is governed by the applicable configuration/reconciliation requirements.

### CAN-MAIN-012 — Deterministic evolution evaluation and configuration reconciliation

Growth Tool evolution shall be deterministically evaluable for every supported cumulative-progress value, including any supported maximum state, using one internally consistent approved evolution-configuration snapshot. One evaluation or reconciliation shall not combine mutually dependent evolution values from different configuration states.

When the effective approved evolution configuration changes, cumulative progress remains unchanged. Before a Growth Tool next produces configuration-dependent gameplay behavior or presentation under the new configuration, its derived material tier, conceptual evolution state, effective enchantments, and next-threshold state shall correspond to the new configuration and its current authoritative branch. Derived evolution state may promote or demote as a result of configuration change.

Configuration reconciliation does not by itself change logical ownership, logical tool identity, lifecycle state, delivery state, active branch, current physical issuance identity, or authority epoch. This requirement does not require eager reconciliation of every persisted or physically existing Growth Tool or global discovery of physical item instances.

Configuration reconciliation alone shall not repair the Growth Tool or change a `BROKEN` tool to `ACTIVE`.

When reconciliation changes the material of a current authorized `ACTIVE` physical Growth Pickaxe, its Minecraft-authoritative remaining-durability fraction shall be preserved across the new material's durability capacity subject to unavoidable discrete durability quantization, with at least one remaining durability point. Reconciliation shall not create a separate authoritative durability value when the current physical representation is not available for reconciliation.

Within normal Growth Tool progression, an accepted qualifying progress addition that crosses one or more configured material or post-Diamond enchantment evolution thresholds causes one full-durability restoration of the applicable current authorized active physical Growth Pickaxe. A progress addition that crosses no evolution threshold does not cause progression-triggered durability restoration. Explicit authorized repair or reissue behavior remains governed by its owning requirements.

The exact evaluation algorithm, configuration-snapshot representation, eager/lazy reconciliation strategy, and physical-item discovery mechanism are not prescribed by this clause.

### CAN-MAIN-013 — Terminal durability and Broken-state continuity

When a current authorized `ACTIVE` Growth Pickaxe reaches terminal durability through an otherwise valid operation, terminal durability shall not cause vanilla destruction of the logical Growth Tool or silent loss of its managed authority. Unless the same qualifying operation produces an applicable progression-triggered full-durability recovery under `CAN-MAIN-012`, Main shall transition the logical tool lifecycle from `ACTIVE` to `BROKEN` and represent the current physical issuance as a Broken Tool.

A progression-triggered full-durability recovery produced by the same qualifying operation is applied before deciding whether terminal Broken conversion remains applicable. If that recovery leaves the tool active above terminal durability, the tool remains `ACTIVE`.

Broken conversion preserves the existing logical tool identity, owner, approved tool type, cumulative progress, delivery state, active branch, current physical issuance identity, and authority epoch. Broken conversion is not a reissue, does not rotate authority, and does not create a new delivery entitlement.

The Broken physical representation shall be distinguishable from an active Growth Pickaxe, shall carry the applicable supported managed-item identity needed to resolve the same current logical authority, and shall not itself become authority through its material or presentation. The initial V0.0.2 supplied/default Broken representation is `GRAY_DYE`; the exact presentation is not a permanent authority or identity contract.

A `BROKEN` representation shall not function as an active Growth Pickaxe or satisfy an operation requiring `ACTIVE` lifecycle state. Progress eligibility and durability/enchantment-modification restrictions continue to follow their applicable Growth Tool requirements. Only an explicitly authorized operation whose own requirements accept `BROKEN` state may restore or otherwise transition the tool.

A current authorized Broken Tool remains eligible for the applicable management interface; the exact management-entry gesture is governed by its owning GUI requirement.

The established Broken state shall remain durably recoverable across supported lifecycle interruption and restart. Restart alone shall not revert the logical tool to `ACTIVE`, rotate its authority, create replacement entitlement, or otherwise erase the Broken transition. This requirement does not prescribe an event priority, temporary `ItemStack` retention, synchronous database write, checkpoint implementation, or another specific terminal-damage mechanism.

### CAN-MAIN-014 — Owner management interface

The current logical owner of a current authorized Growth Tool or Broken Tool shall be able to open the Main management GUI by performing a main-hand air right-click that does not target a block or entity while holding that managed physical representation. The corresponding off-hand interaction shall not open the management GUI. This requirement defines this required player entry route and does not prohibit another later approved management entry route.

Management entry shall resolve the presented physical representation against current logical authority under the applicable Growth Tool identity and ownership requirements. Physical possession by a non-owner, stale physical issuance, stale epoch, malformed identity, or another failed authority condition does not grant access to owner-authorized management behavior.

The management GUI shall present sufficient current state for the owner to understand the Growth Tool, including:

- lifecycle status;
- Growth Pickaxe material tier;
- cumulative progress;
- conceptual evolution state/count;
- next configured evolution threshold or a defined no-next-threshold state;
- effective enchantments;
- active branch;
- current Minecraft-authoritative physical durability when applicable, or Broken state when physical durability is not applicable;
- applicable repair availability and preview/quote information; and
- any material difference between conceptual progression and currently effective enchantment state caused by configured caps or branch projection.

The interface shall provide access to the applicable Repair operation and Help/Status information. Opening the interface or viewing status/repair preview shall not by itself authorize a repair, debit Waymark, or produce another protected state-changing effect. Repair quote, confirmation, transaction, and failure behavior are governed by their owning repair requirements.

Required management information shall remain identifiable and applicable required actions shall remain operable, but exact language, inventory size, slot assignment, decorative item selection, display name, lore, and equivalent presentation details are not fixed as functional Product semantics by this clause.

### CAN-MAIN-015 — Owner-paid full repair and protected transaction

Wayfarer_Main shall provide owner-initiated Waymark repair only as a full repair of the current authorized physical Growth Tool or Broken Tool. Partial repair is not a V0.0.2 player repair option.

An `ACTIVE` Growth Pickaxe is eligible for player-paid repair only when it is below maximum durability. A maximum-durability active tool is not repairable, has a displayed repair charge of `0 WM`, and does not begin a debit operation. A current authorized `BROKEN` tool is eligible for the applicable Broken repair. A missing, stale, revoked, non-owner, unresolved, or otherwise non-current physical representation is not repaired through this operation.

Repair price is determined from the current approved repair-pricing configuration and current authoritative Growth Tool state. The initial V0.0.2 supplied/default pricing schedule is:

- base full-repair cost: `ceil(100 × (1 + evolution_count × 0.08))`;
- active repair: `ceil(base_full_repair_cost × max(0.25, missing_durability_ratio))`;
- broken repair: `base_full_repair_cost + 100 + evolution_count × 5`.

These are initial/default balance values and may be adjusted through approved configuration for playtest/balance purposes. Current active durability used in pricing is obtained from Minecraft-authoritative physical state.

Before a player-paid repair begins any protected financial effect, the owner shall receive an explicit repair quote and explicitly confirm that quote. Current authority, lifecycle, physical-item state, applicable evolution state, pricing configuration, and quote validity shall be revalidated before debit. If a change would alter eligibility or the confirmed amount, the stale confirmation shall not silently debit a different amount and a new quote/confirmation is required.

An accepted repair uses the V0.0.2 Core-provided shared Waymark transaction contract and the applicable Common protected-operation identity, replay, `UNKNOWN`, and compensation rules. Repair benefit is attempted only after the debit effect is proven successful.

Successful repair of an `ACTIVE` Growth Pickaxe preserves its logical identity, owner, cumulative progress, delivery state, branch, current physical issuance identity, and authority epoch while restoring the current authorized physical item to maximum durability.

Successful repair of a `BROKEN` tool transitions the same logical tool from `BROKEN` to `ACTIVE`, preserves its owner, cumulative progress, delivery state, branch, current physical issuance identity, and authority epoch, and establishes the current authorized active Growth Pickaxe representation consistent with the current approved evolution configuration at maximum durability. Repair is not reissue, authority rotation, or a new delivery entitlement.

Repair shall not be reported as successful until the required logical and physical repaired state for the same current authority is established. An ambiguous or partially established repair benefit remains subject to `UNKNOWN` and authorized reconciliation under the Common protected-operation rules. Automatic compensation requires the Common proven-debit/proven-no-benefit conditions; an ambiguous repair benefit is not automatically refunded or blindly retried.

This clause does not prescribe a transaction-coordinator implementation, lock mechanism, quote-session representation, database transaction layout, or cross-authority atomicity mechanism.

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

> **Pending owning-clause review:** `DEC-REQ-006` §7.2 records that reissue safety shall not depend on proving current physical absence across all permitted possession/storage contexts; successful authority rotation shall invalidate prior physical instances. It also records that the exact formula above is no longer fixed so long as paid reissue remains strictly more expensive than the applicable repair price. `CAN-MAIN-016` remains unreviewed and shall be corrected during its own review.

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

### CAN-MAIN-019 — Normal durability behavior

An authorized active Growth Pickaxe follows the approved Paper/vanilla nonterminal durability result during ordinary use. Managed-item handling does not suppress normal durability loss merely because the item is authorized. The explicit exceptions are an evolution-triggered full recovery, denied or cancelled use, controlled repair/reissue, and terminal-damage conversion to the Broken state.

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
2. Exact supported external-repair/modification integration boundary for Growth Tool. The Product-level policy is fixed by reviewed `CAN-MAIN-005`: supported external paths must not bypass Wayfarer-exclusive durability restoration/enchantment modification.
3. Exact public/non-public LeafGrapple 1.0.2 API and a deployable safe tier/configuration.
4. Supported WorldEdit/FAWE protection boundary and the treatment of tools that bypass public hooks.
5. Durable identification of an existing launchpad if the configured physical material changes.
6. The authoritative non-portal return mechanism available while vanilla portals are denied.
7. The allowed invocation context for player-paid Growth Tool reissue.
8. Exact mapping of administrative commands to the approved permission groups.
9. Whether a documented normal-progress checkpoint-loss bound must be fixed at five minutes or remain configurable with a declared maximum.
