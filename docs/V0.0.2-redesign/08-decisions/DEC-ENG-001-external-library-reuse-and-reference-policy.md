# DEC-ENG-001 — External Library Reuse and Reference Policy

Status: `APPROVED`  
Decision date: 2026-08-05 JST  
Decision owner: Project Owner  
Applies to: all current and future Project Wayfarer-owned plugins

## Context

The prior V0.0.2 implementation accumulated avoidable custom code and insufficiently justified use of
Paper event and item contracts. The redesign must reduce unnecessary implementation ownership and make
platform/library assumptions reviewable before Codex construction.

## Decision

1. Wayfarer_Core, Wayfarer_Main, Wayfarer_Frontier, conditionally approved adapters, and future
   Wayfarer-owned plugins may use external libraries.
2. A selected external library must be actively maintained, mature enough not to require ongoing
   change, or slow-moving because its capability and API are stable. Abandoned dependencies with
   unresolved compatibility, security, correctness, licensing, or operational risk are not accepted.
3. Project-owned code must not reimplement a problem already adequately solved by the Java standard
   library, Paper/Spigot/Bukkit APIs, an adopted plugin boundary, or an acceptable external library.
4. SWE.2 and especially SWE.3 must record the official references used to justify designs based on
   Java, Paper, Spigot/Bukkit, or external-library behavior.
5. Codex may only use dependencies and API contracts approved by the controlled SWE.2/SWE.3 design.
   Any dependency addition, substitution, version change, packaging change, or unsupported API
   assumption is `DESIGN_BLOCKED`.

The controlling operational policy is:

`docs/V0.0.2-redesign/00-governance/EXTERNAL_LIBRARY_AND_REFERENCE_POLICY.md`

## Consequences

- SWE.1 will capture maintainability and reuse as software engineering constraints where applicable.
- SWE.2 will allocate capabilities to platform APIs, adopted plugins, external libraries, or
  Wayfarer-owned modules without duplicate ownership.
- SWE.3 will include exact API/library contracts, official links, dependency assessment, and
  integration verification obligations.
- Custom implementation remains possible, but only with an explicit rationale showing why existing
  acceptable solutions do not satisfy the approved requirements.
- Implementation/design consistency review will check actual dependency and API use against the
  approved design.

## Rejected alternatives

### Ban external libraries

Rejected because it would force unnecessary custom implementation and increase correctness,
maintenance, and verification burden.

### Permit any convenient library without maintenance assessment

Rejected because inactive or incompatible dependencies can transfer hidden lifecycle, security, and
runtime risk into every Wayfarer plugin.

### Allow Codex to choose libraries during construction

Rejected because dependency selection changes architecture, licensing, packaging, failure behavior,
and test obligations and therefore belongs to controlled design.
