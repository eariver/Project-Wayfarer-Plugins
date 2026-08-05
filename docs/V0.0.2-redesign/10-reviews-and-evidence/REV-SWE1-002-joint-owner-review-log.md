# SWE.1 Joint Owner Review Log

Document ID: `REV-SWE1-002`  
Revision: A  
State: `IN_REVIEW`  
Date: 2026-08-05 JST  
Reviewers: Project Owner and ChatGPT  
Reviewed source: `SWE1-SRC-002` Revision A  
Reviewed derived documents: current draft SWE.1 package on `redesign/V0.0.2-swe1-3`

## 1. Purpose

Track the clause-by-clause joint review of the canonical requirement source and its derived SWE.1
requirements. An item is not a G1 approval merely because its correction direction is approved here.
All approved corrections must be consolidated, traced, and self-reviewed before the requirements
baseline can be approved.

## 2. Review progress

| Canonical clause | Review disposition | Controlling correction |
|---|---|---|
| `CAN-COM-001` | Correction direction approved | `DEC-REQ-002` §2 |
| `CAN-COM-002` | Correction direction approved | `DEC-REQ-002` §3 |
| `CAN-COM-003` | Correction direction approved | `DEC-REQ-002` §4 |
| `CAN-COM-004` | Correction direction approved with Owner refinement | `DEC-REQ-002` §5 |
| `CAN-COM-005` | Correction direction approved | `DEC-REQ-002` §6 |
| `CAN-COM-006` onward | Not yet jointly reviewed | None |

## 3. Key Owner determinations

### 3.1 Deployment topology is not a plugin runtime-role gate

Current placement of a plugin on a named server is an integration constraint. Plugin functionality
must not be disabled solely because a runtime is not identified as Main, Frontier, or Lobby. Future
server consolidation and co-location must not be unnecessarily prevented.

### 3.2 Shared capabilities are not permanently forced into Core

Shared feature access uses the public contract of whichever software unit receives approved shared
ownership. Feature plugins do not depend on one another's internal implementation, and the dependency
graph remains acyclic.

### 3.3 Authority and access mechanism are separate

MariaDB, Redis, MVI, the economy provider, and Minecraft runtime state have distinct authority and
access roles. A logical Wayfarer authority may coexist with Minecraft-owned physical state when the
owning feature defines reconciliation.

### 3.4 Normal player state is not a Wayfarer general storage service

Wayfarer does not become the general or long-term inventory/profile authority. A future explicitly
approved cross-context transfer may temporarily persist and transform item state as part of a
controlled transaction. This possibility must not be prohibited by the common requirement, but it is
not current V0.0.2 scope.

### 3.5 Shared foundation requirements must be generalized

Shared-foundation semantic neutrality is expressed as a general allocation rule rather than by listing
current Growth Tool, Worlds Beyond, Launchpad, GUI, or command features.

### 3.6 Threading follows platform-authorized execution contexts

Requirements use the execution-context guarantees of the adopted server platform rather than assuming
one global main thread. Blocking I/O remains prohibited on tick-critical or region-critical contexts,
and asynchronous completion requires mutable-precondition revalidation.

## 4. Package impact

The prior 164-requirement self-review snapshot is no longer current. The approved decomposition through
`CAN-COM-005` creates a provisional total of 169 draft requirements. A complete recount and self-review
are required after all joint-review corrections are consolidated.

## 5. Gate state

```text
JOINT REVIEW:
  IN PROGRESS THROUGH CAN-COM-005

CORRECTION DIRECTIONS:
  OWNER APPROVED

CONSOLIDATED REVISIONS:
  REQUIRED BEFORE G1

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 AND LATER:
  NOT AUTHORIZED
```
