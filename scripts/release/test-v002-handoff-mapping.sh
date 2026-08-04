#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
assembler="$script_dir/assemble-stable-release-package.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT

export RELEASE_VERSION=V0.0.2
export RELEASE_SCOPE=main-frontier
export HANDOFF_SOURCE_COMMIT
HANDOFF_SOURCE_COMMIT="$(git rev-parse HEAD)"
export TEST_EVIDENCE=docs/testing/results/V0.0.1-stable-local-acceptance.md
export MAIN_SERVER_INSTRUCTION=docs/requirements/main-server/Project-Wayfarer-V0.1.0/Project_Wayfarer_Plugin_Implementation_Test_Release_and_Mainline_Handoff_Requirements.md
export REQUIREMENT_TRACEABILITY=docs/requirements/main-server/Project-Wayfarer-V0.1.0/V0.0.2/traceability.md
export RELEASE_READINESS=docs/handoff/V0.0.2/release-readiness.md
export PLUGIN_TEST_REPORT=docs/reports/Project_Wayfarer_Plugin_Release_Test_Report_V0.0.2_2026-07-30.md

snapshot_case() {
  export SNAPSHOT_DIR="$1"
  bash "$assembler" snapshot
}

snapshot_case "$temp_root/positive"
test -f "$temp_root/positive/assets/CONFIG_DEFAULT_PROPOSALS.md"
test -f "$temp_root/positive/assets/COMPATIBILITY_MATRIX.md"
test -f "$temp_root/positive/assets/EVIDENCE_INDEX.md"
echo "PASS: V0.0.2 handoff mapping"

RELEASE_VERSION=V0.0.2a snapshot_case "$temp_root/correction"
echo "PASS: V0.0.2 correction reuses the reviewed base handoff"

if RELEASE_VERSION=V0.0.3 SNAPSHOT_DIR="$temp_root/wrong-version" \
    bash "$assembler" snapshot >/dev/null 2>&1; then
  echo "Wrong-version handoff mapping unexpectedly passed." >&2
  exit 1
fi
echo "PASS: rejected wrong-version handoff mapping"

if PLUGIN_TEST_REPORT=docs/reports/missing.md SNAPSHOT_DIR="$temp_root/missing" \
    bash "$assembler" snapshot >/dev/null 2>&1; then
  echo "Missing handoff source unexpectedly passed." >&2
  exit 1
fi
echo "PASS: rejected missing handoff source"

if PLUGIN_TEST_REPORT=../LICENSE SNAPSHOT_DIR="$temp_root/traversal" \
    bash "$assembler" snapshot >/dev/null 2>&1; then
  echo "Handoff path traversal unexpectedly passed." >&2
  exit 1
fi
echo "PASS: rejected handoff path traversal"

grep -Fq 'RELEASE_VERSION="$RELEASE_VERSION" \' .github/workflows/release.yml \
  || {
    echo "Stable workflow does not pass the derived handoff version." >&2
    exit 1
  }
grep -Fq 'record_component' .github/workflows/prerelease.yml \
  || {
    echo "Pre-release workflow does not record scoped component metadata." >&2
    exit 1
  }
echo "PASS: workflows bind handoff version and scoped component metadata"
