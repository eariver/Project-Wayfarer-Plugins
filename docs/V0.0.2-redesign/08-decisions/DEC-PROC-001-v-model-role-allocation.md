# DEC-PROC-001 — V-model and Role Allocation

Revision: A  
State: `APPROVED_BASELINE`  
Decision date: 2026-08-05 JST  
Decision authority: Project Owner  
Recorded by: ChatGPT  
Applicable Product: Plugin V0.0.2 redesign

## Decision

1. Plugin V0.0.1 is treated as complete for the initial V0.0.2 requirements analysis.
2. V0.0.1 may be reconsidered only when V0.0.2 analysis identifies a blocking baseline dependency
   issue; such reconsideration requires a separate explicit decision.
3. Plugin V0.0.2 development follows a general V-model aligned to SWE.1 through SWE.6.
4. ChatGPT owns:
   - SWE.1 Software Requirements Analysis;
   - SWE.2 Software Architectural Design;
   - the software detailed-design work products of SWE.3.
5. Codex owns unit construction from the approved SWE.3 detailed design and must provide a tracked
   implementation-to-detailed-design consistency record.
6. ChatGPT independently reviews the implementation and consistency record before the construction
   gate passes.
7. Codex executes SWE.4 Software Unit Verification, SWE.5 Software Integration and Integration Test,
   and SWE.6 Software Qualification Test from approved specifications and work orders.
8. ChatGPT independently reviews SWE.4 through SWE.6 work products, execution evidence, traceability,
   and results.
9. The Owner retains product decisions, process-gate approval where specified, final acceptance, and
   release authorization.
10. Requirements, designs, decisions, work orders, consistency records, reviews, and evidence summaries
    required to continue the work must be committed to the GitHub repository. Chat history alone is
    not sufficient authority.

## Rationale

The prior V0.0.2 implementation demonstrated that delegating product interpretation and local design
choices to the implementation agent can produce extensive code and tests without assuring the basic
Player-facing vertical slice. The redesigned process therefore separates requirements and design
authority from construction and execution, and requires explicit traceability and independent review
at every V-model transition.

## Consequences

- Candidate-9 remediation is not started as an isolated patch activity.
- PR #14 and Candidate-8 are retained as reference and defect evidence, not as design authority.
- No V0.0.2 production implementation is authorized before the SWE.1, SWE.2, and SWE.3 design gates
  applicable to that implementation scope pass.
- Codex reports `DESIGN_BLOCKED` rather than inventing behavior when the approved detailed design is
  incomplete or contradictory.
- Passing tests do not establish design conformance unless the implementation/design consistency
  record and ChatGPT review also pass.
- This process tailoring does not itself constitute a formal Automotive SPICE assessment or
  certification claim.
