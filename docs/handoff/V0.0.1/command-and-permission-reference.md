# V0.0.1 Command and Permission Reference

| Command | Permission | Player/console | Confirmation | Audit/redaction |
|---|---|---|---|---|
| `/wayfarer admin health` | `wayfarer.admin.health` | Authorized player + console | No mutation | Denial/failure operational event; sanitized output |
| `/wayfarer admin transaction inspect <id>` | `wayfarer.admin.transaction.inspect` | Authorized player + console | No mutation | Durable inspect audit; provider reference presence only |
| `/wayfarer admin transaction reconcile <id> <commit\|refund\|fail> confirm` | `wayfarer.admin.transaction.reconcile` | Authorized player + console | Required trailing `confirm` | Permission/action audit; sanitized response |

General players receive no database, economy, migration, transaction reconcile, or unrestricted
administrative permission. All three nodes default to operators in `plugin.yml`; production
granting remains Project-owned. Player health output omits component details by default, while
authorized console output includes sanitized details. Unknown subcommands return usage without
executing an administrative action.
