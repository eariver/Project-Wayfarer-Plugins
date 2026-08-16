# DEC-DOC-002 — Process-specific Domain and Source Traceability Model

State: `APPROVED`  
Date: 2026-08-05 JST  
Decision owner: Project Owner  
Prepared by: ChatGPT  
Applicable scope: all current and future Project Wayfarer-owned plugin work products  
Supersedes: `DEC-DOC-001`

## Decision

1. Controlled document and normative-item IDs contain no Product-version token.
2. Product-version applicability is recorded as document/item metadata.
3. Each SWE process defines domains suited to that process rather than inheriting one global domain
   partition.
4. SWE.1 and SWE.6 are normally target-oriented by server, theme, plugin, or externally visible
   Product scope.
5. SWE.2 through SWE.5 are normally concern-oriented by the architecture, implementation, unit, or
   integration subject under design or verification.
6. Cross-process coverage is proved by explicit traceability, not matching domain names.
7. Item types describe semantic roles within a process. The process-redundant types `REQ`, `ARC`,
   `UV`, `IV`, and `QV` are retired.
8. `DD` and `IMP` remain valid in SWE.3 because they distinguish approved detailed design from the
   implementation unit that realizes it. `REF` remains valid wherever an authoritative external
   reference is required.
9. Every production class and every production method realizing normative SWE.3 behavior lists its
   applicable full SWE.3 `DD` IDs in Javadoc.
10. Focused inline trace comments are required only when one method contains distinct regions mapped
    to SWE.3 items from multiple detailed-design documents.
11. A deterministic source scan extracts code/Javadoc/comment mappings into the controlled
    implementation-to-detailed-design consistency report and rejects invalid or missing mappings.

## Governing policy

`docs/V0.0.2-redesign/00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md`

Document ID: `GOV-TRACE-001`, Revision B.

## Rationale

A version token would make requirement identities unstable across releases. A single global domain
partition would also obscure the different questions answered by the V-model stages:

- SWE.1 asks which Product target requires behavior;
- SWE.2 and SWE.3 ask which architectural or implementation concern realizes it;
- SWE.4 and SWE.5 ask which unit or integration flow is being verified;
- SWE.6 returns to qualification of the externally visible Product target.

Source-level traceability makes the SWE.3 construction relationship inspectable and mechanically
extractable instead of relying only on a manually written consistency table.

## Consequences

- Existing `V002-*` draft IDs are normalized before G1.
- Each phase owns a separate domain dictionary through its document index.
- Traceability matrices explicitly map target-oriented requirements to concern-oriented design and
  verification items.
- Codex must include the approved Javadoc/inline trace markers in production source.
- Missing or ambiguous source mapping is `DESIGN_BLOCKED` rather than an implementation choice.
- ChatGPT reviews the generated consistency report against both source and the approved SWE.3
  baseline.