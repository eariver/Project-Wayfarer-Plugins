#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=release-policy.sh
source "$script_dir/release-policy.sh"

for version in V0.0.1 V0.0.1a V0.0.1z V0.0.2 V0.0.10b; do
  is_stable_version "$version" || {
    echo "Expected stable version was rejected: $version" >&2
    exit 1
  }
done

for version in v0.0.1 V0.0.0 V0.0.01 V0.0.1A V0.0.1-alpha.1 V0.1.0; do
  if is_stable_version "$version"; then
    echo "Invalid stable version was accepted: $version" >&2
    exit 1
  fi
done

for version in V0.0.2-alpha.1 V0.0.2-rc.3 V0.0.12-test-server.1; do
  is_prerelease_version "$version" || {
    echo "Expected pre-release version was rejected: $version" >&2
    exit 1
  }
done

for scope in core main-frontier all; do
  is_release_scope "$scope" || {
    echo "Expected release scope was rejected: $scope" >&2
    exit 1
  }
done

for scope in core-main core-frontier main frontier everything; do
  if is_release_scope "$scope"; then
    echo "Unsupported release scope was accepted: $scope" >&2
    exit 1
  fi
done

versions=(V0.0.1 V0.0.1a V0.0.1b V0.0.2 V0.0.10)
previous=""
for version in "${versions[@]}"; do
  key="$(stable_version_key "$version")"
  if [[ -n "$previous" && ! "$previous" < "$key" ]]; then
    echo "Stable ordering failed at $version ($previous !< $key)" >&2
    exit 1
  fi
  previous="$key"
done

echo "PASS: V0.0.x version grammar, correction ordering, and release scopes"
