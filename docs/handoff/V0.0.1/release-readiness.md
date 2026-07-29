# V0.0.1 Release Readiness

- Release readiness: READY
- Stable version: `V0.0.1`
- Stable product source commit: `49e00e21716c1c13a2dbb170fdad1b19c4275612`
- Stable preparation evidence record: `eabda6d2c83e7369dd9f4ba4725f80d601a51062`
- Stable candidate JAR: `wayfarer-core-0.0.1.jar`
- Stable candidate SHA-256:
  `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2`
- Requirement traceability:
  `docs/requirements/main-server/Project-Wayfarer-V0.1.0/traceability.md`;
  gate `CLEARED`
- Stable local acceptance:
  `docs/testing/results/V0.0.1-stable-local-acceptance.md`;
  `PASS WITH DISCLOSED FULL-INVENTORY LIMITATION`
- Stable candidate client smoke:
  `PASS`; Java Edition 1.21.11 join, movement/chat, permission denial, authorized health,
  sanitized inspect, OP removal, disconnect/reconnect, and clean stop
- Stable client smoke evidence commit: `7d9a74c6d8a14a2d68d0f3b6e9cf48e1e72dcf06`
- Stable test report:
  `docs/reports/Project_Wayfarer_Plugin_Release_Test_Report_V0.0.1_2026-07-29.md`
- Authoritative final code-bearing preparation CI:
  [`30455335160`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30455335160)
  passed at `d16e92cd47267b749803623a3cf1b58850ac8ce4`
- Earlier supporting preparation CI:
  [`30451364006`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30451364006)
  passed at `d9753420b658a8beb69915980f7994d5b8f3f274`; historical/supporting only
- Earlier supporting preparation gate CI:
  [`30451214126`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30451214126)
  passed at `8f4b353d8d8a815fd2d7781671250ed180f37294`; historical/supporting only
- Stable publication: Pending explicit, approved `release.yml` dispatch
- Project Runtime placement/acceptance: Pending / Project-owned
- Project Runtime changed during preparation: No
- `requirements_cleared`: Must still be supplied explicitly by the Project Owner to confirm
  Plugin-side publication prerequisites and authorize source-side stable publication. It does not
  clear Project Runtime placement/acceptance, and this document does not infer or set the input

## Stable candidate verification

The stable product source is the immutable merge commit above. A detached clean worktree at that
commit built version `0.0.1` with Java 25.0.3 and Gradle 9.6.1.

| Check | Result |
|---|---|
| Clean `check assemble` | PASS |
| Unit tests | 192: 178 Core / 6 API / 8 Common; 0 failed/errors/skipped |
| MariaDB integration | 14; 0 failed/errors/skipped |
| Redis integration | 6; 0 failed/errors/skipped |
| Stable JAR reproducibility | Two clean builds produced the same SHA-256 |
| Runtime JAR count | 1 |
| Core-only boundary | Main/Frontier classes absent |
| Config / migrations | Config `1`; V001/V002/V003 each present once |
| Generated artifacts tracked | No |

## Local isolated acceptance

Paper 1.21.11 build 132 on Java 25 started with fresh task-only MariaDB 11.8 and Redis 8.
The Owner-supplied 23-JAR inventory was copied unchanged, with one Stable Core and a
production-excluded test probe added separately.

Core `0.0.1`, VaultUnlocked `2.20.2`, and RedisEconomy `4.5.12-wayfarer.1` enabled. Health was
`UP`; V001–V003 were applied and then validated as up-to-date after restart. The representative
transaction preserved the fractional balance path:

- initial Vault/Wayfarer balance `37.5`;
- debit `25` to `12.5`;
- same-transaction replay caused no second debit;
- insufficient funds was explicit and non-mutating;
- representative direct Vault withdraw/deposit was visible through Wayfarer;
- refund `25` restored both views to `37.5`;
- both server runs stopped cleanly with exit 0.

The subsequent Stable Candidate Client Smoke used the same fixed Stable JAR without a probe and
without an economy mutation. Client join, movement/chat, non-OP denial, authorized health,
sanitized inspect, OP removal, disconnect/reconnect, and final clean stop passed. Console health
identified `Vault/RedisEconomy`; client health safely showed `overall=UP` and `Waymark: UP`
without provider details. No client-facing regression was observed. Release readiness remains
`READY`, while Project placement and acceptance remain pending.

No raw generated secret, Wayfarer product failure marker, or Wayfarer classloader/API identity
error was found. Task containers and volumes were removed.

## Disclosed limitation

Full-inventory startup is `LIMITED`, not an unqualified full-set PASS:

- the supplied `VelocityScoreboardAPI.v2.1.0.jar` is a library JAR without a Paper plugin
  descriptor, so Paper reports a load error for that file;
- Iris reports Java 25 warning mode and a dynamic-agent warning;
- EliteMobs emitted one task-registration exception during the first clean shutdown after the
  acceptance marker.

Paper initialized 24 actual plugins on both valid runs. The observations did not break a
Wayfarer dependency, affect shared balances, produce a Wayfarer failure marker, or prevent clean
exit. They are nonblocking for the Core-only publication gate and remain Project placement
considerations.

## Publication and authority boundary

ADR 0008 records the Owner-approved direct-stable path. `V0.0.1-alpha.1` remains historical;
there is no requirement to create another pre-release. The stable workflow must:

1. receive the exact stable source commit and expected JAR SHA-256;
2. validate that source against `origin/main`;
3. require committed `CLEARED` traceability and `READY` readiness inputs;
4. rebuild the stable JAR and reject a SHA mismatch;
5. retain GitHub Environment approval and explicit `requirements_cleared=true` Owner
   authorization for source-side stable publication;
6. publish only after the workflow input summary is displayed and separately approved.

No stable tag, GitHub release, URL, or published stable asset exists yet. This readiness result
does not authorize Project Runtime installation, migration, configuration, restart, deployment,
tagging, or release publication. Even after Owner publication authorization, Project Runtime
placement, migration execution, configuration, smoke/acceptance, and Roadmap completion remain
pending and Project-owned.

## Historical evidence retained

Candidate C's client failure, the Client Fix Candidate, RC.2, and RC.3 remain immutable history.
RC.3 corrected fractional balance compatibility and is the product basis merged into the stable
source, but it was never published as a pre-release. ADR 0007's accepted Vault/RedisEconomy
limitation remains: Vault `SUCCESS` is not durable Redis completion, automatic effect lookup is
unavailable, and exactly-once is not claimed.
