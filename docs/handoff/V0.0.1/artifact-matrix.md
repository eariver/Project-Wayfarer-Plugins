# V0.0.1 Artifact Matrix

The table describes the required stable handoff state. Current readiness is tracked separately in
`release-readiness.md`.

| Artifact | Plugin-side implementation | Plugin-side test | Release | Project placement | Project acceptance | Roadmap Order |
|---|---|---|---|---|---|---|
| Wayfarer_Core | rc.2 concrete-provider candidate | 176 unit / 14 MariaDB / 6 Redis, dedicated Vault/RedisEconomy standalone, and CI `30378563840` passed | target V0.0.1 | Main + Frontier | pending | 9 |
| Wayfarer_Main | not included | N/A | N/A | Main only | pending | 10 |
| Wayfarer_Frontier | not included | N/A | N/A | Frontier only | pending | 11 |
| Wayfarer_Frontier_EliteMobsMVI | not authorized / not included | N/A | N/A | Frontier only | pending | Decision Gate |

Only the Core row may become a V0.0.1 release asset.

The tested candidate `wayfarer-core-0.0.1-rc.2.jar` has SHA-256
`8C85F9C0D42EED631F3167DE5827C21139D07B71A63CE3E0AC90F746F9A651E6` at source
`5039e008659be1f7e23658aabba12cb95a8a600d`. It is a candidate, not a published release asset.
