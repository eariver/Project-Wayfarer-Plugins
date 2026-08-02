# V0.0.2 Release Readiness

- Candidate product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`
- Phase 09A independent review: `PASS`
- Phase 09B executor evidence: prepared; independent review required
- Client Test Candidate: `V0.0.2-Client-Candidate-1` fixed
- Mainline Disposable Client Test Preparation: `PENDING_INDEPENDENT_REVIEW`
- Client Acceptance: `NOT STARTED` / `CLIENT_TEST_REQUIRED`
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
  V0.0.2-Client-Candidate-1

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

Remaining gates:

1. Phase 09B independent review of the prepared Plugin-side Client Test input.
2. FRONT-D02 bounded client motion gate (`CLIENT_TEST_REQUIRED`) using the fixed Fixture.
3. Bounded Client Acceptance in a separate disposable environment.
4. Project acceptance and later stable publication.

FRONT-D01 is resolved; FRONT-D04 and MAIN-D08 are accepted with limitation at supported
boundaries. The first-test LeafGrapple baseline is `RESOLVED / APPROVED`, while production balance
is `OPEN_AFTER_CLIENT_TEST`. Phase 09B does not authorize Mainline execution before independent
review, Project deployment, production configuration, PR Ready status, merge, tag, release,
release workflow dispatch, `requirements_cleared=true`, or Client/Project acceptance.
