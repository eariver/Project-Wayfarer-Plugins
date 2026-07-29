# ADR 0008: Direct Stable Release After Local Isolated Acceptance

- Status: Accepted — Owner Decision
- Date: 2026-07-29
- Stable product source: `49e00e21716c1c13a2dbb170fdad1b19c4275612`
- Stable version: `0.0.1`
- Release scope: Core only

## Decision

V0.0.1 does not require a new GitHub pre-release. Release-before validation consists of the
reviewed and merged stable source, automated tests, a reproducible stable-version build, and a
Codex-operated local isolated Paper server with console-controlled health, representative
transaction, clean-stop, and restart evidence.

The stable artifact authority is the exact source commit, filename, and SHA-256. After preparation
review, V0.0.1 may be published directly as a GitHub stable release through the protected stable
workflow. Publication still requires explicit Owner `requirements_cleared=true`, the
`main-server-release` Environment approval, and the other displayed release inputs.

`V0.0.1-alpha.1` remains immutable historical evidence only. It is not the stable source or an
approval input for V0.0.1.

## Consequences

- `release.yml` accepts `stable_source_commit`, verifies that it is a 40-character repository
  commit contained in `origin/main`, and rebuilds/tag-targets that exact commit.
- The workflow compares the rebuilt Core JAR with the SHA-256 fixed by committed local acceptance
  evidence before it can reach publication approval.
- Release automation and committed evidence are read from the selected `main` revision; product
  code is built from the reviewed stable source.
- A pre-release tag, manifest, or GitHub pre-release is not required for this direct stable path.
- Stable publication rows can be `Ready for publication`; Project placement, runtime migration,
  smoke/acceptance, and Roadmap work remain `Project acceptance pending`.
- `CLEARED` and `READY` describe Plugin-side publication prerequisites. They do not claim Project
  Runtime installation or acceptance.
- This decision does not authorize this repository to deploy a JAR, run migrations, change
  configuration, restart a Project server, or set `requirements_cleared=true`.

## Evidence

The local acceptance result is
`docs/testing/results/V0.0.1-stable-local-acceptance.md`. The Project Runtime and Project repository
were unchanged.
