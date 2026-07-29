#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
assembler="$script_dir/assemble-stable-release-package.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT

export RELEASE_VERSION="V0.0.1"
export TAG="V0.0.1"
export RELEASE_SCOPE="core"
export PRODUCT_SOURCE_COMMIT="49e00e21716c1c13a2dbb170fdad1b19c4275612"
export HANDOFF_SOURCE_COMMIT
HANDOFF_SOURCE_COMMIT="$(git rev-parse HEAD)"
export TEST_EVIDENCE="docs/testing/results/V0.0.1-stable-local-acceptance.md"
export MAIN_SERVER_INSTRUCTION="docs/requirements/main-server/Project-Wayfarer-V0.1.0/Project_Wayfarer_Plugin_Implementation_Test_Release_and_Mainline_Handoff_Requirements.md"
export REQUIREMENT_TRACEABILITY="docs/requirements/main-server/Project-Wayfarer-V0.1.0/traceability.md"
export RELEASE_READINESS="docs/handoff/V0.0.1/release-readiness.md"
export SNAPSHOT_DIR="$temp_root/snapshot"
export PACKAGE_ROOT="$temp_root/package"
export PRODUCT_JAR="$temp_root/wayfarer-core-0.0.1.jar"
export DEPENDENCY_VERSIONS_SOURCE="gradle/libs.versions.toml"
export CONFIG_VERSION="1"
export MIGRATION_VERSION="V003"
export GITHUB_SERVER_URL="https://github.com"
export GITHUB_REPOSITORY="eariver/Project-Wayfarer-Plugins"
export GITHUB_RUN_ID="package-test"

printf 'deterministic stable package fixture\n' > "$PRODUCT_JAR"
export EXPECTED_STABLE_SHA256
EXPECTED_STABLE_SHA256="$(sha256sum "$PRODUCT_JAR" | awk '{ print toupper($1) }')"

bash "$assembler" snapshot
bash "$assembler" assemble
bash "$assembler" verify
echo "PASS: stable release package positive validation"

expect_verify_failure() {
  local name="$1"
  local mutation="$2"
  local case_root="$temp_root/case-${name//[^A-Za-z0-9]/-}"
  cp -R -- "$PACKAGE_ROOT" "$case_root"
  PACKAGE_ROOT="$case_root" bash -c "$mutation"
  if PACKAGE_ROOT="$case_root" bash "$assembler" verify >/dev/null 2>&1; then
    echo "Negative package validation unexpectedly passed: $name" >&2
    exit 1
  fi
  echo "PASS: rejected $name"
}

expect_verify_failure \
  "missing Release Notes" \
  'rm -f -- "$PACKAGE_ROOT/RELEASE_NOTES.md"'
expect_verify_failure \
  "missing Asset Filename list" \
  'rm -f -- "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"'
expect_verify_failure \
  "asset list count not 19" \
  'sed -i "1d" "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"'
expect_verify_failure \
  "missing Sanitized Config" \
  'rm -f -- "$PACKAGE_ROOT/assets/SANITIZED_CONFIGURATION.md"'
expect_verify_failure \
  "missing Command Reference" \
  'rm -f -- "$PACKAGE_ROOT/assets/COMMAND_AND_PERMISSION_REFERENCE.md"'
expect_verify_failure \
  "missing Dependency Placement" \
  'rm -f -- "$PACKAGE_ROOT/assets/DEPENDENCY_AND_PLACEMENT.md"'
expect_verify_failure \
  "missing Third-party Notice" \
  'rm -f -- "$PACKAGE_ROOT/assets/THIRD_PARTY_NOTICES.md"'
expect_verify_failure \
  "missing Plugin Test Report" \
  'rm -f -- "$PACKAGE_ROOT/assets/PLUGIN_TEST_REPORT.md"'
expect_verify_failure \
  "missing Known Limitations" \
  'rm -f -- "$PACKAGE_ROOT/assets/KNOWN_LIMITATIONS.md"'
expect_verify_failure \
  "missing Rollback" \
  'rm -f -- "$PACKAGE_ROOT/assets/UPGRADE_AND_ROLLBACK.md"'
expect_verify_failure \
  "missing Project Acceptance Input" \
  'rm -f -- "$PACKAGE_ROOT/assets/PROJECT_ACCEPTANCE_INPUT.md"'
expect_verify_failure \
  "missing License" \
  'rm -f -- "$PACKAGE_ROOT/assets/LICENSE"'
expect_verify_failure \
  "duplicate release filename" \
  'printf "%s\n" "LICENSE" >> "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"'
expect_verify_failure \
  "unsafe release filename" \
  'sed -i "1s|.*|../unsafe.jar|" "$PACKAGE_ROOT/RELEASE_ASSET_FILENAMES.txt"'
expect_verify_failure \
  "listed asset missing" \
  'rm -f -- "$PACKAGE_ROOT/assets/Wayfarer_Core-V0.0.1.jar"'
expect_verify_failure \
  "missing Handoff Source Commit" \
  'sed -i "/^- Handoff source commit:/d" "$PACKAGE_ROOT/assets/RELEASE_MANIFEST.md"'
expect_verify_failure \
  "missing Artifact Matrix Release URL" \
  'sed -i "/^- Release URL:/d" "$PACKAGE_ROOT/assets/ARTIFACT_MATRIX.md"'
expect_verify_failure \
  "missing SHA256SUMS coverage" \
  'sed -i "/  LICENSE$/d" "$PACKAGE_ROOT/assets/SHA256SUMS.txt"'
expect_verify_failure \
  "checksum mismatch" \
  'printf "%s\n" "tampered" >> "$PACKAGE_ROOT/assets/LICENSE"'

grep -Fq \
  'bash scripts/release/test-stable-release-package.sh' \
  .github/workflows/ci.yml \
  || {
    echo "CI does not run the stable release package test." >&2
    exit 1
  }
grep -Fq \
  'mapfile -t release_asset_names < release/RELEASE_ASSET_FILENAMES.txt' \
  .github/workflows/release.yml \
  || {
    echo "Release workflow does not consume the validated attachment list." >&2
    exit 1
  }
grep -Fq \
  '"${release_assets[@]}"' \
  .github/workflows/release.yml \
  || {
    echo "GitHub Release creation does not attach the validated asset array." >&2
    exit 1
  }
echo "PASS: workflow consumes the validated release attachment list"
