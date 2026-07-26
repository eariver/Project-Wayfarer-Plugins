# Project Wayfarer Plugin担当向け
# Roadmap準拠 Plugin実装・試験・Release・本流Handoff要求書

## 1. 推奨Sol

```text
high
```

### 理由

本Taskは、Project Wayfarer V0.1.0へ向けた独自Plugin実装をPlugin担当へ委ねつつ、Project本流が後続の統合・受入Test・Roadmap完了判定に必要とする成果物を漏れなく確保するための要求定義である。

Plugin担当には、設計、Module分割、内部Architecture、実装順序、追加機能、Release単位および試験方法について広い裁量を与える。

一方、次はProject Wayfarer全体へ影響するため、現行方針・正本・Decision Gateを厳守する。

- Runtime Pluginの責務境界
- Main／Frontierの依存方向
- MariaDB／Redis／MVI／WaymarkのAuthority
- Main Thread／Async Thread境界
- Cross-backend Item／Player State隔離
- Database Migration
- Public API互換性
- Security／Permission／Secret
- EliteMobs–MVI Adapter Decision Gate
- Project Runtimeへの導入権限
- Roadmap上の完了判定

---

## 2. 基本方針

### 2.1 Plugin担当へ委ねるもの

Project Wayfarerの現行方針、責務境界、AcceptanceおよびDecision Gateに反しない限り、次はPlugin担当の裁量とする。

```text
内部Architecture
Class／Package構成
Module追加／統合
Design Pattern
Library選択
Build改善
Test Framework
Test Consumer
CI改善
Static Analysis
Formatting
Performance最適化
Observability
追加のAdmin／Debug補助
Releaseの分割／統合
実装順序
追加Document
追加Test
追加Artifact
```

Plugin担当は、Project本流の要求を最低条件として扱い、より高品質な実装・試験・文書・Releaseを提供してよい。

### 2.2 追加実装の許容

必要条件を満たす限り、今回のReleaseへ次を含めてもよい。

```text
Wayfarer_Core
Wayfarer_Main
Wayfarer_Frontier
Growth Pickaxe
Worlds Beyond独自機能
Launchpad
Waystone
Frontier WM Shop
LeafGrapple Adapter
将来拡張を阻害しない共通Library／Testkit
```

追加したArtifact／機能ごとに、後述のRelease成果物とPlugin側Test結果を提出すること。

追加実装が存在しても、Project本流のRoadmap Orderは自動的に完了しない。

```text
Order 9:
  Wayfarer_CoreのProject側受入後に完了判定

Order 10:
  Wayfarer_Main／Growth PickaxeのProject側受入後に完了判定

Order 11:
  Wayfarer_FrontierのProject側受入後に完了判定
```

同一ReleaseにCore／Main／Frontierが含まれても、Project本流ではArtifactごと、Orderごとに分離して受入判定する。

### 2.3 条件付きArtifact

次は、Project側Decisionが成立する前に正式Artifactとして実装・Releaseしてはならない。

```text
Wayfarer_Frontier_EliteMobsMVI
wayfarer-elitemobs-mvi-contract
```

必要条件：

```text
Order 13 Decision Result:
  ADAPTER_REQUIRED
```

順序：

```text
固定Worldの静的MVI登録
→ 承認済みBlueprintに限定した厳密Regex
→ 前二者で安全に成立しない場合だけAdapter
```

実験用Branch、調査用Prototypeまたは捨てる前提のSpikeを作る場合も、正式Release Artifact、Runtime配置候補または既定Architectureとして扱わない。

---

## 3. 本流が要求する最低成果

Plugin担当の実装範囲にかかわらず、今回最低限必要な成果は次である。

```text
Plugin Repository Foundation
Wayfarer_Coreの実装
Wayfarer_CoreのPlugin側Test
Wayfarer_Coreの単体Test Server試験
Wayfarer_Coreの非SNAPSHOT Release／Pre-release
Project本流向けHandoff
```

追加でWayfarer_Main／Wayfarer_Frontier等を実装した場合は、それらについても同等のRelease成果とTest evidenceを要求する。

---

## 4. 対象Repository

### Plugin Repository

```text
eariver/Project-Wayfarer-Plugins
```

基準Commit：

```text
74d7d6e70ebd0d9da9882d912d846c875d2558ac
Fix processResources configuration cache compatibility
```

開始時HEADが進んでいる場合は、最新のOwner／Plugin担当変更を保持し、差分を監査する。

### Project Repository

```text
eariver/Project_Wayfarer
```

参照基準Commit：

```text
344eedc738d75954daa43facfeef302944f2963a
docs: Frontier Lock文書整合Commitを記録
```

Project Repositoryは参照専用である。

Plugin担当はProject Repository、Project Runtime、Project DatabaseまたはProject Permissionを変更しない。

---

## 5. Authority

矛盾時の優先順：

1. Project Ownerの明示判断が反映された最新Repository文書
2. Project Wayfarer現行`docs/`およびRuntime Lock
3. 現行Server／Theme Concept
4. 現行Plugin Concept
5. Plugin Repositoryの作業指示書兼設計仕様書
6. Plugin担当の詳細設計
7. 実装上の便宜

必ず参照する。

### Plugin Repository

```text
docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md
README.md
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
.github/workflows/ci.yml
```

既存のSource、Migration、Config、Contract、ADR、TestおよびModuleもすべて確認する。

### Project Repository

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

参照したVersion、Blob SHAまたはCommit SHAをTest Reportに記録する。

---

## 6. Project Wayfarerの固定境界

Plugin担当の裁量は、次の境界内で行使する。

### 6.1 Runtime Plugin

```text
Wayfarer_Core:
  Main／Frontier
  共通Infrastructure／Service Contract

Wayfarer_Main:
  Main only
  Growth Pickaxe

Wayfarer_Frontier:
  Frontier only
  Worlds Beyond MVPとFrontier固有機能
```

Lobby Pluginは、V0.1.0必須責務が確定するまで作成しない。

### 6.2 依存方向

```text
Wayfarer_Main
  → Wayfarer_Core API

Wayfarer_Frontier
  → Wayfarer_Core API

Wayfarer_Core
  -/→ Wayfarer_Main
  -/→ Wayfarer_Frontier

Wayfarer_Main
  -/→ Wayfarer_Frontier
```

循環依存を作らない。

### 6.3 Data Authority

```text
MariaDB:
  Wayfarer独自Pluginの永続Gameplay Data

Redis:
  Cache／Lock／PubSub／Message補助
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
- MVI Databaseの直接更新
- mcMMO Databaseの直接更新
- EliteMobs Databaseの直接更新
- 通常InventoryのMariaDB保存
- MainとFrontier間のItem移送
- MVIによるMain／Frontier Backend間Profile管理

### 6.4 Threading

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

Main Thread同期DB／Redis I/Oを行わない。

外部APIのThread Contractを推測しない。

### 6.5 Project Runtime

Plugin担当は次を実行しない。

```text
Project Wayfarer ServerへのJAR配置
Project実DBへのMigration適用
Project Runtime Config反映
Project Secret設定
Project Permission変更
Project Server起動
Project World変更
Project Player Data変更
versions.yml更新
plugin-manifest.yml更新
Roadmap完了化
```

Config例、Migration、導入手順、Permission NodeおよびRollback手順をReleaseへ含めることは許可する。

---

## 7. Plugin Repository Foundation最低要求

Plugin担当は、採用するArchitectureにかかわらず次を満たす。

```text
Java 25
Gradle 9.6.1
Kotlin DSL
Paper API 1.21.11-R0.1-SNAPSHOT
Group／Package io.github.eariver.wayfarer
```

必須：

- Gradle Wrapperを追跡する。
- Wrapper Version／Checksumを確認する。
- Clean環境で`check`／`assemble`が成功する。
- CIでJava 25のBuild／Testを行う。
- Configuration Cacheとの互換性を維持する。
- Versionを一元管理する。
- Compiler Warningを無断許容しない。
- Release Artifactを再現可能にする。
- Source CommitとArtifact Hashを対応付ける。
- API Class Identityを破壊しない。
- Runtime JARへ不要なTest／Secret／Runtime Dataを含めない。
- Generated JARを通常Source TreeへCommitしない。
- License／Third-party noticeを提供する。
- Secret／World／DB Data／Player Data／Logを追跡しない。

内部Moduleを追加・統合してよいが、責務境界と依存方向を維持する。

---

## 8. Wayfarer_Core最低機能

Plugin担当は実装方法を決めてよいが、Project本流は最低限次を必要とする。

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

Hot reload／PlugMan系Reloadを正式対応に含める必要はない。

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

ただし、後続Main／Frontierが以下を推測せず利用できること。

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

公開APIへHikari、Flyway、Lettuce、JDBC Connection、Plugin実装Class、Paper Event、Player、ItemStackまたはServer固有Gameplay Domainを漏らさない。

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

Secret値そのものをGit、YAML Sample、Log、Audit、Exceptionへ出さない。

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
- 必要なReconcile state

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

同等以上のState Machineへ改善してよい。

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

RedisEconomy内部Keyへ触れず、正式API／Vault境界を使用する。

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

SecretをRedactする。

Critical Eventを黙ってDropしない。

### 8.7 Identity

Player：

- UUID Authority
- Last known nameは補助
- Authentication／Permission／Inventoryを再実装しない

Item：

```text
wayfarer:item_type
wayfarer:owner_uuid
wayfarer:instance_epoch
wayfarer:schema_version
wayfarer:item_instance_id
wayfarer:display_revision
```

同等以上の安全なIdentityへ改善してよい。

Lore／Name／Materialだけで判定しない。

Unknown Schema／Type、Invalid UUID、Owner mismatch、Epoch mismatchをFail-closedする。

### 8.8 Redis

最低用途：

- Cache
- Lock foundation
- Pub/Sub／Invalidation foundation
- Message foundation
- Idempotency補助

MariaDB Authorityを失わない。

Redis停止時の縮退／拒否対象を文書化する。

### 8.9 Thread／Task

最低：

```text
Main Thread snapshot
→ immutable request
→ async operation
→ immutable result
→ Main Threadで再検証
→ Bukkit mutation
```

- Bukkit可変ObjectをAsyncへ保持しない。
- Executor Queue／Backpressure／Shutdownを持つ。
- Main Thread同期I/O検出Testを行う。
- Disable後Callbackを拒否する。

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

同等以上のCommand体系へ再編してよい。

Permission Node、Console可否、Redaction、Audit、Confirmationを文書化する。

---

## 9. 追加Plugin／機能の要求

Plugin担当がWayfarer_Main、Wayfarer_Frontierまたは他の許容されたArtifactをReleaseへ含める場合、各Artifactについて次を満たす。

### 9.1 個別Artifact情報

```text
Artifact Name
Version
Filename
SHA-256
Source Commit
Tag／Release
Config Version
Migration Version
Required Dependencies
Optional Dependencies
Bundled／Relocated Libraries
Commands
Permissions
Known Limitations
Removal／Rollback
```

### 9.2 個別Test

- Unit Test
- Database Integration Test
- External Plugin Adapter Test
- Standalone Test Server Test
- Restart／Reconnect
- Failure Injection
- Main Thread I/O
- Duplicate／Idempotency
- Permission
- Packaging
- API Class Identity
- Artifact間依存
- Runtime配置境界

### 9.3 Roadmap境界

追加Artifactは次の状態で本流へ渡す。

```text
Plugin-side implementation:
  complete／partial

Plugin-side test:
  passed／limited／failed

Project runtime integration:
  not performed

Project acceptance:
  pending

Roadmap completion:
  pending
```

---

## 10. Plugin側Test最低要求

Plugin担当は、実装した全Artifactについて十分なTestを設計してよい。

本流が最低限必要とするのは以下である。

### 10.1 Automated Test

- Unit
- MariaDB Integration
- Redis Integration
- Migration
- Concurrency
- Idempotency
- Failure／Timeout
- Restart Recovery
- Config Validation
- Secret Redaction
- Packaging
- API compatibility

Testcontainers等の隔離環境を使用する。

### 10.2 単体Test Server

Project Wayfarer統合Runtimeではなく、Plugin担当管理の隔離Serverを使用する。

Baseline：

```text
Paper 1.21.11
Java 25
isolated MariaDB
isolated Redis
Project同系統のWaymark Provider／Vault
```

Wayfarer_Core最低試験：

- Clean enable／disable
- Restart
- Services registration／unregister
- Invalid Config
- Missing Secret
- DB outage
- Migration failure
- Redis outage／reconnect
- Waymark Provider missing／disabled
- Balance
- Debit
- Refund
- Insufficient funds
- Duplicate idempotency key
- Timeout
- UNKNOWN
- Reconcile
- 二重Debitなし
- 二重Refundなし
- Main Thread DB／Redis I/Oなし
- 著しいTick stallなし
- Disable後Callbackなし
- Health
- Admin Commands
- Permission denial
- Sensitive data redaction
- API Class Identity
- JAR dependency inspection

追加Artifactは、そのGameplay／Persistence／Boundaryに応じて同等以上のTestを行う。

---

## 11. 本流が要求するTest Report

Release時に、全体ReportまたはArtifact別Reportを提出する。

推奨Path：

```text
docs/reports/
  Project_Wayfarer_Plugin_Release_Test_Report_<version>_<date>.md
```

最低内容：

1. Plugin Repository HEAD
2. Release Tag／URL
3. 対象Artifact一覧
4. ArtifactごとのVersion／Filename／SHA-256
5. Java／Gradle／Paper
6. MariaDB／Redis／Waymark構成
7. 参照Project文書とCommit／Blob SHA
8. 実装Scope
9. 追加実装Scope
10. Project方針との適合説明
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

「すべて成功」とだけ記載せず、検証内容とEvidenceを示す。

---

## 12. Release成果物

Plugin担当は、実装したArtifactをGitHub ReleaseまたはPre-releaseとして公開する。

最低：

```text
Non-SNAPSHOT Version
Git Tag
Release URL
Source Commit
Runtime JAR
SHA-256
Release Notes
Migration Version
Config Version
Sanitized Config
Command／Permission一覧
Dependency一覧
Bundled／Relocated Library一覧
License／Third-party Notice
Plugin Test Report
Known Limitations
Rollback／Removal手順
```

推奨：

- Checksums file
- SBOM
- Source JAR
- Javadoc JAR
- Test Report attachment
- Migration／Config compatibility matrix
- Upgrade notes

Plugin担当は、Release単位を自由に決めてよい。

例：

```text
CoreだけのRelease
Core／Main／Frontier同時Release
Artifactごとの個別Release
共通VersionのMonorepo Release
```

いずれの場合もArtifact単位の識別、Hash、Test、依存およびProject受入状態を明示する。

---

## 13. 本流Handoff Package

Plugin担当はProject Ownerへ次を渡す。

### 13.1 必須一覧

```text
Release URL
Release Tag
Release Version
Final Source Commit
Artifact一覧
各JAR Filename
各JAR SHA-256
各Config Version
各Migration Version
Sanitized Config
Command一覧
Permission一覧
Dependency／Placement一覧
Plugin Test Report
Known Limitations
Open Decisions
Upgrade／Rollback手順
```

### 13.2 Artifact Matrix

次の形式または同等の表を提出する。

| Artifact | Plugin-side implementation | Plugin-side test | Release | Project placement | Project acceptance | Roadmap Order |
|---|---|---|---|---|---|---|
| Wayfarer_Core | complete／partial | passed／limited／failed | URL／version | Main＋Frontier | pending | 9 |
| Wayfarer_Main | complete／partial／not included | passed／limited／failed／N/A | URL／version／N/A | Main only | pending | 10 |
| Wayfarer_Frontier | complete／partial／not included | passed／limited／failed／N/A | URL／version／N/A | Frontier only | pending | 11 |

条件付きAdapterは、Decision未成立なら`not authorized／not included`とする。

### 13.3 Project側受入Input

Project本流が受入指示書を作るため、次を明示する。

- Runtime前提
- Environment Variables
- Database／User／Schema要件
- Redis要件
- External Plugin要件
- Load order
- Placement
- First migration behavior
- Failure behavior
- Health確認方法
- Smoke Test方法
- Backup対象
- Restore対象
- Removal方法
- Rollback方法
- Data compatibility
- Downgrade可否
- Test Serverとの差分

---

## 14. Plugin担当が変更してはいけないもの

Plugin担当の裁量範囲外：

- Project WayfarerのGameplay結果を無断変更
- Cross-backend Item移送
- Main／Frontierの責務混在
- MariaDB／Redis／MVI／Waymark Authority変更
- Project Runtimeへの実適用
- Project Database Migration実行
- Project Permission変更
- Project World／Player Data変更
- Project Roadmap完了化
- `versions.yml`／`plugin-manifest.yml`更新
- Decision未成立Adapterの正式実装／Release
- Secret／Runtime Data Commit
- Unsupported外部Plugin内部APIへの推測依存

変更が必要と判断した場合は、次を提示して停止する。

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

## 15. Commit／Release運用

Plugin担当はRepositoryの運用方針を決めてよい。

最低条件：

- Force Pushしない。
- Owner変更を巻き戻さない。
- SecretをCommitしない。
- Runtime DataをCommitしない。
- ArtifactとSource Commitを対応付ける。
- Release Tagを付ける。
- Release Notesを作る。
- Test ReportをSourceと対応付ける。
- Failed Testを隠さない。
- Generated JARを通常Source履歴へ混在させない。

Commit分割、Branch、PR、Squash、Merge方式はPlugin担当の裁量とする。ただし最終履歴とRelease provenanceを追跡可能にする。

---

## 16. 完了条件

### 最低完了

- Repository FoundationがRelease可能
- Wayfarer_Core実装
- Automated Test
- Standalone Test Server試験
- Non-SNAPSHOT Release／Pre-release
- JAR／SHA-256
- Test Report
- Handoff Package
- Project Runtime未変更

### 追加Artifactがある場合

- ArtifactごとのRelease evidence
- ArtifactごとのTest
- Artifact Matrix
- Project配置境界
- Roadmap Order対応
- Project acceptance pending表示
- 条件付きDecision遵守

### 品質

- Project方針に反しない
- Authority境界を維持
- Main Thread同期I/Oなし
- 二重課金／返金なし
- Migration安全
- Secret非露出
- Fail-closed
- Known Limitation明示
- Reproduction可能

---

## 17. 完了報告

次を報告する。

1. 推奨Sol
2. Pre-execution HEAD
3. Final Source Commit
4. Release Tag／URL
5. 実装Artifact一覧
6. 追加実装一覧
7. Project方針適合確認
8. ArtifactごとのVersion／Filename／SHA-256
9. Config Version
10. Migration Version
11. Public API
12. Module／Dependency構成
13. Packaging／Relocation
14. Commands
15. Permissions
16. Database Schema
17. Transaction保証
18. Waymark Provider方式
19. Redis用途
20. Health
21. Threading
22. Build結果
23. Automated Test結果
24. Standalone Test Server結果
25. Main Thread I/O結果
26. Failure Injection結果
27. Restart／Disable／Reconnect結果
28. API Class Identity
29. Performance／Tick影響
30. Secret Redaction
31. Known Limitations
32. Failed／Skipped Test
33. Open Decisions
34. Test Report Path
35. Artifact Matrix
36. Project受入Input
37. Runtime非変更
38. Secret／Runtime Data非追跡
39. Final Git status
40. Project Ownerへ渡すFile／URL一覧

---

## 18. 完了後の状態

最低状態：

```text
Plugin Repository Foundation:
  complete

Wayfarer_Core:
  plugin-side implementation complete
  plugin-side standalone test complete
  released

Project Roadmap Order 9:
  plugin-side prerequisites complete
  Project integration／acceptance pending
```

追加Artifactがある場合：

```text
Wayfarer_Main:
  plugin-side status reported
  Project acceptance pending
  Order 10 not automatically complete

Wayfarer_Frontier:
  plugin-side status reported
  Project acceptance pending
  Order 11 not automatically complete
```

次Action：

```text
Project OwnerがRelease、Artifact、Hash、Config、Migration、Test Report、
Known LimitationsおよびArtifact MatrixをProject Wayfarer本流へ共有する。

本流は、その情報を基にProject Wayfarer Serverへの統合・受入Test指示書を作成する。
```
