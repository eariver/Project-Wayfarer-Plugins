# ADR 0006: Keep the Concrete Waymark Provider Behind an Authority Gate

- Status: Accepted for the alpha.4 stacked branch
- Date: 2026-07-28

## Context

Project inventory fixes RedisEconomy at local source commit
`581091a` and VaultUnlocked at 2.20.2. Read-only inspection establishes a synchronous Vault
economy surface, but does not provide an immutable provider statement for safe off-main-thread
calls, timeout/cancellation behavior, or crash-window reference lookup. A local binary or config
cannot establish that contract by inference.

## Decision

The provider-independent transaction engine, MariaDB repository, reconcile logic, public JDK-only
provider SPI, and deterministic fixture provider proceed. No concrete RedisEconomy/Vault adapter is
created. The engine never accesses RedisEconomy keys, Redis balances, or Vault implementation types.

Enabling Waymark without an explicitly injected verified provider fails closed. Production config
remains disabled. Redis remains coordination assistance and MariaDB remains transaction authority.

## Consequences

alpha.4 provider-independent automated work may pass, but concrete provider completion and stable
release remain `BLOCKED`. An owner-approved, version-pinned thread/failure/reference contract is
required before a concrete adapter or runtime balance/debit/refund test can be added.
