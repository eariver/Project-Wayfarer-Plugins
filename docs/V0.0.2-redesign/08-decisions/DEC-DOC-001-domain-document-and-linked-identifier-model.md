# DEC-DOC-001 — Initial Domain Document Split and Linked Identifier Model

State: `SUPERSEDED`  
Date: 2026-08-05 JST  
Decision owner: Project Owner  
Prepared by: ChatGPT  
Applicable scope: historical V0.0.2 redesign governance only  
Superseded by: `DEC-DOC-002`

## Historical decision

This decision originally established:

1. domain-separated requirements, design, and verification documents;
2. a stable document identifier;
3. document-linked normative-item identifiers;
4. one owning document for each normative item;
5. full identifiers in traceability and implementation consistency records.

## Supersession reason

The original model is no longer authoritative because it:

- embedded `V002` in document and item identifiers;
- required one global domain partition across SWE.1 through SWE.6;
- used process-redundant item types such as `REQ`, `ARC`, `UV`, `IV`, and `QV`;
- did not define Javadoc and inline source-code traceability to SWE.3 detailed design.

These points were corrected by the Owner-approved process-specific model in `DEC-DOC-002` and
`GOV-TRACE-001` Revision B.

## Retained principles

The following principles remain valid through the replacement decision:

- documents remain subject-separated and reviewable;
- every controlled document and normative item has a stable full identifier;
- each normative item has one owner;
- traceability uses complete identifiers;
- existing draft documents must be normalized before their applicable gate.

This document is retained for decision history and must not be used as current identifier authority.