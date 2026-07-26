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

## Important boundaries

- Plugin SourceはこのRepositoryで管理します。
- Runtime Config、JAR配置、World、Database Data、SecretはProject／Runtime側で管理します。
- このscaffoldをCommitしただけではRuntime導入を承認しません。
