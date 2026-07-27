# V0.0.1 Migration and Compatibility

- Config version: 1
- Highest Core migration: V003 (`V003__core_transaction_history.sql`)
- Migration location: `db/migration/core`
- Database: MariaDB, UTC, `utf8mb4`, `READ COMMITTED`
- First install: validate then migrate Core schema; fail closed on failure
- Existing install: validate immutable applied migrations; apply additions only
- V001/V002 remain byte-for-byte immutable; V003 adds transaction operation/reconcile metadata and
  `wf_core_transaction_event`
- Core scope prohibition: do not create `wf_main_*` or `wf_frontier_*`
- Downgrade: Not supported unless a later approved procedure proves schema compatibility

Record the tested source/target migration versions, backup prerequisites, runtime result, and
rollback limitations before handoff. Never edit an applied Flyway migration.
