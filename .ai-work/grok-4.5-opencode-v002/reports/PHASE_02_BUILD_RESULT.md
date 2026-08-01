# Phase 02 Build Result — Frontier Durable Redelivery

## 1. Repository Gate

| Item | Value |
| --- | --- |
| Repository | `eariver/Project-Wayfarer-Plugins` |
| Branch | `feature/V0.0.2-main-frontier` |
| Phase 01 baseline | `8e80fb0b7f4b3d497edaf0acbe392faeb3eef17b` |
| Baseline is HEAD or ancestor | Yes (start HEAD == baseline) |
| Draft PR | `#14` |
| Initial `git status --short` | empty |
| OpenCode start credits | `$11.59` (instruction value) |

## 2. Authority SHA

| Path | SHA-256 |
| --- | --- |
| `Project_Wayfarer_Plugin_V0.0.2_PR14_Final_Pre_Client_Codex_Instructions_AUDITED.md` | `6ADD6659937166FD90A970DB6FAA26C02F30A2E6B23557508F8F3BA93349B092` |

Match: **Yes**

## 3. Starting HEAD

`8e80fb0b7f4b3d497edaf0acbe392faeb3eef17b`

## 4. Final HEAD

`845d74921ab384c6e019fb611c4cabd8f273995f`

## 5. Initial / Final git status

- Initial: clean
- Final: clean after commits (report may be included in docs commit)

## 6. Files changed

Production:

- `plugins/wayfarer-frontier/.../application/PlayerOperationSerializer.java` (new)
- `plugins/wayfarer-frontier/.../application/TraversalDeliveryCoordinator.java`
- `plugins/wayfarer-frontier/.../application/TraversalLoadoutRepository.java`
- `plugins/wayfarer-frontier/.../persistence/JdbcTraversalLoadoutRepository.java`
- `plugins/wayfarer-frontier/.../gameplay/FrontierGameplayRuntime.java`
- `plugins/wayfarer-frontier/.../WayfarerFrontierPlugin.java`
- `plugins/wayfarer-frontier/.../domain/DeathIdentitySnapshot.java` (new)
- `plugins/wayfarer-frontier/.../domain/DeathPersistResult.java` (new)
- `plugins/wayfarer-frontier/.../domain/DeliveryCompletion.java` (new)

Tests:

- `PlayerOperationSerializerTest.java` (new)
- `TraversalDeliveryCoordinatorTest.java` (new)
- `FrontierMigrationIntegrationTest.java` (durable redelivery cases)

Docs:

- `PHASE_02_BUILD_RESULT.md` (this file)

Not changed: applied migrations V001/V002, `plugins/wayfarer-main/**`

## 7. New types / methods

- `DeathIdentitySnapshot`, `DeathPersistResult`, `DeliveryCompletion`
- `PlayerOperationSerializer.enqueue` / `shutdown`
- Repository: `persistDeathSnapshot`, `reopenAbsentPermanents`, `markPermanentDelivered`, `markConsumableDelivered`
- Coordinator: `persistDeathSnapshots`, `adminReissueCritical`, expanded `Result`, `formatAdmin`, gateway/audit ports
- Runtime: complete managed permanent parse, exact current physical (storage/armor/offhand/cursor), death snapshot path, compensation remove, safe-entry notify

## 8. Durable lifecycle

Death Main: remove complete managed permanents from drops → value snapshots → serializer enqueue DB persist (match logical → PENDING create/reopen). No `ItemStack` retention map. Safe Entry: ensure → presence → reopen absent → deliver → permanent/consumable mark → cache → notify. Restart: PENDING rows re-read from MariaDB.

## 9. Unified serializer

Single `PlayerOperationSerializer` owned by `TraversalDeliveryCoordinator`, shared by death persist, safe entry, admin reissue critical. FIFO per player, concurrent across players, exceptional cleanup, shutdown rejects new and unstarted suppliers.

## 10. Death / Safe Entry / Reissue ordering

Death enqueue before respawn safe entry on same player tail. Admin reissue critical completes then `retryDelivery` enqueues a separate safe entry (no nested enqueue).

## 11. Permanent / Consumable completion

Permanent: FOR UPDATE delivery + payload + logical match → `STALE_IDENTITY` / `MALFORMED_PAYLOAD` / transitions. Consumable: no logical identity; launchpad/rocket path preserved.

## 12. Physical identity and cursor

Complete managed PDC + owner/theme/type/instance/epoch/schema + ACTIVE logical + exact world. Cursor included in presence/cleanup/compensate. Wrong schema / partial not treated as current.

## 13. Notification / Audit

Inventory full / capability unavailable: player messages. Offline / left theme: no player message. Conflict / unknown: admin-review message. Success audit only on death `PENDING_CREATED`/`REOPENED_TO_PENDING` and delivery `TRANSITIONED_TO_DELIVERED`. Admin formatter avoids record `toString()` (`Result[`).

## 14. Tests added

- Serializer FIFO / concurrency / poison / shutdown / death-before-safe
- Coordinator death/safe cycles, delayed death, failure recovery, counters, MALFORMED→UNKNOWN+compensate, CANCELLED conflict, launchpad consumable mark, admin reissue, audit-once
- MariaDB: death match, STALE_SKIPPED, PENDING_CREATED, ALREADY_PENDING, REOPENED, CANCELLED, completion results, consumable mark, multi-cycle same epoch, reissue epoch, restart pending read

## 15. Commands executed

```text
./gradlew --no-daemon --console=plain :plugins:wayfarer-frontier:test
./gradlew --no-daemon --console=plain :plugins:wayfarer-frontier:mariaDbIntegrationTest
```

## 16. Exact PASS / FAIL

| Command | Result |
| --- | --- |
| `:plugins:wayfarer-frontier:test` | **PASS** (44 tests) |
| `:plugins:wayfarer-frontier:mariaDbIntegrationTest` | **PASS** (6 tests) |

## 17. MariaDB / Docker evidence

Testcontainers `mariadb:11.8` via `MariaDbContainerFixture`. Durable cases in `FrontierMigrationIntegrationTest` migrated core+frontier and exercised repository contracts successfully.

## 18. Commits created

- `43df09d6ed3aa7884586c5138f9e77ea5c65aef3` — `fix(frontier): make permanent redelivery durable`
- `845d74921ab384c6e019fb611c4cabd8f273995f` — `docs(test): record phase 02 durable redelivery validation`

## 19. Push result

Pushed to `origin/feature/V0.0.2-main-frontier` (`8e80fb0..845d749`). Draft PR #14 retained.

## 20. Known limitations

- Full Bukkit inventory behavior is not proven under MockBukkit (not added); unit tests use fakes; integration covers JDBC contracts.
- Audit `record` is fire-and-forget; audit failure does not roll back delivery (by design).
- Some adversarial coordinator races (admin A/B/C exhaustive) are partially covered via serializer + reissue path rather than full multi-thread admin matrix.

## 21. Explicitly deferred scope

Main death/reissue, MVI, epoch rotation on normal death, launchpad free reissue, GUI, permissions, Phase 03+, release/merge/tag, headless gameplay framework.

## 22. Final verdict

**PASS**
