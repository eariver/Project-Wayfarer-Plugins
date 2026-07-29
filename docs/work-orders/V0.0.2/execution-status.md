# V0.0.2 Execution Status

Status vocabulary is restricted to the long-running execution instruction. `TODO` and
`IN_PROGRESS` are permitted while work is active and must be zero before final handoff.

## Fixed execution identity

- Pre-execution branch: `release/V0.0.1-stable-preparation`
- Pre-execution HEAD: `874c2268da5a94f024b8c4532f409d8698b85a2f`
- Work base: `efe9d81029a10ce9ca0ce01f9c6770a4991784bc`
- Work branch: `feature/V0.0.2-main-frontier`
- Project reference: `344eedc738d75954daa43facfeef302944f2963a`
- Requirement SHA-256:
  `2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`
- Project Runtime changed: No

## Ledger

| ID | Work Item | Requirement | Dependency | Status | Evidence | Remaining Gate |
|---|---|---|---|---|---|---|
| AUTH-001 | Read final instruction and all mandatory authorities | Req. 6.2; Exec. 1, 5 | None | DONE | `source.md` | None |
| AUTH-002 | Fix exact requirement snapshot and authority order | Exec. 5 | AUTH-001 | DONE | Requirement SHA and `source.md` | None |
| BASE-001 | Verify V0.0.1 tag/product/artifact and Core migrations | Exec. 2.3, 6.1 | AUTH-001 | DONE | `source.md` | Mechanical API/package gate remains |
| BASE-002 | Add V0.0.1 API compatibility baseline | Exec. 6.3 | BASE-001 | DONE | `V001PublicApiCompatibilityTest` | None |
| BASE-003 | Audit scaffold, CI, reproducibility and package constraints | Exec. 6.1–6.2 | BASE-001 | DONE | Baseline observations; ADR 0009/0010 | None |
| REL-001 | Add correction-suffix grammar/order/docs/tests | Exec. 2.2, 8.1 | BASE-003 | DONE | `release-policy.sh`; focused policy test | None |
| CI-001 | Upgrade official Actions to Node 24 majors | Exec. 8.2 | BASE-003 | DONE | checkout v7, setup-java v5, setup-gradle v6, artifact v7/v8 | Hosted runner compatibility |
| ADR-009 | Decide module persistence boundary | Exec. 7 | BASE-001 | PLUGIN_REVIEW_REQUIRED | `blocking-register.md` | B-001 |
| ADR-010 | Decide Core reuse/artifact release scope | Exec. 8.3 | ADR-009 | DONE | ADR 0010 | None |
| REL-002 | Implement required scope-aware multi-artifact release/package path | Req. 16; Exec. 8.3 | ADR-010 | IN_PROGRESS | Pre-release scope collection and policy | Stable package path |
| MAIN-001 | Main role/config/Core/lifecycle gate | Req. 8.1–8.4 | AUTH-002 | TODO | Pending | None |
| MAIN-002 | Growth Tool pure domain and threshold engine | Req. 8.5, 8.10–8.13 | MAIN-001 | IN_PROGRESS | Domain, identity, session, delivery and focused tests | Bukkit event/GUI surface |
| MAIN-003 | Growth Tool migration/repository/pending delivery | Req. 8.6–8.8 | ADR-009 | TODO | Pending | B-001 for concrete persistence |
| MAIN-004 | PDC/owner/interaction guards | Req. 8.9 | MAIN-002 | TODO | Pending | External repair review |
| MAIN-005 | Break/progress/evolution runtime | Req. 8.10–8.13 | MAIN-002, MAIN-004 | TODO | Pending | Client test |
| MAIN-006 | Broken state/GUI/repair transaction | Req. 8.14–8.17 | MAIN-002, MAIN-003 | TODO | Pending | MAIN-D04/D05; client test |
| MAIN-007 | Session/checkpoint/admin/reconcile | Req. 8.18–8.20 | MAIN-003 | TODO | Pending | Client test |
| FRONT-001 | Frontier role/config/Core/exact-world gate | Req. 11.1–11.3 | AUTH-002 | TODO | Pending | FRONT-D01 |
| FRONT-002 | Traversal/loadout/pending-delivery pure domain | Req. 11.4–11.8 | FRONT-001 | IN_PROGRESS | World/identity/delivery/shop domain and focused tests | Navigation approval/client test |
| FRONT-003 | Frontier migration/repository | Req. 11.4, 13.2 | ADR-009 | TODO | Pending | B-001 for concrete persistence |
| FRONT-004 | LeafGrapple version/capability boundary | Req. 12 | FRONT-001 | TODO | Pending | FRONT-D02; external artifact evidence |
| FRONT-005 | Launchpad domain/runtime/protection/reconcile | Req. 13 | FRONT-003 | IN_PROGRESS | Launchpad and placement state machines; use coordinator | Persistence/protection/client test |
| FRONT-006 | Frontier shop transaction/delivery | Req. 14 | FRONT-002, FRONT-003 | IN_PROGRESS | Fail-closed catalog and idempotent purchase coordinator | Persistence/client test |
| WAYSTONE-001 | Defer Waystone production/GUI/teleport/tool | Final instruction 2.4 | None | DEFERRED_BY_REQUIREMENT | Traceability and decision register | Later V0.0.x |
| ADAPTER-001 | Keep EM–MVI adapter absent | Req. 15; Exec. 2.5 | None | DEFERRED_BY_REQUIREMENT | Existing module boundary | Project Order 13 |
| MIG-001 | Empty/upgrade/repeat/failure/boundary migration tests | Req. 9 | MAIN-003, FRONT-003 | DONE | Main/Frontier isolated MariaDB suites; hashes in `source.md` | Runtime pool integration remains B-001 |
| TEST-001 | Focused domain/unit/API tests and `check` | Req. 17–18 | Product work | TODO | Pending | None |
| TEST-002 | Isolated MariaDB tests | Req. 19 | Persistence work | TODO | Pending | Docker |
| TEST-003 | Headless Main/Frontier Paper wiring | Req. 20 | Runtime work | TODO | Pending | Paper harness |
| TEST-004 | Prepare client acceptance steps | Req. 21 | Runtime work | TODO | Pending | CLIENT_TEST_REQUIRED |
| HANDOFF-001 | Reports/handoff/artifact matrix/readiness | Req. 16, 24–26 | Tests | TODO | Pending | Review/owner/client outcomes |
| GIT-001 | Create/push initial foundation and Draft PR | Exec. 3.2–3.4 | AUTH-002 | DONE | Commit `ae0f80237113fb96dffd6934d016b19d4e768f18`; Draft PR #14 | None |
| RUNTIME-001 | Project Runtime operations | Req. 22; AGENTS | None | NOT_APPLICABLE | Repository boundary | Project-owned |
| RELEASE-001 | Merge/tag/release publication | Final instruction | Review/handoff | OTHER_BLOCKED | No dispatch authorized | Explicit later approval |

## Baseline observations

- `Wayfarer_Main` and `Wayfarer_Frontier` are service-lookup scaffolds with production scaffold
  warnings and no gameplay listeners.
- The LeafGrapple integration is an interface-only scaffold.
- Current stable release tooling is Core-only and uses a single expected JAR hash.
- Existing handoff lookup is fixed to `V0.0.1`.
- Core config version is 1 and Core V001–V003 are immutable.
- Current public database capability is deliberately unavailable; module persistence therefore
  requires ADR 0009 before a concrete pool/migration lifecycle is implemented.
- Current workflow majors include Node 20 actions and require the official Node 24 updates
  recorded during this task.
