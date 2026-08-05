# Frontier Plugin Boundary, MVI, Persistence, and Permission Requirements

Document ID: `SWE1-FRONTIER-001`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `FRONTIER`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A  
Contained normative items: CAP: 2, CON: 8, IFC: 2, QLT: 2

## 1. Purpose

Define the Frontier backend role, exact theme boundary, MVI non-ownership, Frontier durable-state ownership, extensibility, and permission interface.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-FRONTIER-001-CON-001 — Frontier-only activation

**Normative statement:** Wayfarer_Frontier shall activate only on the Frontier backend, shall require compatible Wayfarer_Core capability, and shall not activate on Main or Lobby.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-001  
**Rationale:** Maintains deployment and gameplay ownership boundaries.  
**Precondition / trigger:** At plugin enablement.  
**Required observable result:** Gameplay activates only in the approved backend role with compatible Core services.  
**Verification intent:** SWE.5 deployment/lifecycle integration and SWE.6 Frontier qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-002 — No world generation ownership

**Normative statement:** Wayfarer_Frontier shall not generate, recreate, or select the Worlds Beyond world.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-001; §9 CAN-SCOPE-002  
**Rationale:** Preserves Iris/Project world-generation authority.  
**Precondition / trigger:** At enablement or when the target world is unavailable.  
**Required observable result:** No world-generation operation is initiated by the plugin.  
**Verification intent:** Static inspection and SWE.5 lifecycle integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-001  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CAP-001 — Exact Worlds Beyond recognition

**Normative statement:** Wayfarer_Frontier shall recognize exact `frontier_iris` as the only Worlds Beyond gameplay world.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-002  
**Rationale:** Provides one deterministic theme boundary.  
**Precondition / trigger:** A player, item, command, shop, launchpad, or portal action is evaluated.  
**Required observable result:** The context is accepted as Worlds Beyond only when the exact world identifier matches.  
**Verification intent:** SWE.4 world-policy verification and SWE.6 Frontier/WB qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-003 — Unknown and alternate world denial

**Normative statement:** Similar names, unknown worlds, every Nether world, every End world, and all worlds other than exact `frontier_iris` shall be denied for initial loadout, theme-bound item use, navigation, launchpad, shop, portal policy, and other Worlds Beyond behavior.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-002  
**Rationale:** Prevents prefix and environment inference from widening scope.  
**Precondition / trigger:** A protected Frontier/WB operation is attempted outside exact `frontier_iris`.  
**Required observable result:** No protected gameplay, delivery, financial, or authority mutation occurs.  
**Verification intent:** SWE.4 negative world-policy verification, SWE.5 event/command integration, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-IFC-001 — MVI group authority

**Normative statement:** The software shall treat MVI as authority for normal Frontier player state, shall recognize the Project group set `neutral`, `worlds_beyond`, and `guild`, and shall rely on `frontier_iris` belonging only to `worlds_beyond` without changing MVI profiles.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-003  
**Rationale:** Prevents duplicate player-state ownership.  
**Precondition / trigger:** A player enters, leaves, dies, reconnects, or moves between backends/themes.  
**Required observable result:** Wayfarer_Frontier does not save, restore, or switch the normal MVI-managed profile.  
**Verification intent:** Source inspection, SWE.5 MVI integration, SWE.6 cross-backend qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-004 — No normal Frontier player-state persistence

**Normative statement:** Wayfarer_Frontier shall not persist or restore normal inventory, armor, offhand, Ender Chest, XP, health, food, or MVI profile state.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-003; CAN-FRONTIER-004  
**Rationale:** Keeps typed Wayfarer state separate from MVI-owned state.  
**Precondition / trigger:** Whenever player state is persisted or reconstructed.  
**Required observable result:** Only approved typed item identity and plugin-owned domain state are stored.  
**Verification intent:** Schema/source inspection and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-005 — No duplicate transition ownership

**Normative statement:** Wayfarer_Frontier shall not duplicate Gate, respawn, reconnect, or backend profile-transition behavior owned by Project runtime/MVI.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-003  
**Rationale:** Avoids conflicting player-state transitions.  
**Precondition / trigger:** A runtime transition occurs.  
**Required observable result:** The plugin reacts only through its approved safe-entry/item obligations and does not perform a second profile transition.  
**Verification intent:** SWE.5 lifecycle/MVI integration and inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CAP-002 — Typed Frontier durable state

**Normative statement:** Frontier durable persistence shall support typed traversal identities, initial and pending delivery, active launchpad authority/history, shop pending delivery, and necessary placement/transaction operation state.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-004  
**Rationale:** Provides the minimum durable domains required for approved Frontier/WB behavior.  
**Precondition / trigger:** An approved Frontier/WB durable operation occurs.  
**Required observable result:** The operation can be recovered and reconciled without storing raw normal player state.  
**Verification intent:** Schema inspection and SWE.5 database integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-006 — Frontier persistence ownership

**Normative statement:** Frontier-owned durable records shall use the `wf_frontier_*` namespace and shall not create or modify Core/Main tables.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; §7 CAN-FRONTIER-004  
**Rationale:** Maintains migration and module ownership.  
**Precondition / trigger:** A Frontier schema or record is created or upgraded.  
**Required observable result:** Only Frontier-owned structures are modified by Frontier migrations and repositories.  
**Verification intent:** Migration/schema inspection and SWE.5 combined-schema integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-QLT-001 — Frontier pending-state recoverability

**Normative statement:** Typed pending delivery and durable Frontier operations shall survive restart and shall prevent duplicate delivery or stale replay.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-004; §8 CAN-WB-002; CAN-WB-004  
**Rationale:** Provides restart-safe item obligations without raw inventory persistence.  
**Precondition / trigger:** The process stops after a durable obligation is created but before delivery completes.  
**Required observable result:** After restart, one recoverable obligation remains and no duplicate physical item is produced.  
**Verification intent:** SWE.4 state/idempotency verification and SWE.5 restart integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-IFC-002 — Frontier permission nodes

**Normative statement:** Frontier gameplay and administrative entry points shall use `wayfarer.frontier.use`, `wayfarer.frontier.admin.read`, `wayfarer.frontier.admin.delivery`, `wayfarer.frontier.admin.launchpad`, `wayfarer.frontier.admin.reconcile`, and `wayfarer.frontier.debug`.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-005; AMD-011  
**Rationale:** Fixes the approved medium-grained permission interface.  
**Precondition / trigger:** A gameplay, command, reconciliation, or debug route is invoked.  
**Required observable result:** The route enforces its applicable node and denies unauthorized protected mutation.  
**Verification intent:** SWE.4 permission-policy verification, SWE.5 permission integration, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-008  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-007 — Direct Frontier permission enforcement

**Normative statement:** An optional umbrella node may grant child permissions, but each Frontier handler shall directly enforce the applicable approved permission group.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-005; AMD-011  
**Rationale:** Prevents broad-node privilege leakage.  
**Precondition / trigger:** A protected handler is reached through any registered route.  
**Required observable result:** Callers lacking the handler's group are denied even if command visibility or a broad node differs.  
**Verification intent:** SWE.4 route-to-permission verification and SWE.5 direct invocation integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-008 — Frontier debug dual gate

**Normative statement:** Frontier debug actions shall be disabled by default and shall require both explicit configuration enablement and `wayfarer.frontier.debug`.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-005  
**Rationale:** Prevents accidental production debug exposure.  
**Precondition / trigger:** A debug action is requested.  
**Required observable result:** No debug behavior occurs unless both gates are true.  
**Verification intent:** SWE.4 gate verification and SWE.5 configuration/permission integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-QLT-002 — Forward-extensible Frontier persistence

**Normative statement:** Frontier persistence shall permit forward-only addition of later explicitly approved Frontier domains without requiring destructive reset or treating incomplete Waystone scaffolding as current runtime authority.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-004  
**Rationale:** Preserves future schema evolution while keeping current unavailable functionality inactive.  
**Precondition / trigger:** A current schema is installed/upgraded or a future-domain migration is analyzed.  
**Required observable result:** Current runtime exposes only approved domains, and the schema can be extended through new Frontier-owned migrations without repurposing normal player state.  
**Verification intent:** SWE.2 data-ownership analysis, schema inspection, and SWE.5 migration integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
