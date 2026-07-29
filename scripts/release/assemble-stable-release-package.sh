#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "Stable release package error: $*" >&2
  exit 1
}

require_value() {
  local name="$1"
  [[ -n "${!name:-}" ]] || fail "$name is required."
}

handoff_mappings() {
  require_value TEST_EVIDENCE
  require_value MAIN_SERVER_INSTRUCTION
  require_value REQUIREMENT_TRACEABILITY
  require_value RELEASE_READINESS

  cat <<EOF
docs/handoff/V0.0.1/sanitized-configuration.md|SANITIZED_CONFIGURATION.md
docs/handoff/V0.0.1/command-and-permission-reference.md|COMMAND_AND_PERMISSION_REFERENCE.md
docs/handoff/V0.0.1/dependency-and-placement.md|DEPENDENCY_AND_PLACEMENT.md
docs/handoff/V0.0.1/third-party-notices.md|THIRD_PARTY_NOTICES.md
docs/handoff/V0.0.1/known-limitations.md|KNOWN_LIMITATIONS.md
docs/handoff/V0.0.1/upgrade-and-rollback.md|UPGRADE_AND_ROLLBACK.md
docs/handoff/V0.0.1/project-acceptance-input.md|PROJECT_ACCEPTANCE_INPUT.md
docs/handoff/V0.0.1/artifact-inventory.md|ARTIFACT_INVENTORY.md
docs/reports/Project_Wayfarer_Plugin_Release_Test_Report_V0.0.1_2026-07-29.md|PLUGIN_TEST_REPORT.md
LICENSE|LICENSE
${TEST_EVIDENCE}|TEST_SERVER_EVIDENCE.md
${MAIN_SERVER_INSTRUCTION}|MAIN_SERVER_INSTRUCTION.md
${REQUIREMENT_TRACEABILITY}|REQUIREMENT_TRACEABILITY.md
${RELEASE_READINESS}|RELEASE_READINESS.md
EOF
}

required_release_assets() {
  require_value RELEASE_VERSION
  cat <<EOF
Wayfarer_Core-${RELEASE_VERSION}.jar
SHA256SUMS.txt
RELEASE_MANIFEST.md
DEPENDENCY_VERSIONS.toml
TEST_SERVER_EVIDENCE.md
PLUGIN_TEST_REPORT.md
MAIN_SERVER_INSTRUCTION.md
REQUIREMENT_TRACEABILITY.md
RELEASE_READINESS.md
SANITIZED_CONFIGURATION.md
COMMAND_AND_PERMISSION_REFERENCE.md
DEPENDENCY_AND_PLACEMENT.md
THIRD_PARTY_NOTICES.md
LICENSE
KNOWN_LIMITATIONS.md
UPGRADE_AND_ROLLBACK.md
ARTIFACT_MATRIX.md
PROJECT_ACCEPTANCE_INPUT.md
ARTIFACT_INVENTORY.md
EOF
}

snapshot_handoff() {
  require_value SNAPSHOT_DIR
  require_value HANDOFF_SOURCE_COMMIT

  [[ "$HANDOFF_SOURCE_COMMIT" =~ ^[0-9a-f]{40}$ ]] \
    || fail "HANDOFF_SOURCE_COMMIT must be an exact lowercase commit SHA."
  [[ "$(git rev-parse HEAD)" == "$HANDOFF_SOURCE_COMMIT" ]] \
    || fail "Handoff source commit must equal the checked-out automation revision."
  [[ ! -e "$SNAPSHOT_DIR" ]] \
    || fail "Snapshot destination already exists: $SNAPSHOT_DIR"

  mkdir -p "$SNAPSHOT_DIR/assets"
  : > "$SNAPSHOT_DIR/HANDOFF_ASSET_INDEX.tsv"
  printf '%s\n' "$HANDOFF_SOURCE_COMMIT" > "$SNAPSHOT_DIR/HANDOFF_SOURCE_COMMIT"

  declare -A release_names=()
  while IFS='|' read -r source_path release_name; do
    [[ -n "$source_path" && -n "$release_name" ]] \
      || fail "Empty source path or release filename in handoff mapping."
    [[ "$source_path" != /* && "$source_path" != *".."* ]] \
      || fail "Unsafe handoff source path: $source_path"
    [[ "$release_name" =~ ^[-A-Za-z0-9_.]+$ ]] \
      || fail "Unsafe release filename: $release_name"
    [[ -z "${release_names[$release_name]:-}" ]] \
      || fail "Duplicate release filename: $release_name"
    release_names["$release_name"]=1

    git ls-files --error-unmatch -- "$source_path" >/dev/null 2>&1 \
      || fail "Handoff source is not tracked: $source_path"
    [[ -f "$source_path" && ! -L "$source_path" ]] \
      || fail "Handoff source must be a regular non-symlink file: $source_path"
    [[ "$(git ls-files -s -- "$source_path" | awk 'NR == 1 { print $1 }')" != "120000" ]] \
      || fail "Handoff source symlinks are not permitted: $source_path"

    cp -- "$source_path" "$SNAPSHOT_DIR/assets/$release_name"
    source_sha="$(sha256sum "$SNAPSHOT_DIR/assets/$release_name" | awk '{ print $1 }')"
    printf '%s\t%s\t%s\n' \
      "$source_path" \
      "$release_name" \
      "$source_sha" \
      >> "$SNAPSHOT_DIR/HANDOFF_ASSET_INDEX.tsv"
  done < <(handoff_mappings)
}

assemble_package() {
  require_value SNAPSHOT_DIR
  require_value PACKAGE_ROOT
  require_value RELEASE_VERSION
  require_value TAG
  require_value RELEASE_SCOPE
  require_value PRODUCT_SOURCE_COMMIT
  require_value HANDOFF_SOURCE_COMMIT
  require_value EXPECTED_STABLE_SHA256
  require_value PRODUCT_JAR
  require_value DEPENDENCY_VERSIONS_SOURCE
  require_value CONFIG_VERSION
  require_value MIGRATION_VERSION
  require_value GITHUB_SERVER_URL
  require_value GITHUB_REPOSITORY
  require_value GITHUB_RUN_ID

  [[ "$PRODUCT_SOURCE_COMMIT" =~ ^[0-9a-f]{40}$ ]] \
    || fail "PRODUCT_SOURCE_COMMIT must be an exact lowercase commit SHA."
  [[ "$HANDOFF_SOURCE_COMMIT" =~ ^[0-9a-f]{40}$ ]] \
    || fail "HANDOFF_SOURCE_COMMIT must be an exact lowercase commit SHA."
  [[ "$EXPECTED_STABLE_SHA256" =~ ^[0-9A-F]{64}$ ]] \
    || fail "EXPECTED_STABLE_SHA256 must be uppercase hexadecimal."
  [[ "$RELEASE_VERSION" == "$TAG" ]] \
    || fail "Release version and tag must be identical."
  [[ "$RELEASE_SCOPE" == "core" ]] \
    || fail "Only the Core stable release package is supported."
  [[ -f "$PRODUCT_JAR" && ! -L "$PRODUCT_JAR" ]] \
    || fail "Product JAR must be a regular non-symlink file."
  [[ -f "$DEPENDENCY_VERSIONS_SOURCE" && ! -L "$DEPENDENCY_VERSIONS_SOURCE" ]] \
    || fail "Dependency versions source must be a regular non-symlink file."
  [[ -f "$SNAPSHOT_DIR/HANDOFF_ASSET_INDEX.tsv" ]] \
    || fail "Handoff asset index is missing."
  [[ "$(cat "$SNAPSHOT_DIR/HANDOFF_SOURCE_COMMIT")" == "$HANDOFF_SOURCE_COMMIT" ]] \
    || fail "Handoff source commit is missing or does not match the snapshot."
  [[ ! -e "$PACKAGE_ROOT" ]] \
    || fail "Package destination already exists: $PACKAGE_ROOT"

  actual_jar_sha="$(sha256sum "$PRODUCT_JAR" | awk '{ print toupper($1) }')"
  [[ "$actual_jar_sha" == "$EXPECTED_STABLE_SHA256" ]] \
    || fail "Product JAR SHA-256 does not match the stable candidate."

  assets_dir="$PACKAGE_ROOT/assets"
  mkdir -p "$assets_dir"

  declare -A copied_names=()
  while IFS=$'\t' read -r source_path release_name recorded_sha; do
    [[ -n "$source_path" && -n "$release_name" && "$recorded_sha" =~ ^[0-9a-f]{64}$ ]] \
      || fail "Malformed handoff asset index entry."
    [[ -z "${copied_names[$release_name]:-}" ]] \
      || fail "Duplicate release filename in handoff index: $release_name"
    copied_names["$release_name"]=1
    snapshot_asset="$SNAPSHOT_DIR/assets/$release_name"
    [[ -f "$snapshot_asset" && ! -L "$snapshot_asset" ]] \
      || fail "Snapshot asset is missing or not regular: $release_name"
    actual_snapshot_sha="$(sha256sum "$snapshot_asset" | awk '{ print $1 }')"
    [[ "$actual_snapshot_sha" == "$recorded_sha" ]] \
      || fail "Snapshot asset hash mismatch: $release_name"
    cp -- "$snapshot_asset" "$assets_dir/$release_name"
  done < "$SNAPSHOT_DIR/HANDOFF_ASSET_INDEX.tsv"

  jar_name="Wayfarer_Core-${RELEASE_VERSION}.jar"
  cp -- "$PRODUCT_JAR" "$assets_dir/$jar_name"
  cp -- "$DEPENDENCY_VERSIONS_SOURCE" "$assets_dir/DEPENDENCY_VERSIONS.toml"

  release_url="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG}"
  cat > "$assets_dir/ARTIFACT_MATRIX.md" <<EOF
# Project Wayfarer V0.0.1 Publication Artifact Matrix

- Release version: \`${RELEASE_VERSION}\`
- Release tag: \`${TAG}\`
- Release URL: ${release_url}
- Stable product source: \`${PRODUCT_SOURCE_COMMIT}\`
- Handoff source commit: \`${HANDOFF_SOURCE_COMMIT}\`
- Runtime JAR: \`${jar_name}\`
- SHA-256: \`${EXPECTED_STABLE_SHA256}\`
- Project placement: Main + Frontier / pending
- Project acceptance: pending
- Roadmap Order: 9 / pending
- Wayfarer_Main: not included
- Wayfarer_Frontier: not included
- Conditional EliteMobs-MVI adapter: not authorized / not included
EOF

  required_release_assets > "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"

  checksum_targets="$PACKAGE_ROOT/CHECKSUM_TARGETS.txt"
  while IFS= read -r filename; do
    case "$filename" in
      SHA256SUMS.txt|RELEASE_MANIFEST.md)
        ;;
      *)
        printf '%s\n' "$filename" >> "$checksum_targets"
        ;;
    esac
  done < "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"

  (
    cd "$assets_dir"
    while IFS= read -r filename; do
      [[ -f "$filename" && ! -L "$filename" ]] \
        || fail "Required checksum asset is missing or not regular: $filename"
      sha256sum "$filename"
    done < "$checksum_targets"
  ) > "$assets_dir/SHA256SUMS.txt"

  cat > "$assets_dir/RELEASE_MANIFEST.md" <<EOF
# Project Wayfarer Plugins Release Manifest

- Release type: Stable / main-server handoff package
- Version: ${RELEASE_VERSION}
- Tag: ${TAG}
- Release URL: ${release_url}
- Release scope: ${RELEASE_SCOPE}
- Product source commit: ${PRODUCT_SOURCE_COMMIT}
- Handoff source commit: ${HANDOFF_SOURCE_COMMIT}
- Config version: ${CONFIG_VERSION}
- Migration version: ${MIGRATION_VERSION}
- Source lineage: stable product source contained in main
- Stable JAR filename: ${jar_name}
- Stable JAR SHA-256: ${EXPECTED_STABLE_SHA256}
- Workflow run: ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}
- Pre-release: Not required by Owner Decision
- Plugin-side publication prerequisites: Confirmed cleared by workflow operator
- Stable source-side publication: Explicitly authorized by workflow operator
- Project Runtime placement/acceptance: Pending / Project-owned
- Automatic deployment: Not performed

Publishing this package does not install plugins, run migrations, modify runtime configuration,
restart servers, or constitute Project Runtime acceptance.

## Required release assets

EOF

  while IFS= read -r filename; do
    printf -- '- `%s`\n' "$filename" >> "$assets_dir/RELEASE_MANIFEST.md"
  done < "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"

  cat >> "$assets_dir/RELEASE_MANIFEST.md" <<'EOF'

`SHA256SUMS.txt` and `RELEASE_MANIFEST.md` are excluded from the checksum list to avoid
self-reference. Every other attached release asset is covered.

## Handoff snapshot provenance

| Source path | Release asset | SHA-256 |
|---|---|---|
EOF

  while IFS=$'\t' read -r source_path release_name recorded_sha; do
    printf '| `%s` | `%s` | `%s` |\n' \
      "$source_path" \
      "$release_name" \
      "$recorded_sha" \
      >> "$assets_dir/RELEASE_MANIFEST.md"
  done < "$SNAPSHOT_DIR/HANDOFF_ASSET_INDEX.tsv"

  cat >> "$assets_dir/RELEASE_MANIFEST.md" <<EOF
| \`gradle/libs.versions.toml\` at product source | \`DEPENDENCY_VERSIONS.toml\` | \`$(sha256sum "$assets_dir/DEPENDENCY_VERSIONS.toml" | awk '{ print $1 }')\` |
| Stable product build output | \`${jar_name}\` | \`$(sha256sum "$assets_dir/$jar_name" | awk '{ print $1 }')\` |
| Workflow-generated publication matrix | \`ARTIFACT_MATRIX.md\` | \`$(sha256sum "$assets_dir/ARTIFACT_MATRIX.md" | awk '{ print $1 }')\` |
EOF

  cat > "$PACKAGE_ROOT/RELEASE_NOTES.md" <<EOF
## Status

This stable Core-only release is ready for Project Wayfarer handoff.

- Product source commit: \`${PRODUCT_SOURCE_COMMIT}\`
- Handoff source commit: \`${HANDOFF_SOURCE_COMMIT}\`
- Stable JAR SHA-256: \`${EXPECTED_STABLE_SHA256}\`
- Release URL: ${release_url}
- Pre-release: not required by Owner Decision
- Owner authorization: Plugin-side publication prerequisites confirmed and stable source-side
  publication explicitly authorized
- Project Runtime placement/acceptance: pending / Project-owned

Runtime deployment, migration execution, configuration application, backups, restart operations,
acceptance evidence, and rollback decisions remain owned by the Project integration process.
EOF
}

verify_package() {
  require_value PACKAGE_ROOT
  require_value RELEASE_VERSION
  require_value TAG
  require_value PRODUCT_SOURCE_COMMIT
  require_value HANDOFF_SOURCE_COMMIT
  require_value EXPECTED_STABLE_SHA256
  require_value GITHUB_SERVER_URL
  require_value GITHUB_REPOSITORY

  assets_dir="$PACKAGE_ROOT/assets"
  asset_list="$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"
  release_notes="$PACKAGE_ROOT/RELEASE_NOTES.md"
  [[ -f "$release_notes" && ! -L "$release_notes" ]] \
    || fail "Release Notes must be a regular non-symlink file."
  [[ -f "$asset_list" && ! -L "$asset_list" ]] \
    || fail "Release asset filename list is missing."
  [[ -d "$assets_dir" && ! -L "$assets_dir" ]] \
    || fail "Release assets directory is missing or is a symlink."

  duplicate_name="$(
    sort "$asset_list" | uniq -d | head -n 1
  )"
  [[ -z "$duplicate_name" ]] \
    || fail "Duplicate release filename: $duplicate_name"

  mapfile -t expected_assets < <(required_release_assets)
  mapfile -t listed_assets < "$asset_list"
  [[ "${#listed_assets[@]}" -eq "${#expected_assets[@]}" ]] \
    || fail "Release asset list count does not match the required set."

  for filename in "${listed_assets[@]}"; do
    [[ "$filename" =~ ^[-A-Za-z0-9_.]+$ ]] \
      || fail "Unsafe release asset filename: $filename"
  done

  for filename in "${expected_assets[@]}"; do
    [[ "$(grep -Fxc -- "$filename" "$asset_list")" -eq 1 ]] \
      || fail "Required release asset is absent from the attachment list: $filename"
    [[ -f "$assets_dir/$filename" && ! -L "$assets_dir/$filename" ]] \
      || fail "Required release asset is missing or not regular: $filename"
  done

  checksum_file="$assets_dir/SHA256SUMS.txt"
  checksum_names="$(mktemp)"
  awk '{ print $2 }' "$checksum_file" > "$checksum_names"

  expected_checksum_count=0
  for filename in "${expected_assets[@]}"; do
    case "$filename" in
      SHA256SUMS.txt|RELEASE_MANIFEST.md)
        ;;
      *)
        expected_checksum_count=$((expected_checksum_count + 1))
        [[ "$(grep -Fxc -- "$filename" "$checksum_names")" -eq 1 ]] \
          || fail "SHA256SUMS coverage is missing: $filename"
        ;;
    esac
  done
  [[ "$(wc -l < "$checksum_names" | tr -d ' ')" -eq "$expected_checksum_count" ]] \
    || fail "SHA256SUMS contains an unexpected or duplicate target."
  (
    cd "$assets_dir"
    sha256sum --check SHA256SUMS.txt >/dev/null
  ) || fail "SHA256SUMS verification failed."

  release_url="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/releases/tag/${TAG}"
  manifest="$assets_dir/RELEASE_MANIFEST.md"
  matrix="$assets_dir/ARTIFACT_MATRIX.md"
  grep -Fxq -- "- Product source commit: ${PRODUCT_SOURCE_COMMIT}" "$manifest" \
    || fail "Manifest product source commit is missing."
  grep -Fxq -- "- Handoff source commit: ${HANDOFF_SOURCE_COMMIT}" "$manifest" \
    || fail "Manifest handoff source commit is missing."
  grep -Fxq -- "- Release URL: ${release_url}" "$manifest" \
    || fail "Manifest release URL is missing."
  grep -Fxq -- "- Stable JAR SHA-256: ${EXPECTED_STABLE_SHA256}" "$manifest" \
    || fail "Manifest stable JAR SHA-256 is missing."
  grep -Fxq -- "- Release tag: \`${TAG}\`" "$matrix" \
    || fail "Artifact Matrix release tag is missing."
  grep -Fxq -- "- Release URL: ${release_url}" "$matrix" \
    || fail "Artifact Matrix release URL is missing."
  grep -Fxq -- "- Stable product source: \`${PRODUCT_SOURCE_COMMIT}\`" "$matrix" \
    || fail "Artifact Matrix product source is missing."
  grep -Fxq -- "- Handoff source commit: \`${HANDOFF_SOURCE_COMMIT}\`" "$matrix" \
    || fail "Artifact Matrix handoff source is missing."
  grep -Fxq -- "- Runtime JAR: \`Wayfarer_Core-${RELEASE_VERSION}.jar\`" "$matrix" \
    || fail "Artifact Matrix runtime JAR is missing."
  grep -Fxq -- "- SHA-256: \`${EXPECTED_STABLE_SHA256}\`" "$matrix" \
    || fail "Artifact Matrix stable SHA-256 is missing."
  rm -f -- "$checksum_names"
}

case "${1:-}" in
  snapshot)
    snapshot_handoff
    ;;
  assemble)
    assemble_package
    ;;
  verify)
    verify_package
    ;;
  *)
    fail "Usage: $0 {snapshot|assemble|verify}"
    ;;
esac
