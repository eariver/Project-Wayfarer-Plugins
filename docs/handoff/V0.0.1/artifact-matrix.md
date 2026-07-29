# V0.0.1 Artifact Matrix

The table describes the required stable handoff state. Current readiness is tracked separately in
`release-readiness.md`.

| Artifact | Plugin-side implementation | Plugin-side test | Release | Project placement | Project acceptance | Roadmap Order |
|---|---|---|---|---|---|---|
| Wayfarer_Core | Stable product source `49e00e21716c1c13a2dbb170fdad1b19c4275612` | 192 unit / 14 MariaDB / 6 Redis; reproducible stable JAR; isolated full-inventory 37.5/debit/replay/refund/restart acceptance passed with disclosed external-plugin limitation | Ready for explicit V0.0.1 publication; no tag/release URL yet | Main + Frontier | pending | 9 |
| Wayfarer_Main | not included | N/A | N/A | Main only | pending | 10 |
| Wayfarer_Frontier | not included | N/A | N/A | Frontier only | pending | 11 |
| Wayfarer_Frontier_EliteMobsMVI | not authorized / not included | N/A | N/A | Frontier only | pending | Decision Gate |

Only the Core row may become a V0.0.1 release asset.

The tested stable candidate `wayfarer-core-0.0.1.jar` has SHA-256
`B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2` at source
`49e00e21716c1c13a2dbb170fdad1b19c4275612`. It is locally accepted and ready for publication,
not a published release asset. Prior candidate evidence remains immutable.
