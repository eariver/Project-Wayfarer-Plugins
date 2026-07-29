# V0.0.2 Third-party Notices

The repository license and dependency versions remain authoritative. Relevant runtime/build
boundaries include Paper API, Vault API, HikariCP, Flyway, MariaDB Connector/J, Lettuce,
Testcontainers, and LeafGrapple.

Main/Frontier packaging does not include Paper, Vault, Testcontainers, JUnit, Docker fixtures,
`wayfarer-api`, or Core classes. LeafGrapple itself is not bundled; the Frontier version adapter
uses its observed public 1.0.2 methods when the capability probe passes.

See `LICENSE` and `gradle/libs.versions.toml`. A final release package must include the license,
this notice, and the fixed dependency snapshot.
