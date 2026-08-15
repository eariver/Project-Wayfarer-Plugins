# Main Lifecycle, Tool Authority, Delivery, and Modification Requirements

Document ID: `SWE1-MAIN-001`  
Revision: D  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-15 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision E  
Controlling review decisions: `DEC-REQ-002`, `DEC-REQ-004`, `DEC-REQ-006`, `DEC-REQ-007`  
Contained active normative items: CAP: 11, CON: 9, QLT: 2; historical `CON-008` and `CON-009` superseded

## 1. Purpose

Define Main capability lifecycle, logical and physical Growth Tool authority, initial delivery, owner-bound use, ordinary possession/storage/death-drop neutrality, and controlled tool modification. `CAN-MAIN-001` through `CAN-MAIN-006` are jointly reviewed and integrated under `DEC-REQ-006` / `DEC-REQ-007`; later Main clauses remain subject to their own clause-by-clause review.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue or explicit later-clause conflict remains draft and cannot support G1 PASS until resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- Minecraft-owned physical item state, including current durability and ordinary item lifecycle/possession, is not silently duplicated as authoritative Main durable state unless a later approved requirement explicitly requires it.
- Superseded identifiers remain historical and are not reused or renumbered.

## 3. Requirements

### SWE1-MAIN-001-CON-001 — Main capability allocation independent of backend naming

**Normative statement:** Wayfarer_Main shall not gate Main capability availability solely on a historical `Main` backend identity, physical server name, or equivalent topology label. A Main capability shall execute only where it is allocated/enabled by approved integration/deployment configuration and its actual mandatory prerequisites are satisfied.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-001; DEC-REQ-006 §2  
**Rationale:** Preserves capability ownership while permitting approved deployment consolidation, division, or co-location.  
**Precondition / trigger:** A Main capability is considered for activation or execution on an integrated runtime.  
**Required observable result:** Backend naming alone neither enables nor disables the capability; approved allocation plus actual prerequisites control availability.  
**Verification intent:** SWE.2/SWE.3 allocation inspection followed by SWE.5 deployment/lifecycle integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-CON-001; SWE1-COMMON-001-CON-002  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-QLT-001 — Main capability prerequisite validation

**Normative statement:** Each Main capability shall be available only while its applicable approved configuration, compatible Main-owned schema state, required approved shared/external capability contracts, and capability-specific gameplay/content prerequisites remain valid. Loss of a prerequisite shall close only the capabilities that require it unless that prerequisite is shared by all active Main capabilities or another approved requirement explicitly requires broader unavailability.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-001; §4 CAN-COM-006; DEC-REQ-004 §2; DEC-REQ-006 §2  
**Rationale:** Specializes Common lifecycle rules without making a backend name, plugin identity, or provider implementation a universal prerequisite.  
**Precondition / trigger:** Capability startup and whenever an assigned mandatory prerequisite is refreshed, invalidated, or lost.  
**Required observable result:** Affected capabilities close admission/effects according to Common lifecycle requirements while unrelated capabilities may remain available when their own prerequisites remain valid.  
**Verification intent:** SWE.4 prerequisite-policy verification and SWE.5 Main lifecycle/integration verification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-003; SWE1-COMMON-001-QLT-004; SWE1-COMMON-001-QLT-010; SWE1-COMMON-001-QLT-011  
**Assumptions:** None  
**Open issue / conflict:** None  
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

### SWE1-MAIN-001-CAP-001 — Logical Growth Tool uniqueness

**Normative statement:** Main shall maintain at most one logical Growth Tool authority for each `(owner UUID, approved tool type)` pair. V0.0.2 defines `PICKAXE` as the only approved Growth Tool type.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002; DEC-REQ-006 §3  
**Rationale:** Defines the logical entitlement/authority duplicate boundary without requiring every possible player to have a record before entitlement establishment.  
**Precondition / trigger:** A logical Growth Tool is created, granted, resolved, repaired, or reissued.  
**Required observable result:** Concurrent or replayed creation/grant activity resolves to at most one logical `PICKAXE` authority for the owner.  
**Verification intent:** SWE.4 uniqueness/race verification and SWE.5 MariaDB integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-002 — Logical tool lifecycle state

**Normative statement:** Each logical Growth Tool shall have exactly one current lifecycle status from `ACTIVE`, `BROKEN`, or `REVOKED`, subject to the approved state-transition requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002; DEC-REQ-006 §3  
**Rationale:** Separates lifecycle authority from delivery and branch state so each state dimension remains independently assessable.  
**Precondition / trigger:** A logical tool is created or its lifecycle authority changes.  
**Required observable result:** Exactly one valid lifecycle state is represented durably.  
**Verification intent:** SWE.4 lifecycle-state verification and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** Exact transition semantics remain governed by the owning later Main clauses.  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-010 — Logical delivery state

**Normative statement:** Each logical Growth Tool shall have exactly one current delivery status from `DELIVERED` or `PENDING`, representing whether the applicable authorized physical-delivery obligation has been established as completed or remains outstanding under the delivery requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002; DEC-REQ-006 §3  
**Rationale:** Makes delivery obligation state independently durable without equating `DELIVERED` with current observation of the physical item.  
**Precondition / trigger:** A delivery entitlement is established, completed, or remains outstanding.  
**Required observable result:** Exactly one valid delivery state is represented and drives the applicable delivery/recovery flow.  
**Verification intent:** SWE.4 delivery-state verification and SWE.5 persistence/delivery integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** Detailed transition semantics remain governed by delivery/reissue clauses.  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-011 — Logical branch state

**Normative statement:** Each logical Growth Tool shall retain exactly one active evolution branch from `FORTUNE` or `SILK_TOUCH`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002; DEC-REQ-006 §3  
**Rationale:** Makes the active evolution branch an explicit durable domain state independent of lifecycle and delivery state.  
**Precondition / trigger:** A logical tool is created or an authorized branch change occurs.  
**Required observable result:** Exactly one approved branch remains associated with the logical tool across supported restart and later operations.  
**Verification intent:** SWE.4 branch-state verification and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** Detailed branch behavior remains governed by later evolution/admin clauses.  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-002 — Durable logical-state sufficiency

**Normative statement:** Main-owned durable Growth Tool authority shall retain the semantic state necessary for logical continuity across supported restart, including stable logical identity, owner identity, approved tool type, current authority epoch, cumulative progress, lifecycle status, delivery status, and active branch. Current physical durability/damage shall remain authoritative in Minecraft physical item state and shall not be maintained as a separate authoritative current-damage value in the logical Growth Tool record.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-002; §4 CAN-COM-003; DEC-REQ-006 §3  
**Rationale:** Retains domain-authoritative state while separating Minecraft-owned physical durability from Main-owned durable logical authority.  
**Precondition / trigger:** A logical tool is created, loaded, persisted, or reconciled with its physical representation.  
**Required observable result:** Logical identity/authority can be recovered without requiring a prescribed table layout or a duplicate authoritative durability value.  
**Verification intent:** SWE.4 state-model inspection plus SWE.5 MariaDB/Minecraft authority integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-IFC-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-003 — Physical representation identity

**Normative statement:** Each Growth Tool or Broken Tool physical representation shall carry supported persistent machine-readable identity sufficient to correlate it with its logical Growth Tool authority and current physical issuance, including semantic identification of the managed item class, logical tool, physical instance/issuance, authority epoch, and supported identity format.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003; DEC-REQ-006 §4  
**Rationale:** Allows physical representations to be resolved against current logical authority without fixing a PDC key set or serialization layout in SWE.1.  
**Precondition / trigger:** A managed physical item is created, reconstructed, loaded, or presented for a managed operation.  
**Required observable result:** Persistent machine-readable identity is sufficient to resolve one logical authority/current issuance or to fail closed.  
**Verification intent:** SWE.4 identity-format policy verification and SWE.5 adopted-platform item-metadata integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-003 — No presentation-derived authority

**Normative statement:** Material, display name, lore, enchantments, presentation revision, visual similarity, or other ordinarily mutable presentation/gameplay attributes shall not by themselves establish Growth Tool authority.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003; DEC-REQ-006 §4  
**Rationale:** Prevents ordinary presentation similarity or mutation from establishing managed authority.  
**Precondition / trigger:** A managed-looking item is presented for a Growth Tool operation.  
**Required observable result:** Authorization requires resolution of persistent identity against current logical authority rather than presentation similarity alone.  
**Verification intent:** SWE.4 authorization/forgery verification and SWE.6 representative forged-looking item qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-004 — Current physical-authority validation

**Normative statement:** Before a managed physical representation is accepted for an operation requiring current Growth Tool authority, Main shall resolve its identity against current logical authority and validate the applicable logical tool, owner/subject relationship, current physical issuance, and authority epoch together with the operation-specific state prerequisites defined by the owning requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003; CAN-MAIN-005; DEC-REQ-006 §§4,6  
**Rationale:** Makes current logical authority, not physical possession or metadata alone, the authorization boundary.  
**Precondition / trigger:** A managed physical representation reaches a Growth Tool gameplay, management, financial, or administrative operation requiring current authority.  
**Required observable result:** Only a representation/current logical authority combination satisfying the applicable operation prerequisites proceeds.  
**Verification intent:** SWE.4 common-authority guard verification, SWE.5 event/command integration, and representative SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-004 — Invalid or stale physical identity fail-closed

**Normative statement:** A managed-looking physical item whose identity is unsupported, malformed, unresolved, inconsistent with current logical authority, associated with the wrong subject for an owner-authorized operation, associated with a non-current physical issuance where applicable, or stale by authority epoch shall be denied before a protected managed-tool effect.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-003; DEC-REQ-006 §4  
**Rationale:** Prevents malformed, forged, mismatched, or superseded physical representations from gaining current authority.  
**Precondition / trigger:** Invalid or stale identity reaches an entry point requiring current Growth Tool authority.  
**Required observable result:** No protected managed-tool effect is produced from the invalid/stale representation.  
**Verification intent:** SWE.4 invalid-identity matrix and SWE.6 representative stale/forged qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** Revocation state semantics remain sourced by their owning lifecycle/admin clauses rather than CAN-MAIN-003 alone.  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-005 — Race-safe initial logical-entitlement resolution

**Normative statement:** On an eligible initial Main Growth Tool capability entry, Main shall resolve the owner's existing logical Growth Tool authority or establish it when absent without blocking prohibited runtime execution contexts. Concurrent or replayed entitlement establishment shall satisfy the approved logical-tool uniqueness and protected-operation duplicate-prevention requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-005; §4 CAN-COM-007; DEC-REQ-006 §5  
**Rationale:** Establishes one logical entitlement without topology-fixed join semantics, blocking durable I/O, or duplicate authority/delivery.  
**Precondition / trigger:** An eligible player reaches the initial Growth Tool capability-entry condition.  
**Required observable result:** The existing authority is resolved or one logical authority is established despite concurrent/replayed entry; prohibited runtime contexts are not blocked by durable I/O.  
**Verification intent:** SWE.4 uniqueness/replay/context policy verification and SWE.5 MariaDB/platform integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-002; SWE1-COMMON-001-QLT-005; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-006 — Revalidated physical delivery

**Normative statement:** Before an outstanding initial or pending delivery entitlement produces player-inventory mutation, Main shall revalidate all applicable current lifecycle, player, capability-context, logical-authority, and delivery prerequisites and perform the mutation only from an execution context authorized by the adopted platform.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-005; DEC-REQ-006 §5  
**Rationale:** Prevents late/stale physical delivery after asynchronous/deferred resolution or context change.  
**Precondition / trigger:** An outstanding initial/pending delivery attempt becomes ready for player-inventory mutation.  
**Required observable result:** Delivery occurs only when all current prerequisites remain valid and the current platform execution context permits the mutation; otherwise the still-valid obligation remains safely recoverable.  
**Verification intent:** SWE.4 revalidation/context verification and SWE.5 join/logout/context-change delivery integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-001; SWE1-COMMON-001-QLT-009; SWE1-COMMON-001-QLT-011  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-007 — Durable pending delivery without fallback world drop

**Normative statement:** When a still-valid Growth Tool delivery entitlement cannot be safely completed, including for insufficient inventory capacity, Main shall not create or use a world-item drop as a fallback means of completing/bypassing that delivery and shall retain the same delivery entitlement as an applicable durable pending/recoverable obligation rather than creating duplicate authority or delivery. This fallback-delivery restriction does not prohibit ordinary player-initiated or Minecraft entity/death drop behavior after delivery has completed.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-007; DEC-REQ-006 §5; §6 CAN-MAIN-006; DEC-REQ-007 §2  
**Rationale:** Prevents unsafe fallback delivery and duplicate entitlement while preserving normal post-delivery Minecraft possession/drop behavior.  
**Precondition / trigger:** A still-valid initial/replacement delivery cannot be safely completed.  
**Required observable result:** No fallback world delivery occurs; one existing entitlement remains pending/recoverable. Ordinary post-delivery death/entity drops remain unaffected.  
**Verification intent:** SWE.4 delivery-policy verification, SWE.5 inventory-full/context-failure/death-drop integration, and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-005 — No implicit replacement of delivered authority

**Normative statement:** A logical Growth Tool whose current delivery state is `DELIVERED` shall not receive a new physical issuance, epoch rotation, or replacement delivery merely because the player re-enters the capability context, dies/respawns, the physical item is lost/destroyed, or the currently delivered physical item is not observed. Replacement shall occur only through an authorized recovery/reissue flow.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; CAN-MAIN-006; DEC-REQ-006 §5; DEC-REQ-007 §2  
**Rationale:** Distinguishes completed delivery from current physical-item observability/lifecycle and prevents silent duplication or free authority rotation.  
**Precondition / trigger:** A `DELIVERED` owner re-enters, dies/respawns, loses the item, or the current physical item is not observed.  
**Required observable result:** No new physical issuance, delivery entitlement, or epoch occurs without an authorized recovery/reissue flow.  
**Verification intent:** SWE.4 delivery/replacement policy verification and SWE.5 re-entry/death/respawn/loss integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** Paid-reissue eligibility/current-item absence semantics remain for CAN-MAIN-016 review under DEC-REQ-006 §7.2.  
**State:** `DRAFT`

### SWE1-MAIN-001-CAP-008 — Owner-authorized use

**Normative statement:** The current logical owner shall be permitted to use the current authorized active Growth Pickaxe when the applicable operation-specific prerequisites are satisfied.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005; DEC-REQ-006 §6  
**Rationale:** States the positive side of owner binding without binding ordinary physical possession to the owner.  
**Precondition / trigger:** The current logical owner attempts an approved operation with the current authorized active Growth Pickaxe.  
**Required observable result:** The operation is not denied merely by owner/identity binding when all applicable prerequisites are valid.  
**Verification intent:** SWE.5 gameplay integration and SWE.6 Main qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-006 — Non-owner Growth Tool use denial

**Normative statement:** A player other than the current logical owner shall not use a current or stale physical Growth Tool as an authorized Growth Tool, gain Growth Tool progress, or perform another owner-authorized Growth Tool operation merely by possessing its physical representation.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005; DEC-REQ-006 §6  
**Rationale:** Keeps Growth Tool authority bound to the logical owner while allowing ordinary non-owner possession/transfer.  
**Precondition / trigger:** A non-owner possesses or presents a managed Growth Tool for an owner-authorized operation.  
**Required observable result:** Owner-only use/progress/effects are denied without transferring logical ownership.  
**Verification intent:** SWE.4 authorization verification and SWE.6 representative non-owner-use qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-007 — Ordinary physical possession and lifecycle neutrality

**Normative statement:** Wayfarer_Main shall not prohibit ordinary supported physical possession, player inventory/storage transfer, pickup, or player/entity death/item-lifecycle drop solely because an item is a Growth Tool or Broken Tool. Physical movement through player inventories, chest, Ender Chest, Shulker Box, equivalent supported Minecraft storage/transfer contexts, world item entities, non-owner possession, death drop, ordinary loss/destruction, or despawn shall not transfer or change logical Growth Tool ownership or current authority.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005; CAN-MAIN-006; DEC-REQ-006 §6; DEC-REQ-007 §2  
**Rationale:** Separates logical owner authority from Minecraft physical possession/lifecycle and avoids unnecessary managed-item restrictions.  
**Precondition / trigger:** A managed physical item undergoes an ordinary supported possession, pickup, drop, death/lifecycle, loss, destruction, or storage transfer.  
**Required observable result:** Wayfarer does not cancel the ordinary movement/lifecycle merely because the item is managed, and no logical ownership/authority transfer is inferred from it.  
**Verification intent:** SWE.4 possession/lifecycle-policy verification, SWE.5 representative inventory/storage/death/drop integration, and SWE.6 representative client qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-010 — Wayfarer-exclusive durability restoration and enchantment modification

**Normative statement:** Growth Tool durability restoration and Growth Tool enchantment-state modification shall occur only through an operation authorized by Wayfarer_Main under the applicable Growth Tool requirements. Enchantment modification includes addition, increase, removal, reduction, transfer, and replacement. Supported vanilla or external interaction paths shall not apply a prohibited durability-restoring, enchantment-changing, or approved-evolution-bypassing effect outside an applicable Wayfarer-controlled operation. Ordinary Minecraft/Paper durability loss remains permitted under the applicable durability requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005; DEC-REQ-006 §6  
**Rationale:** Preserves Wayfarer-controlled repair/evolution semantics while leaving ordinary possession and normal durability consumption to Minecraft.  
**Precondition / trigger:** A vanilla, plugin, or external interaction attempts to restore durability, modify enchantments, or bypass approved Growth Tool evolution.  
**Required observable result:** The prohibited effect is not applied unless it is part of an applicable authorized Wayfarer operation.  
**Verification intent:** SWE.4 modification-policy verification and SWE.5 supported vanilla/external integration verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** `SWE1-ISSUE-001-ISSUE-002` remains open for the exact supported external public/cancellable integration boundary.  
**State:** `DRAFT`

### SWE1-MAIN-001-CON-011 — Anvil and grindstone processing prohibition

**Normative statement:** In V0.0.2 a Growth Tool or Broken Tool shall not be processed through an anvil or grindstone. The prohibited processing includes repair, combination, enchantment modification or transfer, grindstone enchantment removal, and anvil renaming.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-005; DEC-REQ-006 §6  
**Rationale:** Provides an explicit supported-Vanilla boundary for item-processing routes whose semantics can modify the controlled Growth Tool representation; rename is intentionally prohibited for V0.0.2 despite not currently carrying authority.  
**Precondition / trigger:** A Growth Tool or Broken Tool is supplied to an anvil or grindstone operation.  
**Required observable result:** The managed item is not processed into a resulting modified item through that route.  
**Verification intent:** SWE.4 route-policy verification and SWE.5 Paper anvil/grindstone integration qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CON-010  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

## 4. Historical superseded identifiers

### SWE1-MAIN-001-CON-008 — Death-drop suppression

**Disposition:** `SUPERSEDED_BY_OWNER_CORRECTION`  
**Former source:** CAN-MAIN-006; AMD-006  
**Replacement coverage:** `SWE1-MAIN-001-CON-007` now requires ordinary death/entity/item-lifecycle drop neutrality. The former suppression behavior is withdrawn by `DEC-REQ-007` §2.  
**Identifier reuse:** Prohibited.

### SWE1-MAIN-001-CON-009 — No automatic respawn restoration

**Disposition:** `SUPERSEDED_BY_DEDUPLICATION`  
**Former source:** CAN-MAIN-006; AMD-006  
**Replacement coverage:** `SWE1-MAIN-001-CON-005` now explicitly covers death/respawn, physical loss/destruction, and non-observation without implicit replacement. No separate respawn-only requirement remains necessary.  
**Identifier reuse:** Prohibited.

## 5. Remaining active requirement

### SWE1-MAIN-001-CAP-009 — Pending-delivery notification and same-entitlement retry

**Normative statement:** When a Growth Tool delivery becomes pending while the player is reachable, Main shall provide an actionable notification and retain retrievable evidence correlating the pending delivery entitlement with its applicable reason/classification. A later eligible entry or authorized administrative retry shall continue the same entitlement without Waymark debit, authority rotation, or duplicate delivery.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-004; §4 CAN-COM-007; §4 CAN-COM-009; DEC-REQ-006 §5  
**Rationale:** Makes pending delivery recoverable/auditable without turning retry into a new entitlement or paid reissue.  
**Precondition / trigger:** A still-valid Main item-delivery entitlement becomes pending.  
**Required observable result:** Reachable player receives an actionable disposition; the pending entitlement/reason remains retrievable; retry continues the same entitlement with no duplicate protected effect.  
**Verification intent:** SWE.4 disposition/replay/audit verification, SWE.5 retry/restart integration, and SWE.6 representative notification qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-COMMON-001-QLT-008; SWE1-COMMON-001-QLT-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
