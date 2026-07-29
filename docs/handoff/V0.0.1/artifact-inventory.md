# V0.0.1 Artifact Inventory

| Filename | Purpose | SHA-256 | Source commit | Release asset |
|---|---|---|---|---|
| `Wayfarer_Core-V0.0.1.jar` | Runtime Plugin | `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2` | `49e00e21716c1c13a2dbb170fdad1b19c4275612` | Required; not yet published |
| `SHA256SUMS.txt` | Hash manifest | Pending | Pending | Required |
| `RELEASE_MANIFEST.md` | Scope/provenance | Pending | Pending | Required |
| `DEPENDENCY_VERSIONS.toml` | Dependency evidence | Pending | Pending | Required |
| `TEST_SERVER_EVIDENCE.md` | Committed test-result snapshot | Pending | Pending | Required |
| `MAIN_SERVER_INSTRUCTION.md` | Committed mainline requirement snapshot | Pending | Pending | Required |
| `REQUIREMENT_TRACEABILITY.md` | Fixed traceability evidence | Pending | Pending | Required |
| `RELEASE_READINESS.md` | Fixed readiness evidence | Pending | Pending | Required |

The workflow generates the remaining manifest/evidence asset hashes at publication from the
reviewed source and committed inputs. Sanitized configuration, command/permission, dependency,
license/notice, stable test report, limitations, and rollback documents are present in source.
Binary assets are stored in GitHub Releases, not Git history.

Stable local candidate (not yet a release asset):

| Filename | SHA-256 | Source commit |
|---|---|---|
| `wayfarer-core-0.0.1.jar` | `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2` | `49e00e21716c1c13a2dbb170fdad1b19c4275612` |

Historical rc.2 and rc.3 identities remain fixed and were not overwritten. No stable tag,
release URL, or published stable asset exists yet.
