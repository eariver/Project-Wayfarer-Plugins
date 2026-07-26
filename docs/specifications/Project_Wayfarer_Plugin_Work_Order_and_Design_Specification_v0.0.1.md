# Project Wayfarer Plugin 作業指示書兼設計仕様書 v0.0.1

> **配置先:** Plugin Repository `docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md`  
> **想定Repository名:** `Project-Wayfarer-Plugins`  
> **状態:** Implementation Ready / Runtime未導入  
> **対象:** `Wayfarer_Core`、`Wayfarer_Main`、`Wayfarer_Frontier`  
> **条件付き対象:** `Wayfarer_Frontier_EliteMobsMVI`はDecision Resultが`ADAPTER_REQUIRED`の場合だけ、別Taskで追加  
> **重要:** 本文書はSource実装を指示するが、Project Wayfarer Runtimeへの配置、Database Migration実行、Config適用、World操作またはReleaseを承認しない。
> **同Revision内訂正:** Testcontainers 2.0系の正式Artifact IDへ修正してVersionを2.0.5へ更新し、Main／Frontier JARへのAPI二重内包を防ぐ依存境界とGradle Wrapper検証を明確化した。

---

## 1. 目的

Project Wayfarer V0.1.0 Alphaに必要な独自Plugin群を、1つのGradle Multi-module Repositoryで実装する。

成果物:

```text
Wayfarer_Core.jar
Wayfarer_Main.jar
Wayfarer_Frontier.jar
```

初期Repositoryには、条件未成立の次を作成しない。

```text
Wayfarer_Frontier_EliteMobsMVI.jar
```

実装はProject WayfarerのConcept、V0.1.0 Scope、Runtime Lock、RoadmapおよびAcceptanceを満たし、既製Pluginの責務を再実装しない。

---

## 2. Authorityと参照固定

実装時の優先順:

1. Project Ownerの明示判断をRepository文書へ反映した最新記録
2. Project Wayfarer現行`docs/`およびRuntime Lock
3. 現行Server／Theme Concept
4. 現行Plugin Concept
5. 本仕様書
6. 実装者の局所判断

本仕様書作成時に確認したProject Wayfarer Design Input:

| Path | Version／Blob SHA |
|---|---|
| `concepts/plugins/Project_Wayfarer_Plugin_Concept_v0.0.3.md` | v0.0.3 / `f5ec0698659f9db2617b79f0d80fc09ce5756dad` |
| `concepts/plugins/main/Project_Wayfarer_Growth_Tool_Concept_v0.0.5.md` | v0.0.5 / `d8d129f67b4c1104e0085a0e71fd93dd154b5209` |
| `concepts/plugins/frontier/Project_Wayfarer_Worlds_Beyond_Plugin_Concept_v0.0.4.md` | v0.0.4 / `8caca1d90dac1db228146bf19fc330ed539785b4` |
| `concepts/plugins/frontier/Project_Wayfarer_Ruined_Frontier_Integration_Decision_Concept_v0.0.2.md` | v0.0.2 / `9d6363c508416fd01f905d9224ac4d853bc6f99d` |
| `concepts/frontier/Frontier_Server_Specification_V0.0.5.md` | v0.0.5 / `38d503c8f37bf6a33976bd2ca56f1024d99710f2` |
| `concepts/frontier/Worlds_Beyond_Specification_V0.0.6.md` | v0.0.6 / `22961a2c9d32c76377fb96ae144193bce0793b00` |
| `concepts/frontier/Ruined_Frontier_Specification_V0.0.5.md` | v0.0.5 / `7dc61b0cb730ed51946cac8a0e12a42a49ca4f3a` |
| `docs/14-frontier-v0.1.0-scope.md` | `8a668437cce826f26828fa6c7460672dbb1906d8` |
| `docs/09-roadmap.md` | `8481727c6b2e339785f3f652c00048fa4443bb52` |
| `docs/06-acceptance-tests.md` | `d838eedfb0f3411746a9b99a2f0c63943fa76efd` |
| `docs/15-frontier-runtime-lock.md` | `e78abf0f5cc4efc2ba64ac2397c6e109c956e2ec` |

実装開始時にProject Wayfarer側のこれらのPathが更新されている場合、差分を先に確認する。Gameplay結果を変更する差分があれば、本仕様書を改訂してから実装する。

---

## 3. Build／Runtime Baseline

| 項目 | Baseline |
|---|---|
| Language | Java 25 |
| Build | Gradle 9.6.1 / Kotlin DSL |
| Server API | Paper API `1.21.11-R0.1-SNAPSHOT` |
| Package Root | `io.github.eariver.wayfarer` |
| Group ID | `io.github.eariver.wayfarer` |
| Initial Version | `0.0.1-SNAPSHOT` |
| Database | MariaDB、UTC、`utf8mb4` |
| DB Pool | HikariCP 7.0.2 |
| Migration | Flyway 12.6.2 Core＋MySQL module |
| JDBC Driver | MariaDB Java Client 3.5.8 |
| Redis Client | Lettuce 7.2.1.RELEASE |
| Test | JUnit Jupiter 5.14.3、Mockito 5.23.0、Testcontainers 2.0.5 |
| Fat JAR | Shadow 9.6.1 |

Paper Plugins experimental manifestは使用せず、各Runtime Pluginは`plugin.yml`を使用する。

依存Versionは`gradle/libs.versions.toml`で一元管理する。Project Runtime Lockで固定された第三者Plugin VersionをSource側から勝手に変更しない。

---

## 4. Repository構成

```text
Project-Wayfarer-Plugins/
├─ docs/
│  ├─ specifications/
│  ├─ architecture/
│  ├─ contracts/
│  ├─ operations/
│  └─ adr/
├─ libraries/
│  ├─ wayfarer-api/
│  ├─ wayfarer-common/
│  └─ wayfarer-testkit/
├─ integrations/
│  └─ wayfarer-leafgrapple-adapter/
├─ plugins/
│  ├─ wayfarer-core/
│  ├─ wayfarer-main/
│  └─ wayfarer-frontier/
├─ tools/
├─ .github/
├─ settings.gradle.kts
├─ build.gradle.kts
├─ gradle.properties
└─ gradle/libs.versions.toml
```

禁止:

- MainとFrontierの相互依存
- CoreからMain／Frontierへの依存
- Server固有GameplayのCore混在
- MVI／EliteMobs／RedisEconomy内部DBの直接更新
- 通常InventoryのMariaDB保存
- Runtime JAR、Secret、World、DB Data、LogのCommit
- 条件未成立のEM–MVI Adapter先行作成

---

## 5. Module責務

### 5.1 `wayfarer-api`

CoreがBukkit ServicesManagerへ公開する安定Contractだけを置く。

最低Interface:

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

APIは可能な限りJDK型、UUID、record、enum、`CompletionStage`で構成する。実装Class、Hikari、Flyway、Lettuce、Paper Event、Plugin固有Domainを公開しない。

### 5.2 `wayfarer-common`

Runtime Plugin内部で共有する実装補助:

- UUID／Instant／JSON／SQL変換
- Config revision
- optimistic locking helper
- idempotency key
- validation
- PDC key name定義
- error/result型

通常InventoryまたはGameplay Domainを置かない。

### 5.3 `wayfarer-testkit`

- fake services
- deterministic clock
- transaction fixtures
- MariaDB Testcontainers fixture
- Paper-independent Domain test utilities

Production moduleから依存しない。

### 5.4 `wayfarer-leafgrapple-adapter`

LeafGrapple 1.0.2のVersion差を隔離する。

責務:

- 正規Hook Item生成
- 必要API／Classの存在確認
- Runtime Version確認
- Entity／Player Hook無効化確認
- 耐久無効
- 連携不能時の明示Error

Hook projectile、pull physics、cooldown計算を再実装しない。

### 5.5 `wayfarer-core`

- MariaDB Pool
- Flyway Migration
- Redis connection foundation
- Waymark Adapter
- Transaction ID／Idempotency
- Common Audit
- Player Identity
- Common PDC identity foundation
- Shared executor／scheduler abstraction
- Health／dependency state
- Bukkit Service registration

### 5.6 `wayfarer-main`

- Growth Pickaxeの全Domain
- Main Resource-family allowlist
- 初回非同期配布
- Progress／Evolution／Broken／Repair
- GUI
- Admin／Debug
- Session cache／checkpoint
- `wf_main_*` persistence

### 5.7 `wayfarer-frontier`

- `frontier_iris`専用Worlds Beyond機能
- Traversal Loadout
- LeafGrapple連携
- Navigation／Discovery／Teleport GUI
- Launchpad
- Waystone
- Frontier WM Shop
- Pending Delivery
- Admin／Reconcile
- `wf_frontier_*` persistence

---

## 6. Runtime Plugin Lifecycle

### Core Enable

```text
Config syntax validation
→ Environment secret resolution
→ Database pool creation
→ Flyway validate／migrate
→ Redis optional connection and health
→ Waymark provider capability probe
→ Service implementation construction
→ Bukkit ServicesManager registration
→ scheduler start
→ ENABLED
```

重大依存に失敗した場合:

- Stack traceだけで継続しない。
- Healthを`FAILED`にする。
- Serviceを登録しない、または書込機能をUnavailableにする。
- Main／FrontierはFail-closedして自身をDisableする。
- Migration失敗時に旧Schemaで継続しない。

### Main／Frontier Enable

```text
Config validation
→ Wayfarer_Core hard dependency確認
→ required service lookup
→ DB schema compatibility確認
→ external Plugin capability probe
→ listener／command登録
→ startup reconcile
→ scheduler start
```

### Disable

- 新規Callback受付停止
- listener／scheduler停止
- dirty session checkpoint
- timeout付きflush
- service unregister
- executor graceful shutdown
- timeout超過をERROR＋Audit
- Disable後にBukkit API callbackを実行しない

---

## 7. Threading Contract

### Main Thread専用

- Bukkit/Paper Entity、Player、Inventory、ItemStack、Block、World、Chunk操作
- GUI
- Teleport
- velocity／Elytra state
- Event cancellation

### 非同期専用

- JDBC
- Migration
- Redis network I/O
- Audit write
- indexed search
- checkpoint
- expiration candidate query

### Bridging

```text
Main Thread snapshot
→ immutable request
→ async DB／service operation
→ result
→ Main ThreadでPlayer online／world／epoch／stateを再検証
→ Bukkit mutation
```

DB Result取得後に、古いPlayer参照やItemStack参照を直接再利用しない。

Main Threadの同期I/Oを検出するtest hookを用意する。

---

## 8. Common Identity

PDC namespaceは`wayfarer`。

共通Key:

```text
wayfarer:item_type
wayfarer:owner_uuid
wayfarer:instance_epoch
wayfarer:schema_version
wayfarer:item_instance_id
wayfarer:display_revision
```

Growth Tool追加Key:

```text
wayfarer:tool_id
wayfarer:tool_type
```

Identity判定ではLore、Name、Materialだけを使用しない。

PDCから読み込んだUUID、epoch、typeをDB／session stateと照合する。未知Schema、未知Type、不正UUID、epoch不一致はFail-closedする。

---

## 9. Database原則

- Table prefix: `wf_core_`、`wf_main_`、`wf_frontier_`
- ID: canonical lowercase UUID `CHAR(36) CHARACTER SET ascii`
- Time: UTC `TIMESTAMP(3)`
- enum相当値: `VARCHAR`＋Java enum、未知値は起動またはrecord loadをFail-closed
- JSON: `LONGTEXT`＋`CHECK(JSON_VALID(...))`
- concurrency: `lock_version BIGINT`
- transaction isolation: default `READ COMMITTED`
- DB transaction内でBukkit APIまたは外部Economy APIを呼ばない
- Migrationは追加専用。適用済みMigrationを書き換えない
- SQL Migrationは各Plugin moduleの`db/migration/<scope>`へ置く
- Coreが利用Pluginのmigration locationを明示的に収集する

---

## 10. Core Transaction State Machine

```text
PREPARED
→ DEBIT_PENDING
→ DEBITED
→ DOMAIN_COMMIT_PENDING
→ COMMITTED

failure:
DEBITED
→ REFUND_PENDING
→ REFUNDED

manual:
any uncertain state
→ UNKNOWN
→ RECONCILED_COMMITTED | RECONCILED_REFUNDED | FAILED
```

必須Field:

- transaction_id
- idempotency_key unique
- transaction_type
- actor_uuid
- subject_type／subject_id
- amount_wm
- state
- provider_reference
- payload_json
- failure_code
- timestamps
- lock_version

同一idempotency keyの再実行は、既存結果を返し、二重Debit／Refundをしない。

Waymark AdapterはRedisEconomy内部Keyを直接編集しない。正式Provider API／Vault境界を使用し、Main Threadを長時間Blockしない。Providerのthread contractを実機確認し、非同期利用不能な同期APIしかない場合は、無断で危険なoff-thread呼出しをせず、対応方式をProject側へ報告する。

---

## 11. Growth Pickaxe設計

### 11.1 Logical Record

`wf_main_growth_tool`を正本とする。

状態:

```text
tool_status: ACTIVE | BROKEN | REVOKED
delivery_status: DELIVERED | PENDING
active_branch: FORTUNE | SILK_TOUCH
```

`REISSUED`をcurrent statusにしない。Reissueはepoch増加＋Audit event。

### 11.2 Session State

Playerごとにimmutable identityとmutable progress accumulatorを保持する。

```text
GrowthToolSession
- logical record snapshot
- derived evolution state
- next threshold
- dirty progress delta
- loaded config revision
- flush generation
- closed flag
```

同一Playerの処理はper-player serial executorまたはlockで直列化する。

### 11.3 Progress

対象World:

```text
resource
resource_nether
resource_end
```

対象:

- `minecraft:mineable/pickaxe`
- Playerの成功した非Cancel `BlockBreakEvent`
- Survival
- 実際に破壊成立したAdventure
- Player設置
- Generator生成
- Silk Touch再設置Ore

対象外:

- Main三次元
- unknown World
- Creative／Spectator
- Explosion／Piston／WorldEdit／Command／Plugin直接削除
- Owner／tool_id／epoch不一致
- BROKEN

1 Eventにつき一度だけ加算する。

固定小数点:

```text
1.000 = 1000 units
```

### 11.4 Threshold Engine

- `threshold[0]=0`
- 累積Threshold配列
- 非再帰Loopで生成
- binary searchでEvolution Count
- cache末尾を超えた時だけ遅延拡張
- config revision単位でimmutable cache
- `Long.MAX_VALUE`接近時は加算停止＋Critical Audit
- Config reloadはvalidate後atomic swap
- unsafe partial reload禁止

Material threshold:

```text
Stone 100
Iron 400
Diamond 1200
```

Diamond後increment:

```text
800 + 200n + 40n²
```

Cycle:

```text
Efficiency
→ Unbreaking
→ Efficiency
→ Unbreaking
→ Fortune
```

Effective cap:

```text
Efficiency 10
Unbreaking 10
Fortune 5
Silk Touch 1
```

### 11.5 Durability

ProgressによりEvolution Countが上がった時だけ最大回復。

Config reconciliation:

- repairしない
- ACTIVEは残存耐久率を維持
- new remaining durabilityを1～maxへClamp
- BROKENはBROKEN維持

破壊直前／Item damage eventをInterceptし、消滅前に`GRAY_DYE`へ変換する。DBをBROKENへ即時checkpointする。DB更新が確定するまで二重操作をlockする。

### 11.6 Repair

ACTIVE:

```text
full = ceil(100 × (1 + evolution_count × 0.08))
cost = ceil(full × max(0.25, missing_ratio))
```

max durabilityなら修理不可／0 WM。

BROKEN:

```text
cost = full + 100 + evolution_count × 5
```

TransactionはCore transaction state machineを使用する。

### 11.7 Checkpoint

- 5分周期
- Evolution
- Broken
- Repair
- Reissue／Admin
- Quit
- Disable

Quit／DisableでもMain Thread DB I/Oを行わない。shutdownはtimeout付きasync flush。

### 11.8 GUI

Main Hand空中右Clickのみ。

Main GUI:

- type／status／material
- evolution count
- cumulative／remaining progress
- enchant／branch
- durability
- repair preview
- clamp
- Repair
- Help

Repair confirm GUIはsingle-use tokenを持ち、close／disconnect／double clickで二重実行しない。

---

## 12. Worlds Beyond設計

### 12.1 World Boundary

唯一の対象World:

```text
frontier_iris
```

次を作成・登録・自動認識しない。

```text
frontier_iris_nether
frontier_iris_the_end
Nether／End／unknown worlds
```

Prefix／部分一致を禁止し、完全一致Allowlistを使用する。

### 12.2 Traversal Loadout

恒久:

- Elytra: Unbreakable、Soulbound
- Grappling Hook: LeafGrapple正規Item、耐久無効、Soulbound
- Navigation Item: Soulbound

消耗:

- Initial Launchpad 2

初回`frontier_iris`入場時だけ配布。Inventory FullはPending Delivery。恒久品はepoch reissue、Launchpadは配達済み紛失・死亡・使用で無料再支給しない。

### 12.3 Navigation GUI

入口:

- Discovery
- Teleport
- Shop
- Loadout
- Help

`frontier_iris`外では操作拒否。

### 12.4 Teleport

- current／destinationとも`frontier_iris`
- discovered
- PROTECTED／CONTESTABLE
- permission
- cooldown 30s
- warmup 3s
- move／damage cancel
- combat／fall／vehicle／portal中拒否
- chunk load
- safe arrival
- execution直前再検証
- cross dimension false
- cost 0 WM

### 12.5 Launchpad

- single `LIGHT_WEIGHTED_PRESSURE_PLATE`
- player yawをdirectionとしてsnapshot
- step-on trigger
- sneaking中無効
- public use
- 3 successful uses
- horizontal 2.5
- vertical 1.2
- 2s cooldown
- auto Elytra
- 30日無操作
- useで期限延長
- placement後回収不可
- 任意Playerが通常Break可能
- Break時Dropなし
- 環境要因保護
- DBとBlockをreconcile
- max active 0 = unlimited

設置は`wf_frontier_placement_transaction`でItem／Row／BlockをExactly-onceへ収束させる。

### 12.6 Waystone

State:

```text
PROTECTED 14d
→ CONTESTABLE 14d
→ DORMANT 180d
→ RUINED
```

価格:

```text
Placement Tool 600 WM
Maintain 200 WM
Contest 300 WM
Reactivate 150 WM
Teleport 0 WM
```

Name:

```text
<Founder Name at Creation>'s <Biome Display Name> #<Sequence>
```

Founder immutable、Maintainer mutable、Founder×Biome sequenceは再利用しない。

配置:

- same-world active waystoneから水平1000以上
- 5×5 template
- bounding box clearance
- foundation tolerance 1
- border margin 32
- Gate／Spawn exclusion 256
- Liquid／Region／protected structure重複禁止
- success時のみTool消費

Waystone全Blockはsystem以外変更不可。Adminも専用Commandを使用する。

Discoveryは現地Interactionで作成する。GUI表示だけでは解禁しない。

Schedulerは期限候補をasync queryし、Main Threadでstructure mutationする。Restart catch-up必須。

---

## 13. Commands／Permissions

Root command:

```text
/wayfarer
```

一般Player:

```text
/wayfarer tool
/wayfarer frontier
```

Admin:

```text
/wayfarer admin health
/wayfarer admin transaction inspect <id>
/wayfarer admin transaction reconcile <id>

 /wayfarer admin tool inspect <player>
 /wayfarer admin tool grant <player>
 /wayfarer admin tool reissue <player>
 /wayfarer admin tool repair <player>
 /wayfarer admin tool branch <player> <fortune|silk_touch>
 /wayfarer admin tool revoke <player>
 /wayfarer admin tool reconcile <player>

 /wayfarer admin frontier loadout inspect <player>
 /wayfarer admin frontier loadout reissue <player> <item>
 /wayfarer admin frontier delivery retry <player>
 /wayfarer admin frontier launchpad inspect <id>
 /wayfarer admin frontier launchpad remove <id>
 /wayfarer admin frontier launchpad reconcile [id]
 /wayfarer admin frontier waystone inspect <id>
 /wayfarer admin frontier waystone repair <id>
 /wayfarer admin frontier waystone remove <id>
 /wayfarer admin frontier waystone reconcile [id]
```

正式Syntaxは実装中にCommand treeとして整えるが、機能を削らない。

Permissionはmoduleごとにprefixを分け、Admin wildcardを一般Player／Builderへ付与しない。

Debug commandはMain configの`debug-commands.enabled=false`がdefaultで、Admin permissionとの両方を要求する。

---

## 14. Configuration

Secret値をYAMLへ直接保存しない。Configは環境変数名を保持し、Runtimeで解決する。

Core:

```text
server-id
database env names／pool／timeouts
redis env name／timeouts
migration locations
economy expected provider／timeouts
executor sizes
audit retention
shutdown timeout
```

Main:

```text
world allowlist
progress weights／ore multipliers／fallback
threshold
enchant cycle／caps
repair
checkpoint
item display
debug gate
```

Frontier:

```text
frontier_iris exact allowlist
portal deny
loadout
LeafGrapple expected version
shop
launchpad
waystone
discovery
teleport
scheduler
protection
```

Config load時に全値をtyped immutable configへ変換し、不正値を部分適用しない。

---

## 15. Migrations

本Repositoryの初期SQLはDDL design baselineである。実装時にFlyway Testcontainers testを通し、Project Runtimeへは別Taskで適用する。

Migration order:

```text
core V001
main V001
frontier V001
```

Migration locationをPluginごとに分離し、CoreだけのServerでMain／Frontier schemaを勝手に作らない。Mainはcore＋main、Frontierはcore＋frontierを要求する。

---

## 16. Testing

### Unit

- threshold generation／binary search
- enchant cycle
- durability conversion
- repair formulas
- identity validation
- transaction transitions
- waystone lifecycle
- sequence allocation
- launchpad expiration
- safe state guards
- config validation

### MariaDB Integration

Testcontainersで:

- migration from empty DB
- idempotent migration
- unique owner＋tool
- optimistic lock conflict
- sequence concurrency
- transaction idempotency
- pending delivery
- expiration query indexes
- restart reconciliation fixtures

### Paper Integration／Smoke

Project Test Serverで:

- Plugin enable order
- no Main Thread DB I/O
- Core service registration
- Main-only／Frontier-only placement
- external Plugin capability
- PDC persistence
- event cancellation
- GUI
- Item duplication prevention
- restart／reconnect
- MVI separation
- no cross-backend item transfer
- LeafGrapple 1.0.2
- WorldGuard／WorldEdit protection behavior
- Waymark debit／refund

### Performance

- join storm
- fast mining
- 3000-block discovery query
- many launchpad expiration candidates
- shutdown flush
- DB outage
- Redis outage
- Waymark provider outage

---

## 17. Implementation Order

### Phase 0: Repository Foundation

- Gradle structure
- CI
- formatting／warnings
- API contracts
- empty plugin bootstraps
- migration test foundation

### Phase 1: Wayfarer_Core

- config／secret loader
- Hikari／Flyway
- services
- audit
- transaction engine
- Waymark adapter
- Redis foundation
- health
- tests
- release candidate

### Phase 2: Wayfarer_Main

- schema／repository
- identity
- session／delivery
- progress／threshold
- evolution／durability
- broken／repair
- GUI／admin
- tests
- release candidate

### Phase 3: Wayfarer_Frontier Foundation

- schema／repository
- exact world boundary
- item identity／pending delivery
- LeafGrapple adapter
- Navigation GUI shell

### Phase 4: Launchpad／Shop

- shop transactions
- placement transaction
- block protection
- use／expiration
- reconcile
- tests

### Phase 5: Waystone

- placement／sequence
- template／protection
- lifecycle
- discovery
- teleport
- history
- reconcile
- tests

### Phase 6: Project Integration

- signed/reproducible build metadata
- three JAR hashes
- migrations/config versions
- Project integration contract
- Test Server delivery
- Project Acceptance

EM–MVI Adapterはこの順序に含めない。Project Decisionが`ADAPTER_REQUIRED`になった後に、独立仕様とmoduleを追加する。

---

## 18. Definition of Done

各Runtime Plugin:

- build success with Java 25
- unit tests pass
- no compiler warnings treated as allowed without explanation
- migration tests pass
- config schema documented
- permission documented
- no secrets／JAR／world data tracked
- exact artifact SHA-256
- source commit recorded
- clean enable／disable
- no Main Thread DB I/O
- no normal inventory persistence
- failure mode documented
- Project-side acceptance evidence produced

V0.1.0完了ではないもの:

- Sourceがcompileしただけ
- PluginがEnableしただけ
- Migration SQLが存在するだけ
- GUIが開くだけ
- one happy-path testだけ

---

## 19. Codex実行規則

Codexは次を守る。

1. 既存ConceptのGameplay結果を変更しない。
2. 不明点は安全側のFail-closedとし、推測で外部Plugin内部APIへ依存しない。
3. Runtime変更をこのRepositoryから実行しない。
4. Source変更ごとに対象module testを実行する。
5. DB migrationを適用済みVersionから書換えない。
6. public API変更はADRとcompatibility noteを要求する。
7. `Wayfarer_Frontier_EliteMobsMVI`を作成しない。
8. generated／binary／secretをCommitしない。
9. implementation reportに変更File、test、known limitationを記載する。
10. 局所Class名やprivate methodは裁量だが、本書のmodule、state、thread、persistence、failure boundaryを変更しない。

---

## 20. Init ZIPの位置付け

同梱ZIPはInit commit向けscaffoldである。

含む:

- Multi-module build
- plugin bootstrap
- API skeleton
- Config baseline
- SQL baseline
- CI
- ADR／contracts
- 本仕様書

含まない:

- 完成Gameplay実装
- Runtime-ready Migration承認
- external Plugin JAR
- Project Runtime Config
- Secret
- generated Gradle Wrapper binary

`tools/bootstrap-gradle-wrapper.ps1`で公式Gradle 9.6.1をSHA-256検証後にWrapper生成し、生成されたWrapper一式をCommitする。Wrapper生成後は`gradle/wrapper/gradle-wrapper.jar`の公式checksumも確認する。
