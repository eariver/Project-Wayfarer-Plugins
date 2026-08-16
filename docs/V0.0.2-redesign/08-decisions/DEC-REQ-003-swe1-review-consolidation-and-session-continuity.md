# SWE.1 Review Consolidation and Session Continuity Policy

Document ID: `DEC-REQ-003`  
Revision: A  
State: `APPROVED`  
Date: 2026-08-06 JST  
Author: ChatGPT  
Approver: Project Owner  
Applicable Product: Plugin V0.0.2 redesign  
Affected process: SWE.1 Software Requirements Analysis

## 1. Purpose

Define how clause-by-clause SWE.1 review decisions are recorded, consolidated into controlled requirement documents, and resumed after a chat-session or context transition.

This decision does not approve G1, SWE.2, SWE.3, implementation, verification execution, merge, tag, deployment, or release.

## 2. Single continuation entry point

The living entry point for resuming this work is:

`docs/V0.0.2-redesign/CONTINUATION.md`

A new session shall begin with that document and follow its ordered references. Conversation history is supplementary only and shall not be treated as requirement or gate authority.

The continuation document shall identify at minimum:

- repository, branch, and draft PR;
- current process, gate, activity, and next review item;
- current authoritative decisions and review log;
- current canonical source and derived SWE.1 documents;
- pending consolidation work;
- prohibited downstream work;
- the ordered resumption procedure.

`CONTINUATION.md` is a living navigation and handoff document. Normative decisions remain in controlled decision, requirement, governance, and review documents referenced from it.

## 3. Authority and navigation order

When resuming work, use the following order:

1. `CONTINUATION.md` for navigation and current resumption instructions;
2. `STATUS.md` for the current process and gate state;
3. the current joint review log for reviewed clauses and next item;
4. Owner-approved decision records that override unintegrated draft text;
5. the current canonical source and derived SWE.1 documents;
6. traceability, verification-intent, issue, and review evidence documents as referenced.

Where an approved decision explicitly overrides conflicting text in an older draft requirement document, the decision controls until consolidation produces a newer integrated revision.

## 4. SWE.1 review checkpoint cadence

During clause-by-clause SWE.1 decomposition review, the Owner will instruct repository reflection at either of these practical checkpoints:

- transition between logical CAN sections; or
- completion of five newly approved CAN clauses since the previous repository checkpoint.

The Owner's explicit instruction controls the actual timing. ChatGPT shall not assume that reaching a numerical or section boundary by itself authorizes a repository write.

Current logical CAN sections are the controlled sections of the canonical source, including Common, Core, Main, Frontier, Worlds Beyond, and Scope. A later canonical revision may refine this list without changing the principle.

## 5. Work between consolidation checkpoints

Between integrated-document checkpoints:

- each reviewed clause receives a disposition in the joint review log;
- approved corrections are recorded in an Owner-approved decision record;
- the decision record explicitly identifies overridden draft text and required downstream propagation;
- partial rewrites of the canonical source and derived SWE.1 documents are avoided unless necessary to prevent ambiguity or unless the Owner directs immediate integration;
- G1 remains unapproved.

This overlay method is temporary. It shall not be used to accumulate an unbounded chain of unresolved decisions.

## 6. Integrated consolidation checkpoint

At a logical-section boundary, after five approved CAN clauses, or at another Owner-directed point, ChatGPT shall determine whether the checkpoint is a decision-only repository reflection or an integrated consolidation.

An integrated consolidation shall update all affected work products as one consistent package:

1. canonical source revision;
2. affected SWE.1 requirement documents;
3. SWE.1 document index;
4. source-to-requirement traceability;
5. verification-intent allocation;
6. open-question and conflict records where affected;
7. requirement counts and item-type totals;
8. joint review log;
9. `STATUS.md`;
10. `CONTINUATION.md`.

The consolidation shall preserve decision records as rationale and shall not delete the historical Owner determination after its content is incorporated.

## 7. Consolidation quality controls

Each integrated consolidation shall verify:

- every approved correction is incorporated or explicitly deferred;
- no older conflicting statement remains authoritative by accident;
- requirement IDs are not silently reused or renumbered;
- added, changed, split, superseded, and removed items have explicit dispositions;
- forward and reverse source traceability remain complete;
- verification intent exists for every normative item;
- document counts match the actual controlled items;
- future topology or capability options approved during review are not accidentally prohibited;
- the current gate and downstream authorization remain correct.

A complete package self-review is mandatory before G1. Partial checkpoint reviews do not replace it.

## 8. Session-resumption protocol

At the start of a new chat session or after context loss:

1. fetch draft PR `#18` and confirm its latest head and branch;
2. open `docs/V0.0.2-redesign/CONTINUATION.md` from that head;
3. follow the ordered references listed there;
4. confirm the next CAN clause and any pending integration checkpoint;
5. do not rely on a remembered commit as the latest state;
6. do not proceed to SWE.2 or implementation unless repository gate documents explicitly authorize it.

If the continuation document, status, review log, and decision records disagree, stop and resolve the repository inconsistency before continuing substantive review.

## 9. Maintenance obligation

Every Owner-directed repository checkpoint during the joint SWE.1 review shall update `CONTINUATION.md` and `STATUS.md` in the same change sequence, so a future session can reconstruct the current work state from one entry document and its references.
