# Release Process

## 1. Purpose

Project Wayfarer Plugins uses separate gates:

1. Continuous Integration verifies every change.
2. Release-before validation fixes and tests a reviewed source commit.
3. A stable GitHub release packages that approved source commit for handoff to the main-server
   integration process.

For V0.0.1, ADR 0008 authorizes a Codex-operated local isolated Paper acceptance instead of a new
GitHub pre-release. The pre-release workflow remains available for other explicitly approved
release lines; it is not an input to V0.0.1 stable publication.

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

- `version`: human-facing pre-release with uppercase `V`, for example `V0.0.1-alpha.1`;
- `release_scope`: artifact set; the V0.0.1 line permits only `core`;
- `test_instructions`: optional Markdown path or commit-pinned URL describing the current
  test-server procedure.

The workflow must be run from `main`. It:

1. validates the pre-release version and selected source ref;
2. strips the leading `V` only when passing the version to Gradle and `plugin.yml`;
3. runs `clean check assemble` with that internal version;
4. collects exactly one Core runtime JAR and rejects every non-Core scope;
5. produces SHA-256 checksums, a release manifest, and dependency version evidence;
6. records release scope, source commit, configuration version, migration version, and hashes;
7. produces GitHub artifact attestations;
8. creates the uppercase-`V` annotated tag on the selected source commit;
9. publishes a GitHub pre-release marked as a test-server candidate.

Expected assets:

```text
Wayfarer_Core-<V-version>.jar
SHA256SUMS.txt
RELEASE_MANIFEST.md
DEPENDENCY_VERSIONS.toml
```

A pre-release is not approved for the main server. Test-server installation, configuration,
migrations, restarts, verification steps, evidence capture, and rollback remain manual and are
performed incrementally.

## 4. Main-server release

Use the **Create Main-Server Release** workflow only after:

- the reviewed stable source completed automated and local isolated test-server verification;
- the main-server project supplied its current requirements or work instruction as Markdown;
- every Plugin-side publication prerequisite was cleared;
- the stable source commit remains contained in the current `main` history.

The workflow and committed evidence are selected from `main`. The stable release JAR and stable
tag are produced from the exact reviewed `stable_source_commit`. Development may continue on
`main` during verification; later product commits are not silently included.

### Inputs

- `version`: stable human-facing version with uppercase `V`, for example `V0.0.1`;
- `release_scope`: artifact set; the V0.0.1 line permits only `core`;
- `stable_source_commit`: exact 40-character reviewed product-source SHA contained in `main`;
- `test_evidence`: committed local stable acceptance under `docs/testing/results/`;
- `main_server_instruction`: committed Markdown snapshot under
  `docs/requirements/main-server/`;
- `requirement_traceability`: committed Markdown under `docs/requirements/main-server/`;
- `release_readiness`: committed Markdown under `docs/handoff/`;
- `requirements_cleared`: explicit operator confirmation that test-server verification and all
  main-server requirements are complete.

The workflow rejects the request when:

- the stable version is malformed;
- `stable_source_commit` is malformed, missing, or not contained in `origin/main`;
- the stable tag or GitHub release already exists;
- the evidence/readiness files do not identify the stable source and expected stable JAR hash;
- traceability does not contain exactly `- Release gate: CLEARED`;
- release readiness does not contain exactly `- Release readiness: READY`;
- traceability contains `Not started`, `Failed`, or `Blocked`, or records nonzero
  `CODEX_FIXABLE`;
- a `Not applicable` traceability row lacks a reason;
- the workflow is not run from `main`;
- the operator does not explicitly confirm requirement clearance;
- the rebuilt stable Core JAR differs from the committed local stable candidate SHA-256.

After verification it checks out the reviewed stable source commit, rebuilds only the Core JAR
with the internal stable version, records the local isolated test authority and main-server
instruction in `RELEASE_MANIFEST.md`, creates build provenance, and publishes a stable GitHub
release.
The stable package also includes commit-pinned `TEST_SERVER_EVIDENCE.md` and
`MAIN_SERVER_INSTRUCTION.md` snapshots, plus fixed `REQUIREMENT_TRACEABILITY.md` and
`RELEASE_READINESS.md` assets with original paths, source commit, SHA-256, and gate values in the
manifest.

Stable publication has four independent gates: traceability `CLEARED`, readiness `READY`,
operator-supplied `requirements_cleared=true`, and `main-server-release` Environment approval.
The boolean input alone is insufficient.

A stable release means **ready for source-side handoff**. It does not itself authorize or execute
runtime deployment. `CLEARED` and `READY` similarly exclude Project placement, Runtime migration,
Project acceptance, and Roadmap completion.

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

Project Wayfarer remains on V0.1.0 while Plugin releases are technically restricted to
`V0.0.<positive integer>` and matching pre-releases. The initial progression is:

```text
V0.0.1-alpha.1
V0.0.1-alpha.2
V0.0.1-alpha.3
V0.0.1-alpha.4
V0.0.1-beta.1
V0.0.1-rc.1
V0.0.1
```

A new pre-release candidate receives a new pre-release version when the Owner selects that path.
ADR 0008 selects direct stable publication for V0.0.1 after commit-pinned local acceptance. Do not
replace an existing release asset or move an existing release tag. Human-facing versions, tags,
release names, documentation directories, and JAR filenames use uppercase `V`; Gradle and
`plugin.yml` omit it.

## 7. V0.0.1 artifact scope

The initial release series requires `release_scope=core` and includes only `Wayfarer_Core`.
Main and Frontier may remain buildable
skeletons but their JARs are not release assets. The conditional EliteMobs–MVI adapter is neither
authorized nor included. `core-main`, `core-frontier`, and `all` are reserved inputs and fail
closed throughout V0.0.1.

Stable publication verifies `release_scope=core`, stable-source ancestry, committed local evidence,
and the expected stable candidate hash before building. The manifest records the source commit,
scope, config version, migration version, local test operator/method, and SHA-256 values. A missing
Core config or migration version blocks publication.

## 8. Approval records

Before a pre-release run, display the repository, workflow, selected ref, source commit, release
version, scope, test instruction, expected assets, and Environment, then obtain explicit user
approval. Before stable release, display the reviewed stable source, committed local test evidence,
immutable mainline requirement reference, traceability result, readiness result, known
limitations, open decisions, and the user-supplied `requirements_cleared` value.

Codex does not infer `requirements_cleared=true`.

The Project mainline requirement snapshot and Codex work order are separate authorities. The
snapshot is stored under `docs/requirements/main-server/`; the derived execution instruction is
stored under `docs/work-orders/` and does not replace the snapshot.

## 9. Source repository rules

- Do not commit release JARs to this repository.
- Do not rewrite an existing release tag.
- Do not modify an existing release asset in place.
- Applied Flyway migrations remain immutable.
- Release publication does not imply Project Wayfarer Runtime acceptance.
