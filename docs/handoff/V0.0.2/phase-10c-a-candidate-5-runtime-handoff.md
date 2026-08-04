# Phase 10C-A Candidate-5 Runtime Handoff

Status: `SUPERSEDED_DO_NOT_EXECUTE`.

Candidate-5 was rejected by the independent review before Runtime Preflight. Do not install its
artifacts, create its database/Redis authority, start Paper, or connect a Minecraft Client.

Authoritative review:

`docs/release-readiness/V0.0.2/phase-10c-a-candidate-5-independent-review.md`

```text
CANDIDATE-5:
  REJECTED / PRESERVED

CANDIDATE-6:
  REQUIRED

RUNTIME PREFLIGHT:
  DO NOT START

CLIENT TEST:
  DO NOT START
```

The remainder of this file is retained only as the historical, unexecuted Candidate-5 runtime plan.
Its identifiers must not be reused for Candidate-6.

---

Historical status: `HANDOFF_ONLY_NOT_EXECUTED`.

## Historical fixed input

- Product HEAD: `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`
- Candidate: `V0.0.2-Client-Candidate-5`
- PR #14: Open / Draft / Unmerged
- Main: `wayfarer-main-0.0.2-SNAPSHOT.jar`, 4690577 bytes,
  SHA-256 `391ea0b1beae8ff4e7ed1e8428179ff5b5166ff85fdd1c67d0fdff6062b82079`
- Frontier: `wayfarer-frontier-0.0.2-SNAPSHOT.jar`, 4713179 bytes,
  SHA-256 `dda3ac825ddde024046e9c72c9954cf3af59ceb1e8643aeb44571ea7f9a312b8`
- Core: published V0.0.1 authority, 11751447 bytes,
  SHA-256 `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`
- Fixture: `docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`, commit
  `521a41bbcc4d4e0e58111deeb663f52bf1c6e1af`, SHA-256
  `ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a`

## Historical disposable identifiers — retired

```text
MariaDB schema: wayfarer_client_v002_c5
Redis prefix: wf-v002-client-c5
Main server ID: wayfarer-client-c5-main
Frontier server ID: wayfarer-client-c5-frontier
Suggested ports: 25572 / 25573
World boundary: frontier_iris
```

These values were never executed and are now retired. Candidate-6 must use a new, separately
specified authority only after Product remediation and independent review pass.

## Execution record

No Project Runtime, database, Redis, Paper, plugin installation, migration, configuration, restart,
world, credential, Player, or Minecraft Client Test operation was performed for Candidate-5.
