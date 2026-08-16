# Worlds Beyond Loadout, Permanent Item, Hook, and Navigation Requirements

Document ID: `SWE1-WB-001`  
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
Contained normative items: CAP: 10, CON: 8, IFC: 1, QLT: 5

## 1. Purpose

Define first safe-entry delivery, permanent traversal-item authority and death recovery, LeafGrapple integration, Elytra behavior, and Navigation.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-WB-001-CAP-001 — First safe-entry loadout

**Normative statement:** On a player's first safe entry into exact `frontier_iris`, the software shall establish the approved loadout entitlement and deliver one Elytra, one authentic LeafGrapple hook, one Navigation item, and two Launchpads.

**Source:** SWE1-SRC-002 §8 CAN-WB-001  
**Rationale:** Provides the minimum Worlds Beyond traversal foundation.  
**Precondition / trigger:** A player enters exact `frontier_iris` without a completed initial-loadout entitlement.  
**Required observable result:** The approved permanent and initial consumable quantities are delivered or represented as typed pending obligations without duplication.  
**Verification intent:** SWE.5 safe-entry/delivery integration and SWE.6 WB qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-001 — No duplicate initial allocation

**Normative statement:** Repeated safe entry shall not duplicate a completed permanent-item delivery or the initial two-Launchpad allocation.

**Source:** SWE1-SRC-002 §8 CAN-WB-001  
**Rationale:** Prevents entitlement duplication through re-entry.  
**Precondition / trigger:** A player with completed or pending loadout state re-enters exact `frontier_iris`.  
**Required observable result:** Only outstanding typed obligations are retried; completed quantities are not regranted.  
**Verification intent:** SWE.4 delivery-state verification, SWE.5 re-entry integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-002 — Asynchronous safe-entry delivery

**Normative statement:** Loadout and pending-delivery record resolution shall occur asynchronously, followed by main-thread revalidation that the player is online and still in exact `frontier_iris` before item mutation.

**Source:** SWE1-SRC-002 §8 CAN-WB-002  
**Rationale:** Prevents late or cross-theme delivery while respecting threading.  
**Precondition / trigger:** A safe-entry delivery operation completes its persistence phase.  
**Required observable result:** Item mutation occurs only for a still-eligible current player session.  
**Verification intent:** SWE.5 join/world-change/logout integration and thread inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-003 — Safe-entry delivery failure handling

**Normative statement:** Inventory-full or capability-unavailable delivery shall not drop the item and shall preserve only the undelivered typed obligation as pending.

**Source:** SWE1-SRC-002 §8 CAN-WB-002  
**Rationale:** Prevents item loss and preserves retryable obligations.  
**Precondition / trigger:** A delivery cannot complete because inventory capacity or required item capability is unavailable.  
**Required observable result:** No world drop or false completion occurs; the exact undelivered obligation remains pending.  
**Verification intent:** SWE.4 delivery-policy verification and SWE.5 failure integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-004 — Actionable delivery notification

**Normative statement:** When reachable, the player shall receive an actionable notification for inventory-full, capability-unavailable, conflict, or unknown safe-entry delivery outcomes; offline or theme-left outcomes shall preserve pending state without attempted player notification.

**Source:** SWE1-SRC-002 §8 CAN-WB-002; delta detail clarification  
**Rationale:** Makes recovery action clear without manufacturing delivery success.  
**Precondition / trigger:** A safe-entry delivery reaches one of the listed non-success dispositions.  
**Required observable result:** The player or administrator is directed to the appropriate retry/review action, and pending state remains accurate.  
**Verification intent:** SWE.4 disposition-message verification and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-QLT-001 — No automatic retry for conflict or unknown

**Normative statement:** Conflict and unknown safe-entry delivery outcomes shall not be automatically retried within the same operation or treated as successful.

**Source:** SWE1-SRC-002 §8 CAN-WB-002  
**Rationale:** Avoids duplicate item creation and hidden authority conflict.  
**Precondition / trigger:** A delivery coordinator reports conflict or unknown.  
**Required observable result:** The operation remains pending or review-required with no duplicate physical item.  
**Verification intent:** SWE.4 outcome-policy verification and SWE.5 failure integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-QLT-002 — Non-duplicative delivery audit

**Normative statement:** Safe-entry delivery reporting shall use sanitized audit and console output only where operationally necessary and shall not create duplicate audit records for one disposition.

**Source:** SWE1-SRC-002 §8 CAN-WB-002  
**Rationale:** Keeps evidence useful and non-noisy.  
**Precondition / trigger:** A delivery reaches a reportable disposition.  
**Required observable result:** One traceable sanitized disposition is available without duplicate semantic records.  
**Verification intent:** SWE.4 audit-format verification and SWE.5 integration inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-005 — Permanent item identity

**Normative statement:** Elytra, Grappling Hook, and Navigation item shall be typed, owner-bound, theme-bound, and validated through MariaDB authority plus PDC identity.

**Source:** SWE1-SRC-002 §8 CAN-WB-003  
**Rationale:** Defines permanent traversal entitlement and physical authority.  
**Precondition / trigger:** A permanent item is created, reconstructed, moved, or used.  
**Required observable result:** The item maps to one current owner/theme authority and is accepted only in that context.  
**Verification intent:** SWE.4 identity verification, SWE.5 Paper integration, SWE.6 WB qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-002 — Permanent item transfer and misuse denial

**Normative statement:** A permanent Worlds Beyond item shall reject non-owner use or equipment, other-player pickup, manual drop, ordinary container storage, and use outside exact `frontier_iris`.

**Source:** SWE1-SRC-002 §8 CAN-WB-003  
**Rationale:** Preserves owner and theme binding.  
**Precondition / trigger:** A listed unauthorized route is attempted.  
**Required observable result:** No unauthorized use, transfer, equip, or authority mutation occurs.  
**Verification intent:** SWE.4 common-guard verification, SWE.5 representative event integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-006 — Permanent item epoch rotation

**Normative statement:** When a permanent item's authority is administratively reissued, the epoch shall rotate and older physical instances shall become invalid.

**Source:** SWE1-SRC-002 §8 CAN-WB-003  
**Rationale:** Prevents stale-copy use after reissue.  
**Precondition / trigger:** An authorized reissue completes.  
**Required observable result:** Exactly one current epoch is authoritative and older epochs fail closed.  
**Verification intent:** SWE.4 state-transition verification and SWE.6 representative stale-item qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-003 — Permanent item death-drop suppression

**Normative statement:** Elytra, Grappling Hook, and Navigation item shall be removed from player death drops.

**Source:** SWE1-SRC-002 §8 CAN-WB-004; AMD-008  
**Rationale:** Prevents uncontrolled world copies before free recovery.  
**Precondition / trigger:** The owner dies while a permanent item would otherwise drop.  
**Required observable result:** No corresponding permanent item entity is created.  
**Verification intent:** SWE.5 death-event integration and SWE.6 WB qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-007 — Durable death recovery obligation

**Normative statement:** Death of the owner while holding a permanent item shall create a typed durable pending-delivery obligation rather than retaining a raw ItemStack in process memory.

**Source:** SWE1-SRC-002 §8 CAN-WB-004; AMD-008  
**Rationale:** Makes recovery survive restart and preserve data-authority boundaries.  
**Precondition / trigger:** A permanent item is removed from death drops.  
**Required observable result:** A durable typed obligation is committed or the failure is safely visible; no raw normal inventory snapshot is stored.  
**Verification intent:** SWE.4 transition verification, SWE.5 database/death integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-008 — Free safe-entry redelivery after death

**Normative statement:** On a later exact Worlds Beyond safe entry, a death-pending permanent item shall be reconstructed and delivered free of charge while preserving the same logical identity, physical instance identity, and epoch.

**Source:** SWE1-SRC-002 §8 CAN-WB-004; AMD-008  
**Rationale:** Defines recovery without authority rotation or player cost.  
**Precondition / trigger:** A valid death-pending obligation exists and the owner safely re-enters exact `frontier_iris`.  
**Required observable result:** One reconstructed current item is delivered or remains pending; no Waymark debit or epoch change occurs.  
**Verification intent:** SWE.5 restart/safe-entry integration and SWE.6 WB qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-QLT-003 — Restart-safe death recovery

**Normative statement:** A restart between death and redelivery shall not lose the pending permanent-item obligation or create a duplicate physical item.

**Source:** SWE1-SRC-002 §8 CAN-WB-004; AMD-008  
**Rationale:** Closes the process-memory loss window in the prior interpretation.  
**Precondition / trigger:** The server restarts after death-drop suppression and before successful redelivery.  
**Required observable result:** After restart exactly one recoverable pending obligation remains.  
**Verification intent:** SWE.5 restart persistence integration and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-004 — No free consumable recovery

**Normative statement:** Launchpads, rockets, and other consumables shall not use the permanent-item free death-recovery rule.

**Source:** SWE1-SRC-002 §8 CAN-WB-004  
**Rationale:** Preserves consumable economy and initial-allocation semantics.  
**Precondition / trigger:** The owner dies with a consumable item.  
**Required observable result:** No permanent-item recovery obligation is created for that consumable.  
**Verification intent:** SWE.4 item-type policy verification and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-009 — Elytra properties

**Normative statement:** The issued Elytra shall be unbreakable, owner-bound, usable only in exact `frontier_iris`, and usable for natural transition from approved hook or launchpad movement into gliding.

**Source:** SWE1-SRC-002 §8 CAN-WB-005  
**Rationale:** Provides the approved permanent traversal baseline.  
**Precondition / trigger:** The owner equips or uses the current Elytra in an allowed or denied context.  
**Required observable result:** Allowed theme use functions without durability loss; non-owner or out-of-theme use is denied.  
**Verification intent:** SWE.5 item/equipment integration and SWE.6 motion/client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-IFC-001 — LeafGrapple 1.0.2 capability

**Normative statement:** The software shall integrate LeafGrapple version `1.0.2` to create the authentic hook and shall verify the version and required capability before issuing or enabling the hook.

**Source:** SWE1-SRC-002 §8 CAN-WB-006  
**Rationale:** Uses the adopted external implementation instead of duplicating hook mechanics.  
**Precondition / trigger:** Wayfarer_Frontier enables or attempts hook delivery.  
**Required observable result:** A compatible authentic hook capability is confirmed; otherwise hook delivery/use fails closed.  
**Verification intent:** SWE.5 dependency/capability integration and inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-003  
**State:** `DRAFT`

### SWE1-WB-001-CON-005 — No hook physics reimplementation

**Normative statement:** Wayfarer-owned code shall not reimplement LeafGrapple projectile behavior, pull physics, or cooldown calculation.

**Source:** SWE1-SRC-002 §8 CAN-WB-006  
**Rationale:** Avoids duplicated external-library functionality and divergent behavior.  
**Precondition / trigger:** A hook capability is designed or implemented.  
**Required observable result:** The adopted plugin remains responsible for those mechanics; Wayfarer adds only approved identity/theme/safety integration.  
**Verification intent:** SWE.2/SWE.3 design inspection and source inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-006 — Safe hook configuration

**Normative statement:** The issued hook shall use a configuration with durability disabled and entity/player hooking disabled.

**Source:** SWE1-SRC-002 §8 CAN-WB-006  
**Rationale:** Matches the approved traversal-only safety model.  
**Precondition / trigger:** Hook capability is enabled and an item is issued.  
**Required observable result:** Hook durability does not decrease and attempts to hook entities/players do not succeed.  
**Verification intent:** SWE.5 external-plugin configuration integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-003  
**State:** `DRAFT`

### SWE1-WB-001-QLT-004 — LeafGrapple fail-closed behavior

**Normative statement:** Unsupported, incompatible, or unsafe LeafGrapple integration shall prevent hook issuance/use rather than falling back to a Wayfarer-owned substitute.

**Source:** SWE1-SRC-002 §8 CAN-WB-006  
**Rationale:** Prevents unsafe or behaviorally divergent fallback.  
**Precondition / trigger:** Version, API, or safe configuration validation fails.  
**Required observable result:** No hook is issued or enabled and an actionable operational reason is available.  
**Verification intent:** SWE.5 capability-failure integration and SWE.6 operational qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CAP-010 — Navigation entry

**Normative statement:** The current authorized Navigation item shall open a Worlds Beyond navigation interface with at least Shop, Loadout, and Help.

**Source:** SWE1-SRC-002 §8 CAN-WB-007  
**Rationale:** Provides the approved theme interaction entry point.  
**Precondition / trigger:** The owner uses the current Navigation item inside exact `frontier_iris` with applicable permission.  
**Required observable result:** The interface opens once and exposes the required entries.  
**Verification intent:** SWE.5 Paper GUI integration and SWE.6 client qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-007 — Waystone unavailable behavior

**Normative statement:** While Waystone is outside scope, Discovery, Teleport, and Waystone Placement Tool actions shall be absent, disabled, or clearly unavailable and shall not produce success or debit.

**Source:** SWE1-SRC-002 §8 CAN-WB-007; §9 CAN-SCOPE-002  
**Rationale:** Prevents incomplete future functionality from becoming usable.  
**Precondition / trigger:** A player views navigation/shop or directly attempts a Waystone action.  
**Required observable result:** The action cannot succeed and no Waymark debit or placement authority is created.  
**Verification intent:** SWE.4 action-policy verification, SWE.5 GUI/command integration, SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-CON-008 — Navigation theme boundary

**Normative statement:** Navigation operations shall be denied outside exact `frontier_iris`.

**Source:** SWE1-SRC-002 §8 CAN-WB-007  
**Rationale:** Preserves theme isolation.  
**Precondition / trigger:** A Navigation item or direct navigation route is used outside the theme.  
**Required observable result:** No GUI capability or protected action succeeds.  
**Verification intent:** SWE.4 world/authority verification and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-WB-001-QLT-005 — Navigation presentation independence

**Normative statement:** Shop, Loadout, Help, and unavailable-state information shall be clear and operable, but exact language and layout shall not be functional acceptance obligations for this scope.

**Source:** SWE1-SRC-002 §8 CAN-WB-007; AMD-010  
**Rationale:** Separates required capability from presentation refinement.  
**Precondition / trigger:** The navigation interface is shown.  
**Required observable result:** The required entries and unavailable status are understandable and usable.  
**Verification intent:** SWE.6 client inspection and Owner usability review.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
