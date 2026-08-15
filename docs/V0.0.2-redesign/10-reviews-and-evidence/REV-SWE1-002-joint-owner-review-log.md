# SWE.1 Joint Owner Review Log

Document ID: `REV-SWE1-002`  
Revision: D  
State: `IN_REVIEW`  
Date: 2026-08-15 JST  
Reviewers: Project Owner and ChatGPT  
Reviewed source: `SWE1-SRC-002` Revision D  
Reviewed derived documents: current draft SWE.1 package on `redesign/V0.0.2-swe1-3`

## 1. Purpose

Track the clause-by-clause joint review of the canonical requirement source and its derived SWE.1 requirements. An item is not a G1 approval merely because its correction direction is approved here. All approved corrections must be consolidated, traced, and self-reviewed before the requirements baseline can be approved.

## 2. Review progress

| Canonical clause | Review disposition | Controlling correction |
|---|---|---|
| `CAN-COM-001` | Correction direction approved | `DEC-REQ-002` §2 |
| `CAN-COM-002` | Correction direction approved | `DEC-REQ-002` §3 |
| `CAN-COM-003` | Correction direction approved | `DEC-REQ-002` §4 |
| `CAN-COM-004` | Correction direction approved with Owner refinement | `DEC-REQ-002` §5 |
| `CAN-COM-005` | Correction direction approved | `DEC-REQ-002` §6 |
| `CAN-COM-006` | Correction direction approved | `DEC-REQ-004` §2 |
| `CAN-COM-007` | Correction direction approved | `DEC-REQ-004` §3 |
| `CAN-COM-008` | Correction direction approved | `DEC-REQ-004` §4 |
| `CAN-COM-009` | Correction direction approved | `DEC-REQ-004` §5 |
| `CAN-COM-010` | Correction direction approved after Owner refinement to retain a SWE.1 reuse/ownership constraint | `DEC-REQ-004` §6 |
| `CAN-CORE-001` | Correction direction approved with Owner refinement: preserve accepted contract, not V0.0.1 implementation | `DEC-REQ-005` §2 |
| `CAN-CORE-002` | Correction direction approved | `DEC-REQ-005` §3 |
| `CAN-CORE-003` | Correction direction approved | `DEC-REQ-005` §4 |
| `CAN-CORE-004` | Correction direction approved; duplicate Core UNKNOWN requirement superseded | `DEC-REQ-005` §5 |
| `CAN-CORE-005` | Correction direction approved | `DEC-REQ-005` §6 |
| `CAN-MAIN-001` | Correction direction approved | `DEC-REQ-006` §2 |
| `CAN-MAIN-002` | Correction direction approved; later Owner refinement moves current durability authority to Minecraft physical item state | `DEC-REQ-006` §3 |
| `CAN-MAIN-003` | Correction direction approved | `DEC-REQ-006` §4 |
| `CAN-MAIN-004` | Correction direction approved with Owner refinement limiting world-drop prohibition to fallback delivery | `DEC-REQ-006` §5 |
| `CAN-MAIN-005` | Correction direction approved with Owner refinements for possession/storage neutrality and controlled processing | `DEC-REQ-006` §6 |
| `CAN-MAIN-006` onward | Not yet jointly reviewed | Later propagation from `DEC-REQ-006` applies where recorded |

The Common and Core canonical sections and Main `CAN-MAIN-001` through `CAN-MAIN-005` are reviewed and integrated. The next substantive review item is `CAN-MAIN-006 — Death behavior`.

## 3. Key Owner determinations

### 3.1 Deployment topology is not a plugin runtime-role gate

Current placement of a plugin on a named server is an integration constraint. Plugin functionality must not be disabled solely because a runtime is not identified as Main, Frontier, or Lobby. Future server consolidation and co-location must not be unnecessarily prevented.

### 3.2 Shared capabilities are not permanently forced into Core

Shared feature access uses the public contract of whichever software unit receives approved shared ownership. Feature plugins do not depend on one another's internal implementation, shared-to-feature layering is preserved, and the dependency graph remains acyclic.

### 3.3 Authority and access mechanism are separate

MariaDB durable state, Redis coordination, MVI normal player state, Waymark provider state, and Minecraft runtime physical state have distinct authority/access roles. A logical Wayfarer authority may coexist with Minecraft-owned physical state when the owning feature defines reconciliation.

### 3.4 Normal player state is not a Wayfarer general storage service

Wayfarer does not become the general or long-term inventory/profile authority. A future explicitly approved cross-context transfer may temporarily persist and transform approved item state as part of a controlled transaction; this possibility is not prohibited but is not V0.0.2 scope.

### 3.5 Shared foundation requirements are generalized

Shared-foundation semantic neutrality is expressed as a general allocation rule rather than by enumerating current gameplay features.

### 3.6 Threading follows platform-authorized execution contexts

Requirements use execution-context guarantees of the adopted server platform rather than assuming one global main thread. Blocking I/O remains prohibited on tick-critical/region-critical contexts, and deferred completion requires mutable-precondition revalidation.

### 3.7 Lifecycle fail-closed is capability-scoped and result-oriented

A capability is available only while its assigned mandatory prerequisites remain valid. Failure closes admission before new capability effects; whole-plugin unavailability is required only where the prerequisite applies to all plugin runtime capabilities or a target requirement explicitly requires it. Accepted work must receive a finite disposition and stale prior-lifecycle completions cannot mutate current/disabled runtime state. Listener/command registration, asynchronous flush, and runtime-generation counters are not fixed by SWE.1.

For Worlds Beyond, the Owner approved a configurable target-world direction: Wayfarer_Frontier does not generate the world, and missing configured world may make the whole plugin unavailable when all Frontier gameplay depends on it. Exact recovery/re-enable behavior remains open for target review.

### 3.8 Protected operations distinguish logical operations, effects, replay, and ambiguity

Protected financial/compensation/durable-entitlement-delivery/authority operations establish recoverable identity before first protected effect. Replay resolves existing operation/effect state and does not create duplicate protected effects. `UNKNOWN` is neither success nor clear failure and does not authorize automatic retry, compensation, benefit completion, entitlement creation, or authority rotation. Compensation is permitted only after proven debit success plus proven downstream no-benefit/clear failure and has its own effect identity.

### 3.9 Migration ownership is durable-domain ownership, not table-prefix architecture

Each durable-state domain has one software owner and owner-attributable migration sequence/applied state. Table prefixes, resource paths, migration executors, and physical history stores are downstream choices unless an accepted baseline fixes them. Accepted/supported migration history is immutable and later evolution uses new forward migrations. Empty installation and V0.0.1 upgrade must not require destructive automatic reset.

### 3.10 Auditability and data minimization are independent obligations

Designated protected outcomes retain retrievable evidence sufficient for operation/event correlation, applicable actor/subject, time, disposition, and reason/classification; protected operations correlate with logical/effect identity and supported inspection/reconciliation evidence survives normal restart. Audit evidence does not prove a stronger effect than the authoritative source. Secrets/credentials/tokens are not recorded and unrelated personal/runtime state is minimized.

### 3.11 Reuse-first remains in the SWE.1 trace chain

The Owner explicitly requested that reuse-first not disappear in SWE.2+. SWE.1 therefore retains a software-level capability ownership non-duplication constraint: Project-owned generic capability duplication requires an identified unmet requirement/constraint or material risk reduction. Concrete dependency/API/version/adapter/reference selection remains governed by `GOV-ENG-001` in SWE.2/SWE.3.

During Common integration, the conversational placeholder `SWE1-COMMON-001-CON-009` for this new requirement was found to collide with the already approved Redis coordination requirement from `DEC-REQ-002`. The integrated identifier is `SWE1-COMMON-001-CON-010`; no approved identifier was silently reused or renumbered.

### 3.12 V0.0.1 compatibility preserves contracts, not implementation

The Owner confirmed that V0.0.1 was not requirements-analyzed or debugged to the same rigor as V0.0.2. V0.0.1 implementation locations are therefore not frozen. The protected baseline is the controlled accepted public contract: source use, binary linkage, and documented externally observable semantics. Internal structure, dependencies, implementation defects, validation/error handling, lifecycle/threading mechanics, and undocumented behavior may be corrected or redesigned while accepted contract compatibility is preserved.

A breaking exception must be explicitly scoped; it does not silently waive the remainder of the accepted baseline. The complete controlled V0.0.1 public-contract/migration inventory remains required before G1.

### 3.13 Core public API is implementation-isolated but not platform-type absolutist

Core public contracts do not expose implementation-specific types or raw internal resource/authority handles merely for convenience. Present library names such as HikariCP/Flyway/Lettuce are not frozen into the SWE.1 prohibition. Platform/external contract types may be used when the approved capability genuinely requires them and compatibility/lifecycle/ownership/execution-context implications are acceptable.

The runtime requirement is compatible contract type identity within one in-process compatibility domain, not one prescribed JAR/class-loader mechanism. Intentionally isolated versions remain possible behind an approved compatibility/adapter boundary.

### 3.14 V0.0.2 shared Waymark transaction contract is allocated to Core without transferring feature semantics

V0.0.2 Core provides the compatibility-preserving shared Waymark transaction contract, consistent with the accepted V0.0.1 `WayfarerServices.transactions()` / `WayfarerTransactions` public surface. This is a V0.0.2 allocation, not a permanent future implementation lock.

Core owns shared provider interaction, operation/effect correlation, financial-effect coordination, inspection, and reconciliation mechanisms. Repair/reissue/shop eligibility, feature-domain mutation, item entitlement/final delivery, and other feature policy remain with the applicable feature owner.

Waymark balance authority remains the economy provider. Wayfarer transaction records own Wayfarer's operation/effect disposition but do not replace provider balance authority or independently prove feature-domain effects. Provider evidence is interpreted only to the strength supplied by the supported provider contract.

### 3.15 Ambiguous provider effects reuse Common UNKNOWN/replay semantics

Core does not maintain a duplicate `UNKNOWN` retry rule. Historical `SWE1-CORE-001-CON-004` is superseded by deduplication because Common `QLT-006`, Common `QLT-012`, and Core `QLT-001` already impose the required behavior.

Balance/aggregate provider state may be used for eligibility, diagnostics, or reconciliation context but not as uncorrelated proof of one exact provider effect. Unsupported provider internals or unilateral side-channel markers cannot manufacture stronger semantics. Wayfarer-owned transaction/audit/reconciliation records remain valid but do not strengthen provider authority.

### 3.16 Core migrations evolve Core-owned state while accepted migration artifacts remain immutable

Core migration justification covers approved changes, corrections, integrity requirements, compatibility requirements, and evolution of existing or new Core-owned durable state; it is not limited to a new capability. Core migrations do not become a persistence container for another owner's domain.

Controlled accepted V0.0.1 Core migration artifacts are different from ordinary V0.0.1 implementation code: migration identity, ordering semantics, and byte-for-byte artifact content remain immutable. Later corrections use a new migration identity. Future migration framework/resource/executor choices remain open provided accepted history and the V0.0.1 upgrade path stay compatible.

### 3.17 Main deployment and lifecycle are capability-scoped

`CAN-MAIN-001` no longer makes a historical Main backend name or Wayfarer_Core plugin presence a universal Main prerequisite. Approved deployment allocation and actual capability prerequisites control availability. V0.0.2 Main financial capabilities depend on the Core-provided shared transaction contract, while unrelated Main capabilities do not inherit that dependency merely because Core currently owns the financial boundary.

### 3.18 Logical Growth Tool authority is separated from physical Minecraft state

Wayfarer_Main owns logical Growth Tool semantics and MariaDB is the durable authority for that logical state. Lifecycle, delivery, and branch are independent state dimensions. Current physical durability/damage is instead authoritative Minecraft item state and is not duplicated as an authoritative current-damage field in Main's logical database record.

The Owner approved two new atomic state requirements: `SWE1-MAIN-001-CAP-010` for delivery state and `CAP-011` for branch state.

### 3.19 Physical item identity is a reference to logical authority, not authority itself

Managed physical representations carry persistent machine-readable identity sufficient to resolve logical tool, current physical issuance, and epoch. SWE.1 does not fix an exact PDC key/layout. Presentation attributes including material/name/lore/enchantments/display revision are not independent authority. Current operations resolve physical identity against logical authority and fail closed on unsupported, malformed, mismatched, or stale identity.

### 3.20 Initial entitlement and physical delivery are distinct effects

Initial logical entitlement resolution is nonblocking on prohibited runtime contexts and race/replay safe. Physical delivery revalidates current mutable prerequisites before inventory mutation. Inventory-full or equivalent safe-delivery failure retains the same durable delivery entitlement; system-generated world drop cannot be used as fallback delivery. This prohibition does not extend to ordinary post-delivery user/entity drop behavior. Pending retry continues the same entitlement without debit, authority rotation, or duplicate delivery.

### 3.21 Owner binding controls use, not ordinary possession/storage

The Owner explicitly narrowed Growth Tool binding to logical use authority. Owner and non-owner physical possession, ordinary pickup/drop, chest/Ender Chest/Shulker Box storage, and equivalent Minecraft transfer are not prohibited merely because the item is managed and do not transfer logical ownership.

Only the logical owner may use/progress the Growth Tool. Durability restoration and enchantment-state modification—including addition, increase, removal, reduction, transfer, and replacement—are Wayfarer-controlled. V0.0.2 additionally prohibits processing a managed Growth Tool/Broken Tool through an anvil or grindstone, including anvil rename. Crafting/Mending/smithing/external routes may not bypass the controlled repair/evolution semantics.

This determination creates mandatory later propagation: CAN-MAIN-006 death-drop suppression conflicts with ordinary drop neutrality, and CAN-MAIN-016 must use authority rotation rather than global physical-absence proof while keeping reissue price strictly above applicable repair rather than fixing the current exact formula.

## 4. Common-section checkpoint integration

The Owner explicitly instructed repository checkpoint reflection after approval of `CAN-COM-010`. This simultaneously satisfied five newly approved clauses since the preceding checkpoint and the Common → Core logical section transition.

That checkpoint integrated the canonical Common section, Common requirements, directly affected target requirements, issue state, index, traceability, verification-intent allocation, review/status/continuation records, and source register. `DEC-REQ-002` and `DEC-REQ-004` remain immutable rationale after integration.

## 5. Core-section checkpoint integration

The Owner explicitly approved `CAN-CORE-001` through `CAN-CORE-005` and instructed repository checkpoint reflection after `CAN-CORE-005`. This simultaneously satisfied five newly approved clauses since the Common checkpoint and the Core → Main logical section transition.

`DEC-REQ-005` records the approved rationale. The Core checkpoint integrated Canonical Revision C, `SWE1-CORE-001` Revision C, index, traceability, verification intent, review log, source/status/continuation records, and the requirement count.

## 6. Main CAN-MAIN-001–005 checkpoint integration

The Owner explicitly approved correction directions for `CAN-MAIN-001` through `CAN-MAIN-005` and instructed repository checkpoint reflection after the fifth newly approved Main clause.

`DEC-REQ-006` records the approved rationale and the required later propagation discovered during this review. This checkpoint integrates Canonical Revision D through CAN-MAIN-005, `SWE1-MAIN-001` Revision C, index, traceability, verification intent, this review log, source/status/continuation records, and the requirement count.

The checkpoint does not approve CAN-MAIN-006 or CAN-MAIN-016. Their current conflicting wording is retained as an explicit owning-clause conflict so the next review cannot accidentally treat it as already approved.

## 7. Package impact

The initial 164-requirement self-review snapshot is historical. The integrated Common checkpoint produced 176 requirements; Core review superseded one redundant Core `UNKNOWN` requirement, producing 175. Main review through `CAN-MAIN-005` adds four atomic requirements and produces a provisional integrated total of **179 active Product requirements**:

```text
CAP: 66
CON: 60
IFC: 14
QLT: 39
TOTAL ACTIVE: 179
```

The added Main identifiers are `SWE1-MAIN-001-CAP-010`, `CAP-011`, `CON-010`, and `CON-011`. `SWE1-MAIN-001` now contains 24 items: CAP 11, CON 11, QLT 2. Historical `SWE1-CORE-001-CON-004` remains superseded and not reused.

The complete post-review automated identifier/source/count audit and full SWE.1 self-review remain required before G1. Checkpoint integration does not claim those activities were executed.

## 8. Checkpoint cadence after Main-001–005 integration

The Main checkpoint resets the cadence counter:

```text
NEWLY APPROVED CLAUSES SINCE CHECKPOINT:
  0 / 5

CURRENT SECTION:
  MAIN

NEXT REVIEW ITEM:
  CAN-MAIN-006 — Death behavior
```

A later logical section transition or five newly approved clauses creates a checkpoint candidate; repository mutation still requires explicit Owner instruction.

## 9. Gate state

```text
JOINT REVIEW:
  COMMON SECTION COMPLETE
  CORE SECTION COMPLETE
  MAIN CAN-MAIN-001–005 INTEGRATED
  MAIN REVIEW CONTINUES AT CAN-MAIN-006

COMMON/CORE/REVIEWED-MAIN CORRECTION DIRECTIONS:
  OWNER APPROVED AND INTEGRATED

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 AND LATER:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / VERIFICATION EXECUTION / PR READY / MERGE / TAG / DEPLOY / RELEASE:
  NOT AUTHORIZED
```
