# Document Control and Lifecycle Gates

Document ID: `V002-GOV-COM-001`  
Revision: B  
State: `PREPARED_FOR_OWNER_CONFIRMATION`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Applicable Product: Plugin V0.0.2 redesign

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

## 4. Domain split and identifier model

The authoritative model is:

`docs/V0.0.2-redesign/00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md`

Requirements and design work products are divided by a single dominant functional or cross-cutting
domain. Each controlled document uses:

```text
V002-<PROCESS>-<DOMAIN>-<DOCUMENT_NUMBER>
```

Every normative item concatenates its owning document identifier with its item type and local number:

```text
<DOCUMENT_ID>-<ITEM_TYPE>-<ITEM_NUMBER>
```

Examples:

```text
V002-SWE1-MAIN-001-REQ-003
V002-SWE2-MAIN-002-ARC-005
V002-SWE3-MAIN-004-DD-017
V002-SWE4-MAIN-001-UV-012
```

Full identifiers are mandatory in traceability, work orders, implementation consistency records,
verification reports, and reviews. A local shorthand such as `REQ-003` is insufficient outside its
owning document.

Each normative item has one owning document. Cross-domain items are owned by the applicable common,
integration, data, security, quality, or operations document and referenced from product-domain
documents instead of duplicated.

Identifiers are never reused after first formal review. Superseded and rejected items retain their
identifier and disposition.

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
an explicit justified `NOT_APPLICABLE` disposition. All trace links use complete linked identifiers.

## 6. Lifecycle gates

### G0 — Redesign governance gate

Entry:

- redesign branch exists from the V0.0.1 baseline;
- charter, roles, document-control rules, domain split, and identifier rules are prepared;
- old V0.0.2 Product work is frozen as reference.

Exit:

- Owner confirms the process and role allocation;
- Owner confirms the domain document and linked identifier model;
- `STATUS.md` authorizes SWE.1 only.

### G1 — SWE.1 requirements baseline gate

Required exit evidence:

- source register complete for the release scope;
- requirements divided into controlled domain documents;
- software requirements uniquely identified by document-linked requirement IDs and testable;
- phase document index complete;
- scope and non-scope explicit;
- conflicts and open decisions resolved or accepted as blockers;
- verification intent defined;
- requirements traceability complete;
- Owner approves the requirements baseline.

SWE.2 cannot begin before G1 PASS.

### G2 — SWE.2 architecture baseline gate

Required exit evidence:

- architecture divided into controlled domain documents;
- all approved requirements allocated;
- architectural elements use document-linked identifiers;
- interfaces, data ownership, threading, lifecycle, failure containment, and external integration
  boundaries defined;
- architecture verification criteria defined;
- phase document index and requirement-to-architecture traceability complete;
- Owner approves the architecture baseline.

Detailed design cannot begin before G2 PASS.

### G3 — SWE.3 detailed-design baseline gate

Required exit evidence:

- detailed design divided into controlled domain documents;
- all implementation-relevant architecture elements decomposed;
- detailed-design elements use document-linked identifiers;
- behavior, state transitions, interfaces, error semantics, platform/library references, and
  construction constraints defined;
- unit-verification obligations defined;
- phase document index and architecture-to-detailed-design traceability complete;
- Owner approves the detailed-design baseline.

Unit construction cannot begin before G3 PASS.

### G3C — Unit construction and conformance gate

Required exit evidence:

- Codex implements only approved work-order scope;
- implementation/design consistency record maps full detailed-design IDs to linked implementation
  unit IDs and changed production files;
- deviations are absent or separately approved in design;
- build succeeds at the prescribed scope;
- ChatGPT independently reviews implementation conformance.

SWE.4 completion cannot be claimed before G3C PASS.

### G4 — SWE.4 unit-verification gate

Required exit evidence:

- approved linked unit-verification cases executed;
- detailed-design coverage complete;
- failures and exclusions resolved or accepted;
- ChatGPT independently reviews adequacy and results.

SWE.5 cannot begin before G4 PASS for the integrated scope.

### G5 — SWE.5 integration gate

Required exit evidence:

- approved integration sequence completed;
- linked interface and integrated-behavior tests pass;
- V0.0.1 compatibility impact assessed;
- integration traceability complete;
- ChatGPT independently reviews adequacy and results.

SWE.6 cannot begin before G5 PASS.

### G6 — SWE.6 qualification gate

Required exit evidence:

- qualification environment and Product identities fixed;
- all applicable software requirements qualified through linked qualification cases;
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
