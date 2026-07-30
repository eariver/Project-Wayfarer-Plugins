# V0.0.2 Execution Status

Status vocabulary follows the long-running execution instruction.

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
| ADR-009 | Decide module persistence boundary | Exec. 7 | BASE-001 | DONE | ADR 0009 owner decision and module-local histories | None |
| ADR-010 | Decide Core reuse/artifact release scope | Exec. 8.3 | ADR-009 | DONE | ADR 0010 | None |
| REL-002 | Implement required scope-aware multi-artifact release/package path | Req. 16; Exec. 8.3 | ADR-010 | DONE | Scope collection, evidence-bound manifest, package and recovery tests | No dispatch |
| MAIN-001 | Main role/config/Core/lifecycle gate | Req. 8.1–8.4 | AUTH-002 | DONE | Typed config, Core gate, async module lifecycle | Client |
| MAIN-002 | Growth Tool pure domain and threshold engine | Req. 8.5, 8.10–8.13 | MAIN-001 | DONE | Domain, identity, session, delivery and focused tests | None |
| MAIN-003 | Growth Tool migration/repository/pending delivery | Req. 8.6–8.8 | ADR-009 | DONE | Module pool, separate history, JDBC repository and combined migration tests | Client |
| MAIN-004 | PDC/owner/interaction guards | Req. 8.9 | MAIN-002 | DONE | Physical-instance PDC authority, reissue rotation, shared validation and representative soulbound transfer tests at `03047cca` | Client interaction |
| MAIN-005 | Break/progress/evolution runtime | Req. 8.10–8.13 | MAIN-002, MAIN-004 | DONE | Evolution-only durability restore, remaining-ratio config reconcile and recalculation wiring at `03047cca` | Client mining feel |
| MAIN-006 | Broken state/GUI/repair transaction | Req. 8.14–8.17 | MAIN-002, MAIN-003 | DONE | Holder-bound preview/confirm/cancel, quote revalidation, action mapping and single-use acceptance at `03047cca` | MAIN-D04/D05; client presentation |
| MAIN-007 | Session/checkpoint/admin/reconcile | Req. 8.18–8.20 | MAIN-003 | DONE | Session/checkpoint, authority mutation, delivery retry, inspect/reconcile and audit wiring | Client; command copy review |
| FRONT-001 | Frontier role/config/Core/exact-world gate | Req. 11.1–11.3 | AUTH-002 | DONE | Config, service and exact-world fail-closed gates | FRONT-D01 stable approval |
| FRONT-002 | Traversal/loadout/pending-delivery pure domain | Req. 11.4–11.8 | FRONT-001 | DONE | Complete permanent-item metadata, owner-bound transfer/death guards, power-3 rockets and tested navigation actions at `03047cca` | FRONT-D05; client presentation |
| FRONT-003 | Frontier migration/repository | Req. 11.4, 13.2 | ADR-009 | DONE | Module pool, separate history, JDBC repositories and combined migration tests | Client |
| FRONT-004 | LeafGrapple version/capability boundary | Req. 12 | FRONT-001 | EXTERNAL_BLOCKED | Public 1.0.2 probe, tests and artifact assessment | Safe tier plus client motion |
| FRONT-005 | Launchpad domain/runtime/protection/reconcile | Req. 13 | FRONT-003 | DONE | Per-item delivery identity, replay guard, tested placement rejection/revalidation, public break and active-coordinate native/WorldGuard/WorldEdit protection at `03047cca` | FRONT-D04; client placement |
| FRONT-006 | Frontier shop transaction/delivery | Req. 14 | FRONT-002, FRONT-003 | DONE | Durable payment, pending/delivered fulfillment and replay tests | Client |
| MAIN-D01 | Block weights and ore multipliers | Req. 15; Exec. 18 | MAIN-002 | DONE | Requirement baseline in fixed-point config/domain | None |
| MAIN-D02 | Threshold coefficients | Req. 15; Exec. 18 | MAIN-002 | DONE | Monotonic baseline and focused tests | None |
| MAIN-D03 | Repair price | Req. 15; Exec. 18 | MAIN-006 | DONE | Fixed-point pricing domain and tests | None |
| MAIN-D04 | Main GUI layout/language | Req. 15; Exec. 18–19 | MAIN-006 | OWNER_APPROVAL_REQUIRED | Japanese 27-slot proposal | Accept or revise proposal |
| MAIN-D05 | Growth Tool name/lore | Req. 15; Exec. 18–19 | MAIN-004 | OWNER_APPROVAL_REQUIRED | Concise Japanese proposal without internal IDs | Accept or revise proposal |
| MAIN-D06 | Pending-delivery text UI | Req. 15; Exec. 18–19 | MAIN-007 | PLUGIN_REVIEW_REQUIRED | Two-line sanitized Japanese proposal | Review copy/channel |
| MAIN-D07 | Admin command/permission surface | Req. 15; Exec. 18 | MAIN-007 | PLUGIN_REVIEW_REQUIRED | Command/permission reference | Review exact nodes |
| MAIN-D08 | External repair guards | Req. 15; Exec. 18 | MAIN-004 | PLUGIN_REVIEW_REQUIRED | Native-first fail-closed proposal | Review supported hook matrix |
| MAIN-D09 | Netherite timing/price | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | Decision register | Later approved scope |
| MAIN-D10 | Reset/preserve authority | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | Plugin performs no reset | Project Order 25 |
| FRONT-D01 | Missing `frontier_iris` behavior | Req. 15; Exec. 18–19 | FRONT-001 | OWNER_APPROVAL_REQUIRED | Admin-health-only proposal | Accept or disable whole plugin |
| FRONT-D02 | LeafGrapple 1.0.2 boundary | Req. 12, 15; Exec. 18 | FRONT-004 | EXTERNAL_BLOCKED | Public API probe; default unsafe | Supply/approve safe tier |
| FRONT-D03 | Launchpad creation snapshot | Req. 13, 15; Exec. 18–19 | FRONT-005 | PLUGIN_REVIEW_REQUIRED | Immutable creation-default proposal | Review values |
| FRONT-D04 | WorldGuard/WorldEdit/FAWE protection | Req. 13, 15; Exec. 18 | FRONT-005 | PLUGIN_REVIEW_REQUIRED | Native/public-hook proposal and limitation | Review coverage sufficiency |
| FRONT-D05 | Navigation GUI layout/language | Req. 15; Exec. 18–19 | FRONT-002 | OWNER_APPROVAL_REQUIRED | Japanese 27-slot proposal; Waystone unavailable | Accept or revise proposal |
| FRONT-D06 | Shop Pending Delivery representation | Req. 14–15; Exec. 18–19 | FRONT-006 | PLUGIN_REVIEW_REQUIRED | Typed durable record tied to Core transaction | Review representation |
| FRONT-D07 | Seed/border/generation | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | Plugin never creates/changes worlds | Project-owned |
| FRONT-D08 | Portal deny | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | No fallback portal path | Project follow-up |
| FRONT-D09 | Gate coordinates/safe arrival | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | No guessed coordinates | Project follow-up |
| FRONT-D10 | MVI Runtime config | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | MVI remains normal-inventory authority | Project-owned |
| FRONT-D11 | Waystone template/palette | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | Production Waystone absent | Later Waystone scope |
| FRONT-D12 | Waystone safe arrival/interaction | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | No teleport behavior | Later Waystone scope |
| FRONT-D13 | Resource pack/model | Req. 15; Exec. 18 | None | NOT_APPLICABLE | Vanilla materials; no custom-model requirement | None |
| FRONT-D14 | Ruined WM rewards | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | Reward domain excluded | Later scope |
| FRONT-D15 | EliteMobs–MVI adapter | Req. 15; Exec. 18 | None | DEFERRED_BY_REQUIREMENT | Module/artifact absent | Order 13 `ADAPTER_REQUIRED` |
| WAYSTONE-001 | Defer Waystone production/GUI/teleport/tool | Final instruction 2.4 | None | DEFERRED_BY_REQUIREMENT | Traceability and decision register | Later V0.0.x |
| ADAPTER-001 | Keep EM–MVI adapter absent | Req. 15; Exec. 2.5 | None | DEFERRED_BY_REQUIREMENT | Existing module boundary | Project Order 13 |
| MIG-001 | Empty/upgrade/repeat/failure/boundary migration tests | Req. 9 | MAIN-003, FRONT-003 | DONE | Core+Main/Core+Frontier same-schema histories and repository CAS tests | None |
| TEST-001 | Focused domain/unit/API tests and `check` | Req. 17–18 | Product work | DONE | Gradle `check`; test report | None for implemented scope |
| TEST-002 | Isolated MariaDB tests | Req. 19 | Persistence work | DONE | Main/Frontier empty/upgrade/repeat/failure Testcontainers | None |
| TEST-003 | Headless Main/Frontier Paper wiring | Req. 20 | Runtime work | DONE | Startup/migration Headless PASS `30546252420`; representative gameplay unit/integration coverage and normal CI PASS `30546252168`, both at `2114e3cd` | Visual/motion client checks remain TEST-004 |
| TEST-004 | Prepare client acceptance steps | Req. 21 | Runtime work | CLIENT_TEST_REQUIRED | `docs/testing/plans/V0.0.2-client-acceptance.md` | Reviewed fixed candidate |
| HANDOFF-001 | Reports/handoff/artifact matrix/readiness | Req. 16, 24–26 | Tests | PLUGIN_REVIEW_REQUIRED | V0.0.2 handoff packet and test report | Review/owner/client outcomes |
| GIT-001 | Create/push initial foundation and Draft PR | Exec. 3.2–3.4 | AUTH-002 | DONE | Commit `ae0f80237113fb96dffd6934d016b19d4e768f18`; Draft PR #14 | None |
| RUNTIME-001 | Project Runtime operations | Req. 22; AGENTS | None | NOT_APPLICABLE | Repository boundary | Project-owned |
| RELEASE-001 | Merge/tag/release publication | Final instruction | Review/handoff | OTHER_BLOCKED | No dispatch authorized | Explicit later approval |

## Historical baseline observations

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
