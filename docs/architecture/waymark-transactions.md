# Waymark Transaction Boundary

`TransactionEngine` coordinates an external Waymark effect without holding a JDBC transaction
across the provider call. MariaDB `wf_core_transaction` is the durable authority; V003 adds
operation/reconcile metadata and an append-only state history table.

The only supported path is:

```text
prepare/idempotency claim in MariaDB
→ DEBIT_PENDING optimistic claim
→ bounded provider call outside JDBC
→ durable known result, or UNKNOWN
→ automatic/manual reconcile
```

The allowed transitions are centralized in `TransactionStateMachine`. Each write matches state and
`lock_version`; losing a concurrency claim does not call debit or refund. Reusing an idempotency key
with different immutable input fails closed. Provider references and failure codes are bounded and
sanitized before persistence.

The provider SPI contains only JDK types and explicit success, known failure, insufficient-funds,
unknown-effect, and resolution outcomes. Timeouts after a possible effect become `UNKNOWN`; the
system does not retry the debit and does not claim unconditional exactly-once behavior.

Redis locks/idempotency hints may reduce contention but never authorize an effect. Provider balance
remains provider authority, and transaction/reconcile history remains MariaDB authority.

Concrete RedisEconomy/Vault invocation is blocked by ADR 0006 until its immutable thread, timeout,
error, and reference lookup contract is established.
