# Cross-target Common Software Requirements

Document ID: `SWE1-COMMON-001`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Target domain: `COMMON`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until superseded  
Primary source: `SWE1-SRC-002` Revision A  
Contained normative items: CON: 8, IFC: 2, QLT: 8

## 1. Purpose

Define software obligations that apply consistently across Core, Main, and Frontier targets and therefore have one shared owner.

## 2. Requirement interpretation rules

- Each item expresses one assessable software obligation.
- Product intent is separated from architecture, class, event-priority, algorithm, and test-procedure decisions.
- Source-prescribed implementation mechanisms are retained only when they are themselves an approved external interface or compatibility constraint.
- A requirement carrying an open issue remains draft and cannot support G1 PASS until the issue is resolved or explicitly accepted as a blocker.
- Full identifiers are used in all downstream traceability.

## 3. Requirements

### SWE1-COMMON-001-CON-001 — Runtime artifact placement

**Normative statement:** The software shall provide separate Core, Main, and Frontier runtime artifacts and shall support only the approved placement: Core on Main and Frontier, Main only on Main, and Frontier only on Frontier; no new Wayfarer-owned gameplay artifact in this scope shall require Lobby deployment.

**Source:** SWE1-SRC-002 §4 CAN-COM-001  
**Rationale:** Prevents runtime-role ambiguity and unintended capability exposure.  
**Precondition / trigger:** At plugin deployment or enablement.  
**Required observable result:** Each artifact either activates only in an approved runtime role or remains fail-closed without registering its gameplay.  
**Verification intent:** SWE.5 deployment-topology integration test and SWE.6 target qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-002 — Target-to-Core dependency direction

**Normative statement:** Wayfarer_Main and Wayfarer_Frontier may depend only on approved Wayfarer_Core public contracts for Wayfarer-owned shared services.

**Source:** SWE1-SRC-002 §4 CAN-COM-002  
**Rationale:** Preserves module ownership and independent deployment.  
**Precondition / trigger:** At build, packaging, class loading, and runtime service resolution.  
**Required observable result:** Target-plugin dependency analysis and runtime resolution show only approved Core public-contract dependencies.  
**Verification intent:** Inspection plus SWE.5 packaging/class-loading integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-001 — Authoritative data ownership

**Normative statement:** The software shall treat MariaDB, Redis, MVI, Wayfarer_Core Waymark services, world files, and Minecraft/Paper as authorities only for the domains assigned in CAN-COM-003.

**Source:** SWE1-SRC-002 §4 CAN-COM-003  
**Rationale:** Avoids conflicting sources of truth.  
**Precondition / trigger:** Whenever state is read, changed, recovered, or reconciled.  
**Required observable result:** The changed state is persisted or mutated only through the authority assigned to that domain.  
**Verification intent:** SWE.2 analysis; SWE.5 authority-boundary integration tests; SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-003 — Prohibited external data access

**Normative statement:** Wayfarer-owned plugins shall not directly manipulate RedisEconomy internal keys or MVI, mcMMO, or EliteMobs internal databases.

**Source:** SWE1-SRC-002 §4 CAN-COM-004  
**Rationale:** Protects external-plugin ownership and upgrade compatibility.  
**Precondition / trigger:** Whenever an external plugin capability is required.  
**Required observable result:** Only approved public/provider/adapter contracts are used; no direct internal data mutation occurs.  
**Verification intent:** Static dependency/source inspection and SWE.5 integration boundary test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-004 — Normal player-state non-ownership

**Normative statement:** Wayfarer-owned MariaDB schemas shall not store normal inventory, armor, offhand, Ender Chest, XP, health, food, or MVI profile state, and the software shall not transfer normal items between Main and Frontier.

**Source:** SWE1-SRC-002 §4 CAN-COM-004  
**Rationale:** Maintains Minecraft/MVI authority and prevents cross-backend leakage.  
**Precondition / trigger:** Whenever player state or item delivery is handled.  
**Required observable result:** Only typed Wayfarer identity, authority, and pending obligations are stored; normal player state remains external.  
**Verification intent:** Schema inspection, source inspection, SWE.5 MVI integration test, SWE.6 cross-backend qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-005 — No unsupported internal API contract

**Normative statement:** The software shall not depend on unsupported internal APIs as a product contract; any unavoidable version-sensitive non-public dependency shall be isolated behind an explicitly approved adapter and limitation.

**Source:** SWE1-SRC-002 §4 CAN-COM-004; §8 CAN-WB-006  
**Rationale:** Contains compatibility risk and prevents internal coupling from spreading.  
**Precondition / trigger:** When a platform or adopted plugin lacks a sufficient public API.  
**Required observable result:** The dependency is isolated, version-checked, fail-closed, documented, and absent from domain-facing contracts.  
**Verification intent:** SWE.2/SWE.3 inspection and SWE.5 compatibility integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-003  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-001 — Main-thread object safety

**Normative statement:** All Minecraft/Paper object access or mutation and event cancellation shall occur on the server main thread.

**Source:** SWE1-SRC-002 §4 CAN-COM-005  
**Rationale:** Paper runtime objects are not generally safe for arbitrary asynchronous access.  
**Precondition / trigger:** Whenever players, inventories, items, blocks, worlds, chunks, GUIs, teleport, velocity, or cancellable events are processed.  
**Required observable result:** No such access or mutation occurs from an asynchronous execution context.  
**Verification intent:** SWE.4 thread-boundary unit verification and SWE.5 runtime integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-002 — No main-thread blocking persistence I/O

**Normative statement:** JDBC, Redis I/O, audit writes, queries, checkpoint persistence, and expiration-candidate searches shall not execute synchronously on the server main thread.

**Source:** SWE1-SRC-002 §4 CAN-COM-005  
**Rationale:** Prevents server-tick stalls and deadlock-sensitive coupling.  
**Precondition / trigger:** Whenever durable or remote state is queried or changed.  
**Required observable result:** The main thread performs only bounded state capture/revalidation/mutation and delegates I/O asynchronously.  
**Verification intent:** SWE.4 unit verification with executor/thread assertions and SWE.5 runtime timing/thread inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-003 — Fail-closed activation

**Normative statement:** A plugin shall not expose affected gameplay until mandatory configuration, runtime role, Core capability, schema compatibility, and required external capability have been validated.

**Source:** SWE1-SRC-002 §4 CAN-COM-006  
**Rationale:** Prevents partially initialized gameplay from corrupting state.  
**Precondition / trigger:** At enablement and capability refresh.  
**Required observable result:** Affected commands/listeners/schedulers remain unavailable or deny operations when a prerequisite is invalid.  
**Verification intent:** SWE.5 lifecycle integration test and SWE.6 representative failure qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** SWE1-ISSUE-001-ISSUE-001  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-004 — Disable and stale-callback containment

**Normative statement:** During disable or runtime replacement, the software shall stop accepting new operations, attempt bounded asynchronous flush, and reject callbacks belonging to an obsolete or disabled runtime generation.

**Source:** SWE1-SRC-002 §4 CAN-COM-006  
**Rationale:** Prevents post-disable mutation and split-brain runtime behavior.  
**Precondition / trigger:** At plugin disable, reload-equivalent replacement, or shutdown.  
**Required observable result:** No obsolete callback mutates current state; bounded flush outcome is observable and unresolved work remains recoverable.  
**Verification intent:** SWE.4 lifecycle unit verification and SWE.5 disable/restart integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-005 — Duplicate-effect prevention

**Normative statement:** Financial, authority-rotation, and durable-delivery operations shall use explicit operation identity and shall prevent duplicate debit, duplicate refund, duplicate item delivery, and stale replay.

**Source:** SWE1-SRC-002 §4 CAN-COM-007  
**Rationale:** Protects economic and item authority under retries and concurrent input.  
**Precondition / trigger:** On replay, double click, reconnect, timeout, or concurrent request.  
**Required observable result:** At most one authoritative effect is committed for one operation identity; later replays return the established disposition or a safe unresolved state.  
**Verification intent:** SWE.4 idempotency unit verification, SWE.5 transaction/delivery integration test, SWE.6 representative qualification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-006 — Ambiguous outcome safety

**Normative statement:** An ambiguous external provider result shall be represented as `UNKNOWN`, shall not be treated as success, and shall not be automatically retried.

**Source:** SWE1-SRC-002 §4 CAN-COM-007  
**Rationale:** Avoids double effect where the external result cannot be proven.  
**Precondition / trigger:** When provider completion cannot be determined reliably.  
**Required observable result:** The operation remains non-terminal or manually reconcilable without a second automatic provider invocation.  
**Verification intent:** SWE.4 outcome-policy unit verification and SWE.5 provider-failure integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-IFC-002 — Module-owned migration namespaces

**Normative statement:** Core, Main, and Frontier shall own separate table prefixes, migration locations, and migration histories and shall not create or mutate another module's tables.

**Source:** SWE1-SRC-002 §4 CAN-COM-008  
**Rationale:** Preserves independent lifecycle and upgrade ownership.  
**Precondition / trigger:** On empty installation or upgrade.  
**Required observable result:** Only the applicable Core plus target-plugin migrations are discovered and applied, with distinct history ownership.  
**Verification intent:** Schema inspection and SWE.5 empty/upgrade migration integration tests.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-006 — Applied migration immutability

**Normative statement:** An applied migration shall never be modified, reordered, deleted, or reused; schema evolution shall be forward-only.

**Source:** SWE1-SRC-002 §4 CAN-COM-008  
**Rationale:** Protects production upgrade determinism.  
**Precondition / trigger:** Whenever a durable schema change is required.  
**Required observable result:** A new migration is added after the accepted sequence; checksum verification of prior migrations remains unchanged.  
**Verification intent:** Inspection and migration checksum verification.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-007 — Install and upgrade compatibility

**Normative statement:** The applicable module migrations shall support both an empty database and an upgrade from the accepted V0.0.1 database baseline without destructive automatic reset.

**Source:** SWE1-SRC-002 §4 CAN-COM-008; §9 CAN-SCOPE-004  
**Rationale:** Supports first installation and safe continuation from the accepted baseline.  
**Precondition / trigger:** At first installation and V0.0.1-to-current upgrade.  
**Required observable result:** Schema creation/upgrade completes or fails without partial cross-module ownership or destructive reset.  
**Verification intent:** SWE.5 database integration tests and inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-QLT-008 — Sanitized auditability

**Normative statement:** Security-sensitive, financial, delivery, recovery, administrative, and reconciliation outcomes shall be auditable without logging secrets, raw credentials, or unnecessary personal/runtime data.

**Source:** SWE1-SRC-002 §4 CAN-COM-009  
**Rationale:** Supports diagnosis and accountability without creating a data-leak path.  
**Precondition / trigger:** Whenever an auditable operation reaches a meaningful disposition.  
**Required observable result:** A sanitized audit reference and operationally useful disposition are available; forbidden sensitive content is absent.  
**Verification intent:** SWE.4 formatting/redaction verification, SWE.5 audit integration test, inspection.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-007 — Core independence from gameplay plugins

**Normative statement:** Wayfarer_Core shall not depend on Wayfarer_Main or Wayfarer_Frontier.

**Source:** SWE1-SRC-002 §4 CAN-COM-002  
**Rationale:** Keeps the shared foundation deployable without target gameplay artifacts.  
**Precondition / trigger:** At build, packaging, and runtime service resolution.  
**Required observable result:** Core compiles, packages, and loads without Main or Frontier classes/services.  
**Verification intent:** Inspection plus SWE.5 packaging/class-loading integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`

### SWE1-COMMON-001-CON-008 — Main/Frontier mutual independence

**Normative statement:** Wayfarer_Main and Wayfarer_Frontier shall not depend on each other, and the Wayfarer module graph shall remain acyclic.

**Source:** SWE1-SRC-002 §4 CAN-COM-002  
**Rationale:** Allows independent target deployment and prevents circular startup contracts.  
**Precondition / trigger:** At dependency resolution and runtime loading.  
**Required observable result:** No direct or transitive Main↔Frontier dependency or cycle exists.  
**Verification intent:** Dependency-graph inspection and SWE.5 class-loading integration test.  
**Priority:** `MUST`  
**Dependencies:** None  
**Assumptions:** None  
**Open issue / conflict:** None  
**State:** `DRAFT`
