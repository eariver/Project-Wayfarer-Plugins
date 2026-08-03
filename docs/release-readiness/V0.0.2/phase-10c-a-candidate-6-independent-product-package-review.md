# Phase 10C-A Candidate-6 Independent Product and Package Review

Recorded: 2026-08-03 JST

## 1. Reviewed authority and inputs

```text
Repository:
  eariver/Project-Wayfarer-Plugins

PR:
  #14 / Open / Draft / Unmerged

Candidate-6 Product HEAD:
  2a3f1cc384c397e610aba33c6ffc0f6a29af2987

Candidate-6 synchronization merge HEAD:
  b0bb5a5f2047a87a5313701d5ae46f825aec16d4

Submission ZIP:
  Candidate-6-V0.0.2-sanitized-submission.zip
  size 10145 bytes
  sha256 4337f09565c256a2d2ca2f1bb5983dee41c3055cd66f0f3582a3ef6e11978f2f

External sidecar:
  Candidate-6-V0.0.2-sanitized-submission.sidecar.txt
  sha256 df16dcfebefe054b929f182957ba96e81f7be001c3b62e6a18735e1a600f8eac

Minecraft Client test:
  NOT STARTED
```

The review inspected the Candidate-6 Product diff and surrounding task/runtime behavior, the
Candidate-6 Handoff Revision C, the supplied ZIP and sidecar bytes, PR/workflow state, and the
recorded formal-build and validation evidence.

## 2. Confirmed successful work

Candidate-6 successfully closes the single-operation ordering defect identified in Candidate-5:

- Reissue, Revoke, and authority refresh invalidate Held Authorization on the main thread before
  dispatching their database operation;
- success, no-change, conflict, and recoverable failure paths load or install an authority state and
  reauthorize the actual Main-Hand item;
- security-sensitive Main entry points compare the current physical claim with the current Session
  authority instead of trusting only a stale state enum;
- the missing Main transition and Frontier late-MVI cancellation tests were added;
- Frontier Product code remained unchanged because the new cancellation tests passed;
- Main, Frontier, MariaDB/Redis integration, Normal CI, and Pre-client Headless completed successfully;
- two clean builds from exact Candidate-6 Product HEAD produced byte-identical Main and Frontier
  artifacts;
- no Minecraft Client connection or client-driven scenario was started.

The Candidate-6 package is substantially improved over Candidate-5. Its external SHA-256, ZIP CRC,
path safety, UTF-8 decoding, internal `SHA256SUMS.txt`, complete one-to-one checksum coverage,
references, sanitization, and required evidence categories pass independent inspection.

These successes remain useful Candidate-7 baseline evidence.

## 3. Blocking Product defect — overlapping async authority completions can restore an older authority

The Candidate-6 implementation protects each Reissue/Revoke/refresh operation in isolation, but it
does not sequence or version concurrent operations for the same Player.

`invalidateBeforeAuthorityRead(...)` invalidates the cache, then each operation independently submits
a database task. Every completion later calls `applyAuthoritativeState(...)`, which unconditionally
opens the supplied `GrowthTool` in `GrowthSessionStore` and reauthorizes Main Hand. There is no
per-Player operation generation, latest-request token, or serialization gate checked before applying
the completion.

The Core task implementation does not provide implicit ordering for this purpose:

- `DefaultWayfarerTasks.database(...)` submits each operation directly to `ManagedExecutor`;
- `ManagedExecutor` is a configurable fixed-size `ThreadPoolExecutor`, so operations and their
  completions may finish out of order.

Therefore an older completion can overwrite a newer authoritative Session state. Two representative
unsafe interleavings are:

```text
Stale refresh resurrection:
  refresh invalidates and reads Epoch 1
  later Reissue commits and applies Epoch 2
  delayed refresh callback applies the previously read Epoch 1
  Session/cache can remain on Epoch 1 after Epoch 2 is authoritative
```

```text
Older successful mutation after newer mutation:
  Reissue A commits an ACTIVE authority
  Revoke B later commits a newer REVOKED authority
  B completion applies REVOKED
  delayed A completion applies its older ACTIVE result
  old capability can be restored after the newer durable change
```

This violates the Candidate-6 authority requirements that an older Item Instance/Epoch cache is never
restored and that authorization generated for an older Item Instance/Epoch is never exposed.

The current focused tests control one database stage at a time. They prove immediate invalidation and
individual success/conflict/failure recovery, but they do not hold two same-Player operations and
complete them in reverse order.

## 4. Required invariant

For every Player, all Reissue/Revoke/refresh requests that can expose changed authority must obey:

1. the request immediately fail-closes Held Authorization on the main thread;
2. each request receives an ordering identity, or enters an equivalent per-Player serialization
   mechanism;
3. an async completion may update Session/Held Authorization only when it is still current for that
   Player;
4. a stale completion must not open a Session, reconcile inventory, or reauthorize Main Hand;
5. the latest recoverable operation must restore the actual current database authority;
6. quit/stop must prevent an older outstanding completion from reopening Player state;
7. no synchronous database or Redis access is introduced into Bukkit handlers.

The implementation mechanism is not prescribed. A monotonic per-Player generation token, a
per-Player serialized async chain with immediate invalidation, or another bounded equivalent is
acceptable.

## 5. Package verdict

```text
EXTERNAL SIDECAR:
  PASS

ZIP INTEGRITY / PATH SAFETY:
  PASS

INTERNAL SHA256SUMS:
  PASS

CHECKSUM COVERAGE:
  PASS

SANITIZATION:
  PASS

FORMAL EVIDENCE COMPLETENESS:
  PASS
```

The supplied package remains valid historical evidence for Candidate-6. It cannot authorize Runtime
Preflight because the fixed Product bytes contain the blocking concurrency defect. Candidate-7 must
produce new Product bytes, new formal-build identities, and a new package; Candidate-6 files must not
be overwritten.

## 6. Independent verdict

```text
PHASE 10C-A CANDIDATE-6 INDEPENDENT REVIEW:
  FAIL / HOLD

CANDIDATE-6 PRODUCT:
  REJECTED BEFORE SERVER-SIDE RUNTIME PREFLIGHT

CANDIDATE-6 PACKAGE MECHANICS / COMPLETENESS:
  PASS

CONFIRMED PRODUCT DEFECT:
  SAME-PLAYER ASYNC AUTHORITY COMPLETION ORDERING

CANDIDATE-7:
  REQUIRED

SERVER-SIDE RUNTIME PREFLIGHT:
  DO NOT START

MINECRAFT CLIENT TEST:
  DO NOT START / DEFERRED

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PRODUCTION BALANCE PROMOTION:
  HOLD

PROJECT ACCEPTANCE:
  PENDING

STABLE PUBLICATION:
  NOT AUTHORIZED
```

## 7. Candidate-7 scope

Candidate-7 is limited to:

1. prevent stale same-Player Reissue/Revoke/refresh completions from applying authority state;
2. add deterministic reverse-completion tests for overlapping refresh/mutation and mutation/mutation;
3. preserve Candidate-6 immediate fail-close, recovery, action-boundary, and Frontier behavior;
4. rerun the focused/full/integration/CI/Headless validation applicable to the Product change;
5. perform two new formal clean builds from exact Candidate-7 Product HEAD;
6. create immutable Candidate-7 artifacts and a new complete sanitized ZIP/sidecar;
7. stop for independent Product/package review before fresh server-side Runtime Preflight;
8. continue to defer the first Minecraft Client connection and all client-driven scenarios.

Candidate-6 Product artifacts and submission bytes remain immutable historical evidence.