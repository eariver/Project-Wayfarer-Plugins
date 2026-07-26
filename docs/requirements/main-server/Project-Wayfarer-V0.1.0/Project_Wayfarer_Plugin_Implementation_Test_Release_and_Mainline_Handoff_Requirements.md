# Project Wayfarer Plugins — Codex実装・試験・Release依頼書

## 0. 実行設定

```text
IDE:
  IntelliJ IDEA

Agent:
  Codex（JetBrains AI Assistant統合）

Model:
  GPT-5.6 Sol

Reasoning:
  High

Mode:
  通常のSource実装・Git操作:
    Agent

  Repository外の隔離Test Server構築・起動・停止:
    Agent (full access)
```

本Taskは、Project Wayfarer V0.1.0に必要な独自Plugin群のうち、最初の正式Plugin Releaseとして`Wayfarer_Core`を実装・試験・Releaseし、Project本流へHandoff可能な状態にするものである。

設計、Public API、Database Migration、Transaction、Idempotency、Threading、Fail-closed、Security、Test evidenceおよびRelease provenanceに関する判断が後続の`Wayfarer_Main`／`Wayfarer_Frontier`へ波及するため、初回作業はHigh reasoningで実施する。

---

## 1. 対象Repository

### Plugin Repository

```text
eariver/Project-Wayfarer-Plugins
```

### Project Repository

```text
eariver/Project_Wayfarer
```

Project Repositoryは参照専用とする。

禁止：

- Project RepositoryへのCommit
- Project RuntimeへのJAR配置
- Project実DatabaseへのMigration
- Project Runtime Config変更
- Project Secret設定
- Project Permission変更
- Project Server起動
- Project World／Player Data変更
- `versions.yml`／`plugin-manifest.yml`更新
- Roadmap完了判定

開始時に次を実行し、結果を作業報告へ記録する。

```powershell
git status --short
git branch --show-current
git rev-parse HEAD
git log -1 --oneline
git remote -v
```

Owner変更および既存Release Automationを保持すること。Force Push、History Rewrite、既存Tagの移動、既存Release Assetの上書きを禁止する。

---

## 2. Authority

矛盾時の優先順：

1. Project Ownerの明示判断が反映された最新Repository文書
2. Project Wayfarer現行`docs/`およびRuntime Lock
3. 現行Server／Theme Concept
4. 現行Plugin Concept
5. 本流から受領した実装・試験・Release・Handoff要求書
6. Plugin Repositoryの作業指示書兼設計仕様書
7. Plugin担当による詳細設計
8. 実装上の便宜

必ず確認するPlugin Repository文書：

```text
AGENTS.md
README.md
docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md
docs/operations/release-process.md
settings.gradle.kts
build.gradle.kts
gradle.properties
gradle/libs.versions.toml
.github/workflows/ci.yml
.github/workflows/prerelease.yml
.github/workflows/release.yml
```

必ず確認するProject Repository文書：

```text
concepts/plugins/Project_Wayfarer_Plugin_Concept_v0.0.3.md
concepts/plugins/main/Project_Wayfarer_Growth_Tool_Concept_v0.0.5.md
concepts/plugins/frontier/Project_Wayfarer_Worlds_Beyond_Plugin_Concept_v0.0.4.md
concepts/plugins/frontier/Project_Wayfarer_Ruined_Frontier_Integration_Decision_Concept_v0.0.2.md

docs/06-acceptance-tests.md
docs/09-roadmap.md
docs/10-waymark-economy.md
docs/12-permission-model.md
docs/14-frontier-v0.1.0-scope.md
docs/15-frontier-runtime-lock.md
versions.yml
plugin-manifest.yml
```

参照したProject Commit、文書Version、Blob SHAまたはCommit SHAをTest Reportへ記録する。

---

## 3. 受領要求書のRepository管理

本流から受領した次の要求書を正本SnapshotとしてPlugin Repositoryへ保存する。

```text
Project_Wayfarer_Plugin_Implementation_Test_Release_and_Mainline_Handoff_Requirements.md
```

配置：

```text
docs/requirements/main-server/Project-Wayfarer-V0.1.0/
├─ Project_Wayfarer_Plugin_Implementation_Test_Release_and_Mainline_Handoff_Requirements.md
├─ source.md
├─ assessment.md
└─ traceability.md
```

### `source.md`

最低限、次を記録する。

```markdown
# Requirement Source

- Requirement set: Project Wayfarer V0.1.0
- Source project: Project Wayfarer
- Source repository: eariver/Project_Wayfarer
- Project reference commit: 344eedc738d75954daa43facfeef302944f2963a
- Plugin repository baseline stated by source:
  74d7d6e70ebd0d9da9882d912d846c875d2558ac
- Plugin repository actual pre-execution HEAD: <actual SHA>
- Received date: <YYYY-MM-DD>
- Authority: Project mainline requirement
- Supersedes: None
```

### `assessment.md`

次を記録する。

- 要求の実装可能性
- 現在Repositoryとの差分
- 既存Release Automationへの必要変更
- Authority conflictの有無
- 未確定事項
- 停止／Escalation条件
- 初回Release Scope
- Test Server前提
- Project Runtime未変更境界

### `traceability.md`

要求を一意なIDへ分解し、最低でも次の列を持つ。

```markdown
| Requirement ID | Requirement | Implementation | Automated test | Pre-release | Runtime evidence | Status |
|---|---|---|---|---|---|---|
```

状態：

```text
Not started
In progress
Implemented
Automated test passed
Runtime test passed
Passed with limitation
Failed
Blocked
Not applicable
```

Codexは、実装・Test・Runtime evidenceのCommit参照がない要求を`Passed`にしない。

---

## 4. Version方針

Project Wayfarer自体が`V0.1.0`へ到達するまで、Plugin Releaseは`V0.0.x`系列を使用する。

初回安定版：

```text
V0.0.1
```

初回検証系列：

```text
V0.0.1-alpha.1
V0.0.1-alpha.2
V0.0.1-alpha.3
V0.0.1-alpha.4
V0.0.1-beta.1
V0.0.1-rc.1
V0.0.1
```

必要に応じてalpha／beta／rc番号を追加してよい。同一TagやRelease Assetを上書きしてはならない。

人間向けVersion、Git Tag、GitHub Release、文書名、Directory名およびJAR Filenameには大文字`V`を付ける。

```text
V0.0.1-alpha.1
V0.0.1
Wayfarer_Core-V0.0.1-alpha.1.jar
Wayfarer_Core-V0.0.1.jar
```

Gradleおよび`plugin.yml`内部Versionは先頭の`V`を外す。

```text
0.0.1-alpha.1
0.0.1
```

将来の安定版例：

```text
V0.0.2
V0.0.3
...
V0.0.99
V0.0.100
```

Project Wayfarer V0.1.0に必要な機能がすべて実装・試験・Handoffされ、本流側のVersion整合判断が成立した後に限り、Plugin側も`V0.1.0`系列へ移行する。

---

## 5. 初回Release Scope

初回安定版`V0.0.1`の正式Runtime Artifactは次だけとする。

```text
Wayfarer_Core
```

関連して実装してよいModule：

```text
libraries/wayfarer-api
libraries/wayfarer-common
libraries/wayfarer-testkit
plugins/wayfarer-core
```

Build成立に必要な最小Bootstrap／Stub／Dependency調整だけ許可するModule：

```text
plugins/wayfarer-main
plugins/wayfarer-frontier
integrations/wayfarer-leafgrapple-adapter
```

初回Releaseへ含めない：

```text
Wayfarer_Main
Wayfarer_Frontier
Wayfarer_Frontier_EliteMobsMVI
wayfarer-elitemobs-mvi-contract
```

条件付きAdapterは、Project側Decision Resultが`ADAPTER_REQUIRED`になるまで正式実装・Releaseしてはならない。

Artifact Matrixでは次の状態を明記する。

```markdown
| Artifact | Plugin-side implementation | Plugin-side test | Release | Project placement | Project acceptance | Roadmap Order |
|---|---|---|---|---|---|---|
| Wayfarer_Core | complete | passed | V0.0.1 | Main + Frontier | pending | 9 |
| Wayfarer_Main | not included | N/A | N/A | Main only | pending | 10 |
| Wayfarer_Frontier | not included | N/A | N/A | Frontier only | pending | 11 |
| Wayfarer_Frontier_EliteMobsMVI | not authorized / not included | N/A | N/A | Frontier only | pending | Decision Gate |
```

同一RepositoryにMain／Frontier Skeletonが存在しても、`V0.0.1` Release AssetへJARを含めない。

---

## 6. Release Workflow修正

実装開始前に、現在のRelease Workflowを初回Release方針へ適合させる。

対象：

```text
.github/workflows/prerelease.yml
.github/workflows/release.yml
docs/operations/release-process.md
```

必須変更：

1. 入力Versionを大文字`V`付きで受け付ける。
2. Git Tagは入力値そのものを使用する。
3. Gradle Versionは先頭`V`を外して渡す。
4. `release_scope`入力を追加する。
5. 初回系列では`release_scope=core`だけを許可する。
6. `core` ScopeではWayfarer_Core JARだけを収集・Releaseする。
7. Pre-releaseとStable ReleaseのScope一致をManifestで検証する。
8. Main／Frontier JARを初回Releaseへ含めない。
9. GitHub Environment承認を維持する。
10. Test evidenceおよび本流要求書の不変参照を維持する。
11. Source Commit、Artifact Hash、Config Version、Migration VersionをManifestへ記録する。
12. Release Assetを通常Git履歴へCommitしない。

推奨入力：

```yaml
release_scope:
  description: "Artifact set included in this release"
  required: true
  type: choice
  options:
    - core
    - core-main
    - core-frontier
    - all
  default: core
```

初回Release系列では、`core-main`、`core-frontier`、`all`を選んだ場合は明示的に失敗させてもよい。

Workflow修正だけを先行PRに分けてもよい。実装PRへ混在させる場合は、差分と検証結果を明確に分離して報告する。

---

## 7. 固定Architecture境界

### Runtime Plugin責務

```text
Wayfarer_Core:
  Main／Frontier共通Infrastructure
  Service Contract

Wayfarer_Main:
  Main only
  Growth Pickaxe

Wayfarer_Frontier:
  Frontier only
  Worlds Beyond／Frontier固有機能
```

### 依存方向

```text
Wayfarer_Main     → Wayfarer_Core API
Wayfarer_Frontier → Wayfarer_Core API

禁止:
Wayfarer_Core     → Wayfarer_Main
Wayfarer_Core     → Wayfarer_Frontier
Wayfarer_Main     ↔ Wayfarer_Frontier
循環依存
```

### Data Authority

```text
MariaDB:
  Wayfarer独自Pluginの永続Gameplay Data

Redis:
  Cache／Lock／PubSub／Message／Idempotency補助
  唯一の永続Authorityではない

MVI:
  Frontier Backend内の通常Player State

Waymark:
  正式Provider API／Vault境界

Minecraft／Paper:
  Runtime World／Entity／Item／Event
```

禁止：

- RedisEconomy内部Keyの直接操作
- MVI DBの直接更新
- mcMMO DBの直接更新
- EliteMobs DBの直接更新
- 通常InventoryのMariaDB保存
- MainとFrontier間のItem移送
- MVIによるMain／Frontier間Profile管理
- 外部Plugin内部APIの推測利用

### Threading

```text
Main Thread:
  Bukkit／Paper mutation

Async:
  JDBC
  Redis I/O
  Audit write
  Query
  Checkpoint
```

Main Thread同期DB／Redis I/Oを禁止する。

基本フロー：

```text
Main Thread snapshot
→ immutable request
→ async operation
→ immutable result
→ Main Thread再検証
→ Bukkit mutation
```

Bukkit可変ObjectをAsync処理へ保持しない。

---

## 8. Wayfarer_Core最低機能

### 8.1 Lifecycle／Failure

- Typed Config validation
- Environment Secret resolution
- MariaDB Pool
- Flyway validate／migrate
- Redis foundation／health
- Waymark Provider capability probe
- Bukkit ServicesManager registration
- Executor／Scheduler lifecycle
- Clean disable
- Timeout付きflush
- Partial initialization cleanup
- Fail-closed
- Disable後Callback拒否
- Migration失敗時の継続禁止
- Shutdown timeoutのAudit

Hot reload／PlugMan系Reloadは正式対応しなくてよい。

### 8.2 Public API

最低Contract：

```text
WayfarerServices
WayfarerDatabase
WayfarerAudit
WayfarerTransactions
WayfarerWaymark
WayfarerItemIdentity
WayfarerTasks
WayfarerHealth
```

同等以上のContractへ再編してよい。

Public APIは次を推測せず利用できること。

- Service lookup
- Thread Contract
- Completion／Failure
- Timeout
- Idempotency
- Disable
- Health
- Item Identity
- Transaction
- Waymark
- Audit
- Task bridge

Public APIへ次を漏らさない。

- Hikari実装
- Flyway実装
- Lettuce実装
- JDBC Connection
- Plugin実装Class
- Paper Event
- Player
- ItemStack
- Server固有Gameplay Domain

### 8.3 Config／Secret

最低：

- Config Version
- Server ID
- MariaDB接続
- Redis接続
- Migration
- Waymark Provider
- Executor
- Audit
- Health
- Shutdown Timeout
- Sanitized Sample

Secret値をGit、YAML Sample、Log、Audit、Exceptionへ出さない。

### 8.4 MariaDB／Migration

Core所有Prefix：

```text
wf_core_
```

最低Domain：

- Transaction
- Transaction Event／History
- Audit
- Player Identity
- 共通Item Identity Foundation
- Reconcile state

要求：

- Empty DB Migration
- Additive Migration
- 適用済みMigration書換え禁止
- UUID Authority
- UTC Timestamp
- Unique Constraint
- Optimistic Lock
- Index
- JSON Validation
- Restart Recovery
- Coreだけで`wf_main_*`／`wf_frontier_*`を作らない

### 8.5 Transaction／Waymark

最低State：

```text
PREPARED
DEBIT_PENDING
DEBITED
DOMAIN_COMMIT_PENDING
COMMITTED
REFUND_PENDING
REFUNDED
UNKNOWN
RECONCILED_COMMITTED
RECONCILED_REFUNDED
FAILED
```

必須：

- Unique Idempotency Key
- 二重Debit防止
- 二重Refund防止
- Crash Window検出
- Timeout
- Retry
- UNKNOWN
- Manual／Automatic Reconcile
- Provider Reference
- Audit
- Restart Recovery
- Insufficient funds
- Provider outage

外部Providerを含むため、無条件のExactly-onceを主張しない。

### 8.6 Audit

最低対象：

- Enable／Disable
- Migration
- Health transition
- Transaction
- Refund
- UNKNOWN
- Reconcile
- Admin action
- Identity failure
- Dependency outage
- Permission denial
- Shutdown timeout

Critical Eventを黙ってDropしない。SecretをRedactする。

### 8.7 Identity

Player：

- UUID Authority
- Last known nameは補助
- Authentication／Permission／Inventoryを再実装しない

Item identity最低Key：

```text
wayfarer:item_type
wayfarer:owner_uuid
wayfarer:instance_epoch
wayfarer:schema_version
wayfarer:item_instance_id
wayfarer:display_revision
```

Lore／Name／Materialだけで判定しない。

次をFail-closedにする。

- Unknown Schema
- Unknown Type
- Invalid UUID
- Owner mismatch
- Epoch mismatch

### 8.8 Redis

用途：

- Cache
- Lock foundation
- Pub/Sub／Invalidation foundation
- Message foundation
- Idempotency補助

MariaDB Authorityを維持し、Redis停止時の縮退／拒否範囲を文書化する。

### 8.9 Thread／Task

- Executor Queue
- Backpressure
- Shutdown
- Main Thread同期I/O検出Test
- Disable後Callback拒否
- Timeout
- Immutable request／result

### 8.10 Health／Admin

最低Health：

- Config
- MariaDB
- Migration
- Redis
- Waymark
- Audit
- Transaction
- Executor
- Services

最低Command：

```text
/wayfarer admin health
/wayfarer admin transaction inspect <id>
/wayfarer admin transaction reconcile <id>
```

Permission、Console可否、Redaction、Audit、Confirmationを文書化する。

---

## 9. Automated Test最低要求

実装したすべての機能について、最低でも次を実施する。

- Unit Test
- MariaDB Integration Test
- Redis Integration Test
- Migration Test
- Concurrency Test
- Idempotency Test
- Failure／Timeout Test
- Restart Recovery Test
- Config Validation Test
- Secret Redaction Test
- Packaging Test
- Public API Compatibility Test
- API Class Identity Test
- Main Thread DB／Redis I/O検出Test
- Disable後Callback拒否Test
- Duplicate Debit／Refund Test

Testcontainers等の隔離環境を使用する。

各PRで次を実行する。

```powershell
.\gradlew.bat --no-daemon check
.\gradlew.bat --no-daemon assemble
```

必要に応じてConfiguration Cache付きでも検証する。

```powershell
.\gradlew.bat --no-daemon --configuration-cache check
.\gradlew.bat --no-daemon --configuration-cache assemble
```

Compiler Warningを無断で許容しない。Failed／Skipped Testを隠さない。

---

## 10. プレリリース計画

プレリリースはCommit単位ではなく、隔離Test Serverで意味のある検証ができるVertical Slice単位で発行する。

### `V0.0.1-alpha.1` — Lifecycle Foundation

対象：

- Typed Config
- Secret resolution
- Lifecycle
- Partial initialization cleanup
- Services registration／unregister
- Executor lifecycle
- Clean disable
- Fail-closed
- Health foundation
- `/wayfarer admin health`

最低Runtime確認：

- Clean enable
- Clean disable
- Restart
- Invalid Config
- Missing Secret
- Service登録／解除
- Permission denial
- Secret redaction

### `V0.0.1-alpha.2` — Persistence and Identity

対象：

- MariaDB Pool
- Flyway validate／migrate
- `wf_core_*`
- Audit foundation
- Player identity
- Item identity
- Restart recovery foundation

最低Runtime確認：

- Empty DB Migration
- Additive Migration
- DB outage
- Migration failure
- Restart
- Invalid item identity
- Owner／Epoch mismatch
- Audit persistence

### `V0.0.1-alpha.3` — Redis and Task Boundary

対象：

- Redis foundation
- Redis health／reconnect
- Cache／lock／message foundation
- Task bridge
- Queue／backpressure
- Disable後Callback拒否
- Shutdown timeout

最低Runtime確認：

- Redis outage
- Redis reconnect
- Degraded／rejected operation
- Queue pressure
- Disable during async operation
- Main Thread I/Oなし
- Tick stallなし

### `V0.0.1-alpha.4` — Waymark Transactions

対象：

- Provider capability probe
- Transaction state machine
- Idempotency
- Debit
- Refund
- UNKNOWN
- Reconcile
- Provider reference
- Timeout／retry
- Admin inspect／reconcile
- Audit integration

最低Runtime確認：

- Balance
- Debit
- Refund
- Insufficient funds
- Provider outage
- Timeout
- Duplicate idempotency key
- 二重Debitなし
- 二重Refundなし
- UNKNOWN
- Reconcile
- Restart recovery

### `V0.0.1-beta.1` — Feature Complete

条件：

- Core最低機能実装済み
- Automated Test一式成功
- Packaging／API Class Identity成功
- Sanitized Config完成
- Command／Permission文書完成
- License／Third-party notice完成
- 重大な未実装なし

### `V0.0.1-rc.1` — Release Candidate

要求書のStandalone Test Server項目を一通り実施し、最終Test ReportとHandoff Packageを完成させる。

### `V0.0.1` — Stable

次の条件をすべて満たした場合だけ公開する。

- 全適用要求のtraceabilityが完了
- Automated Test Passed
- Standalone Test Server Passed
- Failed／Skipped／N/Aの理由記録済み
- Known Limitations記録済み
- Release Test Report Commit済み
- Handoff Package Commit済み
- Stable Sourceが承認済みRCのSource Commitと一致
- Project本流要求を満たした証跡が存在
- Userが`requirements_cleared=true`を明示承認
- `main-server-release` Environment承認

Codexは独断でStable Releaseを実行しない。

---

## 11. 隔離Test Server

### 11.1 配置

Test ServerはGit管理Repository外に作成する。

推奨：

```text
<Project parent>/
├─ Project-Wayfarer-Plugins/   # Git管理
└─ runtime/                    # Git管理しない
   └─ test-server/
      ├─ server.jar
      ├─ plugins/
      ├─ config/
      ├─ logs/
      ├─ world/
      ├─ world_nether/
      ├─ world_the_end/
      ├─ backups/
      └─ start-test-server.ps1
```

`runtime/test-server`に`.git`を作成しない。

禁止：

- Test Server Runtime DataのGit追跡
- WorldのCommit
- LogのCommit
- DB／Redis DataのCommit
- SecretのCommit
- External proprietary Plugin JARのCommit
- Release JARの通常Source履歴へのCommit

### 11.2 Baseline

```text
Paper:
  1.21.11

Java:
  25

MariaDB:
  isolated test instance

Redis:
  isolated test instance

Economy:
  Project同系統のWaymark Provider／Vault境界
```

Providerの正式APIまたはThread Contractを確認できない場合は推測せず停止し、Decisionを要求する。

### 11.3 Codexの責務

- Test Server Directory作成
- Paper取得
- EULA／起動設定準備
- Test専用MariaDB／Redis準備
- Release Asset Download
- SHA-256検証
- 旧Test Runtime Backup
- Plugin JAR配置
- Sanitized Test Config生成
- Server起動／停止
- Startup Log解析
- Error／Warning抽出
- Test evidence下書き
- 再現手順記録

### 11.4 Userの責務

- MinecraftクライアントでServerへ参加
- 指定Commandの実行
- Gameplay／Permission／UI／挙動の確認
- Expected／Actualの報告
- Pass／Failの最終確認
- Environment承認
- `requirements_cleared`の最終判断

CodexはUserが観測していないClient操作を`Passed`にしない。

---

## 12. Git管理するTest成果物

### Test Plan

```text
docs/testing/plans/V0.0.1-alpha.1.md
docs/testing/plans/V0.0.1-alpha.2.md
docs/testing/plans/V0.0.1-alpha.3.md
docs/testing/plans/V0.0.1-alpha.4.md
docs/testing/plans/V0.0.1-beta.1.md
docs/testing/plans/V0.0.1-rc.1.md
```

### Test Result

```text
docs/testing/results/V0.0.1-alpha.1.md
docs/testing/results/V0.0.1-alpha.2.md
docs/testing/results/V0.0.1-alpha.3.md
docs/testing/results/V0.0.1-alpha.4.md
docs/testing/results/V0.0.1-beta.1.md
docs/testing/results/V0.0.1-rc.1.md
```

各結果に最低限含める。

- Release Tag
- Release URL
- Source Commit
- Requirement ID
- Artifact Filename
- SHA-256
- Paper／Java
- MariaDB／Redis／Waymark構成
- Config Version
- Migration Version
- Test Case
- Expected
- Actual
- Evidence
- User observation
- Pass／Fail／Limited
- Known issue
- Next action
- Project Runtime未導入確認

Server Log全文やScreenshot Binaryを通常GitへCommitしない。必要な証跡は、Secretを除去した短い抜粋または再現可能な記述としてMarkdownへ記録する。

---

## 13. Release Test Report

推奨Path：

```text
docs/reports/Project_Wayfarer_Plugin_Release_Test_Report_V0.0.1_<YYYY-MM-DD>.md
```

最低内容：

1. Plugin Repository HEAD
2. Release Tag／URL
3. 対象Artifact
4. Artifact Version／Filename／SHA-256
5. Java／Gradle／Paper
6. MariaDB／Redis／Waymark構成
7. 参照Project文書とCommit／Blob SHA
8. 実装Scope
9. 追加実装Scope
10. Project方針適合
11. Build Command／結果
12. Unit Test Command／件数／結果
13. MariaDB Test
14. Redis Test
15. Migration Test
16. Standalone Test Server構成
17. Test CaseごとのExpected／Actual
18. Main Thread I/O確認
19. Failure Injection
20. Transaction／Refund／Reconcile
21. Restart／Disable／Reconnect
22. API Class Identity
23. Packaging
24. Permission
25. Secret Redaction
26. Performance／Tick impact
27. Known Limitations
28. Failed／Skipped／Not applicable
29. Open Decisions
30. Reproduction手順
31. Evidence Path
32. Project Runtime未導入確認

「すべて成功」だけで済ませず、実行内容と証跡を記録する。

---

## 14. Handoff Package

配置：

```text
docs/handoff/V0.0.1/
├─ release-readiness.md
├─ requirement-compliance.md
├─ artifact-matrix.md
├─ artifact-inventory.md
├─ sanitized-configuration.md
├─ command-and-permission-reference.md
├─ dependency-and-placement.md
├─ migration-and-compatibility.md
├─ upgrade-and-rollback.md
├─ known-limitations.md
└─ project-acceptance-input.md
```

本流へ渡すすべての文書成果物はGit管理する。

Binary Plugin ArtifactはGitHub Release Assetとして管理する。

最低Release Asset：

```text
Wayfarer_Core-V0.0.1.jar
SHA256SUMS.txt
RELEASE_MANIFEST.md
DEPENDENCY_VERSIONS.toml
TEST_SERVER_EVIDENCE.md
MAIN_SERVER_INSTRUCTION.md
Sanitized Config
Command／Permission一覧
Dependency一覧
License／Third-party Notice
Plugin Test Report
Known Limitations
Rollback／Removal手順
```

推奨：

- SBOM
- Source JAR
- Javadoc JAR
- Migration／Config compatibility matrix
- Upgrade notes

本流Handoffでは次を明示する。

- Release URL
- Release Tag
- Release Version
- Final Source Commit
- Artifact一覧
- JAR Filename／SHA-256
- Config Version
- Migration Version
- Sanitized Config
- Commands
- Permissions
- Dependencies
- Placement
- Test Report
- Known Limitations
- Open Decisions
- Upgrade／Rollback
- Runtime前提
- Environment Variables
- DB／Schema要件
- Redis要件
- Load order
- First migration behavior
- Failure behavior
- Health確認方法
- Smoke Test
- Backup／Restore対象
- Removal方法
- Downgrade可否
- Test Serverとの差分
- Project acceptance pending
- Roadmap Order 9 pending

本流へ渡す参照は、必ず不変なGit Commit SHAまたはGitHub Release Tagを使用する。

---

## 15. License／Supply Chain／Packaging

必須：

- Gradle Wrapper Version／Checksum確認
- Java 25
- Gradle 9.6.1
- Kotlin DSL
- Paper API 1.21.11-R0.1-SNAPSHOT
- Group／Package `io.github.eariver.wayfarer`
- Version Catalog
- Configuration Cache互換
- Reproducible Artifact設定
- Source CommitとArtifact Hash対応
- Runtime JARへの不要Test／Secret／Runtime Data非内包
- API Class Identity維持
- Main／Frontier JARへのAPI二重内包防止
- License／Third-party notice
- Bundled／Relocated Library一覧
- Packaging inspection

可能ならDependency Verification、SBOM、Source JARおよびJavadoc JARを提供する。

---

## 16. 停止／Escalation条件

次の場合は推測で進めず停止し、Project OwnerへDecisionを求める。

- Project正本とPlugin仕様でGameplay結果または責務境界が矛盾する。
- CoreからMain／Frontierへの依存が必要になる。
- MainとFrontierの相互依存が必要になる。
- 通常Inventory、MVI、mcMMO、EliteMobs、RedisEconomy内部Dataへ直接アクセスする必要がある。
- Waymark Providerの正式APIまたはThread Contractを確認できない。
- Main Threadを長時間Blockする以外にProvider操作を安全に行えない。
- Migrationの破壊的変更、適用済みMigration書換え、Data Lossまたは手動DB補正が必要になる。
- Public API互換性を破壊する必要がある。
- Secret、Runtime Data、World、DB Data、Logまたは外部Plugin ArtifactをCommitする必要がある。
- Project Runtimeへの配置・Migration・Server起動が必要になる。
- 条件未成立Adapterの作成が必要になる。
- Test Serverで重複課金、Data不整合、Main Thread同期I/O、停止不能または重大な未解決障害が発生する。

報告形式：

```text
Question
Current requirement
Proposed change
Reason
Alternatives
Gameplay impact
Data／Migration impact
Security impact
Compatibility impact
Rollback
Owner decision required
```

---

## 17. Git／PR運用

`main`へ直接Pushしない。

最初の準備Branch例：

```text
feature/V0.0.1-release-foundation
```

Core実装Branch例：

```text
feature/V0.0.1-core-lifecycle
feature/V0.0.1-core-persistence
feature/V0.0.1-core-redis-tasks
feature/V0.0.1-core-transactions
```

各Branchで：

1. `main`の最新化
2. 変更前HEAD記録
3. 実装
4. Narrow test
5. `check`
6. `assemble`
7. `git diff --check`
8. Secret／Binary／Runtime Data非追跡確認
9. Commit
10. Push
11. Draft PR
12. CI確認
13. Review
14. Merge

禁止：

- Force Push
- History Rewrite
- 既存Tag移動
- Release Asset上書き
- Secret Commit
- Runtime Data Commit
- JAR Commit
- Failed Testの隠蔽

---

## 18. Codexの初回作業

最初の作業では、まだ`V0.0.1-alpha.1`をReleaseしない。

次を実施する。

1. Pre-execution HEADを記録する。
2. 全Authority文書を読む。
3. 受領要求書をRepositoryへ保存する。
4. `source.md`を作成する。
5. `assessment.md`を作成する。
6. `traceability.md`へRequirement IDを作成する。
7. `docs/operations/release-process.md`を`V0.0.x`方針へ更新する。
8. `AGENTS.md`へRepository-managed handoff原則を追加する。
9. `prerelease.yml`／`release.yml`を大文字`V`＋`release_scope=core`へ対応させる。
10. `V0.0.1` Release Planを作成する。
11. Test Plan／Result／Handoff Directory Skeletonを作成する。
12. `check`と`assemble`を実行する。
13. 変更ファイル、Test結果、未解決事項、Authority conflictを報告する。
14. Draft PRを作成する。

初回準備PRのMerge後、`V0.0.1-alpha.1`向けLifecycle Foundation実装へ進む。

---

## 19. 各プレリリース実行前の承認

CodexはWorkflow実行前に次を表示し、Userの明示承認を待つ。

### Pre-release

```text
Repository
Workflow
Selected ref
Source commit
Release version
Release scope
Test instruction path
Expected artifacts
Environment
```

### Stable Release

追加で次を表示する。

```text
Approved pre-release tag
Approved source commit
Test evidence path／commit
Main-server requirement path／commit
Requirement traceability result
Known limitations
Open decisions
requirements_cleared value
```

Codexは`requirements_cleared=true`を独断で設定しない。

---

## 20. 完了報告

最終報告では最低でも次を記録する。

1. Recommended Sol
2. Pre-execution HEAD
3. Final Source Commit
4. Release Tag／URL
5. Release Version
6. 実装Artifact
7. 追加実装
8. Project方針適合
9. Artifact Filename／SHA-256
10. Config Version
11. Migration Version
12. Public API
13. Module／Dependency
14. Packaging／Relocation
15. Commands
16. Permissions
17. Database Schema
18. Transaction保証
19. Waymark Provider方式
20. Redis用途
21. Health
22. Threading
23. Build結果
24. Automated Test結果
25. Standalone Test Server結果
26. Main Thread I/O結果
27. Failure Injection結果
28. Restart／Disable／Reconnect結果
29. API Class Identity
30. Performance／Tick影響
31. Secret Redaction
32. Known Limitations
33. Failed／Skipped Test
34. Open Decisions
35. Test Report Path
36. Artifact Matrix
37. Project受入Input
38. Runtime非変更
39. Secret／Runtime Data非追跡
40. Final Git status
41. Project Ownerへ渡すGit Commit／Release URL／File一覧

---

## 21. 完了後の期待状態

```text
Plugin Repository Foundation:
  complete

Wayfarer_Core:
  plugin-side implementation complete
  plugin-side automated test complete
  plugin-side standalone test complete
  V0.0.1 released

Wayfarer_Main:
  not included

Wayfarer_Frontier:
  not included

Conditional Adapter:
  not authorized / not included

Project Runtime integration:
  not performed

Project acceptance:
  pending

Project Roadmap Order 9:
  plugin-side prerequisites complete
  Project integration／acceptance pending

Project Wayfarer V0.1.0:
  incomplete
```
