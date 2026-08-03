# Phase 10C-A Candidate-7 Execution Entry

Revision: B  
Recorded: 2026-08-04 JST

## 1. Precedence and role boundary

Read this file first.

Execution authority precedence is:

1. this Candidate-7 Execution Entry Revision B;
2. `phase-10c-a-candidate-7-remediation-handoff.md` Revision C;
3. `phase-10c-a-candidate-6-independent-product-package-review.md`;
4. older Candidate records as historical evidence only.

The Owner/ChatGPT boundary owns the Product design and test contract. Luna implements the specified
change, executes the specified commands, and reports evidence. Luna must not reintroduce the
superseded Revision-B FIFO serializer, extra serializer tests, duplicate validation commands, a second
formal build, or an evidence ZIP.

Mechanical compiler/import/test-fixture adjustments are permitted only when semantics remain
unchanged. A genuine specification conflict requires STOP and report before a substitute Product
change.

## 2. Recovery and continuation

Follow the Recovery Gate in the Candidate-7 Handoff Revision C. When the gate passes, continue without
waiting for another confirmation.

## 3. Current exact scope

Candidate-7 uses a per-Player monotonic request generation in `MainGameplayRuntime` for admin
Reissue/Revoke and authority refresh/recovery completion. Concurrent database work remains permitted;
only a completion belonging to the current request may change Session, inventory, Held Authorization,
or start reissue delivery/follow-up refresh.

Do not create `MainAuthorityOperationSerializer` and do not copy the Frontier serializer.

The required Product/test files, five focused behaviors, commands, one clean-build fixation, tracked
evidence, and stop conditions are exactly those in Handoff Revision C.

## 4. Runtime boundary

Disposable MariaDB/Redis, disposable migrations, GitHub Actions, and Headless Paper are authorized
non-client validation.

Server-side Runtime Preflight remains pending independent Candidate-7 review. Minecraft Client
connection and client-driven scenarios remain deferred and must not start.
