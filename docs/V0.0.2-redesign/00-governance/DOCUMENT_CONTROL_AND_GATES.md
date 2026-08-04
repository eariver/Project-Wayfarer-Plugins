# Document Control and Lifecycle Gates

Status: `PREPARED_FOR_OWNER_CONFIRMATION`  
Date: 2026-08-05 JST

## 1. Controlled work-product states

Every controlled work product uses one of these states:

```text
DRAFT
IN_REVIEW
APPROVED_BASELINE
SUPERSEDED
REJECTED
REFERENCE_ONLY
```

A document marked `DRAFT` or `IN_REVIEW` may guide analysis but does not authorize downstream
implementation or verification.

## 2. Required document header

Every controlled requirement, design, test specification, work order, review, or evidence summary
must state:

- title;
- document identifier;
- revision;
- state;
- date;
- author or executor role;
- reviewer role;
- applicable Product version;
- predecessor authority or source baseline;
- superseded document, when applicable.

## 3. Immutable references

Approved baselines and handoffs must reference immutable commit SHAs or release tags. Branch names may
be included for convenience but are not sufficient authority.

Raw local evidence may be excluded from Git when it contains binaries, secrets, logs, worlds,
databases, or personal identifiers. A tracked sanitized evidence index must record:

- Product commit and artifact hashes;
- environment identity;
- executed specification revision;
- result summary;
- local evidence location or retention rule;
- limitations and exclusions.

## 4. Identifier model

Planned identifier prefixes:

| Prefix | Meaning |
|---|---|
| `SRC-*` | Requirement source |
| `DEC-*` | Owner or architecture decision |
| `SWR-COM-*` | Common software requirement |
| `SWR-CORE-*` | Wayfarer_Core software requirement |
| `SWR-MAIN-*` | Wayfarer_Main software requirement |
| `SWR-FRONT-*` | Wayfarer_Frontier software requirement |
| `SWR-INT-*` | External integration requirement |
| `SWR-NFR-*` | Cross-cutting non-functional requirement |
| `ARC-*` | Architectural element or decision allocation |
| `DD-*` | Detailed-design unit or behavior |
| `UT-*` | SWE.4 unit-verification case |
| `IT-*` | SWE.5 integration-test case |
| `QT-*` | SWE.6 qualification-test case |
| `WO-*` | Codex work order |
| `REV-*` | Independent review |
| `EVD-*` | Evidence summary |

Identifiers are never reused after approval. Superseded items retain their identifier and point to
the replacement.

## 5. Bidirectional traceability

The required end-to-end chain is:

```text
Source / Owner decision
  -> Software requirement
  -> Architecture allocation
  -> Detailed design
  -> Implementation unit
  -> Unit verification
  -> Integration verification
  -> Qualification verification
  -> Evidence and final status
```

Every downstream item must trace upward, and every approved upstream item must trace downward or carry
an explicit justified `NOT_APPLICABLE` disposition.

## 6. Lifecycle gates

### G0 — Redesign governance gate

Entry:

- redesign branch exists from the V0.0.1 baseline;
- charter, roles, and document-control rules are prepared;
- old V0.0.2 Product work is frozen as reference.

Exit:

- Owner confirms the process and role allocation;
- `STATUS.md` authorizes SWE.1 only.

### G1 — SWE.1 requirements baseline gate

Required exit evidence:

- source register complete for the release scope;
- software requirements uniquely identified and testable;
- scope and non-scope explicit;
- conflicts and open decisions resolved or accepted as blockers;
- verification intent defined;
- requirements traceability complete;
- Owner approves the requirements baseline.

SWE.2 cannot begin before G1 PASS.

### G2 — SWE.2 architecture baseline gate

Required exit evidence:

- all approved requirements allocated;
- interfaces, data ownership, threading, lifecycle, failure containment, and external integration
  boundaries defined;
- architecture verification criteria defined;
- requirement-to-architecture traceability complete;
- Owner approves the architecture baseline.

Detailed design cannot begin before G2 PASS.

### G3 — SWE.3 detailed-design baseline gate

Required exit evidence:

- all implementation-relevant architecture elements decomposed;
- behavior, state transitions, interfaces, error semantics, and construction constraints defined;
- unit-verification obligations defined;
- architecture-to-detailed-design traceability complete;
- Owner approves the detailed-design baseline.

Unit construction cannot begin before G3 PASS.

### G3C — Unit construction and conformance gate

Required exit evidence:

- Codex implements only approved work-order scope;
- implementation/design consistency record is complete;
- deviations are absent or separately approved in design;
- build succeeds at the prescribed scope;
- ChatGPT independently reviews implementation conformance.

SWE.4 completion cannot be claimed before G3C PASS.

### G4 — SWE.4 unit-verification gate

Required exit evidence:

- approved unit-verification cases executed;
- detailed-design coverage complete;
- failures and exclusions resolved or accepted;
- ChatGPT independently reviews adequacy and results.

SWE.5 cannot begin before G4 PASS for the integrated scope.

### G5 — SWE.5 integration gate

Required exit evidence:

- approved integration sequence completed;
- interface and integrated-behavior tests pass;
- V0.0.1 compatibility impact assessed;
- integration traceability complete;
- ChatGPT independently reviews adequacy and results.

SWE.6 cannot begin before G5 PASS.

### G6 — SWE.6 qualification gate

Required exit evidence:

- qualification environment and Product identities fixed;
- all applicable software requirements qualified;
- exclusions justified;
- defects and limitations recorded;
- ChatGPT independently reviews evidence;
- Owner accepts or rejects qualification.

Release readiness remains a separate decision after G6.

## 7. Gate verdicts

Each gate review uses exactly one verdict:

```text
PASS
PASS_WITH_ACCEPTED_LIMITATIONS
REWORK_REQUIRED
BLOCKED_OWNER_DECISION
REJECTED
```

A limitation may not weaken an approved requirement without an explicit requirement change and
upstream impact review.

## 8. Current authorization

Only G0 preparation and SWE.1 analysis are authorized. Product implementation, Candidate creation,
SWE.4 through SWE.6 execution, merge, tag, and release are not authorized.
