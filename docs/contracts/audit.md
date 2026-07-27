# Wayfarer Audit Contract

- API: `WayfarerAudit.record(AuditEvent)` returns `CompletionStage<Void>`.
- Threading: validation is caller-side and non-blocking; persistence uses the Core managed
  executor and `InternalDatabase`.
- Idempotency: an exact `event_id` retry succeeds with one row; conflicting content fails.
- Validation: event/subject identifiers, nonblank subject, configured server-ID equality, UTC
  millisecond timestamp, and null-or-valid JSON up to 16 KiB UTF-8.
- Server authority: caller input must equal the configured Core server ID. Only the configured
  value is persisted.
- Redaction gate: sensitive key tokens, resolved runtime secret values, JDBC/Redis URIs,
  authorization/token/cookie/credential material, raw exceptions, stacks, and config dumps are
  rejected from every caller-controlled persistent string before persistence.
- Failure: caller rejection/conflict is exceptional without lowering health. Infrastructure
  failure is exceptional, sets Audit `DOWN`, warns without content, and is never silently dropped.
- Recovery: a later successful write may set Audit `UP`.
- Disable: new records are rejected after close begins. `CORE_DISABLE_STARTED` is bounded by the
  configured timeout; timeout/failure remains `DOWN`.
- Limitation: once database intake closes it is not bypassed, so durable shutdown-timeout audit is
  not guaranteed.
