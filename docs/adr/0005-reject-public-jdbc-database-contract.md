# ADR 0005: Reject a public JDBC database contract

- Status: Accepted
- Date: 2026-07-28

## Context

The pre-alpha `WayfarerDatabase` stub exposed `java.sql.Connection` through public methods. That
would transfer transaction, lifecycle, threading, and failure authority to Main or Frontier and
would leak JDBC into the stable API. No accepted Main or Frontier consumer used the stub, and the
Core-only alpha.1 runtime published no database capability.

## Decision

`WayfarerDatabase` remains a JDK-only marker whose capability is unavailable in V0.0.1. Public
JDBC `Connection`, Hikari, Flyway, data-source, or Core implementation types are rejected.
`WayfarerServices.database()` fails with the stable unavailable-service behavior.

Any future database capability requires a separate accepted decision and an opaque,
JDK-only asynchronous contract with explicit lookup, completion, timeout, idempotency, disable,
health, and transaction semantics.

## Compatibility

Removing the pre-alpha JDBC methods is source- and binary-incompatible for hypothetical consumers.
There are no accepted Main/Frontier consumers, no released runtime database service, and no
supported compatibility obligation for that stub. The `WayfarerDatabase` class identity remains
in `wayfarer-api`.

## Rejected alternatives

- Restore `Connection` methods: rejected because it leaks JDBC and permits main-thread access.
- Publish Hikari/Flyway types: rejected because those are Core implementation details.
- Invent an opaque beta database API in alpha.2: rejected because its semantics are not yet
  accepted and no consumer requires it.
