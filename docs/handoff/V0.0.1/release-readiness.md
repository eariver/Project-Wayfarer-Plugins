# V0.0.1 Release Readiness

- Release readiness: BLOCKED
- Latest approved pre-release:
  [`V0.0.1-alpha.1`](https://github.com/eariver/Project-Wayfarer-Plugins/releases/tag/V0.0.1-alpha.1)
- Alpha.1 source commit: `192cda35dce0dba855c2da4eb1ed71a0425f549a`
- Stable final source commit: Pending
- Requirement traceability: In progress; global gate remains `BLOCKED`
- Automated tests: Passed, 75 tests / 0 failed / 0 skipped; corrected-head CI passed
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
  Focused regression and local `check` passed. The targeted Paper/client rerun returned the
  sanitized unavailable response with no dispatcher exception, secret, internal exception
  message, or provider reference.
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
- `requirements_cleared`: Project Owner input remains required
- Project Runtime placement/acceptance: Pending / pending

Stable release remains blocked until the stacked PRs are reviewed and merged, concrete Waymark
provider authority is resolved, client acceptance and every applicable traceability/handoff gate
are complete, a stable final source is fixed, Project Runtime placement/acceptance is recorded, and
the Project Owner explicitly clears requirements. The alpha.1 source commit and the unmerged rc.1
head are not stable final source.

The beta feature-complete candidate has current-head automated API/module boundaries, Core
packaging inspection, configuration-cache reuse, dependency/license inventory, and same-source JAR
reproducibility evidence. The rc.1 pre-client workflow adds commit-pinned client-independent Paper
evidence. Candidate C's client failure and the Client Fix Candidate's targeted correction are both
retained in the client result. ADR 0006 blocks a concrete Waymark provider. These results do not
change the `BLOCKED` marker.
