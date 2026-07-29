# V0.0.2 Decision Register

No proposal in this file silently becomes Project authority. Tunable values use the accepted
baseline; Owner-facing items remain configurable and block stable approval, not independent work.

| ID | Class | Question | Provisional / recommended answer | Alternatives | Impact / rollback | Gate |
|---|---|---|---|---|---|---|
| MAIN-D01 | TUNABLE | Exact block weights / ore multipliers | Implement requirement baseline in fixed-point units | Adjust after measured playtest | Gameplay pace only; config rollback | NONE |
| MAIN-D02 | TUNABLE | Threshold coefficients | Implement 100/400/1200 plus 800+200n+40n² | Revised monotonic formula | Re-derived from cumulative progress; config rollback | NONE |
| MAIN-D03 | TUNABLE | Repair price | Implement specified baseline formulas | Measured economy adjustment | No progress mutation; config rollback | NONE |
| MAIN-D04 | UNRESOLVED | Growth Tool GUI layout/language | Japanese 27-slot status view and separate 27-slot repair confirmation; cancel closes/returns safely | English or different slot layout | Presentation only; config/code layout rollback | OWNER_APPROVAL_REQUIRED |
| MAIN-D05 | UNRESOLVED | Item name/lore | Japanese name; concise owner-safe lore with Evolution/status/branch only; no UUID/internal fields | English/minimal lore | Presentation only; regenerate canonical item metadata | OWNER_APPROVAL_REQUIRED |
| MAIN-D06 | UNRESOLVED | Pending Delivery player UI | Two-line sanitized Japanese chat notification; no raw IDs | Action bar or GUI later | No schema impact; message config rollback | NONE |
| MAIN-D07 | UNRESOLVED | Admin commands/permissions | Explicit `wayfarer.main.admin.*` nodes and no wildcard dependency; mutation commands separated from inspect | Alternate syntax | Permission/handoff change; remove nodes/commands | PLUGIN_REVIEW_REQUIRED |
| MAIN-D08 | UNRESOLVED | External repair guards | Native Bukkit interaction guards first; add only verified external hooks | Broad plugin scan | Fail closed for owned items; remove optional adapter | PLUGIN_REVIEW_REQUIRED |
| MAIN-D09 | DEFERRED | Netherite timing/price | Not implemented in V0.0.2 | Later approved transaction | No V0.0.2 data path | DEFERRED_BY_REQUIREMENT |
| MAIN-D10 | PROJECT-OWNED | Pre-release reset/preserve | Never reset from Plugin repository | Project Order 25 decision | Destructive Project data boundary | OTHER_BLOCKED |
| FRONT-D01 | UNRESOLVED | Missing `frontier_iris` behavior | Enable administrative health only; keep gameplay/listeners/schedulers disabled and report DEGRADED/DOWN safely | Disable whole plugin | No world creation or data mutation; config rollback | OWNER_APPROVAL_REQUIRED |
| FRONT-D02 | UNRESOLVED | Exact LeafGrapple API/class boundary | Verify 1.0.2 descriptor/public API; use supported public boundary only, otherwise capability unavailable | Reflection/internal adapter after review | No fallback item; remove adapter integration | PLUGIN_REVIEW_REQUIRED |
| FRONT-D03 | UNRESOLVED | Launchpad creation snapshot | Snapshot max uses, expiry duration/result, velocities, cooldown, auto-Elytra, material and orientation | Read all live config | Predictable existing records; additive migration if later expanded | PLUGIN_REVIEW_REQUIRED |
| FRONT-D04 | UNRESOLVED | WorldGuard/WorldEdit/FAWE protection | Native Bukkit guards mandatory; supported public protection hooks only; unsupported bulk-edit protection disclosed/fail-closed | Private/reflection hooks | Stable release depends on reviewed protection sufficiency | PLUGIN_REVIEW_REQUIRED |
| FRONT-D05 | UNRESOLVED | Navigation GUI layout/language | Japanese 27-slot GUI exposing Loadout, Shop, Help; Waystone/Discovery/Teleport slots shown unavailable, not actionable | Hide deferred entries completely | Presentation only; layout rollback | OWNER_APPROVAL_REQUIRED |
| FRONT-D06 | UNRESOLVED | Shop Pending Delivery representation | Durable item-kind/quantity/idempotency record tied to Core transaction ID, with no raw serialized ItemStack | Refund-only on full inventory | Schema/reconcile impact; forward migration rollback only | PLUGIN_REVIEW_REQUIRED |
| FRONT-D07 | PROJECT-OWNED | Seed/border/generation | Plugin never creates or changes worlds | None | Project Runtime only | OTHER_BLOCKED |
| FRONT-D08 | PROJECT-OWNED | Portal deny implementation | Plugin exposes no fallback portal route; Project Order owns physical/config deny | None | Project Runtime only | OTHER_BLOCKED |
| FRONT-D09 | PROJECT-OWNED | Gate coordinates/safe arrival | No coordinates in plugin | None | Project Order 17 | OTHER_BLOCKED |
| FRONT-D10 | PROJECT-OWNED | MVI Runtime config | MVI remains sole normal-state authority; plugin only validates prerequisites | None | Project Orders 12/16 | OTHER_BLOCKED |
| FRONT-D11 | BLOCKS WAYSTONE | Template/palette | Not selected; production Waystone deferred | Future approved template | No Waystone tables/listeners/shop item in V0.0.2 runtime | DEFERRED_BY_REQUIREMENT |
| FRONT-D12 | BLOCKS WAYSTONE | Safe arrival/interaction | Not selected; production Waystone deferred | Future approved destination contract | No teleport in V0.0.2 | DEFERRED_BY_REQUIREMENT |
| FRONT-D13 | OPTIONAL | Resource pack/model | Use vanilla materials; no custom model requirement | Later Frontier pack | No code/data dependency | NOT_APPLICABLE |
| FRONT-D14 | DEFERRED | Ruined WM rewards | Not implemented | Later balance/reward work | No reward source | DEFERRED_BY_REQUIREMENT |
| FRONT-D15 | DECISION-GATED | EM–MVI adapter | Do not create module/artifact before `ADAPTER_REQUIRED` | Static registration, strict regex, then adapter | MVI/EliteMobs remain external authority | DEFERRED_BY_REQUIREMENT |

## Cross-cutting proposals

- ADR 0009 recommendation: module-local persistence lifecycles in Main and Frontier, using a
  private shared implementation library and no public Core database API.
- ADR 0010 expected consequence if ADR 0009 is approved: Core remains V0.0.1, V0.0.2 stable scope
  is `main-frontier`, and the immutable Core release is referenced as a dependency rather than
  renamed or reattached.
- Correction suffix grammar: `^V0\.0\.[1-9][0-9]*[a-z]?$`; suffixes are stable corrections, not
  pre-releases. No correction tag is created by this task.
