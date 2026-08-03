# Phase 10C-A Candidate-5 Product Remediation Result

Status: `PRODUCT_REMEDIATION_PASS`; Client Test `NOT_STARTED`; Project acceptance `PENDING`;
Stable publication `NOT_AUTHORIZED`.

## Authority and recovery

The Candidate-5 Execution Entry was read in full at the current PR HEAD before any Product edit.
Its Recovery result was:

```text
Local HEAD  = 37e49abff4d83957bd2101c9469ace838b3110dd
Origin HEAD = 37e49abff4d83957bd2101c9469ace838b3110dd
PR #14 HEAD = 37e49abff4d83957bd2101c9469ace838b3110dd
Worktree/index = clean
Branch = feature/V0.0.2-main-frontier
PR = Open / Draft / Unmerged
Fast-forward = NOT NEEDED (Local = Origin = PR)
Required Candidate-4 documents = present and preserved
Client Test = NOT STARTED
```

The Candidate-5 remediation handoff was then read in full and used as execution authority.
Candidate-4 Prepared/review documents remain historical materials and were not used as authority.
No V0.0.2 tag or GitHub Release existed at the recovery gate.

## Product fixation

Candidate-5 Product commit:
`3ba94dd561e2f845fd7726329bd89cdbfb51d51a`

The Product commit contains only the focused Main/Frontier remediation and tests:

- Main Broken-owner branch mutation now denies mutation while retaining Broken GUI/Repair access.
- Main authorization invalidation is fail-closed across held-slot, hand-swap, accepted inventory
  click/number-key, drag, Drop, Pickup, Respawn, reissue, refresh, and authority rewrite paths.
  Cancelled unchanged inventory operations do not permanently invalidate authorization.
- Managed action guards classify the actual item and require the exact status/authority state;
  ordinary items remain outside managed action handling.
- Frontier timeout terminal evidence is bounded, sanitized, and records source, generation,
  pollCount, visibleManagedItems, requiredManagedItems, fingerprint, and `decision=TIMEOUT`.
- Frontier late-MVI coordination is covered by a concrete timeout-then-public-MVI test, including
  duplicate coalescing and same-cycle restart reuse.

No `Wayfarer_Frontier_EliteMobsMVI` module was created. Core, migrations, Runtime configuration,
and Project-side Runtime were not changed.

## Tests-first evidence

Focused RED tests were added before Product changes. The intended RED results are recorded in the
local-only Candidate-5 evidence package:
`.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-5/red-evidence.md`.

The focused Main RED run failed on the Broken-owner branch assertion and deferred held-slot
authorization assertion. The focused Frontier RED run failed on the missing complete timeout
diagnostic. A setup-only Frontier attempt using zero required managed items was discarded and is
not treated as evidence.

## Local green validation

At Product HEAD, the following passed:

- focused Main gameplay/application/domain and remediation tests;
- focused Frontier application/gameplay/domain, timeout, and late-MVI coordination tests;
- full Main test suite: 126 tests, 0 failures, 0 errors, 0 skipped;
- full Frontier test suite: 82 tests, 0 failures, 0 errors, 0 skipped;
- repository `check` with MariaDB and Core Redis integration tasks explicitly excluded by the
  user boundary: 409 available JUnit tests, 0 failures, 0 errors, 0 skipped;
- `clean assemble`: `BUILD SUCCESSFUL`;
- `git diff --check`: passed before Product fixation.

The exact commands and limitations are in the local green evidence file. No local MariaDB,
Redis, Paper, Plugin installation, Runtime, or Minecraft Client Test was started.

## CI and Headless evidence

- Normal CI: [run 30774052884](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30774052884),
  conclusion `success`, head SHA `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`, job
  `91566040761`.
- Pre-client Headless: [run 30774053040](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30774053040),
  conclusion `success`, head SHA `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`, job
  `91566041012`, raw evidence tar SHA-256
  `767639d2929ea891df95fd49643a19b00f7a053c6f6ff209e877e52e30be854c`.

Both pull-request workflows checked out merge ref
`66be3fc0f524e852dfa077b16a2c322ca90a52df`, whose PR-head parent is the Product commit above;
the PR remains unmerged. The remote Headless workflow used its own isolated MariaDB, Redis, and
Paper services. This was an automatic CI consequence of the pushed PR and not a local Runtime
operation.

## Two clean builds and artifacts

Two independent detached worktrees at Product HEAD completed clean builds. Main and Frontier
were byte-identical across both builds:

| Artifact | Size | SHA-256 |
|---|---:|---|
| `wayfarer-main-0.0.2-SNAPSHOT.jar` | 4690577 | `391ea0b1beae8ff4e7ed1e8428179ff5b5166ff85fdd1c67d0fdff6062b82079` |
| `wayfarer-frontier-0.0.2-SNAPSHOT.jar` | 4713179 | `dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8` |

Core authority remains the separately published V0.0.1 artifact: 11751447 bytes,
SHA-256 `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`. The local
multi-module assemble generated a Core snapshot with a different local hash; it was not staged,
attached, renamed, or substituted. This is an authority limitation, not a Product source change;
the remote CI Core reproducibility and packaging checks passed.

## Handoff state and limitations

The local-only staging root is
`.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-5/`. It contains sanitized
hash/test/CI records, a submission ZIP and sidecar, and explicit `NOT_STARTED` placeholders. The
submission ZIP contains no JAR, world, database, Redis data, full log, secret, runtime config,
or raw Player identifier; its internal `SHA256SUMS` covers every other archive file exactly once.

Fresh Candidate-5 runtime values are recorded only in
`phase-10c-a-candidate-5-runtime-handoff.md`. They are not authorization. Client Test, Project
acceptance, production balance promotion, PR Ready status, merge, tag, release, workflow
dispatch, and `requirements_cleared=true` remain unauthorized/not performed.
