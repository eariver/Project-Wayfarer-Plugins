# V0.0.2 Requirement Traceability

Status: terminal Codex classification for Draft PR #14. A blocked row is not a PASS claim;
implementation evidence is fixed at `981e425a4af619340b64b2060c0cb9ac7219cdd2`.

| Requirement ID | Source section | Implementation path | Automated / integration evidence | Headless / client need | Decision / release asset | Status | Notes |
|---|---|---|---|---|---|---|---|
| GOV-001 | Req. 1–7, 22–23 | `docs/requirements/**/V0.0.2/`, `docs/work-orders/V0.0.2/` | Snapshot hash and Git boundary audit | None | Source and execution ledgers | DONE | Project Runtime remains out of scope |
| BASE-001 | Req. 3, 6; Exec. 2.3, 6 | V0.0.1 tag/API/migration/package baselines | Compatibility and immutable-hash gates | None | Compatibility report | DONE | V0.0.1 artifact/tag remain immutable |
| REL-001 | Exec. 2.2, 8.1 | Version parser, release validators, release docs | Stable/correction/pre-release negative tests | None | Release tooling | DONE | No correction release is created |
| CI-001 | Exec. 8.2 | `.github/workflows/` | Workflow/static and normal CI | None | Node 24 action inventory | DONE | Official action majors only |
| PERSIST-001 | Req. 6.5–6.6, 8.6, 11.4; Exec. 7 | ADR 0009 and module persistence implementation | MariaDB/Testcontainers, migration failure tests | Headless startup | ADR 0009 | PLUGIN_REVIEW_REQUIRED | No public JDBC/API leakage |
| REL-002 | Req. 16; Exec. 8.3 | Scope-aware release/package scripts and workflows | Package/recovery/reproducibility tests | None | ADR 0010 and artifact manifest | DONE | Required scopes only: core, main-frontier, all; no dispatch |
| MAIN-001 | Req. 8.1–8.4 | Main lifecycle, config, role and Core gates | Config and capability gate tests | Client commands/UX | Main config | PLUGIN_REVIEW_REQUIRED | Deliberately disables until ADR 0009 |
| MAIN-002 | Req. 8.5–8.9 | Growth Tool model, repository contract, delivery, PDC/owner guard | Domain, identity and migration tests | Client item interaction | Main JAR | PLUGIN_REVIEW_REQUIRED | Production repository/wiring blocked; normal inventory never persisted |
| MAIN-003 | Req. 8.10–8.13 | Progress, threshold/evolution and config reconciliation | Pure domain and boundary tests | Client mining | Main JAR | PLUGIN_REVIEW_REQUIRED | Pure fixed-point domain done; event wiring awaits persistence review |
| MAIN-004 | Req. 8.14–8.18 | Broken state, GUI proposal, repair transaction, sessions/checkpoint | Repair ambiguity/idempotency tests | Client GUI/repair | MAIN-D04/D05; B-004 | PLUGIN_REVIEW_REQUIRED | UNKNOWN is not refunded/retried; presentation also needs Owner approval |
| MAIN-005 | Req. 8.19–8.20 | Admin, inspect, reissue, reconcile, audit | Permission proposal and sanitized boundaries | Client admin flow | Command reference | PLUGIN_REVIEW_REQUIRED | Production handlers depend on reviewed repository lifecycle |
| FRONT-001 | Req. 11.1–11.4 | Frontier lifecycle, config, exact-world and persistence foundation | Config, exact-world and MariaDB tests | Client entry/UX | Frontier config | PLUGIN_REVIEW_REQUIRED | Deliberately disables until ADR 0009 |
| FRONT-002 | Req. 11.5–11.8 | Traversal identity/loadout/Pending Delivery/Navigation proposal | Domain, identity and delivery tests | Client item/GUI | FRONT-D01/D05 | OWNER_APPROVAL_REQUIRED | Production wiring also awaits ADR 0009 |
| FRONT-003 | Req. 12 | LeafGrapple adapter boundary | Version/public-API/capability fail-closed tests | Client hook | FRONT-D02 | EXTERNAL_BLOCKED | Examined default tier is unsafe; no fallback/fork |
| FRONT-004 | Req. 13 | Launchpad state machine, persistence, placement/use/protection/reconcile | Domain, placement, use and MariaDB tests | Client placement/use | FRONT-D03/D04 | PLUGIN_REVIEW_REQUIRED | External bulk-edit coverage requires review |
| FRONT-005 | Req. 14 | Frontier WM shop and Pending Delivery | Transaction ambiguity/idempotency/delivery tests | Client shop | FRONT-D06 | PLUGIN_REVIEW_REQUIRED | Launchpad/rocket only; ambiguous payment not retried |
| WAYSTONE-001 | Exec. 2.4 and final scope | No production Waystone, discovery or teleport registration | Absence/config/package tests | N/A | Known limitation | DEFERRED_BY_REQUIREMENT | Placement tool must not be sold or granted |
| ADAPTER-001 | Req. 11, 15; Exec. 2.5 | No EM–MVI module/artifact | Module/package absence tests | N/A | FRONT-D15 | DEFERRED_BY_REQUIREMENT | Await Project Order 13 `ADAPTER_REQUIRED` |
| MIG-001 | Req. 9 | Separate Core/Main/Frontier locations | Empty/upgrade/repeat/failure/boundary tests | Headless startup after review | Migration handoff | DONE | Core V001–V003 immutable; production ownership still ADR 0009 |
| TEST-001 | Req. 17–21 | Focused unit, MariaDB, headless and client instructions | `check` plus representative suites | Prepared; runtime/client blocked | Test report/evidence index | PLUGIN_REVIEW_REQUIRED | Implemented automated scope passes; headless cannot prove unwired gameplay |
| HANDOFF-001 | Req. 16, 24–26; Exec. 22–23 | `docs/handoff/V0.0.2/`, reports and evidence | Package/document validators | Project acceptance later | Candidate handoff package | PLUGIN_REVIEW_REQUIRED | No candidate/tag/release/merge |
