# Main Progress, Evolution, Durability, and Checkpoint Requirements

Document ID: `SWE1-MAIN-002`  
Revision: B  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-15 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision E  
Controlling review decision: `DEC-REQ-007` for `CAN-MAIN-007` through `CAN-MAIN-010`  
Contained active normative items: CAP: 15, CON: 5, QLT: 6; historical `CAP-006` superseded

## 1. Purpose

Define eligible Growth Tool progress, deterministic numeric safety, uniform progress accumulation, evolution, configuration reconciliation, normal durability, Broken state, and persistence timing. `CAN-MAIN-007` through `CAN-MAIN-010` are jointly reviewed and integrated under `DEC-REQ-007`; later Main clauses remain subject to their own clause review.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.
- Progress numeric semantics do not prescribe Java `long`, `Long.MAX_VALUE`, fixed-point scale, or another implementation encoding.
- Superseded identifiers remain historical and are not reused or renumbered.

## 3. Requirements

### SWE1-MAIN-002-CAP-001 — Configured exact progress-world allowlist

**Normative statement:** Growth Tool progress shall be eligible only when the current platform world identity exactly matches an entry in the approved progress-world configuration. The initial V0.0.2 supplied/default allowlist shall contain `resource`, `resource_nether`, and `resource_end`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-007; DEC-REQ-007 §3  
**Rationale:** Preserves an exact resource-world boundary without permanently fixing later deployment world names in SWE.1.  
**Precondition / trigger:** A qualifying player mining action is evaluated for progress.  
**Required observable result:** World eligibility is true only for exact membership in the current approved allowlist.  
**Verification intent:** SWE.4 world-policy/configuration verification and SWE.6 representative Main qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-001 — No progress outside configured allowlist

**Normative statement:** Growth Tool progress shall not be granted in a world outside the approved exact progress-world allowlist. The software shall not implicitly adopt another world through name similarity, prefix/suffix/substring matching, dimension/environment alone, historical naming convention, or another unapproved heuristic.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-007; DEC-REQ-007 §3  
**Rationale:** Prevents heuristic substitution or accidental expansion of the approved progress boundary.  
**Precondition / trigger:** An otherwise qualifying mining action occurs outside the exact allowlist or one configured world is unavailable.  
**Required observable result:** No progress is granted from the non-member world and no replacement world is inferred; independently valid configured worlds remain eligible.  
**Verification intent:** SWE.4 negative world-policy verification and SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-QLT-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-002 — Qualifying player-mined progress

**Normative statement:** One completed qualifying player-caused block break shall add the applicable Growth Tool progress increment exactly once when the current world is eligible, the broken block is classified by the adopted Minecraft block-tag contract as `minecraft:mineable/pickaxe`, the physical tool used for the mining action is the player's current authorized `ACTIVE` Growth Pickaxe, and the applicable Survival/Adventure completion rule is satisfied.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-008; DEC-REQ-007 §4  
**Rationale:** Defines the externally observable progression trigger while reusing the existing current-authority requirement instead of duplicating owner/tool/epoch fields.  
**Precondition / trigger:** A player mining action completes a block break satisfying all eligibility conditions.  
**Required observable result:** The configured uniform progress increment is added once and only once for that completed physical block break.  
**Verification intent:** SWE.4 eligibility/exactly-once policy verification, SWE.5 Paper event integration, and SWE.6 actual mining qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-001; SWE1-MAIN-001-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-003 — Block-provenance-neutral eligibility

**Normative statement:** An otherwise qualifying block shall remain eligible regardless of whether it was naturally generated, player placed, generator/plugin created, or collected and re-placed, including through Silk Touch. Wayfarer shall not require block-placement/provenance history solely to determine Growth Tool progress eligibility, and repeated eligible mining is not prohibited by this requirement.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-008; DEC-REQ-007 §4  
**Rationale:** Preserves player choice among ordinary mining, generated blocks, and repeated mining without introducing block-history authority.  
**Precondition / trigger:** A qualifying player break occurs on a block with any supported placement/generation provenance.  
**Required observable result:** Provenance alone neither grants nor denies eligibility; the same uniform increment policy applies.  
**Verification intent:** SWE.4 provenance-policy verification and SWE.6 representative placed/generated/re-placed block qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-002  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-002 — Nonqualifying block-removal exclusion

**Normative statement:** Creative or Spectator activity, a denied/cancelled mining attempt that does not complete a qualifying player break, and block removal not caused by a qualifying player mining action shall not grant Growth Tool progress.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-008; DEC-REQ-007 §4  
**Rationale:** Prevents progress from invalid/automated/non-player removal without creating Product dependencies on particular editor or plugin implementations.  
**Precondition / trigger:** A nonqualifying mining/removal action occurs.  
**Required observable result:** Cumulative progress and evolution state remain unchanged by that removal.  
**Verification intent:** SWE.4 exclusion verification and SWE.5 representative cancelled/explosion/piston/command/editor/plugin-removal integration cases.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-002  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-004 — Representation-independent cumulative progress semantics

**Normative statement:** Growth Tool cumulative progress shall preserve one approved logical non-negative progress quantity deterministically across accumulation, durable persistence/reload, threshold evaluation, presentation, and applicable configuration reconciliation without depending on one prescribed physical numeric encoding or scale.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-009; DEC-REQ-007 §5  
**Rationale:** Preserves stable Product semantics while leaving exact numeric representation to downstream design.  
**Precondition / trigger:** Progress is accumulated, stored, loaded, displayed, compared, or reconciled.  
**Required observable result:** Equivalent logical values produce consistent state/results without representation-dependent drift or divergence.  
**Verification intent:** SWE.4 numeric-semantic/property verification and SWE.5 persistence/reload integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-001 — Overflow-safe monotonic progress accumulation

**Normative statement:** An accepted positive progress addition shall not decrease cumulative progress or cause numeric wraparound, negative overflow, corruption, or undefined behavior. If the selected representation is bounded, reaching or exceeding its supported maximum shall resolve to a defined safe maximum/bounded state without unsafe arithmetic behavior.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-009; DEC-REQ-007 §5; AMD-009 superseded by DEC-REQ-007  
**Rationale:** Retains the safety intent of the former `Long.MAX_VALUE` rule without fixing Java `long` as the Product representation.  
**Precondition / trigger:** A positive addition approaches/exceeds the selected representation boundary or progress is already at its supported maximum.  
**Required observable result:** Progress never becomes negative/corrupt and remains in a defined supported state.  
**Verification intent:** SWE.4 property/boundary verification and SWE.5 persistence integration.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-002 — Maximum-progress-state operability

**Normative statement:** At any supported maximum cumulative progress state, applicable threshold/evolution determination, status/next-threshold presentation, durable persistence/reload, and configuration reconciliation shall remain defined and non-failing.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-009; DEC-REQ-007 §5; AMD-009 superseded by DEC-REQ-007  
**Rationale:** Ensures numeric boundary handling remains a supported state rather than an implementation-specific terminal error.  
**Precondition / trigger:** A logical tool is at the selected representation's supported maximum and is loaded, displayed, progressed, or reconciled.  
**Required observable result:** Applicable operations complete deterministically without overflow, corruption, or unbounded work.  
**Verification intent:** SWE.4 boundary verification, SWE.5 reload/persistence integration, and SWE.6 status qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-QLT-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-005 — Uniform configurable progress increment

**Normative statement:** Each qualifying block break shall add the same configured positive logical progress increment regardless of block material, category, ore classification, rarity, provenance, or generation source. The initial V0.0.2 supplied/default increment shall be `1.00` logical progress per qualifying break.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-010; DEC-REQ-007 §6  
**Rationale:** Makes eligible mining count, rather than block rarity, the progression basis and preserves player choice among mining strategies.  
**Precondition / trigger:** A break qualifies under CAN-MAIN-008.  
**Required observable result:** One uniform configured increment is applied; no material/ore/rarity multiplier changes the increment.  
**Verification intent:** SWE.4 configuration/uniformity verification and SWE.6 representative stone/ore/generated/re-placed mining qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-002; SWE1-MAIN-002-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

## 4. Historical superseded identifier

### SWE1-MAIN-002-CAP-006 — Configurable ore multipliers

**Disposition:** `SUPERSEDED_BY_OWNER_CORRECTION`  
**Former source:** CAN-MAIN-010  
**Replacement coverage:** Material/ore-dependent progression weighting was withdrawn by `DEC-REQ-007` §6. Uniform progress is controlled by `SWE1-MAIN-002-CAP-005`.  
**Identifier reuse:** Prohibited.

## 5. Later Main requirements still awaiting owning-clause review

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

**Normative statement:** For every valid cumulative progress value, including any supported maximum state, the software shall determine material tier, conceptual evolution count, effective enchantments, and next-threshold state deterministically and within bounded runtime and memory.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; numeric-boundary terminology constrained by CAN-MAIN-009 / DEC-REQ-007 §5  
**Rationale:** Retains the source's correctness/performance intent without prescribing a specific algorithm or numeric maximum.  
**Precondition / trigger:** A tool is loaded, progressed, displayed, or reconciled.  
**Required observable result:** The same configuration revision and progress produce the same evolution result without recursion overflow or unbounded expansion.  
**Verification intent:** SWE.4 property/boundary verification and SWE.5 large-value integration test.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-QLT-002  
**Assumptions:** None  
**Open issue / conflict:** CAN-MAIN-012 remains unreviewed; exact threshold/reconciliation semantics will be reassessed in its owning review.  
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
**Open issue / conflict:** Current durability is Minecraft physical authority under reviewed CAN-MAIN-002; this requirement must be interpreted/reviewed accordingly in CAN-MAIN-012.  
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
**Open issue / conflict:** Current durability is Minecraft physical authority; owning CAN-MAIN-012 review remains required.  
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
**Open issue / conflict:** Ordinary possession/drop/storage remains allowed under reviewed CAN-MAIN-005/006; this restriction concerns active use/repair only.  
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
**Open issue / conflict:** Current durability remains Minecraft physical authority under reviewed CAN-MAIN-002.  
**State:** `DRAFT`
