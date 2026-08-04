# Domain Document and Linked Identifier Model

Document ID: `V002-GOV-COM-002`  
Revision: A  
State: `APPROVED_OWNER_POLICY`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Applicable Product: Plugin V0.0.2 redesign and later Wayfarer-owned plugin design work

## 1. Purpose

Control the decomposition and identification of SWE.1 through SWE.6 work products so that every
requirement, design element, implementation unit, verification case, and evidence record can be
located and traced without relying on chat history or document titles.

## 2. Domain-first document split

Requirements and design work products must be divided by a single dominant functional or
cross-cutting domain. A document must not become a catch-all specification merely because several
areas are implemented in the same module or class.

Initial domain codes are:

| Code | Domain |
|---|---|
| `COM` | Cross-plugin behavior, shared terminology, repository-wide constraints, and common contracts |
| `CORE` | Wayfarer_Core |
| `MAIN` | Wayfarer_Main and Growth Tool behavior |
| `FRONT` | Wayfarer_Frontier shared behavior |
| `WB` | Worlds Beyond behavior |
| `RF` | Ruined Frontier integration behavior |
| `INT` | External systems, adopted plugins, platform APIs, and inter-module interfaces |
| `DATA` | Persistence, migration, identity, transaction, cache, and audit data contracts |
| `SEC` | Permission, trust, authority, abuse prevention, and security boundaries |
| `QLT` | Cross-cutting quality, timing, reliability, failure, recovery, and observability |
| `OPS` | Deployment, configuration, lifecycle, operations, backup, and release-facing behavior |

A new Wayfarer-owned plugin receives a stable domain code through a tracked `DEC-*` decision before
its first controlled requirement or design document is created.

## 3. Ownership and duplication rules

- Each normative requirement or design element has exactly one owning document.
- Other documents reference the owning item's full identifier; they must not restate it as a second
  independently controlled requirement.
- Behavior spanning multiple domains is owned by a `COM` or `INT` document when no single product
  domain is authoritative.
- Cross-cutting persistence, security, quality, or operational constraints are owned by their
  corresponding cross-cutting domain and allocated to product domains through traceability.
- A document may contain background, rationale, examples, and references, but only explicitly
  identified normative items participate in conformance and coverage accounting.
- Document division follows responsibility and reviewability, not arbitrary file length.

## 4. Controlled document identifier

Every controlled work product uses:

```text
V002-<PROCESS>-<DOMAIN>-<DOCUMENT_NUMBER>
```

Where:

- `V002` identifies Plugin V0.0.2 redesign;
- `<PROCESS>` is `GOV`, `SWE1`, `SWE2`, `SWE3`, `SWE4`, `SWE5`, `SWE6`, `TRC`, `WO`, `REV`, or
  `EVD`;
- `<DOMAIN>` is one approved domain code;
- `<DOCUMENT_NUMBER>` is a three-digit sequence unique inside the process/domain pair.

Examples:

```text
V002-SWE1-MAIN-001
V002-SWE2-CORE-002
V002-SWE3-INT-004
V002-SWE5-WB-001
```

The document identifier must appear in the document header and should prefix the filename:

```text
V002-SWE3-MAIN-004-growth-tool-durability-design.md
```

## 5. Linked item identifier

Every normative item inside a controlled document receives an identifier derived by concatenating the
owning document identifier and a local item identifier:

```text
<DOCUMENT_ID>-<ITEM_TYPE>-<ITEM_NUMBER>
```

The required item types are:

| Type | Meaning |
|---|---|
| `REQ` | SWE.1 software requirement |
| `ARC` | SWE.2 architectural design element or architectural constraint |
| `DD` | SWE.3 detailed-design element or construction constraint |
| `IMP` | Implementation unit recorded by Codex |
| `UV` | SWE.4 unit-verification case |
| `IV` | SWE.5 integration-verification case |
| `QV` | SWE.6 qualification-verification case |
| `REF` | Version-specific official platform/library reference relied upon by design |
| `RISK` | Controlled risk or design assumption requiring disposition |

Examples:

```text
V002-SWE1-MAIN-001-REQ-003
V002-SWE2-MAIN-002-ARC-005
V002-SWE3-MAIN-004-DD-017
V002-SWE3-MAIN-004-REF-006
V002-SWE4-MAIN-001-UV-012
```

This concatenation is mandatory. A local identifier such as `REQ-003` is insufficient outside its
owning document and must not be used in traceability, reviews, code comments, work orders, or test
reports.

## 6. Requirement record

Each SWE.1 requirement record must include at least:

```text
Full requirement ID
Title
Normative statement
Rationale
Source or Owner decision
Applicability and priority
Preconditions or trigger
Required observable result
Failure or denial result, when applicable
Verification intent
Dependencies and assumptions
Open decision or conflict reference
State
```

A requirement must express one independently assessable obligation. When one statement contains
multiple independently failing obligations, it must be split into separate requirement IDs.

## 7. Design record

Each SWE.2 and SWE.3 normative design item must include:

- its full `ARC` or `DD` identifier;
- the upstream requirement identifiers it satisfies;
- the allocated component, interface, state machine, event, data owner, or runtime boundary;
- failure behavior and constraints;
- applicable official-reference identifiers;
- downstream verification obligations.

References to Paper, Spigot/Bukkit, Java, or external-library behavior use document-linked `REF`
identifiers so the exact design assumption is reviewable and traceable.

## 8. Traceability rule

The minimum chain uses full identifiers only:

```text
Source / DEC
  -> ...-REQ-...
  -> ...-ARC-...
  -> ...-DD-...
  -> ...-IMP-...
  -> ...-UV-... / ...-IV-... / ...-QV-...
  -> Evidence and review verdict
```

Every approved upstream item must have one or more downstream allocations or an explicit justified
`NOT_APPLICABLE` disposition. Every downstream item must identify its direct upstream items.

## 9. Document index

Each SWE phase maintains a controlled document index listing:

- document ID;
- title and path;
- domain;
- revision and state;
- predecessor/superseded document;
- owning role and reviewer;
- applicable scope;
- contained normative-item ranges.

The phase index is navigational and does not duplicate the normative content of the owning documents.

## 10. Identifier lifecycle

- Identifiers are not reused.
- After first formal review, an item is not renumbered merely to close a sequence gap.
- A deleted or rejected item retains its identifier and disposition.
- A materially changed approved requirement receives either a new revision with explicit impact
  analysis or a replacement identifier when its obligation changes meaning.
- Superseded items point to their replacement; replacements point back to the superseded items.
- Document moves or filename changes do not change the document identifier.

## 11. Existing draft normalization

The V0.0.2 redesign began before this policy was approved. Existing draft work products may retain
legacy filenames temporarily, but before their applicable gate review they must:

1. receive a compliant document identifier;
2. use linked full identifiers for every normative item;
3. appear in the applicable phase document index;
4. update all traceability references to the full identifiers.

No SWE.1, SWE.2, or SWE.3 baseline may be approved while a normative item uses an unlinked or ambiguous
identifier.
