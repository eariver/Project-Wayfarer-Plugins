# V0.0.1 Project Acceptance Input

| Input | Value |
|---|---|
| Release URL / tag / version | Pending |
| Final source commit | Pending |
| Artifact filenames / SHA-256 | Pending |
| Config / migration version | Pending |
| Sanitized config and environment variables | Pending finalization |
| Commands / permissions | Pending finalization |
| Dependencies / placement / load order | Pending finalization |
| Test report / evidence commit | Pending |
| Known limitations / open decisions | See handoff files |
| Test server vs Project Runtime differences | Pending |
| Project acceptance | Pending |
| Roadmap Order 9 | Pending |
| `requirements_cleared` | Owner decision required |

Recommended Project smoke test: verify hashes, startup/migration, service registration, health,
permission denial, redaction, and a representative transaction only after all prerequisites and
backups are approved. Project integration must record backup/restore, removal, downgrade, and
acceptance evidence in the Project repository; this Plugin repository performs no Runtime change.
