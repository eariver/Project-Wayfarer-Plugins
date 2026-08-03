# Codex Instructions

1. Read `docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md` before editing.
2. Preserve module, threading, persistence, identity, transaction and fail-closed boundaries.
3. Do not create `Wayfarer_Frontier_EliteMobsMVI` unless a Project-side Decision Report says `ADAPTER_REQUIRED`.
4. Never commit JARs, secrets, runtime configs, worlds, databases, logs or caches.
5. Do not perform Project Wayfarer Runtime changes from this repository.
6. Run the narrowest relevant tests, then `check`.
7. Applied Flyway migrations are immutable.
8. Main and Frontier may depend on Core; Core must not depend on Main or Frontier.
9. Normal inventories are owned by the Minecraft backend/MVI, never by this repository's MariaDB schema.
10. Report changed files, tests, limitations and any authority conflict.
11. Keep mainline requirements, source provenance, assessment, traceability, test evidence,
    release-readiness records, and handoff documents under version control in this repository.
    Handoff references must use immutable commit SHAs or release tags; conversation-only
    decisions and mutable branch URLs are not sufficient evidence.

## Specification and implementation authority

- The Project Owner and the current ChatGPT-authored tracked phase handoff own requirements,
  Product behavior, architecture choices, error semantics, acceptance criteria, and the required
  validation sequence for that phase.
- Codex/Luna is the implementation and execution agent. When the current phase handoff specifies an
  exact design or exact tests, Codex/Luna must implement that design and run those tests; it must not
  choose a substitute algorithm, broaden or narrow Product scope, reinterpret the acceptance rule,
  or create a different evidence contract.
- Codex/Luna may make only mechanical adjustments that do not alter the specified semantics, such as
  imports, formatting, local helper-fixture plumbing, or compiler-required generic typing.
- When the prescribed design conflicts with existing authority, cannot be implemented without an
  unapproved Product decision, or a prescribed command cannot be executed as written, Codex/Luna
  must stop before the conflicting Product change and report the exact conflict. It must not silently
  resolve the conflict or continue with an alternative design.
- Codex/Luna test results and completion claims are evidence, not final acceptance. Independent
  ChatGPT review and Owner acceptance remain required where the phase handoff says so.

## GitHub Actions release operations

- Codex may inspect CI runs, workflow runs, tags, releases, logs, and release assets using GitHub CLI.
- Codex may prepare inputs for pre-release and stable-release workflows.
- Before triggering `prerelease.yml`, Codex must display:
    - repository;
    - selected ref;
    - source commit;
    - release version;
    - release scope;
    - test instruction reference;
    - expected artifacts;
    - GitHub Environment;
      and wait for explicit user approval.
- Before triggering `release.yml`, Codex must additionally display:
    - approved pre-release tag;
    - approved source commit;
    - test-server evidence reference;
    - main-server instruction reference;
    - requirement traceability path and gate result;
    - release-readiness path and result;
    - known limitations;
    - open decisions;
    - requirements-clearance value;
      and wait for explicit user approval.
- Codex must never set `requirements_cleared=true` solely from its own inference.
- Codex must not install JARs, execute migrations, modify runtime configuration,
  restart servers, or perform server deployment from this repository.
- After triggering a workflow, Codex must monitor it to completion and report:
    - workflow run URL;
    - conclusion;
    - release tag;
    - source commit;
    - release asset names;
    - SHA-256 manifest.
