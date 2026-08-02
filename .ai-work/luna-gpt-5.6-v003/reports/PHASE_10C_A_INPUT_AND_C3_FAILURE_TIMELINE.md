# Phase 10C-A — Phase 10B-C Input Verification and Candidate-3 Failure Timeline

Date: 2026-08-03 JST  
Source work root: `.ai-work/luna-gpt-5.6-v002/` (historical, unchanged)  
New work root: `.ai-work/luna-gpt-5.6-v003/`

## 1. Required input identities

The requested v003 formal-instruction path was absent at start. Section 0 onward was therefore
executed from the preserved v002 formal instruction below; this authority substitution is
reported and not silently inferred as a v003 file.

```text
Formal instruction:
  .ai-work/luna-gpt-5.6-v002/add_instructions/Luna_Max_Phase_10C_A_Candidate_4_Owner_Bind_and_Readiness_Instructions_Revision_B.md
  size=52763
  sha256=b40cf5ed90cd1dec5ffb82f8371d94977581e3b0a8bc313ee3000535283bcf2e
```

The four required Phase 10B-C inputs were copied read-only into
`.ai-work/luna-gpt-5.6-v003/review-input/phase-10c-a/`. The source V002 files were not modified.

| Input | Size | SHA-256 | Verification |
|---|---:|---|---|
| `PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_SUBMISSION.zip` | 4955 | `293f59881f244a565bf9385ea2d6b390ae4d86f41c9d4886758e2406990c13dc` | size/hash match; ZIP integrity PASS |
| `PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_SUBMISSION.zip.sha256` | 139 | `b0171c403907ebf659cb642404430ad9547e4548c2fb8c29c1e3e28406f5e835` | size/hash match; target filename and ZIP hash match |
| `PHASE_10B_C_CANDIDATE_3_FORMALIZATION_AND_FOCUSED_RETEST_RESULT.md` | 9032 | `5a676a9fb497dc441abe21d454785586692786c355b90e88cd5356e597d09a99` | size/hash match |
| `PHASE_10B_C_CANDIDATE_3_INDEPENDENT_REVIEW_REVISION_B.md` | 6708 | `e661025c9ab27b3110dd094d94fd99869a26ca87cad6bba711b08a444f28216b` | size/hash match |

The ZIP contains the expected sanitized text-only evidence set. Its Candidate checksum file has
five checksum lines; every checksum is exactly 64 lowercase hexadecimal characters. The unchanged
Core authority is:

```text
b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2
```

## 2. Candidate-3 source, CI, headless, and reproducibility authority

The verified Candidate-3 source chain is:

```text
5b939ae055bd60fe3ad1bd01ba115c2ae674830d
  -> 1aa01ca953cc6630a45382f9ad325d4db911d222
  -> 25200ad4745fdaae79c761a56083f6e648e9a06b
```

Candidate-3 product HEAD is `25200ad4745fdaae79c761a56083f6e648e9a06b` on
`feature/V0.0.2-main-frontier`. The retained authority reports:

```text
Normal CI: run 30749708887, exact product HEAD, success
Pre-client Headless Runtime: run 30749708891, exact product HEAD, success
```

Two clean builds were reported byte-identical for Main and Frontier:

```text
wayfarer-main-0.0.2-SNAPSHOT.jar
  size=4678598
  sha256=7d17fbeedec09d8742bf8c9adde0bef2eec6adac5b07d5fb73021234a13e2da3

wayfarer-frontier-0.0.2-SNAPSHOT.jar
  size=4703856
  sha256=676168d135852cad1b45c147cedb916a3d6ed1baddb1b8ad8bcb7c65ba282a9c

Wayfarer_Core-V0.0.1.jar
  size=11751447
  sha256=b045581d3984dddba10ed7b2ada435926b8538ba9b29a1151550ce59588395a2
```

## 3. Candidate-3 focused observations

Facts from the complete report and sanitized evidence:

- Main clean delivery, reconnect stability, and the executed old-spec inventory observations passed.
- Candidate-3 still blocked ordinary Container insertion and manual Drop; the Owner later replaced
  those physical-transfer rules with ALLOW.
- Frontier initial exact-world delivery produced one Elytra, one Grappling Hook, one Navigation
  compass, and two Launchpads.
- Frontier reconnect and backend restart/reconnect did not create a permanent duplicate in the
  nominal scenarios; pending-delivery attempts remained one.
- The exact-current Elytra, Grappling Hook, and Navigation items were copied through the verified
  vanilla Item Copy route. The extra copies remained after controlled reconnect.
- The mandatory self-heal gate stopped at bounded Safe Entry readiness timeout before the cleanup
  operation. No duplicate-self-heal event was observed.
- R2 was discarded as proof because its fixture lacked the Wayfarer Grappling Hook tier and the
  operator died during the planned sequence. R3 used the corrected fixture and the same
  Candidate-3 artifact hashes, then reproduced the failure.

## 4. Retained readiness chronology

The retained R2/R3 Paper logs expose event-level timing, but not individual poll records.
Timestamps below are the timestamps present in the retained log lines; `generation` and
`requiredManagedItems` are included exactly where logged.

| Timestamp | Source | Generation | Poll number | Visible managed count | Required managed count | Fingerprint | Decision |
|---|---|---:|---:|---:|---:|---|---|
| 22:58:31 (C3R2) | `MVI_PUBLIC_EVENT` stabilization begin; later `FINGERPRINT_STABLE` delivery | 2 | not logged | 0 at visibility/delivery event | 0 | not logged | stabilized / delivery began |
| 23:09:48 (C3R2/R2) | `MVI_PUBLIC_EVENT` stabilization begin; later `FINGERPRINT_STABLE` delivery | 2 | not logged | 0 at visibility/delivery event | 0 | not logged | stabilized / delivery began |
| 23:12:58 (C3R2/R2) | `BOUNDED_FINGERPRINT` stabilization begin | 1 | not logged | not logged | 3 | not logged | waiting |
| 23:12:59 (C3R2/R2) | bounded readiness timeout | 1 | not logged | not logged | 3 | not logged | `TIMEOUT` |
| 23:13:51 (C3R2/R2) | `BOUNDED_FINGERPRINT` stabilization begin | 1 | not logged | not logged | 3 | not logged | waiting |
| 23:13:52 (C3R2/R2) | bounded readiness timeout | 1 | not logged | not logged | 3 | not logged | `TIMEOUT` |
| 23:27:02 (C3R2/R3) | `BOUNDED_FINGERPRINT` stabilization begin | 1 | not logged | not logged | 3 | not logged | waiting |
| 23:27:03 (C3R2/R3) | bounded readiness timeout | 1 | not logged | not logged | 3 | not logged | `TIMEOUT` |
| 23:29:10 (C3R2/R3) | `BOUNDED_FINGERPRINT` stabilization begin | 1 | not logged | not logged | 3 | not logged | waiting |
| 23:29:11 (C3R2/R3) | bounded readiness timeout | 1 | not logged | not logged | 3 | not logged | `TIMEOUT` |

The retained logs do not contain poll-by-poll `pollCount`, visible count, or fingerprint values for
the failing bounded runs. They also do not record the first timestamp at which the restored copied
items became visible. Therefore:

```text
EXACT_TIMING_CAUSE_UNRESOLVED
FIRST_RESTORED_COPIED_ITEM_VISIBILITY: NOT_RECORDED
```

No timing evidence is invented here.

## 5. Facts versus inference

Facts:

- Candidate-3's exact-current duplicate self-heal focused scenario failed.
- The readiness phase logged a bounded-fingerprint begin and then a timeout before self-heal.
- The exact cleanup event was absent from the retained sanitized evidence.
- Candidate-3 source and artifacts were not patched after the failure.

Bounded inference:

- Because cleanup is invoked after Safe Entry, the observed timeout prevented the cleanup path from
  being reached in this run.
- The evidence does not independently disprove the exact-current cleanup algorithm; it establishes
  the mandatory focused gate failure and requires the finite, instrumented Candidate-4 remediation.

This timeline is written before Candidate-4 source mutation. Candidate-1, Candidate-2, and
Candidate-3 evidence remains preserved and rejected according to the corrected independent review.
