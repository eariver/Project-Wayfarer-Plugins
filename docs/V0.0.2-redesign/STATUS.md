# V0.0.2 Redesign Status

Updated: 2026-08-06 JST  
Branch: `redesign/V0.0.2-swe1-3`  
Draft PR: `#18`  
Single continuation entry point: [`CONTINUATION.md`](CONTINUATION.md)  
Baseline: Plugin V0.0.1 on `main`  
Current process: `SWE.1 Software Requirements Analysis`  
Current activity: clause-by-clause joint Owner review  
Current gate: `SWE1_OWNER_REVIEW_IN_PROGRESS`

## Executive status

```text
V0.0.1 BASELINE:
  ASSUMED ACCEPTED FOR INITIAL V0.0.2 ANALYSIS

PR #14 / LEGACY IMPLEMENTATION:
  FROZEN REFERENCE / NOT A REQUIREMENT OR DESIGN AUTHORITY

CANONICAL SOURCE MERGE:
  COMPLETE AS REVISION A

INITIAL SWE.1 DECOMPOSITION:
  COMPLETE AS DRAFT

JOINT OWNER REVIEW:
  IN PROGRESS / CAN-COM-001 THROUGH CAN-COM-005 REVIEWED

OWNER-APPROVED CORRECTIONS:
  RECORDED IN DEC-REQ-002

SESSION CONTINUITY AND CONSOLIDATION POLICY:
  APPROVED IN DEC-REQ-003

PROVISIONAL SWE.1 REQUIREMENT COUNT:
  169 AFTER APPROVED ATOMIC DECOMPOSITION THROUGH CAN-COM-005

OPEN ISSUES FROM INITIAL ANALYSIS:
  9 / SUBJECT TO CONTINUED REVIEW

INITIAL SELF-REVIEW SNAPSHOT:
  SUPERSEDED AS CURRENT EVIDENCE BY OWNER-REVIEW CORRECTIONS

G1 REQUIREMENTS BASELINE:
  NOT APPROVED

SWE.2 THROUGH SWE.6:
  NOT AUTHORIZED

PRODUCT IMPLEMENTATION / CANDIDATE / MERGE / TAG / RELEASE:
  NOT AUTHORIZED
```

## Session-resumption rule

A new chat session or recovered context shall begin from [`CONTINUATION.md`](CONTINUATION.md) on the latest PR #18 head and follow its ordered references. Conversation history is supplementary only.

The continuation document must be updated at every Owner-directed repository checkpoint together with this status document.

## Current controlling review records

- `CONTINUATION.md`
  - single living entry point for session resumption;
  - ordered references, current next action, active overlays, and stop rules.
- `08-decisions/DEC-REQ-002-common-requirement-review-corrections.md`
  - Owner-approved correction directions for `CAN-COM-001` through `CAN-COM-005`;
  - overrides conflicting text in the Revision A canonical source and affected draft SWE.1 documents until consolidated revisions are issued.
- `08-decisions/DEC-REQ-003-swe1-review-consolidation-and-session-continuity.md`
  - Owner-approved consolidation cadence and session-continuity policy;
  - defines the single-entry navigation chain and integrated checkpoint package.
- `10-reviews-and-evidence/REV-SWE1-002-joint-owner-review-log.md`
  - joint review progress and rationale;
  - next clause for review is `CAN-COM-006`.

## Approved corrections through CAN-COM-005

### CAN-COM-001

- Separate capability ownership from physical-server placement.
- Do not gate plugin operation solely on a fixed server name or backend role.
- Treat artifact placement and capability composition as integration/deployment configuration.
- Preserve future server consolidation, division, and capability co-location.

### CAN-COM-002

- Generalize shared access from Core-only to public contracts of approved shared-owner software units.
- Prohibit feature-plugin dependency on another feature plugin's internal implementation.
- Preserve shared-to-feature layering and an acyclic dependency graph.

### CAN-COM-003

- Separate authority, ownership, and access mechanism.
- Split the former single authority item into MariaDB durable authority, Redis coordination, external player-state authority, Waymark provider authority, and Minecraft runtime-state authority.
- Permit explicit logical/physical dual-state ownership with feature-owned reconciliation.

### CAN-COM-004

- Prohibit unsupported access to external private/internal state while allowing supported public contracts and approved adapters.
- Prohibit Wayfarer from becoming a general or long-term normal inventory/profile store.
- Preserve the possibility of a future explicitly approved transactional cross-context item-transfer capability using temporary persistence and controlled transformation/redelivery.
- Generalize shared-foundation semantic neutrality instead of enumerating present features.

### CAN-COM-005

- Use platform-authorized execution contexts rather than assuming one global main thread.
- Prohibit blocking external I/O on tick-critical or region-critical execution contexts.
- Add an atomic requirement for asynchronous-completion revalidation.

## SWE.1 repository-checkpoint cadence

During clause-by-clause review, the Owner will instruct repository reflection at either:

- transition between logical CAN sections; or
- completion of five newly approved CAN clauses since the previous repository checkpoint.

The Owner's explicit instruction controls the write. Reaching a boundary does not independently authorize mutation.

At each instructed checkpoint, update at minimum:

1. joint review log;
2. approved correction decision record;
3. `STATUS.md`;
4. `CONTINUATION.md`.

When an integrated consolidation is performed, also update the canonical source, affected SWE.1 requirement documents, document index, traceability, verification intent, issue records where affected, and requirement counts.

## Required consolidation work before G1

1. Revise `SWE1-SRC-002` to incorporate every approved joint-review correction.
2. Revise affected target requirement documents, beginning with `SWE1-COMMON-001` and `SWE1-CORE-001`.
3. Update `SWE1-INDEX-001`, `TRC-SWE1-001`, and `SWE1-VERIFY-001` after item additions or changes.
4. Review and remove equivalent fixed-server-role assumptions in later Main and Frontier clauses.
5. Continue joint review from `CAN-COM-006`.
6. After all corrections are consolidated, rerun automated identifier/source checks and a complete SWE.1 self-review.
7. Present the corrected package for explicit G1 Owner approval.

## Authority restriction

No document in the current branch authorizes SWE.2, SWE.3 construction, SWE.4 through SWE.6 execution, product implementation, Candidate remediation, PR readiness transition, merge, tag, deployment, or release. Only the Owner may approve the next gate.
