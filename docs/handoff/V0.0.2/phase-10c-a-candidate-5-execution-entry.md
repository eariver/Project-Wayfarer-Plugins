# Phase 10C-A Candidate-5 Execution Entry

Recorded: 2026-08-03 JST

## 1. Purpose

This is the entry point for Luna to resume Product work after ChatGPT's Candidate-4 pre-client
review.

Read and apply these files in descending precedence:

1. this execution entry;
2. `phase-10c-a-candidate-5-remediation-handoff.md`;
3. `phase-10c-a-candidate-4-preclient-independent-review-addendum.md`;
4. `phase-10c-a-candidate-4-preclient-independent-review.md`;
5. historical Candidate-4 prepared records only as evidence.

A lower-precedence historical record must not reopen Candidate-4 or authorize Client Test.

## 2. Current state

```text
CANDIDATE-4:
  REJECTED / PRESERVED

CANDIDATE-5:
  REQUIRED

CLIENT TEST:
  DO NOT START

PR #14:
  OPEN / DRAFT / UNMERGED
```

Required immutable ancestry includes:

```text
70b66e17b7308ae4c9529a1685f820e8a7773bfa
b61be3dc5c69e22d507c027f4a3939d2da9330f3
24684652b00ae6e5f9cd00215f8f2fe237cc9ef1
3197039bc1b1ba7efd5c29e0fb143e952b8a37a2
```

## 3. Safe local recovery and fast-forward

ChatGPT updated the remote branch after Luna's previous local stop. A clean local checkout may
therefore be behind origin.

Run read-only checks first:

```bash
git branch --show-current
git status --porcelain=v1
git diff --exit-code
git diff --cached --exit-code
git rev-parse HEAD
git fetch origin
git rev-parse origin/feature/V0.0.2-main-frontier
git log --oneline --decorate --max-count=20
git reflog --max-count=20
```

A fast-forward is authorized only when all are true:

- branch is `feature/V0.0.2-main-frontier`;
- worktree and index are clean;
- local HEAD is an ancestor of origin HEAD;
- there is no local-only commit;
- origin HEAD equals PR #14 head;
- origin history contains all immutable ancestry commits above.

Then run only:

```bash
git merge --ff-only origin/feature/V0.0.2-main-frontier
```

Rerun the complete starting gate from the Candidate-5 remediation handoff afterward.

STOP without reset, clean, stash, rebase, amend, cherry-pick, force push, or deletion when:

- local HEAD is ahead of origin;
- local and origin have diverged;
- worktree/index is dirty;
- an unexplained local artifact or restored IntelliJ change exists;
- origin and PR head differ;
- required ancestry is absent.

## 4. First Luna report

Before adding a test or changing Product code, report:

```text
LOCAL HEAD
ORIGIN HEAD
PR HEAD
FAST-FORWARD PERFORMED: YES/NO
WORKTREE/INDEX: CLEAN
REQUIRED REVIEW ANCESTRY: PASS/FAIL
PR #14: OPEN/DRAFT/UNMERGED
V0.0.2 TAG/RELEASE: ABSENT/PRESENT
CANDIDATE-4 ARTIFACTS: PRESERVED
CLIENT TEST: NOT STARTED
NEXT ACTION: first focused RED test
```

Do not continue when any authority check fails.

## 5. Product execution authority

After the recovery gate passes, execute the complete Candidate-5 remediation handoff in order.

The required scope is:

- Broken Tool Branch denial;
- synchronous fail-closed Held Authorization transitions;
- actual-held-item guards for managed operations;
- late-MVI runtime coordination proof;
- complete Frontier TIMEOUT diagnostics;
- Tests-first RED evidence;
- local focused/module/full validation;
- Product commit and normal push;
- CI/Headless SHA classification;
- two formally qualified clean builds;
- Candidate-5 fixation;
- sanitized submission package and external sidecar;
- tracked status and PR-body synchronization;
- runtime handoff only.

Do not start a runtime or Minecraft Client from this Plugin repository context.

## 6. Stop state

```text
PHASE 10C-A CANDIDATE-5 PRODUCT REMEDIATION:
  PASS or FAIL based on actual evidence

CANDIDATE-4:
  REJECTED / PRESERVED

CANDIDATE-5:
  PREPARED_FOR_RUNTIME_PREFLIGHT only after all Product gates pass

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
