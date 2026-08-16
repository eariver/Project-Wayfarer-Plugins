# Main Requirement Review Corrections — CAN-MAIN-001 through CAN-MAIN-005

Document ID: `DEC-REQ-006`  
Revision: A  
State: `APPROVED`  
Date: 2026-08-15 JST  
Author: ChatGPT  
Approver: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Affected source: `SWE1-SRC-002` Revision C  
Affected SWE.1 documents: `SWE1-MAIN-001`, `TRC-SWE1-001`, `SWE1-VERIFY-001`, `SWE1-INDEX-001`

## 1. Purpose

Record the Owner-approved correction directions resulting from joint review of `CAN-MAIN-001` through `CAN-MAIN-005` and control their integration at the first Main-section repository checkpoint.

This decision follows the Common/Core corrections in `DEC-REQ-002`, `DEC-REQ-004`, and `DEC-REQ-005` and the checkpoint/session-continuity policy in `DEC-REQ-003`. It does not approve `CAN-MAIN-006` or later clauses, G1, SWE.2, SWE.3, Product implementation, verification execution, PR readiness, merge, tag, deployment, or release.

## 2. CAN-MAIN-001 — Main capability deployment and lifecycle

### 2.1 Approved correction

Main capability ownership is independent of a historical `Main` backend identity, physical server name, or equivalent topology label. Main capabilities execute only where approved integration/deployment configuration allocates them and while their actual mandatory prerequisites remain valid.

Mandatory prerequisites are capability-scoped. Applicable prerequisites may include approved configuration, compatible Main-owned schema state, required approved shared/external capability contracts, and capability-specific gameplay/content context. Loss of a prerequisite makes only the capabilities requiring it unavailable unless the prerequisite is shared by every active Main capability or another approved requirement explicitly requires broader unavailability.

For V0.0.2, Main financial capabilities using the shared Waymark transaction contract require the compatible Core-provided contract allocated by `CAN-CORE-003`. The presence of Wayfarer_Core is not thereby a universal prerequisite for unrelated Main capabilities.

Common lifecycle admission-closure, accepted-operation disposition, and stale-completion requirements apply. SWE.1 does not prescribe whole-plugin disablement, listener registration, startup ordering, or another lifecycle mechanism.

The previously derived `operationally observable reason` requirement is removed from this clause because no reviewed source currently requires that additional observability contract.

### 2.2 Required SWE.1 changes

- revise `SWE1-MAIN-001-CON-001` to **Main capability allocation independent of backend naming**;
- revise `SWE1-MAIN-001-QLT-001` to **Main capability prerequisite validation**;
- preserve capability-scoped fail-closed semantics inherited from Common requirements.

No new Product requirement is required.

## 3. CAN-MAIN-002 — Logical Growth Tool authority and durable state

### 3.1 Approved correction

Wayfarer_Main owns the logical Growth Tool domain. MariaDB is the durable authority for Main-owned logical Growth Tool state under the Common state-authority requirements; MariaDB does not own Growth Tool gameplay semantics.

For each owner and approved Growth Tool type, at most one logical Growth Tool authority may exist. V0.0.2 approves only `PICKAXE`; this uniqueness rule does not add other tool types to current scope.

Lifecycle status, delivery status, and active branch are independent state dimensions:

- lifecycle: `ACTIVE`, `BROKEN`, `REVOKED`;
- delivery: `DELIVERED`, `PENDING`;
- branch: `FORTUNE`, `SILK_TOUCH`.

Durable logical authority retains the semantic state required for logical continuity, including stable logical identity, owner identity, approved tool type, current epoch, cumulative progress, lifecycle state, delivery state, and active branch.

The Owner subsequently refined the durability authority during `CAN-MAIN-005` review: current physical durability/damage is not Main-owned durable database state. Minecraft's physical item state is authoritative for current durability under `CAN-COM-003`. Main may inspect and, through approved Wayfarer operations, mutate that physical durability, but it shall not maintain a separate authoritative current-damage value in the logical Growth Tool record.

SWE.1 does not prescribe a table layout, per-record schema-version field, optimistic-lock counter, creation/update timestamps, checkpoint-timestamp field, or another persistence/concurrency mechanism.

### 3.2 Required SWE.1 changes

- revise `SWE1-MAIN-001-CAP-001` to **Logical Growth Tool uniqueness**;
- revise `SWE1-MAIN-001-CAP-002` to **Logical tool lifecycle state**;
- add `SWE1-MAIN-001-CAP-010` — **Logical delivery state**;
- add `SWE1-MAIN-001-CAP-011` — **Logical branch state**;
- revise `SWE1-MAIN-001-CON-002` to **Durable logical-state sufficiency**, explicitly excluding current durability/damage as separate DB authority.

This atomic decomposition adds two CAP requirements.

## 4. CAN-MAIN-003 — Physical representation identity and logical-authority validation

### 4.1 Approved correction

A physical Growth Tool/Broken Tool is a Minecraft runtime representation of Main-owned logical Growth Tool authority. Item metadata is a claim/reference used to correlate the physical representation with current logical authority; it is not independent authority.

Each managed physical representation carries supported persistent machine-readable identity sufficient to correlate the managed item class, stable logical tool identity, physical instance/issuance identity, current authority epoch, and supported identity-format semantics. Exact PDC key names, namespace, field layout, serialization representation, and redundant copies of logical owner/tool type are SWE.2/SWE.3 choices. Paper PDC remains an allowed implementation mechanism but is not fixed as the Product-level encoding contract.

Material, display name, lore, enchantments, presentation revision, visual similarity, and equivalent player-visible/mutable attributes do not by themselves establish managed-item authority. `display revision` is not an authority element.

Before an operation requiring current physical Growth Tool authority, Main resolves the physical identity against current logical authority. Unsupported/malformed identity, unresolved or mismatched logical authority, subject-owner mismatch, invalid current issuance where applicable, or stale epoch fails closed.

Revocation semantics are not independently sourced by `CAN-MAIN-003`; the relevant lifecycle/administrative clauses remain responsible for that state transition.

### 4.2 Required SWE.1 changes

- revise `SWE1-MAIN-001-CAP-003` to **Physical representation identity**;
- revise `SWE1-MAIN-001-CON-003` to **No presentation-derived authority**;
- revise `SWE1-MAIN-001-CAP-004` to **Current physical-authority validation**;
- revise `SWE1-MAIN-001-CON-004` to **Invalid or stale physical identity fail-closed**.

No new Product requirement is required.

## 5. CAN-MAIN-004 — Initial logical entitlement and durable delivery

### 5.1 Approved correction

The initial entitlement trigger is an eligible Main Growth Tool capability entry condition, not a permanently fixed physical `Main backend` join. Durable resolution shall not block a runtime execution context on which such I/O is prohibited.

Logical-entitlement establishment and physical delivery are distinct effects. Concurrent/replayed entry shall not create duplicate logical authority or duplicate initial delivery entitlement. Before an outstanding initial/pending delivery mutates player inventory, Main revalidates the current lifecycle, player, capability-context, logical-authority, and delivery prerequisites and uses a platform-authorized execution context.

If a still-valid delivery entitlement cannot be safely completed, including insufficient inventory capacity, Main shall not create/use a world-item drop as a fallback mechanism for completing or bypassing delivery. The same outstanding delivery entitlement remains durably pending/recoverable under the Common lifecycle/protected-operation requirements.

This fallback-delivery prohibition is narrow: it does not prohibit ordinary user-initiated or Minecraft entity/death drop behavior after a physical Growth Tool has already been delivered.

When a pending outcome occurs while the player is reachable, Main provides an actionable notification and retains retrievable evidence correlating the outstanding entitlement with the applicable reason/classification. Later eligible or authorized administrative retry continues the same entitlement without Waymark debit, logical-authority duplication, authority rotation, or duplicate delivery.

A `DELIVERED` logical tool is not implicitly reissued because the player re-enters the capability context or the physical item is not observed. Replacement uses an authorized recovery/reissue flow.

### 5.2 Required SWE.1 changes

- revise `SWE1-MAIN-001-CAP-005` to **Race-safe initial logical-entitlement resolution**;
- revise `SWE1-MAIN-001-CAP-006` to **Revalidated physical delivery**;
- revise `SWE1-MAIN-001-CAP-007` to **Durable pending delivery without fallback world drop**;
- revise `SWE1-MAIN-001-CON-005` to **No implicit replacement of delivered authority**;
- revise `SWE1-MAIN-001-CAP-009` to **Pending-delivery notification and same-entitlement retry**.

No new Product requirement is required.

## 6. CAN-MAIN-005 — Owner-bound use and controlled tool modification

### 6.1 Approved correction

Owner binding applies to Growth Tool use/authority, not to physical possession or storage. A physical Growth Tool remains logically bound to its current owner regardless of which player, inventory, storage container, item entity, or other supported Minecraft possession context currently contains it. Physical transfer, pickup, drop, storage, or possession does not transfer logical ownership.

Only the current logical owner may use a current authorized Growth Tool as a Growth Tool, gain Growth Tool progress, or perform another owner-authorized Growth Tool operation. A non-owner may physically possess, carry, pick up, drop, store, or transfer the item but gains no Growth Tool authority from possession.

Wayfarer_Main shall not prohibit ordinary supported physical possession, inventory/storage transfer, pickup, or player/entity drop merely because the item is a Growth Tool. This includes player inventory, chest, Ender Chest, Shulker Box, and equivalent supported Minecraft storage/transfer behavior. Ordinary death/entity drop behavior is therefore not to be blocked by Owner binding; the conflicting still-unreviewed `CAN-MAIN-006` wording is carried forward for explicit review rather than silently rewritten at this checkpoint.

Growth Tool durability restoration and Growth Tool enchantment-state modification are Wayfarer-controlled. Enchantment modification includes addition, increase, removal, reduction, transfer, and replacement. Ordinary durability loss remains governed by Minecraft/Paper physical-state behavior.

In V0.0.2 a Growth Tool shall not be processed through an anvil or grindstone. The prohibition includes repair, combination, enchantment modification/transfer, and anvil renaming. Crafting repair/combination, Mending-based durability restoration, smithing/equivalent transformation that bypasses approved Growth Tool evolution, and supported external modification paths likewise shall not apply a prohibited durability-restoring, enchantment-changing, or evolution-bypassing effect outside an applicable Wayfarer-controlled operation.

Physical movement or possession does not rotate Growth Tool authority. A later authorized reissue rotates current physical authority; older physical instances may remain physically present anywhere permitted by Minecraft but become stale and unusable as an authorized Growth Tool.

### 6.2 Required SWE.1 changes

- retain/revise `SWE1-MAIN-001-CAP-008` as **Owner-authorized use**;
- revise `SWE1-MAIN-001-CON-006` to **Non-owner Growth Tool use denial**;
- replace the old blanket transfer restriction in `SWE1-MAIN-001-CON-007` with **Ordinary physical possession and storage neutrality**;
- add `SWE1-MAIN-001-CON-010` — **Wayfarer-exclusive durability restoration and enchantment modification**;
- add `SWE1-MAIN-001-CON-011` — **Anvil and grindstone processing prohibition**.

The supported external-repair/modification boundary remains open under `SWE1-ISSUE-001-ISSUE-002`; the Product policy is fixed, while SWE.2 must identify the supported public/cancellable integration boundary.

## 7. Required propagation to later unreviewed Main clauses

The following Owner determinations were made while reviewing `CAN-MAIN-001` through `CAN-MAIN-005` but directly conflict with later canonical wording. They are recorded as mandatory inputs to the owning later clause reviews; those later clauses are not approved by this decision.

### 7.1 CAN-MAIN-006 — death behavior

Current `CAN-MAIN-006`/AMD-006 death-drop suppression conflicts with the approved possession/drop neutrality. The owning review must remove any Wayfarer prohibition on ordinary Growth Tool/Broken Tool drop caused by player/entity death or despawn behavior. The separate question of automatic respawn restoration remains for `CAN-MAIN-006` review.

### 7.2 CAN-MAIN-016 — paid reissue

Reissue safety shall not depend on proving that the current physical instance is absent from every possible player/storage/world context. The owning review shall instead rely on successful authority rotation: a new physical issuance/current epoch is established and prior physical instances become stale/unusable as Growth Tools.

A successful reissue produces a fully repaired new physical item. The exact current formula `broken_repair_cost + full_repair_cost` is no longer a fixed Owner requirement; the owning `CAN-MAIN-016` review shall preserve the Product invariant that paid reissue is always priced strictly above the applicable repair price for the same logical tool/configuration.

The existing pending-delivery no-charge retry distinction remains applicable and will be reviewed with `CAN-MAIN-016`.

## 8. Requirement-count effect

The integrated Core checkpoint contained 175 provisional active Product requirements.

Main review through `CAN-MAIN-005` adds four atomic requirements without removing an active identifier:

- `SWE1-MAIN-001-CAP-010` — Logical delivery state;
- `SWE1-MAIN-001-CAP-011` — Logical branch state;
- `SWE1-MAIN-001-CON-010` — Wayfarer-exclusive durability restoration and enchantment modification;
- `SWE1-MAIN-001-CON-011` — Anvil and grindstone processing prohibition.

The provisional integrated total after this checkpoint is therefore **179** Product requirements:

- CAP: 66
- CON: 60
- IFC: 14
- QLT: 39

No existing Main identifier is renumbered. Still-unreviewed later-clause requirements that conflict with the propagation directions in §7 remain draft conflict items until their owning canonical clause is reviewed.

## 9. Checkpoint and downstream obligations

The Owner explicitly approved the correction directions for `CAN-MAIN-001` through `CAN-MAIN-005` and authorized repository checkpoint reflection after the fifth newly approved Main clause.

At this checkpoint, the approved corrections shall be integrated into the canonical source through `CAN-MAIN-005`, `SWE1-MAIN-001`, index, traceability, verification-intent allocation, review log, source/status/continuation records, and requirement counts. Later Main canonical clauses are not silently rewritten; conflicts identified in §7 are carried into their owning review.

The next substantive canonical review item after checkpoint integration is `CAN-MAIN-006 — Death behavior`.

All gate restrictions remain unchanged.