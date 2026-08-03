# Phase 10C-A Candidate-5 Runtime Handoff

Status: `HANDOFF_ONLY_NOT_EXECUTED`.

This document is not authorization to change Project Runtime or to start MariaDB, Redis, Paper,
plugin installation, migrations, server restarts, or Minecraft Client Test. It records fresh,
Candidate-5-specific values for a later separately authorized disposable environment.

## Fixed input

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

## Fresh disposable identifiers

```text
MariaDB schema: wayfarer_client_v002_c5
Redis prefix: wf-v002-client-c5
Main server ID: wayfarer-client-c5-main
Frontier server ID: wayfarer-client-c5-frontier
Suggested ports: 25572 / 25573, subject to Owner availability approval
World boundary: exact case-sensitive frontier_iris; no world creation by Wayfarer
```

## Execution boundary

The operation record is `NOT_STARTED`: no Project Runtime, database, Redis, Paper, plugin
installation, migration, configuration, restart, world, credential, Player, or Minecraft Client
Test operation was performed from this repository. Later execution must use the bounded plan at
`docs/testing/plans/V0.0.2-client-acceptance.md`, a fresh disposable environment, and explicit
Owner authorization. Project acceptance and stable publication remain outside this handoff.
