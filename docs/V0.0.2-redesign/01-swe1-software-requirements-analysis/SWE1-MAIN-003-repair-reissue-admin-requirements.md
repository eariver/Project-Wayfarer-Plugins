# Main GUI, Repair, Reissue, Administration, and Permission Requirements

Document ID: `SWE1-MAIN-003`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A  
Contained normative items: CAP: 9, CON: 4, IFC: 1, QLT: 5

## 1. Purpose

Define Main user-management entry, repair and paid reissue transactions, administrative operations, permission interfaces, and presentation constraints.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-MAIN-003-CAP-001 — Management GUI entry

**Normative statement:** With the current authorized Growth Tool or Broken Tool in the main hand, an air right-click that does not target a block or entity shall open the Main management GUI; off-hand interaction shall not open it.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014  
**Rationale:** Defines the externally visible management entry contract.  
**Precondition / trigger:** The current owner performs the specified interaction with a current authorized item and has applicable use permission.  
**Required observable result:** Exactly one management GUI opens for the owner; excluded interactions do not open it.  
**Verification intent:** SWE.5 Paper event integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-002 — Management status presentation

**Normative statement:** The management GUI shall present tool type, status, material, conceptual evolution count, cumulative progress, next-threshold state, enchantments, active branch, durability, repair cost preview, and any active configuration clamp or equivalent state.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014  
**Rationale:** Provides the information needed to understand and operate the tool.  
**Precondition / trigger:** The authorized GUI is opened.  
**Required observable result:** The current authoritative values are visible and consistent with the logical/physical state.  
**Verification intent:** SWE.4 presentation-model verification and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-003 — Management actions

**Normative statement:** The management GUI shall provide access to full repair and Help/Status behavior, with explicit preview and confirm/cancel semantics for a financial action.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; CAN-MAIN-015  
**Rationale:** Prevents accidental payment and exposes required management capability.  
**Precondition / trigger:** The owner selects an available management action.  
**Required observable result:** Non-financial help/status is shown without debit; repair requires an explicit confirm after preview.  
**Verification intent:** SWE.5 GUI-flow integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-001 — GUI replay safety

**Normative statement:** Double click, lag, inventory-event replay, disconnect, or reopening a stale GUI shall not cause duplicate debit, duplicate repair, duplicate reissue, or authority mutation.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; CAN-MAIN-015; CAN-MAIN-016  
**Rationale:** Protects financial and authority effects at a high-replay user interface.  
**Precondition / trigger:** A financial confirmation is submitted more than once or after its session becomes stale.  
**Required observable result:** At most one operation identity is accepted; stale/replayed submissions cause no duplicate effect.  
**Verification intent:** SWE.4 session/idempotency verification, SWE.5 GUI integration, SWE.6 representative client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-004 — Full repair pricing

**Normative statement:** The software shall offer only full repair and shall compute the initial price using: full repair base `ceil(100 × (1 + evolution_count × 0.08))`; active repair `ceil(full_repair_cost × max(0.25, missing_durability_ratio))`; broken repair `full_repair_cost + 100 + evolution_count × 5`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015  
**Rationale:** Defines the approved initial economy result.  
**Precondition / trigger:** An authorized repair preview is requested.  
**Required observable result:** The quoted amount equals the applicable formula using current authoritative evolution/durability state.  
**Verification intent:** SWE.4 formula/boundary verification and SWE.6 representative price qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CON-001 — No repair at full durability

**Normative statement:** An active item at maximum durability shall not be repairable and shall have a repair cost of 0 WM.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015  
**Rationale:** Prevents meaningless payment.  
**Precondition / trigger:** Repair preview or confirm is requested for a fully durable active item.  
**Required observable result:** The operation is unavailable or rejected before debit and displays 0 WM.  
**Verification intent:** SWE.4 policy verification and SWE.6 GUI qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-005 — Repair transaction execution

**Normative statement:** A confirmed repair shall execute through the Core Waymark transaction boundary using transaction identity, current player/tool authority validation, quote revalidation, and same-player/tool serialization.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015  
**Rationale:** Ensures payment and repair use current authority and shared economy semantics.  
**Precondition / trigger:** The owner confirms a non-stale eligible repair quote.  
**Required observable result:** One authorized debit attempt and one corresponding repair operation are coordinated.  
**Verification intent:** SWE.4 coordinator verification, SWE.5 Core/Main transaction integration, SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-002 — Repair compensation on clear failure

**Normative statement:** A clear Waymark debit followed by a clear downstream repair failure shall use the approved refund or compensation path, and duplicate refund shall be prevented.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007  
**Rationale:** Protects player funds without inventing atomic guarantees.  
**Precondition / trigger:** A repair operation fails or becomes ambiguous after provider interaction.  
**Required observable result:** The operation reaches one compensated or compensation-pending disposition without a second refund effect.  
**Verification intent:** SWE.4 failure-policy verification and SWE.5 provider-failure integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-006 — Player-paid missing-tool reissue

**Normative statement:** The owner shall be able to request paid reissue of a missing Growth Tool through an explicit quote and confirm flow.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; AMD-007  
**Rationale:** Provides the approved recovery path after death or loss without automatic restoration.  
**Precondition / trigger:** The owner has a logical tool but no eligible current physical item or pending delivery and invokes reissue in an allowed context.  
**Required observable result:** A reissue quote is shown and no debit or authority rotation occurs until explicit confirmation.  
**Verification intent:** SWE.5 command/GUI flow integration and SWE.6 Main qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-007  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-007 — Reissue pricing

**Normative statement:** The paid reissue quote shall equal the current broken-repair cost plus the current full-repair cost for the logical tool.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; AMD-007  
**Rationale:** Makes reissue more expensive than controlled item-present recovery.  
**Precondition / trigger:** An eligible reissue quote is calculated.  
**Required observable result:** The quoted WM amount equals `broken_repair_cost + full_repair_cost` based on current authoritative state.  
**Verification intent:** SWE.4 formula verification and SWE.6 representative price qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-008 — Successful reissue result

**Normative statement:** A successful paid reissue shall preserve the logical tool ID, cumulative progress, and active branch; create a new physical instance ID; increment the epoch; invalidate all older physical instances; set the logical state to `ACTIVE`; set damage to zero; and deliver the item immediately or through typed pending delivery.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; AMD-006; AMD-007  
**Rationale:** Defines complete authority rotation and recovery semantics.  
**Precondition / trigger:** A current eligible quote is explicitly confirmed and the financial/authority operation succeeds.  
**Required observable result:** Exactly one new current physical authority exists or is pending, old epochs are denied, and preserved gameplay state is unchanged.  
**Verification intent:** SWE.4 transition verification, SWE.5 transaction/delivery integration, SWE.6 reissue qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CON-002 — Paid reissue pre-debit rejection

**Normative statement:** Paid reissue shall be rejected before debit when a current authorized physical item exists or a typed pending delivery already exists; the player shall be directed to the free delivery-retry path when pending delivery is present.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; AMD-007  
**Rationale:** Prevents paying for a duplicate or bypassing an existing free obligation.  
**Precondition / trigger:** A reissue is requested while current physical authority or pending delivery exists.  
**Required observable result:** No provider debit, epoch rotation, new instance, or duplicate delivery is created; an actionable denial is returned.  
**Verification intent:** SWE.4 precondition verification, SWE.5 command/transaction integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-003 — Reissue replay and UNKNOWN safety

**Normative statement:** Replay, duplicate confirmation, timeout recovery, or `UNKNOWN` shall not cause duplicate debit, duplicate epoch increment, duplicate physical instance, or automatic re-execution of the provider effect.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; §4 CAN-COM-007  
**Rationale:** Protects both economy and logical authority.  
**Precondition / trigger:** The same reissue operation is submitted or recovered multiple times.  
**Required observable result:** The established operation identity yields at most one provider effect and one authority rotation; unresolved effects remain reconcilable.  
**Verification intent:** SWE.4 idempotency/race verification and SWE.5 failure/replay integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-009 — Main administrative capabilities

**Normative statement:** Wayfarer_Main shall provide authorized capabilities for inspect, grant, administrative reissue, repair, branch change, revoke, reconcile, and pending-delivery retry.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-018  
**Rationale:** Provides required operational recovery and control.  
**Precondition / trigger:** An authorized administrator invokes a supported operation with valid arguments.  
**Required observable result:** The operation performs only its documented state change, is auditable, and returns an actionable disposition.  
**Verification intent:** SWE.5 command integration and SWE.6 representative administration qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-IFC-001 — Main permission nodes

**Normative statement:** Main gameplay and administrative entry points shall use the nodes `wayfarer.main.use`, `wayfarer.main.admin.read`, `wayfarer.main.admin.delivery`, `wayfarer.main.admin.modify`, `wayfarer.main.admin.reconcile`, and `wayfarer.main.debug`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-018; AMD-011  
**Rationale:** Fixes the approved medium-grained permission interface.  
**Precondition / trigger:** A player or administrator reaches a gameplay, command, or debug entry point.  
**Required observable result:** Access is evaluated against the directly applicable node; absence of the node denies the protected action without protected mutation.  
**Verification intent:** SWE.4 permission-policy verification, SWE.5 LuckPerms/Bukkit permission integration, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-008  
**State:** `DRAFT`

### SWE1-MAIN-003-CON-003 — Direct permission-group enforcement

**Normative statement:** An optional umbrella permission may grant child nodes, but each Main gameplay or command handler shall directly enforce the applicable approved group rather than relying only on a broad admin node or command visibility.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-018; AMD-011  
**Rationale:** Prevents accidental privilege expansion.  
**Precondition / trigger:** A protected handler is invoked directly or through an alternate command/event route.  
**Required observable result:** The handler independently denies callers lacking its required group.  
**Verification intent:** SWE.4 route-to-permission verification, SWE.5 direct invocation integration, SWE.6 representative deny qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CON-004 — Debug dual gate

**Normative statement:** Main debug actions shall be disabled by default and shall require both explicit configuration enablement and `wayfarer.main.debug` permission.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-018  
**Rationale:** Prevents production exposure through permission or configuration alone.  
**Precondition / trigger:** A debug action is requested.  
**Required observable result:** The action is available only when both gates are true; otherwise no debug mutation occurs.  
**Verification intent:** SWE.4 gate verification and SWE.5 configuration/permission integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-004 — Presentation independence

**Normative statement:** The required Main GUI information and actions shall remain clear and operable, but exact language, slot layout, item display name, lore, and presentation configuration shall not be used as functional acceptance criteria for this scope.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; AMD-010  
**Rationale:** Separates product capability from deferred presentation refinement.  
**Precondition / trigger:** The GUI and managed item are presented to the player.  
**Required observable result:** Required status and actions are understandable and usable even though exact presentation is implementation-selected.  
**Verification intent:** SWE.6 client inspection and Owner usability review.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-005 — Repair ambiguous-outcome containment

**Normative statement:** An ambiguous repair debit, repair commit, or refund shall remain `UNKNOWN` for manual reconciliation and shall not be automatically completed or retried.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007  
**Rationale:** Prevents duplicate financial or repair effects when completion cannot be proven.  
**Precondition / trigger:** A repair stage returns an ambiguous result.  
**Required observable result:** No automatic second provider/repair effect occurs; the operation is inspectable and reconcilable.  
**Verification intent:** SWE.4 outcome-policy verification and SWE.5 provider-failure integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
