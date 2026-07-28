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

Core now has a production-valid `WaymarkProviderSource` boundary. The Paper entry point discovers
only a `WayfarerWaymarkProvider` registered under Core's shared, unshaded `wayfarer-api` class
identity. Constructor/fixed sources use the same boundary in integration and headless fixture
tests. Fixture code is test-only and excluded from the production Core candidate.

No current authority proves a safe external plugin load order for registering that shared type
before Core startup. A provider plugin that bundles its own API copy is rejected by class identity,
while an ordinary hard dependency on Core enables after Core and is too late for startup recovery.
Do not guess around this cycle. A future approved adapter may need an owner-defined load contract,
late registration lifecycle, or Core-internal provider factory. Until then, production discovery
returns absent and only provider-independent services are published.

The unmerged alpha.4 SPI and operator detail records are intentionally revised before beta:
resolution is now a structured result and debit/refund effect identities are separate. This is a
source/binary change to an unpublished stacked-branch API with no accepted external consumer.
Contract, boundary tests, beta API inventory, and downstream stacked branches must move together.

## Consequences

alpha.4 provider-independent automated work may pass, but concrete provider completion and stable
release remain `BLOCKED`. An owner-approved, version-pinned thread/failure/reference contract is
required before a concrete adapter or runtime balance/debit/refund test can be added.

ADR 0007 follows up with exact fixed-source and fixed-JAR evidence. It confirms that the current
Vault surface does not satisfy the required contract and records Owner Gate B/C/D and the proposed
resume path.
