# V0.0.2 Known Limitations

## Review-blocking limitations

- ADR 0009 and B-004 are owner-approved and implemented with module-local pools, separate
  histories, durable pre-payment records, CAS claims, pending fulfillment, and UNKNOWN-no-retry.
- Core V0.0.1 Transactions and module domain commits are not cross-store atomic. Manual
  inspection remains required for UNKNOWN outcomes; unconditional exactly-once is not claimed.
- The examined LeafGrapple 1.0.2 artifact's default `wood` tier enables durability and entity
  hooking. The adapter reports `UNSAFE_CONFIGURATION`; it does not synthesize or fork a hook.
- Native explosion, piston, fluid, fire, physics, growth, and entity-change launchpad guards are
  implemented. Public WorldGuard/WorldEdit/FAWE bulk-edit coverage remains an external review
  gate; unsupported bulk edits may require reconcile.

## Accepted/deferred product limitations

- Waystone placement, sale, discovery, teleport, and lifecycle are
  `DEFERRED_BY_REQUIREMENT`. Existing unreleased Frontier V001 scaffold tables are dormant and no
  runtime code registers Waystone behavior.
- The EliteMobs–MVI adapter is not authorized and is not present.
- Normal inventory/profile state remains solely owned by the Minecraft backend/MVI.
- Growth progress is session-cached. A crash may lose up to the configured checkpoint interval of
  non-critical progress; evolution, broken, repair, reissue, quit, and disable checkpoints use
  immediate or bounded persistence paths.
- Native anvil, grindstone, smithing, crafting, and mending repair paths are denied for the
  Growth Tool. External repair plugins without a supported cancellable event remain a review
  matrix item and must not be assumed safe.
- Vault/RedisEconomy `SUCCESS` means the shared provider path accepted an operation, not durable
  Redis completion. There is no provider effect lookup or unconditional exactly-once guarantee.
- `UNKNOWN` effects are not automatically debited or refunded again.

No secret, raw provider object/reference, internal exception message, or stack trace is intended
for player-facing output.
