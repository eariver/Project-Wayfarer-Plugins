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
11. Keep requirements, source provenance, design, traceability, verification evidence,
    release-readiness records, and handoff documents under version control in this repository.
    Handoff references must use immutable commit SHAs or release tags; conversation-only
    decisions and mutable branch URLs are not sufficient authority.

## V0.0.2 redesign authority

The current V0.0.2 redesign is governed by `docs/V0.0.2-redesign/`.
Read its `README.md` and `STATUS.md` before any V0.0.2 work.

The project uses a tailored V-model aligned to the SWE.1 through SWE.6 process sequence:

- SWE.1 Software Requirements Analysis: ChatGPT authors; Owner reviews/approves.
- SWE.2 Software Architectural Design: ChatGPT authors; Owner reviews/approves.
- SWE.3 Software Detailed Design: ChatGPT authors; Owner reviews/approves.
- SWE.3 Unit Construction: Codex implements only from the approved detailed design and supplies an
  implementation-to-detailed-design consistency record; ChatGPT independently reviews it.
- SWE.4 Software Unit Verification: Codex implements and executes the approved verification design;
  ChatGPT independently reviews results and coverage.
- SWE.5 Software Integration and Integration Test: Codex integrates and executes the approved plan;
  ChatGPT independently reviews results, interfaces, and traceability.
- SWE.6 Software Qualification Test: Codex executes the approved qualification specification;
  ChatGPT independently reviews evidence and qualification status.

This tailoring is the Project Wayfarer development workflow. It does not by itself assert formal
Automotive SPICE assessment or certification.

## Specification and implementation authority

- The Project Owner owns product intent, scope decisions, unresolved gameplay choices, acceptance,
  and release authorization.
- ChatGPT owns SWE.1 through the detailed-design portion of SWE.3, including requirements,
  architecture, detailed behavior, state machines, interfaces, error semantics, permission rules,
  threading, persistence, observability, traceability, acceptance criteria, and verification design.
- Codex is the implementation and execution agent. It must not invent missing behavior, select an
  alternative architecture, alter acceptance criteria, or broaden/narrow scope.
- Codex may make only mechanical adjustments that do not change approved semantics, such as imports,
  formatting, compiler-required typing, and test-fixture plumbing.
- When approved design is incomplete, contradictory, or not implementable without a new product
  decision, Codex must stop and report `DESIGN_BLOCKED`. It must not infer a resolution.
- Every implementation delivery must include a tracked consistency record mapping approved detailed
  design identifiers to changed production files, implementation units, and deviations. An empty
  deviation list must be explicit; undocumented deviation is a failure.
- Codex test results and completion claims are evidence, not acceptance. ChatGPT review and Owner
  approval remain required at the defined gates.
- No implementation, build-candidate creation, runtime test, or release operation is authorized while
  `docs/V0.0.2-redesign/STATUS.md` says the applicable predecessor gate is incomplete.

## Domain documents and linked identifiers

Read and comply with:

`docs/V0.0.2-redesign/00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md`

- Requirements, architecture, detailed design, verification, and evidence are divided by controlled
  functional or cross-cutting domain documents.
- Codex must use full linked identifiers such as
  `V002-SWE3-MAIN-004-DD-017`; local shorthand such as `DD-017` is insufficient in work orders,
  implementation records, tests, reports, or reviews.
- Each implementation unit in the consistency record receives a linked `IMP` identifier and maps to
  one or more approved full `DD` identifiers.
- Codex must not merge domains, duplicate normative items, invent new document IDs, renumber approved
  items, or assign a new plugin-domain code.
- When an implementation task cannot be mapped unambiguously to an approved document-linked detailed
  design item, stop with `DESIGN_BLOCKED`.

## External library and platform API authority

Read and comply with:

`docs/V0.0.2-redesign/00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md`

- Java standard APIs, Paper/Spigot/Bukkit APIs, adopted plugin boundaries, and approved external
  libraries must be used instead of unnecessary Project-owned reimplementation when they adequately
  satisfy the approved design.
- Codex may use only dependencies, versions, packaging modes, adapter boundaries, and API contracts
  explicitly approved by the current SWE.2/SWE.3 baseline.
- Codex must not add, remove, replace, upgrade, shade, relocate, or change the scope of a dependency
  without an approved design update.
- Codex must not replace an approved library capability with custom code or copy external-library
  source into the Project.
- Codex must not rely on undocumented platform or library behavior. When an implementation detail
  reveals that the approved official reference is incomplete, incompatible, or incorrect for the
  selected version, stop with `DESIGN_BLOCKED`.
- The implementation-to-detailed-design consistency record must identify the actual Java,
  Paper/Spigot/Bukkit, adopted-plugin, and external-library APIs used and map them to the approved
  design references.

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
  restart servers, or perform server deployment from this repository unless an approved tracked
  qualification-test instruction explicitly authorizes a disposable test environment.
- After triggering a workflow, Codex must monitor it to completion and report:
    - workflow run URL;
    - conclusion;
    - release tag;
    - source commit;
    - release asset names;
    - SHA-256 manifest.
