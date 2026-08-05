# V0.0.2 SWE.1 Glossary and Controlled Terms

Document ID: `SWE1-GLOSSARY-001`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `GLOSSARY`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A

## 1. Purpose

Provide controlled terminology used by the SWE.1 target requirements. A term definition does not add
a requirement unless a target requirement cites the term.

## 2. Terms

| Term | Controlled meaning |
|---|---|
| Active Growth Tool | The current physical representation of a logical Growth Tool whose durable tool status is `ACTIVE` |
| Broken Tool | The owner-bound `GRAY_DYE` representation of the same logical Growth Tool when durable tool status is `BROKEN` |
| Current physical instance | A physical item whose identity and epoch match current durable authority |
| Logical Growth Tool | The MariaDB-authoritative player entitlement and progression record, independent of any one physical ItemStack |
| Physical instance ID | Stable identity of one physical item issuance/reconstruction as defined by the approved authority contract |
| Epoch | Monotonic authority generation used to invalidate older physical instances after authority rotation |
| Pending delivery | A typed durable obligation to deliver a Wayfarer-owned item when safe delivery preconditions are later satisfied |
| Delivered | Durable delivery state indicating that the entitlement is not currently awaiting the free pending-delivery path |
| Safe entry | A player becoming eligible while online in exact `frontier_iris`, after current-session and exact-world revalidation |
| Permanent Worlds Beyond item | Elytra, authentic LeafGrapple hook, or Navigation item; excludes Launchpad, rocket, and other consumables |
| Launchpad item | Unplaced consumable typed item used to request placement |
| Active Launchpad | Durable launchpad authority associated with an expected physical block and active lifecycle state |
| Successful launch | A Launchpad activation that passes authority, claim, cooldown, and safety checks and applies the approved movement effect |
| Current configuration | The internally consistent approved configuration revision active when an operation is evaluated |
| Creation-time durable value | A value fixed in Launchpad durable authority at successful placement and not changed by later live configuration |
| Fixed-point progress | Integer representation in which 1000 units equal 1.000 displayed progress |
| Saturation | Arithmetic behavior in which positive addition is capped at `Long.MAX_VALUE` rather than overflowing |
| Conceptual evolution count | Evolution count that continues after effective enchantment caps and remains derived from cumulative progress |
| `UNKNOWN` | Operation disposition in which an external effect cannot be proven successful or failed and must not be automatically retried |
| Fail closed | Deny or keep unavailable the protected capability when mandatory authority, dependency, schema, identity, world, or safety state cannot be established |
| Owner binding | Requirement that only the current owner and current authority may use or operate a protected item |
| Theme binding | Requirement that Worlds Beyond capability is available only in exact `frontier_iris` |
| Normal player state | Inventory, armor, offhand, Ender Chest, XP, health, food, MVI profile, and similar state owned by Minecraft/MVI rather than Wayfarer persistence |
| Typed identity | Domain-specific identifiers and schema fields sufficient to reconstruct and authorize a Wayfarer item without storing raw normal inventory |
| Typed pending delivery | Pending delivery that records the item/authority type and identifiers, not a serialized raw player inventory or unrestricted ItemStack snapshot |
| Authoritative record | Current durable data owned by the domain's assigned authority |
| Reissue | Explicit authority/physical-item recovery operation; may rotate epoch depending on the target requirement |
| Free delivery retry | Retry of an existing pending delivery obligation without a Waymark debit or new entitlement |
| Reconciliation | Authorized inspection and correction of a known discrepancy using current durable authority and explicit operation disposition |
| Runtime generation | One active lifecycle instance used to reject callbacks created by an obsolete or disabled plugin runtime |

## 3. Exact identifiers and worlds

Exact values are case-sensitive where the platform contract is case-sensitive:

- Main progress worlds: `resource`, `resource_nether`, `resource_end`
- Worlds Beyond world: `frontier_iris`
- Main permissions: defined by `SWE1-MAIN-003-IFC-001`
- Frontier permissions: defined by `SWE1-FRONTIER-001-IFC-002`

Prefix matching, partial matching, or environment inference does not satisfy an exact-world
requirement.
