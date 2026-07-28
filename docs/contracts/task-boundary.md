# Task Boundary Contract

`WayfarerTasks` is JDK-only and lifecycle-scoped.

- `database` submits an existing JDK-only operation to the bounded worker executor.
- `bridge` accepts an immutable snapshot, executes its function on a managed worker, validates the
  immutable result, dispatches to the main thread, revalidates current state, and mutates only when
  the revalidation predicate succeeds.
- `TaskBridgeResult.applied=false` is an explicit stale-result outcome, not a successful mutation.
- `mainThread` and the callback half of `bridge` recheck lifecycle state immediately before
  execution.
- Submission after disable, late callback after disable, bounded-queue overflow, mutable/Bukkit
  task data, and task failure complete exceptionally.

Immediate queue rejection is the configured backpressure policy; callers are not allowed to
block the Paper thread waiting for capacity. Critical work must observe the exceptional result.
Redis and JDBC network I/O additionally reject main-thread entry at their internal boundaries.
