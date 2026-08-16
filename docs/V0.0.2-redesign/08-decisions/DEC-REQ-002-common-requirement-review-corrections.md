# Common Requirement Review Corrections

Document ID: `DEC-REQ-002`  
Revision: A  
State: `APPROVED`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Approver: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Affected source: `SWE1-SRC-002` Revision A  
Affected SWE.1 documents: `SWE1-COMMON-001` Revision A; `SWE1-CORE-001` Revision A

## 1. Purpose

Record the Owner-approved corrections resulting from joint review of `CAN-COM-001` through
`CAN-COM-005`. Until the canonical source and affected SWE.1 documents are consolidated into their
next revisions, this decision overrides the conflicting text identified below.

This decision does not approve G1, SWE.2, SWE.3, implementation, verification execution, merge, tag,
or release.

## 2. CAN-COM-001 — capability ownership and deployment-topology independence

### 2.1 Approved correction

Core, Main, and Frontier remain distinct capability-ownership areas, but plugin operation must not be
conditioned on a fixed physical server identity, server name, or Project backend role merely because
the current integration topology places a plugin on one server.

Artifact placement and capability composition are integration/deployment decisions. A plugin may deny
an affected capability when the capability's actual functional prerequisites are absent, including
required dependencies, configuration, approved external capabilities, or required world/context
conditions. A plugin must not deny operation solely because the current runtime does not identify
itself as a historically named Main, Frontier, or Lobby server.

The software shall not unnecessarily prevent a future topology in which multiple Wayfarer feature
capabilities coexist on one server or current servers are consolidated or divided.

### 2.2 Replacement SWE.1 obligations

`SWE1-COMMON-001-CON-001` is revised in substance to **Deployment-topology independence**:

> Wayfarer-owned software shall separate shared and feature capability ownership without requiring a
> fixed physical server identity, server name, or Project backend role as a general prerequisite for
> operation. Artifact placement and capability composition shall be controlled by the approved
> integration configuration. Each plugin shall activate only capabilities whose actual functional
> prerequisites are satisfied.

`SWE1-CORE-001-CAP-001` is revised in substance to **Shared service availability independent of
backend naming**:

> A software unit assigned shared-service ownership shall provide its approved services to any runtime
> containing an approved dependent Wayfarer capability, without assuming ownership of that dependent
> capability's feature-specific gameplay semantics.

### 2.3 Deferred propagation

Equivalent fixed-backend restrictions in `CAN-MAIN-001`, `CAN-FRONTIER-001`, and their derived SWE.1
items require review and correction when those clauses are reached.

## 3. CAN-COM-002 — dependency direction

### 3.1 Approved correction

The dependency model is generalized so that shared functionality is not permanently forced into
Wayfarer_Core. Feature plugins shall consume Wayfarer-owned shared capabilities only through public
contracts of software units explicitly assigned shared ownership. A feature plugin shall not depend
on another feature plugin's internal implementation.

A software unit assigned shared-foundation ownership shall not acquire a dependency on a
feature-specific software unit that reverses the approved architectural layering. The Wayfarer module
dependency graph shall remain acyclic.

### 3.2 Replacement SWE.1 obligation

`SWE1-COMMON-001-CON-002` is revised in substance to **Approved shared-contract dependency**:

> Wayfarer feature plugins shall obtain Wayfarer-owned shared capabilities only through the public
> contracts of software units explicitly assigned shared ownership and shall not depend on another
> feature plugin's internal implementation.

`SWE1-COMMON-001-CON-007` and `SWE1-COMMON-001-CON-008` remain applicable in principle, subject to
wording that preserves an acyclic dependency graph and prohibits reversed shared-to-feature layering
without treating present server placement as a software prerequisite.

## 4. CAN-COM-003 — state ownership and access authority

### 4.1 Approved correction

Authority, ownership, and access mechanism shall be distinguished.

1. Wayfarer-owned durable business state uses MariaDB as its durable authority unless a later approved
   requirement assigns a different authority.
2. Redis may provide cache, lock, coordination, pub/sub, and messaging assistance, but shall not be
   the sole durable authority for Wayfarer-owned business state. Recoverable cached state shall be
   reconstructable from its authority.
3. MVI remains the authority for the normal player-state domains assigned to it. Wayfarer shall not
   silently substitute itself for that authority.
4. Waymark balance authority remains the approved economy provider. Wayfarer feature capabilities
   shall use an approved shared transaction contract rather than direct provider-internal access.
5. Minecraft Server owns runtime world, block, entity, inventory-item, and related physical state.
   Wayfarer accesses or mutates that state through approved platform contracts.
6. A feature may legitimately combine a Wayfarer-owned logical authority with Minecraft-owned
   physical state. Such dual-state features shall define mismatch detection and reconciliation in the
   owning feature requirements and design.

### 4.2 Required SWE.1 decomposition

The single former `SWE1-COMMON-001-IFC-001` obligation shall be decomposed into atomic obligations:

- `SWE1-COMMON-001-IFC-001` — Wayfarer durable-state authority;
- `SWE1-COMMON-001-CON-009` — Redis non-authoritative coordination;
- `SWE1-COMMON-001-IFC-003` — external player-state authority;
- `SWE1-COMMON-001-IFC-004` — Waymark provider authority and approved access boundary;
- `SWE1-COMMON-001-IFC-005` — Minecraft runtime-state authority and approved platform access.

Detailed authority pairing for Launchpad and comparable features remains owned by the applicable target
requirement documents.

## 5. CAN-COM-004 — prohibited ownership and access

### 5.1 External private-state access

Wayfarer-owned software shall not read or mutate another product's private database, undocumented
storage, internal cache, or implementation-specific state as an integration contract. Supported public
APIs, events, provider contracts, platform-visible state, and explicitly approved adapters may be used
within their documented guarantee boundaries.

`SWE1-COMMON-001-CON-003` shall be revised accordingly so it does not prohibit legitimate supported
integration access.

### 5.2 Normal player-state authority non-substitution

Wayfarer-owned software shall not act as the general or long-term authority for normal player
inventory or profile state.

An explicitly approved cross-context transfer capability may temporarily capture, persist, transform,
and redeliver item state through a controlled transaction, provided that it:

- does not replace the authoritative inventory/profile system;
- does not become a general inventory-storage service;
- defines the permitted source and destination contexts and item classes;
- controls removal, delivery, replay, duplication, loss, expiration, compensation, audit, and
  reconciliation;
- receives its own approved scope, requirements, and downstream design before implementation.

This exception preserves the possibility of future WM- or other cost-based item transfer between
content contexts. It does not add such a transfer capability to V0.0.2 scope.

`SWE1-COMMON-001-CON-004` shall be revised to express this non-substitution rule rather than an
absolute prohibition on cross-context transfer.

### 5.3 Non-public API exception control

Unsupported or non-public APIs shall not be adopted as a product dependency unless an explicit
Owner-approved exception identifies the exact version, necessity, isolation boundary, failure
behavior, and limitation. An approved exception shall be contained behind a replaceable adapter and
shall fail closed outside the approved compatibility range.

`SWE1-COMMON-001-CON-005` shall be revised accordingly.

### 5.4 Shared-foundation semantic neutrality

The present feature-name enumeration in `SWE1-CORE-001-CON-006` is over-specific and shall be replaced
by the following generalized rule:

> A software unit assigned shared-foundation ownership shall not own policy or gameplay semantics that
> apply only to a specific feature domain unless an approved architecture allocation explicitly
> assigns that responsibility to the shared unit. Shared units may provide reusable mechanisms and
> contracts without embedding feature-specific decisions.

## 6. CAN-COM-005 — threading boundary

### 6.1 Approved correction

The requirement shall not assume that every permitted runtime operation occurs on one global main
thread. It shall follow the authoritative execution-context contract of the adopted server platform,
including any future region-thread model.

Blocking or completion-waiting database, Redis, filesystem, network, or durable-audit I/O shall not
execute on tick-critical or region-critical server execution contexts. Enqueuing bounded in-memory
work is not treated as blocking durable I/O.

After asynchronous work completes, the software shall return to an execution context authorized for
the affected Minecraft runtime state and revalidate mutable preconditions before applying the result.
Applicable revalidation includes runtime generation, subject identity, online state, location or
content context, item/authority identity, and any other mutable precondition required by the operation.

### 6.2 Required SWE.1 decomposition

- `SWE1-COMMON-001-QLT-001` — platform-authorized runtime-state access;
- `SWE1-COMMON-001-QLT-002` — no blocking I/O on tick-critical or region-critical execution contexts;
- `SWE1-COMMON-001-QLT-009` — asynchronous completion revalidation.

## 7. Requirement-count effect

The approved atomic decomposition adds five draft SWE.1 items relative to the previous 164-item
snapshot:

- four additional items for `CAN-COM-003` decomposition;
- one additional item for asynchronous completion revalidation under `CAN-COM-005`.

The provisional draft total is therefore 169 requirements. This count remains subject to further
joint review corrections and final automated recount.

## 8. Consolidation obligations

Before G1 review completion, ChatGPT shall:

1. issue revised canonical source and affected SWE.1 documents incorporating this decision directly;
2. update the phase index and source-to-requirement traceability;
3. update verification-intent allocation for all newly introduced items;
4. remove stale fixed-server-role wording from affected later clauses after their review;
5. rerun the complete SWE.1 self-review and automated identifier/source checks;
6. retain this decision as the immutable Owner-review rationale.
