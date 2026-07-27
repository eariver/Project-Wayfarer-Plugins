# Durable Audit and Identity

## Authority and threading

MariaDB is the durable authority for `wf_core_audit`, player UUID snapshots, and common item
identity. Player names are auxiliary and non-unique. Normal inventories, NBT, lore, display name,
material, authentication, and permissions are not stored. Redis, MVI, Waymark, Main, and Frontier
authority are unchanged.

Paper handlers snapshot immutable JDK values on the main thread. `InternalDatabase` accepts the
work and performs all JDBC access on the managed executor. No `Player`, `ItemStack`,
`PersistentDataContainer`, JDBC, Hikari, or Flyway type crosses the public API boundary.

## Durable audit

`WayfarerAudit.record` is asynchronous. It validates event/subject/server identifiers, truncates
timestamps to milliseconds, parses JSON, and enforces a 16 KiB UTF-8 details limit before any
database work. Sensitive key tokens, resolved config-secret values, JDBC/Redis URIs,
authorization/bearer material, blank/invalid JSON, and sensitive subject values are rejected
without a database hit. Warnings and health details never echo event content or raw exceptions.

`event_id` is idempotent. A retry succeeds only when every persisted field is equal at
`TIMESTAMP(3)` precision. Different content is an exceptional conflict and never overwrites the
original. Caller validation and conflict do not lower Audit health. Database/pool/executor failure
completes exceptionally, marks Audit `DOWN`, and emits a sanitized warning; a later successful
write can restore `UP`.

Enable probes the repository and persists `CORE_AUDIT_ENABLED` and `CORE_MIGRATION_READY` before
service publication. Close rejects new intake, attempts `CORE_DISABLE_STARTED` within the
configured bound, and reports `DISABLED` only on success. A close failure/timeout remains `DOWN`.
After the database intake gate closes, audit is not reopened. Consequently a durable
shutdown-timeout event is not guaranteed; health and sanitized logging remain mandatory evidence.

## Player identity

The join bridge snapshots UUID, account name, server ID, and UTC observation time. The repository
upserts by UUID. Only a strictly newer observation changes name/server/last-seen and increments
`lock_version`; a stale or equal observation cannot regress the row. Equal names for different
UUIDs remain separate. The listener is explicitly registered after identity initialization and
unregistered before identity/audit close.

## Item identity

The common key strings are:

```text
wayfarer:item_type
wayfarer:owner_uuid
wayfarer:instance_epoch
wayfarer:schema_version
wayfarer:item_instance_id
wayfarer:display_revision
```

Validation order is presence, canonical UUID parsing, scalar range, allowed type, supported schema,
database lookup, then type/owner/epoch/schema/display-revision equality. Unknown or mismatched data
fails closed. Presentation data is neither an input nor evidence.

Identity health is `UP` after repository initialization and successful operations, remains `UP`
for audited invalid claims, becomes `DOWN` on repository or required-audit failure, and becomes
`DISABLED` on clean close.
