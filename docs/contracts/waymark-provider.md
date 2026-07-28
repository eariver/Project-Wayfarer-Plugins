# Waymark Provider SPI Contract

`WayfarerWaymarkProvider` is a JDK-only capability boundary:

- `probe` reports availability and a safe provider identifier.
- `balance` reads provider authority.
- `debit` and `refund` accept stable, separately persisted operation IDs.
- `resolve` receives the effect kind and stable operation ID, then returns a structured resolution:
  `APPLIED`, `NOT_APPLIED`, or `UNKNOWN`, a safe bounded nullable provider reference, and a safe
  bounded nullable failure code.
- every method returns a `CompletionStage`; blocking, timeout, and scheduling policy belong to the
  adapter contract and must be verified before an adapter is enabled.

An adapter must never expose Vault, RedisEconomy, Bukkit, Redis, or provider implementation types.
It must not access RedisEconomy internal keys. Unknown/timeout results must not be converted to
known failure or success. Provider references must not be guessed or synthesized.

The alpha.4 fixture can reproduce success, insufficient funds, timeout before/after effect, known
failure, unknown effect, outage, resolution outcomes, restart, and duplicate request. It uses the
same provider-source seam as production Core tests, is excluded from the runtime candidate, and is
test authority only—not a runtime economy.

Production discovery uses the shared unshaded `wayfarer-api` class identity through Bukkit
ServicesManager. No authorized concrete provider currently satisfies the required classloader/load
order and immutable behavior contract, so absence fails closed and ADR 0006 remains `BLOCKED`.
