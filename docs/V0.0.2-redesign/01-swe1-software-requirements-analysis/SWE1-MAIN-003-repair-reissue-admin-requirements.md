# Main GUI, Repair, Reissue, Administration, and Permission Requirements

Document ID: `SWE1-MAIN-003`  
Revision: C  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision F  
Controlling review decisions: `DEC-REQ-004`, `DEC-REQ-005`, `DEC-REQ-008`  
Contained active normative items: CAP: 10, CON: 4, IFC: 1, QLT: 4; historical `QLT-002` superseded

## 1. Purpose

Define the Main owner management interface, owner-paid full repair, paid reissue, administrative operations, permission interfaces, and presentation constraints while specializing the approved Common protected-operation rules. `CAN-MAIN-014` and `CAN-MAIN-015` are jointly reviewed and integrated under `DEC-REQ-008`; `CAN-MAIN-016` and later clauses remain subject to their owning review.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- V0.0.2 Main financial capabilities use the Core-provided shared Waymark transaction contract approved by `CAN-CORE-003`; feature-specific eligibility, quote, repair/reissue benefit, and physical/logical state remain Main responsibilities.
- Superseded identifiers remain historical and are not reused or renumbered.

## 3. Reviewed management and repair requirements

### SWE1-MAIN-003-CAP-001 — Owner management GUI entry

**Normative statement:** The current logical owner of a current authorized Growth Tool or Broken Tool shall be able to open the Main management GUI by performing a main-hand air right-click that does not target a block or entity while holding that managed physical representation. The corresponding off-hand interaction shall not open the GUI. This required entry route does not prohibit another later approved management entry route.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; DEC-REQ-008 §5  
**Rationale:** Defines the externally visible owner management entry without treating physical possession as authority or pre-approving later permission-node allocation.  
**Precondition / trigger:** The invoking player performs the required gesture with a managed representation.  
**Required observable result:** The GUI opens only when the presented representation resolves to the invoking player's current logical ownership/current authority; non-owner possession, malformed identity, stale issuance, stale epoch, or equivalent failed authority does not grant owner management access.  
**Verification intent:** SWE.4 authority/entry policy verification, SWE.5 Paper interaction integration, and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CAP-004; SWE1-MAIN-001-CAP-008; SWE1-MAIN-002-CON-005  
**Assumptions:** None  
**Open issue / conflict:** Exact permission-node allocation remains for CAN-MAIN-018 owning review.  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-002 — Management state presentation

**Normative statement:** The management GUI shall present sufficient current state for the owner to understand the Growth Tool, including lifecycle status; Growth Pickaxe material tier; cumulative progress; conceptual evolution state/count; next configured evolution threshold or a defined no-next-threshold state; effective enchantments; active branch; current Minecraft-authoritative physical durability when applicable or Broken state when physical durability is not applicable; applicable repair availability/preview; and any material conceptual/effective difference caused by configured caps or branch projection.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; DEC-REQ-008 §5  
**Rationale:** Makes the interface explain the authoritative/derived Growth Tool state rather than merely exposing raw physical presentation.  
**Precondition / trigger:** The authorized management GUI is opened.  
**Required observable result:** Presented information is consistent with current authoritative logical/physical state. For a Broken representation, the material value represents the derived Growth Pickaxe material tier rather than the Broken presentation material.  
**Verification intent:** SWE.4 presentation-model verification and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-QLT-003; SWE1-MAIN-002-CAP-009; SWE1-MAIN-002-CON-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-003 — Management operation access

**Normative statement:** The management GUI shall provide access to the applicable Repair operation and Help/Status information and shall present the applicable repair availability/preview required by the repair requirements. Opening the GUI, viewing status, or viewing a repair preview shall not by itself authorize repair, debit Waymark, or produce another protected state-changing effect.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; CAN-MAIN-015; DEC-REQ-008 §§5–6  
**Rationale:** Preserves a clear management path without conflating presentation/preview with financial authorization.  
**Precondition / trigger:** The owner opens the management interface or selects a non-committing status/repair-preview action.  
**Required observable result:** Required management information/entry actions are available with no protected financial/domain effect until the separately governed repair confirmation is accepted.  
**Verification intent:** SWE.5 GUI-flow integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-003-CAP-001; SWE1-MAIN-003-CAP-005  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-004 — Presentation-independent management usability

**Normative statement:** Required management information shall remain identifiable and applicable required actions shall remain operable without depending on one exact language, inventory size, slot assignment, decorative item selection, display name, lore, or equivalent presentation encoding.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-014; AMD-010; DEC-REQ-008 §5  
**Rationale:** Separates required Product usability from a fixed inventory-screen presentation design.  
**Precondition / trigger:** The management GUI and managed item are presented to the player.  
**Required observable result:** Required state/actions remain understandable and usable even when exact wording/layout/presentation changes.  
**Verification intent:** SWE.6 client inspection and Owner usability review.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-003-CAP-002; SWE1-MAIN-003-CAP-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-001 — Protected repair/reissue UI replay safety

**Normative statement:** Double click, lag, inventory-event replay, disconnect, or reopening a stale financial interface shall not create a second accepted protected repair/reissue operation or duplicate debit, repair, reissue, delivery entitlement, or authority mutation. Replay of an already accepted operation shall resolve to that operation; a stale unaccepted confirmation shall be rejected when its current prerequisites no longer hold.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; CAN-MAIN-016; §4 CAN-COM-007; DEC-REQ-008 §§5–6  
**Rationale:** Applies the Common accepted-operation versus stale-request distinction to the Main financial interaction surface. CAN-MAIN-014 is no longer a source because non-financial management entry/presentation does not itself create a protected operation.  
**Precondition / trigger:** A repair/reissue financial confirmation is repeated, recovered, or submitted after its quote/session/context becomes stale.  
**Required observable result:** Accepted replay returns/advances one established logical operation; stale unaccepted confirmation produces no protected effect.  
**Verification intent:** SWE.4 UI-session/replay verification, SWE.5 transaction integration, SWE.6 representative client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** Reissue-specific application remains subject to CAN-MAIN-016 owning review.  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-004 — Configured full-repair pricing

**Normative statement:** V0.0.2 player repair shall offer only full repair. Repair price shall be determined from the current approved repair-pricing configuration and current authoritative Growth Tool state. The initial V0.0.2 supplied/default schedule shall be: base full-repair cost `ceil(100 × (1 + evolution_count × 0.08))`; active repair `ceil(base_full_repair_cost × max(0.25, missing_durability_ratio))`; broken repair `base_full_repair_cost + 100 + evolution_count × 5`. These values are initial/default balance values and may be adjusted through approved configuration.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; DEC-REQ-008 §6  
**Rationale:** Preserves the approved initial economy while allowing balance adjustment without changing Product code/requirements.  
**Precondition / trigger:** An eligible repair preview/quote is calculated.  
**Required observable result:** The quote uses one current approved pricing configuration and current authoritative state; active missing-durability ratio is derived from Minecraft-authoritative physical durability rather than a separate Main damage authority.  
**Verification intent:** SWE.4 formula/configuration/boundary verification and SWE.6 representative price qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CON-002; SWE1-MAIN-002-QLT-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CON-001 — No paid repair at full durability

**Normative statement:** A current authorized `ACTIVE` Growth Pickaxe at maximum durability shall not be eligible for player-paid repair, shall present a repair charge of `0 WM`, and shall not begin a debit operation for repair.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; DEC-REQ-008 §6  
**Rationale:** Prevents meaningless payment and avoids creating a zero-value protected financial operation solely for a no-op repair.  
**Precondition / trigger:** Repair preview/confirm is requested for a fully durable active Growth Pickaxe.  
**Required observable result:** Repair is unavailable/rejected before debit and the charge is shown as 0 WM.  
**Verification intent:** SWE.4 eligibility policy verification and SWE.6 GUI qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-005 — Confirmed repair transaction admission and execution

**Normative statement:** Before any protected financial effect, an eligible owner-paid repair shall present an explicit quote and require explicit confirmation of that quote. Before debit, Main shall revalidate current logical/physical authority, lifecycle, physical-item state, applicable evolution state, pricing configuration, and quote validity. If a change alters eligibility or the confirmed amount, the stale confirmation shall be rejected without silently charging a different amount and a new quote/confirmation shall be required. An accepted repair shall use the V0.0.2 Core-provided shared Waymark transaction contract and applicable Common protected-operation identity/replay/UNKNOWN rules; the repair benefit shall proceed only after debit success is proven.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007; §5 CAN-CORE-003; DEC-REQ-008 §6  
**Rationale:** Protects the owner from stale pricing/state and sequences the paid benefit behind proven debit without inventing stronger provider guarantees.  
**Precondition / trigger:** The owner confirms an eligible repair quote.  
**Required observable result:** Stale/changed quotes are rejected before debit; an accepted current quote establishes one protected repair operation whose benefit is not attempted before proven debit success.  
**Verification intent:** SWE.4 quote/revalidation/effect-order verification and SWE.5 Core transaction/Main integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-004; SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-013  
**Assumptions:** None  
**Open issue / conflict:** None; the V0.0.2 shared transaction owner is already allocated to Core by reviewed CAN-CORE-003.  
**State:** `DRAFT`

### SWE1-MAIN-003-CAP-010 — Successful full-repair result

**Normative statement:** Successful repair of a current authorized `ACTIVE` Growth Pickaxe shall preserve logical identity, owner, cumulative progress, delivery state, branch, current physical issuance identity, and authority epoch while restoring the current physical item to maximum durability. Successful repair of a current authorized `BROKEN` tool shall transition the same logical tool `BROKEN → ACTIVE`, preserve owner/progress/delivery/branch/current issuance/epoch, establish the current authorized active Growth Pickaxe representation consistent with the current approved evolution configuration, and restore it to maximum durability. Repair shall not rotate authority or create a new delivery entitlement.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; reviewed CAN-MAIN-013; DEC-REQ-008 §§4,6  
**Rationale:** Defines the actual paid Repair benefit and distinguishes repair from reissue/authority rotation.  
**Precondition / trigger:** A repair operation has proven debit success and its Main-owned repair benefit is eligible to commit.  
**Required observable result:** The same current authority is fully repaired; BROKEN repair returns the same issuance/epoch to ACTIVE rather than replacing it.  
**Verification intent:** SWE.4 state-transition verification, SWE.5 logical/physical repair integration, and SWE.6 active/broken repair qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-003-CAP-005; SWE1-MAIN-002-CAP-014  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-003-QLT-005 — Repair-benefit completion and ambiguous partial-state containment

**Normative statement:** A repair operation shall not be reported as successful until the required logical and physical repaired state for the same current authority is established. If the repair benefit may be partially established or cannot be proven success or clear no-benefit failure, the exact repair effect shall remain `UNKNOWN` for authorized reconciliation and shall not be blindly retried, treated as success, treated as clear failure, or automatically compensated. Compensation remains governed by the Common requirement for proven debit success plus proven absence/clear failure of the downstream benefit.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-015; §4 CAN-COM-007; DEC-REQ-008 §6  
**Rationale:** Handles Main's logical/Minecraft physical repair boundary without claiming cross-authority atomicity or refunding an ambiguously successful repair.  
**Precondition / trigger:** Repair-benefit completion/recovery cannot establish the complete required repaired state.  
**Required observable result:** Ambiguity remains correlated to the exact operation/effect and enters supported reconciliation without duplicate repair/debit/refund.  
**Verification intent:** SWE.4 repair benefit/partial-state outcome matrix and SWE.5 failure/restart/reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-006; SWE1-COMMON-001-QLT-013; SWE1-MAIN-003-CAP-010  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

## 4. Historical superseded identifier

### SWE1-MAIN-003-QLT-002 — Repair compensation on proven clear failure

**Disposition:** `SUPERSEDED_BY_DEDUPLICATION`  
**Former source:** CAN-MAIN-015; CAN-COM-007  
**Replacement coverage:** The generic proven-debit/proven-no-benefit compensation rule is already controlled by the Common protected-operation requirements. Repair-specific ambiguity/completion semantics remain in `SWE1-MAIN-003-QLT-005`, and Repair admission/result remain in `SWE1-MAIN-003-CAP-005` / `CAP-010`.  
**Identifier reuse:** Prohibited.

## 5. Later Main requirements still awaiting owning-clause review

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
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-007; CAN-MAIN-016 remains unreviewed and current physical-absence semantics require correction under DEC-REQ-006 §7.2.  
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
**Open issue / conflict:** `DEC-REQ-006` §7.2 already supersedes this exact formula pending CAN-MAIN-016 owning review; only the strict-more-expensive-than-applicable-repair invariant is carried forward.  
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
**Open issue / conflict:** CAN-MAIN-016 remains unreviewed; successful reissue is known to be fully repaired and old instances stale under DEC-REQ-006 §7.2.  
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
**Open issue / conflict:** Current-item physical-absence wording conflicts with reviewed possession/storage neutrality and must be corrected during CAN-MAIN-016 owning review.  
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
**Open issue / conflict:** CAN-MAIN-016 remains unreviewed.  
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
**Open issue / conflict:** CAN-MAIN-018 remains unreviewed.  
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
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-008; CAN-MAIN-018 remains unreviewed.  
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
**Open issue / conflict:** CAN-MAIN-018 remains unreviewed.  
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
**Open issue / conflict:** CAN-MAIN-018 remains unreviewed.  
**State:** `DRAFT`
