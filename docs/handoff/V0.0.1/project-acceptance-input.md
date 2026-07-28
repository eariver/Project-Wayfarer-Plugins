# V0.0.1 Project Acceptance Input

| Input | Value |
|---|---|
| Release URL / tag / version | Pending |
| Final source commit | Pending; pre-client tested source `6d25105f516a76cc373e5259fcef9d34de414543` |
| Artifact filenames / SHA-256 | Candidate `wayfarer-core-0.0.1-rc.1.jar` / `f36fe57370b4d123b13b5bf328c029c03407338e83e781953db81547de8a334a` |
| Config / migration version | `1` / `V003` |
| Sanitized config and environment variables | `docs/handoff/V0.0.1/sanitized-configuration.md` |
| Commands / permissions | `docs/handoff/V0.0.1/command-and-permission-reference.md` |
| Dependencies / placement / load order | `docs/handoff/V0.0.1/dependency-and-placement.md` |
| Test report / evidence commit | Pre-client report/evidence at tested source; final handoff commit pending |
| Known limitations / open decisions | See handoff files |
| Test server vs Project Runtime differences | Headless: Paper 1.21.11 build 132, Java 25, ephemeral MariaDB 11.8/Redis 8; no concrete provider or client |
| Project acceptance | Pending |
| Roadmap Order 9 | Pending |
| `requirements_cleared` | Owner decision required |

Recommended Project smoke test: verify hashes, startup/migration, service registration, health,
permission denial, redaction, and a representative transaction only after all prerequisites and
backups are approved. Project integration must record backup/restore, removal, downgrade, and
acceptance evidence in the Project repository; this Plugin repository performs no Runtime change.

Use `docs/testing/plans/V0.0.1-client-acceptance.md` for the remaining minimum client-only cases.
Do not execute a representative concrete-provider transaction while ADR 0006 remains blocked.
