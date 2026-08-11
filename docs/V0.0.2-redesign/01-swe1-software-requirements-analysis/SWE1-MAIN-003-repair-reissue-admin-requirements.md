# Main GUI, Repair, Reissue, Administration, and Permission Requirements

Document ID: `SWE1-MAIN-003`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-11 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision B  
Controlling Common review decisions: `DEC-REQ-004`  
Contained normative items: CAP: 9, CON: 4, IFC: 1, QLT: 5

## 1. Purpose

Define Main user-management entry, repair and paid reissue transactions, administrative operations, permission interfaces, and presentation constraints while specializing the approved Common protected-operation rules.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- Where this document refers to a shared Waymark transaction capability, the concrete shared owner remains subject to the applicable Core/shared-capability Owner review; feature requirements do not depend on provider internals.

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

**Normative statement:** Double click, lag, inventory-event replay, disconnect, or reopening a stale GUI shall not create a second protected repair/reissue operation or duplicate debit, repair, reissue, delivery entitlement, or authority mutation. Replay of an already accepted operation shall resolve to that operation; a stale unaccepted confirmation shall be rejected when its current prerequisites no longer hold.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; CAN-MAIN-015; CAN-MAIN-016; §4 CAN-COM-007  
**Rationale:** Applies the Common accepted-operation versus stale-request distinction at a high-replay user interface.  
**Precondition / trigger:** A financial confirmation is repeated, recovered, or submitted after its quote/session/context becomes stale.  
**Required observable result:** Accepted replay returns/advances one established logical operation; stale unaccepted confirmation produces no protected effect.  
**Verification intent:** SWE.4 GUI-session/replay verification, SWE.5 GUI transaction integration, SWE.6 representative client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-012  
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

**Normative statement:** A confirmed repair shall execute through the approved shared Waymark transaction capability using the established logical operation/effect identities, current player/tool authority validation, quote revalidation, and same-player/tool serialization.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007  
**Rationale:** Ensures payment and repair use current authority and the approved shared economy contract without depending on provider internals.  
**Precondition / trigger:** The owner confirms a non-stale eligible repair quote.  
**Required observable result:** The repair operation coordinates one protected debit effect and one corresponding repair-benefit effect under one logical operation identity.  
**Verification intent:** SWE.4 coordinator/identity verification and SWE.5 shared-transaction/Main integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-004; SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-013  
**Assumptions:** None  
**Open issue / conflict:** Current canonical `CAN-MAIN-015` says Core transaction boundary; exact shared owner remains subject to CAN-CORE-003/CAN-MAIN-015 Owner review.  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-002 — Repair compensation on proven clear failure

**Normative statement:** Automatic repair refund/compensation shall begin only after the original debit success is proven and the downstream repair benefit is proven not committed due to a clear failure. The compensation shall use its own stable effect identity and shall not be duplicated.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Protects player funds without refunding an ambiguous repair that may actually have succeeded.  
**Precondition / trigger:** A repair operation has proven debit success and a clear downstream repair failure with no committed repair benefit.  
**Required observable result:** At most one compensation effect is initiated for the exact repair operation; an ambiguous downstream result does not enter automatic compensation.  
**Verification intent:** SWE.4 repair failure/compensation matrix and SWE.5 provider/domain failure integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-013  
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

**Normative statement:** A successful paid reissue shall preserve the logical tool ID, cumulative progress, and active branch; create a new physical instance ID; increment the epoch; invalidate all older physical instances; set the logical state to `ACTIVE`; set damage to zero; and establish exactly one immediate or typed-pending delivery entitlement for the replacement item.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; AMD-006; AMD-007; §4 CAN-COM-007  
**Rationale:** Defines complete authority rotation while connecting physical delivery to one protected entitlement.  
**Precondition / trigger:** A current eligible quote is explicitly confirmed and the financial/authority operation reaches proven success for the required preceding effects.  
**Required observable result:** Exactly one new current physical authority exists or is pending under one entitlement, old epochs are denied, and preserved gameplay state is unchanged.  
**Verification intent:** SWE.4 transition/entitlement verification, SWE.5 transaction/delivery integration, SWE.6 reissue qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-012; SWE1-COMMON-001-QLT-013  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CON-002 — Paid reissue pre-debit rejection

**Normative statement:** Paid reissue shall be rejected before debit when a current authorized physical item exists or a typed pending delivery already exists; the player shall be directed to the free delivery-retry path when pending delivery is present.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; AMD-007  
**Rationale:** Prevents paying for a duplicate or bypassing an existing free obligation.  
**Precondition / trigger:** A reissue is requested while current physical authority or pending delivery exists.  
**Required observable result:** No provider debit, epoch rotation, new instance, or duplicate entitlement/delivery is created; an actionable denial is returned.  
**Verification intent:** SWE.4 precondition verification, SWE.5 command/transaction integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-003 — Reissue protected-effect replay and UNKNOWN safety

**Normative statement:** One accepted paid-reissue logical operation shall establish at most one successful debit effect, one authority rotation/current replacement authority, and one replacement delivery entitlement. Replay, duplicate confirmation, timeout/restart recovery, or an `UNKNOWN` stage shall not create an additional debit, epoch increment, physical authority, entitlement, or automatic re-execution of the uncertain provider effect. Delivery retry may continue only the same established entitlement when final delivery has not been proven.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-016; §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Protects economy, logical authority, and delivery as separate effects under one reissue operation.  
**Precondition / trigger:** The same reissue operation is submitted, replayed, recovered, or delivery-retried.  
**Required observable result:** The established operation/effect identities yield one allowed protected result per effect kind; unresolved effects remain inspectable/reconcilable.  
**Verification intent:** SWE.4 reissue idempotency/effect-matrix verification and SWE.5 failure/restart/delivery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-006; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-009 — Main administrative capabilities

**Normative statement:** Wayfarer_Main shall provide authorized capabilities for inspect, grant, administrative reissue, repair, branch change, revoke, reconcile, and pending-delivery retry.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-018  
**Rationale:** Provides required operational recovery and control.  
**Precondition / trigger:** An authorized administrator invokes a supported operation with valid arguments.  
**Required observable result:** The operation performs only its documented state change, produces retrievable audit correlation where designated, and returns an actionable disposition.  
**Verification intent:** SWE.5 command/audit integration and SWE.6 representative administration qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-008  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-IFC-001 — Main permission nodes

**Normative statement:** Main gameplay and administrative entry points shall use the nodes `wayfarer.main.use`, `wayfarer.main.admin.read`, `wayfarer.main.admin.delivery`, `wayfarer.main.admin.modify`, `wayfarer.main.admin.reconcile`, and `wayfarer.main.debug`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-018; AMD-011  
**Rationale:** Fixes the approved medium-grained permission interface.  
**Precondition / trigger:** A player or administrator reaches a gameplay, command, or debug entry point.  
**Required observable result:** Access is evaluated against the directly applicable node; absence of the node denies the protected action without protected mutation.  
**Verification intent:** SWE.4 permission-policy verification, SWE.5 permission integration, SWE.6 representative qualification.  
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

### SWE1-MAIN-003-QLT-005 — Repair effect-level ambiguous-outcome containment

**Normative statement:** An ambiguous repair debit effect, repair-commit effect, or compensation effect shall remain `UNKNOWN` for authorized reconciliation and shall not be automatically retried, treated as success, treated as clear failure, or used to authorize the next success/failure-dependent protected effect.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007; DEC-REQ-004 §3  
**Rationale:** Prevents duplicate debit/repair/refund and prevents an ambiguous successful repair from being refunded automatically.  
**Precondition / trigger:** Any protected repair effect returns or is recovered with an outcome that cannot be proven success or clear no-effect failure.  
**Required observable result:** The exact operation/effect identity remains `UNKNOWN`, inspectable, and reconcilable; no automatic second effect or compensation is initiated from that ambiguity.  
**Verification intent:** SWE.4 effect-level outcome-policy verification and SWE.5 provider/domain failure/reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-006; SWE1-COMMON-001-QLT-013  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
