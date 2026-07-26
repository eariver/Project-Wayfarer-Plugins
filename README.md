# Project Wayfarer Plugins

Project Wayfarerの独自Paper Plugin群を管理するGradle Multi-module Repositoryです。

## Runtime artifacts

- `Wayfarer_Core`
- `Wayfarer_Main`
- `Wayfarer_Frontier`

`Wayfarer_Frontier_EliteMobsMVI`は、Project側Decision Resultが`ADAPTER_REQUIRED`の場合だけ別Taskで追加します。

## Authority

実装要件は次を参照してください。

- [`docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md`](docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md)
- Project Wayfarer Repositoryの現行Concept、Scope、Runtime Lock、Roadmap、Acceptance

## Bootstrap

初回だけPowerShellでGradle Wrapperを生成します。

```powershell
pwsh ./tools/bootstrap-gradle-wrapper.ps1
./gradlew.bat check
```

Wrapper生成後、`gradlew`、`gradlew.bat`、`gradle/wrapper/`をCommitしてください。

## V0.0.1-alpha.1 Lifecycle Foundation

`Wayfarer_Core`はconfig version 1のtyped validation、環境変数secret reference、
fail-closed lifecycle、Bukkit ServicesManager公開、managed executor、sanitized health、
`/wayfarer admin health`を実装します。

```text
Command:    /wayfarer admin health
Permission: wayfarer.admin.health
```

MariaDB、Flyway、Redis、Waymark、transaction、identity、audit persistenceは後続Sliceです。
未実装dependencyはhealthで`UNKNOWN`と表示されます。現在のrelease gate/readinessは
`BLOCKED`で、Runtime testとPre-releaseは未実施です。

## Important boundaries

- Plugin SourceはこのRepositoryで管理します。
- Runtime Config、JAR配置、World、Database Data、SecretはProject／Runtime側で管理します。
- このscaffoldをCommitしただけではRuntime導入を承認しません。
