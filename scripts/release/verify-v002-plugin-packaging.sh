#!/usr/bin/env bash
set -euo pipefail

verify_plugin() {
  local module="$1"
  local plugin_prefix="$2"
  shift 2
  local migrations=("$@")

  mapfile -t jars < <(
    find "plugins/$module/build/libs" -maxdepth 1 -type f -name '*.jar' \
      ! -name '*-sources.jar' \
      ! -name '*-javadoc.jar' \
      | sort
  )
  [[ "${#jars[@]}" -eq 1 ]] || {
    echo "$module must produce exactly one runtime JAR." >&2
    exit 1
  }
  jar_path="${jars[0]}"
  entries="$(mktemp)"
  trap 'rm -f -- "$entries"' RETURN
  unzip -Z1 "$jar_path" > "$entries"

  for migration in "${migrations[@]}"; do
    [[ "$(grep -Fxc -- "$migration" "$entries")" -eq 1 ]] || {
      echo "$module migration is missing or duplicated: $migration" >&2
      exit 1
    }
  done

  if grep -Eq \
    '(^|/)(src/test|test-results|org/junit|org/testcontainers|com/github/dockerjava)(/|$)|^io/github/eariver/wayfarer/api/|^io/github/eariver/wayfarer/core/|^io/github/eariver/wayfarer/(main|frontier)/.*Test|(^|/)(application|runtime|server)\.(yml|yaml|properties)$|^(\.env|secrets?[^/]*|worlds?|logs?|cache|runtime-data|database)(/|$)|\.(db|sqlite|sql\.dump|log)$' \
    "$entries"
  then
    echo "$module contains a forbidden API/Core/test/secret/runtime entry." >&2
    exit 1
  fi

  [[ "$(grep -Ec "^${plugin_prefix}/.*\\.class$" "$entries")" -gt 0 ]] || {
    echo "$module contains no expected implementation classes." >&2
    exit 1
  }
}

verify_plugin \
  wayfarer-main \
  io/github/eariver/wayfarer/main \
  db/migration/main/V001__growth_tool_schema.sql \
  db/migration/main/V002__growth_tool_repair_recovery.sql

verify_plugin \
  wayfarer-frontier \
  io/github/eariver/wayfarer/frontier \
  db/migration/frontier/V001__worlds_beyond_schema.sql \
  db/migration/frontier/V002__purchase_and_launchpad_recovery.sql

frontier_jar="$(
  find plugins/wayfarer-frontier/build/libs -maxdepth 1 -type f -name '*.jar' \
    ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print -quit
)"
if unzip -Z1 "$frontier_jar" \
  | grep -Eq 'EliteMobsMVI|frontier/(waystone|ruined)/|WaystonePlugin'; then
  echo "Deferred Waystone or unauthorized EM-MVI runtime code is packaged." >&2
  exit 1
fi

echo "PASS: V0.0.2 Main/Frontier packaging boundaries"
