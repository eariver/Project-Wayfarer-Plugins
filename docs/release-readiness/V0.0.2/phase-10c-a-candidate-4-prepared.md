# Phase 10C-A Revision B — Candidate-4 Prepared Readiness Record

> **Superseded by pre-client independent review.** Candidate-4 is rejected before Client Test and
> must not be used for a Minecraft Client connection. See
> `phase-10c-a-candidate-4-preclient-independent-review.md` and PR #14 comment `5159668460`.

Current authority:

```text
PHASE 10C-A PRE-CLIENT INDEPENDENT REVIEW: FAIL / HOLD
CANDIDATE-4: REJECTED BEFORE CLIENT TEST
CANDIDATE-5: REQUIRED
CLIENT TEST: DO NOT START
FULL CLIENT ACCEPTANCE: NOT COMPLETE
PRODUCTION BALANCE PROMOTION: HOLD
PROJECT ACCEPTANCE: PENDING
STABLE PUBLICATION: NOT AUTHORIZED
```

The remainder of this file is preserved as the historical Candidate-4 preparation record. It is not
a current authorization to proceed.

---

This is a prepared/intermediate record. It is not a Focused Gate PASS,
Project Acceptance, Full Client Acceptance, production promotion, or stable
publication authorization.

## Historical prepared state

```text
PHASE 10C-A EXECUTION: PREPARED_WAITING_FOR_OPERATOR
CANDIDATE-4: PREPARED_FOR_FOCUSED_CLIENT_RETEST
CLIENT TEST: NOT STARTED
FULL CLIENT ACCEPTANCE: NOT COMPLETE
PRODUCTION BALANCE PROMOTION: HOLD
PROJECT ACCEPTANCE: PENDING
STABLE PUBLICATION: NOT AUTHORIZED
```

## Product and workflow authority

- Candidate-4 Product HEAD: `9fe86d2e787ab1f86dcf38a5abdba6168515a802`
- Current branch／PR HEAD at product preparation: `acc175f7e8768a40ec1f86a9493b64ddc0caaf0d`
- `acc175f` is a Sidecar-only checksum normalization commit and is not the
  Product HEAD.
- Formal Instruction Authority: V002 substitution, Size `52763`, SHA-256
  `b40cf5ed90cd1dec5ffb82f8371d94977581e3b0a8bc313ee3000535283bcf2e`.
- Normal CI: Run `30757341843`, success.
- Headless: Run `30757341825`, success.
- Event Head: `acc175f7e8768a40ec1f86a9493b64ddc0caaf0d`.
- PR merge ref and workflow checkout: `c529d77478cf4ee8ca30bf3aefc50765622f6937`.
- `PR_MERGE_REF_VALIDATION`: PASS.
- `EXACT_PRODUCT_HEAD_CHECKOUT`: NOT PERFORMED.

## Fixed Candidate-4 evidence

- Headless Artifact ID: `8836419960`.
- Headless Artifact GitHub digest and downloaded ZIP SHA-256:
  `23faa83e435315a2234a8ab5fe0cb0b893e9d8b17e687d3210b2b6d154204433`.
- Internal raw evidence tar SHA-256:
  `2f73bf675298c6a9978eb3f1872b5f312d5b9278f266eb21e8dd7e2e003f9e06`.
- Main: Size `4690292`, SHA-256
  `c263f6957c69bf958b6374e37efbf0cff7cc0e21d27530acf7faa46cd1b54522`.
- Frontier: Size `4710866`, SHA-256
  `7897c31bdc69e05112e286235658364d2771ab875113f9410341b6d9910e1bac`.
- Published Core authority: Size `11751447`, SHA-256
  `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`.
- Approved Fixture: Size `3011`, SHA-256
  `ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a`.
- Build 1: preserved preliminary only; formal Build 1 qualification not
  accepted because its durable start/end record was not captured.
- Build 2: Product HEAD `9fe86d2`, Java `25.0.3`, Gradle `9.6.1`, clean assemble
  success; Main／Frontier／Fixture byte-identical to preserved Build 1.
- Local-only paths: `.ai-work/luna-gpt-5.6-v003/reproducibility/` and
  `.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-4/`.

## Runtime boundary

The intended fresh authority is MariaDB schema `wayfarer_client_v002_c4`, Redis
prefix `wf-v002-client-c4`, Main ID `wayfarer-client-c4-main`, Frontier ID
`wayfarer-client-c4-frontier`, Main `192.168.10.6:25570`, Frontier
`192.168.10.6:25571`, and `online-mode=true`. The host currently has
`192.168.10.6` Preferred and both ports were free at read-only audit time.

Project Runtime creation, installation, migration, plugin enable, server start,
restart, and deployment were not executed because AGENTS.md prohibits Project
Runtime changes from this repository. No temporary permission was granted. No
Minecraft Client connection or Client Scenario was started.

## Handoff references

- Local Candidate manifest:
  `.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-4/CANDIDATE_4_MANIFEST.md`
- Local checksum file:
  `.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-4/CANDIDATE_4_SHA256SUMS.txt`
- Worksheet root:
  `.ai-work/luna-gpt-5.6-v003/client-test/V0.0.2-Client-Candidate-4-Focused/worksheet/`
- The fixed JARs must not be overwritten. Product changes belong to Candidate-5; Candidate-4 is
  rejected and remains immutable historical evidence.
