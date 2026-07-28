# V0.0.1 Sanitized Configuration

- Config version: `1`; missing or unsupported versions fail closed.
- Server ID: operator-defined, non-secret, 1–64 characters from `[A-Za-z0-9._-]`.
  `change_me`, `change-me`, `changeme`, `default`, and `example` are reserved
  case-insensitively. The default `CHANGE_ME` fails closed until explicitly replaced.
- MariaDB: `enabled`, environment references `WAYFARER_DB_URL`,
  `WAYFARER_DB_USERNAME`, `WAYFARER_DB_PASSWORD`, pool sizes, and timeout.
- Redis: `enabled`, environment reference `WAYFARER_REDIS_URI`, and connect timeout.
- Migration: `enabled` plus Core-only `db/migration/core`; alpha.2 applies immutable V001 and
  additive V002, and enabling migration requires MariaDB.
- Waymark: `enabled`, expected provider `RedisEconomy`, and operation timeout. Provider
  capability/thread verification remains a later gate.
- Executor: 1–64 threads and a thread prefix containing `Wayfarer`.
- Audit: `enabled: true` activates durable MariaDB audit plus player/item identity and therefore
  requires both MariaDB and migration. The sample defaults to `false`. An alpha.1 config with
  audit enabled but MariaDB/migration disabled now intentionally fails closed; config version
  remains `1`.
- Health: player detail output defaults to `false`.
- Shutdown timeout: 1–300 seconds. The configured duration is used once for graceful
  termination and once after `shutdownNow`, so the maximum blocking duration is approximately
  twice the configured value.

Never place secret values in this document, YAML samples, logs, audits, exceptions, or release
manifests. Disabled integrations retain only reference names. Enabled integrations resolve every
required reference from the environment and fail closed if a value is absent or blank.
