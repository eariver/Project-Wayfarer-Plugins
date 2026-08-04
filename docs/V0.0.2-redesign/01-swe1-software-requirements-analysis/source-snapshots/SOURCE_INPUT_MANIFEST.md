# SWE.1 Source Input Manifest

Document ID: `SWE1-SRC-MANIFEST-001`  
Revision: A  
State: `CONTROLLED_INPUT_IDENTITY`  
Date: 2026-08-05 JST  
Prepared by: ChatGPT  
Reviewer: Project Owner

## 1. Purpose

Record the immutable identities of the two Owner-provided input files used to construct
`SWE1-SRC-002`. The normalized active requirement content required to continue the redesign is stored
in the canonical source; this manifest prevents source-file ambiguity.

## 2. Inputs

### SRC-MAINLINE-001

```text
Uploaded filename:
  Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED(1).md

Logical source name:
  Project_Wayfarer_Plugin_V0.0.2_Main_Frontier_Requirements_REGENERATED.md

SHA-256:
  2AD3CFB8AE54CA2149D8EABA44CBBC32470383787C35DB7C458704F87C67167F

Use:
  Base product requirement source
```

### SRC-DELTA-001

```text
Uploaded filename:
  Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register(1).md

Logical source name:
  Project_Wayfarer_Plugin_V0.0.2_Requirement_Implementation_Delta_Register.md

SHA-256:
  A5300317A51BEDA75F5DEAED32A1A795B7DDA553C11C4C895743D4A1D5E924D1

Use:
  Only Owner clarification and contradiction-resolution subset selected by DEC-REQ-001
```

## 3. Derived controlled source

```text
Document:
  SWE1-SRC-002

SHA-256:
  A04C1DBA6FE0D9568C51CE2D2F7FE591F0598C3B92A1EDD4B47AFF779F9A9121
```

`SWE1-SRC-002` is the repository-controlled positive requirement source used for all current SWE.1
requirements. The delta register's implementation status, test status, old roadmap, and release
sequence were not imported.
