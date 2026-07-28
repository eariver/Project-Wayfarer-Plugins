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
- Isolated test server: Alpha.1 `PASSED`;
  `docs/testing/evidence/V0.0.1-alpha.1-runtime-evidence.md`
- User observations: Non-OP denial passed; OP health passed; internal-detail suppression passed
- Known limitations/open decisions: See linked handoff records
- `requirements_cleared`: Project Owner input remains required
- Project Runtime placement/acceptance: Pending / pending

Stable release remains blocked until later alpha slices, every applicable traceability and handoff
gate, a stable final source commit, Project Runtime placement/acceptance, and explicit
Project Owner requirements clearance are complete. The alpha.1 source commit is not the stable
final source, and release readiness must remain `BLOCKED` after alpha.1 evidence alone.

The beta feature-complete candidate has current-head automated API/module boundaries, Core
packaging inspection, configuration-cache reuse, dependency/license inventory, and same-source JAR
reproducibility evidence. The rc.1 headless runtime gate is still required; ADR 0006 also blocks a
concrete Waymark provider. These additions do not change the `BLOCKED` marker.
