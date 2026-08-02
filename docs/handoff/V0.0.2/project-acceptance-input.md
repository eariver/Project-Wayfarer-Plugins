# V0.0.2 Project Acceptance Input

Status: pending; not ready for Project Runtime acceptance. Candidate-1, Candidate-2, and
Candidate-3 are retained rejected evidence. Candidate-4 is prepared for focused Client retest
from Product HEAD `9fe86d2e787ab1f86dcf38a5abdba6168515a802`; full Client Acceptance has not
passed.

After the bounded Client Test, Project/client evidence should cover:

- Main delivery, mining/evolution, broken/repair, death with no automatic return, paid Player
  reissue, old-item/old-epoch invalidation, Pending Delivery free retry, and Main V004.
- Main exact allowed worlds and fail-closed behavior.
- Frontier exact `frontier_iris`, initial loadout, permanent-item death redelivery, same
  identity/epoch, Safe Entry/respawn reconciliation, and no Launchpad/Rocket free redelivery.
- Approved temporary test-only LeafGrapple safe tier and bounded client motion; final
  motion/range/balance remains Mainline/Frontier-owned and the temporary values are not a
  production recommendation.
- Exact Candidate-4 artifacts: `Wayfarer_Core-V0.0.1.jar` SHA-256
  `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2`,
  `wayfarer-main-0.0.2-SNAPSHOT.jar` SHA-256
  `c263f6957c69bf958b6374e37efbf0cff7cc0e21d27530acf7faa46cd1b54522`, and
  `wayfarer-frontier-0.0.2-SNAPSHOT.jar` SHA-256
  `7897c31bdc69e05112e286235658364d2771ab875113f9410341b6d9910e1bac`.
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
