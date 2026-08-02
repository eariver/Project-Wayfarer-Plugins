≠rá^—f•ñÿ¶{Oly 'v√Æ∂õ≠# Phase 10C-A Candidate-4 RED Test Evidence

Recorded: 2026-08-03 JST  
Purpose: prove the new Owner-Bind and Frontier readiness contract was added before Product Code.

## Main RED run

Command:

```text
./gradlew.bat --no-daemon --console=plain :plugins:wayfarer-main:test --tests '*HeldGrowthToolAuthorizationTest'
```

Result: `FAILED` (exit code 1) at `:plugins:wayfarer-main:compileTestJava`.

The intended RED cause was present: the newly added tests referenced the not-yet-created
`HeldGrowthToolAuthorization`, `HeldGrowthToolAuthorizer`, `GrowthToolInventoryPolicy`, and
`GrowthToolDeliveryPresentation` production types. The updated death test also defines the new
expected contract but no Product Code change had been made to satisfy it. No test was skipped,
disabled, quarantined, or weakened; execution did not reach the test task because compilation
correctly failed first.

## Frontier RED run

Command:

```text
./gradlew.bat --no-daemon --console=plain :plugins:wayfarer-frontier:test --tests '*SafeEntryReadinessTest'
```

Result: `FAILED` (exit code 1) at `:plugins:wayfarer-frontier:compileTestJava`.

The intended RED cause was present: the newly added tests referenced the not-yet-created
`EntryCycleRegistry` and `TraversalItemPresentation` production types. The changed readiness test
also expects the new finite 40-observation boundary and the zero-required-items stable path, which
the current Product Code does not yet implement. No test was skipped, disabled, quarantined, or
weakened.

## Source-order assertion

At both RED runs, only test sources and this evidence file had been added/changed for Candidate-4;
the Candidate-3 Product Code remained unmodified. Product implementation begins only after this
evidence was preserved.
