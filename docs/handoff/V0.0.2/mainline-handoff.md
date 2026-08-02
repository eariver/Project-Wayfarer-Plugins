# V0.0.2 Mainline Handoff

PR #14 remains Open / Draft / Unmerged. Phase 09A independent review is `PASS`. Phase 09B
executor evidence is prepared, but independent review is still required before Mainline
disposable Client Test preparation may proceed. The first bounded Client Test Candidate is fixed
as `V0.0.2-Client-Candidate-1` from accepted product source commit
`90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`. The Phase 09B metadata commit does not change any
candidate product bytes.

## Fixed candidate artifacts

| Component | Exact artifact | Size | SHA-256 / provenance |
|---|---|---:|---|
| Core | `Wayfarer_Core-V0.0.1.jar` | 11751447 | `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`; published GitHub Release `V0.0.1`, reused unchanged |
| Main | `wayfarer-main-0.0.2-SNAPSHOT.jar` | 4671368 | `730d56888001e9c76bd127b25c118a937f03a5dd95a0fa381c8c38fec2517113` |
| Frontier | `wayfarer-frontier-0.0.2-SNAPSHOT.jar` | 4682233 | `f43829c7b6e06ea44549ffdd1ef26a567aef1563ba73a0808c47634742e9d3ec` |

The exact local staging path is
`.ai-work/luna-gpt-5.6-v002/candidate/V0.0.2-Client-Candidate-1/`. The candidate JARs are
ignored local handoff artifacts, not tracked or published. Core was not rebuilt as V0.0.2.
Embedded descriptor identities and the checksum manifest are recorded in the local candidate
manifest.

## Frontier-approved first Client Test Fixture

The external Project/Frontier authority is Project Issue
`eariver/Project_Wayfarer#4`, specifically
https://github.com/eariver/Project_Wayfarer/issues/4#issuecomment-5155937809 (comment ID
`5155937809`). That comment records:

```text
Concrete Fixture:
  ACKNOWLEDGED

First Client Test baseline:
  APPROVED

Candidate:
  V0.0.2-Client-Candidate-1

Use:
  Client Test only

Pre-test balance changes:
  NONE

Production promotion:
  DECIDE_AFTER_CLIENT_TEST
```

The approved immutable Fixture is
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml` at commit
`521a41bbcc4d4e0e58111deeb663f52bf1c6e1af`, SHA-256
`ed210f8e56db26315f91fecb9e1d35d686c8fe647480498b7588467a6fa2448a`. The pinned LeafGrapple
artifact is `LeafGrapple.jar`, plugin version `1.0.2`, embedded Maven version
`1.0.2-SNAPSHOT`, size `56175` bytes, SHA-256
`FFE4B3305BB48737E1B6C373698FEFE7121B879FD5B9399F930F5023B5F78833`.

The first-test movement/range/cooldown values are:

```text
max-distance: 16.0
max-pull-distance: 32.0
launch-speed: 1.2
pull-acceleration: 0.05
max-pull-speed: 0.85
cooldown-ticks: 20
```

The Fixture also fixes `Wood Grapple`, PAPER with
`leafgrapple:wood_grapple` / `leafgrapple:wood_grapple_head`, durability disabled, glowing
display at scale `0.35`, offset Y `-0.1`, and entity-hook disabled with players, mobs, animals,
and monsters all false. The complete Entity Hook subtree remains copied from `hooks.wood`.
There are no pre-test balance changes.

## Tier configuration and Runtime Guard boundary

The LeafGrapple Tier Fixture owns the LeafGrapple public tier configuration: item/model/display,
range, pull distance, launch speed, pull acceleration, maximum pull speed, cooldown, disabled
durability, and disabled entity-hook targets. It does not own a world restriction.

The Wayfarer Runtime Guard separately enforces the exact case-sensitive `frontier_iris` boundary:
Themes outside `frontier_iris` are rejected; the plugin remains enabled when `frontier_iris` is
absent or unloaded; Wayfarer never creates the world; and Multiverse owns world
creation/loading. LeafGrapple itself must not be described as enforcing this boundary.

## Mainline disposable-client preparation

Mainline disposable Client Test preparation is `PENDING_INDEPENDENT_REVIEW`. After that review,
Mainline owns the separate disposable environment, server/world setup within that environment,
actual Minecraft Client execution, and the resulting evidence. It must use the exact candidate
bytes and the immutable test-only Fixture above; it must not use Project Runtime, Project worlds,
Project data, credentials, or Player UUIDs.

The exact detailed procedure is solely
`docs/testing/plans/V0.0.2-client-acceptance.md`. The required scenario groups are summarized
here only for handoff:

- motion and range: short-range Block Hook, horizontal long-range Hook, upward Hook to a cliff,
  mountain, tree, or structure, and cave/ceiling/narrow-space Hook;
- interaction composition: Hook-to-Elytra, Hook after Launchpad, cooldown-boundary repeated
  input, and reissued canonical LeafGrapple item use;
- safety and fail-closed: Player/Mob/Animal/Monster rejection, exact `frontier_iris` Runtime
  Guard rejection outside the boundary, and missing plugin, wrong version, missing tier, or unsafe
  configuration;
- display: held Hook and launched Hook Head Resource Pack models, including glowing/scale/offset
  usability.

For motion and interaction scenarios, the bounded repetition rules are normally three attempts,
stopping after three consecutive unambiguous successes; at least five consecutive cooldown
inputs; one or two representative attempts per configured entity target category; and at least
ten attempts for an affected scenario when an anomaly, instability, or unclear reproducibility
remains under constant conditions. Distinct artifact/configuration/fail-closed states need one
unambiguous result each. A single major failure remains material.

## Resolved and residual gates

- LeafGrapple first-test baseline: `RESOLVED / APPROVED` by the external Project/Frontier
  authority above.
- LeafGrapple production balance: `OPEN_AFTER_CLIENT_TEST`.
- Client Acceptance: `NOT STARTED`.
- Project acceptance: `PENDING` and Project-owned.
- Stable publication: `NOT AUTHORIZED`.

No Project Runtime, server, world, database, migration, secret, external original JAR, or
production configuration was changed. This handoff does not authorize Client Test execution,
production promotion, PR Ready status, merge, tag or GitHub Release creation, release workflow
dispatch, or setting/inferencing `requirements_cleared`.
