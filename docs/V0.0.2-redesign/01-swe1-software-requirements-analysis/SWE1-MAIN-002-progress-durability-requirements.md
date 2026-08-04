# Main Progress, Evolution, Durability, and Checkpoint Requirements

Document ID: `SWE1-MAIN-002`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A  
Contained normative items: CAP: 16, CON: 5, QLT: 6

## 1. Purpose

Define eligible Growth Tool progress, fixed-point arithmetic, evolution, configuration reconciliation, normal durability, Broken state, and persistence timing.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-MAIN-002-CAP-001 — Exact progress world allowlist

**Normative statement:** Growth Tool progress shall be enabled only in exact worlds `resource`, `resource_nether`, and `resource_end`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-007  
**Rationale:** Restricts progression to approved resource worlds.  
**Precondition / trigger:** A player successfully breaks a block while holding an authorized active Growth Pickaxe.  
**Required observable result:** World eligibility is true only for one of the three exact names.  
**Verification intent:** SWE.4 world-policy verification and SWE.6 representative Main qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-001 — Progress denial outside allowlist

**Normative statement:** Growth Tool progress shall not be granted in Main worlds, similarly named worlds, unknown worlds, or any world outside the exact progress allowlist.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-007  
**Rationale:** Prevents prefix/heuristic world adoption.  
**Precondition / trigger:** An otherwise eligible break occurs outside the exact allowlist.  
**Required observable result:** Progress remains unchanged.  
**Verification intent:** SWE.4 negative world-policy verification and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-002 — Eligible player break progress

**Normative statement:** One successful block break shall add progress exactly once when the block is in `minecraft:mineable/pickaxe`, the current main-hand item is the current owner's authorized active Growth Pickaxe, and the non-cancelled break is valid in Survival or actually succeeds in Adventure.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-008  
**Rationale:** Defines the externally observable progression trigger.  
**Precondition / trigger:** All eligibility conditions are true at completion of one player break.  
**Required observable result:** The computed progress increment is added once and only once.  
**Verification intent:** SWE.4 eligibility/policy verification, SWE.5 Paper event integration, SWE.6 actual mining qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-003 — Eligible block provenance

**Normative statement:** Progress eligibility shall not depend on whether an otherwise eligible block was naturally generated, player placed, generator created, re-placed after Silk Touch collection, or normally created by another plugin.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-008  
**Rationale:** Preserves the approved broad progression model.  
**Precondition / trigger:** An eligible player break occurs on one of the listed provenance classes.  
**Required observable result:** The same block/category weight policy is applied.  
**Verification intent:** SWE.4 provenance-policy verification and SWE.6 representative placed/generated block qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-002 — Non-player and invalid break exclusion

**Normative statement:** Creative, Spectator, cancelled breaks, explosions, pistons, commands, WorldEdit/FAWE removal, plugin-direct removal, and other non-player-break removal shall not grant Growth Tool progress.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-008  
**Rationale:** Prevents unintended or automated progress sources.  
**Precondition / trigger:** A listed excluded removal occurs.  
**Required observable result:** Cumulative progress and evolution state remain unchanged.  
**Verification intent:** SWE.4 exclusion verification and SWE.5 representative event integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-004 — Fixed-point progress

**Normative statement:** Growth Tool progress shall be represented as integer units where `1.000` progress equals `1000` internal units.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-009  
**Rationale:** Avoids floating-point accumulation error and provides deterministic persistence.  
**Precondition / trigger:** Progress is calculated, stored, displayed, or compared to a threshold.  
**Required observable result:** Equivalent values map consistently to integer units without fractional drift.  
**Verification intent:** SWE.4 numeric-policy verification and persistence integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-001 — Saturating progress addition

**Normative statement:** Every positive progress addition shall saturate at `Long.MAX_VALUE`, shall never wrap negative, and shall become a no-op when the stored value is already saturated.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-009; AMD-009  
**Rationale:** Eliminates overflow failure and undefined negative progress.  
**Precondition / trigger:** A progress addition would exceed the maximum representable value or the current value is already maximum.  
**Required observable result:** Stored progress equals `Long.MAX_VALUE`; no exception, negative wrap, or additional change occurs.  
**Verification intent:** SWE.4 boundary-value verification and SWE.5 persistence integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-002 — Saturated-state operability

**Normative statement:** Evolution determination, next-threshold presentation, GUI status, checkpointing, reload, and configuration reconciliation shall remain defined and non-failing when cumulative progress equals `Long.MAX_VALUE`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-009; AMD-009  
**Rationale:** Ensures saturation is a supported state rather than a terminal error.  
**Precondition / trigger:** A saturated logical tool is loaded, displayed, reconciled, or checkpointed.  
**Required observable result:** The operation completes with a deterministic capped/no-next-threshold result and without overflow or unbounded work.  
**Verification intent:** SWE.4 boundary verification, SWE.5 reload/persistence integration, SWE.6 status qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-005 — Configurable block weights

**Normative statement:** The software shall support configurable block/category progress weights with the initial defaults defined in CAN-MAIN-010, including a default weight of `1.00` for otherwise undefined pickaxe-tag blocks.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-010  
**Rationale:** Provides the approved baseline while permitting controlled balance adjustment.  
**Precondition / trigger:** An eligible break is classified by block/category.  
**Required observable result:** The corresponding configured fixed-point base weight is selected deterministically.  
**Verification intent:** SWE.4 weight-mapping verification and SWE.6 representative mining qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-006 — Configurable ore multipliers

**Normative statement:** The software shall support the initial ore-group multipliers defined in CAN-MAIN-010 and shall apply the applicable multiplier to an eligible base weight.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-010  
**Rationale:** Implements the approved ore progression baseline.  
**Precondition / trigger:** An eligible break belongs to a configured ore group.  
**Required observable result:** The progress increment equals the configured base weight multiplied according to the fixed-point policy.  
**Verification intent:** SWE.4 multiplier verification and SWE.6 representative ore qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-007 — Material evolution

**Normative statement:** The Growth Tool shall progress Wood → Stone → Iron → Diamond at cumulative progress thresholds 100, 400, and 1200 respectively.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011  
**Rationale:** Defines the first externally visible growth sequence.  
**Precondition / trigger:** Cumulative progress reaches or crosses a material threshold.  
**Required observable result:** The authorized physical representation and logical evolution state reflect the highest reached material tier.  
**Verification intent:** SWE.4 threshold/state verification, SWE.5 item reconstruction integration, SWE.6 evolution qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-008 — Post-Diamond enchantment schedule

**Normative statement:** After Diamond, enchantment evolution number `n` shall begin at `n=1`, use the initial increment `800 + 200n + 40n²`, and repeat the sequence Efficiency, Unbreaking, Efficiency, Unbreaking, Fortune.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011  
**Rationale:** Defines deterministic post-material progression.  
**Precondition / trigger:** Progress reaches successive post-Diamond evolution thresholds.  
**Required observable result:** Conceptual evolution count and the corresponding enchantment increment follow the specified formula and cycle.  
**Verification intent:** SWE.4 formula/cycle verification and SWE.6 representative cycle qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-003 — Effective enchantment caps with conceptual continuation

**Normative statement:** Effective enchantments shall be capped at Efficiency 10, Unbreaking 10, Fortune 5, and Silk Touch 1, while cumulative progress, conceptual level, and evolution count continue beyond those caps.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011  
**Rationale:** Separates gameplay-effective caps from long-term progression.  
**Precondition / trigger:** An evolution would increase an enchantment above its effective cap.  
**Required observable result:** The effective value remains capped while conceptual progression advances.  
**Verification intent:** SWE.4 cap verification and SWE.6 status/evolution qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-009 — Branch authority

**Normative statement:** The default Growth Tool branch shall be `FORTUNE`; authorized administration shall be able to select `FORTUNE` or `SILK_TOUCH`, and ordinary player-paid branch switching is not required.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011; §9 CAN-SCOPE-001  
**Rationale:** Defines approved branch control and non-scope.  
**Precondition / trigger:** A logical tool is created or an authorized branch change is applied.  
**Required observable result:** The branch is valid, durable, reflected in the item, and preserved across later operations.  
**Verification intent:** SWE.4 branch-state verification, SWE.5 persistence integration, SWE.6 admin qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-003 — Deterministic threshold evaluation

**Normative statement:** For every valid cumulative progress value, including `Long.MAX_VALUE`, the software shall determine material tier, conceptual evolution count, effective enchantments, and next-threshold state deterministically and within bounded runtime and memory.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012  
**Rationale:** Retains the source's correctness/performance intent without prescribing a specific algorithm.  
**Precondition / trigger:** A tool is loaded, progressed, displayed, or reconciled.  
**Required observable result:** The same configuration revision and progress produce the same evolution result without recursion overflow or unbounded expansion.  
**Verification intent:** SWE.4 property/boundary verification and SWE.5 large-value integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-010 — Configuration reconciliation

**Normative statement:** When an approved Growth Tool configuration revision changes, the software shall recompute material, enchantments, and conceptual evolution from unchanged cumulative progress and shall permit both promotion and demotion.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012  
**Rationale:** Ensures configuration changes are applied consistently without rewriting earned progress.  
**Precondition / trigger:** A logical tool is reconciled against a different approved configuration revision.  
**Required observable result:** Progress is unchanged and all derived evolution attributes match the new revision.  
**Verification intent:** SWE.4 reconciliation verification and SWE.5 reload/persistence integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-004 — No repair from configuration reconciliation

**Normative statement:** Configuration reconciliation alone shall not restore durability.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012  
**Rationale:** Prevents balance-changing free repair caused by configuration reload.  
**Precondition / trigger:** An active or broken tool is reconciled without a real progress-triggered evolution.  
**Required observable result:** No durability increase is caused solely by reconciliation.  
**Verification intent:** SWE.4 durability-reconcile verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-011 — Durability preservation across material change

**Normative statement:** For an active tool whose material changes during reconciliation, the software shall preserve the remaining durability ratio and shall retain at least one durability point; a broken tool shall remain broken.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012  
**Rationale:** Preserves player state across material maximum-durability changes.  
**Precondition / trigger:** Configuration reconciliation changes material tier.  
**Required observable result:** Active remaining durability is proportionally mapped with minimum one; broken status and representation remain broken.  
**Verification intent:** SWE.4 ratio/boundary verification and SWE.5 item reconstruction integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-012 — Evolution-triggered full recovery

**Normative statement:** The tool shall restore to maximum durability only when a real eligible progress addition increases conceptual evolution count.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012  
**Rationale:** Defines the approved evolution reward and excludes unrelated recovery.  
**Precondition / trigger:** An eligible progress addition is applied.  
**Required observable result:** Full durability is restored exactly when the before/after evolution count increases; otherwise normal durability semantics remain.  
**Verification intent:** SWE.4 transition verification, SWE.5 Paper damage/break integration, SWE.6 mining qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-013 — Terminal durability interception

**Normative statement:** Before vanilla terminal durability would destroy the managed physical item, the software shall preserve the logical tool and convert the authorized physical representation to the Broken Tool state.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013  
**Rationale:** Prevents permanent logical-item loss through vanilla breakage.  
**Precondition / trigger:** An authorized active tool would take terminal damage.  
**Required observable result:** The item is not destroyed; logical state becomes `BROKEN` and the physical representation becomes the approved broken representation.  
**Verification intent:** SWE.4 durability-boundary verification, SWE.5 Paper damage integration, SWE.6 breakage qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-014 — Broken representation and preserved identity

**Normative statement:** A Broken Tool shall use `GRAY_DYE` and shall preserve tool ID, owner, tool type, epoch, progress, branch, schema, and current authority.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013  
**Rationale:** Maintains continuity between active, broken, repair, and reissue states.  
**Precondition / trigger:** Terminal durability conversion completes or a broken record is reconstructed after restart.  
**Required observable result:** The physical and durable states identify the same logical tool and remain authorized only for the owner.  
**Verification intent:** SWE.4 item/state verification and SWE.5 restart integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-005 — Broken-state restrictions

**Normative statement:** A Broken Tool shall not mine, grant progress, receive ordinary or external repair, or act as an active pickaxe, while owner binding and management-GUI access remain available.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013  
**Rationale:** Separates controlled repair from vanilla/external restoration.  
**Precondition / trigger:** A broken physical item is used in gameplay or a repair/transfer route.  
**Required observable result:** Only approved management and full-repair/reissue-related actions are available; protected restrictions remain enforced.  
**Verification intent:** SWE.4 state-policy verification and SWE.6 representative broken-state qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-004 — Broken-state durability

**Normative statement:** Broken conversion shall be persisted as a critical state promptly and shall survive quit, disable, and restart.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013; CAN-MAIN-017  
**Rationale:** Prevents a restart from resurrecting or losing the logical state.  
**Precondition / trigger:** A tool becomes broken before a lifecycle interruption.  
**Required observable result:** After recovery, the logical and physical reconstruction remains broken with the same authority.  
**Verification intent:** SWE.5 persistence/restart integration and SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-015 — Checkpoint triggers

**Normative statement:** The software shall checkpoint normal progress periodically and shall persist evolution, broken, repair, reissue, authorized administrative changes, quit, and disable according to their criticality.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-017  
**Rationale:** Balances persistence cost with bounded loss and critical-state durability.  
**Precondition / trigger:** A periodic interval or listed state transition occurs.  
**Required observable result:** The applicable durable record advances without synchronous main-thread I/O.  
**Verification intent:** SWE.4 checkpoint-policy verification and SWE.5 persistence/lifecycle integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-009  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-005 — Same-player serialization

**Normative statement:** Operations that can change one player's logical tool, physical authority, progress, repair, delivery, or reissue state shall be serialized sufficiently to prevent lost update or conflicting terminal state.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-017  
**Rationale:** Protects the single logical tool under concurrent events and commands.  
**Precondition / trigger:** Two or more state-changing operations for the same owner overlap.  
**Required observable result:** The resulting durable state corresponds to one valid serial order and no protected effect is duplicated.  
**Verification intent:** SWE.4 concurrency verification and SWE.5 integrated race test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-006 — Bounded normal-progress loss

**Normative statement:** The configured checkpoint policy shall declare and enforce a maximum normal-progress loss window after an ungraceful process failure; critical state transitions shall not rely solely on that periodic window.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-017  
**Rationale:** Makes the accepted crash-loss limitation measurable.  
**Precondition / trigger:** The process fails between periodic checkpoints.  
**Required observable result:** Normal progress loss does not exceed the declared bound, while already committed critical states remain durable.  
**Verification intent:** Analysis, configuration inspection, and SWE.5 crash-window persistence test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-009  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-016 — Normal authorized durability consumption

**Normative statement:** During ordinary authorized use, an active Growth Pickaxe shall preserve the approved Paper/vanilla nonterminal durability result and shall not suppress normal durability loss merely because the item is managed; explicit exceptions are evolution-triggered full recovery, denied or cancelled use, controlled repair/reissue, and terminal conversion to Broken.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-019; derived from CAN-MAIN-012 and CAN-MAIN-013  
**Rationale:** Makes ordinary durability consumption explicit so repair and Broken behavior are reachable and verifiable.  
**Precondition / trigger:** An authorized active Growth Pickaxe incurs ordinary nonterminal durability damage without crossing an evolution threshold.  
**Required observable result:** The post-use damage reflects the platform result; no managed-item guard cancels it unless an explicit exception applies.  
**Verification intent:** SWE.4 durability-policy verification, SWE.5 Paper damage integration, SWE.6 actual mining qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
