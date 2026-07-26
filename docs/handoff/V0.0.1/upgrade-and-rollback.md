# V0.0.1 Upgrade and Rollback

## Upgrade

1. Project integration verifies the immutable release tag, source commit, and SHA-256.
2. Back up authoritative MariaDB data and other Project-owned state under a separate approved task.
3. Review config/migration compatibility and required environment variables.
4. Stop and deploy using the Project-owned runbook; this repository does not perform deployment.
5. Verify enable, migration, services, health, and a focused smoke test.

## Rollback

Do not move tags or overwrite assets. Code rollback uses a previously verified immutable release
only when schema/config compatibility permits it. Flyway rollback is not automatic; applied
migrations are not reversed or edited. Restore decisions and runtime removal are Project-owned and
require backups and explicit approval.
