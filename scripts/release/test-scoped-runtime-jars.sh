#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
collector="$script_dir/collect-scoped-runtime-jars.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT

for module in core main frontier; do
  mkdir -p "$temp_root/source/plugins/wayfarer-$module/build/libs"
  printf '%s fixture\n' "$module" \
    > "$temp_root/source/plugins/wayfarer-$module/build/libs/wayfarer-$module.jar"
done

run_scope() {
  local scope="$1"
  local expected_count="$2"
  local output="$temp_root/output-$scope"
  (
    cd "$temp_root/source"
    RELEASE_VERSION=V0.0.2-rc.1 \
      RELEASE_SCOPE="$scope" \
      OUTPUT_DIR="$output" \
      bash "$collector" >/dev/null
  )
  actual_count="$(find "$output" -maxdepth 1 -type f -name '*.jar' | wc -l | tr -d ' ')"
  [[ "$actual_count" -eq "$expected_count" ]] \
    || {
      echo "$scope produced $actual_count JARs; expected $expected_count." >&2
      exit 1
    }
}

run_scope core 1
run_scope main-frontier 2
run_scope all 3

[[ ! -e "$temp_root/output-main-frontier/Wayfarer_Core-V0.0.2-rc.1.jar" ]] \
  || {
    echo "main-frontier scope must reuse Core rather than package it." >&2
    exit 1
  }

if (
  cd "$temp_root/source"
  RELEASE_VERSION=V0.0.2-rc.1 \
    RELEASE_SCOPE=core-main \
    OUTPUT_DIR="$temp_root/invalid" \
    bash "$collector" >/dev/null 2>&1
); then
  echo "Unsupported partial scope was accepted." >&2
  exit 1
fi

echo "PASS: scoped runtime JAR collection"
