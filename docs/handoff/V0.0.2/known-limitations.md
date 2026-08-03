# V0.0.2 Known Limitations

## Transaction and persistence

- Core transaction state and module domain persistence are not cross-store atomic. Unconditional
  exactly-once is not claimed.
- `UNKNOWN` is an explicit recovery state; it is never automatically debited, refunded, or
  retried.
- Paid Main reissue may rotate authority before physical delivery. A Pending Delivery is then
  retried free; it never causes a second debit or rotation.
- Growth progress is session-cached. A crash may lose up to the configured checkpoint interval
  of non-critical progress.
- Normal inventories/profile state remains solely owned by the Minecraft backend/MVI; no raw
  Inventory or ItemStack is persisted in this repository's MariaDB schema.

## External integration

- The examined LeafGrapple 1.0.2 default tier is not a verified safe tier. Without an approved
  safe tier, the adapter fails closed and does not synthesize/fork hook physics. Any temporary
  safe tier for client testing is test-only, must copy movement/range/display values from a
  reviewed 1.0.2 standard tier, and is not a production balance recommendation.
- Native/public Launchpad protection paths are covered at the supported boundary. External tools
  that bypass Bukkit and the supported WorldEdit/WorldGuard APIs are not claimed covered; this is
  the accepted FRONT-D04 limitation.
- Native repair guards are implemented. External repair plugins without a supported cancellable
  event remain outside the MAIN-D08 claim.

## Gameplay and presentation boundaries

- Main death removes managed Growth Tool/Broken Tool from drops and does not automatically restore
  it. Player paid reissue is the recovery route.
- Frontier Elytra/Grappling Hook/Navigation use typed durable same-identity/epoch free redelivery;
  Launchpad/Rocket are excluded from permanent free redelivery.
- The Frontier plugin remains enabled when `frontier_iris` is absent or unloaded and never creates
  worlds. Launchpad expiration defers while the target world is unavailable; after a later load
  and relevant Join, WorldChanged, or Respawn path, exact player-triggered behavior resumes. No
  `WORLD_DOWN`, `DEGRADED`, or new health/status subsystem is added.
- The V0.0.2 world name is fixed to exact case-sensitive `frontier_iris`. Future single-name
  configurability is tracked by Issue #17 and is not implemented here.
- Candidate-5 Main hold-time authorization is intentionally fail-closed after every Main-Hand
  changing handler family and requires the exact managed-item/status/authority state for use;
  cancelled operations that leave the Main Hand unchanged do not permanently invalidate it.
- Candidate-5 Frontier timeout diagnostics retain only bounded, sanitized terminal observations;
  they include source, generation, pollCount, visible/required managed counts, fingerprint, and
  `decision=TIMEOUT`, without raw Player identifiers.
- Launchpad uses the current Player view direction at use time. Current config controls performance;
  persisted yaw is reserved/non-authoritative. A separate physical-material or full immutable
  performance snapshot is not claimed. When expiration classification is `UNKNOWN`, the
  destructive DB/index/block path is deferred for a later scheduler pass.
- Portal handling is exact: `FrontierGameplayRuntime` cancels `PlayerPortalEvent` when the
  Player's current world is `frontier_iris`. End Gateway is an observation for client/Project
  testing, not a proven separate interception.
- Issue #15 tracks the in-world Frontier return mechanism; Issue #16 tracks true orphan
  `BLOCK_ONLY` recovery.
- Japanese localization and GUI/presentation tuning are deferred; current English presentation is
  accepted for V0.0.2.

## Deferred and acceptance boundaries

- Phase 10B-A Candidate-1 failed Main first delivery and Frontier exact-world reconnect
  scenarios. Candidate-2 and Candidate-3 are rejected historical evidence; Candidate-4 was
  rejected before Client Test. Candidate-5 adds the focused Main authorization/action guards and
  Frontier terminal diagnostics/late-MVI coverage, but focused Client retest is still required;
  no Client Acceptance pass is claimed.
- Resource Pack rendering is `SKIPPED_OUT_OF_SCOPE_BY_OWNER` for this remediation phase.
- Waystone behavior and the EM–MVI adapter are absent/deferred; the adapter requires a Project
  decision of `ADAPTER_REQUIRED` before creation.
- FRONT-D01 is resolved for V0.0.2. FRONT-D02 remains a bounded client safe-tier/motion gate;
  FRONT-D04 and MAIN-D08 are accepted with the supported-boundary limitations above.
- No full Client Acceptance, Project acceptance, or Stable Release is claimed. Candidate-1
  through Candidate-4 are rejected/preserved; Candidate-5 Product is fixed for focused Client
  retest only.
- Project Runtime, permissions groups, configuration, worlds, databases, migrations, and servers
  were not changed by this repository task.

No secret, raw provider object/reference, internal exception message, or stack trace is intended
for Player-facing output.
