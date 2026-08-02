# Phase 10C-A Candidate-5 Execution Entry

Revision: B  
Recorded: 2026-08-03 JST

## 1. Purpose and precedence

This is the sole entry point for Luna to resume Candidate-5 Product work.

Apply these tracked files in order:

1. this execution entry;
2. `phase-10c-a-candidate-5-remediation-handoff.md`;
3. `../release-readiness/V0.0.2/phase-10c-a-candidate-4-preclient-independent-review-addendum.md`;
4. `../release-readiness/V0.0.2/phase-10c-a-candidate-4-preclient-independent-review.md`.

Candidate-4 prepared records are historical evidence only. They cannot authorize runtime or Client
Test.

```text
CANDIDATE-4: REJECTED / PRESERVED
CANDIDATE-5: REQUIRED
CLIENT TEST: DO NOT START
PR #14: OPEN / DRAFT / UNMERGED
```

## 2. Safe local recovery

The local checkout may be behind the remote branch because the independent review and handoff were
pushed after Luna stopped.

First obtain:

```text
current branch
worktree/index status
local HEAD
origin branch HEAD after fetch
PR #14 HEAD and state
local-vs-origin ahead/behind counts
```

A fast-forward is allowed only when:

- the branch is `feature/V0.0.2-main-frontier`;
- worktree and index are clean;
- local is not ahead of origin and has not diverged;
- origin HEAD equals PR #14 HEAD;
- the current origin tree contains this execution entry, the Candidate-5 remediation handoff, and
  both Candidate-4 independent-review files.

Then use only:

```bash
git merge --ff-only origin/feature/V0.0.2-main-frontier
```

Do not discard or rewrite state. Stop and report when the checkout is dirty, locally ahead,
diverged, missing the required files, or inconsistent with PR #14.

## 3. First Luna report

Before adding tests or changing Product code, report only:

```text
LOCAL HEAD
ORIGIN HEAD
PR HEAD
FAST-FORWARD: PERFORMED / NOT NEEDED / BLOCKED
WORKTREE/INDEX: CLEAN / DIRTY
PR #14: OPEN / DRAFT / UNMERGED or mismatch
CANDIDATE-4 ARTIFACTS: PRESERVED / NOT VERIFIED
CLIENT TEST: NOT STARTED / mismatch
NEXT ACTION
```

Continue only when the recovery gate passes.

## 4. Execution

Execute the Candidate-5 remediation handoff. Its scope is limited to:

- Broken Tool Branch denial;
- fail-closed Held Authorization transitions and managed-action guards;
- Frontier late-MVI coordination proof;
- complete bounded Frontier TIMEOUT diagnostics;
- focused and full validation;
- Candidate-5 Product commit, CI/Headless evidence, two formal clean builds, artifact fixation,
  submission package, truthful tracked status, and runtime handoff.

Do not start MariaDB, Redis, Paper, a backend, or a Minecraft Client from this Plugin repository
context.

## 5. Stop state

```text
CANDIDATE-4:
  REJECTED / PRESERVED

CANDIDATE-5:
  PREPARED_FOR_RUNTIME_PREFLIGHT only after every Product gate passes

RUNTIME PREFLIGHT:
  NOT STARTED IN PLUGIN REPOSITORY CONTEXT

CLIENT TEST:
  NOT STARTED

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PRODUCTION BALANCE PROMOTION:
  HOLD

PROJECT ACCEPTANCE:
  PENDING

STABLE PUBLICATION:
  NOT AUTHORIZED
```
