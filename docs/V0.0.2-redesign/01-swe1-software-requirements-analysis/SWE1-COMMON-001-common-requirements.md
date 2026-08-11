# Cross-target Common Software Requirements

Document ID: `SWE1-COMMON-001`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `COMMON`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision B  
Controlling review decisions: `DEC-REQ-002`, `DEC-REQ-004`  
Contained normative items: CON: 10, IFC: 6, QLT: 14

## 1. Purpose

Define software obligations that apply consistently across Wayfarer-owned shared and feature capabilities without fixing those capabilities to one physical server topology or one implementation unit.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- `Capability`, `owner`, and `authority` describe software responsibility and state semantics; they do not imply fixed physical-server placement unless a target requirement explicitly makes that placement an external contract.

## 3. Requirements

### SWE1-COMMON-001-CON-001 — Deployment-topology independence

**Normative statement:** Wayfarer-owned software shall separate shared and feature capability ownership without requiring a fixed physical server identity, server name, or historical Project backend role as a general prerequisite for operation. Artifact placement and capability composition shall be controlled by approved integration configuration, and each capability shall become available only when its actual functional prerequisites are satisfied.

**Source:** SWE1-SRC-002 §4 CAN-COM-001; DEC-REQ-002 §2  
**Rationale:** Preserves capability ownership while permitting future server consolidation, division, or co-location.  
**Precondition / trigger:** At deployment, enablement, or capability composition change.  
**Required observable result:** A capability is not denied solely because the runtime lacks a historically named Main, Frontier, or Lobby identity; actual missing prerequisites still fail closed.  
**Verification intent:** SWE.2 allocation inspection plus SWE.5 multi-topology lifecycle integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-002 — Approved shared-contract dependency

**Normative statement:** A Wayfarer feature capability shall obtain Wayfarer-owned shared capabilities only through public contracts of software units explicitly assigned shared ownership and shall not depend on another feature plugin's internal implementation.

**Source:** SWE1-SRC-002 §4 CAN-COM-002; DEC-REQ-002 §3  
**Rationale:** Allows shared ownership to evolve without coupling feature domains to one another or permanently forcing all shared functionality into Core.  
**Precondition / trigger:** At build, packaging, architecture allocation, class loading, and runtime capability resolution.  
**Required observable result:** Feature-to-shared access crosses only approved public contracts and no feature implementation is consumed as another feature's internal dependency.  
**Verification intent:** Dependency/API inspection and SWE.5 contract-resolution integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-007 — Shared-foundation independence from feature internals

**Normative statement:** A software unit assigned shared-foundation ownership shall not depend on a feature-specific software unit in a way that reverses the approved shared-to-feature layering.

**Source:** SWE1-SRC-002 §4 CAN-COM-002; DEC-REQ-002 §3  
**Rationale:** Prevents shared mechanisms from acquiring feature-specific runtime dependencies that invert ownership.  
**Precondition / trigger:** At architecture allocation, dependency declaration, packaging, and runtime service resolution.  
**Required observable result:** Shared-foundation units remain usable without loading feature-internal classes or services that are not part of an explicitly approved shared contract.  
**Verification intent:** SWE.2 dependency-allocation inspection and SWE.5 packaging/class-loading integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-008 — Feature mutual independence and acyclic dependency graph

**Normative statement:** Wayfarer feature plugins shall not depend on one another's internal implementation, and the Wayfarer software dependency graph shall remain acyclic.

**Source:** SWE1-SRC-002 §4 CAN-COM-002; DEC-REQ-002 §3  
**Rationale:** Supports independent feature evolution and prevents circular startup/ownership contracts.  
**Precondition / trigger:** At architecture allocation, dependency resolution, packaging, and runtime loading.  
**Required observable result:** No direct or transitive feature-to-feature implementation cycle exists.  
**Verification intent:** Dependency-graph inspection and SWE.5 class-loading integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-001 — Wayfarer durable-state authority

**Normative statement:** Wayfarer-owned durable business state shall use MariaDB as its durable authority unless a later approved requirement explicitly assigns a different authority.

**Source:** SWE1-SRC-002 §4 CAN-COM-003; DEC-REQ-002 §4  
**Rationale:** Establishes one durable source of truth for Wayfarer-owned business state without conflating cache or runtime state with authority.  
**Precondition / trigger:** Whenever Wayfarer-owned durable business state is created, read, recovered, or reconciled.  
**Required observable result:** Durable business state can be reconstructed from the assigned durable authority after runtime restart.  
**Verification intent:** Schema/authority inspection and SWE.5 persistence/restart integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-009 — Redis non-authoritative coordination

**Normative statement:** Redis may provide cache, lock, coordination, pub/sub, messaging, or equivalent transient assistance but shall not be the sole durable authority for Wayfarer-owned business state; recoverable cached state shall be reconstructable from its assigned authority.

**Source:** SWE1-SRC-002 §4 CAN-COM-003; DEC-REQ-002 §4  
**Rationale:** Prevents transient coordination infrastructure from silently becoming the durable source of truth.  
**Precondition / trigger:** Whenever Redis-backed state participates in a Wayfarer operation.  
**Required observable result:** Loss or recreation of Redis state does not by itself destroy the authoritative Wayfarer durable business state.  
**Verification intent:** SWE.2 authority analysis and SWE.5 cache-loss/recovery integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-003 — External player-state authority

**Normative statement:** For normal player-state domains assigned to an adopted player-state system such as MVI, Wayfarer-owned software shall treat that system as the authority and shall not silently substitute Wayfarer persistence for it.

**Source:** SWE1-SRC-002 §4 CAN-COM-003; DEC-REQ-002 §4  
**Rationale:** Prevents competing inventory/profile authorities and duplicate transition ownership.  
**Precondition / trigger:** Whenever normal player inventory/profile state is saved, restored, switched, or reconciled.  
**Required observable result:** Wayfarer does not overwrite or independently reconstruct a state domain assigned to the external authority except through an explicitly approved capability contract.  
**Verification intent:** SWE.2 authority allocation and SWE.5 adopted-player-state integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-004 — Waymark provider authority and approved access boundary

**Normative statement:** Waymark balance authority shall remain the approved economy provider, and Wayfarer feature capabilities shall access that authority through an approved shared transaction/provider contract rather than provider-internal state.

**Source:** SWE1-SRC-002 §4 CAN-COM-003; DEC-REQ-002 §4  
**Rationale:** Separates external balance authority from Wayfarer-owned transaction coordination and prevents unsupported provider coupling.  
**Precondition / trigger:** Whenever a Waymark debit, refund, balance-dependent decision, or reconciliation operation is required.  
**Required observable result:** Provider state is accessed only through the approved contract and Wayfarer does not claim stronger provider authority than the contract supplies.  
**Verification intent:** Contract inspection and SWE.5 provider integration/failure-mode verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-005 — Minecraft runtime-state authority and platform access

**Normative statement:** Minecraft runtime world, block, entity, inventory-item, and related physical state shall remain authoritative in the Minecraft runtime and shall be accessed or mutated only through approved platform or adopted-plugin contracts.

**Source:** SWE1-SRC-002 §4 CAN-COM-003; DEC-REQ-002 §4  
**Rationale:** Distinguishes Wayfarer logical authority from runtime physical state and preserves platform consistency.  
**Precondition / trigger:** Whenever a Wayfarer capability observes or changes Minecraft-owned physical/runtime state.  
**Required observable result:** Physical mutations occur through supported contracts, and any feature combining logical and physical authorities retains explicit mismatch detection/reconciliation responsibility.  
**Verification intent:** SWE.2 authority analysis and SWE.5 platform/feature reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-003 — Supported external-state access only

**Normative statement:** Wayfarer-owned software shall not use another product's private database, undocumented storage, internal cache, implementation-specific state, or equivalent unsupported internals as an integration contract. Supported public APIs, events, provider contracts, platform-visible state, and explicitly approved adapters may be used within their documented guarantee boundaries.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; DEC-REQ-002 §5.1  
**Rationale:** Protects external ownership and upgrade compatibility without prohibiting legitimate supported integrations.  
**Precondition / trigger:** Whenever an external product capability or state is required.  
**Required observable result:** External integration uses a supported contract or an explicit approved exception; no unapproved internal-state mutation/access is used as the product contract.  
**Verification intent:** Dependency/source inspection and SWE.5 supported-boundary integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-004 — Normal player-state authority non-substitution

**Normative statement:** Wayfarer-owned software shall not act as the general or long-term authority for normal player inventory or profile state. A later explicitly approved cross-context item-transfer capability may temporarily capture, persist, transform, and redeliver only its approved item classes and contexts through a controlled transaction without becoming a general inventory-storage service.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; DEC-REQ-002 §5.2  
**Rationale:** Preserves external player-state authority while not blocking a future explicitly approved transactional transfer feature.  
**Precondition / trigger:** Whenever normal player state, cross-context item movement, or pending item state is persisted.  
**Required observable result:** Current V0.0.2 behavior does not persist general inventory/profile state; any later transfer feature requires its own approved scope, duplication/loss controls, audit, compensation, and reconciliation.  
**Verification intent:** Schema/scope inspection and SWE.5 player-state integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-005 — Non-public API exception control

**Normative statement:** Unsupported or non-public APIs shall not be adopted as a Product dependency unless an explicit Owner-approved exception identifies the exact compatibility range, necessity, isolation boundary, failure behavior, and limitation; an approved exception shall be contained behind a replaceable adapter and fail closed outside its approved compatibility range.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; §8 CAN-WB-006; DEC-REQ-002 §5.3  
**Rationale:** Contains unavoidable version-sensitive integration risk and prevents it from becoming an implicit domain contract.  
**Precondition / trigger:** When an approved capability cannot be provided through a sufficient public/supported contract.  
**Required observable result:** No non-public dependency is used without the explicit exception and isolation controls.  
**Verification intent:** SWE.2/SWE.3 adapter/reference inspection and SWE.5 compatibility failure verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-003  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-001 — Platform-authorized runtime-state access

**Normative statement:** Access to or mutation of Minecraft runtime state and event-control operations shall occur only from execution contexts authorized by the adopted server-platform contract for the affected state; the software shall not assume one global main thread when the platform defines more specific authorized contexts.

**Source:** SWE1-SRC-002 §4 CAN-COM-005; DEC-REQ-002 §6  
**Rationale:** Preserves runtime safety while remaining compatible with region-thread or other future platform execution models.  
**Precondition / trigger:** Whenever players, inventories, items, blocks, worlds, chunks, entities, GUIs, movement, or cancellable events are accessed or mutated.  
**Required observable result:** Every runtime-state access/mutation occurs from a platform-authorized execution context.  
**Verification intent:** SWE.4 execution-context verification and SWE.5 runtime integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-002 — No blocking I/O on tick-critical or region-critical contexts

**Normative statement:** Blocking or completion-waiting database, Redis, filesystem, network, or durable-audit I/O shall not execute on tick-critical or region-critical server execution contexts. Bounded in-memory enqueueing alone is not treated as blocking durable I/O.

**Source:** SWE1-SRC-002 §4 CAN-COM-005; DEC-REQ-002 §6  
**Rationale:** Prevents server-tick/region stalls and deadlock-sensitive coupling without prescribing one asynchronous implementation.  
**Precondition / trigger:** Whenever external or durable I/O is required from gameplay/runtime processing.  
**Required observable result:** Critical server execution contexts do not synchronously wait for external/durable I/O completion.  
**Verification intent:** SWE.4 executor/context assertions and SWE.5 runtime timing/context inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-009 — Asynchronous completion revalidation

**Normative statement:** After asynchronous or deferred work completes, the software shall return to an execution context authorized for the affected runtime state and revalidate every mutable precondition required by that operation before applying a protected runtime mutation.

**Source:** SWE1-SRC-002 §4 CAN-COM-005; DEC-REQ-002 §6  
**Rationale:** Prevents stale asynchronous results from mutating a player, item, world, authority, or lifecycle state that changed while work was outstanding.  
**Precondition / trigger:** When a deferred result is ready to produce a runtime or authority-dependent effect.  
**Required observable result:** Applicable lifecycle validity, subject identity, online state, location/content context, item/authority identity, and other operation-specific mutable preconditions are current immediately before mutation.  
**Verification intent:** SWE.4 stale-completion/race verification and SWE.5 logout/world-change/authority-change integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-003 — Capability prerequisite availability invariant

**Normative statement:** Wayfarer-owned software shall make a gameplay capability available only while every mandatory prerequisite assigned to that capability is valid. Applicable prerequisites shall be expressed in terms of approved configuration, compatible schema state, required approved capability contracts, and required world/content-context conditions rather than fixed physical server names, historical backend roles, or specific shared-provider implementations.

**Source:** SWE1-SRC-002 §4 CAN-COM-006; DEC-REQ-004 §2  
**Rationale:** Prevents partially valid gameplay while preserving capability-scoped deployment flexibility.  
**Precondition / trigger:** At startup and whenever a mandatory prerequisite is established, refreshed, lost, or invalidated.  
**Required observable result:** A capability is available only when all of its assigned mandatory prerequisites are currently valid.  
**Verification intent:** SWE.4 prerequisite-state verification and SWE.5 lifecycle/capability integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-004 — Capability unavailability and operation-admission closure

**Normative statement:** When a mandatory prerequisite becomes invalid, or when a capability or plugin begins disablement or replacement, the affected capability shall become unavailable before accepting any new capability operation or producing any new capability-owned state change.

**Source:** SWE1-SRC-002 §4 CAN-COM-006; DEC-REQ-004 §2  
**Rationale:** Provides one observable admission boundary independent of listener, command, scheduler, or state-machine implementation.  
**Precondition / trigger:** Prerequisite loss, capability deactivation, plugin disablement, or runtime replacement.  
**Required observable result:** No operation first accepted after the closure point produces a protected capability effect; unaffected capabilities may remain available only when their own prerequisites remain valid.  
**Verification intent:** SWE.4 lifecycle/admission verification and SWE.5 prerequisite-loss/disable integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-010 — Bounded accepted-operation disposition

**Normative statement:** On capability deactivation, plugin disablement, or runtime replacement, every operation accepted before admission closure shall, within a finite lifecycle deadline, reach a completed, safely rejected without protected effect, compensated, or explicitly unresolved but durably recoverable disposition. The software shall not wait indefinitely and shall not silently discard incomplete durable work.

**Source:** SWE1-SRC-002 §4 CAN-COM-006; DEC-REQ-004 §2  
**Rationale:** Protects accepted work during lifecycle transitions without mandating an asynchronous flush implementation.  
**Precondition / trigger:** Lifecycle admission closes while one or more accepted operations remain incomplete.  
**Required observable result:** Each accepted operation has a finite, inspectable disposition or recoverable unresolved obligation; lifecycle completion does not wait without bound.  
**Verification intent:** SWE.4 bounded-lifecycle/disposition verification and SWE.5 disable/restart recovery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-011 — Prior-lifecycle completion containment

**Normative statement:** A callback or completion originating from a lifecycle instance that is no longer current and enabled shall not mutate current or disabled runtime state. A completion recognized by the current lifecycle shall additionally satisfy all applicable asynchronous-completion revalidation requirements before mutation.

**Source:** SWE1-SRC-002 §4 CAN-COM-006; DEC-REQ-004 §2  
**Rationale:** Prevents stale lifecycle completions from crossing disable/replacement boundaries without fixing a runtime-generation mechanism.  
**Precondition / trigger:** A deferred completion arrives after disablement, replacement, or lifecycle-state change.  
**Required observable result:** Prior-lifecycle completion produces no unauthorized current/disabled-state mutation; current recognized work is still revalidated under QLT-009.  
**Verification intent:** SWE.4 stale-lifecycle/race verification and SWE.5 replacement/restart integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-009  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-005 — Stable protected-operation identity

**Normative statement:** Before the first protected financial, compensation, durable-entitlement/delivery, or authority-rotation effect is invoked or committed, the software shall establish a stable logical operation identity and recoverable operation disposition. Each protected effect and available provider reference shall be linked to that logical operation and distinguishable by effect kind.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Makes replay/recovery decisions possible before an irreversible or externally ambiguous effect occurs.  
**Precondition / trigger:** Before a protected operation's first protected effect and during later recovery/replay.  
**Required observable result:** Crash/replay can resolve the same logical operation and its individual debit/refund/compensation/entitlement/delivery/authority effects instead of inventing new identities.  
**Verification intent:** SWE.4 operation/effect identity verification and SWE.5 persistence/restart integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-006 — Ambiguous protected-effect containment

**Normative statement:** When a protected effect cannot be proven successful or a clear no-effect failure, the software shall record that effect as `UNKNOWN`. An `UNKNOWN` effect shall not authorize automatic retry, success-dependent continuation, failure-dependent compensation, delivery-entitlement creation, paid-benefit completion, or authority mutation and shall remain inspectable and reconcilable.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Prevents uncertain effects from being converted into duplicate payment, free benefit, duplicate delivery, or authority rotation.  
**Precondition / trigger:** Provider, durable commit, delivery, compensation, or another protected effect has an outcome that cannot be proven.  
**Required observable result:** The exact effect remains `UNKNOWN` until authoritative evidence or authorized reconciliation resolves it; no automatic second uncertain effect occurs.  
**Verification intent:** SWE.4 ambiguity/outcome-policy verification and SWE.5 failure-injection/reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-012 — Replay and duplicate-effect containment

**Normative statement:** A replay of an accepted protected operation shall resolve to the same logical operation and recorded effect dispositions and shall not create a second debit, refund/compensation, durable item entitlement, final item delivery, or authority rotation for the same effect kind. A delivery retry may continue only the same entitlement when prior final delivery has not been established.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Separates legitimate recovery attempts from creation of duplicate protected entitlements/effects.  
**Precondition / trigger:** Double input, reconnect, timeout recovery, restart recovery, reconciliation, or explicit delivery retry.  
**Required observable result:** Replay returns or advances the existing operation without creating a second protected entitlement/effect; successful final delivery exists at most once for the entitlement.  
**Verification intent:** SWE.4 idempotency/replay verification and SWE.5 restart/delivery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-006  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-013 — Paid-benefit sequencing and compensation containment

**Normative statement:** A paid benefit shall not be finalized until debit success is proven. Automatic refund or compensation shall begin only after both debit success and clear downstream paid-benefit failure without committed benefit are proven. The compensation effect shall have its own stable effect identity, shall not be duplicated, and shall follow the common `UNKNOWN` containment rule.

**Source:** SWE1-SRC-002 §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Prevents free benefits, duplicate refunds, and refund-after-ambiguous-success outcomes.  
**Precondition / trigger:** A paid operation proceeds from debit into its downstream domain/delivery effect or compensation handling.  
**Required observable result:** Benefit finalization follows proven debit success; compensation follows proven downstream non-benefit and is itself replay/ambiguity safe.  
**Verification intent:** SWE.4 transaction-sequencing/failure-matrix verification and SWE.5 provider/domain integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-006; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-002 — Durable-schema ownership isolation

**Normative statement:** Each Wayfarer-owned durable-state domain shall have one assigned software owner. The owner shall control schema objects for that domain, and a migration owned by one software unit shall not create, alter, drop, or repurpose schema objects owned by another durable-state owner.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; DEC-REQ-004 §4  
**Rationale:** Preserves independent schema ownership without fixing ownership to a table-prefix or physical migration-layout mechanism.  
**Precondition / trigger:** At schema definition, migration, empty installation, or supported upgrade.  
**Required observable result:** Every Wayfarer-owned schema object is attributable to one durable-state owner and is evolved only through that ownership domain.  
**Verification intent:** SWE.2 data-ownership inspection, schema inspection, and SWE.5 combined-schema migration integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-006 — Owner-attributable migration sequence

**Normative statement:** For each durable-state owner, the software shall maintain an independently identifiable migration sequence and applied-migration state such that a migration belonging to one owner cannot be mistaken for, satisfy, supersede, or alter the migration state of another owner.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; DEC-REQ-004 §4  
**Rationale:** Preserves migration ownership while leaving physical history tables, resource paths, and migration executors to downstream design.  
**Precondition / trigger:** At migration discovery, validation, installation, upgrade, or recovery.  
**Required observable result:** Migration ownership and applied state are unambiguous for each durable-state owner even when migration identifiers or runtime composition overlap.  
**Verification intent:** Migration-catalog/history inspection and SWE.5 multi-owner migration integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-002  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-006 — Accepted migration immutability and forward evolution

**Normative statement:** A migration included in an accepted Product baseline, or that may already have been applied to a supported installation, shall not have its identity, ordering semantics, or content modified, deleted, reused, or repurposed. Further schema evolution shall be expressed by a new forward migration.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; DEC-REQ-004 §4  
**Rationale:** Prevents supported installations from receiving divergent history under the same migration identity.  
**Precondition / trigger:** Whenever a durable schema change is proposed after a migration has crossed the accepted/supported boundary.  
**Required observable result:** Existing accepted migration identity/content remains unchanged and the new schema state is reached through a distinct later migration.  
**Verification intent:** Migration-history/checksum inspection and supported-baseline upgrade verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-007 — Empty-install and supported-baseline upgrade compatibility

**Normative statement:** The current required durable schema shall be constructible from an empty database and upgradeable from the accepted V0.0.1 database baseline without destructive automatic reset or silent loss of accepted durable state. Required data transformation is permitted when it preserves defined authority and resulting state semantics. If migration or schema validation cannot establish compatibility, affected capabilities shall remain fail-closed.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; DEC-REQ-004 §4  
**Rationale:** Supports both first installation and safe continuation from the accepted baseline without treating reset as an upgrade strategy.  
**Precondition / trigger:** Empty installation, V0.0.1-to-current upgrade, or post-migration schema validation.  
**Required observable result:** Installation/upgrade reaches a compatible schema with preserved/transformed accepted state, or affected capabilities remain unavailable without destructive reset.  
**Verification intent:** SWE.5 empty-database and V0.0.1 upgrade integration plus schema inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-002; SWE1-COMMON-001-CON-006  
**Assumptions:** Complete accepted V0.0.1 migration inventory will be established before G1.  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-008 — Retrievable protected-outcome audit evidence

**Normative statement:** For each designated auditable protected outcome, the software shall retain retrievable evidence sufficient to identify the operation or event, applicable actor and subject, occurrence time, resulting disposition, and operationally relevant reason or classification. Where protected-operation identity applies, the evidence shall be correlatable with the applicable logical operation and effect identity. Evidence required for supported inspection or reconciliation shall remain retrievable across normal runtime restart.

**Source:** SWE1-SRC-002 §4 CAN-COM-009; DEC-REQ-004 §5  
**Rationale:** Makes security-sensitive, financial, delivery, recovery, administrative, and reconciliation outcomes operationally traceable without requiring a duplicate dedicated audit store.  
**Precondition / trigger:** Whenever a designated protected outcome reaches a meaningful disposition or reconcilable unresolved state.  
**Required observable result:** Supported inspection can correlate the outcome and its disposition after restart; audit evidence alone is not treated as proof stronger than the underlying authoritative source.  
**Verification intent:** SWE.4 audit-model/correlation verification, SWE.5 persistence/restart inspection, and representative operational review.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** Retention duration is controlled by the applicable operational policy/design and is sufficient for supported inspection/reconciliation.  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-014 — Audit and diagnostic data minimization

**Normative statement:** Audit and diagnostic evidence shall retain only information necessary for approved attribution, operation/effect correlation, disposition analysis, supported recovery, or reconciliation. Secret material, raw credentials, authentication tokens, and equivalently protected values shall not be recorded; personal or incidental runtime data shall not be retained unless required for an approved audit purpose.

**Source:** SWE1-SRC-002 §4 CAN-COM-009; DEC-REQ-004 §5  
**Rationale:** Preserves operational accountability without turning audit/diagnostic facilities into a sensitive-data replication path.  
**Precondition / trigger:** Whenever audit, diagnostic, error, or reconciliation evidence is produced or retained.  
**Required observable result:** Required stable actor/subject/correlation identifiers remain usable where necessary, while secrets and unrelated personal/runtime state are absent.  
**Verification intent:** SWE.4 redaction/data-minimization verification and SWE.5 representative audit/diagnostic inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-008  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-010 — Capability ownership non-duplication

**Normative statement:** Wayfarer-owned software shall not duplicate ownership of a generic capability when an approved Java/platform/adopted-plugin/external-library contract can adequately satisfy the applicable software requirements and integration constraints without unacceptable compatibility, lifecycle, threading, security, licensing, operational, or maintainability risk. Project-owned implementation of an equivalent capability shall require an identified requirement/constraint that the external capability cannot adequately satisfy or an identified material risk reduced by Project ownership.

**Source:** SWE1-SRC-002 §4 CAN-COM-010; DEC-REQ-004 §6; GOV-ENG-001  
**Rationale:** Keeps the reuse-first principle in the SWE.1 trace chain while leaving concrete dependency/API selection to controlled design.  
**Precondition / trigger:** Whenever SWE.2/SWE.3 allocates or designs a generic capability that may be delegated to an existing platform, adopted plugin, or external library.  
**Required observable result:** A Project-owned duplicate implementation is not allocated solely for convenience; the approved design records the applicable reuse assessment and authoritative references required by `GOV-ENG-001`.  
**Verification intent:** SWE.2 capability-allocation inspection and SWE.3 dependency/reference assessment against `GOV-ENG-001`.  
**Priority:** `MUST`  
**Dependencies:** GOV-ENG-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
