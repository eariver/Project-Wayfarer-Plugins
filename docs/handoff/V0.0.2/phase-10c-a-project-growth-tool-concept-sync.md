# Phase 10C-A Project Growth Tool Concept Sync

Status: `PROJECT_SIDE_SYNC_REQUIRED`

This handoff records the Owner decision boundary for Phase 10C-A Candidate-5. It is not Project
Runtime execution, Project acceptance, or a production promotion.

## Required Project-side synchronization

The Project Growth Tool concept must be synchronized with the Candidate-5 Owner contract before
Project acceptance. In particular, confirm that:

- physical transfer, ordinary inventory movement, ordinary-container movement, and Death Drop are
  distinct from use authorization;
- non-owner and stale physical possession is retained but fails closed for use;
- death does not reopen delivery, rotate authority, change delivery status, or debit Waymark;
- the exact Growth Tool and Broken Growth Tool names and the post-commit delivery message are
  accepted by the Project-side UX concept;
- the Main hold-time authorization cache and Candidate-5 invalidation boundaries are compatible
  with the Project-side ownership concept; every Main-Hand-changing handler family must
  invalidate/recompute authorization, while cancelled unchanged inventory operations must not
  permanently fail closed;
- Frontier exact-current cleanup excludes Launchpad, Rocket, ordinary items, lookalikes, and
  incomplete metadata; and
- Resource Pack work remains `SKIPPED_OUT_OF_SCOPE_BY_OWNER` for this candidate.

## Authority references

- Execution authority: `docs/handoff/V0.0.2/phase-10c-a-candidate-5-execution-entry.md`
- Remediation authority: `docs/handoff/V0.0.2/phase-10c-a-candidate-5-remediation-handoff.md`
- Product result: `docs/handoff/V0.0.2/phase-10c-a-candidate-5-product-remediation-result.md`
- Independent review: Candidate-4 review documents remain historical and do not authorize this
  Candidate-5 execution.
- Decision register: `docs/decisions/V0.0.2/decision-register.md`
- Requirement amendment: `docs/requirements/main-server/Project-Wayfarer-V0.1.0/V0.0.2/Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements.md`
- Candidate-3 failure timeline: `.ai-work/luna-gpt-5.6-v003/reports/PHASE_10C_A_INPUT_AND_C3_FAILURE_TIMELINE.md`

The candidate handoff must refer to the immutable Candidate-5 Product Commit SHA and artifact
checksums. Mutable branch URLs and conversation-only decisions are not sufficient evidence.
