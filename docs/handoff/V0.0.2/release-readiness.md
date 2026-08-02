# V0.0.2 Release Readiness

Current Phase 10C-A Revision B state:

```text
PHASE 10C-A EXECUTION: PREPARED_WAITING_FOR_OPERATOR
CANDIDATE-4: PREPARED_FOR_FOCUSED_CLIENT_RETEST
CLIENT TEST: NOT STARTED
FULL CLIENT ACCEPTANCE: NOT COMPLETE
PRODUCTION BALANCE PROMOTION: HOLD
PROJECT ACCEPTANCE: PENDING
STABLE PUBLICATION: NOT AUTHORIZED
```

- Candidate-4 Product HEAD: `9fe86d2e787ab1f86dcf38a5abdba6168515a802`.
- Candidate-3: rejected and preserved as historical failure evidence.
- Candidate-4 Main／Frontier／Fixture: two-clean-build byte-identical evidence fixed;
  Published V0.0.1 Core authority verified separately.
- Current PR remains Open / Draft / Unmerged; no Client Scenario has started.

- Candidate-1 historical product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`
- Candidate-2 product-source anchor: `f2281093a03c17be0b0e69004059dd7ccb072b1c`
- Phase 09A independent review: `PASS`
- Phase 09B executor evidence: historical; Candidate-1 client result is `FAIL`
- Client Test Candidate-1: rejected for promotion; evidence retained immutable
- Client Test Candidate-2: historical and superseded by Candidate-4 preparation
- Mainline Disposable Client Test Preparation: fresh two-backend environment prepared;
  focused retest pending independent review
- Client Acceptance: `NOT COMPLETE` / `CLIENT_TEST_REQUIRED`
- Resource Pack: `SKIPPED_OUT_OF_SCOPE_BY_OWNER`
- Stable tag/release: not authorized and not created
- `requirements_cleared`: absent and not inferred
- Project Runtime changed: No

Phase 01–06 capability evidence is retained in the Evidence Index. Fixed Phase 06 evidence:

```text
Phase 06 CI: 30701316290
Phase 06 Headless: 30701316289
Artifact: preclient-headless-evidence
Digest: sha256:218a363b71acaa93b8e55ee9b6a2215e98f94763f732e636edb38cd068065fef
```

## Frontier-approved first-test baseline

External authority: Project Issue `eariver/Project_Wayfarer#4`, approval comment
https://github.com/eariver/Project_Wayfarer/issues/4#issuecomment-5155937809 (ID
`5155937809`). The exact Fixture is acknowledged and approved for the first Client Test only:

```text
Candidate:
  V0.0.2-Client-Candidate-4

Pre-test balance changes:
  NONE

Production promotion:
  DECIDE_AFTER_CLIENT_TEST

Fixture commit:
  521a41bbcc4d4e0e58111deeb663f52bf1c6e1af

Fixture SHA-256:
  ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a
```

Approved movement/range/cooldown values are `16.0`, `32.0`, `1.2`, `0.05`, `0.85`, and `20`
for `max-distance`, `max-pull-distance`, `launch-speed`, `pull-acceleration`, `max-pull-speed`,
and `cooldown-ticks`, respectively. The LeafGrapple Fixture owns these tier values; the Wayfarer
Runtime Guard separately owns exact case-sensitive `frontier_iris` rejection and world-availability
behavior. Wayfarer never creates the world; Multiverse owns world creation/loading.

The Fixture bytes and approved values are unchanged from the historical Candidate-1 handoff.
Candidate-1 failed before the LeafGrapple motion/balance scenarios, so balance remains pending.

Remaining gates:

1. Candidate-4 focused Main/Frontier client gates using the fixed Fixture.
2. FRONT-D02 bounded client motion gate (`CLIENT_TEST_REQUIRED`) using the fixed Fixture.
3. Bounded Client Acceptance in the separate disposable environment.
4. Project acceptance and later stable publication.

FRONT-D01 is resolved; FRONT-D04 and MAIN-D08 are accepted with limitation at supported
boundaries. The first-test LeafGrapple baseline is `RESOLVED / APPROVED`, while production balance
is `OPEN_AFTER_CLIENT_TEST`. Phase 10B-B does not authorize Project deployment, production
configuration, PR Ready status, merge, tag, release, release workflow dispatch,
`requirements_cleared=true`, or Client/Project acceptance.
