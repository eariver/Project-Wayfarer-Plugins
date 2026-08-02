�r�^�f��ئ{Oly�'vî���# Phase 10B-C — Candidate-3 Formalization and Focused Retest Result

**実施日:** 2026-08-02 (JST)  
**Candidate:** `V0.0.2-Client-Candidate-3`  
**判定:** `REJECTED`  
**次Candidate:** `Candidate-4 REQUIRED`

## 1. Authority and stop condition

The formal instruction and independent review were read in full and their SHA-256
values matched the supplied authority:

| Document | SHA-256 |
|---|---|
| `Luna_Max_Phase_10B_C_Candidate_3_Formalization_and_Focused_Retest_Instructions.md` | `E948FD68FE48E40A6C8CE66094F358D162DB63B50C2283CE407ACBBAF00BD404` |
| `PHASE_10B_B_CANDIDATE_2_AND_3_INDEPENDENT_REVIEW.md` | `455906667F4F45EC16BB068A5DA5E3D5CE8DBDF3903CCD0D2C0B7F9D24DDE9BC` |

The required Candidate-3 source chain was present and contiguous:

```text
5b939ae055bd60fe3ad1bd01ba115c2ae674830d
  -> 1aa01ca953cc6630a45382f9ad325d4db911d222
  -> 25200ad4745fdaae79c761a56083f6e648e9a06b
```

The branch was clean, the chain was an ancestor chain, and the product branch was
fast-forward pushed normally. No force push, history rewrite, or Candidate-3 runtime
source change was made during this retest.

The focused-gate stop condition was reached when exact-current duplicate self-heal
failed in the real Frontier client scenario. Therefore the residual/full client matrix
was not started, and PR #14 was not synchronized as a passing Candidate-3 result.

## 2. Source and verification evidence

Candidate-3 product range `5b939ae..25200ad` contained seven files, with `318
insertions(+), 20 deletions(-)`:

```text
plugins/wayfarer-frontier/src/main/java/io/github/eariver/wayfarer/frontier/application/SafeEntryReadiness.java
plugins/wayfarer-frontier/src/main/java/io/github/eariver/wayfarer/frontier/application/TraversalDeliveryCoordinator.java
plugins/wayfarer-frontier/src/main/java/io/github/eariver/wayfarer/frontier/gameplay/FrontierGameplayRuntime.java
plugins/wayfarer-frontier/src/test/java/io/github/eariver/wayfarer/frontier/application/SafeEntryReadinessTest.java
plugins/wayfarer-frontier/src/test/java/io/github/eariver/wayfarer/frontier/application/TraversalDeliveryCoordinatorTest.java
plugins/wayfarer-main/src/main/java/io/github/eariver/wayfarer/main/gameplay/MainGameplayRuntime.java
plugins/wayfarer-main/src/test/java/io/github/eariver/wayfarer/main/gameplay/MainGameplayRuntimePersistenceTest.java
```

The Main numeric PDC read path is fail-closed: stored Long is read as Long; Integer
is checked and converted to long; absent and unsupported types return the absent
sentinel; no material, lore, or display-name fallback exists. The Candidate-3 bytes
were not changed after the real-client failure.

## 3. Local and product-head verification

The following local checks passed:

```text
:plugins:wayfarer-main:test (focused persistence/delivery/death tests)
:plugins:wayfarer-main:test
:plugins:wayfarer-frontier:test (focused readiness/coordinator tests)
:plugins:wayfarer-frontier:test
check
:plugins:wayfarer-main:shadowJar :plugins:wayfarer-frontier:shadowJar
```

The normal fast-forward push completed from `5b939ae` to `25200ad`.

- Normal CI: [run 30749708887](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30749708887) — exact product HEAD `25200ad4745fdaae79c761a56083f6e648e9a06b`, `success`.
- Pre-client Headless Runtime: [run 30749708891](https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30749708891) — exact product HEAD, `success`.
- Headless evidence artifact: `preclient-headless-evidence`, digest `sha256:dcb9fee619daa80a271a1f564439f8aa8eaa275a212ecf2823048c63dd982cae`.

Two clean-build passes reproduced the Main and Frontier JAR bytes:

| Artifact | Size | SHA-256 |
|---|---:|---|
| `wayfarer-main-0.0.2-SNAPSHOT.jar` | 4,678,598 | `7d17fbeedec09d8742bf8c9adde0bef2eec6adac5b07d5fb73021234a13e2da3` |
| `wayfarer-frontier-0.0.2-SNAPSHOT.jar` | 4,703,856 | `676168d135852cad1b45c147cedb916a3d6ed1baddb1b8ad8bcb7c65ba282a9c` |
| unchanged `Wayfarer_Core-V0.0.1.jar` dependency | 11,751,447 | `b045581d3984dddba10ed7b2ada435926b8538ba9b29a1150ce59588395a2` |

The Candidate-3-specific checksum file was generated and the hashes were verified
against the staged artifacts.

## 4. Focused client evidence

The fresh C3R2 environments used new MariaDB, Redis, world, player-data, and MVI
state. The same local Minecraft client/account `kanpyo_himono` was used for both
modules as permitted by the operator; no second LAN client was used.

### Main — PASS

- Clean first join used a new authority path: no authority -> authority created ->
  canonical Growth Tool -> physical insertion -> `DELIVERED`.
- The client received exactly one `木のツルハシ`.
- Three reconnects left item count, epoch, delivery state, and pending attempt count
  unchanged.
- Inventory regression passed for ordinary inventory movement, chest interaction,
  Growth Tool movement, chest insertion blocking, and drop blocking.
- Only `wayfarer.main.admin.read` was temporarily granted; it was removed after the
  Main checks. No wildcard permission and no OP were used.

### Frontier — initial delivery, reconnect, and restart PASS

The corrected test-only LeafGrapple tier configuration loaded two tiers. On clean
first entry the client reported exactly:

```text
Elytra 1
Grappling Hook 1
Navigation compass 1
Launchpad pressure plates 2
```

The delivery message was `Worlds Beyond loadout items were delivered.` Three
reconnects and one backend restart/reconnect preserved the same inventory result.
Database evidence remained stable: three item authorities, four pending-delivery
rows, and `attempts=1` for each row, including Launchpad quantity 2. No permanent
item count or pending attempt count changed.

### Frontier — exact-current duplicate self-heal — FAIL

The current Elytra, Grappling Hook, and Navigation items were copied to empty slots
using the vanilla console Item Copy path. No raw NBT, PDC, or database authority was
edited. This intentionally created one exact-current duplicate for each of the three
managed items (and the two Launchpad items were correspondingly duplicated by the
copy setup).

After the controlled reconnect, the operator reported:

```text
余分な3アイテムは消えていません。
```

The server evidence showed the bounded entry readiness phase timing out before the
delivery/self-heal operation. A sanitized form of the relevant observation is:

```text
WAYFARER_ENTRY_STABILIZATION_BEGIN; source=BOUNDED_FINGERPRINT;
requiredManagedItems=3
Frontier safe entry stabilization timed out; retry later
```

There was no `WAYFARER_DUPLICATE_SELF_HEAL` event and no
`Frontier exact current duplicate self-heal applied` event. The database authority,
permanent item count, and pending-delivery attempt count stayed unchanged, but the
duplicate cleanup gate itself did not pass. Frontier inventory regression was not
run after this failure, as required by the stop condition.

An earlier Frontier-R2 attempt had two non-product issues: the initial fixture lacked
the `wayfarer` Grappling Hook tier, and the operator died during the planned sequence.
Those results were discarded as proof; R3 corrected the fixture and reproduced the
self-heal gate failure with the same Candidate-3 JAR hashes.

## 5. Permissions, cleanup, and scope limits

Only the exact read leaf was used for the active module:

```text
wayfarer.main.admin.read
wayfarer.frontier.admin.read
```

The temporary leaves were removed after testing. The delivery leaf was not needed.
No wildcard permission (`*`, `wayfarer.main.admin.*`, or
`wayfarer.frontier.admin.*`) and no OP were used in the focused retest.

The separate LAN-PC client reachability was not independently demonstrated because
the operator requested a single-client test. Host-side endpoint reachability was
available while the servers were running, but this report does not claim a second-PC
acceptance result.

## 6. Final authority

The failure is at the Candidate-3 Frontier self-heal focused gate. The observed
readiness timeout prevents the self-heal path from being reached in the real-client
scenario. Investigating or changing that runtime behavior requires a new Candidate;
Candidate-3 is not relabeled or patched.

```text
Candidate-3: REJECTED
Next Candidate: Candidate-4 REQUIRED
Full Client Acceptance: NOT COMPLETE
Production Balance Promotion: HOLD
Project Acceptance: PENDING
Stable Publication: NOT AUTHORIZED
PR #14 Candidate-3 synchronization: NOT PERFORMED (focused gates did not all pass)
```

The required Phase 10B-C submission ZIP and external SHA-256 sidecar were created
from sanitized text-only evidence. They contain no Candidate JAR, dependency JAR,
world, database dump, player data, MVI profile, secret, raw UUID, full log, or cache.

## 7. Submission artifact

```text
File: PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_SUBMISSION.zip
Size: 4955 bytes
SHA-256: 293f59881f244a565bf9385ea2d6b390ae4d86f41c9d4886758e2406990c13dc
Sidecar: PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_SUBMISSION.zip.sha256
```
