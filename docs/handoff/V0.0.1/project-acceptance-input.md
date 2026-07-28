# V0.0.1 Project Acceptance Input

| Input | Value |
|---|---|
| Release URL / tag / version | Pending |
| Final source commit | Pending; rc.2 candidate source `5039e008659be1f7e23658aabba12cb95a8a600d` |
| Artifact filenames / SHA-256 | Candidate `wayfarer-core-0.0.1-rc.2.jar` / `8C85F9C0D42EED631F3167DE5827C21139D07B71A63CE3E0AC90F746F9A651E6` |
| Config / migration version | `1` / `V003` |
| Sanitized config and environment variables | `docs/handoff/V0.0.1/sanitized-configuration.md` |
| Commands / permissions | `docs/handoff/V0.0.1/command-and-permission-reference.md` |
| Dependencies / placement / load order | `docs/handoff/V0.0.1/dependency-and-placement.md` |
| Test report / evidence commit | Pre-client report/evidence at tested source; final handoff commit pending |
| Known limitations / open decisions | See handoff files |
| Test server vs Project Runtime differences | Concrete standalone: Paper 1.21.11 build 132, Java 25, task-only MariaDB 11.8/Redis 8, fixed VaultUnlocked/RedisEconomy, test-only probe; Project placement/config/other plugins not changed |
| Project acceptance | Pending |
| Roadmap Order 9 | Pending |
| `requirements_cleared` | Owner decision required |

Recommended Project smoke test: verify hashes, Vault → RedisEconomy → Core startup/migration,
provider health, permission/redaction, shared balance, one debit/idempotent replay, insufficient
funds, and refund only after all prerequisites/backups are approved. Project integration must
record backup/restore, removal, downgrade, and acceptance evidence in the Project repository; this
Plugin repository performs no Runtime change.

Open decisions are review/merge, stable release identity, Project placement/acceptance, explicit
requirements clearance, and Project ownership of the shared Vault/RedisEconomy durability deferred
item. That item should move to `docs/11-deferred-design-items.md` or equivalent; it must not become
a Wayfarer-only side channel.
