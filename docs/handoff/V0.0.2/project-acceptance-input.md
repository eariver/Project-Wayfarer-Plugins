# V0.0.2 Project Acceptance Input

Status: pending; not ready for Project Runtime acceptance. Phase 08B independent review is
`PASS`, and `V0.0.2-Client-Candidate-1` is fixed from product source
`90c3f5fe0f02fe297bd6d12f596ce6c9bac27cce`; Client Acceptance has not started.

After the bounded Client Test, Project/client evidence should cover:

- Main delivery, mining/evolution, broken/repair, death with no automatic return, paid Player
  reissue, old-item/old-epoch invalidation, Pending Delivery free retry, and Main V004.
- Main exact allowed worlds and fail-closed behavior.
- Frontier exact `frontier_iris`, initial loadout, permanent-item death redelivery, same
  identity/epoch, Safe Entry/respawn reconciliation, and no Launchpad/Rocket free redelivery.
- Approved temporary test-only LeafGrapple safe tier and bounded client motion; final
  motion/range/balance remains Mainline/Frontier-owned and the temporary values are not a
  production recommendation.
- Exact candidate artifacts: `Wayfarer_Core-V0.0.1.jar` SHA-256
  `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`,
  `wayfarer-main-0.0.2-SNAPSHOT.jar` SHA-256
  `730d56888001e9c76bd127b25c118a937f03a5dd95a0fa381c8c38fec2517113`, and
  `wayfarer-frontier-0.0.2-SNAPSHOT.jar` SHA-256
  `f43829c7b6e06ea44549ffdd1ef26a567aef1563ba73a0808c47634742e9d3ec`.
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
