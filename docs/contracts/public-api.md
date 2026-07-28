# V0.0.1 Public API Contract

All contracts are loaded through the unshaded `wayfarer-api` identity published by
`Wayfarer_Core`. Public signatures contain only JDK and `io.github.eariver.wayfarer.api` types.

| Contract | Lookup | Thread/completion | Timeout/failure/disable |
|---|---|---|---|
| `WayfarerServices` | Bukkit ServicesManager | snapshot/service access on caller thread | unavailable components throw; lookup is invalid after unpublish |
| `WayfarerDatabase` | `services.database()` | marker only; no JDBC operations | deliberately unavailable under ADR 0005 |
| `WayfarerAudit` | `services.audit()` | asynchronous durable completion | rejected after persistence intake closes; failure is exceptional |
| `WayfarerTransactions` | `services.transactions()` | asynchronous; provider calls outside JDBC | explicit timeout/UNKNOWN; idempotent key; unavailable without provider |
| `WayfarerWaymark` | `services.waymark()` | asynchronous provider boundary | provider-defined result; unavailable without verified provider |
| `WayfarerItemIdentity` | `services.itemIdentity()` | async immutable request/result | unknown/mismatch claims fail closed; disabled intake rejects |
| `WayfarerTasks` | `services.tasks()` | bounded worker and main-thread bridge | overflow/late callback exceptional; stale revalidation is not applied |
| `WayfarerHealth` | direct service and `services.health()` | immutable snapshot | reports component degradation/down/disabled without secrets |

`WayfarerTransactions.TransactionDetails` exposes separate debit operation/reference and refund
operation/reference fields. It does not expose the raw payload or collapse the two provider effects
into one ambiguous reference. Inspection is presence-safe at the command boundary.

`WayfarerWaymarkProvider` is an adapter SPI, not a published runtime service. Debit and refund use
stable, separate operation IDs. Resolution identifies the effect kind and returns structured
`APPLIED`, `NOT_APPLIED`, or `UNKNOWN` status with a bounded nullable reference and failure code.
Concrete provider availability remains governed by ADR 0006.

Forbidden public types are covered by `PublicApiBoundaryTest`: Hikari, Flyway, Lettuce, JDBC,
Paper/Bukkit, Redis authentication, provider implementation, Main/Frontier domain, and gameplay
objects.
