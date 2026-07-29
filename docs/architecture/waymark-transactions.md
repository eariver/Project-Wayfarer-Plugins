# Waymark Transaction Boundary

`TransactionEngine` coordinates external Waymark effects without holding a JDBC transaction
across a provider call. MariaDB `wf_core_transaction` and its append-only
`wf_core_transaction_event` history are the durable state authority. Provider balance and effect
lookup remain provider authority; Redis remains a coordination hint only.

The only supported path is:

```text
prepare/full-input idempotency claim in MariaDB
→ persist stable debit operation ID
→ DEBIT_PENDING optimistic claim and durable event
→ bounded provider call outside JDBC
→ durable known result, or UNKNOWN
→ automatic/manual reconcile
```

Debit and refund have separate durable operation IDs and provider references. A refund operation ID
and intended refund terminal state are persisted with the winning `REFUND_PENDING` transition
before the provider is called. Restart and retry never generate a replacement operation ID.

The allowed transitions are centralized in `TransactionStateMachine`. Each state write matches
state and `lock_version`; a recovery resolver additionally acquires a bounded durable claim. Losing
either claim does not call debit, refund, or provider resolution. Reusing an idempotency key requires
null-safe exact equality of transaction type, actor UUID, subject type, subject ID, amount, and raw
payload string. JSON whitespace and field-order differences therefore fail closed.

## Recovery matrix

| Persisted state | Recovery behavior |
|---|---|
| `PREPARED` | Claim `DEBIT_PENDING`, then perform the first debit with the persisted debit operation ID |
| `DEBIT_PENDING` | Resolve the debit operation; `APPLIED` continues from `DEBITED`, `NOT_APPLIED` fails, and `UNKNOWN` becomes/stays `UNKNOWN` |
| `DEBITED` | Do not call the provider; claim `DOMAIN_COMMIT_PENDING` and continue |
| `DOMAIN_COMMIT_PENDING` | Do not call the provider; complete `COMMITTED` |
| `REFUND_PENDING` | Resolve the refund operation; never replace its operation ID or debit reference |
| `UNKNOWN` | Resolve the persisted refund operation when present, otherwise the persisted debit operation |

Startup recovery runs only after migration and a successful verified-provider probe. It scans a
bounded batch and uses bounded overall and per-provider timeouts. Core publishes its
provider-independent services first, then performs provider verification and recovery on the
bounded executor so Paper's main thread can service Vault calls. Transaction/Waymark capability
getters remain unavailable and health remains `UNKNOWN` until recovery completes. A missing or
failed provider leaves those capabilities unavailable with `DOWN` health.

## Durable event and general audit

Every prepare, transition, and recovery-claim update writes the new state/version, debit/refund
metadata, failure code, claim metadata, and an append-only transaction event in one MariaDB
transaction. A failed event insert rolls the state update back.

`wf_core_audit` remains a general mirror and continues to cover critical admin requests. A mirror
failure after a durable transaction transition marks Audit health `DOWN` and emits only a sanitized
warning; it does not undo or stall the state machine and cannot authorize a duplicate provider
effect. Permission denial, inspect, and explicit reconcile request audit retain their critical
admin policy.

The provider SPI contains only JDK types and explicit success, known failure, insufficient-funds,
unknown-effect, and structured resolution results. Resolution returns `APPLIED`, `NOT_APPLIED`, or
`UNKNOWN` plus a safe bounded nullable provider reference and failure code. References are never
guessed or synthesized. Timeouts after a possible effect become `UNKNOWN`; the system does not
retry the effect and does not claim unconditional exactly-once behavior.

Redis locks/idempotency hints may reduce contention but never authorize an effect. Provider balance
remains provider authority, and transaction/reconcile history remains MariaDB authority.

Balance reads cross the public API/SPI as JDK `BigDecimal` so a shared Vault balance such as 37.5
is preserved. Transaction, debit, and refund amounts remain integral positive `long` values in
Core and V003. This read-contract correction changes no state transition, repository column,
migration, idempotency identity, or effect classification.

The concrete adapter follows ADR 0007: Bukkit ServicesManager → Vault `Economy` → the selected
RedisEconomy provider. UUID remains Core authority; `OfflinePlayer` exists only inside the
main-thread Vault call. Explicit Vault success/failure is mapped from the fixed source, while a
provider exception, timeout, null/ambiguous result, or uncertain disable race becomes `UNKNOWN`.
Because Vault has no caller operation ID or effect lookup, concrete `resolve` remains `UNKNOWN` and
no provider reference is synthesized.

The Owner accepts that Vault `SUCCESS` means only that the common path accepted the operation; it
does not prove durable Redis completion. The state machine still prevents normal duplicate effects
and never automatically repeats an `UNKNOWN` debit/refund, but it does not claim external
exactly-once.
