# Redis and Task Boundary

## Authority and provenance

MariaDB remains the durable authority. Redis provides bounded cache, lease, invalidation/message,
and duplicate-hint assistance only. No Redis result can commit gameplay state, replace a MariaDB
unique key, or justify unconditional exactly-once behavior.

The test/runtime shape is fixed from `eariver/Project_Wayfarer` commit
`344eedc738d75954daa43facfeef302944f2963a`, `infrastructure/compose.yml` blob
`42e62979c8c70290fae78c0731ae628e940d74e9`: `redis:8-alpine`, password
authentication, AOF enabled, container port 6379, and host publication restricted to the
configured loopback address. TLS is not configured by that authority. Core accepts only the
environment-resolved URI and never persists or logs it.

## Internal Redis runtime

Lettuce 7.2.1 remains inside Core. Its client, connection, future, command, Netty, and Reactor
types do not cross the public API. Enabled startup connects command and Pub/Sub channels on the
managed worker executor, verifies `PING`, subscribes to the versioned channel, and publishes
Redis `UP`. Initial connection failure is fail-closed.

Connection listeners make outage and reconnect explicit:

- both command and Pub/Sub connections available: `UP`;
- disconnect, command failure, or timeout: `DOWN`;
- both channels reconnected and a successful operation: `UP`;
- disabled configuration: `UNKNOWN`;
- clean bounded close: `DISABLED`;
- timed-out, interrupted, or failed close: `DOWN`.

Commands reject main-thread entry before Lettuce dispatch. Disable stops Redis intake, waits
within the configured operation timeout for accepted commands, closes Pub/Sub, closes the command
connection, and shuts down the client. New work after intake close is exceptional.

## Foundations

Cache keys are namespaced under the configured prefix and carry a schema-version envelope plus a
bounded TTL. `cacheAside` treats missing, stale-schema, malformed, or unavailable Redis data as a
miss and invokes the authoritative loader. A failed best-effort refresh cannot change the
authoritative result.

Locks use an unguessable owner UUID, bounded lease, `SET NX PX`, and an atomic compare-and-delete
Lua release. A non-owner cannot unlock. Expiry or loss is never treated as durable transaction
evidence.

Pub/Sub uses an immutable version-1 envelope with message UUID, configured origin server ID,
message type, millisecond timestamp, and bounded safe payload. Invalid envelopes, duplicate
message IDs, and self-origin messages are rejected/ignored by documented policy. Gameplay-specific
messages are not implemented.

Idempotency assistance uses a bounded `SET NX PX` duplicate hint. `NEW_HINT` and
`DUPLICATE_HINT` are advisory; a MariaDB unique key is the final authority.

## Bounded task bridge

The executor uses a fixed worker count and bounded `ArrayBlockingQueue`. Queue overflow is an
immediate exceptional rejection, changes Executor health through the failure observer, and emits
a sanitized warning. Already accepted queued work remains accepted during graceful shutdown.

`WayfarerTasks.bridge` implements:

```text
JDK-only immutable scalar/enum/record snapshot
→ bounded worker operation
→ JDK-only immutable result
→ lifecycle-guarded main-thread dispatch
→ main-thread revalidation
→ mutation only when current
```

The runtime guard rejects mutable request/result classes and any Bukkit-typed snapshot, including
`Player`, `ItemStack`, `Inventory`, `World`, `Event`, and `PersistentDataContainer`. Callers must
resolve current Bukkit state by stable identifiers during revalidation and must not capture a
mutable Bukkit object in an async callback.
