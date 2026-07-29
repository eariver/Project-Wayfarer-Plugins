# V0.0.1 Project Acceptance Input

| Input | Value |
|---|---|
| Release URL / tag / version | Stable version `V0.0.1`; tag, URL, and published asset pending explicit stable workflow. `V0.0.1-alpha.1` is historical; no additional pre-release is required |
| Final source commit | `49e00e21716c1c13a2dbb170fdad1b19c4275612` |
| Artifact filenames / SHA-256 | Local stable candidate `wayfarer-core-0.0.1.jar` / `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2`; release asset name `Wayfarer_Core-V0.0.1.jar` |
| Config / migration version | `1` / `V003` |
| Sanitized config and environment variables | `docs/handoff/V0.0.1/sanitized-configuration.md` |
| Commands / permissions | `docs/handoff/V0.0.1/command-and-permission-reference.md` |
| Dependencies / placement / load order | `docs/handoff/V0.0.1/dependency-and-placement.md` |
| Test report / evidence | `docs/testing/results/V0.0.1-stable-local-acceptance.md`; `docs/reports/Project_Wayfarer_Plugin_Release_Test_Report_V0.0.1_2026-07-29.md`; preparation evidence record `eabda6d2c83e7369dd9f4ba4725f80d601a51062` |
| Known limitations / open decisions | See handoff files |
| Test server vs Project Runtime differences | Local isolated: Paper 1.21.11 build 132, Java 25, task-only MariaDB 11.8/Redis 8, Owner-supplied 23-JAR inventory, fixed VaultUnlocked/RedisEconomy, test-only probe; Project Runtime/config not changed. Full-inventory external-plugin limitation is recorded in the stable result |
| Project acceptance | Pending |
| Roadmap Order 9 | Pending |
| `requirements_cleared` | Explicit Owner authorization required for source-side stable publication after Plugin-side prerequisites are confirmed; not Project Runtime acceptance |

Recommended Project smoke test: verify hashes, Vault → RedisEconomy → Core startup/migration,
provider health, permission/redaction, shared fractional balance, one long debit/idempotent replay,
insufficient funds, and long refund only after all prerequisites/backups are approved. Project integration must
record backup/restore, removal, downgrade, and acceptance evidence in the Project repository; this
Plugin repository performs no Runtime change.

Open actions are Draft review/merge, explicit Owner source-side publication authorization and
stable workflow approval/publication, Project placement/acceptance, and Project ownership of the shared
Vault/RedisEconomy durability deferred item. That item should move to
`docs/11-deferred-design-items.md` or equivalent; it must not become a Wayfarer-only side
channel. The direct nonblocking Project reference is
[`eariver/Project_Wayfarer#1`](https://github.com/eariver/Project_Wayfarer/issues/1).

The stable local candidate evidence is available for review. The stable GitHub tag, URL, and
published asset remain future workflow outputs and must be recorded before Project placement.
