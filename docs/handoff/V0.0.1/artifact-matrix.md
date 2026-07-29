# V0.0.1 Artifact Matrix

The table describes the required stable handoff state. Current readiness is tracked separately in
`release-readiness.md`.

| Artifact | Plugin-side implementation | Plugin-side test | Release | Project placement | Project acceptance | Roadmap Order |
|---|---|---|---|---|---|---|
| Wayfarer_Core | rc.3 fractional-balance candidate | 192 unit / 14 MariaDB / 6 Redis, dedicated fractional Vault/RedisEconomy standalone with final Vault/Wayfarer 37.5, and CI `30413198551` plus final probe-head CI `30445725741` passed | Unpublished rc.3 review candidate; no tag/release URL; stable V0.0.1 publication pending | Main + Frontier | pending | 9 |
| Wayfarer_Main | not included | N/A | N/A | Main only | pending | 10 |
| Wayfarer_Frontier | not included | N/A | N/A | Frontier only | pending | 11 |
| Wayfarer_Frontier_EliteMobsMVI | not authorized / not included | N/A | N/A | Frontier only | pending | Decision Gate |

Only the Core row may become a V0.0.1 release asset.

The tested candidate `wayfarer-core-0.0.1-rc.3.jar` has SHA-256
`6E58B501EF0B58AA19C9DD1A39D41ABE13173EDE32BE70E3DB0979CE10A3278F` at source
`95b2cf1ef159b4d16921ddb4c8698621b8134c3e`. It is a candidate, not a published release asset.
The rc.2 source/JAR/hash remain immutable historical evidence and were not overwritten.
