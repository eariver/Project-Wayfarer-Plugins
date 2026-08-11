# Main Lifecycle, Tool Authority, and Delivery Requirements

Document ID: `SWE1-MAIN-001`  
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
Controlling Common review decisions: `DEC-REQ-002`, `DEC-REQ-004`  
Contained normative items: CAP: 9, CON: 9, QLT: 2

## 1. Purpose

Define Main lifecycle, logical and physical Growth Tool authority, initial delivery, owner binding, transfer controls, and death behavior while carrying unresolved Main-specific topology wording to the later `CAN-MAIN-001` Owner review.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue or explicit Common/target conflict remains draft and cannot support G1 PASS until resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-MAIN-001-CON-001 — Main-only activation

**Normative statement:** Under the current unreviewed `CAN-MAIN-001` wording, Wayfarer_Main activates its gameplay only on the Main backend and requires a compatible Wayfarer_Core service.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-001  
**Rationale:** Preserves the current target-clause text until the Main section is jointly reviewed; it is not a reaffirmation of fixed-backend gating.  
**Precondition / trigger:** At plugin enablement.  
**Required observable result:** Pending target-clause correction.  
**Verification intent:** Deferred until `CAN-MAIN-001` is reconciled with Common topology/capability ownership requirements.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-001  
**Assumptions:** None  
**Open issue / conflict:** Fixed Main-backend and specific Core dependency wording conflicts with Owner-approved `CAN-COM-001`/`CAN-COM-002` direction and must be corrected during `CAN-MAIN-001` review.  
**State:** `DRAFT`

### SWE1-MAIN-001-QLT-001 — Main Growth Tool prerequisite validation

**Normative statement:** The Main Growth Tool capability shall become available only after its approved configuration, Main-owned schema compatibility, required approved shared-capability contracts, and applicable configured gameplay-context conditions have been validated.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-001; §4 CAN-COM-006; DEC-REQ-004 §2  
**Rationale:** Specializes Common capability-prerequisite availability without making a backend name or specific shared-provider implementation a generic lifecycle prerequisite.  
**Precondition / trigger:** At capability startup and whenever an assigned mandatory prerequisite is refreshed or invalidated.  
**Required observable result:** The Main Growth Tool capability remains unavailable with an operationally observable reason whenever an assigned prerequisite is invalid.  
**Verification intent:** SWE.4 prerequisite-policy verification and SWE.5 Main lifecycle integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-003; SWE1-COMMON-001-QLT-004  
**Assumptions:** None  
**Open issue / conflict:** Exact Main placement/context allocation remains subject to `CAN-MAIN-001` review.  
**State:** `DRAFT`

### SWE1-MAIN-001-QLT-002 — Dirty-session lifecycle disposition

**Normative statement:** When the Main Growth Tool capability is deactivated, each accepted dirty-session persistence obligation shall be checkpointed/completed, safely rejected where no protected effect occurred, or retained as an explicitly recoverable pending obligation within the Common bounded accepted-operation lifecycle disposition.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-017; §4 CAN-COM-006; DEC-REQ-004 §2  
**Rationale:** Specializes Main session durability without mandating asynchronous flush, runtime-generation counters, or duplicating Common admission/stale-completion rules.  
**Precondition / trigger:** Capability deactivation, plugin disablement, runtime replacement, or another Main lifecycle close while dirty work is accepted.  
**Required observable result:** Dirty accepted work has a finite inspectable/recoverable disposition; no incomplete durable obligation is silently discarded.  
**Verification intent:** SWE.4 session-disposition verification and SWE.5 disable/restart recovery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-010; SWE1-COMMON-001-QLT-011  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-001 — Single logical pickaxe

**Normative statement:** The software shall maintain at most one logical Growth Tool of type `PICKAXE` for each owner UUID.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002  
**Rationale:** Defines the durable player entitlement and duplicate boundary.  
**Precondition / trigger:** When a record is created, granted, delivered, repaired, or reissued.  
**Required observable result:** Concurrent creation or grant resolves to one logical pickaxe authority for the owner.  
**Verification intent:** SWE.4 uniqueness/race verification and SWE.5 database integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-002 — Logical tool state

**Normative statement:** The logical Growth Tool shall represent `ACTIVE`, `BROKEN`, and `REVOKED` tool states; `DELIVERED` and `PENDING` delivery states; and `FORTUNE` and `SILK_TOUCH` active branches.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002  
**Rationale:** Provides explicit durable state for all approved gameplay outcomes.  
**Precondition / trigger:** Whenever tool authority or delivery state changes.  
**Required observable result:** The durable record exposes exactly one valid state from each applicable state set.  
**Verification intent:** SWE.4 state-model verification and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-002 — Logical record completeness

**Normative statement:** The durable logical tool record shall contain stable tool identity, owner, tool type, epoch, cumulative fixed-point progress, active branch, tool state, delivery state, stored damage, schema version, optimistic-lock version, creation/update timestamps, and checkpoint timestamp.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002  
**Rationale:** Supports restart-safe authority, evolution, repair, reissue, and concurrency.  
**Precondition / trigger:** When a logical tool record is created or loaded.  
**Required observable result:** All required fields are valid and sufficient to reconstruct the approved physical representation and state.  
**Verification intent:** Schema inspection and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-003 — Physical item identity

**Normative statement:** Every Growth Tool and Broken Tool physical item shall carry typed PDC identifying item type, physical instance, logical tool, owner, tool type, epoch, schema version, and display revision.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003  
**Rationale:** Allows physical items to be checked against durable authority.  
**Precondition / trigger:** When a physical item is created or reconstructed.  
**Required observable result:** The PDC contains valid values that map to one logical authority record.  
**Verification intent:** SWE.4 item-identity verification and SWE.5 Paper item integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-003 — No display-only identity

**Normative statement:** The software shall not authorize a Growth Tool by material, display name, lore, enchantments, or visual similarity alone.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003  
**Rationale:** Prevents forgery through ordinary item metadata.  
**Precondition / trigger:** Whenever a player attempts a managed-tool operation.  
**Required observable result:** Authorization requires the typed physical and logical identity contract.  
**Verification intent:** SWE.4 authorization verification and SWE.6 forged-item qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-004 — Current authority validation

**Normative statement:** Before managed-tool use, progress, GUI, repair, delivery, or authority-changing operations, the software shall validate owner, logical tool ID, physical instance as applicable, current epoch, schema, tool state, and delivery state against current authority.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003; CAN-MAIN-005  
**Rationale:** Ensures every entry route applies the same authority.  
**Precondition / trigger:** At each managed-tool gameplay or administrative entry point.  
**Required observable result:** Only the current authorized state proceeds; invalid state produces no protected mutation or financial effect.  
**Verification intent:** SWE.4 common-guard verification, SWE.5 event/command integration, SWE.6 Main qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-004 — Invalid identity fail-closed

**Normative statement:** Unknown item type, unknown schema, malformed UUID/identifier, wrong owner, wrong logical tool, stale epoch, and revoked authority shall be denied.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003  
**Rationale:** Prevents stale or malformed items from becoming authoritative.  
**Precondition / trigger:** When an invalid managed-looking item reaches an entry point.  
**Required observable result:** No use, progress, GUI mutation, repair, reissue bypass, transfer, or delivery acknowledgment occurs.  
**Verification intent:** SWE.4 invalid-identity matrix and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-005 — Race-safe initial entitlement

**Normative statement:** On an applicable Main join/context entry, the software shall load the player's logical tool without blocking a critical runtime execution context and race-safely create it when absent, using the single-tool uniqueness rule.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-005  
**Rationale:** Supports existing and new players without duplicate entitlements or blocking runtime I/O.  
**Precondition / trigger:** An eligible player enters the Main capability context with no logical pickaxe record.  
**Required observable result:** Exactly one logical record is established despite concurrent join/grant activity, without synchronous durable I/O on a prohibited execution context.  
**Verification intent:** SWE.4 concurrency/context verification and SWE.5 MariaDB integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-002  
**Assumptions:** None  
**Open issue / conflict:** Exact Main context remains subject to `CAN-MAIN-001` review.  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-006 — Validated initial delivery

**Normative statement:** After deferred entitlement resolution, the software shall revalidate the player's current eligibility and perform physical-item mutation only from an execution context authorized by the adopted platform for that player/inventory state.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-005  
**Rationale:** Prevents late delivery after logout/context change and avoids assuming one global main-thread model.  
**Precondition / trigger:** The deferred load/create operation completes.  
**Required observable result:** Delivery occurs only if current lifecycle/player/context/authority preconditions remain valid; otherwise the obligation remains safely recoverable.  
**Verification intent:** SWE.4 revalidation/context verification and SWE.5 join/logout/context-change integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-001; SWE1-COMMON-001-QLT-009  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-007 — Pending delivery without world drop

**Normative statement:** When the authorized physical item cannot be placed safely in the player's inventory, the software shall not drop it and shall retain a typed pending-delivery obligation.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004  
**Rationale:** Prevents item loss and duplicate paid recovery.  
**Precondition / trigger:** Initial or replacement delivery encounters insufficient inventory capacity or a safe-delivery precondition failure.  
**Required observable result:** No world drop or duplicate authority occurs; the exact item obligation remains pending.  
**Verification intent:** SWE.4 delivery-policy verification, SWE.5 inventory-full integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-005 — No automatic reissue of delivered authority

**Normative statement:** A logical tool recorded as delivered shall not be automatically reissued merely because a join occurs or the physical item is not immediately observed.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004  
**Rationale:** Avoids silent duplication and authority rotation.  
**Precondition / trigger:** An eligible player enters the Main capability context with delivery state `DELIVERED`.  
**Required observable result:** No new physical instance or epoch is created without an authorized recovery/reissue flow.  
**Verification intent:** SWE.4 delivery-state verification and SWE.5 rejoin integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-008 — Owner-authorized use

**Normative statement:** The current owner shall be able to use the current authorized active Growth Pickaxe in approved contexts.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005  
**Rationale:** States the positive side of owner binding.  
**Precondition / trigger:** The owner holds the current active authorized item and satisfies target operation preconditions.  
**Required observable result:** The requested approved operation is not denied by owner/identity binding.  
**Verification intent:** SWE.5 gameplay integration and SWE.6 Main qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-006 — Non-owner and stale-item denial

**Normative statement:** A non-owner, a stale-epoch holder, or a holder of a physical item not matching current authority shall not use the tool or gain progress.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005  
**Rationale:** Prevents transfer of entitlement and stale-copy exploitation.  
**Precondition / trigger:** A managed item is held by an unauthorized player or is stale.  
**Required observable result:** Use and progress are denied without changing logical authority.  
**Verification intent:** SWE.4 authorization verification and SWE.6 representative non-owner/stale-item qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-007 — Managed-item transfer restrictions

**Normative statement:** The software shall deny manual drop, other-player pickup, ordinary container storage, anvil, grindstone, smithing, crafting repair, same-tool combination, Mending, item-frame placement, armor-stand placement, and any supported equivalent route for a current Growth Tool or Broken Tool.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005  
**Rationale:** Preserves owner binding and controlled durability/repair semantics.  
**Precondition / trigger:** A listed transfer or transformation route is attempted.  
**Required observable result:** The route produces no unauthorized transfer, repair, merge, or replacement.  
**Verification intent:** SWE.4 common-guard verification, SWE.5 representative event integration, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-002  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-008 — Death-drop suppression

**Normative statement:** The current Growth Tool or Broken Tool shall be removed from player death drops.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-006; AMD-006  
**Rationale:** Prevents uncontrolled world copies and supports explicit recovery semantics.  
**Precondition / trigger:** The owner dies while the managed item would otherwise be included in drops.  
**Required observable result:** No managed item entity is created from the death drop.  
**Verification intent:** SWE.5 death-event integration and SWE.6 Main qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-009 — No automatic respawn restoration

**Normative statement:** The software shall not retain the dead player's managed raw ItemStack in process memory and shall not automatically restore the Growth Tool or Broken Tool on respawn.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-006; AMD-006  
**Rationale:** Makes paid reissue the explicit loss-recovery path and survives process restart semantics cleanly.  
**Precondition / trigger:** The owner respawns after death-drop suppression.  
**Required observable result:** No physical managed item is automatically added; the logical tool remains unchanged and available for the authorized reissue path.  
**Verification intent:** SWE.4 death-policy verification, SWE.5 death/respawn integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-009 — Pending-delivery notification and retry

**Normative statement:** For a Main pending-delivery outcome, the software shall notify the reachable player, retain a sanitized operational reason/audit correlation, and allow later free retry through an approved entry or administrative path.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-009  
**Rationale:** Makes the recovery path explicit without charging for an existing obligation and keeps it auditable.  
**Precondition / trigger:** A Main item delivery becomes pending.  
**Required observable result:** The reachable player receives an actionable disposition, the pending entitlement remains correlated to retrievable audit evidence, and retry does not rotate authority or debit Waymark.  
**Verification intent:** SWE.4 disposition/audit verification, SWE.5 retry/restart integration, SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-008; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
