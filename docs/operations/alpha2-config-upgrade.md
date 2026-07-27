# Alpha.2 Durable Audit Configuration Upgrade

Config version remains `1`; no field was added.

In alpha.1, `audit.enabled` was a non-durable placeholder. In alpha.2,
`audit.enabled: true` activates durable MariaDB-backed audit and item/player identity. It therefore
requires both `mariadb.enabled: true` and `migration.enabled: true`; any other combination fails
closed and the diagnostic names fields only.

The generated sample now uses:

```yaml
audit:
  enabled: false
```

Operators retaining an alpha.1 config with audit enabled and MariaDB disabled must either keep
audit disabled or supply the approved MariaDB secret references and enable Core migrations. This
repository does not install runtime config, apply Project migrations, or start a server.
