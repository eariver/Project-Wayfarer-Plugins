# ADR 0004: Adopt an asynchronous JDK-only item identity contract

- Status: Accepted
- Date: 2026-07-27

## Context

The original `WayfarerItemIdentity.newIdentity` stub created an in-memory UUID synchronously. It
could report success before durable storage, had no lookup or raw-claim validation operation, and
could not express malformed or missing PDC values. No accepted external Main or Frontier consumer
exists for that stub; alpha.1 released Core only and the method remained unavailable at runtime.

Item identity is MariaDB-authoritative and every JDBC operation is asynchronous. Validation
failures are security-relevant and require durable audit before the caller may receive an invalid
domain result.

## Decision

`WayfarerItemIdentity` provides:

```text
CompletionStage<Identity> create(CreateRequest)
CompletionStage<Optional<Identity>> find(UUID)
CompletionStage<ValidationResult> validate(ValidationRequest)
```

All public types use only JDK classes. `RawClaim` keeps the six PDC values nullable and unparsed so
missing fields and malformed UUIDs can fail closed. Requests defensively copy allowed item types
and supported schema versions.

`create` validates first, generates a test-injectable UUID internally, inserts the identity, and
only then completes successfully. A generated-UUID collision is an explicit conflict; there is no
unbounded retry. `find` and `validate` execute through the managed asynchronous database boundary.
Caller validation failures are domain results. Database, executor, and required-audit failures
complete exceptionally.

Every invalid validation result records `ITEM_IDENTITY_VALIDATION_FAILED`. The audit contains only
the failure reason, a successfully parsed item UUID, and a syntactically safe item type. If that
audit fails, validation completes exceptionally so the item operation remains rejected.

## Compatibility

This is a source- and binary-incompatible revision of the unimplemented stub. The revision is
accepted before any Main/Frontier consumer or identity service publication. API class identity is
unchanged: the contract still belongs only to `libraries:wayfarer-api`.

Main and Frontier must migrate by snapshotting the six string/number PDC claims on the Paper main
thread, constructing an immutable `ValidationRequest`, awaiting it asynchronously, and performing
any later Bukkit mutation on the main thread after revalidation.

## Rejected alternatives

- Synchronous create/find: rejected because it would require main-thread JDBC or false success.
- UUID-only typed input: rejected because it cannot represent malformed or absent PDC values.
- Lore/name/material identity: rejected because presentation data is forgeable and non-authority.
- Silent audit failure: rejected because a critical invalid identity could otherwise appear safe.
- Paper/PDC types in the API module: rejected to preserve API class identity and backend isolation.
