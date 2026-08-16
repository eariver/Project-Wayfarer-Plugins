# Frontier Plugin Boundary, MVI, Persistence, and Permission Requirements

Document ID: `SWE1-FRONTIER-001`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `FRONTIER`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision B  
Controlling Common review decisions: `DEC-REQ-002`, `DEC-REQ-004`  
Contained normative items: CAP: 2, CON: 8, IFC: 2, QLT: 2

## 1. Purpose

Define the Frontier gameplay boundary, MVI non-ownership, Frontier durable-state ownership, extensibility, and permission interface while carrying target-specific deployment/recovery details to the later Frontier-clause Owner review.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue or explicit Common/target conflict remains draft and cannot support G1 PASS until resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- The Worlds Beyond gameplay-world identifier is treated as configuration/target context rather than a Common fixed server/world-name mechanism. Exact Frontier clauses remain subject to their own Owner review.

## 3. Requirements

### SWE1-FRONTIER-001-CON-001 — Frontier activation allocation pending target review

**Normative statement:** The current unreviewed `CAN-FRONTIER-001` assigns Wayfarer_Frontier to Frontier gameplay and requires the approved shared capabilities needed by that gameplay; fixed physical backend-name gating is not reaffirmed by this requirement.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-001; §4 CAN-COM-001; DEC-REQ-002 §2  
**Rationale:** Preserves Frontier feature ownership while explicitly carrying the old fixed-backend wording to target review instead of overriding the approved Common topology rule.  
**Precondition / trigger:** At deployment and capability enablement.  
**Required observable result:** Pending `CAN-FRONTIER-001` Owner review; actual functional prerequisites rather than historical backend name control capability availability under Common requirements.  
**Verification intent:** Deferred target deployment/topology cases until `CAN-FRONTIER-001` is reconciled.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-001; SWE1-COMMON-001-CON-002  
**Assumptions:** None  
**Open issue / conflict:** Current canonical `CAN-FRONTIER-001` fixed-backend/Core wording conflicts with the approved Common topology/shared-ownership direction and requires target-clause review.  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-002 — No world generation ownership and missing-world fail-closed

**Normative statement:** Wayfarer_Frontier shall not generate or recreate the configured Worlds Beyond gameplay world. When that configured world is absent at enablement and the plugin's runtime gameplay capabilities all depend on it, the plugin shall remain unavailable/fail enable rather than generating the world or exposing affected gameplay.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-001; §9 CAN-SCOPE-002; §4 CAN-COM-006; DEC-REQ-004 §2.3  
**Rationale:** Preserves external world-generation authority and applies the Owner-approved capability/whole-plugin fail-closed direction.  
**Precondition / trigger:** Plugin enablement or required-world availability check.  
**Required observable result:** No world-generation operation is initiated and no dependent Frontier gameplay becomes available while the configured world is absent.  
**Verification intent:** Static inspection and SWE.5 lifecycle/missing-world integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-003; SWE1-COMMON-001-QLT-004  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-001 — exact health/status and later recovery/re-enable behavior remains unresolved.  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CAP-001 — Configured Worlds Beyond gameplay-world recognition

**Normative statement:** Wayfarer_Frontier shall recognize exactly one configured Worlds Beyond gameplay-world identifier as the current Worlds Beyond gameplay context; similar or unrelated world identifiers shall not be inferred as equivalent.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-002; §4 CAN-COM-006; DEC-REQ-004 §2.3  
**Rationale:** Preserves a deterministic exact theme boundary without fixing the literal `frontier_iris` name in SWE.1.  
**Precondition / trigger:** A player, item, command, shop, launchpad, portal, or other Worlds Beyond action is evaluated.  
**Required observable result:** The context is accepted as Worlds Beyond only when the exact configured gameplay-world identifier matches.  
**Verification intent:** SWE.4 world-policy verification and SWE.6 Frontier/WB qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-003  
**Assumptions:** The initial integration configuration may retain the historical `frontier_iris` value; the literal is not the software invariant.  
**Open issue / conflict:** Canonical `CAN-FRONTIER-002` still contains the literal name and requires target-clause consolidation during its Owner review.  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-003 — Unknown and alternate world denial

**Normative statement:** Worlds other than the exact configured Worlds Beyond gameplay world, including similar names and unrelated Nether/End/other contexts, shall be denied for initial loadout, theme-bound item use, navigation, launchpad, shop, portal policy, and other Worlds Beyond behavior unless a later approved target requirement explicitly expands the context set.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-002; DEC-REQ-004 §2.3  
**Rationale:** Prevents prefix/environment inference from widening scope while permitting the configured identifier to change.  
**Precondition / trigger:** A protected Frontier/WB operation is attempted outside the configured gameplay world.  
**Required observable result:** No protected gameplay, delivery, financial, or authority mutation occurs.  
**Verification intent:** SWE.4 negative world-policy verification, SWE.5 event/command integration, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-FRONTIER-001-CAP-001  
**Assumptions:** None  
**Open issue / conflict:** Exact target-world policy remains subject to `CAN-FRONTIER-002` Owner review.  
**State:** `DRAFT`

### SWE1-FRONTIER-001-IFC-001 — MVI group authority

**Normative statement:** The software shall treat MVI as authority for normal Frontier player state, shall recognize the Project group set `neutral`, `worlds_beyond`, and `guild`, and shall rely on the configured Worlds Beyond gameplay world belonging only to the approved `worlds_beyond` MVI group without Wayfarer changing MVI profiles.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-003; §4 CAN-COM-003  
**Rationale:** Prevents duplicate player-state ownership while separating the configured world identifier from MVI authority.  
**Precondition / trigger:** A player enters, leaves, dies, reconnects, or moves between relevant contexts.  
**Required observable result:** Wayfarer_Frontier does not save, restore, or switch the normal MVI-managed profile.  
**Verification intent:** Source/configuration inspection, SWE.5 MVI integration, SWE.6 cross-context qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-003  
**Assumptions:** None  
**Open issue / conflict:** Exact Project MVI/world assignment remains subject to `CAN-FRONTIER-003` Owner review.  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-004 — No normal Frontier player-state persistence

**Normative statement:** Wayfarer_Frontier shall not persist or restore normal inventory, armor, offhand, Ender Chest, XP, health, food, or MVI profile state as the general/long-term player-state authority.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-003; CAN-FRONTIER-004; §4 CAN-COM-004  
**Rationale:** Keeps typed Wayfarer state separate from MVI-owned normal state.  
**Precondition / trigger:** Whenever player state is persisted or reconstructed.  
**Required observable result:** Only approved typed Wayfarer identity/obligation/domain state is stored; normal player-state authority remains external.  
**Verification intent:** Schema/source inspection and SWE.5 persistence/MVI integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-004; SWE1-COMMON-001-IFC-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-005 — No duplicate transition ownership

**Normative statement:** Wayfarer_Frontier shall not duplicate Gate, respawn, reconnect, or profile-transition behavior owned by Project runtime/MVI.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-003  
**Rationale:** Avoids conflicting player-state transitions.  
**Precondition / trigger:** A runtime/context transition occurs.  
**Required observable result:** The plugin reacts only through its approved safe-entry/item obligations and does not perform a second externally owned profile transition.  
**Verification intent:** SWE.5 lifecycle/MVI integration and inspection.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-003  
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
**Dependencies:** SWE1-COMMON-001-IFC-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-FRONTIER-001-CON-006 — Frontier durable-schema ownership

**Normative statement:** Durable state allocated to the Frontier ownership domain shall be evolved through Frontier-owned schema/migration authority and shall not be migrated through, written into, altered within, or repurposed from schema objects owned by another Wayfarer durable-state owner merely to satisfy Frontier persistence needs.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; §7 CAN-FRONTIER-004; DEC-REQ-004 §4  
**Rationale:** Maintains Frontier persistence ownership without fixing a `wf_frontier_*` table-prefix mechanism at SWE.1.  
**Precondition / trigger:** A Frontier schema object, record domain, migration, installation, or upgrade is defined or executed.  
**Required observable result:** Frontier durable objects/migrations are attributable to Frontier ownership and another owner's schema is not changed by Frontier migration responsibility.  
**Verification intent:** SWE.2 data-ownership inspection, migration/schema inspection, SWE.5 multi-owner migration integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-002; SWE1-COMMON-001-IFC-006  
**Assumptions:** None  
**Open issue / conflict:** Exact Frontier physical schema naming remains a downstream design/accepted-baseline matter and will be rechecked under `CAN-FRONTIER-004`.  
**State:** `DRAFT`

### SWE1-FRONTIER-001-QLT-001 — Frontier pending-state recoverability

**Normative statement:** Typed pending-delivery and protected durable Frontier operations shall survive normal runtime restart and shall preserve one established entitlement/operation identity without duplicate final delivery or stale replay effect.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-004; §8 CAN-WB-002; CAN-WB-004; §4 CAN-COM-007  
**Rationale:** Provides restart-safe item obligations without raw inventory persistence and aligns Frontier recovery with Common replay containment.  
**Precondition / trigger:** Runtime stops after a durable obligation/operation is established but before final delivery/disposition completes.  
**Required observable result:** After restart, one recoverable obligation/operation remains and replay does not create a second entitlement or final delivered item.  
**Verification intent:** SWE.4 state/idempotency verification and SWE.5 restart/delivery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-012  
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

**Normative statement:** Frontier persistence shall permit addition of later explicitly approved Frontier durable domains through new owner-attributable forward migrations without destructive automatic reset or treating incomplete future-domain scaffolding as current runtime authority.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-004; §4 CAN-COM-008  
**Rationale:** Preserves future schema evolution while keeping unavailable functionality inactive and accepted state intact.  
**Precondition / trigger:** Current schema installation/upgrade or analysis of a later approved Frontier domain.  
**Required observable result:** Current runtime exposes only approved domains, and later Frontier-owned evolution can proceed through new migrations without repurposing normal player state or another owner's schema.  
**Verification intent:** SWE.2 data-ownership analysis, schema inspection, SWE.5 migration upgrade integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-006; SWE1-COMMON-001-QLT-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
