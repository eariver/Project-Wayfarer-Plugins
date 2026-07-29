# V0.0.2 Compatibility Matrix

| Consumer | Core requirement | Config | Migration | Upgrade from V0.0.1 | Downgrade |
|---|---|---|---|---|---|
| Wayfarer_Main V0.0.2 | `>=0.0.1 <0.1.0` | module config v1 | Main V001→V002 | isolated schema test passes; production lifecycle awaits ADR 0009 | V001 code after V002 is unsupported |
| Wayfarer_Frontier V0.0.2 | `>=0.0.1 <0.1.0` | module config v1 | Frontier V001→V002 | isolated schema test passes; production lifecycle awaits ADR 0009 | V001 code after V002 is unsupported |
| Wayfarer_Core V0.0.1 | self | Core config v1 | Core V001–V003 | unchanged; executable API baseline passes | existing V0.0.1 rollback authority |

Main and Frontier have no mutual dependency. Core has no dependency on either gameplay module.
Paper/Bukkit, JDBC, Hikari, Flyway, OfflinePlayer, and provider objects do not enter the public
JDK-only API.
