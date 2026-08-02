# V0.0.2 Mainline Handoff

PR #14 remains Open / Draft / Unmerged. Phase 08B independent review is `PASS`. The first
bounded Client Test Candidate is fixed as `V0.0.2-Client-Candidate-1` from the accepted product
source commit `90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`. The later candidate-fixation metadata
HEAD is documentation-only; its immutable SHA is recorded in the Phase 09A result report and
PR #14, and it does not change the candidate product bytes.

## Fixed candidate artifacts

| Component | Exact artifact | Size | SHA-256 / provenance |
|---|---|---:|---|
| Core | `Wayfarer_Core-V0.0.1.jar` | 11751447 | `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`; published GitHub Release `V0.0.1`, reused unchanged |
| Main | `wayfarer-main-0.0.2-SNAPSHOT.jar` | 4671368 | `730d56888001e9c76bd127b25c118a937f03a5dd95a0fa381c8c38fec2517113` |
| Frontier | `wayfarer-frontier-0.0.2-SNAPSHOT.jar` | 4682233 | `f43829c7b6e06ea44549ffdd1ef26a567aef1563ba73a0808c47634742e9d3ec` |

The exact local staging path is
`.ai-work/luna-gpt-5.6-v002/candidate/V0.0.2-Client-Candidate-1/`.
The candidate JARs are ignored local handoff artifacts, not tracked or published. Core was not
rebuilt as V0.0.2. Embedded descriptor identities and the checksum manifest are recorded in the
local candidate manifest.

## LeafGrapple test-only handoff

The pinned read-only artifact is `LeafGrapple.jar` version `1.0.2` / Maven `1.0.2-SNAPSHOT`,
SHA-256 `FFE4B3305BB48737E1B6C373698FEFE7121B879FD5B9399F930F5023B5F78833`. The complete
test-only `hooks.wayfarer` fragment is tracked at
`docs/testing/fixtures/V0.0.2/leafgrapple-wayfarer-test-only.yml` and is byte-identical to the
local candidate handoff. It copies the reviewed `hooks.wood` tier and changes only the tier key,
durability enabled flag, entity-hook enabled flag, and all four entity target safety values to
false. It is not a production balance recommendation and has not been applied to a server.

## Mainline disposable-client preparation

Mainline may use the exact local candidate bytes and the tracked Safe Tier fixture to prepare its
separate disposable environment. The test plan is
`docs/testing/plans/V0.0.2-client-acceptance.md`.

- The exact Frontier world name is `frontier_iris` (case-sensitive).
- Multiverse owns world creation and loading. Wayfarer must not create the world.
- Wayfarer must remain enabled when `frontier_iris` is absent or unloaded; it must fail closed at
  the exact player/world boundary and resume the relevant Join, WorldChanged, or Respawn path after
  a later load. Unloaded-world Launchpad expiration must defer before destructive transition.
- Mainline should verify the canonical LeafGrapple capability/item boundary, durability-disabled
  behavior, entity/player/mob/animal/monster-hooking-disabled behavior, and bounded canonical
  motion using the copied tier.
- Mainline owns client execution, server configuration, world operations, motion/range/feel/balance
  observation, and the resulting client evidence.

Accepted product-head evidence remains Normal CI run `30713914057` (`success`) and Pre-client
Headless Runtime run `30713914051` (`success`), both at product source
`90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`. A later metadata-head Normal CI run is required after
the documentation commit; any automatically triggered metadata-head Headless run is observation
only and does not replace the accepted product-head runtime gate.

## Resolved and residual gates

- `FRONT-D01`: resolved for V0.0.2.
- `FRONT-D02`: Plugin public-capability/fail-closed boundary accepted with limitation; Safe Tier
  handoff is complete, while bounded client motion remains required.
- `FRONT-D04` and `MAIN-D08`: accepted with their supported-boundary limitations.
- Client Test Candidate: fixed.
- Client Acceptance: `NOT STARTED` / `CLIENT_TEST_REQUIRED`.
- Project acceptance: pending and Project-owned.
- Stable publication: not authorized.

No Project Runtime, server, world, database, migration, secret, external original JAR, or
production configuration was changed. No candidate JAR was attached to a Release. Do not mark PR
#14 Ready for Review, merge it, create a tag or GitHub Release, dispatch a stable release workflow,
or set/infer `requirements_cleared`.
