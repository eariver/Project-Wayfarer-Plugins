# V0.0.2 Artifact Matrix

Implementation evidence is fixed at
`ddc6711e358067414d180d0780eac490faf00dff`. No release candidate or stable
artifact is fixed.

| Area | State | Release artifact / dependency | Gate |
|---|---|---|---|
| Core | reused unchanged | released `Wayfarer_Core-V0.0.1.jar`; source `49e00e21716c1c13a2dbb170fdad1b19c4275612` | API and V001–V003 compatibility pass |
| Main module | production lifecycle wired; development artifact only | proposed `Wayfarer_Main-V0.0.2.jar` | Plugin review, Owner UI, client |
| Growth Tool | config/domain/persistence/gameplay/recovery wired | Main | Owner UI and client |
| Frontier foundation | production lifecycle wired; development artifact only | proposed `Wayfarer_Frontier-V0.0.2.jar` | external, Owner and client gates |
| Traversal loadout | identity, individual epoch reissue and pending delivery wired | Frontier | Owner UI and client |
| LeafGrapple | public 1.0.2 adapter complete; examined default unsafe | external LeafGrapple 1.0.2 | safe tier and client motion |
| Launchpad | state/use/placement/protection/expiration/reconcile wired | Frontier | external protection review and client |
| Shop | catalog, durable payment, pending delivery and idempotent replay wired | Frontier | client |
| Waystone | deferred | none | `DEFERRED_BY_REQUIREMENT` |
| EliteMobs–MVI adapter | prohibited/not present | none | Order 13 `ADAPTER_REQUIRED` |
| Project acceptance | pending | Main and Frontier accepted separately | reviewed candidate required |

The expected stable scope remains `main-frontier` only if review confirms that Core requires no
change. The V0.0.1 Core JAR must not be renamed or reattached as V0.0.2.
