# V0.0.2 Artifact Matrix

Implementation evidence is fixed at
`981e425a4af619340b64b2060c0cb9ac7219cdd2`. No release candidate or stable
artifact is fixed.

| Area | State | Release artifact / dependency | Gate |
|---|---|---|---|
| Core | reused unchanged | released `Wayfarer_Core-V0.0.1.jar`; source `49e00e21716c1c13a2dbb170fdad1b19c4275612` | API and V001–V003 compatibility pass |
| Main module | partial, fail-closed | proposed `Wayfarer_Main-V0.0.2.jar` | ADR 0009 and B-004 |
| Growth Tool | domain/config/recovery draft complete; no production wiring | Main | Plugin review, Owner UI, client |
| Frontier foundation | partial, fail-closed | proposed `Wayfarer_Frontier-V0.0.2.jar` | ADR 0009 |
| Traversal loadout | domain/identity/pending delivery complete; no production wiring | Frontier | Plugin review and client |
| LeafGrapple | public 1.0.2 adapter complete; examined default unsafe | external LeafGrapple 1.0.2 | safe tier and client motion |
| Launchpad | state/use/placement/recovery draft complete; no production wiring | Frontier | protection review and client |
| Shop | catalog/order/idempotency draft complete; no production wiring | Frontier | persistence review and client |
| Waystone | deferred | none | `DEFERRED_BY_REQUIREMENT` |
| EliteMobs–MVI adapter | prohibited/not present | none | Order 13 `ADAPTER_REQUIRED` |
| Project acceptance | pending | Main and Frontier accepted separately | reviewed candidate required |

The expected stable scope remains `main-frontier` only if review confirms that Core requires no
change. The V0.0.1 Core JAR must not be renamed or reattached as V0.0.2.
