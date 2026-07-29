# V0.0.2 Upgrade and Rollback

## Upgrade

1. Do not proceed until the review/readiness gates are cleared and a candidate is fixed.
2. Verify every published SHA-256 and the exact release scope.
3. Reuse immutable Core V0.0.1 for `main-frontier`; do not rename it as V0.0.2.
4. Project integration owns backups, configuration, migration execution, placement, restart, and
   acceptance.
5. Place Main only on Main and Frontier only on Frontier.

## Rollback/removal

- Disable/remove the affected gameplay module; do not delete or downgrade its schema.
- Never reverse or edit an applied Flyway migration.
- Preserve pending repair, purchase, delivery, placement, and launchpad records for reconciliation.
- A rollback from module V002 code to V001 code is unsupported after V002 is applied.
- Core V0.0.1 remains independently usable because no Core API or migration was changed.
- Project inventory/profile rollback remains an MVI/backend operation, not a Wayfarer database
  operation.
