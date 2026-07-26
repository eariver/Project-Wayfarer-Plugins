# V0.0.1 Command and Permission Reference

| Command | Permission | Player/console | Confirmation | Audit/redaction |
|---|---|---|---|---|
| `/wayfarer admin health` | `wayfarer.admin.health` | Authorized player + console | No mutation | Denial/failure operational event; sanitized output |
| `/wayfarer admin transaction inspect <id>` | Pending exact node | Pending | No mutation | Required |
| `/wayfarer admin transaction reconcile <id>` | Pending exact node | Pending | Pending explicit confirmation | Required |

General players receive no database, economy, migration, transaction reconcile, or unrestricted
administrative permission. The health node defaults to operators in `plugin.yml`; production
granting remains Project-owned. Player health output omits component details by default, while
authorized console output includes sanitized details. Unknown subcommands return usage without
executing an administrative action.
