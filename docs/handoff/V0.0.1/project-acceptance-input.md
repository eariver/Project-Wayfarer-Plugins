# V0.0.1 Project Acceptance Input

| Input | Value |
|---|---|
| Release URL / tag / version | Pending |
| Final source commit | Pending; rc.3 candidate source `95b2cf1ef159b4d16921ddb4c8698621b8134c3e` |
| Artifact filenames / SHA-256 | Candidate `wayfarer-core-0.0.1-rc.3.jar` / `6E58B501EF0B58AA19C9DD1A39D41ABE13173EDE32BE70E3DB0979CE10A3278F` |
| Config / migration version | `1` / `V003` |
| Sanitized config and environment variables | `docs/handoff/V0.0.1/sanitized-configuration.md` |
| Commands / permissions | `docs/handoff/V0.0.1/command-and-permission-reference.md` |
| Dependencies / placement / load order | `docs/handoff/V0.0.1/dependency-and-placement.md` |
| Test report / evidence commit | `docs/testing/results/V0.0.1-concrete-waymark-provider.md` at evidence record `92e32db98758eddad46c5f18772c21ef83366057` |
| Known limitations / open decisions | See handoff files |
| Test server vs Project Runtime differences | Concrete standalone: Paper 1.21.11 build 132, Java 25, task-only MariaDB 11.8/Redis 8, fixed VaultUnlocked/RedisEconomy, test-only probe; Project placement/config/other plugins not changed |
| Project acceptance | Pending |
| Roadmap Order 9 | Pending |
| `requirements_cleared` | Owner decision required |

Recommended Project smoke test: verify hashes, Vault → RedisEconomy → Core startup/migration,
provider health, permission/redaction, shared fractional balance, one long debit/idempotent replay,
insufficient funds, and long refund only after all prerequisites/backups are approved. Project integration must
record backup/restore, removal, downgrade, and acceptance evidence in the Project repository; this
Plugin repository performs no Runtime change.

Open decisions are review/merge, stable release identity, Project placement/acceptance, explicit
requirements clearance, and Project ownership of the shared Vault/RedisEconomy durability deferred
item. That item should move to `docs/11-deferred-design-items.md` or equivalent; it must not become
a Wayfarer-only side channel. The direct nonblocking Project reference is
[`eariver/Project_Wayfarer#1`](https://github.com/eariver/Project_Wayfarer/issues/1).
