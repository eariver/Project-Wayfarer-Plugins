# V0.0.2 Known Limitations

## Transaction and persistence

- Core transaction state and module domain persistence are not cross-store atomic. Unconditional
  exactly-once is not claimed.
- `UNKNOWN` is an explicit recovery state; it is never automatically debited, refunded, or
  retried.
- Paid Main reissue may rotate authority before physical delivery. A Pending Delivery is then
  retried free; it never causes a second debit or rotation.
- Growth progress is session-cached. A crash may lose up to the configured checkpoint interval
  of non-critical progress.
- Normal inventories/profile state remains solely owned by the Minecraft backend/MVI; no raw
  Inventory or ItemStack is persisted in this repository's MariaDB schema.

## External integration

- The examined LeafGrapple 1.0.2 default tier is not a verified safe tier. Without an approved
  safe tier, the adapter fails closed and does not synthesize/fork hook physics.
- Native/public Launchpad protection paths are covered at the supported boundary. External tools
  that bypass Bukkit and the supported WorldEdit/WorldGuard APIs are not claimed covered; this is
  the FRONT-D04 review limitation.
- Native repair guards are implemented. External repair plugins without a supported cancellable
  event remain a review limitation.

## Gameplay and presentation boundaries

- Main death removes managed Growth Tool/Broken Tool from drops and does not automatically restore
  it. Player paid reissue is the recovery route.
- Frontier Elytra/Grappling Hook/Navigation use typed durable same-identity/epoch free redelivery;
  Launchpad/Rocket are excluded from permanent free redelivery.
- Launchpad uses the current Player view direction at use time. Current config controls performance;
  persisted yaw is reserved/non-authoritative. A separate physical-material or full immutable
  performance snapshot is not claimed.
- Portal handling is exact: `FrontierGameplayRuntime` cancels `PlayerPortalEvent` when the
  Player's current world is `frontier_iris`. End Gateway is an observation for client/Project
  testing, not a proven separate interception.
- Issue #15 tracks the in-world Frontier return mechanism; Issue #16 tracks true orphan
  `BLOCK_ONLY` recovery.
- Japanese localization and GUI/presentation tuning are deferred; current English presentation is
  accepted for V0.0.2.

## Deferred and acceptance boundaries

- Waystone behavior and the EM–MVI adapter are absent/deferred; the adapter requires a Project
  decision of `ADAPTER_REQUIRED` before creation.
- FRONT-D01, FRONT-D02, FRONT-D04, and MAIN-D08 remain review/external gates.
- No Client Acceptance, Project acceptance, Client Test Candidate, or Stable Release is claimed.
- Project Runtime, permissions groups, configuration, worlds, databases, migrations, and servers
  were not changed by this repository task.

No secret, raw provider object/reference, internal exception message, or stack trace is intended
for Player-facing output.
