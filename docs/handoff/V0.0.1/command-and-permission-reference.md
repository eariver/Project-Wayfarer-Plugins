# V0.0.1 Command and Permission Reference

| Command | Permission | Player/console | Confirmation | Audit/redaction |
|---|---|---|---|---|
| `/wayfarer admin health` | Pending exact node | Pending | No mutation | Required |
| `/wayfarer admin transaction inspect <id>` | Pending exact node | Pending | No mutation | Required |
| `/wayfarer admin transaction reconcile <id>` | Pending exact node | Pending | Pending explicit confirmation | Required |

General players receive no database, economy, migration, transaction reconcile, or unrestricted
administrative permission. Exact nodes are recorded after implementation and Project permission
review; no broad wildcard or OP dependency is assumed.
