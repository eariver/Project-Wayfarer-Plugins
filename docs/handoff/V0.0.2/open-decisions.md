# V0.0.2 Open Decisions

## Plugin review

- B-001 / ADR 0009: approve module-local bounded Hikari/Flyway pools or select another opaque
  boundary. Recommended maximum is three connections per module.
- B-004: approve the durable module-order plus pending fulfillment/at-most-once refund model, or
  require an additive Core transaction-participant contract and `all` release scope.
- FRONT-D02: supply/approve a LeafGrapple 1.0.2 tier with durability and entity hooking disabled.
- FRONT-D03: approve Launchpad creation snapshots.
- FRONT-D04: approve the public protection-hook coverage and disclosed bulk-edit limitation.
- FRONT-D06: approve durable typed Pending Delivery tied to Core transaction IDs.
- MAIN-D07/D08: approve command permission granularity and external repair guard policy.

## Owner approval

- MAIN-D04: Japanese 27-slot status plus separate 27-slot repair confirmation.
- MAIN-D05: Japanese name and concise lore with no internal IDs.
- FRONT-D01: administrative health only when `frontier_iris` is absent.
- FRONT-D05: Japanese 27-slot navigation with Shop/Loadout/Help and explicit unavailable Waystone.

Waystone and EM–MVI are deferred, not open V0.0.2 implementation choices.
