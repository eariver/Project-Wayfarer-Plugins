# V0.0.2 Migration and Compatibility

| Component | Version | Status |
|---|---|---|
| Core API | V0.0.1 | unchanged; executable compatibility baseline passes |
| Core migrations | V001–V003 | byte-identical and immutable |
| Main config | 1 | new gameplay-module config |
| Main migrations | V001–V003 | additive; isolated tests pass |
| Frontier config | 1 | new gameplay-module config |
| Frontier migrations | V001–V002 | additive; isolated tests pass |

Main/Frontier require Core `>=0.0.1 <0.1.0`. Upgrade tests cover an empty schema and prior module
migrations, then repeated validation. Main V003 adds current physical-instance authority. A
broken V004 fixture fails exceptionally and is never counted successful. Migration locations do
not apply Core or sibling-module tables.

ADR 0009 authorizes module-local migration ownership in plugin code. It does not authorize
Project Runtime changes. Do not manually edit `flyway_schema_history`, apply these migrations to
Project databases, or treat test execution as runtime authorization.
