# Project Wayfarer V0.0.2
## Phase 10B-C Candidate-3 Independent Review
### Revision B — corrected input accounting

Review date: 2026-08-02 JST

# 1. Reviewed evidence set

The following three Phase 10B-C deliverables were verified together.

```text
Submission ZIP:
  PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_SUBMISSION.zip
  size 4955
  SHA-256 293f59881f244a565bf9385ea2d6b390ae4d86f41c9d4886758e2406990c13dc
  ZIP integrity PASS

External sidecar:
  PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_SUBMISSION.zip.sha256
  size 139
  SHA-256 b0171c403907ebf659cb642404430ad9547e4548c2fb8c29c1e3e28406f5e835
  target filename matches
  target ZIP SHA-256 matches

Complete result report:
  PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_RESULT.md
  size 9032
  SHA-256 5a676a9fb497dc441abe21d454785586692786c355b90e88cd5356e597d09a99
```

The compact ZIP intentionally contains sanitized summary evidence, while the complete report was
supplied as a separate file. The external sidecar is present and valid.

The Candidate checksum file contains a valid 64-character Core SHA-256:

```text
b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2
```

Earlier statements that the sidecar, complete report, or valid Core hash were absent are withdrawn.

The evidence set is sufficient to determine the Candidate-3 failure and next remediation scope.
It is not a Candidate PASS package because the mandatory focused gate itself failed.

# 2. Candidate-3 source and automated verification

Candidate-3 source authority:

```text
Candidate-2 metadata base:
  5b939ae055bd60fe3ad1bd01ba115c2ae674830d

Frontier remediation:
  1aa01ca953cc6630a45382f9ad325d4db911d222

Candidate-3 product HEAD:
  25200ad4745fdaae79c761a56083f6e648e9a06b
```

The complete result report records:

```text
Normal CI:
  run 30749708887
  success at exact Candidate-3 product HEAD

Pre-client Headless Runtime:
  run 30749708891
  success at exact Candidate-3 product HEAD

Main JAR:
  size 4678598
  SHA-256 7d17fbeedec09d8742bf8c9adde0bef2eec6adac5b07d5fb73021234a13e2da3

Frontier JAR:
  size 4703856
  SHA-256 676168d135852cad1b45c147cedb916a3d6ed1baddb1b8ad8bcb7c65ba282a9c

Core:
  unchanged
```

The Main numeric-PDC correction reads Long as Long, checks Integer before reading Integer, converts
it to long, and returns the absent sentinel for absent or unsupported numeric types. No
Material, Lore, or Display Name fallback is used.

# 3. Main focused observations

The complete report records the following Candidate-3 observations:

```text
Clean first delivery:
  PASS

Three reconnects:
  no duplicate

Authority/delivery state:
  stable

Ordinary inventory movement:
  PASS
```

The same run also confirmed Candidate-3's then-current Growth Tool transfer restrictions:

```text
ordinary Container insertion:
  blocked

manual Drop:
  blocked
```

Those two results are valid observations of Candidate-3, but they are no longer desired acceptance
criteria. The Owner subsequently replaced that physical-transfer policy.

# 4. Frontier focused observations

The complete report records:

```text
Initial exact-world delivery:
  Elytra 1
  Grappling Hook 1
  Navigation 1
  Launchpad 2

Three reconnects:
  no permanent duplicate

Backend restart/reconnect:
  stable

Pending-delivery attempts:
  remained 1
```

The exact-current duplicate self-heal focused gate failed.

The report states that exact-current permanent items were copied through the verified vanilla Item
Copy route. After controlled reconnect, the extra exact-current copies remained.

Sanitized runtime evidence:

```text
WAYFARER_ENTRY_STABILIZATION_BEGIN; source=BOUNDED_FINGERPRINT;
requiredManagedItems=3

Frontier safe entry stabilization timed out; retry later
```

No duplicate-self-heal event was observed.

# 5. Failure interpretation

Current Candidate-3 control flow performs duplicate cleanup only after Safe Entry reaches
`TraversalDeliveryCoordinator.onSafeEntry(...)`.

Therefore:

```text
Readiness TIMEOUT:
  confirmed

Self-heal path reached:
  no

Exact-current cleanup algorithm independently disproven:
  no

Mandatory focused self-heal gate:
  FAIL
```

The current timing boundary is five observations at a five-tick polling period, allowing timeout
after approximately one second. The complete report does not contain enough poll-by-poll evidence
to establish exactly when MVI restored the copied inventory. The exact timing cause therefore
remains unresolved.

The next Candidate must:

- preserve bounded/fail-closed readiness;
- extend and instrument the finite observation window;
- handle a public MVI completion signal that arrives after the original request has timed out;
- coalesce any restart;
- prohibit unbounded retries;
- rerun the exact duplicate scenario without duplicating Launchpads or arbitrary items.

A blind one-tick delay is not an acceptable remediation.

# 6. Owner-authorized Growth Tool redesign

The Owner established the following latest authority after Candidate-3 testing:

```text
Manual Drop:
  allowed

Ordinary Pickup:
  allowed

Non-owner physical possession:
  allowed

Ordinary Container storage:
  allowed

Death Drop:
  allowed

Old Instance/Epoch physical possession:
  allowed

Non-owner or stale use:
  denied

Full authority/PDC comparison:
  when the managed Tool enters Main Hand

Per-use full PDC revalidation:
  not required

Invalid held managed Tool:
  unusable, including Block Break denial

Successful physical distribution:
  visible through a chat message

Provisional display names:
  required for Main Growth Tool and Beyond delivery items

General inventory cleanup:
  prohibited
```

Candidate-3 does not satisfy this updated Main contract and must not be promoted even if its
Frontier self-heal failure were ignored.

# 7. GitHub state at review

PR #14 is:

```text
Open
Draft
Unmerged
Mergeable
Head 25200ad4745fdaae79c761a56083f6e648e9a06b
```

The PR body still describes Candidate-2 as the current prepared candidate. It must be corrected by
the Candidate-4 work order without claiming Candidate-3 PASS.

# 8. Final independent verdict

```text
PHASE 10B-C INDEPENDENT REVIEW:
  FAIL

PHASE 10B-C EVIDENCE SET:
  VERIFIED / SUFFICIENT FOR FAILURE VERDICT

CANDIDATE-3 AUTOMATED CI:
  PASS

CANDIDATE-3 HEADLESS RUNTIME:
  PASS

CANDIDATE-3 MAIN OBSERVATIONS:
  PASS FOR EXECUTED OLD-SPEC GATES

CANDIDATE-3 FRONTIER DELIVERY/RECONNECT/RESTART:
  PASS

CANDIDATE-3 FRONTIER EXACT-CURRENT SELF-HEAL:
  FAIL

CANDIDATE-3:
  REJECTED / PRESERVED

CANDIDATE-4:
  REQUIRED

FULL CLIENT ACCEPTANCE:
  NOT COMPLETE

PRODUCTION BALANCE PROMOTION:
  HOLD

PROJECT ACCEPTANCE:
  PENDING

STABLE PUBLICATION:
  NOT AUTHORIZED
```
