# ADR 0007: Use RedisEconomy Through the Shared Vault Economy Boundary

- Status: Accepted — Owner Decision
- Date: 2026-07-29
- Owner decision: Gate B/C/D resolved
- Plugin repository base: `16ce48dd0cdf905c5fcff5430b54459a72173e4a`
- Project authority source: `344eedc738d75954daa43facfeef302944f2963a`
- Fixed RedisEconomy source: `581091a121ac155a830b927b3a8af6cbc4de2946`
- Follows: ADR 0006

## Context

The Project fixes the following economy artifacts:

| Artifact | Version | SHA-256 |
|---|---|---|
| RedisEconomy | `4.5.12-wayfarer.1` | `AB00270CD970A909F54F6EE7C2C47151FB90DB0EA36FA6AB68AC59D939CFCA47` |
| VaultUnlocked | `2.20.2` | `BD9E7A31F1B2D31A591497174887EEA7AE7E632C6B179DA13E4F0AD732DE2DF7` |

The designated RedisEconomy source worktree includes a determinable compatibility patch with Git
patch ID `edd629de328edf205ddce5a6e2692fbf8c9a8c3b`. It changes the Paper compatibility path and build
version, not the economy operations assessed here. Neither that source nor the Project repository
was modified.

The fixed source establishes:

- RedisEconomy registers its default `Currency` as the Vault `Economy` service during enable.
- Vault `getBalance`, `withdrawPlayer`, and `depositPlayer` use `OfflinePlayer`.
- RedisEconomy updates its local cache and accepts asynchronous Redis work before returning Vault
  `SUCCESS`.
- insufficient funds returns an explicit Vault `FAILURE` before mutation;
- the Vault surface accepts no Wayfarer operation ID and exposes no effect lookup;
- balance comparison cannot identify a particular operation;
- the debit cache read/check/write is not one cross-server atomic conditional operation.

Therefore Vault `SUCCESS` is not proof of durable Redis persistence, provider transaction-record
completion, or exactly-once external effect.

## Owner decision

For V0.0.1, Wayfarer uses RedisEconomy through the same standard Vault Economy boundary used by
EvenMoreFish and EconomyShopGUI. No RedisEconomy-specific Waymark API, class dependency, or internal
Redis-key access is added.

The approved path is:

```text
EvenMoreFish / EconomyShopGUI / Wayfarer_Core private adapter
→ Vault Economy API
→ RedisEconomy Economy provider
→ shared Waymark balance
```

The common consumer path and its shared semantics take priority over giving Wayfarer a stronger
private side channel.

## Core boundary and discovery

- `WayfarerWaymarkProvider`, public API, and `TransactionEngine` remain JDK-only.
- Vault, Bukkit, `OfflinePlayer`, and `EconomyResponse` types are private to the Core adapter.
- Player authority, transaction identity, and idempotency remain UUID-based. Player names never
  become authority.
- Core discovers Vault `Economy` through Bukkit `ServicesManager`.
- The selected provider must safely identify as the configured `RedisEconomy`; health reports only
  `Vault/RedisEconomy`, never a raw provider object.
- Vault calls are bridged to the Paper main thread. The fixed source adds no synchronous Redis I/O
  to that call; Redis work is accepted on RedisEconomy-owned executors.
- `OfflinePlayer` is resolved from UUID within the main-thread call and is not retained as a Core
  asynchronous domain object.

## Result mapping

| Vault/provider observation | SPI result |
|---|---|
| explicit Vault `SUCCESS` | `SUCCEEDED` |
| fixed explicit insufficient-funds `FAILURE` | `INSUFFICIENT_FUNDS` |
| other explicit Vault `FAILURE` before a successful effect response | `KNOWN_FAILURE` |
| null response, provider exception, timeout, dispatch/disable race after possible invocation | `UNKNOWN` through the engine |
| no operation-ID effect lookup | `resolve = UNKNOWN` |

No provider reference is synthesized. Raw provider messages and exceptions are not exposed.
Timeout before the queued main-thread call begins prevents the call. Once invocation may have
started, ambiguity is not converted to `NOT_APPLIED`, and the engine does not automatically repeat
debit or refund.

## Accepted provider limitation

V0.0.1 interprets Vault `SUCCESS` only as:

> the current common Vault/RedisEconomy route accepted the operation as successful.

It does not establish durable Redis completion, a provider transaction record, atomic operation
identity, effect lookup, cross-server exactly-once, or crash-window resolution. The owner explicitly
accepts this limitation because all current economy consumers should use the same route and
semantics.

Core still guarantees unique idempotency keys, one provider call per winning durable operation,
normal duplicate debit/refund prevention, `UNKNOWN` for unprovable outcomes, no automatic repeat
from `UNKNOWN`, and manual reconcile/audit. Core does not claim exactly-once.

## Consequences

- Core may implement a private Vault adapter without changing the SPI, engine, repository, or
  V001–V003.
- Missing, disabled, unexpected, or failed provider discovery leaves Transaction and Waymark
  unavailable while provider-independent services remain available.
- Startup provider verification/recovery is asynchronous so Paper's main thread remains available
  for Vault calls. Provider-dependent capabilities become available only after verification and
  recovery complete.
- Gate B/C/D is resolved for V0.0.1 by explicit Owner acceptance, not by stronger technical
  guarantees.

## Deferred design item

The current Vault/RedisEconomy route cannot align `EconomyResponse.SUCCESS` with durable Redis
completion and supplies no atomic operation ID, effect lookup, or crash-window resolution. This
affects every economy consumer, including EvenMoreFish and EconomyShopGUI.

If stronger guarantees are required, improve or replace Vault, RedisEconomy, or both with an
economy boundary all consumers can share. Do not add a Wayfarer-only RedisEconomy side channel.
The Project owner should transfer this item to a Project-side deferred-design record such as
`docs/11-deferred-design-items.md`.

## Rollback

Disable Waymark or restore the prior verified Core artifact. No schema rollback is required because
this decision adds no migration. Runtime deployment, configuration, restart, and rollback remain
Project-owned.
