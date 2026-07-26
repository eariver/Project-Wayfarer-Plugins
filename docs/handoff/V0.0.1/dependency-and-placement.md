# V0.0.1 Dependency and Placement

| Component | Version | Placement | Required | Notes |
|---|---|---|---|---|
| Paper | 1.21.11 | Main + Frontier runtime | Yes | Java 25 |
| Wayfarer_Core | V0.0.1-alpha.1 source | Main + Frontier | Yes | Only planned V0.0.1 runtime artifact |
| MariaDB | Project-approved | External service | Later slice | Core schema authority; no alpha.1 connection |
| Redis | Project-approved | External service | Later slice | Not persistent authority; no alpha.1 connection |
| VaultUnlocked | 2.20.2 locked baseline | Main + Frontier as Project defines | Later Waymark operations | Formal boundary |
| RedisEconomy | 4.5.12-wayfarer.1 locked baseline | Main + Frontier | Later Waymark provider | Never access internal keys |

`Wayfarer_Core` publishes the unshaded `wayfarer-api` contracts through Bukkit ServicesManager;
Main and Frontier must compile against that API identity and must not bundle a duplicate API copy.
Load order and exact provider compatibility are recorded after capability testing. Main,
Frontier, and the conditional adapter JARs are not included in V0.0.1 release assets.
