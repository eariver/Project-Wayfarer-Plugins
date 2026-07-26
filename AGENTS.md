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
