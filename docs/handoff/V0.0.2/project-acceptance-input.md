# V0.0.2 Project Acceptance Input

Status: pending; not ready for Project Runtime acceptance. Phase 08B independent review is
`PASS`; Phase 10B-A Candidate-1 is a retained `CLIENT_TEST_FAIL` and is rejected for promotion.
Candidate-2 is prepared for focused Client retest from product source
`f2281093a03c17be0b0e69004059dd7ccb072b1c`; full Client Acceptance has not passed.

After the bounded Client Test, Project/client evidence should cover:

- Main delivery, mining/evolution, broken/repair, death with no automatic return, paid Player
  reissue, old-item/old-epoch invalidation, Pending Delivery free retry, and Main V004.
- Main exact allowed worlds and fail-closed behavior.
- Frontier exact `frontier_iris`, initial loadout, permanent-item death redelivery, same
  identity/epoch, Safe Entry/respawn reconciliation, and no Launchpad/Rocket free redelivery.
- Approved temporary test-only LeafGrapple safe tier and bounded client motion; final
  motion/range/balance remains Mainline/Frontier-owned and the temporary values are not a
  production recommendation.
- Exact Candidate-2 artifacts: `Wayfarer_Core-V0.0.1.jar` SHA-256
  `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`,
  `wayfarer-main-0.0.2-SNAPSHOT.jar` SHA-256
  `5b40dd4b66ab5fd15b9b89f30e5db09923759171e1f69429ab3ff669120ab36b`, and
  `wayfarer-frontier-0.0.2-SNAPSHOT.jar` SHA-256
  `1559af0ebebb664a4f29dd08df41228fc9dfd9df1930da469b877075d829033d`.
- Launchpad current-view direction, Sneak behavior, cooldown, uses, auto-Elytra, current config,
  restart/reconcile, and supported protection behavior.
- Phase 06 permission leaves through LuckPerms, authorized/denied routes, no top-level command
  permission, no dependency on Project OP, and temporary `wayfarer_admin` global `*` behavior.
- Restart/disable, sanitized output, provider/transaction limitations, and no cross-backend item
  transfer.
- Portal boundary: the implemented `PlayerPortalEvent` behavior; End Gateway is an observation,
  not a proven separate interception.
- Absence of Waystone production behavior and EM–MVI adapter/deferred features.

Project configuration, database/Redis, worlds, players, and Runtime operations are Project-owned.
This repository has not installed plugins, changed Project configuration, run Project migrations,
restarted servers, or connected to Project Runtime.

The later client result must include the exact LeafGrapple JAR filename/version/SHA-256, exact
temporary tier/config values actually used, copied reviewed-1.0.2 movement/range/display values,
durability-disabled and entity/player/mob/animal/monster-hooking-disabled results, Wayfarer
capability detection, canonical item creation/delivery, identity/death/redelivery, and bounded
motion observation. It must explicitly state that the temporary values are test-only and not a
production balance recommendation.
