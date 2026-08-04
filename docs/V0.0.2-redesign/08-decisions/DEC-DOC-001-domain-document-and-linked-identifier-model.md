# DEC-DOC-001 — Domain Document Split and Linked Identifier Model

State: `APPROVED`  
Date: 2026-08-05 JST  
Decision owner: Project Owner  
Prepared by: ChatGPT  
Applicable scope: all V0.0.2 redesign work products and later Wayfarer-owned plugin design work

## Decision

1. Requirements, architecture, detailed design, verification, and related controlled work products
   are divided by functional or cross-cutting domain.
2. Every controlled document receives a stable document identifier.
3. Every normative requirement or design/verification item inside the document receives an individual
   identifier formed by concatenating the owning document identifier and a local item identifier.
4. Full linked identifiers, not local shorthand, are used in traceability, work orders, implementation
   consistency records, verification reports, and reviews.
5. Each normative item has one owning document; other documents reference it instead of duplicating
   it.
6. Existing draft redesign documents are normalized before the applicable lifecycle gate.

## Governing policy

`docs/V0.0.2-redesign/00-governance/DOMAIN_DOCUMENT_AND_IDENTIFIER_MODEL.md`

## Rationale

The repository must be sufficient to continue and audit the work without conversational context.
Domain separation limits monolithic specifications and ownership ambiguity. Linked identifiers make
the owning document and item relationship explicit and preserve end-to-end traceability through
SWE.1 to SWE.6.

## Consequences

- A monolithic V0.0.2 requirements or design specification is prohibited.
- New plugin domains require a tracked domain-code decision.
- Document indexes are required per SWE phase.
- Codex implementation consistency records must cite full `DD` identifiers and create linked `IMP`
  identifiers.
- ChatGPT reviews reject ambiguous, duplicated, unowned, or locally identified normative items.
