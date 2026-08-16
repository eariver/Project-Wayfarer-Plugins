# Main Progress, Evolution, Durability, and Checkpoint Requirements

Document ID: `SWE1-MAIN-002`  
Revision: C  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-16 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `MAIN`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision F  
Controlling review decisions: `DEC-REQ-007` for `CAN-MAIN-007` through `CAN-MAIN-010`; `DEC-REQ-008` for `CAN-MAIN-011` through `CAN-MAIN-013`  
Contained active normative items: CAP: 16, CON: 5, QLT: 7; historical `CAP-006` superseded

## 1. Purpose

Define eligible Growth Tool progress, deterministic numeric safety, uniform progress accumulation, configured evolution, configuration reconciliation, normal durability, Broken-state continuity, and persistence timing. `CAN-MAIN-007` through `CAN-MAIN-013` are jointly reviewed and integrated under `DEC-REQ-007` / `DEC-REQ-008`; later Main clauses remain subject to their own owning-clause review.

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

### SWE1-MAIN-002-CAP-007 — Configured base-material evolution

**Normative statement:** A Growth Pickaxe shall begin at Wood and progress through the V0.0.2 base-material sequence Wood → Stone → Iron → Diamond as cumulative progress reaches the approved material thresholds. The initial V0.0.2 supplied/default cumulative thresholds shall be Stone `100`, Iron `400`, and Diamond `1200`. Progress-based automatic material evolution shall not extend beyond Diamond unless a later approved requirement adds another tier.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011; DEC-REQ-008 §2  
**Rationale:** Preserves the Product-visible material progression while leaving playtest balance thresholds configurable.  
**Precondition / trigger:** Current cumulative progress is evaluated against the approved material thresholds.  
**Required observable result:** The derived material tier is the highest applicable tier in the approved sequence for the current configuration/progress.  
**Verification intent:** SWE.4 threshold/configuration verification, SWE.5 item reconstruction integration, and SWE.6 representative evolution qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-008 — Configured post-Diamond evolution thresholds

**Normative statement:** After Diamond, each post-Diamond enchantment evolution number `n`, beginning at `n=1`, shall add a configured positive progress increment to the preceding evolution threshold to form the next cumulative threshold. The initial V0.0.2 supplied/default increment definition shall be `800 + 200n + 40n²`.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011; DEC-REQ-008 §2  
**Rationale:** Removes ambiguity between an absolute threshold and an increment while retaining the initial growth curve as configurable balance.  
**Precondition / trigger:** The tool has reached Diamond and a post-Diamond evolution threshold is evaluated.  
**Required observable result:** Successive cumulative thresholds are derived by adding the configured increment for each `n` to the preceding threshold.  
**Verification intent:** SWE.4 formula/threshold/configuration verification and SWE.6 representative post-Diamond qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-003 — Configured effective enchantment caps with conceptual continuation

**Normative statement:** Effective enchantment application shall be limited by the approved cap configuration while cumulative progress, conceptual evolution count, and conceptual enchantment progression continue beyond effective caps. The initial V0.0.2 supplied/default caps shall be Efficiency 10, Unbreaking 10, Fortune 5, and Silk Touch 1.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011; DEC-REQ-008 §2  
**Rationale:** Separates long-term conceptual progression from currently effective gameplay values and permits later approved balance adjustment without erasing earned progression.  
**Precondition / trigger:** A conceptual evolution would place an enchantment at or above its current effective cap.  
**Required observable result:** Effective application does not exceed the configured cap while conceptual progression continues.  
**Verification intent:** SWE.4 cap/configuration verification and SWE.6 status/evolution qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-009 — Branch-dependent effective enchantment projection

**Normative statement:** The default Growth Tool branch shall be `FORTUNE`. In `FORTUNE`, the current capped conceptual Fortune value shall be applied and Silk Touch shall not be applied. In `SILK_TOUCH`, Silk Touch I shall be applied and Fortune shall not be applied to the physical item while conceptual Fortune progression continues. Returning to `FORTUNE` shall reapply the then-current conceptual Fortune value subject to the effective cap. V0.0.2 shall permit authorized administration to select either branch; ordinary player-paid branch switching is not required.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011; §9 CAN-SCOPE-001; DEC-REQ-008 §2  
**Rationale:** Defines observable branch semantics without discarding conceptual Fortune progression while Silk Touch is active.  
**Precondition / trigger:** A logical tool is created, evaluated, or undergoes an authorized branch change.  
**Required observable result:** Effective enchantment projection matches the authoritative branch while conceptual Fortune progression remains continuous.  
**Verification intent:** SWE.4 branch/effective-state verification, SWE.5 persistence integration, and SWE.6 admin/branch qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CAP-011; SWE1-MAIN-002-CON-003  
**Assumptions:** None  
**Open issue / conflict:** Exact administrative command/permission allocation remains for CAN-MAIN-018 review.  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-017 — Configured post-Diamond enchantment mapping

**Normative statement:** Post-Diamond conceptual enchantment evolution shall follow the approved V0.0.2 evolution mapping. The initial V0.0.2 supplied/default repeating mapping shall be Efficiency → Unbreaking → Efficiency → Unbreaking → Fortune. The V0.0.2 conceptual progression family shall not add other enchantment kinds unless a later approved requirement extends it.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-011; DEC-REQ-008 §2  
**Rationale:** Keeps the enchantment mapping atomic and configurable while preventing configuration alone from silently expanding Product scope.  
**Precondition / trigger:** A post-Diamond evolution threshold is reached or derived state is reconciled.  
**Required observable result:** The conceptual enchantment step follows the approved mapping for the applicable evolution position.  
**Verification intent:** SWE.4 mapping/configuration verification and SWE.6 representative cycle qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-008  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-003 — Deterministic bounded evolution evaluation

**Normative statement:** For every supported cumulative progress value, including any supported maximum state, and one internally consistent approved evolution-configuration snapshot, Main shall determine the applicable material tier, conceptual evolution state, effective enchantments under the current branch, and next-threshold state deterministically and within bounded runtime and memory.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; CAN-MAIN-009; DEC-REQ-007 §5; DEC-REQ-008 §3  
**Rationale:** Keeps evolution evaluation deterministic and safe without prescribing a loop, recursive algorithm, closed-form inversion, precomputed table, or numeric maximum.  
**Precondition / trigger:** A tool is loaded, progressed, displayed, or reconciled.  
**Required observable result:** The same progress, branch, and approved configuration snapshot produce the same evolution result without unbounded work.  
**Verification intent:** SWE.4 property/boundary verification and SWE.5 large-value integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-QLT-002; SWE1-MAIN-002-QLT-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-010 — Configuration-driven derived-state reconciliation

**Normative statement:** When the effective approved evolution configuration changes, Main shall preserve cumulative progress and other authoritative logical Growth Tool state and, before the tool next produces configuration-dependent Growth Tool behavior or presentation, shall derive the applicable material tier, conceptual evolution state, effective enchantments, and next-threshold state from the current approved configuration and authoritative branch. Derived evolution state may promote or demote because of the configuration change.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; DEC-REQ-008 §3  
**Rationale:** Applies balance/configuration changes without rewriting earned progress or forcing a global scan of all stored/physical items.  
**Precondition / trigger:** A Growth Tool is evaluated after an effective approved evolution-configuration change.  
**Required observable result:** Derived configuration-dependent state matches the current approved configuration while logical identity, owner, lifecycle, delivery state, branch, issuance, epoch, and cumulative progress remain unchanged solely because of reconciliation.  
**Verification intent:** SWE.4 reconciliation/state-preservation verification and SWE.5 reload/lazy-reconciliation integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-QLT-007  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-004 — No repair or revival from configuration reconciliation

**Normative statement:** Configuration reconciliation alone shall not restore Growth Tool durability, change a `BROKEN` tool to `ACTIVE`, rotate authority, or otherwise create a repair/reissue effect, including when the new configuration promotes the tool to a higher derived evolution state.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; DEC-REQ-008 §3  
**Rationale:** Prevents configuration reload/reconciliation from becoming a free recovery or authority-change path.  
**Precondition / trigger:** An active or broken tool is reconciled without an independently authorized repair/reissue or a real progress-triggered evolution.  
**Required observable result:** Reconciliation alone causes no repair/revival/authority rotation.  
**Verification intent:** SWE.4 reconciliation/durability/lifecycle verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-011 — Physical durability preservation across reconciliation material change

**Normative statement:** When configuration reconciliation changes the material of a current authorized `ACTIVE` physical Growth Pickaxe, Main shall map its current Minecraft-authoritative remaining-durability fraction to the new material's durability capacity, subject to unavoidable discrete durability quantization, and shall retain at least one remaining durability point. Reconciliation shall not create a separate authoritative current-durability value when the current physical representation is unavailable.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; DEC-REQ-008 §3  
**Rationale:** Preserves player durability state across a derived material-capacity change without duplicating Minecraft physical authority.  
**Precondition / trigger:** Reconciliation changes the current authorized active physical pickaxe material.  
**Required observable result:** Remaining durability fraction is preserved to the representable extent, with minimum one point, and no DB-authoritative physical damage value is invented.  
**Verification intent:** SWE.4 ratio/quantization verification and SWE.5 item reconstruction integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CON-002  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-012 — Progression-triggered full durability recovery

**Normative statement:** An accepted qualifying progress addition that advances the Growth Tool across one or more configured material or post-Diamond enchantment evolution thresholds shall cause one progression-triggered restoration of the applicable current authorized active physical Growth Pickaxe to its maximum durability. A progress addition that crosses no evolution threshold shall not cause progression-triggered durability restoration. This requirement does not prohibit explicitly authorized repair or reissue from restoring durability under their owning requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; DEC-REQ-008 §3  
**Rationale:** Defines the progression reward without accidentally prohibiting separate repair/reissue capabilities.  
**Precondition / trigger:** An eligible progress addition is accepted.  
**Required observable result:** Threshold crossing causes one full-recovery result; no crossing causes no progression-triggered recovery.  
**Verification intent:** SWE.4 threshold-transition verification, SWE.5 Paper durability integration, and SWE.6 mining qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-002; SWE1-MAIN-002-QLT-003  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-007 — Internally consistent evolution configuration

**Normative statement:** One evolution evaluation or reconciliation shall use one complete approved evolution-configuration snapshot and shall not combine mutually dependent thresholds, increment definitions, mappings, caps, or equivalent evolution values from different configuration states.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-012; DEC-REQ-008 §3  
**Rationale:** Prevents mixed-revision evolution results when related balance values change together.  
**Precondition / trigger:** Evolution state is evaluated while configuration may have been reloaded or replaced.  
**Required observable result:** All dependent evolution inputs used for one result come from one internally consistent approved configuration state.  
**Verification intent:** SWE.4 configuration-snapshot consistency verification and SWE.5 configuration-reload integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-QLT-001  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-013 — Terminal durability to Broken transition

**Normative statement:** When a current authorized `ACTIVE` Growth Pickaxe reaches terminal durability through an otherwise valid operation, terminal durability shall not destroy the logical Growth Tool. Unless that same qualifying operation produces an applicable progression-triggered full-durability recovery, Main shall transition the logical lifecycle `ACTIVE → BROKEN` and represent the current physical issuance as a Broken Tool. A same-operation progression recovery shall be applied before the terminal Broken decision.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013; CAN-MAIN-012; DEC-REQ-008 §§3–4  
**Rationale:** Preserves the logical Growth Tool at terminal durability while resolving the evolution-versus-break ordering deterministically.  
**Precondition / trigger:** A current authorized active Growth Pickaxe would otherwise reach terminal durability.  
**Required observable result:** If same-operation progression recovery makes the tool valid, it remains `ACTIVE`; otherwise the same authority transitions to `BROKEN` rather than disappearing.  
**Verification intent:** SWE.4 durability/evolution ordering verification, SWE.5 Paper damage integration, and SWE.6 break/evolution boundary qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-002-CAP-012  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-014 — Broken representation and authority continuity

**Normative statement:** Broken conversion shall preserve the existing logical tool identity, owner, approved tool type, cumulative progress, delivery state, active branch, current physical issuance identity, and authority epoch. The Broken physical representation shall remain distinguishable from an active Growth Pickaxe and carry sufficient supported managed identity to resolve the same current logical authority. The initial V0.0.2 supplied/default Broken presentation shall be `GRAY_DYE`, but presentation/material alone shall not establish authority.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013; CAN-MAIN-003; DEC-REQ-008 §4  
**Rationale:** Makes Broken a lifecycle/presentation transition of the same authority rather than an implicit reissue.  
**Precondition / trigger:** Terminal Broken conversion completes or the current Broken representation is resolved.  
**Required observable result:** The same issuance/epoch/logical authority remains current with `BROKEN` lifecycle and a recognizable non-active representation.  
**Verification intent:** SWE.4 authority/state verification and SWE.5 item/restart integration.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CAP-004  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-CON-005 — Broken lifecycle operation boundary

**Normative statement:** A current authorized `BROKEN` representation shall not function as an active Growth Pickaxe or satisfy an operation requiring `ACTIVE` lifecycle state. Only an explicitly authorized operation whose owning requirements accept `BROKEN` state may restore or otherwise transition the tool. Ordinary possession/drop/storage remains governed by the reviewed Main physical-lifecycle requirements, and applicable Progress/external-modification rules remain governed by their owning requirements.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013; CAN-MAIN-005; DEC-REQ-008 §4  
**Rationale:** Prevents Broken state from acting as an active tool without incorrectly prohibiting ordinary Minecraft possession/lifecycle behavior or duplicating repair/modification requirements.  
**Precondition / trigger:** A Broken representation is presented to an operation or used in gameplay.  
**Required observable result:** ACTIVE-only Growth Tool behavior is unavailable; only explicitly Broken-eligible operations proceed.  
**Verification intent:** SWE.4 lifecycle-policy verification and SWE.6 representative Broken-state qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-CON-007; SWE1-MAIN-001-CON-010  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-MAIN-002-QLT-004 — Durable Broken-state continuity

**Normative statement:** Once a Broken transition is established, the logical `BROKEN` state and same current authority shall remain durably recoverable across supported quit, disable, lifecycle replacement, and restart. Lifecycle interruption/restart alone shall not revert the tool to `ACTIVE`, rotate authority, create replacement entitlement, or erase the Broken transition.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-013; DEC-REQ-008 §4  
**Rationale:** Protects Broken-state continuity without prescribing a synchronous write, checkpoint implementation, event priority, or raw `ItemStack` retention mechanism.  
**Precondition / trigger:** A Broken transition has been established before a supported lifecycle interruption.  
**Required observable result:** Recovery preserves the same logical authority in `BROKEN` state rather than resurrecting or replacing it.  
**Verification intent:** SWE.5 persistence/restart integration and SWE.6 qualification.  
**Priority:** `MUST`  
**Dependencies:** SWE1-MAIN-001-QLT-002  
**Assumptions:** None  
**Open issue / conflict:** Exact persistence/checkpoint policy remains for CAN-MAIN-017 owning review.  
**State:** `DRAFT`

## 4. Historical superseded identifier

### SWE1-MAIN-002-CAP-006 — Configurable ore multipliers

**Disposition:** `SUPERSEDED_BY_OWNER_CORRECTION`  
**Former source:** CAN-MAIN-010  
**Replacement coverage:** Material/ore-dependent progression weighting was withdrawn by `DEC-REQ-007` §6. Uniform progress is controlled by `SWE1-MAIN-002-CAP-005`.  
**Identifier reuse:** Prohibited.

## 5. Later Main requirements still awaiting owning-clause review

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
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-009; CAN-MAIN-017 remains unreviewed and the specific checkpoint mechanism/timing wording will be reassessed.  
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
**Open issue / conflict:** CAN-MAIN-017 remains unreviewed.  
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
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-009; CAN-MAIN-017 remains unreviewed.  
**State:** `DRAFT`

### SWE1-MAIN-002-CAP-016 — Normal authorized durability consumption

**Normative statement:** During ordinary authorized use, an active Growth Pickaxe shall preserve the approved Paper/vanilla nonterminal durability result and shall not suppress normal durability loss merely because the item is managed; explicit exceptions are progression-triggered full recovery, denied or cancelled use, controlled repair/reissue, and terminal conversion to Broken.

**Source:** SWE1-SRC-002 §6 CAN-MAIN-019; reviewed CAN-MAIN-012/CAN-MAIN-013 constrain the named exceptions  
**Rationale:** Makes ordinary durability consumption explicit so repair and Broken behavior are reachable and verifiable.  
**Precondition / trigger:** An authorized active Growth Pickaxe incurs ordinary nonterminal durability damage without an applicable exception.  
**Required observable result:** The post-use durability reflects the platform result; no managed-item guard cancels ordinary durability loss unless an approved exception applies.  
**Verification intent:** SWE.4 durability-policy verification, SWE.5 Paper damage integration, SWE.6 actual mining qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** CAN-MAIN-019 remains unreviewed; current durability remains Minecraft physical authority.  
**State:** `DRAFT`
