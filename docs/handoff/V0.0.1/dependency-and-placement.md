# V0.0.1 Dependency and Placement

| Component | Version | Placement | Required | Notes |
|---|---|---|---|---|
| Paper | 1.21.11 | Main + Frontier runtime | Yes | Java 25 |
| Wayfarer_Core | V0.0.1-rc.1 pre-client candidate source | Main + Frontier | Yes | Only planned V0.0.1 runtime artifact |
| MariaDB | 11.8 isolated authority | External service | Yes when persistence enabled | Core schema authority, V001–V003 |
| Redis | 8-alpine isolated authority | External service | Yes when Redis enabled | Coordination/cache only |
| VaultUnlocked | 2.20.2 locked baseline | Main + Frontier as Project defines | Provider-gated | SHA-256 `BD9E7A31F1B2D31A591497174887EEA7AE7E632C6B179DA13E4F0AD732DE2DF7` |
| RedisEconomy | 4.5.12-wayfarer.1 locked baseline | Main + Frontier | Provider-gated | SHA-256 `AB00270CD970A909F54F6EE7C2C47151FB90DB0EA36FA6AB68AC59D939CFCA47`; never access internal keys |

`Wayfarer_Core` publishes the unshaded `wayfarer-api` contracts through Bukkit ServicesManager;
Main and Frontier must compile against that API identity and must not bundle a duplicate API copy.
Load order is Core before API consumers. Exact provider compatibility remains blocked pending the
ADR 0006 authority contract. For the fixed external economy chain, VaultUnlocked loads at
`STARTUP`, RedisEconomy loads at `STARTUP` with a hard `Vault` dependency and registers its default
Vault `Economy` during `onEnable`, and Core's soft dependencies place it after both. This ordering
is determinable, but ADR 0007 shows that the fixed Vault surface lacks the required atomic
completion/effect-lookup contract. Main,
Frontier, and the conditional adapter JARs are not included in V0.0.1 release assets.
