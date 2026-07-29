# V0.0.1 Dependency and Placement

| Component | Version | Placement | Required | Notes |
|---|---|---|---|---|
| Paper | 1.21.11 | Main + Frontier runtime | Yes | Java 25 |
| Wayfarer_Core | `0.0.1`; stable product source `49e00e21716c1c13a2dbb170fdad1b19c4275612` | Main + Frontier | Yes | Stable runtime JAR `Wayfarer_Core-V0.0.1.jar`; SHA-256 `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2`; planned V0.0.1 release, publication pending |
| MariaDB | 11.8 isolated authority | External service | Yes when persistence enabled | Core schema authority, V001–V003 |
| Redis | 8-alpine isolated authority | External service | Yes when Redis enabled | Coordination/cache only |
| VaultUnlocked | 2.20.2 locked baseline | Main + Frontier | Yes when Waymark enabled | SHA-256 `BD9E7A31F1B2D31A591497174887EEA7AE7E632C6B179DA13E4F0AD732DE2DF7` |
| RedisEconomy | 4.5.12-wayfarer.1 locked baseline | Main + Frontier | Yes when Waymark enabled | SHA-256 `AB00270CD970A909F54F6EE7C2C47151FB90DB0EA36FA6AB68AC59D939CFCA47`; never access internal keys |

`Wayfarer_Core` publishes the unshaded `wayfarer-api` contracts through Bukkit ServicesManager;
Main and Frontier must compile against that API identity and must not bundle a duplicate API copy.
Load order is VaultUnlocked → RedisEconomy → Wayfarer_Core → API consumers. VaultUnlocked and
RedisEconomy load at `STARTUP`; RedisEconomy has a hard `Vault` dependency and registers its
default Vault `Economy` during `onEnable`; Core's soft dependencies place it after both. Core
discovers that service and requires the safe provider identity `RedisEconomy`.

ADR 0007 records the accepted limitation that Vault success precedes durable Redis completion and
offers no operation-ID effect lookup. This is shared with existing Vault economy consumers and is
not a Wayfarer-only route. Main, Frontier, the test-only probe, and conditional adapter JARs are not
included in V0.0.1 release assets.

RC.3 source `95b2cf1ef159b4d16921ddb4c8698621b8134c3e` is retained only as historical
provenance for the fractional-balance correction; it is not the Stable Runtime identity.
