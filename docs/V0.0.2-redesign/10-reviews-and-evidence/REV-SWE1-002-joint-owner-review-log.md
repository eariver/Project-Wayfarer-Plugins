# SWE.1 Joint Owner Review Log

Document ID: `REV-SWE1-002`  
Revision: B  
State: `IN_REVIEW`  
Date: 2026-08-11 JST  
Reviewers: Project Owner and ChatGPT  
Reviewed source: `SWE1-SRC-002` Revision B  
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
| `CAN-CORE-001` onward | Not yet jointly reviewed | None |

The Common canonical section is therefore completely reviewed. The next substantive review item is `CAN-CORE-001 — V0.0.1 compatibility`.

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

During integration, the conversational placeholder `SWE1-COMMON-001-CON-009` for this new requirement was found to collide with the already approved Redis coordination requirement from `DEC-REQ-002`. The integrated identifier is `SWE1-COMMON-001-CON-010`; no approved identifier was silently reused or renumbered.

## 4. Common-section checkpoint integration

The Owner explicitly instructed repository checkpoint reflection after approval of `CAN-COM-010`. This simultaneously satisfied:

- five newly approved clauses since the previous repository checkpoint (`CAN-COM-006` through `CAN-COM-010`); and
- the Common → Core logical section transition.

The integrated checkpoint updates the canonical Common section, Common requirements, directly affected Core/Main/Frontier/WB requirements, issue state, index, traceability, verification-intent allocation, this review log, `STATUS.md`, and `CONTINUATION.md`. `DEC-REQ-002` and `DEC-REQ-004` remain immutable rationale/decision records after integration.

The checkpoint does not pre-approve later target clauses. Where an unreviewed target clause still conflicts with the approved Common direction, the target requirement explicitly records that conflict/deferred correction rather than silently deciding the target clause.

## 5. Package impact

The initial 164-requirement self-review snapshot is historical. Owner-approved Common decomposition now produces a provisional integrated total of **176** Product requirements:

```text
CAP: 64
CON: 59
IFC: 14
QLT: 39
TOTAL: 176
```

The Common requirement document now contains 30 items: CON 10, IFC 6, QLT 14.

The complete post-review automated identifier/source/count audit and full SWE.1 self-review remain required before G1. Checkpoint integration does not claim those activities were executed.

## 6. Checkpoint cadence after integration

The Common checkpoint resets the cadence counter:

```text
NEWLY APPROVED CLAUSES SINCE CHECKPOINT:
  0 / 5

CURRENT SECTION:
  CORE

NEXT REVIEW ITEM:
  CAN-CORE-001 — V0.0.1 compatibility
```

A later logical section transition or five newly approved clauses creates a checkpoint candidate; repository mutation still requires explicit Owner instruction.

## 7. Gate state

```text
JOINT REVIEW:
  COMMON SECTION COMPLETE / CORE SECTION NOT STARTED

COMMON CORRECTION DIRECTIONS:
  OWNER APPROVED AND INTEGRATED

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 AND LATER:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / VERIFICATION EXECUTION / PR READY / MERGE / TAG / DEPLOY / RELEASE:
  NOT AUTHORIZED
```
