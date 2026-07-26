# Release Process

1. `check` passes.
2. Build three shadow JARs.
3. Record source commit.
4. Record Gradle, Java, dependency and migration versions.
5. Calculate SHA-256.
6. Produce test report.
7. Hand artifacts and metadata to Project Wayfarer integration task.
8. Do not push JARs into either source repository.
