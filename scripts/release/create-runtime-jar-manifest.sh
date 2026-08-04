#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=release-policy.sh
source "$script_dir/release-policy.sh"

: "${RELEASE_VERSION:?RELEASE_VERSION is required}"
: "${RELEASE_SCOPE:?RELEASE_SCOPE is required}"
: "${TEST_EVIDENCE:?TEST_EVIDENCE is required}"
: "${OUTPUT_MANIFEST:?OUTPUT_MANIFEST is required}"

is_release_scope "$RELEASE_SCOPE" || {
  echo "Release scope is invalid." >&2
  exit 1
}
[[ -f "$TEST_EVIDENCE" ]] || {
  echo "Test evidence is missing." >&2
  exit 1
}

expected_hash() {
  local artifact="$1"
  local hash
  hash="$(
    sed -nE \
      "s/^- ${artifact} stable candidate SHA-256: \`([0-9A-Fa-f]{64})\`$/\\1/p" \
      "$TEST_EVIDENCE"
  )"
  [[ "$hash" =~ ^[0-9A-Fa-f]{64}$ && "$hash" != *$'\n'* ]] || {
    echo "Exactly one expected hash is required for $artifact." >&2
    exit 1
  }
  printf '%s' "${hash^^}"
}

runtime_jar() {
  local module_path="$1"
  mapfile -t jars < <(
    find "$module_path/build/libs" -maxdepth 1 -type f \
      -name '*.jar' \
      ! -name '*-sources.jar' \
      ! -name '*-javadoc.jar' \
      | sort
  )
  [[ "${#jars[@]}" -eq 1 ]] || {
    echo "Expected one runtime JAR under $module_path." >&2
    exit 1
  }
  printf '%s' "${jars[0]}"
}

: > "$OUTPUT_MANIFEST"
add_artifact() {
  local module_path="$1"
  local artifact="$2"
  local source_path
  source_path="$(runtime_jar "$module_path")"
  local hash
  hash="$(expected_hash "$artifact")"
  printf '%s-%s.jar|%s|%s\n' \
    "$artifact" "$RELEASE_VERSION" "$source_path" "$hash" \
    >> "$OUTPUT_MANIFEST"
}

if scope_includes_core "$RELEASE_SCOPE"; then
  add_artifact "plugins/wayfarer-core" "Wayfarer_Core"
fi
if scope_includes_main_frontier "$RELEASE_SCOPE"; then
  add_artifact "plugins/wayfarer-main" "Wayfarer_Main"
  add_artifact "plugins/wayfarer-frontier" "Wayfarer_Frontier"
fi
