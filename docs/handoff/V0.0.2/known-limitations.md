# V0.0.2 Known Limitations

## Review-blocking limitations

- ADR 0009 is `PLUGIN_REVIEW_REQUIRED`. Main and Frontier have migration drafts, private
  repository contracts, and application/domain code, but no approved concrete module pool or
  production repository lifecycle. Both plugin entry points deliberately disable fail-closed.
- Core V0.0.1 Transactions complete their own debit state before a gameplay module commit.
  Frontier uses a durable pre-payment order plus pending fulfillment design. Main's proposed
  repair path uses a durable claim and at-most-once refund attempt; B-004 must be reviewed before
  runtime integration.
- The examined LeafGrapple 1.0.2 artifact's default `wood` tier enables durability and entity
  hooking. The adapter reports `UNSAFE_CONFIGURATION`; it does not synthesize or fork a hook.
- Supported WorldGuard/WorldEdit/FAWE launchpad protection has not been reviewed. Native event
  guard design cannot guarantee interception of every external bulk edit.

## Accepted/deferred product limitations

- Waystone placement, sale, discovery, teleport, and lifecycle are
  `DEFERRED_BY_REQUIREMENT`. Existing unreleased Frontier V001 scaffold tables are dormant and no
  runtime code registers Waystone behavior.
- The EliteMobs–MVI adapter is not authorized and is not present.
- Normal inventory/profile state remains solely owned by the Minecraft backend/MVI.
- Growth progress is session-cached. After later runtime integration, a crash may lose up to the
  configured checkpoint interval of non-critical progress; evolution, broken, repair, reissue,
  quit, and disable checkpoints remain immediate/bounded design requirements.
- Vault/RedisEconomy `SUCCESS` means the shared provider path accepted an operation, not durable
  Redis completion. There is no provider effect lookup or unconditional exactly-once guarantee.
- `UNKNOWN` effects are not automatically debited or refunded again.

No secret, raw provider object/reference, internal exception message, or stack trace is intended
for player-facing output.
