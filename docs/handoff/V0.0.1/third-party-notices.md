# V0.0.1 Third-party Notices

The Core Shadow JAR bundles the following direct libraries and their runtime transitives. Versions
are fixed by `gradle/libs.versions.toml` and the resolved `runtimeClasspath`.

| Component | Version | License |
|---|---:|---|
| HikariCP | 7.0.2 | Apache-2.0 |
| Flyway Core / MySQL | 12.6.2 | Apache-2.0 |
| MariaDB Connector/J | 3.5.8 | LGPL-2.1-or-later |
| Lettuce | 7.2.1.RELEASE | MIT |
| SLF4J API | 2.0.17 | MIT |
| Jackson annotations | 2.21 | Apache-2.0 |
| Jackson core/databind | 3.1.1 | Apache-2.0 |
| Redis AuthX core | 0.1.1-beta2 | MIT |
| Netty modules | 4.2.5.Final | Apache-2.0 |
| Reactor Core | 3.6.6 | Apache-2.0 |
| Reactive Streams | 1.0.4 | MIT-0 |

Paper API is compile-only and is not bundled. JUnit, Mockito, Testcontainers, and Docker Java are
test-only and are not bundled. Main and Frontier are separate modules and are not included in the
Core candidate.

The repository `LICENSE`, dependency-provided license/notice resources, and merged service metadata
are included according to the Shadow packaging configuration. This inventory is evidence for the
candidate, not legal advice and not a release publication.
