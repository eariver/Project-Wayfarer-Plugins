#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
runtime_root="$repository_root/build/preclient-runtime"
paper_jar="$runtime_root/paper-1.21.11-132.jar"
paper_url="https://fill-data.papermc.io/v1/objects/5ffef465eeeb5f2a3c23a24419d97c51afd7dbb4923ff42df9a3f58bba1ccfba/paper-1.21.11-132.jar"
paper_sha256="5ffef465eeeb5f2a3c23a24419d97c51afd7dbb4923ff42df9a3f58bba1ccfba"
evidence_root="$runtime_root/evidence"
server_pid=""
server_input_fd=""
java_executable="${JAVA_HOME:?JAVA_HOME is required}/bin/java"

: "${WAYFARER_DB_USERNAME:?WAYFARER_DB_USERNAME is required}"
: "${WAYFARER_DB_PASSWORD:?WAYFARER_DB_PASSWORD is required}"
: "${WAYFARER_REDIS_URI:?WAYFARER_REDIS_URI is required}"
: "${MARIADB_CONTAINER_ID:?MARIADB_CONTAINER_ID is required}"
: "${MARIADB_ROOT_PASSWORD:?MARIADB_ROOT_PASSWORD is required}"
: "${REDIS_CONTAINER_ID:?REDIS_CONTAINER_ID is required}"

mkdir -p "$runtime_root" "$evidence_root"
"$java_executable" -version

curl --fail --silent --show-error --location \
  --user-agent "Project-Wayfarer-Plugins-preclient/0.0.1 (https://github.com/eariver/Project-Wayfarer-Plugins)" \
  "$paper_url" \
  --output "$paper_jar"
echo "$paper_sha256  $paper_jar" | sha256sum --check -

mapfile -t core_jars < <(
  find "$repository_root/plugins/wayfarer-core/build/libs" -maxdepth 1 -type f \
    -name 'wayfarer-core-0.0.1-rc.1.jar'
)
mapfile -t probe_jars < <(
  find "$repository_root/testkit/headless-paper/build/libs" -maxdepth 1 -type f \
    -name 'wayfarer-preclient-probe-0.0.1-rc.1.jar'
)
mapfile -t fixture_jars < <(
  find "$repository_root/testkit/headless-waymark-fixture/build/libs" -maxdepth 1 -type f \
    -name 'wayfarer-preclient-waymark-fixture-0.0.1-rc.1.jar'
)
test "${#core_jars[@]}" -eq 1
test "${#probe_jars[@]}" -eq 1
test "${#fixture_jars[@]}" -eq 1
core_jar="${core_jars[0]}"
probe_jar="${probe_jars[0]}"
fixture_jar="${fixture_jars[0]}"
core_sha256="$(sha256sum "$core_jar" | awk '{print $1}')"
probe_sha256="$(sha256sum "$probe_jar" | awk '{print $1}')"
fixture_sha256="$(sha256sum "$fixture_jar" | awk '{print $1}')"

db_url() {
  printf 'jdbc:mariadb://127.0.0.1:3306/%s' "$1"
}

reset_database() {
  local database="$1"
  [[ "$database" =~ ^wayfarer_preclient_[a-z_]+$ ]]
  docker exec "$MARIADB_CONTAINER_ID" \
    mariadb -uroot "-p$MARIADB_ROOT_PASSWORD" \
    -e "DROP DATABASE IF EXISTS \`$database\`;
        CREATE DATABASE \`$database\`;
        GRANT ALL PRIVILEGES ON \`$database\`.* TO '$WAYFARER_DB_USERNAME'@'%';
        FLUSH PRIVILEGES;"
}

query_database() {
  local database="$1"
  local query="$2"
  docker exec "$MARIADB_CONTAINER_ID" \
    mariadb -uroot "-p$MARIADB_ROOT_PASSWORD" -N "$database" -e "$query"
}

write_server_files() {
  local server_root="$1"
  local jdbc_url="$2"
  local include_probe="$3"
  local include_fixture="$4"
  local scenario="$5"
  local fixture_mode="$6"
  local shutdown_seconds="$7"
  local redis_prefix="$8"

  [[ "$redis_prefix" =~ ^[a-z0-9][a-z0-9._-]{0,31}$ ]]
  mkdir -p "$server_root/plugins/Wayfarer_Core"
  printf 'eula=true\n' > "$server_root/eula.txt"
  cat > "$server_root/server.properties" <<'PROPERTIES'
online-mode=false
server-port=25580
level-name=preclient
level-type=minecraft:normal
generate-structures=false
spawn-protection=0
max-players=1
view-distance=2
simulation-distance=2
motd=Project Wayfarer isolated pre-client test
PROPERTIES
  cat > "$server_root/plugins/Wayfarer_Core/config.yml" <<CONFIG
config-version: 1
server-id: wayfarer-preclient
shutdown-timeout:
  seconds: $shutdown_seconds
executor:
  threads: 2
  queue-capacity: 32
  thread-name-prefix: Wayfarer-Preclient
audit:
  enabled: true
health:
  player-details: false
mariadb:
  enabled: true
  jdbc-url-env: WAYFARER_DB_URL
  username-env: WAYFARER_DB_USERNAME
  password-env: WAYFARER_DB_PASSWORD
  maximum-pool-size: 4
  minimum-idle: 1
  connection-timeout-ms: 2000
redis:
  enabled: true
  uri-env: WAYFARER_REDIS_URI
  connect-timeout-ms: 2000
  operation-timeout-ms: 2000
  cache-maximum-ttl-seconds: 60
  lock-maximum-lease-seconds: 10
  key-prefix: $redis_prefix
migration:
  enabled: true
  locations:
    - db/migration/core
waymark:
  enabled: true
  expected-provider: preclient-fixture
  operation-timeout-ms: 1000
CONFIG
  cp "$core_jar" "$server_root/plugins/Wayfarer_Core-0.0.1-rc.1.jar"
  if [[ "$include_probe" == "yes" ]]; then
    cp "$probe_jar" "$server_root/plugins/Wayfarer_Preclient_Probe-0.0.1-rc.1.jar"
  fi
  if [[ "$include_fixture" == "yes" ]]; then
    cp "$fixture_jar" \
      "$server_root/plugins/Wayfarer_Preclient_Waymark_Fixture-0.0.1-rc.1.jar"
  fi
  printf '%s\n' "$jdbc_url" > "$server_root/.jdbc-url"
  printf '%s\n' "$scenario" > "$server_root/preclient-scenario.txt"
  printf '%s\n' "$fixture_mode" > "$server_root/fixture-mode.txt"
}

start_server() {
  local server_root="$1"
  local log_file="$2"
  local jdbc_url
  jdbc_url="$(cat "$server_root/.jdbc-url")"
  local input_pipe="$server_root/server-input"
  rm -f "$input_pipe"
  mkfifo "$input_pipe"
  (
    cd "$server_root"
    WAYFARER_DB_URL="$jdbc_url" \
      exec "$java_executable" -Xms512M -Xmx1024M -jar "$paper_jar" --nogui \
      < "$input_pipe" > "$log_file" 2>&1
  ) &
  server_pid="$!"
  exec {server_input_fd}>"$input_pipe"
}

wait_for_log() {
  local log_file="$1"
  local marker="$2"
  local attempts="${3:-180}"
  for ((attempt = 0; attempt < attempts; attempt++)); do
    if grep -Fq "$marker" "$log_file"; then
      return 0
    fi
    if ! kill -0 "$server_pid" 2>/dev/null; then
      echo "Server exited before marker: $marker"
      tail -n 120 "$log_file"
      return 1
    fi
    sleep 1
  done
  echo "Timed out waiting for marker: $marker"
  tail -n 120 "$log_file"
  return 1
}

send_command() {
  printf '%s\n' "$1" >&"$server_input_fd"
}

stop_server() {
  send_command "stop"
  exec {server_input_fd}>&-
  for ((attempt = 0; attempt < 90; attempt++)); do
    if ! kill -0 "$server_pid" 2>/dev/null; then
      wait "$server_pid"
      server_pid=""
      return 0
    fi
    sleep 1
  done
  echo "Paper did not stop within the bounded wait"
  return 1
}

wait_for_injected_exit() {
  local expected_code="$1"
  for ((attempt = 0; attempt < 60; attempt++)); do
    if ! kill -0 "$server_pid" 2>/dev/null; then
      break
    fi
    sleep 1
  done
  exec {server_input_fd}>&-
  set +e
  wait "$server_pid"
  local actual_code="$?"
  set -e
  server_pid=""
  test "$actual_code" -eq "$expected_code"
}

assert_no_sensitive_output() {
  local log_file="$1"
  if grep -Fq "$WAYFARER_DB_PASSWORD" "$log_file"; then
    echo "Synthetic test credential appeared in runtime output"
    return 1
  fi
}

assert_fixture_worker_threads() {
  local log_file="$1"
  grep -Fq "WAYFARER_FIXTURE: CALL probe thread=Wayfarer-Preclient" "$log_file"
  grep -Fq "WAYFARER_FIXTURE: CALL debit thread=Wayfarer-Preclient" "$log_file"
  if grep -F "WAYFARER_FIXTURE: CALL" "$log_file" | grep -Fq "thread=Server thread"; then
    echo "Provider fixture was called on the Paper main thread"
    return 1
  fi
}

# Baseline, API probes, transaction success, console, Redis outage/reconnect, restart.
baseline_db="wayfarer_preclient_baseline"
reset_database "$baseline_db"
baseline_root="$runtime_root/baseline"
baseline_first_log="$evidence_root/baseline-first.log"
baseline_restart_log="$evidence_root/baseline-restart.log"
rm -rf "$baseline_root"
write_server_files \
  "$baseline_root" "$(db_url "$baseline_db")" yes yes baseline success 5 \
  wf-preclient-baseline
start_server "$baseline_root" "$baseline_first_log"
wait_for_log "$baseline_first_log" "WAYFARER_PRECLIENT_PROBE: PASS scenario=baseline" 240
baseline_transaction_id="$(
  query_database "$baseline_db" \
    "SELECT transaction_id FROM wf_core_transaction ORDER BY created_at DESC LIMIT 1;"
)"
test "$(
  query_database "$baseline_db" \
    "SELECT COUNT(*) FROM wf_core_player_identity
     WHERE player_uuid = '12345678-1234-5678-9234-567812345678'
       AND last_known_name = 'PreclientProbe'
       AND last_server_id = 'wayfarer-preclient';"
)" -eq 1
send_command "wayfarer admin health"
send_command "wayfarer admin transaction inspect $baseline_transaction_id"
send_command "wayfarer admin transaction reconcile $baseline_transaction_id fail"
send_command "wayfarer admin transaction reconcile $baseline_transaction_id fail confirm"
send_command "tick query"
send_command "version"
sleep 3
docker stop --time 1 "$REDIS_CONTAINER_ID" >/dev/null
sleep 5
send_command "wayfarer admin health"
docker start "$REDIS_CONTAINER_ID" >/dev/null
sleep 12
send_command "wayfarer admin health"
sleep 2
kill -3 "$server_pid"
sleep 2
stop_server

grep -Fq "Wayfarer_Core 0.0.1-rc.1 | server=wayfarer-preclient | config=1 | lifecycle=ENABLED" "$baseline_first_log"
grep -Fq "MariaDB: UP" "$baseline_first_log"
grep -Fq "Migration: UP" "$baseline_first_log"
grep -Fq "Redis: DOWN" "$baseline_first_log"
grep -Fq "Redis: UP" "$baseline_first_log"
grep -Fq "TRANSACTION_SUCCESS" "$baseline_first_log"
grep -Fq "PLAYER_IDENTITY PASS" "$baseline_first_log"
grep -Fq "MAIN_THREAD_GUARDS PASS jdbc=true redis=true" "$baseline_first_log"
grep -Fq "REDIS_PRIMITIVES PASS cache=true lock=true message=true" \
  "$baseline_first_log"
grep -Fq "debitReference=present" "$baseline_first_log"
grep -Fq "Reconciliation requires an explicit trailing 'confirm'." "$baseline_first_log"
grep -Fq "reconciliation result=COMMITTED" "$baseline_first_log"
grep -Fq "Full thread dump" "$baseline_first_log"
grep -Fq "WAYFARER_PRECLIENT_PROBE: DISABLED" "$baseline_first_log"
assert_fixture_worker_threads "$baseline_first_log"
assert_no_sensitive_output "$baseline_first_log"

start_server "$baseline_root" "$baseline_restart_log"
wait_for_log "$baseline_restart_log" "WAYFARER_PRECLIENT_PROBE: PASS scenario=baseline" 180
stop_server
grep -Fq "WAYFARER_PRECLIENT_PROBE: SERVICE_LOOKUP PASS" "$baseline_restart_log"
grep -Fq "WAYFARER_PRECLIENT_PROBE: DISABLED" "$baseline_restart_log"
if grep -Fq "Wayfarer services are already published" "$baseline_restart_log"; then
  echo "Duplicate service registration detected"
  exit 1
fi
assert_no_sensitive_output "$baseline_restart_log"

# Concrete provider remains absent; the test-only fixture outage is fail-closed.
outage_db="wayfarer_preclient_provider_outage"
reset_database "$outage_db"
outage_root="$runtime_root/provider-outage"
outage_log="$evidence_root/provider-outage.log"
rm -rf "$outage_root"
write_server_files \
  "$outage_root" "$(db_url "$outage_db")" yes yes provider-outage outage 5 \
  wf-preclient-provider-outage
start_server "$outage_root" "$outage_log"
wait_for_log "$outage_log" "PASS scenario=provider-outage" 180
send_command "wayfarer admin health"
sleep 2
stop_server
grep -Fq "WAYFARER_FIXTURE: CALL probe" "$outage_log"
grep -Fq "Transaction: DOWN" "$outage_log"
assert_no_sensitive_output "$outage_log"

# Timeout before effect resolves NOT_APPLIED; timeout after effect resolves APPLIED.
for timeout_case in before after; do
  timeout_db="wayfarer_preclient_timeout_${timeout_case}"
  reset_database "$timeout_db"
  timeout_root="$runtime_root/timeout-$timeout_case"
  timeout_log="$evidence_root/timeout-$timeout_case.log"
  rm -rf "$timeout_root"
  write_server_files \
    "$timeout_root" "$(db_url "$timeout_db")" yes yes \
    "timeout-${timeout_case}-effect" "timeout-${timeout_case}-effect" 5 \
    "wf-preclient-timeout-$timeout_case"
  start_server "$timeout_root" "$timeout_log"
  wait_for_log "$timeout_log" "PASS scenario=timeout-${timeout_case}-effect" 180
  stop_server
  grep -Fq "mode=timeout-${timeout_case}-effect" "$timeout_log"
  grep -Fq "WAYFARER_FIXTURE: CALL resolve" "$timeout_log"
  assert_no_sensitive_output "$timeout_log"
done

# Bounded executor queue rejection/backpressure.
queue_db="wayfarer_preclient_queue"
reset_database "$queue_db"
queue_root="$runtime_root/queue"
queue_log="$evidence_root/queue-rejection.log"
rm -rf "$queue_root"
write_server_files \
  "$queue_root" "$(db_url "$queue_db")" yes yes queue-rejection success 5 \
  wf-preclient-queue
start_server "$queue_root" "$queue_log"
wait_for_log "$queue_log" "WAYFARER_PRECLIENT_PROBE: QUEUE_PASS" 180
wait_for_log "$queue_log" "PASS scenario=queue-rejection" 30
stop_server
assert_no_sensitive_output "$queue_log"

# Crash after debit effect, then restart recovery from durable DEBIT_PENDING.
debit_db="wayfarer_preclient_debit_recovery"
reset_database "$debit_db"
debit_root="$runtime_root/debit-recovery"
debit_crash_log="$evidence_root/debit-crash.log"
debit_recovery_log="$evidence_root/debit-recovery.log"
rm -rf "$debit_root"
write_server_files \
  "$debit_root" "$(db_url "$debit_db")" yes yes debit-crash crash-after-debit 5 \
  wf-preclient-debit-recovery
start_server "$debit_root" "$debit_crash_log"
wait_for_injected_exit 73
grep -Fq "HALT_AFTER_DEBIT" "$debit_root/fixture-crash-marker.txt"
debit_transaction_id="$(
  query_database "$debit_db" \
    "SELECT transaction_id FROM wf_core_transaction WHERE state = 'DEBIT_PENDING' LIMIT 1;"
)"
test -n "$debit_transaction_id"
printf '%s\n' recovery-verify > "$debit_root/preclient-scenario.txt"
printf '%s\n' success > "$debit_root/fixture-mode.txt"
printf '%s\n' "$debit_transaction_id" > "$debit_root/expected-transaction-id.txt"
printf '%s\n' COMMITTED > "$debit_root/expected-state.txt"
start_server "$debit_root" "$debit_recovery_log"
wait_for_log "$debit_recovery_log" \
  "RECOVERY_PASS id=$debit_transaction_id state=COMMITTED" 180
stop_server
test "$(
  query_database "$debit_db" \
    "SELECT state FROM wf_core_transaction WHERE transaction_id = '$debit_transaction_id';"
)" = "COMMITTED"
assert_no_sensitive_output "$debit_recovery_log"

# Crash after refund effect, then restart recovery from durable REFUND_PENDING.
refund_db="wayfarer_preclient_refund_recovery"
reset_database "$refund_db"
refund_root="$runtime_root/refund-recovery"
refund_crash_log="$evidence_root/refund-crash.log"
refund_recovery_log="$evidence_root/refund-recovery.log"
rm -rf "$refund_root"
write_server_files \
  "$refund_root" "$(db_url "$refund_db")" yes yes refund-crash unknown-after-effect 5 \
  wf-preclient-refund-recovery
start_server "$refund_root" "$refund_crash_log"
wait_for_injected_exit 74
grep -Fq "HALT_AFTER_REFUND" "$refund_root/fixture-crash-marker.txt"
refund_transaction_id="$(
  query_database "$refund_db" \
    "SELECT transaction_id FROM wf_core_transaction WHERE state = 'REFUND_PENDING' LIMIT 1;"
)"
test -n "$refund_transaction_id"
printf '%s\n' recovery-verify > "$refund_root/preclient-scenario.txt"
printf '%s\n' success > "$refund_root/fixture-mode.txt"
printf '%s\n' "$refund_transaction_id" > "$refund_root/expected-transaction-id.txt"
printf '%s\n' RECONCILED_REFUNDED > "$refund_root/expected-state.txt"
start_server "$refund_root" "$refund_recovery_log"
wait_for_log "$refund_recovery_log" \
  "RECOVERY_PASS id=$refund_transaction_id state=RECONCILED_REFUNDED" 180
stop_server
test "$(
  query_database "$refund_db" \
    "SELECT state FROM wf_core_transaction WHERE transaction_id = '$refund_transaction_id';"
)" = "RECONCILED_REFUNDED"
assert_no_sensitive_output "$refund_recovery_log"

# Accepted work drains cleanly before disable.
drain_db="wayfarer_preclient_drain"
reset_database "$drain_db"
drain_root="$runtime_root/accepted-drain"
drain_log="$evidence_root/accepted-drain.log"
rm -rf "$drain_root"
write_server_files \
  "$drain_root" "$(db_url "$drain_db")" yes yes accepted-drain success 5 \
  wf-preclient-drain
start_server "$drain_root" "$drain_log"
wait_for_log "$drain_log" "ACCEPTED_WORK_RUNNING" 180
stop_server
grep -Fq "ACCEPTED_WORK_COMPLETED" "$drain_log"
grep -Fq "WAYFARER_PRECLIENT_PROBE: DISABLED" "$drain_log"

# A deliberately interruption-resistant task proves bounded non-clean shutdown.
shutdown_db="wayfarer_preclient_shutdown"
reset_database "$shutdown_db"
shutdown_root="$runtime_root/shutdown-timeout"
shutdown_log="$evidence_root/shutdown-timeout.log"
rm -rf "$shutdown_root"
write_server_files \
  "$shutdown_root" "$(db_url "$shutdown_db")" yes yes shutdown-timeout success 1 \
  wf-preclient-shutdown
start_server "$shutdown_root" "$shutdown_log"
wait_for_log "$shutdown_log" "SHUTDOWN_BLOCKER_RUNNING" 180
stop_server
grep -Fq "Wayfarer executor exceeded graceful shutdown timeout; forcing termination" \
  "$shutdown_log"
grep -Fq "Wayfarer executor did not terminate after forced shutdown" "$shutdown_log"
assert_no_sensitive_output "$shutdown_log"

# Migration checksum and MariaDB-unavailable enable failures use isolated roots.
docker exec "$MARIADB_CONTAINER_ID" \
  mariadb -uroot "-p$MARIADB_ROOT_PASSWORD" \
  -e "UPDATE \`$baseline_db\`.flyway_schema_history
      SET checksum = checksum + 1 WHERE version = '001';"
migration_failure_root="$runtime_root/migration-failure"
migration_failure_log="$evidence_root/migration-failure.log"
rm -rf "$migration_failure_root"
write_server_files \
  "$migration_failure_root" "$(db_url "$baseline_db")" no no baseline success 5 \
  wf-preclient-migration-failure
start_server "$migration_failure_root" "$migration_failure_log"
wait_for_log "$migration_failure_log" "Wayfarer_Core failed closed during enable" 180
send_command "stop"
exec {server_input_fd}>&-
wait "$server_pid"
server_pid=""
assert_no_sensitive_output "$migration_failure_log"

mariadb_failure_root="$runtime_root/mariadb-failure"
mariadb_failure_log="$evidence_root/mariadb-unavailable.log"
rm -rf "$mariadb_failure_root"
write_server_files \
  "$mariadb_failure_root" \
  "jdbc:mariadb://127.0.0.1:1/wayfarer_preclient_unavailable" \
  no no baseline success 5 wf-preclient-mariadb-failure
start_server "$mariadb_failure_root" "$mariadb_failure_log"
wait_for_log "$mariadb_failure_log" "Wayfarer_Core failed closed during enable" 90
send_command "stop"
exec {server_input_fd}>&-
wait "$server_pid"
server_pid=""
assert_no_sensitive_output "$mariadb_failure_log"

migration_count="$(
  query_database "$debit_db" \
    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1;"
)"
latest_migration="$(
  query_database "$debit_db" \
    "SELECT MAX(version) FROM flyway_schema_history WHERE success = 1;"
)"
test "$migration_count" -eq 3
test "$latest_migration" = "003"

cat > "$evidence_root/summary.txt" <<SUMMARY
result=PASS
paper=1.21.11-build-132
paper_sha256=$paper_sha256
java=25
candidate_core_version=0.0.1-rc.1
candidate_core_sha256=$core_sha256
candidate_core_runtime_jar_count=1
test_only_probe_sha256=$probe_sha256
test_only_fixture_sha256=$fixture_sha256
config_version=1
migration_latest=V003
migration_count=3
transaction_success=pass
player_identity_repository_hook=pass
redis_cache_lock_message=pass
main_thread_jdbc_redis_rejection=pass
timeout_before_effect=pass
timeout_after_effect=pass
automatic_reconcile=pass
debit_pending_restart_recovery=pass
refund_pending_restart_recovery=pass
queue_rejection=pass
accepted_work_drain=pass
shutdown_timeout_non_clean=pass
provider=TEST_ONLY_FIXTURE
concrete_provider=BLOCKED_ADR_0006
minecraft_client_acceptance=pending
project_runtime_changed=no
SUMMARY
