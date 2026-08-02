# V0.0.2 Execution Status

Status: Phase 01–06 Plugin implementation/evidence work and Phase 08B remediation are complete
at the accepted product source. Phase 09A fixed the first bounded Client Test Candidate. Phase
09B adopted the external Frontier-approved first-test Fixture and prepared the Plugin-side
handoff; independent review and Client Acceptance remain pending, and Project acceptance and
Stable Release remain not authorized.

## Fixed execution identity

- Work branch: `feature/V0.0.2-main-frontier`
- Candidate product-source anchor: `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`
- Client Test Candidate: `V0.0.2-Client-Candidate-1` (`FIXED`)
- Candidate-fixation metadata / Fixture commit: `521a41bbcc4d4e0e58111deeb663f52bf1c6e1af`
- Frontier concrete Fixture authority: `eariver/Project_Wayfarer#4`, approval comment
  `5155937809`; first Client Test baseline `APPROVED`, Client Test only
- Fixture: `docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml`; SHA-256
  `ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a`
- Approved first-test values: `max-distance=16.0`, `max-pull-distance=32.0`,
  `launch-speed=1.2`, `pull-acceleration=0.05`, `max-pull-speed=0.85`, `cooldown-ticks=20`
- Pre-test balance changes: `NONE`; production promotion: `DECIDE_AFTER_CLIENT_TEST`
- Requirement SHA-256: `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`
- Project Runtime changed: No
- PR #14: Open / Draft / Unmerged
- `requirements_cleared`: absent and not inferred
- V0.0.2 exact Frontier world: `frontier_iris`; future single-name configurability is Issue #17
- Core: exact published V0.0.1 runtime reused unchanged; not rebuilt as V0.0.2
- Main/Frontier: exact staged runtime filenames, sizes, and SHA-256 values are recorded in the
  candidate manifest and Mainline handoff
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
- Phase 09A independent review: `PASS`.
- Phase 09B executor evidence: prepared; independent review required. Mainline disposable Client
  Test preparation is pending that review.
- FRONT-D02 client motion gate: `CLIENT_TEST_REQUIRED`; Safe Tier preparation is complete.
- Client Test Candidate: fixed; LeafGrapple first-test baseline: `RESOLVED / APPROVED`.
- Bounded Client Acceptance: `NOT STARTED` / `CLIENT_TEST_REQUIRED`.
- Project acceptance: pending; Stable publication: not authorized in this task.
- Waystone and EM–MVI remain deferred/not authorized.

Phase 07's documentation-only history remains retained. Phase 08B changed only the focused
Frontier source/test path and required handoff documents. Phase 09A changed candidate metadata,
handoff documents, and one sanitized test fixture. Phase 09B is documentation-only: it does not
change the Fixture bytes, candidate artifacts, product source, migration, config, Gradle,
dependency, or Project Runtime.
