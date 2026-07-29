# ADR 0010: Reuse stable Core for the V0.0.2 Main/Frontier release

- Status: Accepted
- Date: 2026-07-30

## Decision

V0.0.2 adds Main and Frontier artifacts while reusing the exact V0.0.1 Core product:

- Core product commit:
  `49e00e21716c1c13a2dbb170fdad1b19c4275612`
- Core stable artifact SHA-256:
  `B045581D3984DDDBA10ED7B2ADA435926B8538BA9B29A1151550CE59588395A2`
- Required Core service/API range for Main and Frontier: `>=0.0.1 <0.1.0`
- Core public API change: none
- Core migration change: none; V001–V003 are immutable

Development builds use `0.0.2-SNAPSHOT`. Release automation exposes only:

- `core`: a V0.0.1-compatible Core correction publication;
- `main-frontier`: Main and Frontier artifacts with the stable Core reused, not repackaged;
- `all`: Core, Main, and Frontier only when a reviewed Core change is intentionally included.

`core-main`, `core-frontier`, and arbitrary combinations are not valid scopes.

Stable correction tags use `V0.0.N[a-z]` and are stable releases, not pre-releases. Ordering is
numeric patch first, then no suffix, `a`, `b`, and so on.

## Rationale

The V0.0.1 public API already supplies tasks, audit, identity, transactions, health, and lifecycle
services needed by the gameplay modules. Rebuilding Core under a new version without a product
change would obscure immutable provenance and unnecessarily widen rollback and acceptance scope.

## Consequences

- The V0.0.2 compatibility matrix must identify two product versions in a normal
  `main-frontier` deployment.
- Main/Frontier packaging must not duplicate API classes or include Core classes.
- A future Core correction uses `core`; a future reviewed Core feature uses `all`.
- Workflow dispatch still requires the explicit approvals in `AGENTS.md`; this ADR does not
  authorize publication.
