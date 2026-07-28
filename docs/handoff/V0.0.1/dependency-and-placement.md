# V0.0.1 Dependency and Placement

| Component | Version | Placement | Required | Notes |
|---|---|---|---|---|
| Paper | 1.21.11 | Main + Frontier runtime | Yes | Java 25 |
| Wayfarer_Core | V0.0.1 beta candidate source | Main + Frontier | Yes | Only planned V0.0.1 runtime artifact |
| MariaDB | 11.8 isolated authority | External service | Yes when persistence enabled | Core schema authority, V001–V003 |
| Redis | 8-alpine isolated authority | External service | Yes when Redis enabled | Coordination/cache only |
| VaultUnlocked | 2.20.2 locked baseline | Main + Frontier as Project defines | Provider-gated | Formal boundary |
| RedisEconomy | 4.5.12-wayfarer.1 locked baseline | Main + Frontier | Provider-gated | ADR 0006; never access internal keys |

`Wayfarer_Core` publishes the unshaded `wayfarer-api` contracts through Bukkit ServicesManager;
Main and Frontier must compile against that API identity and must not bundle a duplicate API copy.
Load order is Core before API consumers. Exact provider compatibility remains blocked pending the
ADR 0006 authority contract. Main,
Frontier, and the conditional adapter JARs are not included in V0.0.1 release assets.
