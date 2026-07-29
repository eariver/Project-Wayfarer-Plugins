#!/usr/bin/env bash

# Shared V0.0.x release grammar. Callers are expected to enable `set -euo pipefail`.

is_stable_version() {
  [[ "${1:-}" =~ ^V0\.0\.[1-9][0-9]*[a-z]?$ ]]
}

is_prerelease_version() {
  [[ "${1:-}" =~ ^V0\.0\.[1-9][0-9]*-[0-9A-Za-z][0-9A-Za-z.-]*$ ]]
}

is_release_scope() {
  case "${1:-}" in
    core|main-frontier|all) return 0 ;;
    *) return 1 ;;
  esac
}

stable_version_key() {
  local version="${1:-}"
  is_stable_version "$version" || return 1

  local suffix="${version: -1}"
  local patch="${version#V0.0.}"
  local suffix_order=0
  if [[ "$suffix" =~ [a-z] ]]; then
    patch="${patch%?}"
    suffix_order="$(( $(printf '%d' "'$suffix") - $(printf '%d' "'a") + 1 ))"
  fi

  printf '%010d:%03d\n' "$patch" "$suffix_order"
}

scope_includes_core() {
  [[ "${1:-}" == "core" || "${1:-}" == "all" ]]
}

scope_includes_main_frontier() {
  [[ "${1:-}" == "main-frontier" || "${1:-}" == "all" ]]
}
