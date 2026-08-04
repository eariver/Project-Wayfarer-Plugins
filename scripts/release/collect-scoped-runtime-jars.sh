#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=release-policy.sh
source "$script_dir/release-policy.sh"

: "${RELEASE_VERSION:?RELEASE_VERSION is required}"
: "${RELEASE_SCOPE:?RELEASE_SCOPE is required}"
: "${OUTPUT_DIR:?OUTPUT_DIR is required}"

is_release_scope "$RELEASE_SCOPE" \
  || {
    echo "Release scope must be core, main-frontier, or all." >&2
    exit 1
  }
[[ "$RELEASE_VERSION" =~ ^V0\.0\.[1-9][0-9]*([a-z]|-[0-9A-Za-z][0-9A-Za-z.-]*)?$ ]] \
  || {
    echo "Release version is invalid." >&2
    exit 1
  }

mkdir -p "$OUTPUT_DIR"

copy_plugin_jar() {
  local module_path="$1"
  local artifact_name="$2"
  mapfile -t jars < <(
    find "$module_path/build/libs" -maxdepth 1 -type f \
      -name '*.jar' \
      ! -name '*-sources.jar' \
      ! -name '*-javadoc.jar' \
      | sort
  )
  if [[ "${#jars[@]}" -ne 1 ]]; then
    echo "Expected exactly one runtime JAR for $module_path, found ${#jars[@]}." >&2
    exit 1
  fi
  cp -- "${jars[0]}" "$OUTPUT_DIR/${artifact_name}-${RELEASE_VERSION}.jar"
}

if scope_includes_core "$RELEASE_SCOPE"; then
  copy_plugin_jar "plugins/wayfarer-core" "Wayfarer_Core"
fi
if scope_includes_main_frontier "$RELEASE_SCOPE"; then
  copy_plugin_jar "plugins/wayfarer-main" "Wayfarer_Main"
  copy_plugin_jar "plugins/wayfarer-frontier" "Wayfarer_Frontier"
fi

find "$OUTPUT_DIR" -maxdepth 1 -type f -name 'Wayfarer_*.jar' -printf '%f\n' | sort
