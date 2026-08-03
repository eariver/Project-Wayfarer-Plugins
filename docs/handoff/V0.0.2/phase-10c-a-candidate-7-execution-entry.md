# Phase 10C-A Candidate-7 Execution Entry

Revision: A  
Recorded: 2026-08-04 JST

## 1. Precedence and role boundary

Read this file first.

Execution authority precedence is:

1. this Candidate-7 Execution Entry;
2. `phase-10c-a-candidate-7-remediation-handoff.md` Revision B;
3. `phase-10c-a-candidate-6-independent-product-package-review.md`;
4. older Candidate records as historical evidence only.

The Owner/ChatGPT boundary owns the Product design and test contract. Luna implements the specified
design, executes the specified tests/commands, and reports evidence. Luna must not select a different
algorithm, alter scope, or replace the prescribed tests. Mechanical compiler/import/test-fixture
adjustments are permitted only when semantics remain unchanged. A genuine specification conflict
requires STOP and report before a substitute Product change.

## 2. Recovery

Follow the Recovery Gate in the Candidate-7 Handoff Revision B. When the gate passes, continue without
waiting for another confirmation.

## 3. FIFO test-sequence clarification

The per-Player FIFO serializer means request B cannot apply while request A's complete serialized body
is still unfinished. Therefore the mandatory `staleRecoveryCannotCloseOrOverwriteNewerSession` test
must use this exact order:

1. start request A and hold its conflict/failure recovery result;
2. accept request B for the same Player, making A's generation stale while B remains queued;
3. complete A's held recovery;
4. assert A performs no Session close/open, inventory reconciliation, or Held Authorization change;
5. assert B's database supplier starts only after A's serialized body completes;
6. complete B and assert B alone installs the current Tool ID, Item Instance, Epoch, lock version,
   Status, and Held Authorization.

Do not attempt to apply B before A completes; that would contradict the prescribed FIFO design.

All other Product design, file scope, tests, validation commands, evidence requirements, and stop
conditions remain exactly as written in the Candidate-7 Handoff Revision B.

## 4. Runtime boundary

Disposable MariaDB/Redis, migrations in disposable test environments, GitHub Actions, and Headless
Paper are authorized non-client validation.

Server-side Runtime Preflight remains pending independent Candidate-7 Product/package review.
Minecraft Client connection and client-driven scenarios remain deferred and must not start.
