# V0.0.1 Migration and Compatibility

- Config version: 1
- Highest Core migration: V003 (`V003__core_transaction_history.sql`)
- Migration location: `db/migration/core`
- Database: MariaDB, UTC, `utf8mb4`, `READ COMMITTED`
- First install: validate then migrate Core schema; fail closed on failure
- Existing install: validate immutable applied migrations; apply additions only
- V001/V002 remain byte-for-byte immutable.
- Obsolete pre-merge V003 identity: SHA-256
  `3f0ba44065b4fd3d0139048e9758b457f64c5ce3d2bd29de134e7d170f786954`, Git blob
  `cb3152468143c3a2818ace767e57efcd19a3bf0e`.
- Current stacked candidate V003 identity: SHA-256
  `83483e0494b687ec4ff11af7872ae89e1406bb678ae2b000897fc66ff7a048b4`, Git blob
  `f4f0b195318fa79518f331c0f519272c96c40ff3`.
- Current V003 separates durable debit/refund operation IDs and references, adds bounded recovery
  claim metadata/indexing, and makes `wf_core_transaction_event` the atomic append-only transition
  authority.
- Core scope prohibition: do not create `wf_main_*` or `wf_frontier_*`
- Downgrade: Not supported unless a later approved procedure proves schema compatibility
- Public pre-release compatibility: rc.3 changes only Waymark balance reads from
  `CompletionStage<Long>` to `CompletionStage<BigDecimal>`. This is source/binary incompatible
  with an rc.2 consumer compiled against the unpublished API, so API consumers must compile
  against the selected final Core artifact. Effect and database amounts remain `long`.
- Config version remains 1 and V001–V003 are unchanged by the fractional-balance correction.

Record the tested source/target migration versions, backup prerequisites, runtime result, and
rollback limitations before handoff. Never edit an applied Flyway migration.
