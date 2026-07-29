# V0.0.2 Blocking Register

Only tasks directly named in a blocker are stopped. Independent domain, config, tests, release
tooling and documentation continue.

## B-001 — Module persistence architecture review

- Category: `PLUGIN_REVIEW_REQUIRED`
- Affected requirement: Req. 6.5–6.6, 8.6, 11.4; execution instruction section 7
- Observed fact: Main and Frontier require authoritative `wf_main_*` and `wf_frontier_*`
  persistence and module-local Flyway locations. V0.0.1 intentionally publishes no JDBC or opaque
  database operations, while Core owns only its private Core pool and migrations.
- Evidence: ADR 0005, `WayfarerDatabase`, Core persistence architecture, current module scaffolds
- Why Codex cannot safely decide: every viable connection path changes a review-controlled
  architecture boundary: public Core API, internal extension registration, or multiple pools.
- Tasks directly blocked: final concrete module pool/migration/repository lifecycle; runtime
  publication of persistence-backed gameplay
- Tasks not blocked: pure domains, config, DDL draft, repository interfaces, PDC/item logic,
  threshold/cost/state machines, command/GUI proposals, world guards, release tooling and tests
  that do not pretend to prove real MariaDB persistence
- Safe provisional work completed: option comparison, module-only DDL and repository contracts,
  empty/upgrade/repeat/failure migration tests, sanitized configuration, and deliberately disabled
  plugin entry points
- Exact approval needed: approve or revise ADR 0009 before concrete persistence integration
- Options:
  - A: additive JDK-only public Core persistence API
  - B: Main and Frontier own bounded module-local Hikari/Flyway lifecycles, sharing only a private
    implementation library
  - C: Core internal extension registration
  - D: another design that exposes no JDBC/framework/server type
- Recommended option: B. It preserves the released API and dependency direction, keeps module
  migrations and failures isolated, and avoids turning Core into a gameplay repository host.
- Gameplay impact: module features remain fail-closed until their own persistence is healthy
- Data / Migration impact: two module-specific migration histories; at most one module pool in
  addition to Core on each backend; explicit pool budgets and shutdown order required
- Compatibility impact: no V0.0.1 API or Core migration change
- Security impact: repeated secret references but no resolved secret may be logged or persisted;
  each module must validate and release its own secret values
- Rollback: disable/remove the affected module without deleting schema; Core V0.0.1 remains usable

## B-002 — Owner approval for player-facing proposals

- Category: `OWNER_APPROVAL_REQUIRED`
- Affected requirement: MAIN-D04, MAIN-D05, FRONT-D01, FRONT-D05
- Observed fact: layout, language, item presentation and missing-world policy are Owner decisions.
- Evidence: requirement section 15 and decision register
- Why Codex cannot safely decide: these define final player-facing product behavior, not merely
  internal code structure.
- Tasks directly blocked: stable approval of those presentation/policy choices
- Tasks not blocked: safe configurable provisional implementations and all non-presentation logic
- Safe provisional work completed: proposals are tracked in the decision register
- Exact approval needed: accept or revise the four proposals before stable publication
- Options: listed per decision ID in `docs/decisions/V0.0.2/decision-register.md`
- Recommended option: use the safe provisional defaults so implementation and test can proceed
- Gameplay impact: presentation and degraded behavior only; core progression/state semantics stay
  fixed
- Data / Migration impact: none
- Compatibility impact: config defaults may change before stable publication
- Security impact: no raw UUID, provider reference, exception or secret in player text
- Rollback: change reviewed config/presentation before the candidate is fixed

## B-003 — Client acceptance

- Category: `CLIENT_TEST_REQUIRED`
- Affected requirement: requirement section 21
- Observed fact: visual GUI, item interaction, movement feel, LeafGrapple and Launchpad experience
  cannot be fully established by unit or headless tests.
- Evidence: client test plan to be added after runtime implementation
- Why Codex cannot safely decide: a real Minecraft client and user observation are required.
- Tasks directly blocked: final client acceptance result
- Tasks not blocked: all automated, MariaDB, headless, packaging and handoff preparation
- Safe provisional work completed:
  `docs/testing/plans/V0.0.2-client-acceptance.md`
- Exact approval needed: execute the bounded client checklist and record actual results
- Options: pass, plugin defect, configuration/external limitation
- Recommended option: run only the representative cases defined by the final client plan
- Gameplay impact: player-visible behavior
- Data / Migration impact: isolated task database only
- Compatibility impact: may identify a focused correction before stable release
- Security impact: verify sanitized messages and permission denial
- Rollback: discard task-only data and candidate runtime

## B-004 — Core transaction/domain fulfillment boundary

- Category: `PLUGIN_REVIEW_REQUIRED`
- Affected requirement: Req. 8.14, 9.15; execution instruction section 7
- Observed fact: V0.0.1 `WayfarerTransactions.execute` durably handles the provider debit and its
  own transaction state, but exposes no module domain-commit callback. It reaches `COMMITTED`
  before a Main repair or Frontier delivery has been persisted or applied.
- Safe provisional pattern: claim an idempotent module order before Core Transactions; after
  Core `COMMITTED`, record payment and retain fulfillment as durable pending delivery. Ambiguous
  effects become module `UNKNOWN` and are not automatically retried. Main never refunds when a
  physical repair effect may have occurred. Neither coordinator claims cross-store atomicity.
- Exact review needed: confirm whether Main repair may use a durable module recovery record plus a
  separately idempotent Core Waymark refund, or whether an additive JDK-only Core
  transaction-participant/recovery contract is required.
- Tasks directly blocked: final Main repair integration and final release scope if a Core addition
  is required
- Tasks not blocked: pure pricing, module repair state machine, GUI proposal, Frontier pending
  delivery, and all non-transaction runtime work
- Compatibility impact: a Core API addition changes ADR 0010 from `main-frontier` to `all`
- Security impact: no provider reference or exception message may become player-facing
- Rollback: retain V0.0.1 Core and leave Main repair fail-closed until the reviewed path exists

## B-005 — External gameplay integration boundaries

- Category: `EXTERNAL_BLOCKED`
- Affected requirement: Req. 12–13; FRONT-D02 and FRONT-D04
- Observed fact: the inspected LeafGrapple 1.0.2 default tier enables durability and entity
  hooking, so it cannot satisfy the V0.0.2 permanent-item contract. Native Bukkit event guards
  also cannot prove interception of every WorldEdit/FAWE bulk edit.
- Evidence:
  `docs/reports/V0.0.2-leafgrapple-1.0.2-capability-assessment.md` and the public capability probe
- Tasks directly blocked: canonical client hook motion; final Launchpad external-protection claim
- Tasks not blocked: fail-closed adapter, pure Launchpad state/use/placement, migration, package,
  configuration and bounded client plan
- Safe provisional work completed: exact-version public-method adapter, unsafe-tier rejection,
  no fallback physics/fork/private-field access, placement policy and known limitation
- Exact resolution needed: provide/approve a LeafGrapple tier with durability and entity hooking
  disabled; Plugin review must approve the supported public protection-hook matrix
- Security/compatibility impact: raw plugin objects and internal exceptions remain hidden; an
  unavailable capability produces no substitute item
- Rollback: omit/disable Frontier gameplay; do not alter the external plugin artifact
