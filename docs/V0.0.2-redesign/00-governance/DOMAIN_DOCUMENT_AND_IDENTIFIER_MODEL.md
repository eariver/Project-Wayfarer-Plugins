# Phase-specific Domain, Identifier, and Source Traceability Model

Document ID: `GOV-TRACE-001`  
Revision: B  
State: `APPROVED_OWNER_POLICY`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Owner  
Applicable scope: all current and future Project Wayfarer-owned plugin work products  
Supersedes: Revision A and `DEC-DOC-001`

## 1. Purpose

Control the decomposition and identification of SWE.1 through SWE.6 work products and the mapping
from approved detailed design to source code. The model must make the subject of each process work
product clear without assuming that every SWE process uses the same domain partition.

## 2. Core principles

1. Controlled documents are split into reviewable subject domains.
2. Domain meaning is defined separately for each SWE process.
3. A domain name in one SWE process does not have to exist or mean the same thing in another process.
4. Cross-process relationships are expressed by explicit traceability links, never inferred from
   matching domain names.
5. Document and item identifiers contain no Product-version token.
6. Version applicability is metadata, not identifier content.
7. Every normative item has one owning document and one globally unambiguous full identifier.
8. SWE.3 design-to-code traceability is recorded in production-source Javadoc and, where necessary,
   focused inline comments.

## 3. Controlled document identifier

Every controlled work product uses:

```text
<PROCESS>-<DOMAIN>-<DOCUMENT_NUMBER>
```

Where:

- `<PROCESS>` is `GOV`, `SWE1`, `SWE2`, `SWE3`, `SWE4`, `SWE5`, `SWE6`, `TRC`, `WO`, `REV`, or
  `EVD`;
- `<DOMAIN>` is a domain approved for that process;
- `<DOCUMENT_NUMBER>` is a three-digit sequence unique inside the process/domain pair.

Examples:

```text
SWE1-MAIN-001
SWE2-PERMISSION-001
SWE3-INVENTORY-002
SWE4-DURABILITY-001
SWE5-GAMEPLAY-FLOW-001
SWE6-MAIN-001
```

A Product version such as `V002` or `V0.0.2` must not appear in a document or normative-item ID.
Document headers instead record:

- introduced Product version;
- applicable Product version or version range;
- current revision;
- lifecycle state;
- superseded/replacement identifiers where applicable.

The same approved requirement retains its identifier when carried into a later Product version. A
materially different obligation receives a new item identifier and explicit supersession links.

## 4. Process-specific domain model

### 4.1 SWE.1 — requirement target domains

SWE.1 domains identify the Product, server, theme, or externally visible target to which the
requirement applies. Initial examples are:

```text
COMMON
CORE
MAIN
FRONTIER
WB
RF
ADAPTER
```

`COMMON` owns obligations that genuinely apply across targets. It must not become a substitute for
allocating a requirement to its actual target.

A requirement about Main permission enforcement remains owned by a `MAIN` SWE.1 document. It may map
later to a `PERMISSION` or `AUTHORITY` domain in SWE.2/SWE.3.

### 4.2 SWE.2 — architectural concern domains

SWE.2 domains identify the architectural concern being designed. They are derived after the SWE.1
baseline and may include, for example:

```text
COMMAND
PERMISSION
ITEM
INVENTORY
AUTHORITY
STATE
PERSISTENCE
TRANSACTION
EVENT
LIFECYCLE
INTEGRATION
OBSERVABILITY
```

The final SWE.2 domain dictionary is controlled by the SWE.2 document index. It is not required to
mirror the SWE.1 target domains.

### 4.3 SWE.3 — detailed-design concern domains

SWE.3 domains identify implementation-facing design subjects. They may refine, split, combine, or
rename SWE.2 concerns when this improves construction clarity, while retaining explicit upstream
traceability.

Examples include:

```text
HELD-AUTHORIZATION
GUI-ENTRY
BLOCK-PROGRESS
DURABILITY
DELIVERY
REPAIR
REISSUE
PLAYER-COMMAND
DATABASE-ACCESS
RUNTIME-SHUTDOWN
```

The final SWE.3 domain dictionary is controlled by the SWE.3 document index. Domain changes from
SWE.2 are valid only when the traceability matrix makes the allocation explicit.

### 4.4 SWE.4 — unit-verification subject domains

SWE.4 domains identify the unit behavior or design concern under verification. They generally follow
construction boundaries but may be reorganized for verification clarity. They are not required to
match SWE.3 one-for-one.

### 4.5 SWE.5 — integration subject domains

SWE.5 domains identify the interface, interaction, or integrated flow under test, such as:

```text
GAMEPLAY-FLOW
PERSISTENCE-FLOW
WAYMARK-TRANSACTION
PLUGIN-LIFECYCLE
MAIN-CORE-INTEGRATION
FRONTIER-MVI-INTEGRATION
```

### 4.6 SWE.6 — qualification target domains

SWE.6 returns to externally observable Product targets and therefore normally uses target-oriented
domains comparable to SWE.1, such as `MAIN`, `FRONTIER`, `WB`, or `RF`. Exact equality with the SWE.1
domain set is not required; qualification scope controls the final dictionary.

## 5. Linked item identifier

Every normative item inside a controlled document receives:

```text
<DOCUMENT_ID>-<ITEM_TYPE>-<ITEM_NUMBER>
```

The item type describes the semantic role of the item inside its process. It must not merely repeat
the process name.

### 5.1 SWE.1 item types

| Type | Meaning |
|---|---|
| `CAP` | Required capability or externally observable behavior |
| `CON` | Required constraint or prohibition |
| `IFC` | External or inter-product interface obligation |
| `QLT` | Quality, reliability, performance, security, recovery, or operability obligation |

### 5.2 SWE.2 item types

| Type | Meaning |
|---|---|
| `CMP` | Component or responsibility allocation |
| `IFC` | Architectural interface contract |
| `FLOW` | Control, data, or interaction flow |
| `STM` | Architecture-level state model or transition allocation |
| `DAT` | Data ownership or architectural data model |
| `DEP` | Platform, library, runtime, or deployment dependency allocation |
| `CON` | Architectural invariant or constraint |

### 5.3 SWE.3 item types

| Type | Meaning |
|---|---|
| `DD` | Normative detailed-design or construction obligation |
| `REF` | Authoritative Java, Paper/Spigot/Bukkit, plugin, library, or standards reference |
| `IMP` | Implementation unit recorded during construction and consistency reporting |

Each `DD` item also carries a `Design kind` attribute, selected from a controlled vocabulary such as
`BEHAVIOR`, `STATE_TRANSITION`, `API_CONTRACT`, `DATA`, `THREADING`, `ERROR_HANDLING`, `ALGORITHM`,
`CONFIGURATION`, or `OBSERVABILITY`.

`DD` and `IMP` are intentionally retained because they distinguish approved design from the source
unit that realizes it within the SWE.3 process.

### 5.4 SWE.4 through SWE.6 item types

| Type | Meaning |
|---|---|
| `CASE` | Executable verification or qualification case |
| `PROC` | Controlled setup, execution, or teardown procedure |
| `ENV` | Environment or configuration definition |
| `DATA` | Test input, fixture, or dataset definition |
| `ORCL` | Pass/fail oracle or expected-result rule |
| `EVID` | Evidence or result record linked to an execution identity |
| `REF` | Authoritative external reference used by the verification design |

The former generic types `REQ`, `ARC`, `UV`, `IV`, and `QV` are not used. Their process meaning is
already expressed by the owning document ID and they add no semantic information.

### 5.5 Cross-process supporting item types

`RISK` and `ISSUE` may be used in any process when a controlled risk, assumption, conflict, or open
question requires an individual disposition. They are not substitutes for normative requirements or
design items.

Examples:

```text
SWE1-MAIN-001-CAP-003
SWE1-MAIN-001-QLT-004
SWE2-AUTHORITY-001-STM-002
SWE3-HELD-AUTHORIZATION-001-DD-007
SWE3-HELD-AUTHORIZATION-001-REF-003
SWE3-HELD-AUTHORIZATION-001-IMP-002
SWE4-HELD-AUTHORIZATION-001-CASE-004
SWE5-GAMEPLAY-FLOW-001-CASE-006
SWE6-MAIN-001-CASE-011
```

A local identifier such as `DD-007` is insufficient outside its owning document.

## 6. Cross-process traceability

The target-to-concern transition is explicit:

```text
SWE1-MAIN-001-CAP-003
  -> SWE2-AUTHORITY-001-STM-002
  -> SWE3-HELD-AUTHORIZATION-001-DD-007
  -> SWE3-HELD-AUTHORIZATION-001-IMP-002
  -> SWE4-HELD-AUTHORIZATION-001-CASE-004
  -> SWE5-GAMEPLAY-FLOW-001-CASE-006
  -> SWE6-MAIN-001-CASE-011
```

This chain demonstrates that SWE.1 and SWE.6 can be target-oriented while SWE.2 through SWE.5 are
concern-oriented. Domain-name equality is never used as evidence of coverage.

Every approved upstream item must have one or more downstream allocations or an explicit justified
`NOT_APPLICABLE` disposition. Every downstream item identifies its direct upstream items.

## 7. Source-code traceability to SWE.3

### 7.1 Javadoc requirement

Every production class and every production method that realizes normative SWE.3 behavior must list
its applicable full SWE.3 `DD` identifiers in Javadoc.

Use a standard Javadoc paragraph rather than an undocumented custom tag:

```java
/**
 * Performs the authorized Growth Tool damage transition.
 *
 * <p><strong>SWE.3 traceability:</strong>
 * {@code SWE3-DURABILITY-001-DD-004},
 * {@code SWE3-DURABILITY-001-DD-006}
 */
```

Class-level Javadoc identifies the design scope owned by the class. Method-level Javadoc identifies
the design obligations realized by that method. Constructors, generated code, trivial accessors, and
mechanical data holders may use a class-level mapping only when the applicable SWE.3 design explicitly
permits that treatment.

### 7.2 Inline-comment rule

Javadoc alone is sufficient when the complete class or method unambiguously realizes one design item
or a small set of items from one detailed-design document.

When one method contains materially distinct code regions that realize SWE.3 items from multiple
documents, the method Javadoc lists every applicable full ID and each region is preceded by a focused
comment identifying the item realized by that region:

```java
// SWE.3 traceability: SWE3-AUTHORITY-001-DD-009

// SWE.3 traceability: SWE3-DURABILITY-001-DD-004
```

Inline trace comments must describe mapping only. They must not duplicate design prose or explain
ordinary Java syntax.

### 7.3 Extracted consistency evidence

Construction must include a deterministic source scan that extracts:

- production source path;
- class and method signature;
- source line range;
- Javadoc SWE.3 IDs;
- inline SWE.3 IDs;
- corresponding `IMP` identifier;
- unmatched, unknown, duplicated, or missing IDs.

The generated implementation-to-detailed-design consistency report is committed as controlled,
sanitary evidence. The scanner must fail the prescribed conformance check when:

- a cited SWE.3 ID does not exist in the approved baseline;
- an implementation unit lacks required Javadoc mapping;
- an inline mapping cites an ID absent from the enclosing method Javadoc;
- an approved construction-scope `DD` item has no implementation allocation without an approved
  `NOT_APPLICABLE` disposition.

Codex must not invent or alter a SWE.3 ID while implementing. A missing or unsuitable mapping is
`DESIGN_BLOCKED`.

## 8. Ownership and duplication

- Each normative item has exactly one owning document.
- Other documents reference the owning item's full identifier instead of duplicating the obligation.
- A document may include rationale, diagrams, examples, and references, but only identified normative
  items participate in conformance and coverage accounting.
- Document division follows the subject appropriate to that SWE process, not module layout, source
  file layout, or arbitrary document length.

## 9. Phase document indexes

Each SWE phase maintains its own controlled document index containing:

- document ID;
- domain definition and rationale;
- title and path;
- revision and state;
- applicable Product versions;
- predecessor/superseded documents;
- contained item ranges;
- upstream and downstream traceability status.

A new process domain is approved through that phase's document index or a tracked decision before the
first normative document in the domain is created.

## 10. Identifier lifecycle

- Identifiers are never reused.
- Identifiers do not change solely because the Product version changes.
- Version applicability is updated in metadata and traceability records.
- After first formal review, an item is not renumbered merely to close a sequence gap.
- Rejected and superseded items retain their identifier and disposition.
- A materially changed obligation receives a replacement identifier when its semantic contract is no
  longer the same obligation.
- File moves and filename changes do not change the controlled document ID.

## 11. Existing draft normalization

Before G1 review:

1. remove `V002` from all controlled document and item IDs;
2. replace the earlier global-domain model with process-specific domains;
3. replace `REQ` with the applicable SWE.1 semantic item type;
4. rename the SWE.1 phase index and revise its reservations;
5. update all traceability and status references;
6. mark `DEC-DOC-001` superseded by `DEC-DOC-002`.

No SWE.1, SWE.2, or SWE.3 baseline may be approved while a normative item uses a version-prefixed,
process-redundant, or ambiguous identifier.