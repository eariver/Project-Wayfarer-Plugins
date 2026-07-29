# V0.0.1 Release Readiness

- Release readiness: BLOCKED
- Latest approved pre-release:
  [`V0.0.1-alpha.1`](https://github.com/eariver/Project-Wayfarer-Plugins/releases/tag/V0.0.1-alpha.1)
- Alpha.1 source commit: `192cda35dce0dba855c2da4eb1ed71a0425f549a`
- Stable final source commit: Pending
- RC.3 evidence record commit: `92e32db98758eddad46c5f18772c21ef83366057`
- RC.3 final balance probe commit: `7f013b346d2cd97705c45dbbf8a18f51e9607525`
- Requirement traceability: In progress; global gate remains `BLOCKED`
- Latest local automated tests: Passed, 192 unit (178 Core / 6 API / 8 Common),
  14 MariaDB, and 6 Redis; 0 failed/errors/skipped
- Alpha.2 PR B correction: 131 local unit tests / 0 failed / 0 skipped; updated-head GitHub
  Actions `check` and `assemble` passed at
  `0d5bf928a489d7fcbac51e93244af2180b4a539c`
- Alpha.3 automated gate: 140 unit, 9 MariaDB, and 6 Redis cases / 0 failed / 0 skipped;
  commit-pinned CI `30290422624` passed
- Corrected alpha.4 provider-independent gate: 167 unit, 14 MariaDB, and 6 Redis cases /
  0 failed / 0 skipped; CI `30354268891` passed at
  `38fc2b55cfa1c91f2ca04daab47d062aada8a42e`. Concrete Waymark provider authority remains
  blocked by ADR 0006
- Corrected beta.1 gate: 169 unit, 14 MariaDB, and 6 Redis cases / 0 failed / 0 skipped;
  clean `check`, clean `assemble`, configuration-cache reuse, API/module boundaries, packaging,
  migration hashes, and same-source reproducibility passed in CI `30355673880` at
  `b173ebfcff75b4fc4689155fc5b0d54960a04ab6`
- RC.1 pre-client headless gate: passed at
  Candidate C `7557b1836b0fe943a07f06d8af5c05849c6c8941`; workflow
  [`30360911544`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30360911544)
  and normal CI
  [`30360911048`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30360911048)
  passed with 173 unit, 14 MariaDB, and 6 Redis cases and zero failed/skipped tests
- RC.1 candidate Core SHA-256:
  `5ab6200839baa02d637bdf214126d79a10e925884da92b491bc9dcc0f8ddec1a`
- Candidate C client acceptance: `FAIL`; authorized transaction inspect leaked a synchronous
  provider-absent service exception to the Paper command dispatcher. The successful Candidate C
  headless evidence remains valid and immutable.
- Client Fix Candidate:
  `d8804c68e2f7501b83206c8d4afa5e31dfdc3eb7`;
  `wayfarer-core-0.0.1-rc.1-client-fix.1.jar` SHA-256
  `c58a550a5fc0811bf8bbdfc4e42e6cee4316fc2bc6ece9db83bbc1784550674f`.
  Focused regression and local `check` passed. GitHub Actions CI
  [`30367633302`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30367633302)
  passed `check`, `assemble`, configuration-cache reuse, same-source JAR reproducibility, and Core
  packaging at code-bearing PR head `586db6ddc49218ab06b2421de896148d75e27916`.
  The targeted Paper/client rerun returned the sanitized unavailable response with no dispatcher
  exception, secret, internal exception message, or provider reference.
- Isolated test server: Alpha.1 `PASSED`;
  `docs/testing/evidence/V0.0.1-alpha.1-runtime-evidence.md`
- Pre-client evidence:
  `docs/testing/evidence/V0.0.1-rc.1-preclient-headless.md`;
  result wording is `Automated/headless runtime passed`
- Client acceptance evidence:
  `docs/testing/results/V0.0.1-client-acceptance.md` at
  `f2deab28c307de460adffb3b8dfc8c7252c0ec7a`
- User observations: Real-player reconnect/identity, non-OP denial, authorized health,
  responsiveness, and corrected sanitized inspect passed
- Known limitations/open decisions: See linked handoff records
- RC.2 concrete-provider candidate:
  `5039e008659be1f7e23658aabba12cb95a8a600d`;
  `wayfarer-core-0.0.1-rc.2.jar` SHA-256
  `8C85F9C0D42EED631F3167DE5827C21139D07B71A63CE3E0AC90F746F9A651E6`.
  Focused Vault/config tests, 176 unit, 14 MariaDB, and 6 Redis tests passed with zero
  failed/errors/skipped; `check`, `assemble`, and configuration-cache reuse passed. GitHub Actions
  [`30378563840`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30378563840)
  passed check, assemble, cache reuse, same-source JAR reproducibility, and Core packaging at the
  candidate source.
- Concrete provider standalone: Paper 1.21.11 build 132 / Java 25 with task-only MariaDB 11.8,
  Redis 8, fixed VaultUnlocked, and fixed RedisEconomy passed shared balance, debit, idempotent
  replay, insufficient funds, refund, representative direct Vault withdraw/deposit, safe health,
  provider-absent fail-closed, and clean disable. Test containers/volumes were removed.
- RC.3 fractional-balance candidate:
  `95b2cf1ef159b4d16921ddb4c8698621b8134c3e`;
  `wayfarer-core-0.0.1-rc.3.jar` SHA-256
  `6E58B501EF0B58AA19C9DD1A39D41ABE13173EDE32BE70E3DB0979CE10A3278F`.
  Focused/API/transaction regressions and local `check assemble` passed. Dedicated Paper 1.21.11
  build 132 / Java 25 acceptance preserved 37.5 → long debit 25 → 12.5 → long refund 25, with
  final Vault 37.5 and final Wayfarer 37.5,
  direct Vault interoperability, idempotent replay, insufficient funds, sanitized output, and
  clean disable. GitHub Actions
  [`30413198551`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30413198551)
  passed check, assemble, cache reuse, same-source JAR reproducibility, and Core packaging. Final
  probe-head CI
  [`30445725741`](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30445725741)
  also passed.
- RC.3 publication status: `UNPUBLISHED REVIEW CANDIDATE`. The prerelease workflow was not
  dispatched; no RC.3 tag, GitHub pre-release/release, release URL, or published RC.3 asset exists.
  The JAR above was built locally from the fixed product source and its standalone acceptance is
  candidate evidence, not publication evidence. Release publication remains
  `RELEASE_PUBLICATION_PENDING`; `V0.0.1-alpha.1` remains the latest published approved pre-release.
- ADR 0007 Owner Decision resolves Gate B/C/D for V0.0.1 by accepting common Vault semantics.
  Vault `SUCCESS` is not durable Redis proof; concrete resolve remains UNKNOWN and no provider
  reference or exactly-once guarantee is synthesized.
- `requirements_cleared`: Project Owner input remains required
- Project Runtime placement/acceptance: Pending / pending

Stable release remains blocked until Draft PR #12 is reviewed and approved/merged, every applicable
traceability/handoff gate is complete, a stable final source is fixed, Project Runtime
placement/acceptance is recorded, and the Project Owner explicitly clears requirements. RC.3 is a
review candidate, not stable final source.

The beta/rc.1 evidence and Candidate C client failure remain immutable history; the Client Fix
Candidate retains its targeted correction. RC.2 adds the Owner-approved Vault concrete provider
and remains immutable history. RC.3 corrects fractional balance compatibility without overwriting
those candidates. The accepted provider
limitation and Project-side future design item remain disclosed. These results do not change the
stable-release `BLOCKED` marker.
