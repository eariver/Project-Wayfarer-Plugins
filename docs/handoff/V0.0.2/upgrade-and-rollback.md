# V0.0.2 Upgrade and Rollback

This is a future handoff procedure, not authorization to publish or operate Project Runtime.

## Upgrade

1. Do not proceed until the review gates are cleared and a Client Test Candidate is explicitly
   fixed.
2. Verify the exact source commit, release scope, and later-authorized artifact hashes.
3. Reuse immutable Core V0.0.1 for `main-frontier`; do not rename it as V0.0.2.
4. Apply Main migrations through V004 and the Frontier migrations at their current source level
   only through Project-owned, separately authorized Runtime operations.
5. Project integration owns backups, configuration, migration execution, placement, restart, and
   acceptance.
6. Place Main only on Main and Frontier only on Frontier; preserve the Phase 06 leaf permission
   model and temporary Project Admin behavior.

## Rollback/removal

- Disable/remove the affected gameplay module; do not delete or downgrade its schema.
- Never reverse or edit an applied Flyway migration.
- Preserve paid reissue, Pending Delivery, purchase, delivery, placement, and launchpad records
  for state-aware reconciliation.
- A rollback from Main V004 or Frontier's current applied level to older code is unsupported after
  the migration is applied.
- Core V0.0.1 remains independently usable because no Core API or migration was changed.
- JAR removal is not schema rollback.
- Project inventory/profile rollback remains an MVI/backend operation, not a Wayfarer database
  operation.
