# V0.0.1 Artifact Inventory

| Filename | Purpose | SHA-256 | Source commit | Release asset |
|---|---|---|---|---|
| `Wayfarer_Core-V0.0.1.jar` | Runtime Plugin | `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2` | `49e00e21716c1c13a2dbb170fdad1b19c4275612` | Required; not yet published |
| `SHA256SUMS.txt` | Hash manifest | Pending | Pending | Required |
| `RELEASE_MANIFEST.md` | Scope/provenance | Pending | Pending | Required |
| `DEPENDENCY_VERSIONS.toml` | Dependency evidence | Pending | Pending | Required |
| `TEST_SERVER_EVIDENCE.md` | Committed test-result snapshot | Pending | Pending | Required |
| `PLUGIN_TEST_REPORT.md` | Stable Plugin Test Report snapshot | Pending | Pending | Required |
| `MAIN_SERVER_INSTRUCTION.md` | Committed mainline requirement snapshot | Pending | Pending | Required |
| `REQUIREMENT_TRACEABILITY.md` | Fixed traceability evidence | Pending | Pending | Required |
| `RELEASE_READINESS.md` | Fixed readiness evidence | Pending | Pending | Required |
| `SANITIZED_CONFIGURATION.md` | Sanitized configuration and environment reference | Pending | Pending | Required |
| `COMMAND_AND_PERMISSION_REFERENCE.md` | Command and permission reference | Pending | Pending | Required |
| `DEPENDENCY_AND_PLACEMENT.md` | Dependency, placement, and load-order reference | Pending | Pending | Required |
| `THIRD_PARTY_NOTICES.md` | Bundled library and third-party notice inventory | Pending | Pending | Required |
| `LICENSE` | Repository license snapshot | Pending | Pending | Required |
| `KNOWN_LIMITATIONS.md` | Known limitations and open decisions | Pending | Pending | Required |
| `UPGRADE_AND_ROLLBACK.md` | Upgrade, removal, downgrade, and rollback procedure | Pending | Pending | Required |
| `ARTIFACT_MATRIX.md` | Publication-time matrix with deterministic tag and Release URL | Pending | Workflow-generated | Required |
| `PROJECT_ACCEPTANCE_INPUT.md` | Project-owned placement and acceptance inputs | Pending | Pending | Required |
| `ARTIFACT_INVENTORY.md` | Stable package inventory snapshot | Pending | Pending | Required |

Before checking out the immutable Product Source, the Stable workflow captures every required
Handoff document from the workflow's main revision as tracked regular files. It maps those fixed
snapshots to the Release filenames above, generates the publication-time Artifact Matrix, hashes
every attached non-self-referential asset, and attaches the complete set to the GitHub Release.
The Product Source and Handoff Source commits are recorded separately in `RELEASE_MANIFEST.md`.
Binary assets are stored in GitHub Releases, not Git history.

Stable local candidate (not yet a release asset):

| Filename | SHA-256 | Source commit |
|---|---|---|
| `wayfarer-core-0.0.1.jar` | `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2` | `49e00e21716c1c13a2dbb170fdad1b19c4275612` |

Historical rc.2 and rc.3 identities remain fixed and were not overwritten. No stable tag,
release URL, or published stable asset exists yet.
