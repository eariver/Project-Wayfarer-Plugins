# V0.0.2 Requirement Traceability

Status: Initial authority and execution mapping. Implementation evidence is added at each
milestone; no row may be promoted from a proposal or test plan alone.

| Requirement ID | Source section | Implementation path | Automated / integration evidence | Headless / client need | Decision / release asset | Status | Notes |
|---|---|---|---|---|---|---|---|
| GOV-001 | Req. 1–7, 22–23 | `docs/requirements/**/V0.0.2/`, `docs/work-orders/V0.0.2/` | Snapshot hash and Git boundary audit | None | Source and execution ledgers | DONE | Project Runtime remains out of scope |
| BASE-001 | Req. 3, 6; Exec. 2.3, 6 | V0.0.1 tag/API/migration/package baselines | Compatibility and immutable-hash gates | None | Compatibility report | TODO | V0.0.1 artifact/tag remain immutable |
| REL-001 | Exec. 2.2, 8.1 | Version parser, release validators, release docs | Stable/correction/pre-release negative tests | None | Release tooling | TODO | No correction release is created |
| CI-001 | Exec. 8.2 | `.github/workflows/` | Workflow/static and normal CI | None | Node 24 action inventory | TODO | Official action majors only |
| PERSIST-001 | Req. 6.5–6.6, 8.6, 11.4; Exec. 7 | ADR 0009 and module persistence implementation | MariaDB/Testcontainers, migration failure tests | Headless startup | ADR 0009 | PLUGIN_REVIEW_REQUIRED | No public JDBC/API leakage |
| REL-002 | Req. 16; Exec. 8.3 | Scope-aware release/package scripts and workflows | Package/recovery/reproducibility tests | None | ADR 0010 and artifact manifest | TODO | Required scopes only: core, main-frontier, all |
| MAIN-001 | Req. 8.1–8.4 | Main lifecycle, config, role and Core gates | Focused unit/runtime startup tests | Client commands/UX | Main config | TODO | Main-only, fail closed |
| MAIN-002 | Req. 8.5–8.9 | Growth Tool model, repository contract, delivery, PDC/owner guard | Domain and MariaDB tests | Client item interaction | Main JAR | TODO | MVI/normal inventory never persisted |
| MAIN-003 | Req. 8.10–8.13 | Progress, threshold/evolution and config reconciliation | Pure domain and boundary tests | Client mining | Main JAR | TODO | Fixed-point progress; actual break once |
| MAIN-004 | Req. 8.14–8.18 | Broken state, GUI proposal, repair transaction, sessions/checkpoint | Domain/transaction/runtime tests | Client GUI/repair | MAIN-D04/D05 | TODO | Core transaction UNKNOWN semantics preserved |
| MAIN-005 | Req. 8.19–8.20 | Admin, inspect, reissue, reconcile, audit | Command/permission/redaction tests | Client admin flow | Command reference | TODO | Debug disabled by default |
| FRONT-001 | Req. 11.1–11.4 | Frontier lifecycle, config, exact-world and persistence foundation | Focused unit/MariaDB/runtime tests | Client entry/UX | Frontier config | TODO | `frontier_iris` exact match only |
| FRONT-002 | Req. 11.5–11.8 | Traversal identity/loadout/Pending Delivery/Navigation proposal | Domain, identity, delivery tests | Client item/GUI | FRONT-D01/D05 | TODO | Navigation exposes available functions only |
| FRONT-003 | Req. 12 | LeafGrapple adapter boundary | Descriptor/API/capability and fail-closed tests | Client hook | FRONT-D02 | TODO | No fork or movement reimplementation |
| FRONT-004 | Req. 13 | Launchpad state machine, persistence, placement/use/protection/reconcile | Domain/MariaDB/Paper event tests | Client placement/use | FRONT-D03/D04 | TODO | Public use; no drop on removal |
| FRONT-005 | Req. 14 | Frontier WM shop and Pending Delivery | Transaction/idempotency/delivery tests | Client shop | FRONT-D06 | TODO | Launchpad/rocket only |
| WAYSTONE-001 | Exec. 2.4 and final scope | No production Waystone, discovery or teleport registration | Absence/config/package tests | N/A | Known limitation | DEFERRED_BY_REQUIREMENT | Placement tool must not be sold or granted |
| ADAPTER-001 | Req. 11, 15; Exec. 2.5 | No EM–MVI module/artifact | Module/package absence tests | N/A | FRONT-D15 | DEFERRED_BY_REQUIREMENT | Await Project Order 13 `ADAPTER_REQUIRED` |
| MIG-001 | Req. 9 | Separate Core/Main/Frontier locations | Empty/upgrade/repeat/failure/boundary tests | Headless startup | Migration handoff | TODO | Core V001–V003 immutable |
| TEST-001 | Req. 17–21 | Focused unit, MariaDB, headless and client instructions | `check` plus representative suites | Client test steps | Test report/evidence index | TODO | No unnecessary stress/crash matrix |
| HANDOFF-001 | Req. 16, 24–26; Exec. 22–23 | `docs/handoff/V0.0.2/`, reports and evidence | Package/document validators | Project acceptance later | Candidate handoff package | TODO | No tag/release/merge |

