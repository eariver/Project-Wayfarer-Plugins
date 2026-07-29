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
- `release_scope`: `core`, `main-frontier`, or `all` as defined by ADR 0010;
- `test_instructions`: optional Markdown path or commit-pinned URL describing the current
  test-server procedure.

The workflow must be run from `main`. It:

1. validates the pre-release version and selected source ref;
2. strips the leading `V` only when passing the version to Gradle and `plugin.yml`;
3. runs `clean check assemble` with that internal version;
4. collects exactly the runtime JARs selected by the scope;
5. produces SHA-256 checksums, a release manifest, and dependency version evidence;
6. records release scope, source commit, configuration version, migration version, and hashes;
7. produces GitHub artifact attestations;
8. creates the uppercase-`V` annotated tag on the selected source commit;
9. publishes a GitHub pre-release marked as a test-server candidate.

Expected runtime assets depend on scope:

```text
core: Wayfarer_Core-<V-version>.jar
main-frontier: Wayfarer_Main-<V-version>.jar and Wayfarer_Frontier-<V-version>.jar
all: all three runtime JARs
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
- `release_scope`: `core`, `main-frontier`, or `all`; partial combinations are rejected;
- `stable_source_commit`: exact 40-character reviewed product-source SHA contained in `main`;
- `test_evidence`: committed local stable acceptance under `docs/testing/results/`;
- `main_server_instruction`: committed Markdown snapshot under
  `docs/requirements/main-server/`;
- `requirement_traceability`: committed Markdown under `docs/requirements/main-server/`;
- `release_readiness`: committed Markdown under `docs/handoff/`;
- `requirements_cleared`: explicit Owner confirmation that Plugin-side stable publication
  prerequisites are cleared and authorization to perform V0.0.1 source-side stable publication.
  Project Runtime placement, migration execution, configuration, smoke/acceptance, and Roadmap
  completion remain pending.

The workflow rejects the request when:

- the stable version is malformed;
- `stable_source_commit` is malformed, missing, or not contained in `origin/main`;
- the GitHub Release already exists;
- an existing stable tag is lightweight, cannot dereference to a commit, or does not point to the
  exact reviewed Stable Product Source;
- the evidence/readiness files do not identify the stable source and expected stable JAR hash;
- traceability does not contain exactly `- Release gate: CLEARED`;
- release readiness does not contain exactly `- Release readiness: READY`;
- any requirement Status is empty or outside the explicit terminal allow-list;
- `READY_FOR_PUBLICATION` or `PROJECT_ACCEPTANCE_PENDING` classification counts differ from their
  corresponding requirement Status rows;
- `CODEX_FIXABLE` is nonzero, or the remaining-work classification arithmetic differs from
  `Acceptance units`;
- a `Not applicable` traceability row lacks a reason;
- the workflow is not run from `main`;
- the Owner does not explicitly authorize stable source-side publication after confirming
  Plugin-side publication prerequisites are cleared;
- any rebuilt scoped runtime JAR differs from its committed stable candidate SHA-256.

An absent tag selects `new` publication mode. A pre-existing annotated tag is accepted only as
partial-publication recovery when it dereferences to the exact reviewed Stable Product Source and
the GitHub Release is absent. The tag object is reused without recreation, movement, force push, or
replacement. Existing Releases are never overwritten.

After verification it checks out the reviewed stable source commit, rebuilds the exact scoped
runtime JAR set with the internal stable version, records the test authority and main-server
instruction in `RELEASE_MANIFEST.md`, creates build provenance, and publishes a stable GitHub
release. A `main-frontier` package references the immutable V0.0.1 Core and does not rename or
reattach it.
The stable package also includes commit-pinned `TEST_SERVER_EVIDENCE.md` and
`MAIN_SERVER_INSTRUCTION.md` snapshots, plus fixed `REQUIREMENT_TRACEABILITY.md` and
`RELEASE_READINESS.md` assets with original paths, source commit, SHA-256, and gate values in the
manifest.

Stable publication has four independent gates: traceability `CLEARED`, readiness `READY`,
operator-supplied `requirements_cleared=true`, and `main-server-release` Environment approval.
The boolean input alone is insufficient and does not represent Project Runtime acceptance.

Before any new annotated tag is pushed, the Publish Job reruns the same package verifier used by
the Build Job. `RELEASE_NOTES.md` and `RELEASE_ASSET_FILENAMES.txt` must be regular non-symlink
files; the attachment list must contain exactly 19 unique safe filenames; every listed asset must
be a regular non-symlink file; SHA256SUMS must verify; and the Manifest and Artifact Matrix must
match the Product Source, Handoff Source, Stable JAR SHA, tag, and deterministic Release URL.
Failure stops publication before tag creation.

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
`V0.0.<positive integer>`, optional lowercase stable correction suffixes, and matching
pre-releases. Stable ordering is:

```text
V0.0.1 < V0.0.1a < V0.0.1b < V0.0.2
```

A suffix such as `V0.0.1a` is a stable correction, not a pre-release. The initial progression was:

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

If an annotated Stable Tag was pushed successfully but GitHub Release creation or asset upload
failed, rerunning the approved workflow is safe only when the Release remains absent and the
existing annotated tag still dereferences to the exact Stable Product Source. A lightweight tag,
wrong-source tag, malformed tag, completed Release, or indeterminate GitHub lookup fails closed.

## 7. Artifact scopes

V0.0.1 used `release_scope=core` and included only `Wayfarer_Core`. V0.0.2 adds:

- `core`: a V0.0.1-compatible Core correction;
- `main-frontier`: Main and Frontier while immutable V0.0.1 Core is reused;
- `all`: Core, Main, and Frontier after an intentional reviewed Core change.

`core-main`, `core-frontier`, single gameplay-module scopes, and arbitrary combinations fail
closed. The conditional EliteMobs–MVI adapter and deferred Waystone gameplay are not release
artifacts.

Stable publication verifies the exact scope, stable-source ancestry, committed evidence, and every
expected candidate hash before packaging. The manifest records the source commits, scope,
compatibility, and SHA-256 values.

## 8. Approval records

Before a pre-release run, display the repository, workflow, selected ref, source commit, release
version, scope, test instruction, expected assets, and Environment, then obtain explicit user
approval. Before stable release, display the reviewed stable source, committed local test evidence,
immutable mainline requirement reference, traceability result, readiness result, known
limitations, open decisions, and the user-supplied `requirements_cleared` value.

Codex does not infer `requirements_cleared=true`. Its meaning is limited to explicit Owner
authorization for source-side stable publication after Plugin-side publication prerequisites are
cleared; Project Runtime placement/acceptance remains pending.

The Project mainline requirement snapshot and Codex work order are separate authorities. The
snapshot is stored under `docs/requirements/main-server/`; the derived execution instruction is
stored under `docs/work-orders/` and does not replace the snapshot.

## 9. Source repository rules

- Do not commit release JARs to this repository.
- Do not rewrite an existing release tag.
- Do not modify an existing release asset in place.
- Applied Flyway migrations remain immutable.
- Release publication does not imply Project Wayfarer Runtime acceptance.
