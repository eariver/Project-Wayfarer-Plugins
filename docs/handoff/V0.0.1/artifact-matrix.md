# V0.0.1 Artifact Matrix

The table describes the required stable handoff state. Current readiness is tracked separately in
`release-readiness.md`.

| Artifact | Plugin-side implementation | Plugin-side test | Release | Project placement | Project acceptance | Roadmap Order |
|---|---|---|---|---|---|---|
| Wayfarer_Core | rc.1 pre-client candidate | automated/headless run `30317207610` and CI `30317207616` passed; client acceptance pending | target V0.0.1 | Main + Frontier | pending | 9 |
| Wayfarer_Main | not included | N/A | N/A | Main only | pending | 10 |
| Wayfarer_Frontier | not included | N/A | N/A | Frontier only | pending | 11 |
| Wayfarer_Frontier_EliteMobsMVI | not authorized / not included | N/A | N/A | Frontier only | pending | Decision Gate |

Only the Core row may become a V0.0.1 release asset.

The tested candidate `wayfarer-core-0.0.1-rc.1.jar` has SHA-256
`f36fe57370b4d123b13b5bf328c029c03407338e83e781953db81547de8a334a` at source
`6d25105f516a76cc373e5259fcef9d34de414543`. It is a candidate, not a published release asset.
