# Waymark Provider SPI Contract

`WayfarerWaymarkProvider` is a JDK-only capability boundary:

- `probe` reports availability and a safe provider identifier.
- `balance` reads provider authority.
- `debit` and `refund` accept stable operation IDs.
- `resolve` classifies a previous effect as applied, not applied, or unknown.
- every method returns a `CompletionStage`; blocking, timeout, and scheduling policy belong to the
  adapter contract and must be verified before an adapter is enabled.

An adapter must never expose Vault, RedisEconomy, Bukkit, Redis, or provider implementation types.
It must not access RedisEconomy internal keys. Unknown/timeout results must not be converted to
known failure or success.

The alpha.4 fixture can reproduce success, insufficient funds, timeout before/after effect, known
failure, unknown effect, outage, and duplicate request. It is test authority only and is not a
runtime economy.
