# DEC-REQ-001 — Canonical Source Merge and SWE.1 Analysis Method

State: `APPROVED`  
Date: 2026-08-05 JST  
Decision owner: Project Owner  
Prepared by: ChatGPT  
Applicable scope: Plugin V0.0.2 redesign SWE.1 analysis

## Decision

1. The Owner-provided mainline Main/Frontier requirement document is the base source for the current
   requirements analysis.
2. The prior requirement/implementation delta register is not adopted wholesale.
3. Only later Owner decisions that clarify a base requirement or resolve a contradiction are applied
   from the delta register.
4. Prior implementation state, test state, code work lists, candidate/release roadmap, Project
   roadmap, and future-version scheduling from the delta register are ignored.
5. A single canonical positive-requirement source is created before SWE.1 decomposition.
6. That canonical source is decomposed into target-oriented SWE.1 requirement documents.
7. Every unresolved ambiguity, conflict, feasibility dependency, or missing measurable bound is
   recorded explicitly in the SWE.1 issue register.
8. ChatGPT performs a self-review of the complete SWE.1 package before joint Owner/ChatGPT review.
9. SWE.2 and implementation remain unauthorized until the G1 gate is separately approved.

## Controlled input identities

```text
Mainline source SHA-256:
  2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F

Delta register SHA-256:
  A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1
```

## Canonical output

```text
Document:
  SWE1-SRC-002

Path:
  docs/V0.0.2-redesign/01-swe1-software-requirements-analysis/
  SWE1-SRC-002-canonical-mainline-requirements.md
```

## Rationale

The prior delta register mixed several different kinds of information:

- actual Owner requirement amendments;
- implementation interpretation;
- current implementation and test state;
- prior remediation work;
- candidate/release sequencing;
- future roadmap.

Using it as one undifferentiated authority would reintroduce implementation-driven requirements and
the abandoned roadmap. The controlled merge preserves explicit Owner product decisions while
restarting the V-model from an auditable source baseline.

## Consequences

- `SWE1-SRC-002` is the direct provenance source for the current decomposed requirements.
- The original input identities are retained by filename and SHA-256 in `SOURCE_INPUT_MANIFEST.md`; the complete normalized active requirement content is retained in `SWE1-SRC-002`.
- AMD-001 through AMD-011 are applied only according to the disposition in `SWE1-SRC-002`.
- AMD-012 is excluded from software behavior.
- PR #14 remains reference evidence only.
- All generated target requirements remain draft until Owner review and G1 approval.
