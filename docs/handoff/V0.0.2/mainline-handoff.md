# V0.0.2 Mainline Handoff

Draft PR #14 remains review-only. The Plugin implementation/test anchor is the immutable commit
`7faf79081572df028a5ec19ccfbc820123180fc7`. The Phase 07 documentation revision is the current
documentation commit recorded by Git history and PR #14.

Owner Amendments are documented separately from the immutable requirement. Phase 01–06 Plugin
implementation/evidence is complete, and Phase 08B adds the focused unloaded-world expiration
guard plus regression evidence. This remains pre-client review evidence: it is not a Client Test
Candidate and does not represent Client or Project acceptance.

## Phase 08B synchronized decisions

- FRONT-D01 is resolved for V0.0.2: the plugin stays enabled when `frontier_iris` is absent or
  unloaded, never creates worlds, and defers Launchpad expiration until the world can be
  classified. A later load and relevant Join, WorldChanged, or Respawn path restores the exact
  case-sensitive player boundary. No health/degraded subsystem is added.
- The exact V0.0.2 world name remains `frontier_iris`; future single-name configurability is
  tracked by Issue #17 and is not implemented here.
- FRONT-D02's Plugin adapter/fail-closed source boundary is accepted with limitation. A temporary
  safe tier and bounded client motion test remain; final motion/range/balance is Mainline/Frontier-
  owned and the temporary values are not a production recommendation.
- FRONT-D04 and MAIN-D08 are accepted with limitation at the supported native/public or
  cancellable boundaries described in the compliance and limitations documents.
- The next gate is ChatGPT's independent review of this remediation. No Client Test Candidate,
  Client/Project acceptance, tag, release, or `requirements_cleared` is fixed.

## Review order

1. Review the Phase 01–06 capability evidence, Phase 07 history, and Phase 08B remediation.
2. Perform ChatGPT's independent review of the Phase 08B diff, focused regression, and exact-head
   CI/Headless evidence.
3. Fix a Client Test Candidate only after that review and later gate authorization; retain the
   FRONT-D02 temporary safe-tier/client-motion boundary.
4. Run bounded Client Acceptance, then perform any separately authorized correction and later
   Stable publication.

## Project-owned follow-up

- Verify LuckPerms leaf behavior with temporary `wayfarer_admin` global `*`; Project OP remains
  disabled.
- Verify exact worlds, Main V004, restart/disable, Safe Entry, provider behavior, Portal boundary,
  and End Gateway observation in a task-approved Project/client environment.
- Own gate coordinates, safe arrival, seed/border/generation, MVI configuration, and any return
  mechanism tracked by Issue #15.
- Review unsupported external protection/repair boundaries and true orphan recovery Issue #16.

The later client result/handoff must record, without secrets or raw Player IDs:

- exact LeafGrapple JAR filename, version, and SHA-256;
- exact temporary tier/config values actually used, including movement/range/display values copied
  from a reviewed LeafGrapple 1.0.2 standard tier;
- durability disabled and entity/player/mob/animal/monster hooking disabled;
- Wayfarer capability detection, canonical item creation/delivery, and identity/death/redelivery
  results;
- bounded client motion observation; and
- an explicit statement that the temporary values are test-only and not a production balance
  recommendation.

Core V0.0.1 remains independently reused. No stable tag, release, deployment, migration, server
restart, or roadmap completion is implied by this handoff.
