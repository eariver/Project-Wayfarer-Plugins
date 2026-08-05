# Worlds Beyond Launchpad, Shop, Portal, and Administration Requirements

Document ID: `SWE1-WB-002`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `WB`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A  
Contained normative items: CAP: 16, CON: 7, IFC: 1, QLT: 4

## 1. Purpose

Define Launchpad item, placement, use, lifecycle, protection, reconciliation, shop transactions, vanilla portal denial, and theme administration.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-WB-002-CAP-001 — Launchpad item identity

**Normative statement:** Each unplaced Launchpad shall carry typed identity for item type, physical instance, definition, and schema.

**Source:** SWE1-SRC-002 §8 CAN-WB-008  
**Rationale:** Distinguishes valid launchpad consumables from visual copies.  
**Precondition / trigger:** A Launchpad is issued, purchased, transferred within allowed theme inventory, or placed.  
**Required observable result:** The item can be validated against the approved definition/schema without using display metadata alone.  
**Verification intent:** SWE.4 item-identity verification and SWE.5 Paper item integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CON-001 — Unplaced item state boundary

**Normative statement:** An unplaced Launchpad shall not store remaining uses or active-structure state and shall remain an ordinary Worlds Beyond consumable subject to normal death loss.

**Source:** SWE1-SRC-002 §8 CAN-WB-008  
**Rationale:** Separates consumable item identity from placed durable authority.  
**Precondition / trigger:** An unplaced item is inspected or the owner dies with it.  
**Required observable result:** No active-use state exists on the item and no free permanent-item recovery is created.  
**Verification intent:** SWE.4 data-policy verification and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-002 — Placement consumption semantics

**Normative statement:** A successful Launchpad placement shall consume exactly one current authorized Launchpad item; an unsuccessful placement shall consume none.

**Source:** SWE1-SRC-002 §8 CAN-WB-008; CAN-WB-009  
**Rationale:** Prevents silent loss and duplication.  
**Precondition / trigger:** A player attempts placement.  
**Required observable result:** Item count decreases once only after all placement and durable-authority conditions succeed.  
**Verification intent:** SWE.4 placement-coordinator verification, SWE.5 Paper/database integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-003 — Launchpad configurable defaults

**Normative statement:** The software shall support the initial configurable defaults: price 30 WM, purchase amount 1, initial free amount 2, maximum successful uses 3, expiration after 30 days without successful use, horizontal velocity 2.5, vertical velocity 1.2, cooldown 2 seconds, auto-Elytra enabled, and no per-player active limit.

**Source:** SWE1-SRC-002 §8 CAN-WB-008  
**Rationale:** Defines the initial approved balance while permitting controlled configuration.  
**Precondition / trigger:** Configuration is loaded and a launchpad is issued, purchased, placed, or used.  
**Required observable result:** The active behavior and quote use the approved current/default values according to snapshot rules.  
**Verification intent:** SWE.4 configuration verification and SWE.6 representative balance inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-004 — Launchpad placement eligibility

**Normative statement:** Launchpad placement shall require exact `frontier_iris`, a solid supporting top surface, air at the target, no liquid, a loaded chunk, world-border inclusion, spawn exclusion, WorldGuard permission, and no active-launchpad overlap.

**Source:** SWE1-SRC-002 §8 CAN-WB-009; AMD-004  
**Rationale:** Defines the current approved placement safety boundary.  
**Precondition / trigger:** A player attempts to place a current authorized Launchpad.  
**Required observable result:** Placement succeeds only when every listed condition is true.  
**Verification intent:** SWE.4 placement-policy verification, SWE.5 WorldGuard/Paper integration, SWE.6 representative client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CON-002 — No generic structure-exclusion requirement

**Normative statement:** The software shall not be required in this scope to implement an additional generic Portal, Gate, Waystone, or System-Structure exclusion registry beyond the explicit placement conditions in SWE1-WB-002-CAP-004.

**Source:** SWE1-SRC-002 §8 CAN-WB-009; AMD-004  
**Rationale:** Removes a source contradiction between broad exclusion wording and the later Owner-approved current scope.  
**Precondition / trigger:** Placement policy is assessed for scope completeness.  
**Required observable result:** Absence of a generic exclusion subsystem is not treated as nonconformance when the explicit required conditions are enforced.  
**Verification intent:** Inspection and traceability analysis.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-QLT-001 — Compensatable placement flow

**Normative statement:** Launchpad item consumption, durable authority creation, and physical block placement shall use a recoverable flow that prevents silent double consumption, duplicate active authority, item loss after a clear failure, or an unreported authoritative orphan.

**Source:** SWE1-SRC-002 §8 CAN-WB-009  
**Rationale:** Addresses the three-system consistency boundary without claiming external atomicity.  
**Precondition / trigger:** Any placement stage succeeds, fails, times out, or is replayed.  
**Required observable result:** The operation reaches one valid terminal or reconcilable state with no duplicate item/record/block effect.  
**Verification intent:** SWE.4 coordinator/idempotency verification and SWE.5 failure-injection integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-005 — Durable launchpad authority

**Normative statement:** An active Launchpad authority shall include stable launchpad identity, exact world/location, placer, successful-use count, maximum uses at creation, creation time, last-use time, expiration time, definition, state, schema, and optimistic-lock version.

**Source:** SWE1-SRC-002 §8 CAN-WB-010  
**Rationale:** Provides sufficient durable state for use, expiration, break, and reconciliation.  
**Precondition / trigger:** A launchpad placement commits.  
**Required observable result:** A complete active authority record exists and can be loaded after restart.  
**Verification intent:** Schema inspection and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-005  
**State:** `DRAFT`

### SWE1-WB-002-CAP-006 — Snapshot and live-config split

**Normative statement:** Maximum uses and expiration-related authority shall be fixed from the creation-time approved definition, while launch velocity, cooldown, and auto-Elytra shall use the current approved configuration at activation time.

**Source:** SWE1-SRC-002 §8 CAN-WB-010; AMD-003  
**Rationale:** Resolves the original unspecified snapshot scope.  
**Precondition / trigger:** An existing launchpad is used after relevant configuration changes.  
**Required observable result:** Durable creation values remain unchanged and designated live values reflect the current configuration.  
**Verification intent:** SWE.4 configuration-transition verification and SWE.5 reload/use integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-005  
**State:** `DRAFT`

### SWE1-WB-002-CON-003 — Placement orientation non-authority

**Normative statement:** Placement yaw or a stored orientation value shall not determine Launchpad launch direction; if retained for compatibility, it shall be non-authoritative metadata.

**Source:** SWE1-SRC-002 §8 CAN-WB-010; AMD-001  
**Rationale:** Resolves the prior fixed-yaw requirement.  
**Precondition / trigger:** An existing launchpad is activated by players looking in different directions.  
**Required observable result:** Stored placement orientation does not override the current user's direction.  
**Verification intent:** SWE.4 launch-vector policy verification and SWE.6 client motion qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-007 — Public step activation

**Normative statement:** Any eligible player stepping on an active Launchpad inside exact `frontier_iris` shall be able to activate it unless sneaking.

**Source:** SWE1-SRC-002 §8 CAN-WB-011  
**Rationale:** Defines public use and the intentional opt-out.  
**Precondition / trigger:** An eligible player enters the trigger area.  
**Required observable result:** Non-sneaking activation proceeds subject to claim/cooldown/safety; sneaking causes no launch or use increment.  
**Verification intent:** SWE.5 movement/event integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-008 — Current-look launch direction

**Normative statement:** Launch direction shall be derived from the activating player's current look direction and combined with the current configured horizontal and vertical launch behavior.

**Source:** SWE1-SRC-002 §8 CAN-WB-011; AMD-001  
**Rationale:** Makes launch skill responsive to the user rather than placement metadata.  
**Precondition / trigger:** A Launchpad activation passes eligibility checks.  
**Required observable result:** The applied velocity reflects the user's current viewing direction and approved live configuration.  
**Verification intent:** SWE.4 vector-policy verification, SWE.5 Paper velocity integration, SWE.6 motion qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-QLT-002 — Launch claim, cooldown, and safe execution

**Normative statement:** A successful launch shall acquire a single-use claim, enforce cooldown, pass safe-launch checks, avoid embedding the player in blocks, and apply auto-Elytra behavior when enabled.

**Source:** SWE1-SRC-002 §8 CAN-WB-011  
**Rationale:** Prevents concurrent double use and unsafe motion.  
**Precondition / trigger:** One or more players attempt to activate the same launchpad.  
**Required observable result:** Only valid claimed activations launch; denied/unsafe activations do not increment use count.  
**Verification intent:** SWE.4 concurrency/safety verification, SWE.5 movement integration, SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-009 — Successful-use state update

**Normative statement:** Each successful launch shall increment successful-use count exactly once, update `last_used_at`, extend expiration according to the durable policy, and remove the launchpad when the maximum-use boundary is reached.

**Source:** SWE1-SRC-002 §8 CAN-WB-011  
**Rationale:** Defines the consumable structure lifecycle.  
**Precondition / trigger:** A launch succeeds.  
**Required observable result:** Durable and physical state reflect exactly one use, and terminal use removes active authority/block once.  
**Verification intent:** SWE.4 state-transition verification, SWE.5 database/block integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-010 — Manual break behavior

**Normative statement:** Any player shall be able to normally break an active Launchpad; a successful break shall produce no item, remove the physical block and active authority exactly once, and produce an audit reference.

**Source:** SWE1-SRC-002 §8 CAN-WB-012  
**Rationale:** Defines public cleanup without item recovery.  
**Precondition / trigger:** A non-cancelled ordinary player break targets an active launchpad.  
**Required observable result:** The block and active record are removed with no drop and no duplicate deletion.  
**Verification intent:** SWE.5 Paper/database integration and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CON-004 — Cancelled and duplicate break safety

**Normative statement:** A cancelled break or repeated break processing shall not remove active authority, generate a drop, or apply deletion twice.

**Source:** SWE1-SRC-002 §8 CAN-WB-012  
**Rationale:** Protects event interoperability and idempotency.  
**Precondition / trigger:** The break event is cancelled or the same physical/durable deletion is replayed.  
**Required observable result:** State remains valid for cancellation or reaches the same single deleted disposition for replay.  
**Verification intent:** SWE.4 policy/idempotency verification and SWE.5 event integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-011 — Active launchpad environmental protection

**Normative statement:** Through supported platform or adopted-plugin contracts, active Launchpad locations shall be protected from explosion, fire/burn, fluid, piston, entity block change, falling block, block spread, tree/mushroom growth, structure generation, supported WorldEdit/FAWE editing, and mob griefing.

**Source:** SWE1-SRC-002 §8 CAN-WB-012  
**Rationale:** Preserves durable/physical consistency against common world mutation.  
**Precondition / trigger:** A listed supported mutation targets an active launchpad location.  
**Required observable result:** The mutation does not silently remove or transform the active block outside an approved removal/reconciliation path.  
**Verification intent:** SWE.5 representative Paper/WorldGuard/WorldEdit integration and inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-004  
**State:** `DRAFT`

### SWE1-WB-002-CAP-012 — Expiration basis and due removal

**Normative statement:** Launchpad expiration shall be based on `last_used_at` after at least one successful use and otherwise on `created_at`; an overdue active launchpad shall be removed according to the idempotent lifecycle policy.

**Source:** SWE1-SRC-002 §8 CAN-WB-013  
**Rationale:** Provides durable time-based lifecycle without tick blocking.  
**Precondition / trigger:** A launchpad reaches expiration during runtime or downtime.  
**Required observable result:** The due time is derived from the correct timestamp and overdue physical/durable state reaches one deleted disposition.  
**Verification intent:** SWE.4 expiration-policy verification and SWE.5 restart/scheduler integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-013 — Record-without-block reconciliation

**Normative statement:** The software shall detect and provide authorized reconciliation for a durable active Launchpad record whose expected physical block is absent.

**Source:** SWE1-SRC-002 §8 CAN-WB-013  
**Rationale:** Provides recovery for authoritative database residue.  
**Precondition / trigger:** Inspection or scheduled reconciliation finds an active record with no matching physical block.  
**Required observable result:** The discrepancy is reported and can be resolved idempotently without issuing a free Launchpad item.  
**Verification intent:** SWE.4 classification verification and SWE.5 reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CON-005 — No arbitrary block-only classification

**Normative statement:** The software shall not be required to classify an arbitrary `LIGHT_WEIGHTED_PRESSURE_PLATE` with no durable Launchpad record as a Launchpad; such a block shall remain ordinary non-authoritative world state.

**Source:** SWE1-SRC-002 §8 CAN-WB-013; AMD-005  
**Rationale:** Resolves the impossibility of distinguishing an orphan launchpad plate from an ordinary identical block without durable identity.  
**Precondition / trigger:** A physical pressure plate exists at a location with no active launchpad authority.  
**Required observable result:** No launch, deletion, reissue, or launchpad reconciliation is performed solely because of the material.  
**Verification intent:** SWE.4 classification verification and inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-014 — Frontier shop catalog

**Normative statement:** Inside exact `frontier_iris`, the shop shall offer Launchpad ×1 for 30 WM and a non-explosive Flight Duration 3 Firework Rocket ×1 for 200 WM.

**Source:** SWE1-SRC-002 §8 CAN-WB-014  
**Rationale:** Defines the approved initial shop capability.  
**Precondition / trigger:** An authorized player opens the current shop.  
**Required observable result:** The two products, quantities, and current approved prices are available.  
**Verification intent:** SWE.4 catalog verification and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CON-006 — Waystone purchase denial

**Normative statement:** While Waystone is unavailable, the Waystone Placement Tool shall not be sold and a direct purchase request shall be rejected before debit.

**Source:** SWE1-SRC-002 §8 CAN-WB-014; §9 CAN-SCOPE-002  
**Rationale:** Prevents payment for unavailable functionality.  
**Precondition / trigger:** The catalog is displayed or a direct Waystone purchase route is invoked.  
**Required observable result:** No Waymark debit, delivery, or Waystone authority occurs.  
**Verification intent:** SWE.4 action-policy verification, SWE.5 transaction integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-QLT-003 — Shop transaction safety

**Normative statement:** Shop purchases shall use the Core transaction boundary with operation identity, duplicate-click protection, current quote/item validation, and typed pending delivery or approved refund for a clear post-debit delivery failure; `UNKNOWN` shall not be treated as success or automatically retried.

**Source:** SWE1-SRC-002 §8 CAN-WB-014; §4 CAN-COM-007  
**Rationale:** Protects funds and delivery under replay and partial failure.  
**Precondition / trigger:** A player confirms a shop purchase and any later delivery stage succeeds, fails, or becomes ambiguous.  
**Required observable result:** At most one debit and one item entitlement result from one operation identity; unresolved outcomes remain reconcilable.  
**Verification intent:** SWE.4 transaction/delivery verification, SWE.5 Core/Frontier integration, SWE.6 representative purchase qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-CAP-015 — Vanilla portal denial

**Normative statement:** Vanilla portal traversal from or within exact `frontier_iris` shall be denied.

**Source:** SWE1-SRC-002 §8 CAN-WB-015; AMD-002  
**Rationale:** Adds the explicit Worlds Beyond portal boundary approved after the original source.  
**Precondition / trigger:** A player attempts a vanilla Nether, End, gateway, or equivalent portal traversal covered by the selected platform contract in exact `frontier_iris`.  
**Required observable result:** Traversal does not complete and no unintended dimension/world transfer occurs.  
**Verification intent:** SWE.5 Paper portal integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-006  
**State:** `DRAFT`

### SWE1-WB-002-CON-007 — No Gate or return-structure ownership

**Normative statement:** Portal denial shall not cause Wayfarer_Frontier to generate worlds, own Gate lifecycle, or implement an in-world return structure in this scope.

**Source:** SWE1-SRC-002 §8 CAN-WB-015; §9 CAN-SCOPE-002  
**Rationale:** Keeps portal policy separate from Project-owned travel infrastructure.  
**Precondition / trigger:** Portal denial and return-path design are assessed.  
**Required observable result:** The plugin only enforces the approved denial and relies on an externally authorized return mechanism.  
**Verification intent:** Architecture/traceability inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-006  
**State:** `DRAFT`

### SWE1-WB-002-CAP-016 — Worlds Beyond administration

**Normative statement:** The software shall provide authorized capabilities for loadout inspection/reissue, delivery inspection/retry, Launchpad inspection/removal/reconciliation, transaction inspection, and audit reference.

**Source:** SWE1-SRC-002 §8 CAN-WB-016  
**Rationale:** Provides operational recovery for the approved theme domains.  
**Precondition / trigger:** An authorized administrator invokes a supported operation.  
**Required observable result:** Only the documented domain state is inspected or changed, with an auditable actionable result.  
**Verification intent:** SWE.5 command integration and SWE.6 representative administration qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-002-IFC-001 — Worlds Beyond permission mapping

**Normative statement:** Worlds Beyond gameplay, delivery, Launchpad, reconciliation, transaction-inspection, and debug routes shall be protected by the applicable Frontier permission nodes defined in SWE1-FRONTIER-001-IFC-002.

**Source:** SWE1-SRC-002 §7 CAN-FRONTIER-005; §8 CAN-WB-016; AMD-011  
**Rationale:** Connects theme capabilities to the approved external permission interface.  
**Precondition / trigger:** A protected player or administrator route is invoked.  
**Required observable result:** The route directly evaluates its assigned permission group before any protected effect.  
**Verification intent:** SWE.4 permission allocation verification, SWE.5 route integration, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-008  
**State:** `DRAFT`

### SWE1-WB-002-QLT-004 — Asynchronous expiration and restart catch-up

**Normative statement:** Expiration candidates shall be searched without main-thread blocking I/O, physical world mutation shall occur on the main thread, and overdue expiration shall be processed after restart.

**Source:** SWE1-SRC-002 §8 CAN-WB-013  
**Rationale:** Ensures timed cleanup is thread-safe and durable across downtime.  
**Precondition / trigger:** The scheduler runs or the plugin restarts with overdue active launchpads.  
**Required observable result:** Candidate lookup does not block the main thread and overdue records are safely revalidated and removed.  
**Verification intent:** SWE.4 scheduler/thread verification and SWE.5 restart/expiration integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
