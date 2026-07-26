# Release Process

## 1. Purpose

Project Wayfarer Plugins uses three separate gates:

1. Continuous Integration verifies every change.
2. A GitHub pre-release packages a candidate for step-by-step test-server verification.
3. A stable GitHub release packages an approved source commit for handoff to the main-server integration process.

GitHub Actions never installs plugins, runs Flyway migrations, modifies runtime configuration,
restarts a server, accepts a runtime result, or performs rollback.

## 2. Continuous Integration

`.github/workflows/ci.yml` runs on:

- pushes to `main`;
- pull requests targeting the repository.

It checks out the repository, sets up Java 25, and runs:

```bash
./gradlew --no-daemon check
./gradlew --no-daemon assemble
```

A failed CI run blocks release preparation. CI artifacts are temporary build outputs and are not
release-authoritative packages.

## 3. Test-server pre-release

Use the **Create Test-Server Pre-release** workflow when source implementation and automated tests
are complete and the remaining work is step-by-step verification on the test server.

### Inputs

- `version`: pre-release SemVer without a leading `v`, for example `0.1.0-alpha.1`;
- `test_instructions`: optional Markdown path or URL describing the current test-server procedure.

The workflow must be run from `main`. It:

1. validates the pre-release version and selected source ref;
2. runs `clean check assemble` with the requested release version;
3. collects exactly one runtime JAR from each Plugin module;
4. produces SHA-256 checksums, a release manifest, and dependency version evidence;
5. produces GitHub artifact attestations;
6. creates an annotated tag on the selected source commit;
7. publishes a GitHub pre-release marked as a test-server candidate.

Expected assets:

```text
Wayfarer_Core-<version>.jar
Wayfarer_Main-<version>.jar
Wayfarer_Frontier-<version>.jar
SHA256SUMS.txt
RELEASE_MANIFEST.md
DEPENDENCY_VERSIONS.toml
```

A pre-release is not approved for the main server. Test-server installation, configuration,
migrations, restarts, verification steps, evidence capture, and rollback remain manual and are
performed incrementally.

## 4. Main-server release

Use the **Create Main-Server Release** workflow only after:

- the selected pre-release completed test-server verification;
- the main-server project supplied its current requirements or work instruction as Markdown;
- every applicable requirement was cleared;
- the selected `main` commit is exactly the source commit of the approved pre-release.

### Inputs

- `version`: stable SemVer without a leading `v`, for example `0.1.0`;
- `approved_prerelease_tag`: the verified candidate, for example `v0.1.0-alpha.3`;
- `main_server_instruction`: required Markdown path or URL containing the main-server requirements;
- `requirements_cleared`: explicit operator confirmation that test-server verification and all
  main-server requirements are complete.

The workflow rejects the request when:

- the stable version is malformed;
- the approved tag is not an existing GitHub pre-release;
- the workflow is not run from `main`;
- the approved pre-release tag points to a different source commit;
- the operator does not explicitly confirm requirement clearance;
- the target stable release already exists.

After verification it rebuilds the three JARs with the stable version, records the approved
pre-release and main-server instruction in `RELEASE_MANIFEST.md`, creates build provenance, and
publishes a stable GitHub release.

A stable release means **ready for source-side handoff**. It does not itself authorize or execute
runtime deployment.

## 5. Main-server handoff

The Project Wayfarer main-server integration process owns:

- confirming the current Markdown requirements and authority;
- retrieving release assets and verifying SHA-256 values;
- recording source commit, release tag, artifact hashes, configuration version, and migration state;
- backup and restore preparation;
- Plugin placement and dependency checks;
- Flyway migration execution when approved;
- runtime configuration and permission application;
- restart sequencing;
- acceptance evidence;
- rollback decisions.

## 6. Version progression

Recommended progression:

```text
0.1.0-alpha.1
0.1.0-alpha.2
0.1.0-beta.1
0.1.0-rc.1
0.1.0
```

A new test-server candidate always receives a new pre-release version. Do not replace an existing
release asset or move an existing release tag.

## 7. Source repository rules

- Do not commit release JARs to this repository.
- Do not rewrite an existing release tag.
- Do not modify an existing release asset in place.
- Applied Flyway migrations remain immutable.
- Release publication does not imply Project Wayfarer Runtime acceptance.
