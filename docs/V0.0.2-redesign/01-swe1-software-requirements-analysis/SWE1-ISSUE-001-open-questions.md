# V0.0.2 SWE.1 Open Questions, Conflicts, and Baseline Dependencies

Document ID: `SWE1-ISSUE-001`  
Revision: A  
State: `DRAFT_FOR_OWNER_REVIEW`  
Date: 2026-08-05 JST  
Author: ChatGPT  
Reviewer: Project Owner  
SWE process: SWE.1 Software Requirements Analysis  
Support domain: `ISSUE`  
Introduced Product version: Plugin V0.0.2 redesign  
Applicable Product versions: V0.0.2 until resolved or superseded  
Primary source: `SWE1-SRC-002` Revision A

## 1. Purpose

Record every unresolved question, contradiction, external feasibility dependency, or missing product
decision identified during decomposition. These items are not substitute requirements. A requirement
that cites one of these items remains draft until the issue is resolved or explicitly accepted as a
G1 blocker.

## 2. Issues

### SWE1-ISSUE-001-ISSUE-001 — Missing `frontier_iris` at enablement

**Problem:** The canonical source requires Frontier not to generate the world and to fail closed outside exact `frontier_iris`, but it does not decide whether the plugin as a whole must disable, enter a degraded administrative-only state, or remain enabled with gameplay dormant when the world is absent.

**Impact on SWE.1:** Owner decision required before G1 PASS because the externally observable health/lifecycle state is not defined.

**Required resolution content:** Options to review: disable plugin; degraded/admin-health-only; enabled but all WB entry points dormant. The decision must define health/status, listener/scheduler registration, recovery when the world later appears, and restart behavior.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-002 — Supported external repair boundary

**Problem:** The source requires denial of supported external repair routes but does not define which external repair plugins or event contracts are in the supported compatibility boundary.

**Impact on SWE.1:** Owner acceptance of a bounded support contract is required before the requirement can be judged complete.

**Required resolution content:** SWE.2 should propose a public/cancellable API support boundary using authoritative references. Unsupported tools that bypass Paper/public hooks must be explicitly excluded or separately adapted.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-003 — LeafGrapple 1.0.2 safe integration

**Problem:** The source fixes LeafGrapple 1.0.2 and requires authentic hook generation, no durability, and no entity/player hooking, but the exact supported API and deployable safe tier/configuration are not established.

**Impact on SWE.1:** External capability evidence and Owner approval are required before G1 can declare the requirement feasible.

**Required resolution content:** Required evidence: versioned official/public API or isolated adapter boundary, configuration that disables durability and entity/player hooks, initialization/failure semantics, and client-observable motion acceptance.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-004 — WorldEdit/FAWE protection support boundary

**Problem:** The source requires launchpad protection from supported WorldEdit/FAWE editing but does not define whether support is limited to public WorldEdit EditSession hooks or extends to tools that bypass public/Bukkit hooks.

**Impact on SWE.1:** A precise supported integration boundary is required to make the requirement objectively verifiable.

**Required resolution content:** SWE.2 must identify official public contracts and classify bypassing tools as supported, unsupported limitation, or separately integrated.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-005 — Launchpad physical-material identity across configuration changes

**Problem:** The source defines `LIGHT_WEIGHTED_PRESSURE_PLATE` as the initial block and defines some creation-time values, but does not decide how an existing active launchpad is recognized if the configured physical material changes.

**Impact on SWE.1:** Owner/design decision required before architecture baseline because persistence/world reconciliation depends on the authority model.

**Required resolution content:** Options include immutable durable definition/material snapshot, versioned definition lookup, or prohibiting material changes while active records exist. Current-world material alone must not silently orphan authority.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-006 — Authoritative return path under portal denial

**Problem:** Vanilla portal traversal is denied in `frontier_iris`, while Gate implementation and an in-world return structure are outside scope. The source does not identify the authoritative command/proxy return mechanism that prevents players from being trapped.

**Impact on SWE.1:** Owner/Project runtime decision required before G1 qualification intent is complete.

**Required resolution content:** The decision must identify the owning system, availability/permission, failure behavior, and what SWE.6 must verify without making Wayfarer_Frontier own Gate/profile switching.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-007 — Player-paid Growth Tool reissue invocation context

**Problem:** Paid reissue behavior and price are defined, but the allowed invocation context is not: command versus GUI, exact world/backend restrictions, permission, and whether a missing physical item is required to open an alternative management route.

**Impact on SWE.1:** Owner decision required before the user-visible capability is unambiguous.

**Required resolution content:** The resolution must preserve pre-debit rejection for current item/pending delivery and must not require possession of the missing item to start the recovery flow.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-008 — Command-to-permission group allocation

**Problem:** Exact permission node names are approved, but the complete mapping from gameplay/administrative routes to read, delivery, modify/launchpad, reconcile, use, and debug groups is not yet controlled.

**Impact on SWE.1:** SWE.1 may retain capability requirements, but G1 requires agreement on the externally visible authorization intent or an explicit allocation rule.

**Required resolution content:** SWE.2 should provide a route/operation matrix. Ambiguous or multi-group commands must be split or specify the strongest directly enforced group.

**State:** `OPEN`

### SWE1-ISSUE-001-ISSUE-009 — Maximum normal-progress loss window

**Problem:** The original source states a five-minute periodic checkpoint and allows that much crash loss; the canonical analysis preserves a declared bounded loss window but does not decide whether five minutes is fixed or configurable with a maximum.

**Impact on SWE.1:** Owner decision required to make the reliability requirement measurable.

**Required resolution content:** Options: fixed five minutes; configurable at or below five minutes; another explicit maximum. Critical transitions remain immediate regardless.

**State:** `OPEN`

## 3. Issue disposition rules

- Resolution must be committed as an Owner or controlled engineering decision and linked from every
  affected requirement.
- A resolution that changes product behavior requires impact analysis against the canonical source,
  scope, target requirements, verification intent, and traceability.
- A purely architectural/API choice may be resolved in SWE.2 only when the SWE.1 observable behavior
  is already unambiguous.
- G1 may be recommended `BLOCKED_OWNER_DECISION` while any issue prevents a testable requirement or
  feasibility conclusion.
