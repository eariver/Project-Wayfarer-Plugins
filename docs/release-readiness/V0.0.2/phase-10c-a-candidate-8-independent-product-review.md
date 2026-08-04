# Phase 10C-A Candidate-8 Independent Product Review

Recorded: 2026-08-04 JST  
Reviewer boundary: ChatGPT independent review / Owner authorization pending

## 1. Reviewed inputs

- Candidate-8 Product HEAD: `698c387dfe2e86de8e48ea59a80f35f14c728e2a`.
- Candidate-8 result/handoff commit: `bb311b6a3a1874c0120f5cf66684f38353f8603b`.
- Candidate-8 Product diff and the four authorized changed files.
- `phase-10c-a-candidate-8-product-remediation-result.md`.
- `phase-10c-a-candidate-8-runtime-handoff.md`.
- Normal CI run `30863949449`.
- Pre-client Headless run `30863949421`.
- Exact-head Candidate-8 artifact fixation recorded by Luna.

The branch advanced from the Candidate-8 handoff authority by exactly one Product commit and one
metadata-only result/handoff commit. No unauthorized Product or test file was changed.

## 2. Product review

Candidate-8 implements the prescribed final delivery-admission boundary:

1. the original `onJoin(UUID)` path remains and delegates with an always-true admission predicate;
2. the request-aware overload evaluates its admission predicate inside the existing Bukkit Main
   Thread runnable;
3. the predicate executes immediately before `gateway.deliverIfEligible(tool)` with no asynchronous
   boundary between the two operations;
4. rejected admission returns coordinator `SUPERSEDED` without invoking the physical gateway;
5. `completeDelivery(...)` returns immediately for `SUPERSEDED`, so it performs neither
   `markDelivered(...)` nor obsolete delivery-result/pending audit;
6. Main admin Reissue passes `ignoredTool -> isCurrent(request)` through that final boundary;
7. ordinary join and explicit retry delivery retain the existing non-request-aware path.

This closes the Candidate-7 race: once a newer same-Player request has been accepted on the Bukkit
Main Thread, the older Reissue cannot subsequently enter the physical inventory gateway. Conversely,
when the admission and gateway execute in one Main Thread runnable while the request is current, no
newer Bukkit Main Thread request can interleave between the final check and the physical mutation.

No new serializer, blocking synchronization, executor, schema, migration, transaction, balance,
Frontier, Core, build, workflow, or Runtime change was introduced.

## 3. Test review

The valid pre-change RED reproduced the exact defect at the physical mutation boundary:

- `deliveryStartedThenSupersededBeforeGatewaySkipsPhysicalMutation`;
- 19 focused ordering tests with one failure;
- failure was the required `PlayerInventory.addItem(...)` invocation after supersession.

The Product change then passed:

- Main ordering suite: 19 tests, zero failures/errors/skips;
- Delivery coordinator suite: 5 tests, zero failures/errors/skips;
- direct coordinator proof that rejected admission returns `SUPERSEDED` and invokes neither gateway
  nor `markDelivered(...)`;
- full Main regression;
- repository `clean check` and `clean assemble`;
- V0.0.2 packaging validation.

The test-only fixture adjustment was limited to deterministic gateway and pending-operation control.
It did not alter Product semantics or replace the prescribed assertion.

## 4. CI, Headless, and artifact evidence

- Normal CI `30863949449`: SUCCESS at Candidate-8 Product HEAD.
- Pre-client Headless `30863949421`: SUCCESS at Candidate-8 Product HEAD.
- Both classified the PR merge-ref as
  `1deefb31e716b56a4b48b6c52e19ea12b0a125bdf`, with Product HEAD as the PR-head parent.
- Main artifact: `wayfarer-main-0.0.2-SNAPSHOT.jar`, 4,696,199 bytes,
  SHA-256 `93c76efe11211b3f52319513ef560ef26bcda5a04155699e7edce16f5269139d`.
- Frontier artifact: `wayfarer-frontier-0.0.2-SNAPSHOT.jar`, 4,713,179 bytes,
  SHA-256 `dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8`.

Candidate-6 and Candidate-7 evidence/artifacts remain preserved separately. Candidate-8 did not
require a second formal build or evidence ZIP/sidecar.

## 5. Verdict

```text
PHASE 10C-A CANDIDATE-8 INDEPENDENT PRODUCT REVIEW:
  PASS

CANDIDATE-7:
  REJECTED / PRESERVED

CANDIDATE-8 PRODUCT:
  ACCEPTED FOR SERVER-SIDE RUNTIME PREFLIGHT

CANDIDATE-8 NON-CLIENT VALIDATION:
  PASS

SERVER-SIDE RUNTIME PREFLIGHT:
  NOT STARTED / READY FOR OWNER AUTHORIZATION

MINECRAFT CLIENT TEST:
  NOT STARTED / DEFERRED

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PR #14:
  OPEN / DRAFT / UNMERGED

TAG / RELEASE / STABLE PUBLICATION:
  NOT AUTHORIZED
```

No Candidate-9 remediation is required from the reviewed Product and evidence. The next permitted
technical phase is a fresh Candidate-8 Server-side Runtime Preflight after explicit Owner
authorization. Minecraft Client connection and client-driven scenarios remain deferred until that
preflight passes and the Owner is available.
