# Project Wayfarer Plugin担当向け
## Plugin V0.0.2 Wayfarer_Main／Wayfarer_Frontier 実装・軽量試験・段階Release・本流Handoff要求書

- 作成日: 2026-07-30
- 対象Plugin Repository: `eariver/Project-Wayfarer-Plugins`
- 対象Project Repository: `eariver/Project_Wayfarer`
- 現行Stable Plugin Release: `V0.0.1`
- 現行Stable Core Artifact: `Wayfarer_Core-V0.0.1.jar`
- 本要求の主対象: `Plugin V0.0.2`
- 状態: Plugin Source実装、Plugin側試験、Releaseを承認する。Project Wayfarer Runtimeへの配置、Project DatabaseへのMigration、Project Config反映、World操作は承認しない。

---

## 1. 推奨Sol

推奨Solは `high` とする。

### 理由

本Taskは、V0.0.1で確立した`Wayfarer_Core`を基盤として、Main専用`Wayfarer_Main`とFrontier専用`Wayfarer_Frontier`の主要Gameplay Domainを初めて実装する作業である。

特に次はServer V0.1.0へ直接影響する。

- Growth Pickaxeの論理Item Identity、Progress、Evolution、Broken、Repair
- Waymark Transactionと二重請求防止
- Traversal LoadoutとTheme-bound Item
- LeafGrapple連携
- LaunchpadのItem、Database、Physical Blockの整合
- Frontier WM Shop
- Main、Frontier、MVI、MariaDBのAuthority分離
- V0.0.1からのAPI、Config、Migration互換性
- 段階ReleaseとProject側受入単位

Sol Highは、網羅的な反復試験を意味しない。設計判断、責務境界、Migration、安全側動作へ思考を使い、検証は第13章の軽量方針に従う。

---

## 2. Version体系とRelease方針

### 2.1 ServerとPluginのVersionは別管理

- Project Wayfarer Server `V0.1.0`には、最終的にPlugin `V0.1.0`を適用する予定である。
- Plugin `V0.0.1`は`Wayfarer_Core`の初期Stable Releaseである。
- Plugin `V0.0.2`は、本要求書で定義するMain／Frontier主要機能の最初の実装Releaseである。
- Plugin `V0.0.3`以降で、未決事項、Waystone、調整、統合上の追加要求を段階的に実装してよい。
- Server `V0.1.0`の全独自Plugin要件が満たされた時点で、Plugin `V0.1.0`をReleaseする。

### 2.2 V0.1.0以前のStable Version

Stable Releaseは `V0.0.x` とする。`x`は1以上の整数であり、1桁に限定しない。

例:

- `V0.0.2`
- `V0.0.3`
- `V0.0.10`

公開済みTag／Releaseは移動、削除、上書きしない。

RC、alpha、betaなどの候補VersionはPlugin担当の裁量とする。ただし、Project本流へ導入候補として渡すAuthorityはStable `V0.0.x`とする。

---

## 3. 本Taskの目的

V0.0.1のProject本番サーバ受入と並行し、Plugin Repositoryで次を進める。

### Wayfarer_Core

- V0.0.1互換を維持する。
- Main／Frontierに必要な最小限のAPIを追加する。
- 必要な場合だけ追加Migrationを作成する。
- V0.0.1からV0.0.2へのUpgrade手順を示す。

### Wayfarer_Main

- Growth Pickaxeの初期Gameplayを、配布から成長、破損、修理まで一貫して利用できる状態にする。
- Main専用ArtifactとしてReleaseする。

### Wayfarer_Frontier

- Worlds Beyond向けの主要Foundationを実装する。
- Traversal Loadout、LeafGrapple連携、Navigation GUI基盤、Launchpad、Frontier WM Shopを実装する。
- Waystoneを後続Releaseで安全に追加できるPersistence／Domain境界を作る。

### Release

- Stable `V0.0.2`を公開する。
- 本流がProject Server受入に必要とする全成果物を提出する。

Plugin側ReleaseだけではRoadmap Order 10／11を完了としない。Project本番サーバで受入後に完了判定する。

---

## 4. 段階Releaseの区切り

### 4.1 V0.0.2の必須区切り

V0.0.2では、次を最低Release対象とする。

| Artifact／機能 | V0.0.2要求 |
|---|---|
| Wayfarer_Core | V0.0.1互換維持。Main／Frontierに必要な最小拡張 |
| Wayfarer_Main | Growth Pickaxeの一貫した初期Vertical Slice |
| Wayfarer_Frontier | `frontier_iris`境界、Traversal Loadout、LeafGrapple、Navigation GUI基盤、Launchpad、Shop |
| Waystone | 原則として後続Release。完成条件を満たせる場合はV0.0.2へ含めてもよい |
| EM–MVI Adapter | Order 13が`ADAPTER_REQUIRED`となるまで禁止 |

Wayfarer_Mainは、配布だけ、Progressだけ、Broken化だけなどの中途半端な状態をProduction-readyとしない。V0.0.2へ含める場合は、第8章のVertical Sliceをまとめて成立させる。

Wayfarer_Frontierでは、Waystone未実装中にWaystone Placement Toolを販売、付与、使用可能にしない。

### 4.2 V0.0.3推奨区切り

Waystoneは独立性と未決事項が多いため、次を一つのRelease単位としてV0.0.3へ分離することを推奨する。

- Physical Template
- Placement
- Founder／Maintainer／Sequence
- Lifecycle
- Maintain／Contest／Reactivate
- Discovery
- Discovery GUI
- Teleport GUI
- Safe Arrival
- Scheduler
- Restart Catch-up
- History
- Admin／Reconcile
- Waystone Placement Tool販売

V0.0.2作業中に未決事項が解消し、すべてのProduction条件を満たせる場合はV0.0.2へ含めてもよい。

### 4.3 V0.0.4以降の候補

- Main／FrontierのPlaytest Balance調整
- GUI／表示改善
- Resource Pack／Custom Model追加
- Waystone運用Feedback反映
- Ruined Frontier向けWayfarer固有Reward Foundation
- Project Runtime受入で判明した互換修正
- Order 13後の条件付きAdapter
- Server V0.1.0向け残機能

Version番号を事前予約しない。次の未使用`V0.0.x`へ割り当てる。

---

## 5. Plugin担当の裁量

Project Wayfarerの現行方針、責務境界、Gameplay結果、Authority、Decision Gateに反しない限り、次はPlugin担当へ委ねる。

- 内部Architecture
- Package／Class構成
- Domain分割
- Module追加／統合
- Design Pattern
- Library選択
- Repository実装
- Cache方式
- Migration内部構成
- GUI実装方式
- Command Tree内部実装
- Test Framework
- Test Consumer
- CI改善
- Observability
- Performance最適化
- Release Package構成
- 実装順序
- 追加Document
- 追加Test
- 追加Artifact

本要求書は最低機能と本流Handoff成果を指定する。追加実装は許容するが、次を満たすこと。

- Project方針に反しない。
- V0.0.2の必須機能を不必要に遅延させない。
- Artifact、Version、依存、Migration、試験、Known Limitationを報告する。
- Project Runtime受入待ちであることを維持する。

---

## 6. Authorityと固定境界

### 6.1 Authority優先順

1. Project Ownerの明示判断が反映された最新Repository文書
2. Project Wayfarer現行`docs/`およびRuntime Lock
3. 現行Server／Theme Concept
4. 現行Plugin Concept
5. Plugin Repositoryの現行Contract、ADR、Stable V0.0.1
6. 本要求書
7. Plugin担当の局所設計
8. 実装上の便宜

### 6.2 必須参照文書

Project Repository:

- `docs/06-acceptance-tests.md`
- `docs/09-roadmap.md`
- `docs/10-waymark-economy.md`
- `docs/12-permission-model.md`
- `docs/14-frontier-v0.1.0-scope.md`
- `docs/15-frontier-runtime-lock.md`
- `versions.yml`
- `plugin-manifest.yml`
- `concepts/plugins/Project_Wayfarer_Plugin_Concept_v0.0.3.md`
- `concepts/plugins/main/Project_Wayfarer_Growth_Tool_Concept_v0.0.5.md`
- `concepts/plugins/frontier/Project_Wayfarer_Worlds_Beyond_Plugin_Concept_v0.0.4.md`
- `concepts/plugins/frontier/Project_Wayfarer_Ruined_Frontier_Integration_Decision_Concept_v0.0.2.md`
- `concepts/frontier/Frontier_Server_Specification_V0.0.5.md`
- `concepts/frontier/Worlds_Beyond_Specification_V0.0.6.md`
- `concepts/frontier/Ruined_Frontier_Specification_V0.0.5.md`

Plugin Repository:

- `docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md`
- `docs/contracts/`
- `docs/architecture/`
- `docs/adr/`
- `docs/handoff/V0.0.1/`
- `docs/reports/`
- `libraries/`
- `integrations/`
- `plugins/`
- `gradle/libs.versions.toml`
- `.github/workflows/`

参照したCommit、Tag、Blob SHAをTest Reportに記録する。

### 6.3 Runtime配置

| Artifact | 配置 |
|---|---|
| Wayfarer_Core | Main＋Frontier |
| Wayfarer_Main | Main only |
| Wayfarer_Frontier | Frontier only |

Lobbyへ新規独自Pluginを追加しない。

### 6.4 依存方向

- `Wayfarer_Main`は`Wayfarer_Core API`へ依存する。
- `Wayfarer_Frontier`は`Wayfarer_Core API`へ依存する。
- `Wayfarer_Core`はMain／Frontierへ依存しない。
- MainとFrontierは相互依存しない。
- 循環依存を作らない。

### 6.5 Data Authority

| Domain | Authority |
|---|---|
| Wayfarer独自永続Data | MariaDB |
| Cache／Lock／PubSub／Message補助 | Redis |
| Frontier通常Player State | MVI |
| Waymark | Wayfarer_Coreの正式Provider／Transaction境界 |
| Block／Entity／Physical Structure | World Files |
| Runtime Event／Item／World mutation | Minecraft／Paper |

禁止事項:

- RedisEconomy内部Keyの直接操作
- MVI Databaseの直接更新
- mcMMO Databaseの直接更新
- EliteMobs Databaseの直接更新
- 通常InventoryのMariaDB保存
- MainとFrontier間のItem移送
- MVIによるMain／Frontier Backend間Profile管理
- CoreへのMain／Frontier固有Gameplay混在
- Unsupported内部APIへの推測依存

### 6.6 Threading

Main Thread専用:

- Player、Inventory、ItemStack
- Block、World、Chunk
- GUI、Teleport、Velocity
- Event cancellation
- Bukkit／Paper Objectの参照とmutation

非同期専用:

- JDBC
- Redis I/O
- Audit
- Query
- Checkpoint
- Expiration candidate search

Main Thread同期DB／Redis I/Oを行わない。

---

## 7. V0.0.1との互換要求

Project本流ではV0.0.1の本番受入を別Taskとして進める。Plugin担当はV0.0.2を並行実装してよい。

### 7.1 必須境界

- Project本番Serverへ接続しない。
- Project実DB、Redis、World、Player Data、Secretを開発へ持ち込まない。
- V0.0.1受入完了を仮定しない。
- V0.0.1で重大なCore不具合が見つかった場合は、V0.0.2へ反映し、本流へ影響を報告する。
- V0.0.1の既存Migrationを変更しない。
- V0.0.1 Tag、Release、Artifactを上書きしない。
- V0.0.2はV0.0.1からのUpgradeを明示的に扱う。

### 7.2 Core API

- V0.0.1 Public APIを原則維持する。
- Main／Frontierに必要なAPIは互換的に追加する。
- Binary／Source compatibilityを破る変更はOwner判断を要求する。
- Bukkit Object、JDBC Connection、Hikari、Flyway、LettuceをPublic APIへ公開しない。
- API Classの二重内包を防ぐ。

### 7.3 Core Migration

- V0.0.1で適用済みのMigrationを変更しない。
- 新規Core Migrationは、既存番号の次から追加する。
- 必要がなければCore Migrationを増やさない。

### 7.4 Waymark制約

Vault／RedisEconomy経由の成功は、Providerが操作を受け入れたことまでを意味する。次は保証しない。

- Durable Redis completion
- Provider effect lookup
- External atomic operation identity
- Unconditional exactly-once

曖昧な結果は`UNKNOWN`とする。

- Balance差分を成功証明に使わない。
- `UNKNOWN`を自動再実行しない。
- Wayfarer専用Side Channelを追加しない。
- 共有EconomyのDeferred Design Itemとして維持する。

### 7.5 Compatibility Matrix

Release時に次を明示する。

- Core Artifact Version
- Main／Frontierが要求するCore Version範囲
- V0.0.1からのUpgrade可否
- API変更
- Core Migration
- Config変更
- Rollback／Downgrade制約

全Artifactを0.0.2へ揃えるか、Core V0.0.1を再利用するかはPlugin担当の裁量とする。

---

## 8. Wayfarer_Main V0.0.2要求

### 8.1 配置とLifecycle

- Main BackendだけでEnableする。
- Wayfarer_CoreをHard Dependencyとする。
- Frontier／LobbyでEnableしない。
- Config、Core Service、Schema互換性を確認してからListener／Commandを登録する。
- 必須依存失敗時はFail-closedする。
- Disable時に新規処理を停止し、Dirty SessionをTimeout付きでFlushする。

### 8.2 論理Growth Pickaxe

MariaDB上の論理Toolを正本とする。

初期Tool Typeは`PICKAXE`とする。

状態:

- Tool Status: `ACTIVE`、`BROKEN`、`REVOKED`
- Delivery Status: `DELIVERED`、`PENDING`
- Active Branch: `FORTUNE`、`SILK_TOUCH`

最低Field:

- `tool_id`
- `owner_uuid`
- `tool_type`
- `instance_epoch`
- `cumulative_progress_units`
- `active_branch`
- `tool_status`
- `delivery_status`
- `stored_damage`
- `schema_version`
- `lock_version`
- `created_at`
- `updated_at`
- `last_checkpoint_at`

Unique Constraintは`owner_uuid + tool_type`とし、Playerごとに論理Pickaxeを1本だけ持つ。

### 8.3 Physical Item Identity

最低PDC:

- `wayfarer:item_type`
- `wayfarer:item_instance_id`
- `wayfarer:tool_id`
- `wayfarer:owner_uuid`
- `wayfarer:tool_type`
- `wayfarer:instance_epoch`
- `wayfarer:schema_version`
- `wayfarer:display_revision`

要件:

- Lore、Name、MaterialだけでIdentityを判定しない。
- Owner、Tool ID、Epoch、SchemaをDB／Sessionと照合する。
- Unknown Schema／Type、不正UUID、旧EpochをFail-closedする。
- Reissue時にEpochを増加し、旧Physical Itemを無効化する。

### 8.4 初回非同期配布

Main Join時のFlow:

1. `PlayerJoinEvent`では非同期処理を登録する。
2. MariaDBからRecordを取得する。
3. RecordがなければUnique Constraintを用いてRace-safeに作成する。
4. PlayerがOnlineであることをMain Threadで再確認する。
5. ItemをMain Threadで付与する。
6. Auditを記録する。

要件:

- Main Thread DB I/Oなし。
- RecordなしPlayerへ一度だけ配布する。
- Plugin導入前から存在するPlayerも対象。
- DB処理中Logout時は付与しない。
- Inventory Full時にDropしない。
- Pending Deliveryとして保持する。
- Playerへ通知する。
- Console／Auditへ理由を記録する。
- 次回JoinまたはAdmin操作で再試行できる。
- `DELIVERED` Recordを自動Reissueしない。

### 8.5 Owner Bind

Phase 10C-A Revision BのOwner Amendmentでは、物理移動と使用権限を分離する。

物理的に許可する操作:

- Ownerによる手動Drop／Pickup
- Owner以外のPlayerが物理的に拾うこと
- 通常Inventoryおよび通常Containerへの移動
- Death Dropとしての残存
- 旧EpochまたはOwner不一致Itemの物理的残存

上記は使用権限を付与しない。Owner、Tool ID、Instance、Epoch、Schema、Statusの
Hold-time authorizationが有効な場合だけ、Ownerの使用操作を許可する。

使用またはIdentity変更として拒否する操作:

- Owner以外または旧EpochのBreak／Progress／GUI／Repair／Branch／debug
- Anvil、Grindstone、Smithing、Crafting修理、同種Tool合成
- Mendingおよび対応可能な外部修理
- Item Frame／Armor Stand

DeathではDeliveryを再開せず、Replacement／Rotationを行わず、Delivery Statusを変更せず、
WaymarkをDebitしない。Paid ReissueとPending DeliveryのFree Retryは別のFlowとして維持する。

Hold-time authorizationは非同期DB結果をMain-thread Session Cacheへ反映する。Main Hand、
authority availability/change、またはidentity rewriteの境界で完全比較とCache invalidationを
行い、通常の使用EventではPDC／DBの完全比較を繰り返さない。Cacheが不在、期限切れ、または
authority unavailableならFail-closedする。

#### 8.5.1 Delivery notification and presentation

物理Inventoryへの挿入とdurableな`DELIVERED` commitの両方が完了した後だけ、次の一度限りの
success messageを表示する。

`[Wayfarer] Growth Tool「Wayfarer Growth Pickaxe」を受け取りました。`

Present、Already Present、Pending、Full、Offline、Wrong、Conflict、Unknownなど、物理挿入と
durable commitが成立しない結果ではsuccess messageを表示しない。表示名はidentityの代替ではない。
Growth Tool名は`Wayfarer Growth Pickaxe`、Broken Tool名は`Broken Wayfarer Growth Pickaxe`とする。

全Eventを個別に網羅試験する必要はない。代表操作と共通Guardの構造確認で判定する。

### 8.6 Progress World

完全一致Allowlist:

- `resource`
- `resource_nether`
- `resource_end`

対象外:

- `main`
- `main_nether`
- `main_the_end`
- unknown worlds

Prefix、部分一致、未知Worldの自動採用を行わない。

### 8.7 Progress Event

Progress対象条件:

- `minecraft:mineable/pickaxe`
- Main Handの正規Growth Pickaxe
- Owner、Tool ID、Epoch一致
- Tool Statusが`ACTIVE`
- Cancelされていない成功した`BlockBreakEvent`
- Survival
- Adventureで実際にBlock破壊が成立した場合

Progress対象:

- 自然生成Block
- Player設置Block
- Generator生成Block
- Cobblestone／Stone／Basalt Generator
- Silk Touchで回収し再設置したOre
- Plugin生成BlockをPlayerが通常破壊した場合

Progress対象外:

- Creative
- Spectator
- CancelされたBreak
- Explosion
- Piston
- WorldEdit／FAWE
- Command
- Plugin直接削除
- その他Player Break Eventではない除去

一つの成功Breakにつき一度だけ加算する。

### 8.8 Progress内部表現

- `1.000 Progress = 1000 internal units`
- 整数で保持し、小数誤差を避ける。
- Overflow接近時は加算をFail-closedし、Critical Log／Auditを記録する。

### 8.9 初期Weight Baseline

値はConfig Defaultであり、Playtestで調整可能とする。

| Block／Category | Progress |
|---|---:|
| Cobblestone | 0.25 |
| Cobbled Deepslate | 0.35 |
| Stone／Granite／Diorite／Andesite／Tuff／Calcite | 1.00 |
| Netherrack／Blackstone／Basalt | 1.00 |
| Deepslate／End Stone | 1.25 |
| Obsidian／Crying Obsidian | 2.00 |
| 未定義Pickaxe Tag Block | 1.00 |

Ore倍率:

| Ore Group | Multiplier |
|---|---:|
| Coal／Nether Quartz | 1.50 |
| Copper | 1.60 |
| Redstone | 1.75 |
| Iron／Nether Gold | 2.00 |
| Lapis | 2.10 |
| Gold | 2.50 |
| Diamond | 3.50 |
| Emerald／Ancient Debris | 4.00 |

Player設置Oreを許可するため、極端な倍率にしない。

### 8.10 Evolution

Material Evolution:

- Wood
- Stone
- Iron
- Diamond

初期累積Threshold:

| Result | Cumulative Progress |
|---|---:|
| Stone | 100 |
| Iron | 400 |
| Diamond | 1200 |

Diamond後の第`n`回Enchant Evolution増分は、初期値として`800 + 200n + 40n²`を使用する。

Cycle:

1. Efficiency
2. Unbreaking
3. Efficiency
4. Unbreaking
5. Fortune
6. Repeat

Effective Cap:

| Enchantment | Cap |
|---|---:|
| Efficiency | 10 |
| Unbreaking | 10 |
| Fortune | 5 |
| Silk Touch | 1 |

Cap到達後もConceptual Level、Progress、Evolution Countは増加する。

Default Branchは`FORTUNE`とする。AdminはFortune／Silk Touchを切り替えられる。一般Player向け有料切替はV0.0.2対象外とする。

### 8.11 Threshold Engine／Config Reconciliation

- 累積Thresholdを非再帰Loopで生成する。
- 必要範囲まで遅延拡張してよい。
- Binary SearchでEvolution Countを求める。
- Config RevisionごとにImmutable Cacheを持つ。
- Tool使用ごとにDBへ問い合わせない。
- Config変更でMaterial／Enchant／Evolutionを完全再計算する。
- 累計Progressは変更しない。
- Configによる昇格／降格を許容する。
- Config Reconcileだけでは修理しない。
- ACTIVE Toolは残存耐久率を維持する。
- Material換算後のACTIVE耐久は最低1とする。
- BROKENはBROKENを維持する。
- Unsafe partial reloadを行わない。
- 実際のProgress増加でEvolution Countが上昇した場合だけ最大耐久まで回復する。

### 8.12 Broken Tool

耐久0によるItem消滅前にInterceptする。

- Material: `GRAY_DYE`
- Status: `BROKEN`

保持する情報:

- Tool ID
- Owner
- Tool Type
- Epoch
- Progress
- Branch
- Schema

BROKEN状態:

- Pickaxe利用不可
- Progressなし
- 外部修理不可
- Owner Bind維持
- GUI起動可能
- Restart後もBROKEN
- BROKEN化は重要状態として即時Checkpoint

### 8.13 統合GUI

起動条件:

- Main HandにGrowth ToolまたはBroken Tool
- 空中への右Click
- Block／EntityをTargetにしていない
- Off Handでは開かない

表示:

- Tool Type
- Status
- Material
- Evolution Count
- Cumulative Progress
- 次Thresholdまで
- Enchantment
- Branch
- Durability
- Repair Cost Preview
- Config Clamp

操作:

- Repair
- Help／Status

Repair Flow:

1. Main GUI
2. Repair Preview
3. Confirm／Cancel

二重Click、Disconnect、Lagで二重請求しない。

GUI Layout、Slot、Item Name／Loreは未決事項としてPlugin担当案をOwnerと確認する。

### 8.14 Waymark Full Repair

全回復のみを提供する。

Full Repair Base:

`ceil(100 × (1 + evolution_count × 0.08))`

ACTIVE Repair:

`ceil(full_repair_cost × max(0.25, missing_durability_ratio))`

最大耐久時は修理不可、0 WMとする。

BROKEN Repair:

`full_repair_cost + 100 + evolution_count × 5`

Core Transactionを使用し、次を満たす。

- Transaction ID
- Idempotency
- Player／Tool Lock
- 二重Debitなし
- 二重Refundなし
- Item／DB失敗時Refund
- Refund不明時`UNKNOWN`
- `UNKNOWN`を自動完了しない
- Admin Reconcile
- Audit

価格は初期Defaultであり、Playtest調整可能とする。

### 8.15 Session／Checkpoint

最低Checkpoint Timing:

- 5分周期
- Evolution
- BROKEN
- Repair
- Reissue
- Admin操作
- Quit
- Disable

要件:

- 通常ProgressはSession Cacheへ加算する。
- Evolution、Broken、RepairなどのCritical Stateは即時保存する。
- Main Thread DB I/Oなし。
- 同一Player処理を直列化する。
- Quit／DisableはTimeout付きAsync Flush。
- Disable後Callbackを拒否する。
- Crash時に最大Checkpoint間隔分の通常Progressを失う可能性をKnown Limitationへ記載する。

### 8.16 Admin／Debug

最低Admin機能:

- inspect
- grant
- reissue
- repair
- branch
- revoke
- reconcile
- pending delivery retry

Debug候補:

- progress-next
- durability-one
- repair-free

DebugはDefault disabledとし、Admin PermissionとConfig Enableの両方を要求する。

Exact Command SyntaxとPermission NodeはPlugin担当が提案し、Release Handoffで固定する。

### 8.17 V0.0.2対象外

- Axe
- Shovel
- Player有料Fortune／Silk切替
- Netherite Upgrade
- Ranking
- Evolution Reward
- Ability
- Cosmetic
- Cross-server Tool利用
- Vanilla Tool禁止

---

## 9. Wayfarer_Frontier V0.0.2要求

### 9.1 配置とWorld境界

- Frontier BackendだけでEnableする。
- Wayfarer_CoreをHard Dependencyとする。
- Main／Lobbyへ配置しない。
- Worlds Beyond対象Worldを完全一致で判定する。

対象World:

- `frontier_iris`

対象外:

- `frontier_iris_nether`
- `frontier_iris_the_end`
- すべてのNether World
- すべてのEnd World
- Unknown World

対象外Worldで次をFail-closedする。

- 初回Loadout
- Traversal Item使用
- Navigation操作
- Launchpad
- Shop
- 将来Waystone

Prefix、部分一致、Environment推測を使用しない。PluginはWorldを生成しない。

### 9.2 MVI境界

MVI Groupの正本:

- `neutral`
- `worlds_beyond`
- `guild`

`frontier_iris`は`worlds_beyond`だけに属する。

Wayfarer_Frontierは次を行わない。

- Inventory保存／復元
- Armor／Offhand／Ender Chest保存
- XP／Health／Food保存
- MVI Profile切替
- Gate／Respawn／Reconnectの二重切替
- Main BackendとのProfile共有

Wayfarer_Frontierは、自身のItem Identity、Pending Delivery、Launchpad、Shop、Waystone Domainだけを管理する。

### 9.3 Traversal Loadout

`frontier_iris`への初回入場時だけ支給する。

恒久Item:

| Item | Initial |
|---|---|
| Elytra | 1、Unbreakable、Soulbound |
| Grappling Hook | 1、LeafGrapple正規Item、耐久無効、Soulbound |
| Navigation Item | 1、GUI入口、Soulbound |

消耗Item:

| Item | Initial |
|---|---|
| Launchpad | 初回のみ2 |

恒久Item要件:

- Owner以外の使用不可
- 物理Drop／Pickup／通常Container移動は許可。ただし使用権限は付与しない
- Theme外使用拒否
- Death Dropとして残存
- Respawn後保持
- Epoch Reissue
- 旧Instance失効
- MariaDB＋PDC Identity
- System Fault時の安全な復旧

Launchpadは消耗品であり、恒久Itemの無料Reissue規則を適用しない。

### 9.4 初回配布／Pending Delivery

Flow:

1. `frontier_iris`初回入場を検出する。
2. 非同期でRecordを取得または作成する。
3. PlayerがOnlineかつ`frontier_iris`内であることを再確認する。
4. Main ThreadでItemを付与する。
5. Auditを記録する。

Inventory Full:

- ItemをDropしない。
- 未配達分だけPendingへ保持する。
- Playerへ通知する。
- Console／Auditへ記録する。
- 次回安全入場またはAdmin Retryで再試行する。

初回Launchpadは配達済み後の紛失、死亡、設置、使用完了を理由に再支給しない。

#### 9.4.1 Phase 10C-A readiness amendment

- Safe Entryの観測は最大40回のbounded pollとし、同一Fingerprintの連続2回をReady条件とする。
- Required Item数が0でも、1回の観測だけでReadyにせず、同一Fingerprintの2回を要求する。
- MVIの遅延public eventは、同一のexternal entry cycleに限りtimeout後に一度だけ再開できる。
- timeout後の再帰、repeating task、無制限retry、および新しいexternal cycleの暗黙生成を行わない。
- self-heal対象はexact-current identityのElytra、Grappling Hook、Navigationだけとする。
- Launchpad、Rocket、通常Item、lookalike、未完全PDCはcleanup対象にしない。
- 表示名はそれぞれ`Beyond Wayfarer Elytra`、`Beyond Wayfarer Grappling Hook`、
  `Beyond Wayfarer Navigation`とする。

### 9.5 Elytra

- Unbreakable
- Owner Bind
- `frontier_iris`内だけ使用可能
- 他Player装備不可
- Death Drop除外
- Reissueで旧Instance失効
- Hook／Launchpadから自然に滑空へ移行可能

### 9.6 LeafGrapple Adapter

採用VersionはLeafGrapple `1.0.2`とする。

Adapter責務:

- 正規Hook Item生成
- Version／Capability確認
- Owner／Theme Identity
- 耐久無効
- Soulbound
- Theme外使用拒否
- Entity／Player Hook無効確認
- 連携不能時Fail-closed

禁止:

- Hook Projectile再実装
- Pull Physics再実装
- Cooldown計算再実装
- Unsupported内部APIをDomain本体へ漏らす

非公開Class依存が必要ならVersion Adapterへ隔離し、Known Limitationとして記録する。

### 9.7 Navigation GUI基盤

Navigation Itemから開く。

V0.0.2最低入口:

- Shop
- Loadout
- Help

Waystone未導入時のDiscovery／Teleport:

- 非表示、または明確なUnavailable表示
- 未実装操作を成功扱いしない

WaystoneをV0.0.2へ含めた場合だけDiscovery／Teleportを有効化する。

`frontier_iris`外では全操作を拒否する。

### 9.8 Launchpad Item

最低Identity:

- `item_type=LAUNCHPAD`
- `item_instance_id`
- `definition_id`
- `schema_version`

要件:

- 未設置Itemへremaining usesを保存しない。
- 通常Theme Inventory ItemとしてDeath Drop対象。
- 配置成功時だけ1個消費。
- 配置失敗時は消費しない。
- 配置後にItemへ戻さない。

### 9.9 Launchpad初期Balance

| Parameter | Initial Value |
|---|---:|
| Shop Price | 30 WM |
| Amount | 1 |
| Initial Free | 2 |
| Max Successful Uses | 3 |
| Expiration | 30 days without successful use |
| Horizontal Velocity | 2.5 |
| Vertical Velocity | 1.2 |
| Cooldown | 2 seconds |
| Auto Elytra | true |
| Max Active per Player | 0（上限なし） |

値はConfig Defaultであり、Playtest調整可能とする。

### 9.10 Launchpad設置

初期Physical Block:

- `LIGHT_WEIGHTED_PRESSURE_PLATE`
- 単一Block
- 設置時Player yawを射出方向として保存

最低条件:

- Worldが`frontier_iris`
- Solid Block上面
- 対象位置がAir
- Air以外を上書きしない
- Liquid外
- Portal／Gate／Spawn除外
- World Border内
- WorldGuard禁止Region外
- Waystone／System Structureと非重複
- 既存Launchpadと非重複
- Chunk Load済み
- 成功時だけItem消費

Item、DB Record、Physical Blockの三者を補償可能にする。

無条件のExactly-onceを主張しない。二重消費、Item損失、DBだけ残留、Blockだけ残留を検出し、Reconcile可能にする。

### 9.11 Launchpad利用

- 金の感圧板へ乗ると起動する。
- Sneak中は起動しない。
- 公共利用とする。
- 成功射出だけUse Countを加算する。
- 同一Launchpadの同時利用をLockする。
- Cooldownを持つ。
- Safe Launchを確認する。
- Elytraを自動展開する。
- Block埋まりを防ぐ。
- 成功時に`last_used_at`を更新する。
- 成功時に期限を延長する。
- Max Uses到達時に削除する。
- `frontier_iris`以外では利用不可。

### 9.12 Launchpad手動破壊

任意Playerの通常Block Breakを許可する。

- Owner限定にしない。
- Item Dropなし。
- Block削除。
- Active Row削除。
- Audit。
- CancelされたBreakでは削除しない。
- 二重Breakで二重削除しない。

### 9.13 Launchpad環境保護

最低限、共通Guardまたは採用Plugin Hookにより次から保護する。

- Explosion
- Fire／Burn
- Fluid
- Piston
- Entity Change Block
- Falling Block
- Block Spread
- Tree／Mushroom Growth
- Structure Generation
- 通常WorldEdit／FAWE
- Mob Griefing

全外部Plugin操作を網羅再現する必要はない。主要Bukkit Eventの代表試験とWorldEdit／FAWE連携方式の構造確認で判定する。

### 9.14 Launchpad期限／Reconcile

Active Record最低候補:

- `launchpad_id`
- `world_id`
- `x`
- `y`
- `z`
- `orientation`
- `placer_uuid`
- `successful_use_count`
- `max_uses_at_creation`
- `created_at`
- `last_used_at`
- `expires_at`
- `definition_id`
- `state`
- `schema_version`
- `lock_version`

期限基準:

- 一度以上利用済み: `last_used_at`
- 未利用: `created_at`

削除条件:

- Max Uses
- Expiration
- Player Break
- Admin操作
- Reconcile

Scheduler要件:

- 非同期で候補を検索する。
- Main ThreadでBlockを操作する。
- Restart Catch-upを行う。
- DBありBlockなしを検出する。
- BlockありDBなしを検出する。
- 削除をIdempotentにする。
- 不確実時は自動Item再発行せず、Audit／Admin Reconcileへ送る。

### 9.15 Frontier WM Shop

V0.0.2販売品:

| Item | Price |
|---|---:|
| Launchpad ×1 | 30 WM |
| Flight Duration 3 Firework Rocket ×1 | 200 WM |

Waystone未実装時:

- Waystone Placement Toolを非表示またはDisabledにする。
- 購入要求をDebit前に拒否する。

Shop要件:

- `frontier_iris`内だけ。
- Core Waymark Adapter経由。
- Transaction ID。
- Idempotency。
- 二重Click防止。
- Inventory Full対応。
- Item付与失敗時RefundまたはPending Delivery。
- Audit。
- RedisEconomy内部Key非使用。
- `UNKNOWN`を成功扱いしない。

RocketはFlight Duration 3、Explosionなしとする。

### 9.16 Frontier Admin／Reconcile

最低機能:

- loadout inspect
- loadout reissue
- delivery inspect／retry
- launchpad inspect
- launchpad remove
- launchpad reconcile
- transaction inspect
- audit reference

Exact Syntax／PermissionはPlugin担当が提案し、Handoffで固定する。

### 9.17 `wf_frontier_*` Persistence Foundation

V0.0.2最低Domain:

- Traversal logical items
- Initial delivery
- Pending delivery
- Launchpad active state
- Launchpad history／audit reference
- Shop pending delivery
- 必要なplacement transaction
- Waystone後続Migrationを安全に追加できる境界

通常Inventoryを保存しない。

### 9.18 V0.0.2対象外

- Waystone Full implementation
- Discovery GUI Full behavior
- Teleport GUI Full behavior
- Waystone Placement Tool販売
- Ruined Frontier Gameplay
- EliteMobs Gameplay
- MVI Profile実装
- Gate実装
- Portal deny実装
- Iris World生成
- Frontier Resource Pack build／delivery
- EliteMobs–MVI Adapter

---

## 10. Ruined Frontierと条件付きAdapterの境界

V0.0.2では次を実装しない。

- EliteMobs Instance lifecycle
- Dungeon／Boss／Loot
- MVI Profile切替
- World生成／削除
- Guild Gate
- Main／Frontier Item移送
- EliteMobs内部DB／Config変更

`Wayfarer_Frontier_EliteMobsMVI`は、Order 13が`ADAPTER_REQUIRED`を返すまで作成、Releaseしない。

Decision順:

1. Static MVI registration
2. Strict approved-Blueprint Regex
3. Adapter

---

## 11. Migration要求

### 11.1 所有Prefix

| Module | Table Prefix |
|---|---|
| Core | `wf_core_*` |
| Main | `wf_main_*` |
| Frontier | `wf_frontier_*` |

各Pluginは他ScopeのTableを作成、更新しない。

### 11.2 Migration原則

- ModuleごとにMigration Locationを分離する。
- Main BackendはCore＋Main Migrationを適用する。
- Frontier BackendはCore＋Frontier Migrationを適用する。
- Empty DBから適用可能。
- V0.0.1既存DBからUpgrade可能。
- Forward-only。
- 適用済みMigration書換え禁止。
- UUID、UTC、Unique Constraint、Index、Optimistic Lock、JSON Validationを維持する。
- Project実DBへPlugin担当が適用しない。
- Project受入前にBackup対象とRestore要件を報告する。

### 11.3 Pre-release Data

Plugin V0.1.0前のGrowth Tool／Frontier DataをResetするかPreserveするかは未決であり、Project Order 25のOwner判断対象である。

V0.0.2は自動Resetを実行しない。

---

## 12. Waystone後続要求

WaystoneをV0.0.2へ含めない場合も、Architectureを阻害しない。

### 12.1 Lifecycle

| State | Initial Period | Teleport |
|---|---:|---|
| PROTECTED | 14 days | 可 |
| CONTESTABLE | 14 days | 可 |
| DORMANT | 180 days | 不可 |
| RUINED | Permanent history | 不可 |

RUINEDではPhysical Structureを削除し、Historyは保持する。

### 12.2 Identity／Name

- Founderは不変。
- Maintainerは更新可能。
- Name形式は`<Founder Name at Creation>'s <Biome Display Name> #<Sequence>`。
- Sequenceは`Founder UUID × Biome Key`ごとに採番する。
- Sequenceを再利用しない。

### 12.3 Cost

| Operation | Cost |
|---|---:|
| Placement Tool | 600 WM |
| Maintain | 200 WM |
| Contest | 300 WM |
| Reactivate | 150 WM |
| Teleport | 0 WM |

Maintain／Contest／Reactivateは現地Interactionだけを許可する。

### 12.4 Placement

初期値:

| Parameter | Value |
|---|---:|
| Minimum horizontal distance | 1000 |
| Footprint | 5×5 |
| Foundation tolerance | 1 |
| World border margin | 32 |
| Gate／Spawn exclusion | 256 |

要件:

- `frontier_iris`だけ。
- Active Waystoneから水平1000 Block以上。
- RUINEDは距離判定対象外。
- Airだけを置換。
- Solid Foundation。
- Liquid外。
- Gate／Spawn／Portal／禁止Region外。
- Launchpad／System Structure非重複。
- 成功時だけPlacement Toolを消費する。

### 12.5 Discovery

- 現地Interactionで解禁する。
- Founderは設置成功時にDiscovery済みとする。
- GUI表示だけでDiscoveryを解禁しない。
- DORMANTでもDiscovery履歴を保持する。
- RUINED後もDB Historyを保持する。
- 未発見WaystoneへTeleport不可。

初期値:

| Parameter | Value |
|---|---:|
| Search Radius | 3000 |
| Search Cooldown | 30 seconds |
| Sort | Distance ascending |

### 12.6 Teleport

初期値:

| Parameter | Value |
|---|---:|
| Cost | 0 WM |
| Cooldown | 30 seconds |
| Warmup | 3 seconds |
| Cancel on move | true |
| Cancel on damage | true |
| Cross dimension | false |

要件:

- Current／Destinationがともに`frontier_iris`。
- 発見済み。
- Teleport可能State。
- Permission。
- Cooldown。
- Combat／Fall／Vehicle／Portal移動中ではない。
- Chunk Load。
- Safe Arrival。
- 実行直前に再検証。

### 12.7 Waystone ProductionをBlockする未決事項

- 最終5×5 Template
- Block Palette
- Core／Interaction Block
- Maintainer Head位置
- Safe Arrival位置
- Rotation
- Template ID／Metadata
- WorldEdit／FAWE保護方式
- WorldGuard重複検査方式
- Particle／Light表現
- GUI Layout／Icon／表示言語
- History表示範囲
- Exact DDL
- Scheduler間隔
- Structure破損時の自動修復範囲

未決のまま推測したPhysical TemplateをProduction Authorityにしない。Test Fixture用Templateは明示的に非Productionとする。

---

## 13. 軽量検証方針

各確認は、要件を判定できるだけの代表的な操作を行う。

- 正常結果が明確: そこで終了する。
- 結果が曖昧: 判断に必要な最小限だけ追加確認する。
- 明確な不具合: 同じ試験を繰り返さず、影響と発生頻度を評価して修正判断する。
- 低頻度かつ軽微: 追加確認しない。
- 低頻度だが重大: Fail-safeの構造確認と代表試験に留める。
- 高頻度かつ重大: 原因と修正結果を十分に確認する。

### 13.1 適用原則

- 全Commandを一つずつ試さない。
- 全Config組合せを試さない。
- 全Owner Bind Eventを機械的に反復しない。
- 全Crash WindowをPaper Runtimeで再現しない。
- Unit／Integration Testで明確な項目をRuntimeで重複検証しない。
- 一度明確に成功した正常系を繰り返さない。
- 不具合発見時は再現回数より、頻度、重大性、Fail-safeを評価する。
- Test件数やAcceptance Unit数を目的化しない。

### 13.2 重点確認

重大性が高いため、代表試験と構造確認を行う。

- Item重複
- 二重Debit／Refund
- Main Thread DB／Redis I/O
- Migration
- Owner／Epoch失効
- Broken Tool消滅
- Inventory Full時Drop
- Launchpad二重消費
- Launchpad DB／Block不整合
- Theme外使用
- Cross-backend Item漏出
- Permission／Secret
- Disable／Restart後のStale Callback

---

## 14. Plugin側代表試験

以下は最低限の代表観点であり、網羅リストではない。

### 14.1 Build／Packaging

- Clean `check`
- Clean `assemble`
- Java 25
- API二重内包なし
- Core／Main／Frontier依存方向
- JARにSecret／Test Data／World Dataなし
- Artifact Version／SHA生成
- V0.0.1→V0.0.2 compatibility

### 14.2 Wayfarer_Main

配布:

- RecordなしPlayerへ1本。
- 再Joinで重複なし。
- Inventory FullでDropなし。
- Logout中Late Deliveryなし。

Identity:

- Owner利用可能。
- 非Ownerの代表操作拒否。
- Reissue後旧Epoch拒否。
- Restart後Identity維持。

Progress:

- `resource`で代表Blockが加算。
- `main`で非加算。
- Player設置Oreが加算。
- CreativeまたはCancelで非加算。
- 非Player除去で非加算。

Evolution:

- Wood→Stone→Iron→Diamond。
- Diamond後Cycleの代表1周。
- Config ReconcileでProgress維持。
- Reconcileだけで全回復しない。
- 実Progress Evolutionで全回復。

Broken／Repair:

- 消滅前にGRAY_DYE。
- BROKENでProgressなし。
- Repairで復元。
- Idempotent Replayで二重Debitなし。
- UNKNOWNを自動完了しない。

Thread／Restart:

- Join／BreakでMain Thread DB I/Oなし。
- Checkpoint／Restartで状態維持。
- Disable後Callbackなし。

### 14.3 Wayfarer_Frontier

World Boundary:

- `frontier_iris`で有効。
- 類似名または未知Worldで拒否。
- Nether／Endで拒否。

Loadout:

- 初回入場で恒久3Item＋Launchpad 2。
- 再入場で重複なし。
- Inventory FullでDropなし。
- Owner／Theme外代表操作拒否。
- Reissue後旧Epoch拒否。

LeafGrapple:

- 正規Hook生成。
- Theme内代表Hook動作。
- Theme外拒否。
- Entity／Player Hook無効。
- 連携不能時Fail-closed。

Launchpad:

- 成功設置で一度だけ消費。
- 失敗設置で非消費。
- Step-on射出。
- Sneakで無効。
- 3成功で削除。
- 任意Player Breakで非Drop。
- 代表Explosion／Piston保護。
- Restart後Record／Block整合。
- DB／Block不整合の代表Reconcile。

Shop:

- Launchpad購入。
- Rocket購入。
- Insufficient funds。
- Idempotent Replay。
- Inventory Full時の補償。
- Waystone未実装時にPlacement Tool販売なし。

MVI／Boundary:

- PluginがMVI Profileを変更しないことをCode／Dependency境界で確認する。
- 完全なMVI切替、Gate、Backend Item隔離はProject側Order 12／16で確認する。

---

## 15. Decision Register

本章の未決事項は、Plugin担当が推測でProject決定へ昇格させない。OwnerとPlugin担当で相談し、DecisionをRepositoryへ記録する。

### 15.1 Main

| ID | 状態 | Question | V0.0.2対応 |
|---|---|---|---|
| MAIN-D01 | TUNABLE | Exact Block Weight／Ore倍率 | 現行Baselineで実装可 |
| MAIN-D02 | TUNABLE | Threshold係数 | 現行Baselineで実装可 |
| MAIN-D03 | TUNABLE | Repair価格 | 現行Baselineで実装可 |
| MAIN-D04 | UNRESOLVED | GUI Layout／Slot／表示言語 | Plugin担当案をOwner確認 |
| MAIN-D05 | UNRESOLVED | Item Name／Lore | Plugin担当案をOwner確認 |
| MAIN-D06 | UNRESOLVED | Pending Delivery Player UI | 安全なText通知で暫定可 |
| MAIN-D07 | UNRESOLVED | Exact Admin Command／Permission | Release前に固定 |
| MAIN-D08 | UNRESOLVED | 外部Repair遮断対象Plugin | 共通Guard＋確認済みPluginで開始可 |
| MAIN-D09 | DEFERRED | Netherite Upgrade時期／価格 | V0.0.2対象外 |
| MAIN-D10 | PROJECT-OWNED | Pre-release Data Reset／Preserve | Order 25まで自動Reset禁止 |

### 15.2 Frontier

| ID | 状態 | Question | V0.0.2対応 |
|---|---|---|---|
| FRONT-D01 | UNRESOLVED | `frontier_iris`未存在時のDisable／Degraded方針 | Fail-closed案を提示 |
| FRONT-D02 | UNRESOLVED | LeafGrapple Exact API／Class境界 | 1.0.2調査後に固定 |
| FRONT-D03 | UNRESOLVED | Launchpad Config Snapshot範囲 | Max Uses等のCreation Snapshotを推奨 |
| FRONT-D04 | UNRESOLVED | WorldGuard／WorldEdit／FAWE保護Hook | 調査結果を記録 |
| FRONT-D05 | UNRESOLVED | Navigation GUI Layout／表示言語 | Plugin担当案をOwner確認 |
| FRONT-D06 | UNRESOLVED | Shop Pending Delivery内部表現 | Core Transactionと整合する案を提示 |
| FRONT-D07 | PROJECT-OWNED | `frontier_iris` Seed／Border／生成 | PluginはWorld生成しない |
| FRONT-D08 | PROJECT-OWNED | Portal denyの具体方式 | Project後続Order |
| FRONT-D09 | PROJECT-OWNED | Gate座標／Safe Arrival | Project Order 17 |
| FRONT-D10 | PROJECT-OWNED | MVI Runtime Config | Project Order 12／16 |
| FRONT-D11 | BLOCKS WAYSTONE | Waystone Template／Palette | Waystone ProductionをBlock |
| FRONT-D12 | BLOCKS WAYSTONE | Waystone Safe Arrival／Interaction位置 | Waystone ProductionをBlock |
| FRONT-D13 | OPTIONAL | Resource Pack Asset／Model | Custom ModelなしでV0.0.2可 |
| FRONT-D14 | DEFERRED | Ruined WM Reward Domain | 後続候補 |
| FRONT-D15 | DECISION-GATED | EliteMobs–MVI Adapter | Order 13まで禁止 |

Decision記録には、Question、Authority、Options、推奨、理由、Gameplay影響、Data／Migration影響、Compatibility、Rollback、Owner判断、対象Versionを含める。

---

## 16. Stop／Escalation条件

次の場合は推測で進めず停止する。

- Project正本とConcept／Plugin仕様でGameplay結果が矛盾する。
- V0.0.1 Core Public APIを破壊する必要がある。
- V0.0.1 Migrationを書き換える必要がある。
- MainとFrontierの相互依存が必要になる。
- CoreがMain／Frontier Gameplayを所有する必要がある。
- 通常InventoryをMariaDBへ保存する必要がある。
- MVI／mcMMO／EliteMobs／RedisEconomy内部Dataへ直接アクセスする必要がある。
- Main Thread同期DB／Redis I/O以外に実現方法がない。
- 二重課金、Item重複、Data Lossが高頻度で発生する。
- LeafGrapple 1.0.2と安全に連携できない。
- Waystone Templateを推測しないとProduction実装できない。
- 条件未成立Adapterが必要になる。
- Project Runtimeへの変更が必要になる。
- Destructive Migration／Data削除が必要になる。
- Secret／Runtime DataをCommitする必要がある。

低頻度かつ軽微な不具合は、必要以上に調査せずKnown Limitationとして提示してよい。

低頻度だが重大な不具合は、Fail-safe構造と代表試験を確認し、残RiskをOwnerへ提示する。

---

## 17. Release成果物

Stable V0.0.2で、実装したArtifactごとに次を提出する。

- Release URL
- Tag
- Release Version
- Final Source Commit
- Artifact Filename
- Artifact SHA-256
- Config Version
- Migration Version
- Required Core Version
- Required Dependencies
- Optional Dependencies
- Placement
- Load Order
- Commands
- Permissions
- Sanitized Config
- Database Schema／Migration
- Bundled／Relocated Libraries
- License／Third-party Notice
- Test Report
- Known Limitations
- Open Decisions
- Upgrade
- Rollback／Removal

推奨Runtime Artifact:

- `Wayfarer_Core-V0.0.2.jar`
- `Wayfarer_Main-V0.0.2.jar`
- `Wayfarer_Frontier-V0.0.2.jar`

CoreをV0.0.1のまま再利用する場合はRelease Manifestで明示し、Main／Frontierの互換Core Versionを固定する。

Releaseへ最低限次の情報を含める。ファイル分割方法とAsset数はPlugin担当の裁量とする。

- Checksums
- Release Manifest
- Artifact Matrix
- Plugin Test Report
- Sanitized Configuration
- Command／Permission Reference
- Dependency／Placement
- Migration／Compatibility
- Known Limitations
- Open Decisions
- Upgrade／Rollback
- Project Acceptance Input
- License
- Third-party Notices

---

## 18. Artifact Matrix

次または同等の表を提出する。

| Artifact／Feature | Plugin-side implementation | Plugin-side test | Stable Release | Project placement | Project acceptance | Roadmap |
|---|---|---|---|---|---|---|
| Wayfarer_Core | complete／changed／reused | passed／limited | V0.0.2 or V0.0.1 reuse | Main＋Frontier | pending | Order 9 maintenance |
| Wayfarer_Main | complete／partial | passed／limited／failed | V0.0.2／not included | Main only | pending | Order 10 |
| Growth Pickaxe | complete／partial | passed／limited／failed | enabled／disabled | Main only | pending | Order 10 |
| Wayfarer_Frontier Foundation | complete／partial | passed／limited／failed | V0.0.2／not included | Frontier only | pending | Order 11 |
| Traversal Loadout | complete／partial | passed／limited／failed | enabled／disabled | `frontier_iris` | pending | Orders 11／15 |
| Launchpad | complete／partial | passed／limited／failed | enabled／disabled | `frontier_iris` | pending | Orders 11／15 |
| Frontier Shop | complete／partial | passed／limited／failed | enabled／disabled | `frontier_iris` | pending | Orders 11／15 |
| Waystone | complete／deferred | passed／N/A | V0.0.2／later target | `frontier_iris` | pending | Orders 11／15 |
| EM–MVI Adapter | not authorized | N/A | not included | Frontier | pending | Order 13 |

---

## 19. Plugin Test Report

推奨Path:

`docs/reports/Project_Wayfarer_Plugin_Release_Test_Report_V0.0.2_<date>.md`

最低内容:

1. Pre-execution HEAD
2. Final Source Commit
3. Release Tag／URL
4. Artifact一覧
5. Artifact Version／Filename／SHA-256
6. Core compatibility
7. Config／Migration Version
8. Project Authority Snapshot
9. 実装Scope
10. Deferred Scope
11. Open Decisions
12. Build結果
13. Automated Testの代表結果
14. Standalone Test Server構成
15. Main代表試験
16. Frontier代表試験
17. Main Thread I/O
18. Migration／Upgrade
19. Transaction／Idempotency
20. Restart／Disable
21. Permission／Secret
22. Known Limitation
23. Failed／Skipped／N/A
24. Evidence Path
25. Project Runtime未変更
26. Artifact Matrix
27. Project Acceptance Input

全Test Caseを列挙する必要はない。正常結果が明確な代表操作と、判断に必要なEvidenceを記録する。

---

## 20. Project側Handoff Input

Project本流が受入指示書を作るため、Artifactごとに次を明示する。

- Runtime前提
- Placement
- Load Order
- Core compatibility
- Environment Variables
- Database／User／Schema
- Migration初回動作
- Redis／Waymark要件
- External Plugin要件
- World Allowlist
- MVI境界
- Feature Gate
- Health確認方法
- 代表Smoke操作
- Backup対象
- Restore対象
- Upgrade手順
- Removal手順
- Rollback手順
- Downgrade可否
- Data互換性
- Resource Pack input
- Test Serverとの差分
- 未決事項
- Project側でのみ確認可能な項目

---

## 21. Project側の段階受入

同一V0.0.2 Releaseでも、Project本番への受入をArtifact単位に分けてよい。

### 21.1 Main受入

対象:

- Compatible Wayfarer_Core
- Wayfarer_Main

確認:

- Release provenance
- Backup
- Main DB Migration
- Config／Secret
- Main-only placement
- Health
- 代表配布
- 代表Progress
- Evolution
- Broken／Repair
- Restart
- Rollback

### 21.2 Frontier受入

Project側Order 12／15の前提が整った後に実施する。

対象:

- Compatible Wayfarer_Core
- Wayfarer_Frontier
- LeafGrapple
- `frontier_iris`

確認:

- Frontier-only placement
- Exact World allowlist
- MVI非所有
- Loadout
- Hook
- Launchpad
- Shop
- Theme外Fail-closed
- Restart
- Rollback

Waystoneを後続Releaseへ分けた場合、V0.0.2 Frontier受入ではWaystoneを要求しない。

---

## 22. Plugin担当が実行してはいけないもの

- Project ServerへのJAR配置
- Project MariaDBへのMigration
- Project Redis／Waymark操作
- Project Secret設定
- Project Config反映
- Project Permission変更
- Project World生成／変更
- Project MVI設定
- Project Gate／Portal設定
- Project Resource Pack publish
- Project Player Data変更
- Project Repositoryの`versions.yml`／`plugin-manifest.yml`更新
- Project Roadmap完了化
- V0.0.1 Tag／Release変更
- 未承認Adapter Release
- Secret／Runtime Data Commit

---

## 23. Git／Release運用

Plugin担当のBranch／PR／Commit分割は裁量とする。

最低条件:

- Force Pushしない。
- Owner変更を巻き戻さない。
- Public Releaseを上書きしない。
- Source CommitとArtifactを対応付ける。
- Failed Testを隠さない。
- Generated Runtime JARを通常Source TreeへCommitしない。
- Secret／World／DB Data／LogをCommitしない。
- Release Tagを固定する。
- Handoff文書をRelease Sourceと対応付ける。

---

## 24. V0.0.2完了条件

### 24.1 必須

- V0.0.1 Core互換性を説明できる。
- Core変更がある場合、互換的に完成している。
- Wayfarer_Main Vertical Sliceが完成している。
- Wayfarer_Frontier V0.0.2 Scopeが完成している。
- Main／Frontier Migrationが分離されている。
- Plugin側代表試験が完了している。
- Stable `V0.0.2` Releaseが公開されている。
- Artifact Hash、Config、Migration、Test Reportがある。
- Artifact Matrixがある。
- Project Acceptance Inputがある。
- Project Runtimeを変更していない。

### 24.2 WaystoneをV0.0.2へ含めない場合

次を明記すれば完了を妨げない。

- Waystone: deferred to later `V0.0.x`
- Discovery／Teleport: disabled／unavailable
- Waystone Placement Tool: not sold
- Unresolved template decisions: listed

### 24.3 品質

- Project方針に反しない。
- Authorityを維持する。
- Main Thread同期I/Oなし。
- Item重複を明確に防止する。
- 二重Debit／Refundを明確に防止する。
- Migrationが安全である。
- Secretを露出しない。
- Fail-closedする。
- Known Limitationを明示する。
- 代表操作で主要機能が成立する。
- 不要な網羅検証で時間／Tokenを浪費しない。

---

## 25. 完了報告

次を報告する。

1. 推奨Sol
2. Pre-execution HEAD
3. Final Source Commit
4. Release Tag／URL
5. 実装Artifact一覧
6. Release Stage
7. V0.0.1互換性
8. Core変更
9. Main実装範囲
10. Frontier実装範囲
11. Waystone状態
12. Deferred Scope
13. Open Decisions
14. Artifact Version／Filename／SHA
15. Config Version
16. Migration Version
17. Public API変更
18. Module／Dependency
19. Placement／Load Order
20. Commands
21. Permissions
22. DB Schema
23. Waymark Transaction
24. Redis用途
25. Main Thread境界
26. Build結果
27. Automated Test代表結果
28. Main Standalone代表結果
29. Frontier Standalone代表結果
30. Migration／Upgrade結果
31. Restart／Disable結果
32. Permission／Secret結果
33. Known Limitations
34. Failed／Skipped／N/A
35. Test Report Path
36. Artifact Matrix
37. Project Acceptance Input
38. Runtime非変更
39. Secret／Runtime Data非追跡
40. Final Git status
41. Ownerへ渡すFile／URL
42. 次の推奨Plugin Version／Scope

---

## 26. 完了後の状態

V0.0.2最小完了状態:

| 対象 | 状態 |
|---|---|
| Wayfarer_Core | V0.0.1互換。V0.0.2での変更／再利用状況を報告 |
| Wayfarer_Main | Plugin側実装、Standalone Test、Release完了。Project受入待ち |
| Wayfarer_Frontier | Foundation、Loadout、Launchpad、ShopのPlugin側実装、Standalone Test、Release完了。Project受入待ち |
| Waystone | 完成条件を満たさない場合は後続V0.0.x |
| EM–MVI Adapter | 不在 |
| Project Runtime | 未変更 |

Roadmap上の扱い:

- Order 9: V0.0.1 Project受入を別Taskとして継続。
- Order 10: V0.0.2 Plugin側前提完了後、Project Main受入待ち。
- Order 11: V0.0.2 Plugin側主要前提完了後、Project Frontier受入待ち。
- Orders 12～16: Project側Runtime統合作業として継続。

次Action:

Plugin担当はV0.0.2 Release、Artifact、Hash、Config、Migration、Test Report、Open Decisions、Known Limitations、Artifact Matrix、Project Acceptance InputをProject Ownerへ渡す。

Project本流は、MainとFrontierを必要に応じて分けて受入指示書を作成する。
