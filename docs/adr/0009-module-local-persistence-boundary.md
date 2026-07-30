# ADR 0009: Module-local opaque persistence boundary

- Status: Accepted — Plugin owner decision 2026-07-30
- Date: 2026-07-30
- Scope: Wayfarer_Main and Wayfarer_Frontier V0.0.2

## Context

ADR 0005 deliberately rejects a public JDBC database contract. `WayfarerDatabase` therefore
remains an unavailable marker and the V0.0.1 public API cannot expose `Connection`, Hikari,
Flyway, Bukkit, or other implementation types. Main and Frontier nevertheless require
module-owned `wf_main_*` and `wf_frontier_*` durable state and Flyway lifecycle control.

The V0.0.2 execution instruction requires an explicit review gate before adopting a non-trivial
database boundary or multiple pools.

## Options assessed

1. Public opaque Core database API: rejected for V0.0.2 because it expands the stable Core API
   and concentrates module-specific transaction semantics in Core.
2. Module-local bounded Hikari/Flyway lifecycle: recommended. Each gameplay plugin owns only its
   prefix, migrations, repositories, queue, drain timeout, and health. A private common helper may
   remove mechanical duplication but is not a service contract.
3. Core internal extension used by sibling modules: rejected because Java visibility would either
   be brittle or become an undeclared cross-module API.
4. JDBC/Hikari objects through `WayfarerDatabase`: rejected by ADR 0005 and the V0.0.2 authority.

## Decision

Use option 2:

- Main and Frontier independently resolve sanitized secret references from their own config.
- Each uses a small, bounded pool and its own Flyway location.
- Defaults are maximum pool size 3, minimum idle 0, and 5000 ms connection timeout.
- Core retains `flyway_schema_history`; Main uses `wf_main_flyway_schema_history`; Frontier
  uses `wf_frontier_flyway_schema_history`.
- All database work runs through `WayfarerTasks.database`; no main-thread database I/O.
- Each module owns enable migration, work admission, bounded disable drain, and fail-closed health.
- Repository/domain types remain private to their module.
- Core V0.0.1 public API and Core V001–V003 remain unchanged.
- Cross-module atomicity is not claimed. Core Transactions remain the Waymark debit/refund
  authority and module commits retain explicit recovery state.

## Consequences

- Two additional bounded pools may exist on a server hosting both modules. Production deployment
  normally places Main and Frontier on different backends, but the pool count is still a material
  operational choice.
- Pool sizing and aggregate MariaDB connection budget require reviewer confirmation.
- Module migration failure disables only the affected module.
- Pure domain logic, repository contracts, config validation, tests, packaging, and other
  independent work may proceed.

The decision was implemented without changing the Core public API or Core V001–V003.
