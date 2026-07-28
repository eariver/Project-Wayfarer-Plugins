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

: "${WAYFARER_DB_URL:?WAYFARER_DB_URL is required}"
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
test "${#core_jars[@]}" -eq 1
test "${#probe_jars[@]}" -eq 1
core_jar="${core_jars[0]}"
probe_jar="${probe_jars[0]}"
core_sha256="$(sha256sum "$core_jar" | awk '{print $1}')"

write_server_files() {
  local server_root="$1"
  local jdbc_url="$2"
  local include_probe="$3"

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
  cat > "$server_root/plugins/Wayfarer_Core/config.yml" <<'CONFIG'
config-version: 1
server-id: wayfarer-preclient
shutdown-timeout:
  seconds: 5
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
  key-prefix: wayfarer-preclient
migration:
  enabled: true
  locations:
    - db/migration/core
waymark:
  enabled: false
  expected-provider: RedisEconomy
  operation-timeout-ms: 1000
CONFIG
  cp "$core_jar" "$server_root/plugins/Wayfarer_Core-0.0.1-rc.1.jar"
  if [[ "$include_probe" == "yes" ]]; then
    cp "$probe_jar" "$server_root/plugins/Wayfarer_Preclient_Probe-0.0.1-rc.1.jar"
  fi
  printf '%s\n' "$jdbc_url" > "$server_root/.jdbc-url"
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
      tail -n 100 "$log_file"
      return 1
    fi
    sleep 1
  done
  echo "Timed out waiting for marker: $marker"
  tail -n 100 "$log_file"
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

assert_no_sensitive_output() {
  local log_file="$1"
  if grep -Fq "$WAYFARER_DB_PASSWORD" "$log_file"; then
    echo "Synthetic test credential appeared in runtime output"
    return 1
  fi
}

valid_root="$runtime_root/valid"
valid_first_log="$evidence_root/valid-first.log"
valid_restart_log="$evidence_root/valid-restart.log"
rm -rf "$valid_root"
write_server_files "$valid_root" "$WAYFARER_DB_URL" "yes"

start_server "$valid_root" "$valid_first_log"
wait_for_log "$valid_first_log" "Done (" 240
wait_for_log "$valid_first_log" "WAYFARER_PRECLIENT_PROBE: PASS" 90
send_command "wayfarer admin health"
send_command "wayfarer admin transaction inspect 00000000-0000-0000-0000-000000000000"
send_command "wayfarer admin transaction reconcile 00000000-0000-0000-0000-000000000000 fail confirm"
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

grep -Fq "Wayfarer_Core 0.0.1-rc.1 | server=wayfarer-preclient | config=1 | lifecycle=ENABLED" "$valid_first_log"
grep -Fq "MariaDB: UP" "$valid_first_log"
grep -Fq "Migration: UP" "$valid_first_log"
grep -Fq "Redis: DOWN" "$valid_first_log"
grep -Fq "Redis: UP" "$valid_first_log"
grep -Fq "Usage: /wayfarer admin health" "$valid_first_log"
grep -Fq "WAYFARER_PRECLIENT_PROBE: DISABLED" "$valid_first_log"
grep -Fq "Full thread dump" "$valid_first_log"
assert_no_sensitive_output "$valid_first_log"

start_server "$valid_root" "$valid_restart_log"
wait_for_log "$valid_restart_log" "Done (" 180
wait_for_log "$valid_restart_log" "WAYFARER_PRECLIENT_PROBE: PASS" 90
send_command "wayfarer admin health"
sleep 2
stop_server
grep -Fq "WAYFARER_PRECLIENT_PROBE: SERVICE_LOOKUP PASS" "$valid_restart_log"
grep -Fq "WAYFARER_PRECLIENT_PROBE: DISABLED" "$valid_restart_log"
if grep -Fq "Wayfarer services are already published" "$valid_restart_log"; then
  echo "Duplicate service registration detected"
  exit 1
fi
assert_no_sensitive_output "$valid_restart_log"

migration_count="$(
  docker exec \
    "$MARIADB_CONTAINER_ID" \
    mariadb -uroot "-p$MARIADB_ROOT_PASSWORD" -N \
    -e "SELECT COUNT(*) FROM wayfarer_preclient.flyway_schema_history WHERE success = 1;"
)"
latest_migration="$(
  docker exec \
    "$MARIADB_CONTAINER_ID" \
    mariadb -uroot "-p$MARIADB_ROOT_PASSWORD" -N \
    -e "SELECT MAX(version) FROM wayfarer_preclient.flyway_schema_history WHERE success = 1;"
)"
test "$migration_count" -eq 3
test "$latest_migration" = "003"

docker exec \
  "$MARIADB_CONTAINER_ID" \
  mariadb -uroot "-p$MARIADB_ROOT_PASSWORD" \
  -e "UPDATE wayfarer_preclient.flyway_schema_history SET checksum = checksum + 1 WHERE version = '001';"

migration_failure_root="$runtime_root/migration-failure"
migration_failure_log="$evidence_root/migration-failure.log"
rm -rf "$migration_failure_root"
write_server_files "$migration_failure_root" "$WAYFARER_DB_URL" "no"
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
  "jdbc:mariadb://127.0.0.1:1/wayfarer_preclient" \
  "no"
start_server "$mariadb_failure_root" "$mariadb_failure_log"
wait_for_log "$mariadb_failure_log" "Wayfarer_Core failed closed during enable" 90
send_command "stop"
exec {server_input_fd}>&-
wait "$server_pid"
server_pid=""
assert_no_sensitive_output "$mariadb_failure_log"

cat > "$evidence_root/summary.txt" <<SUMMARY
result=PASS
paper=1.21.11-build-132
paper_sha256=$paper_sha256
java=25
core_version=0.0.1-rc.1
core_sha256=$core_sha256
core_runtime_jar_count=1
config_version=1
migration_latest=V003
migration_count=3
provider=fixture-automated-only
concrete_provider=BLOCKED_ADR_0006
project_runtime_changed=no
SUMMARY
