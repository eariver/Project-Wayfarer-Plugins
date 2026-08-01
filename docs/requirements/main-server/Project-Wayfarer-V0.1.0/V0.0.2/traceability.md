# V0.0.2 Requirement Traceability

Classification: pre-client Plugin implementation/evidence at product anchor
`7faf79081572df028a5ec19ccfbc820123180fc7` for Draft PR #14.

The immutable mainline requirement SHA-256 is
`2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F`.
Owner Amendments are documented as a separate decision layer; they do not rewrite the immutable
requirement. `requirements_cleared` is absent and is not inferred.

| Requirement / area | Implementation path | Automated or source evidence | Headless / client / Project evidence | State | Notes |
|---|---|---|---|---|---|
| Governance and authority | `docs/decisions/V0.0.2/`, `docs/work-orders/V0.0.2/` | Hash and Git boundary checks | Project Runtime unchanged | `DONE` | Phase 07 is documentation/full validation only. |
| Core compatibility and migrations | Core API, V001–V003 | Compatibility and immutable migration tests | None required here | `DONE` | Core V0.0.1 is reused unchanged. |
| Main progress | Main progress/threshold domain | Saturation and terminal idempotency tests | Client mining later | `DONE` | Positive addition saturates at `Long.MAX_VALUE`; no negative wrap. |
| Main death/reissue | Main death policy, V004, reissue coordinator/commands | Death, quote, payment, rotation, pending, recovery tests | Client death/reissue later | `DONE` | No automatic respawn restoration; paid reissue; pending retry is free. |
| Main transaction recovery | Core transaction boundary and Main recovery | Idempotency/CAS and recovery tests | Project provider verification later | `DONE_WITH_LIMITATION` | `UNKNOWN` is explicit/manual and never auto-redebited or retried. |
| Main permissions | Main descriptor and leaf policy | Phase 06 descriptor/policy tests | LuckPerms Project check later | `DONE` | Old exact broad node is inactive. |
| Frontier permanent-item death | Typed durable Pending Delivery and Safe Entry/respawn reconciliation | Delivery/death identity and persistence tests | Client redelivery later | `DONE` | Elytra/Grappling Hook/Navigation redeliver free at same identity/epoch. |
| Frontier Launchpad | Launchpad authority, current config, placement/use/reconcile | Identity/replay/placement/protection tests | Client motion/protection review later | `DONE_WITH_LIMITATION` | Current view direction at use time; stored yaw is reserved, not authoritative. |
| Frontier portal boundary | `FrontierGameplayRuntime` | Source/runtime event coverage | Nether/End/End Gateway client observation later | `ACCEPTED_SCOPE` | Cancels `PlayerPortalEvent` when current world is `frontier_iris`; no separate End Gateway interception. |
| Frontier permissions | Frontier descriptor and leaf policy | Phase 06 descriptor/policy tests | LuckPerms Project check later | `DONE` | No top-level command permission; debug is separate. |
| FRONT-D01 | Frontier missing-world policy | Source/runtime boundary | Plugin review required | `PLUGIN_REVIEW_REQUIRED` | Not changed in Phase 07. |
| FRONT-D02 | LeafGrapple 1.0.2 public capability boundary | Fail-closed capability tests | Safe tier and client motion required | `EXTERNAL_BLOCKED` / `CLIENT_TEST_REQUIRED` | No fallback physics or private API claim. |
| FRONT-D04 | WorldGuard/WorldEdit/public protection boundary | Native/public hook tests | Plugin review required | `PLUGIN_REVIEW_REQUIRED` | Unsupported bypassing tools remain outside the claim. |
| MAIN-D08 | External repair guard matrix | Native-first guard evidence | Plugin review required | `PLUGIN_REVIEW_REQUIRED` | Unsupported cancellable boundaries remain limited. |
| Waystone | No production Waystone behavior | Absence/config/package checks | Not authorized | `DEFERRED_BY_REQUIREMENT` | Not an open V0.0.2 choice. |
| EM–MVI adapter | No module or artifact | Absence checks | Project decision required | `DEFERRED_BY_REQUIREMENT` | Create only after `ADAPTER_REQUIRED`. |
| Client Test Candidate / acceptance | Client plan only | No candidate fixed | Bounded client run required | `CLIENT_TEST_REQUIRED` | Candidate and acceptance remain unclaimed. |
| Project acceptance / Stable publication | Handoff and readiness docs | No release/tag/hash/dispatch | Project-owned | `PENDING` | No Client/Project acceptance, Stable Release, or `requirements_cleared`. |

Evidence types are intentionally separated: source/test evidence, headless evidence, client
observation, and Project acceptance are not interchangeable.
