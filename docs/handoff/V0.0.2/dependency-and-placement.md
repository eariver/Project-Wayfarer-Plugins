# V0.0.2 Dependency and Placement

| Artifact | Placement | Hard dependency | Optional dependency |
|---|---|---|---|
| Wayfarer_Core V0.0.1 | Main + Frontier | Vault; RedisEconomy selected through Vault | none |
| Wayfarer_Main V0.0.2 | Main backend only | Wayfarer_Core `>=0.0.1 <0.1.0` | none |
| Wayfarer_Frontier V0.0.2 | Frontier backend only | Wayfarer_Core `>=0.0.1 <0.1.0` | MVI, LeafGrapple 1.0.2, WorldGuard/WorldEdit |

Load Core before the gameplay module. Main must not be placed on Lobby/Frontier. Frontier must not
be placed on Lobby/Main and never creates `frontier_iris`. MVI remains the sole normal inventory
authority. No EliteMobs–MVI adapter is included.

Main and Frontier shade only their private common/adapter implementation dependencies; they do not
package `wayfarer-api` or Core classes.
