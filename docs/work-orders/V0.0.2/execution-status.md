# V0.0.2 Execution Status

Status: Phase 01–06 Plugin implementation/evidence work, Phase 08B remediation, Phase 10B-B
client-failure remediation, and Phase 10C-A Candidate-7 Product remediation are complete at the
fixed Product HEAD `980eda20921a5f3ae1f795a2b9a23b92f53ac8e2`. Candidate-4 and Candidate-5 remain
immutable historical evidence. Candidate-7 Product gates, local validation, Normal CI, and
Pre-client Headless passed; independent Product review, Server-side Runtime Preflight, Client Test,
Project acceptance, and Stable Release remain pending/not authorized.

## Fixed execution identity

- Work branch: `feature/V0.0.2-main-frontier`
- Candidate-1 historical product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`
- Candidate-2 historical product-source anchor: `f2281093a03c17be0b0e69004059dd7ccb072b1c`
- Candidate-4 historical Product HEAD: `9fe86d2e787ab1f86dcf38a5abdba6168515a802`
  (`REJECTED_BEFORE_CLIENT_TEST`, immutable evidence retained)
- Candidate-5 Product HEAD: `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`
- Candidate-7 Product HEAD: `980eda20921a5f3ae1f795a2b9a23b92f53ac8e2`
- Client Test Candidate-5: `V0.0.2-Client-Candidate-5`
  (`PRODUCT_FIXED_CLIENT_TEST_NOT_STARTED`)
- Candidate-1 fixation / Fixture commit: `521a41bbcc4d4e0e58111deeb663f52bf1c6e1af`
- Frontier concrete Fixture authority: `eariver/Project_Wayfarer#4`, approval comment
  `5155937809`; first Client Test baseline `APPROVED`, Client Test only
- Fixture: `docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`; SHA-256
  `ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a`
- Approved first-test values: `max-distance=16.0`, `max-pull-distance=32.0`,
  `launch-speed=1.2`, `pull-acceleration=0.05`, `max-pull-speed=0.85`, `cooldown-ticks=20`
- Candidate-5 Product commit: `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`; Pre-test balance
  changes: `NONE`; production promotion: `DECIDE_AFTER_CLIENT_TEST`
- Requirement SHA-256: `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`
- Project Runtime changed: No
- PR #14: Open / Draft / Unmerged; Product CI run `30774052884` PASS; Pre-client Headless run
  `30774053040` PASS
- Candidate-7 Product CI run `30831784629` PASS after allowed failed-job rerun; Pre-client Headless
  run `30831782928` PASS; both validated Product HEAD `980eda20921a5f3ae1f795a2b9a23b92f53ac8e2`
- Candidate-7 staging path: `.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-7/`
- `requirements_cleared`: absent and not inferred
- V0.0.2 exact Frontier world: `frontier_iris`; future single-name configurability is Issue #17
- Core: exact published V0.0.1 runtime reused unchanged; not rebuilt as V0.0.2
- Main/Frontier: Candidate-5 clean-build runtime hashes are recorded in the Candidate-5 result
  and Mainline handoff; Candidate-1 through Candidate-4 hashes remain historical evidence
- Candidate-5 staging path: `.ai-work/luna-gpt-5.6-v003/candidate/V0.0.2-Client-Candidate-5/`
- LeafGrapple: pinned 1.0.2 artifact and complete test-only Safe Tier handoff passed; tracked
  fixture is `docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`

## Phase 01–06 capability evidence

| Phase | Completed Plugin implementation/evidence | Current boundary |
|---|---|---|
| Phase 01 | Main progress addition saturates at `Long.MAX_VALUE`; terminal threshold evaluation is idempotent. | Client mining remains later. |
| Phase 02 | Frontier durable typed Pending Delivery, same-identity/epoch permanent-item redelivery, and Safe Entry reconciliation are implemented. | Client redelivery remains later. |
| Phase 03 | Main reissue architecture and authority/recovery boundaries are established. | External repair and client gates remain. |
| Phase 04 | Main paid reissue domain/persistence flow, V004, quote/confirm, rotation, pending delivery, and recovery evidence are present. | Cross-store atomicity is not claimed. |
| Phase 05 | Main death handling, no automatic restore, Player paid reissue command/runtime, free pending retry, and admin recovery are implemented. | Client death/reissue remains later. |
| Phase 06 | Main/Frontier permission leaves, umbrella children, defaults, and top-level command boundary are implemented and tested. | LuckPerms Project verification remains Project-owned. |

## Implemented scope at the anchor

- Core V0.0.1 API and migrations V001–V003 remain unchanged.
- Main uses current source through migration V004; Frontier uses its current source migration
  level and separate history.
- Main and Frontier depend on Core; Core has no dependency on either gameplay module.
- Normal inventories/profile state remains owned by the Minecraft backend/MVI.
- Launchpad uses current Player view direction at use time and current config performance values;
  persisted yaw is reserved/non-authoritative.
- Portal documentation is limited to the implemented `PlayerPortalEvent` boundary.
- Frontier remains enabled when `frontier_iris` is absent or unloaded; it never creates worlds.
  Expired Launchpads defer destructive transition while the world is unavailable and resume after
  a later load/relevant player entry. No health/degraded subsystem is added.
- Phase 08B regression covers the `UNKNOWN` defer decision and loaded-world expiration eligibility.
- FRONT-D02 Plugin source boundary is accepted with limitation; its exact first-test Fixture
  baseline is resolved/approved, while the bounded client motion gate and final production
  motion/range/balance remain after Client Test.
- Wayfarer Runtime Guard separately enforces exact case-sensitive `frontier_iris`; Themes outside
  it are rejected, the plugin remains enabled while the world is absent/unloaded, Wayfarer never
  creates worlds, and Multiverse owns world creation/loading.
- FRONT-D04 and MAIN-D08 are accepted with limitation at their supported public/cancellable
  boundaries.

## Remaining gates

- ChatGPT independent review of the Phase 08B remediation: `PASS` (external decision).
- Phase 09A/09B historical evidence: retained; Candidate-1 is rejected after Phase 10B-A
  Client Test failure.
- Candidate-4 Product remediation: `REJECTED_BEFORE_CLIENT_TEST` / preserved historical evidence.
- Candidate-5 Product remediation: historical evidence only; its prior result is superseded.
- Candidate-7 Product remediation: `PASS`; Product HEAD, local validation, Normal CI, Pre-client
  Headless, exact-head artifact hashes, and current Git/PR relation are fixed in the Candidate-7
  tracked result.
- Candidate-7 independent Product review: `PENDING`; do not start Server-side Runtime Preflight.
- Candidate-5 focused Client retest: `CLIENT_TEST_REQUIRED` / `NOT_STARTED`.
- FRONT-D02 client motion gate: `CLIENT_TEST_REQUIRED`; Safe Tier preparation is complete.
- Resource Pack: `SKIPPED_OUT_OF_SCOPE_BY_OWNER`.
- LeafGrapple first-test baseline: unchanged; balance decision remains pending after Client Test.
- Full Bounded Client Acceptance: `NOT COMPLETE` / `CLIENT_TEST_REQUIRED`.
- Project acceptance: pending; Stable publication: not authorized in this task.
- Waystone and EM–MVI remain deferred/not authorized.

Phase 07's documentation-only history remains retained. Phase 08B changed only the focused
Frontier source/test path and required handoff documents. Phase 09A changed candidate metadata,
handoff documents, and one sanitized test fixture. Phase 09B is documentation-only: it did not
change the Fixture bytes, candidate artifacts, product source, migration, config, Gradle,
dependency, or Project Runtime. Phase 10B-B changed Main/Frontier delivery boundaries and
focused regression coverage in product commit `f2281093a03c17be0b0e69004059dd7ccb072b1c`. Phase
10C-A Candidate-5 changed only the Main/Frontier product remediation and focused tests in
Product commit `3ba94dd561e2f845fd7726329bd89cdbfb51d51a`; it prepared a fresh local-only
evidence/handoff package. It does not claim Client Acceptance or change Project Runtime.
Phase 10C-A Candidate-7 changed only the Main Product remediation and focused tests in Product
commit `980eda20921a5f3ae1f795a2b9a23b92f53ac8e2`; its tracked result and Runtime Handoff are
metadata records only and do not authorize Project Runtime or Client execution. Candidate-7 did
not create a duplicate serializer, second formal clean build, evidence ZIP, or sidecar.
