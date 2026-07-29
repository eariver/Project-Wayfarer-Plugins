#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "Scoped stable package error: $*" >&2
  exit 1
}

require_value() {
  local name="$1"
  [[ -n "${!name:-}" ]] || fail "$name is required."
}

validate_common() {
  require_value PACKAGE_ROOT
  require_value RELEASE_VERSION
  require_value TAG
  require_value RELEASE_SCOPE
  require_value PRODUCT_SOURCE_COMMIT
  require_value HANDOFF_SOURCE_COMMIT
  require_value RUNTIME_JAR_MANIFEST
  require_value SNAPSHOT_DIR
  require_value DEPENDENCY_VERSIONS_SOURCE

  script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  # shellcheck source=release-policy.sh
  source "$script_dir/release-policy.sh"
  is_stable_version "$RELEASE_VERSION" || fail "Stable release version is invalid."
  [[ "$RELEASE_VERSION" == "$TAG" ]] || fail "Version and tag must match."
  is_release_scope "$RELEASE_SCOPE" || fail "Release scope is invalid."
  [[ "$RELEASE_SCOPE" != "core" ]] \
    || fail "Core-only publication must use the immutable V0.0.1-compatible assembler."
  [[ "$PRODUCT_SOURCE_COMMIT" =~ ^[0-9a-f]{40}$ ]] \
    || fail "Product source commit is invalid."
  [[ "$HANDOFF_SOURCE_COMMIT" =~ ^[0-9a-f]{40}$ ]] \
    || fail "Handoff source commit is invalid."
  [[ -f "$RUNTIME_JAR_MANIFEST" && ! -L "$RUNTIME_JAR_MANIFEST" ]] \
    || fail "Runtime JAR manifest is missing."
  [[ -d "$SNAPSHOT_DIR/assets" && ! -L "$SNAPSHOT_DIR/assets" ]] \
    || fail "Handoff snapshot is missing."
}

expected_runtime_names() {
  case "$RELEASE_SCOPE" in
    main-frontier)
      printf '%s\n' \
        "Wayfarer_Main-${RELEASE_VERSION}.jar" \
        "Wayfarer_Frontier-${RELEASE_VERSION}.jar"
      ;;
    all)
      printf '%s\n' \
        "Wayfarer_Core-${RELEASE_VERSION}.jar" \
        "Wayfarer_Main-${RELEASE_VERSION}.jar" \
        "Wayfarer_Frontier-${RELEASE_VERSION}.jar"
      ;;
    *) fail "Unsupported scoped package: $RELEASE_SCOPE" ;;
  esac
}

validate_runtime_manifest() {
  declare -A seen=()
  while IFS='|' read -r filename source_path expected_hash; do
    [[ "$filename" =~ ^Wayfarer_(Core|Main|Frontier)-V0\.0\.[-A-Za-z0-9.]+\.jar$ ]] \
      || fail "Unsafe runtime filename: $filename"
    [[ -z "${seen[$filename]:-}" ]] || fail "Duplicate runtime filename: $filename"
    seen["$filename"]=1
    verification_path="$source_path"
    if [[ ! -f "$verification_path" && -f "$PACKAGE_ROOT/assets/$filename" ]]; then
      verification_path="$PACKAGE_ROOT/assets/$filename"
    fi
    [[ -f "$verification_path" && ! -L "$verification_path" ]] \
      || fail "Runtime JAR is missing: $filename"
    [[ "$expected_hash" =~ ^[0-9A-F]{64}$ ]] \
      || fail "Runtime hash is invalid: $filename"
    actual_hash="$(sha256sum "$verification_path" | awk '{ print toupper($1) }')"
    [[ "$actual_hash" == "$expected_hash" ]] \
      || fail "Runtime JAR hash mismatch: $filename"
  done < "$RUNTIME_JAR_MANIFEST"

  mapfile -t expected < <(expected_runtime_names | sort)
  mapfile -t actual < <(printf '%s\n' "${!seen[@]}" | sort)
  [[ "${expected[*]}" == "${actual[*]}" ]] \
    || fail "Runtime JAR manifest does not match release scope."
}

assemble() {
  validate_common
  validate_runtime_manifest
  [[ ! -e "$PACKAGE_ROOT" ]] || fail "Package destination already exists."

  assets="$PACKAGE_ROOT/assets"
  mkdir -p "$assets"
  cp -- "$DEPENDENCY_VERSIONS_SOURCE" "$assets/DEPENDENCY_VERSIONS.toml"
  cp -- "$SNAPSHOT_DIR/assets/"* "$assets/"

  while IFS='|' read -r filename source_path expected_hash; do
    cp -- "$source_path" "$assets/$filename"
  done < "$RUNTIME_JAR_MANIFEST"

  release_url="${GITHUB_SERVER_URL:-https://github.com}/${GITHUB_REPOSITORY:-unknown}/releases/tag/${TAG}"
  cat > "$assets/ARTIFACT_MATRIX.md" <<EOF
# Project Wayfarer ${RELEASE_VERSION} Artifact Matrix

- Release tag: \`${TAG}\`
- Release URL: ${release_url}
- Release scope: \`${RELEASE_SCOPE}\`
- Product source commit: \`${PRODUCT_SOURCE_COMMIT}\`
- Handoff source commit: \`${HANDOFF_SOURCE_COMMIT}\`
- Core compatibility: \`>=0.0.1 <0.1.0\`
- Project Runtime acceptance: pending
- Waystone: deferred by requirement
- EM–MVI adapter: not authorized / not included

| Runtime artifact | SHA-256 |
|---|---|
EOF
  while IFS='|' read -r filename source_path expected_hash; do
    printf '| `%s` | `%s` |\n' "$filename" "$expected_hash" \
      >> "$assets/ARTIFACT_MATRIX.md"
  done < "$RUNTIME_JAR_MANIFEST"

  {
    expected_runtime_names
    find "$assets" -maxdepth 1 -type f -printf '%f\n' \
      | grep -vE '^(SHA256SUMS.txt|RELEASE_MANIFEST.md|Wayfarer_.*\.jar)$'
    printf '%s\n' SHA256SUMS.txt RELEASE_MANIFEST.md
  } | sort -u > "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"

  (
    cd "$assets"
    while IFS= read -r filename; do
      case "$filename" in
        SHA256SUMS.txt|RELEASE_MANIFEST.md) ;;
        *) sha256sum --text "$filename" ;;
      esac
    done < "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"
  ) > "$assets/SHA256SUMS.txt"

  cat > "$assets/RELEASE_MANIFEST.md" <<EOF
# Project Wayfarer Plugins Release Manifest

- Release type: Stable / main-server handoff package
- Version: ${RELEASE_VERSION}
- Tag: ${TAG}
- Release URL: ${release_url}
- Release scope: ${RELEASE_SCOPE}
- Product source commit: ${PRODUCT_SOURCE_COMMIT}
- Handoff source commit: ${HANDOFF_SOURCE_COMMIT}
- Core product reused: $([[ "$RELEASE_SCOPE" == "main-frontier" ]] && echo "V0.0.1 / 49e00e21716c1c13a2dbb170fdad1b19c4275612" || echo "No")
- Project Runtime placement/acceptance: pending / Project-owned
- Automatic deployment: not performed

## Runtime artifact SHA-256

EOF
  while IFS='|' read -r filename source_path expected_hash; do
    printf -- '- `%s`: `%s`\n' "$filename" "$expected_hash" \
      >> "$assets/RELEASE_MANIFEST.md"
  done < "$RUNTIME_JAR_MANIFEST"

  cat > "$PACKAGE_ROOT/RELEASE_NOTES.md" <<EOF
## Status

This package contains the reviewed \`${RELEASE_SCOPE}\` artifact scope. Project Runtime
installation, migration execution, configuration, restart, acceptance, and rollback remain
Project-owned and are not performed by this workflow.

- Product source commit: \`${PRODUCT_SOURCE_COMMIT}\`
- Handoff source commit: \`${HANDOFF_SOURCE_COMMIT}\`
- Release URL: ${release_url}
EOF
}

verify() {
  validate_common
  validate_runtime_manifest
  assets="$PACKAGE_ROOT/assets"
  list="$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"
  [[ -f "$list" && -f "$PACKAGE_ROOT/RELEASE_NOTES.md" ]] \
    || fail "Package control files are missing."
  duplicate="$(sort "$list" | uniq -d | head -n 1)"
  [[ -z "$duplicate" ]] || fail "Duplicate release asset: $duplicate"
  while IFS= read -r filename; do
    [[ "$filename" =~ ^[-A-Za-z0-9_.]+$ ]] || fail "Unsafe release asset name."
    [[ -f "$assets/$filename" && ! -L "$assets/$filename" ]] \
      || fail "Listed release asset is missing: $filename"
  done < "$list"
  (
    cd "$assets"
    sha256sum --check SHA256SUMS.txt >/dev/null
  ) || fail "Release checksums failed."
  while IFS= read -r expected; do
    [[ "$(grep -Fxc -- "$expected" "$list")" -eq 1 ]] \
      || fail "Scoped runtime asset is missing: $expected"
  done < <(expected_runtime_names)
  [[ "$(find "$assets" -maxdepth 1 -type f -name 'Wayfarer_*.jar' | wc -l | tr -d ' ')" \
      -eq "$(expected_runtime_names | wc -l | tr -d ' ')" ]] \
    || fail "Unexpected runtime JAR is present."
}

case "${1:-}" in
  assemble) assemble ;;
  verify) verify ;;
  *) fail "Usage: $0 {assemble|verify}" ;;
esac
