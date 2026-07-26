# V0.0.1 Migration and Compatibility

- Config version: Pending
- Highest Core migration: Pending release source inspection
- Migration location: `db/migration/core`
- Database: MariaDB, UTC, `utf8mb4`, `READ COMMITTED`
- First install: validate then migrate Core schema; fail closed on failure
- Existing install: validate immutable applied migrations; apply additions only
- Core scope prohibition: do not create `wf_main_*` or `wf_frontier_*`
- Downgrade: Not supported unless a later approved procedure proves schema compatibility

Record the tested source/target migration versions, backup prerequisites, runtime result, and
rollback limitations before handoff. Never edit an applied Flyway migration.
