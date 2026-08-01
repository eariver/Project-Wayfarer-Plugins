# Project Wayfarer V0.0.2 Phase 04 — Main Reissue Domain／Persistence Result

## 1. Verdict

`PHASE 04: PASS`

`PHASE 05 GATE: GO`

初回自己Reportは`PASS`としていたが、独立Reviewで未接続のpaid `UNKNOWN` rotation recoveryをBuild Blockerとして指摘され、補正開始時の判定は`CHANGES_REQUIRED`として扱った。Blockerを修正し、追加Test、MariaDB CAS検証、local validation、normal push、最終HEADのCI／Pre-client Headlessを完了したため、現時点の最終Verdictは`PASS`である。

## 2. Model／Reasoning

- Model: `GPT-5.6 Luna`
- Reasoning: `Max`
- 実装判断はPrimary Authority、Phase 04 Instruction、Phase 03 `FINAL-3`を優先し、棄却済み文書およびOperational Evaluation Reportは使用していない。
- Phase 04の範囲に限定し、PlayerDeathEvent、Command Tree、Bukkit Inventory、Physical Delivery、Player通知、Runtime Wiring、`plugin.yml`は変更していない。

## 3. Starting HEAD

- Repository: `eariver/Project-Wayfarer-Plugins`
- Branch: `feature/V0.0.2-main-frontier`
- Starting HEAD: `ffa784f183b9d865114999a5ec344ec644d5e433`
- 開始時 `git status --short`: clean
- PR: `#14`、Open／Draft／Unmergedを維持

AuthorityおよびPhase 04 Instructionの指定SHA-256は開始時に検証し、いずれも一致した。

## 4. Implementation HEAD

- Implementation／validation tree HEAD: `016c230e6dda9fdf8f40885fde3d0eec1b88aba2`
- Report Parent HEAD: `ac608ade15ea24d417a71ad12fe8b574de945ead`
- Report自身のCommit SHAは本文へ埋め込まず、自己参照するReport-only Commitを作らない。

## 5. Changed Files

Phase 04 scope内の実効変更は次のとおり。

- Application: `ConfirmRequest`, `DeliveryOutcome`, `PhysicalItemPresence`, `PrepareOutcome`, `PrepareResult`, `QuoteRequest`, `ReissueCoordinator`, `ReissueDeliveryGateway`, `ReissueEligibilityPort`, `ReissueEligibilitySnapshot`, `ReissueOperationRepository`, `ReissueQuote`, `ReissueQuoteStore`
- Domain: `GrowthTool`, `ReissueOperation`, `ReissuePricing`
- Persistence: `JdbcRepairOperationRepository`, `JdbcReissueOperationRepository`
- Migration: `V004__growth_tool_reissue_operation.sql`
- Migration tests: `MainMigrationHashTest`, `MainMigrationIntegrationTest`, `ReissueOperationIntegrationTest`
- Unit tests: `ReissueCoordinatorTest`, `ReissueQuoteStoreTest`, `ReissueOperationTest`, `ReissuePricingTest`
- Correction tests: `ReissueCoordinatorTest`へUNKNOWN rotation／replay／failure／eligibility／recovery Scenarioを追加し、`ReissueOperationIntegrationTest`へpayment marker付きUNKNOWN reopen CASを追加
- Failure fixture: `V004__broken.sql`を内容不変のまま`V005__broken.sql`へrename
- Headless validation expectation: `scripts/runtime/preclient/run-headless-paper.sh`のMain専用history count `4`→`5`
- Housekeeping: `.ai-work/grok-4.5-opencode-v002/`の追跡済み3ファイルをlocal file保持のままIndexから除外。`.git/info/exclude`、`.gitignore`、正式Report／Authorityは変更していない。

V001～V003、Core、Frontier、Runtime本体、Command、Permission、`plugin.yml`、Raw ItemStack永続化、Dedicated Reissue Tableは変更していない。

## 6. Architecture Inputs

読了・SHA-256検証済み。

- `Project_Wayfarer_Plugin_V0.0.2_PR14_Final_Pre_Client_Codex_Instructions_AUDITED.md`
  - `6ADD6659937166FD90A970DB6FAA26C02F30A2E6B23557508F8F3BA93349B092`
- `05_BUILD_PHASE_04_MAIN_REISSUE_DOMAIN_PERSISTENCE.md`
  - `815B9AE97B83A1D73ECD7EF34790D6ABBF53C3C88540686F8C3D6A0F72086093`
- `PHASE_03_MAIN_REISSUE_ARCHITECTURE_RESULT.md`
  - `f1deafb296df54ad3b05aceecae08e37ed2ca957a821e772399ca9560c946dd2`
- `PHASE_03_MAIN_REISSUE_ARCHITECTURE_FINAL_RESULT.md`
  - `ec86eba8d239e7573904b319b797d8d3ab188f0e1f6bb8b800b6180436a76de8`
- `PHASE_03_MAIN_REISSUE_ARCHITECTURE_FINAL-2_RESULT.md`
  - `e7736fd3a149848b555aa825b2a07750cf94712efe8f2f70348e5b80a9d3660e`
- `PHASE_03_MAIN_REISSUE_ARCHITECTURE_FINAL-3_RESULT.md`
  - `9b5ccd82ce93ac98468c52b4ff77525e770591e9700f8f33c8bbf1451961da90`

`FINAL-3`を最終Contractとして適用した。

## 7. Migration

`V004__growth_tool_reissue_operation.sql`をshared `wf_main_repair_operation`へadditive適用した。

- 物理Primary Keyは既存どおり`repair_id`。`reissue_id`列は追加していない。
- 追加列: `operation_kind`, `config_revision`, `evolution_count`, `expected_item_instance_id`, `new_item_instance_id`, `payment_committed_at`, `active_guard`
- `REPAIR`／`REISSUE` kind、kind別state、snapshot、evolution、paid/unpaid payment marker、transaction ID、active guardのCHECKを追加。
- `REPAIR`は`active_guard IS NULL`、REISSUE active stateは`active_guard=tool_id`、terminal stateは`active_guard IS NULL`。
- `active_guard` unique indexおよびrecovery検索用indexを追加。
- V004 SHA-256: `8f3b609842da5dd2fa4fb72222f78b91f008a29da745f3986004d327dc0bc156`
- V001～V003のSHA-256は不変。
- Failure fixtureは`V005__broken.sql`へrenameし、blob内容は不変。

MariaDBでNULLを含むCHECK評価も確認し、guard equalityがNULLで抜けないようにV004 CHECKをfail-closedにした。

## 8. Domain／Repository／Coordinator

- `ReissuePricing`は既存`RepairPricing`を再利用し、価格式を再実装していない。`reissueCost = brokenRepairCost + fullRepairCost`。
- `ReissueQuote`／`ReissueQuoteStore`はquoteのsingle-use、expiry、replacement、current item／epoch／config／delivery／amount bindingを保持する。
- `ReissueOperation`は`PREPARED`, `PAYMENT_PENDING`, `PAYMENT_COMMITTED`, `PENDING_DELIVERY`, `DELIVERED`, `FAILED`, `ABANDONED`, `UNKNOWN`とstate nullable invariantをDomain Constructorで検証する。
- `GrowthTool.reissued(UUID newItemInstanceId, Instant now)`は保存済みUUIDを採用し、`instance_epoch + 1`, `ACTIVE`, `stored_damage=0`, `delivery_status=PENDING`とする。progress、branch、tool identity、schema、logical historyは維持する。
- `JdbcRepairOperationRepository`の全SELECT／UPDATEを`operation_kind='REPAIR'`に限定した。
- `JdbcReissueOperationRepository`の全SELECT／UPDATEを`operation_kind='REISSUE'`に限定し、全SQLのPrimary Keyに`repair_id`を使用した。
- 必須repository operationとCAS、idempotency、active guard、transaction ID conflict、recovery scanを実装した。
- `ReissueCoordinator`はQuote、eligibility、prepare、payment、UNKNOWN、rotation checkpoint、pending delivery resultをBukkit非依存で合成する。
- `ReissueCoordinator.resumeRotationFromUnknown(UUID reissueId)`を追加した。`tasks.database`でReadし、`state=UNKNOWN`、transaction ID、payment markerを厳密確認後、`reopenToPaymentCommitted`をCASし、成功時だけ既存`resumeRotation`へ継続する。CAS敗北時は再ReadしてDurable State由来のResultを返す。
- 既にRotation済みの場合は保存済み`newItemInstanceId`／`instanceEpoch`を認識して`replaceAuthority`を再実行しない。成功後は`PENDING_DELIVERY`でguardを解放し、`failure_code`をクリアする。この経路は`transactions.execute()`を呼ばない。
- Authority rotationとGrowth Toolの`delivery_status=PENDING`を同一Growth Tool UPDATEで扱い、Phase 04ではphysical deliveryを実行しない。

## 9. UNKNOWN／Recovery Contract

- Core Resultが中間stateまたは`UNKNOWN`でも、non-null transaction IDを同一CASで保存する。
- 既存transaction IDと異なるIDは上書きしない。
- `UNKNOWN + transactionId=null + marker=null`だけがadmin `resume-payment`候補。
- transaction ID既知のUNKNOWNは`transactions.execute()`を再呼出しせず、`confirmPaymentAndResumeRotation`で`transactions.inspect()`を使う。
- Coreが`COMMITTED`または`RECONCILED_COMMITTED`の場合だけ`confirmPaymentCommittedFromUnknown`をCASし、`PAYMENT_COMMITTED`からrotationを再開する。
- Core `FAILED`またはinspect failureでは行を不変にし、rotationしない。
- `reopenPayment`は`transaction_id IS NULL AND payment_committed_at IS NULL`を必須条件とする。
- `reopenToPaymentCommitted`は`UNKNOWN`、non-null transaction ID、non-null `payment_committed_at`、expected lock versionを必須とし、CAS成功時に`failure_code=NULL`とする。marker null／transaction ID null／lock version不一致は失敗する。
- 支払marker付きUNKNOWNの再開は新しい明示Admin `resumeRotationFromUnknown`だけが到達し、`resumePayment`およびRestart自動Recoveryからは到達しない。
- Restart時の孤立`PREPARED`だけをCASで`ABANDONED`へ遷移し、`active_guard`を解放する。`PAYMENT_PENDING`以降はabandonしない。
- `recoverAfterRestart()`のDB scan failureは`0件成功`へ変換せず、failed stageとして伝播する。`PAYMENT_COMMITTED` candidateは`PENDING`／`DELIVERED`だけをrecovered countへ加算し、`UNKNOWN`／`UNAVAILABLE`は加算しない。
- `PENDING_DELIVERY`はterminal business resultとしてguardを解放し、`GrowthToolDeliveryCoordinator`によるPhase 05以降の再配達へ渡す。

## 10. Unit Test

実行Command:

```text
./gradlew --no-daemon --console=plain --rerun-tasks :plugins:wayfarer-main:test
```

結果: `BUILD SUCCESSFUL`、Main Unit Test total 74 tests、failures=0、errors=0、skipped=0。新規Reissue Testは`ReissueCoordinatorTest` 25 tests、うち今回追加19 tests。

主な検証対象:

- Pricing `e=0,3,10,28`、overflow、ordering、invalid config fail-closed
- Quote single-use、expiry、replacement、current／pending／REVOKED／in-flight reject
- Double Confirm、same quote replay、Debit FAILED、Core UNKNOWN／intermediate state
- transaction ID保存、conflict、PREPARED abandon、paid-only rotation
- already-rotated resume、rotation once、fixed UUID、epoch、ACTIVE、damage、progress、branch、PENDING_DELIVERY
- `confirmPaymentAndResumeRotation`、`COMMITTED`／`RECONCILED_COMMITTED`、FAILED、inspect failure、`executeCalls == 0`
- double admin execution、Domain state invariant

今回追加した`ReissueCoordinatorTest`の正確なScenario:

- `currentPhysicalItemRejectsBeforeOperationOrDebit`
- `pendingDeliveryRejectsQuote`
- `revokedToolRejectsQuote`
- `existingActiveOperationRejectsNewQuoteAsInFlight`
- `doubleConfirmDebitsOnce`
- `sameQuoteReplayCreatesOneOperationAndDebitsOnce`
- `failedDebitTransitionsFailedAndReleasesGuard`
- `intermediateDebitStateStoresTransactionAsUnknown`
- `differentInspectTransactionDoesNotOverwriteExistingTransactionId`
- `inspectFailureLeavesModuleRowUnchanged`
- `reconciledCommittedResumesRotationWithoutExecute`
- `transactionlessUnknownIsTheOnlyResumePaymentPath`
- `knownTransactionUnknownCannotResumePayment`
- `paymentMarkedUnknownUsesOnlyExplicitRotationResumeAndRecoversCrashWindow`
- `markerlessUnknownCannotUseRotationResume`
- `rotationResumeCasLossReturnsDurableState`
- `restartRecoveryDoesNotAutoResumeUnknown`
- `restartRecoveryDoesNotCountUnknownPaymentAsRecovered`
- `restartRecoveryScanFailureIsNotReportedAsZero`

Crash Window Testでは、Rotation成功、pendingDelivery checkpoint失敗、UNKNOWNへの遷移、transaction／marker／guard保持、Admin `resumeRotationFromUnknown`、reopen CAS、Already-Rotated認識、checkpoint、guard解放、二回目Adminのno-opを実際に通過させた。

## 11. MariaDB Test

実行Command:

```text
./gradlew --no-daemon --console=plain --rerun-tasks :plugins:wayfarer-main:mariaDbIntegrationTest
```

結果: `BUILD SUCCESSFUL`、MariaDB Test total 9 tests、failures=0、errors=0、skipped=0。新規Reissue persistence Testは`ReissueOperationIntegrationTest` 4 tests。

検証対象:

- V004 apply、column／index／CHECK、V001→V004 upgrade、repeated migrate 0件
- Existing Repair round-trip、Repair／Reissue kind分離、Reissue round-trip、idempotency replay
- active guard競合、transaction ID同一CAS、異transaction ID非上書き、payment marker round-trip
- `confirmPaymentCommittedFromUnknown`、PREPARED abandon、recovery scan、restart simulation
- `paymentCommittedUnknownCanBeReopenedOnlyWithPaymentEvidence`によるUNKNOWN→`reopenToPaymentCommitted` CAS、payment marker／transaction ID保持、`failure_code`消去、guard保持、lock／marker／transaction null拒否
- State／Guard CHECK、Paid State CHECK、V005 broken migration failure、V001～V003 hash不変

`MainMigrationHashTest`および`MainMigrationIntegrationTest`を含むMain `check`も実行し、成功した。

## 12. Scope Check

- 開始前およびCommit前に変更File一覧を確認した。
- 実効差分はMain Phase 04、migration tests／fixture、Headless migration expectation、および依頼されたGrok workspace housekeepingに限定された。
- Core Public API、Frontier、Runtime wiring、Command、Bukkit gameplay、Permission、V001～V003は変更していない。
- Grok workspaceの追跡解除後、`git ls-files -- .ai-work/grok-4.5-opencode-v002`は空。local filesは保持し、`.git/info/exclude`は変更していない。
- `git diff --check`: clean
- Report更新前のworking tree: clean。Report correction commit後も最終statusを再確認する。

## 13. Known Limitations

- Phase 04ではphysical item生成／delivery、Bukkit event、Command、player notification、runtime wiringを提供しない。これはPhase 05境界である。
- `TransactionDetails.payloadJson`は現行Core APIで提供されないため、利用可能なtransaction fieldsを一致確認し、未提供payloadは確認対象外とした。
- Headless evidenceの`minecraft_client_acceptance=pending`はPhase 05以降のclient acceptanceであり、Phase 04のBukkit非依存実装範囲外である。
- Restart scan failureは安全側にexceptionとして伝播するため、呼び出し側はfailed stageを監視する必要がある。

## 14. Phase 05 API

提供したBukkit非依存Port／Result:

- `ReissueEligibilityPort`
- `ReissueEligibilitySnapshot`
- `ReissueDeliveryGateway`
- `PhysicalItemPresence`
- `DeliveryOutcome`
- `ConfirmRequest`
- `QuoteRequest`
- `PrepareOutcome`
- `PrepareResult`
- `ReissueCoordinator.confirmPaymentAndResumeRotation(UUID reissueId)`
- `ReissueCoordinator.resumeRotationFromUnknown(UUID reissueId)`

Physical deliveryおよびBukkit runtime wiringは未実行である。

## 15. Commit

- `3dc0a7d679460eb83246b43ec60812ed76fae6a4` — `feat(main): add durable paid reissue domain`
- `7c3b8ed63303621b980005a4f99801e7b1cb07df` — `test(preclient): track V004 migration`
- `d00d6a0b747ae4aaa95018ad3f0d791eadb04f32` — `test(preclient): correct V004 history assertion`
- `1d8f113fc167190dab7a1290ccc5b12817e38a66` — `docs(test): record phase 04 reissue validation` (初回Report)
- `10ccb9deb6653de41e8d60b938991e7d2001b803` — `chore(repo): untrack accidental grok workspace` (Housekeeping Commit)
- `4d1ffeb0e65b8fbce9895282dd709ee099a7825f` — `fix(main): restore paid reissue rotation recovery` (Correction Commit)
- `016c230e6dda9fdf8f40885fde3d0eec1b88aba2` — `test(main): cover reissue failure and replay paths`
- Amend、rebase、merge、force push、tag、releaseは行っていない。

## 16. Push

- Remote: `origin`
- Branch: `feature/V0.0.2-main-frontier`
- 最終Implementation HEAD `016c230e6dda9fdf8f40885fde3d0eec1b88aba2`を通常Push済み。
- PR `#14`はOpen／Draft／Unmergedのまま維持した。

## 17. CI／Headless

最終Implementation HEAD `016c230e6dda9fdf8f40885fde3d0eec1b88aba2`で確認した。

- CI: SUCCESS
  - Run: `30692759099`
  - URL: `https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30692759099`
- Pre-client Headless Runtime: SUCCESS
  - Run: `30692759108`
  - URL: `https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30692759108`
  - Artifact: `preclient-headless-evidence`
  - Evidence SHA-256: `5d211e0445e022a9a470e573d83c0a2bdde17f403db8d550e9b1f2853a520f0f`
  - Paper: `1.21.11-build-132`
  - `result=PASS`
  - `module_runtime_wiring=pass`
- `project_runtime_changed=no`

初回Headless実行で旧Core migration assertionにより失敗したが、V004で増加するMain専用history countだけを更新し、Core `3`／`003`期待値を維持する修正後に再実行して成功した。独立ReviewのRecovery Blocker補正後も最終HEADでCI／HeadlessともSUCCESSとなった。

最終Report Commitを含むReport Parent HEAD `ac608ade15ea24d417a71ad12fe8b574de945ead`でも再確認した。

- Final CI: SUCCESS — Run `30693120041` — `https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30693120041`
- Final Pre-client Headless Runtime: SUCCESS — Run `30693120024` — `https://github.com/eariver/Project-Wayfarer-Plugins/actions/runs/30693120024`
- Final Headless Evidence SHA-256: `16a1c7ead2d894ce0ac9f161394a2cdd0b3273b9a6c9210188a15238cd47ee2e`
