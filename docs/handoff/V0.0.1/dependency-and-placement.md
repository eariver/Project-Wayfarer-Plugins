# V0.0.1 Dependency and Placement

| Component | Version | Placement | Required | Notes |
|---|---|---|---|---|
| Paper | 1.21.11 | Main + Frontier runtime | Yes | Java 25 |
| Wayfarer_Core | V0.0.1 | Main + Frontier | Yes | This release |
| MariaDB | Project-approved | External service | Yes | Core schema authority |
| Redis | Project-approved | External service | Conditional by feature | Not persistent authority |
| VaultUnlocked | 2.20.2 locked baseline | Main + Frontier as Project defines | Waymark operations | Formal boundary |
| RedisEconomy | 4.5.12-wayfarer.1 locked baseline | Main + Frontier | Waymark provider | Never access internal keys |

Load order and exact provider compatibility are recorded after capability testing. Main, Frontier,
and the conditional adapter JARs are not included in V0.0.1.
