# External Library and Reference Policy

Document ID: `GOV-ENG-001`  
Revision: A  
State: `APPROVED_OWNER_POLICY`  
Date: 2026-08-05 JST  
Applicable scope: all current and future Project Wayfarer-owned plugins

## 1. Purpose

Project Wayfarer-owned plugins may use Java standard APIs, Paper/Spigot platform APIs, and external
libraries. The design must prefer established, suitable implementations over unnecessary custom
reimplementation while preserving maintainability, compatibility, licensing, security, and clear
runtime ownership.

This policy applies to Wayfarer_Core, Wayfarer_Main, Wayfarer_Frontier, any conditionally approved
adapter, and every later Wayfarer-owned plugin added to this repository.

## 2. Reuse-first principle

For each required capability, SWE.2 and SWE.3 must evaluate existing solutions before designing
Project-owned implementation.

Use the lowest-risk suitable solution in this order, adjusted where architecture or runtime ownership
requires a different choice:

1. Java standard library or language facility;
2. Paper API or, where relevant and supported, Spigot/Bukkit API;
3. an already approved dependency in the repository;
4. a suitable maintained or mature external library;
5. Project-owned implementation only when the preceding options do not satisfy the approved
   requirements and design constraints.

A Project-owned implementation must not duplicate a capability already adequately provided by the
selected platform or an acceptable external library merely for implementation convenience or because
its API was not investigated.

## 3. Acceptable maintenance profiles

An external library is acceptable only when its maintenance profile is explicitly assessed and falls
into at least one of these classes:

### A. Actively maintained

The project continues to publish releases, accept fixes, or otherwise demonstrates active maintenance
compatible with the target Java, Paper, and dependency ecosystem.

### B. Mature and effectively stable

The capability is complete, based on stable standards or algorithms, has no meaningful dependency on
rapidly changing runtime internals, and does not require frequent releases to remain safe and usable.
A lack of recent releases is not itself disqualifying when continued change is genuinely unnecessary.

### C. Slow-moving but low-churn

The project changes infrequently because its problem domain and API are stable. Compatibility,
security exposure, issue health, and target-runtime support must still be acceptable.

A library is not acceptable when it is abandoned in a way that creates unresolved compatibility,
security, correctness, licensing, or operational risk for the approved runtime.

## 4. Mandatory selection assessment

Every external dependency selected or retained by SWE.2/SWE.3 must record at least:

- capability and requirement/design identifiers served;
- candidate alternatives considered;
- selected artifact, group/module coordinates, and exact version or version policy;
- official documentation or Javadoc URL;
- official source repository and release/history URL;
- maintenance-profile classification and assessment date;
- evidence supporting that classification;
- license and redistribution/shading implications;
- Java and Paper/Spigot compatibility;
- transitive dependencies and notable conflicts;
- packaging decision, including provided/compile-only/runtime/shaded/relocated status;
- threading and lifecycle assumptions;
- failure behavior and fail-closed/fail-open implications;
- reason for rejection of custom implementation, or the approved reason custom implementation is
  still necessary.

The record may be contained in the applicable architecture or detailed-design document, a controlled
dependency register, or an ADR referenced by those documents.

## 5. Reference requirements

When a design depends on Java, Paper, Spigot/Bukkit, or an external library behavior, the design must
link the authoritative reference used to validate that behavior.

Preferred references are:

1. official Java API documentation;
2. official Paper Javadocs and Paper documentation;
3. official Spigot/Bukkit Javadocs when Paper does not provide the relevant contract or when the
   inherited contract must be shown;
4. official external-library documentation and versioned API references;
5. the upstream source repository, release notes, changelog, and artifact publication metadata;
6. a primary specification or standard when the implementation is based on one.

Unofficial tutorials, forum posts, generated summaries, and example snippets may be supplementary but
must not be the sole authority for event semantics, threading rules, cancellation behavior, lifecycle,
persistence, security, or compatibility decisions.

Where available, link the documentation matching the selected version. When only latest documentation
exists, record that limitation and verify the selected version's source or release notes.

## 6. SWE.2 obligations

Software Architectural Design must:

- identify capabilities delegated to platform APIs or external libraries;
- allocate ownership between Wayfarer modules, the Minecraft platform, adopted plugins, and libraries;
- prevent duplicate ownership of the same persistence, inventory, transaction, scheduling, or
  integration responsibility;
- document dependency direction and runtime deployment implications;
- record material dependency risks and alternatives.

## 7. SWE.3 obligations

Software Detailed Design must:

- identify the exact platform API classes, events, methods, and contracts relied upon;
- link the authoritative references that justify event priority, cancellation handling, thread
  affinity, scheduler use, inventory/item semantics, lifecycle, and failure behavior;
- identify exact external-library APIs used and the adapter/wrapper boundary, when applicable;
- avoid designing replacement implementations for functionality already supplied by a selected API or
  library;
- specify what Project-owned code adds beyond the delegated capability;
- define integration verification for assumptions at the platform/library boundary;
- identify any version-sensitive behavior and the response to future dependency upgrades.

A detailed design is not ready for implementation when a material platform or library assumption lacks
an authoritative reference or when the selected dependency has not passed the maintenance assessment.

## 8. Construction obligations

Codex must use only dependencies and platform contracts approved by the current SWE.2/SWE.3 baseline.
Codex must not:

- replace an approved library with custom code;
- introduce a new dependency;
- change a dependency version or packaging mode;
- rely on undocumented API behavior;
- copy library source into the Project;
- bypass an approved adapter boundary.

A required change to any of these items is `DESIGN_BLOCKED` until ChatGPT updates the controlled design
and the applicable gate is re-approved.

The implementation-to-detailed-design consistency record must identify the actual platform/library APIs
used and confirm that they match the approved references and dependency decision.

## 9. Verification implications

Wayfarer verification must test Project-owned behavior and the selected integration contract. It should
not recreate the external library's full internal test suite.

Verification must cover material assumptions such as:

- event delivery and cancellation behavior relied upon by the design;
- thread and scheduler boundaries;
- serialization and persistence adapter behavior;
- transaction and retry integration;
- library initialization, shutdown, and failure handling;
- shading/relocation or runtime-classpath conflicts;
- compatibility with the approved target runtime.

## 10. Change control

Adding, removing, replacing, or materially upgrading a dependency requires:

1. an updated maintenance and compatibility assessment;
2. updated authoritative references;
3. impact analysis against requirements, architecture, detailed design, implementation, and tests;
4. an ADR or equivalent controlled decision when the change is architecturally material;
5. approval at the applicable gate before construction.
