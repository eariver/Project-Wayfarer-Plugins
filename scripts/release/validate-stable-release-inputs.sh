#!/usr/bin/env bash
set -euo pipefail

: "${INPUT_VERSION:?INPUT_VERSION is required}"
: "${RELEASE_SCOPE:?RELEASE_SCOPE is required}"
: "${STABLE_SOURCE_COMMIT:?STABLE_SOURCE_COMMIT is required}"
: "${TEST_EVIDENCE:?TEST_EVIDENCE is required}"
: "${MAIN_SERVER_INSTRUCTION:?MAIN_SERVER_INSTRUCTION is required}"
: "${REQUIREMENT_TRACEABILITY:?REQUIREMENT_TRACEABILITY is required}"
: "${RELEASE_READINESS:?RELEASE_READINESS is required}"
: "${REQUIREMENTS_CLEARED:?REQUIREMENTS_CLEARED is required}"

if [[ "$REQUIREMENTS_CLEARED" != "true" ]]; then
  echo "Main-server requirements must be explicitly confirmed as cleared." >&2
  exit 1
fi

if [[ ! "$INPUT_VERSION" =~ ^V0\.0\.[1-9][0-9]*$ ]]; then
  echo "Stable release version must resemble V0.0.1." >&2
  exit 1
fi

if [[ "$RELEASE_SCOPE" != "core" ]]; then
  echo "The current V0.0.x release policy permits only release_scope=core." >&2
  exit 1
fi

if [[ ! "$STABLE_SOURCE_COMMIT" =~ ^[0-9a-fA-F]{40}$ ]]; then
  echo "Stable source commit must be an exact 40-character hexadecimal SHA." >&2
  exit 1
fi

if ! git cat-file -e "${STABLE_SOURCE_COMMIT}^{commit}" 2>/dev/null; then
  echo "Stable source commit does not exist in this repository." >&2
  exit 1
fi

if ! git merge-base --is-ancestor "$STABLE_SOURCE_COMMIT" origin/main; then
  echo "Stable source commit is not contained in origin/main." >&2
  exit 1
fi

if git rev-parse -q --verify "refs/tags/$INPUT_VERSION" >/dev/null; then
  echo "Stable tag $INPUT_VERSION already exists." >&2
  exit 1
fi

validate_path() {
  local value="$1"
  local pattern="$2"
  local description="$3"
  if [[ "$value" == *".."* || ! "$value" =~ $pattern ]]; then
    echo "$description path is invalid: $value" >&2
    exit 1
  fi
  if ! git ls-files --error-unmatch "$value" >/dev/null 2>&1; then
    echo "$description is not committed on the selected automation revision: $value" >&2
    exit 1
  fi
}

validate_path \
  "$TEST_EVIDENCE" \
  '^docs/testing/results/[A-Za-z0-9._/-]+\.md$' \
  'Test evidence'
validate_path \
  "$MAIN_SERVER_INSTRUCTION" \
  '^docs/requirements/main-server/[A-Za-z0-9._/-]+\.md$' \
  'Main-server instruction'
validate_path \
  "$REQUIREMENT_TRACEABILITY" \
  '^docs/requirements/main-server/[A-Za-z0-9._/-]+\.md$' \
  'Requirement traceability'
validate_path \
  "$RELEASE_READINESS" \
  '^docs/handoff/[A-Za-z0-9._/-]+\.md$' \
  'Release readiness'

if ! grep -Fxq -- "- Release gate: CLEARED" "$REQUIREMENT_TRACEABILITY"; then
  echo "Requirement traceability release gate is not CLEARED." >&2
  exit 1
fi

if ! grep -Fxq -- "- Release readiness: READY" "$RELEASE_READINESS"; then
  echo "Release readiness is not READY." >&2
  exit 1
fi

if awk -F'|' '
  BEGIN { rejected = 0 }
  /^\|/ {
    status = $9
    gsub(/^[ \t]+|[ \t]+$/, "", status)
    if (status == "Not started" || status == "Failed" || status == "Blocked") {
      rejected = 1
    }
  }
  END { exit rejected ? 0 : 1 }
' "$REQUIREMENT_TRACEABILITY"; then
  echo "Requirement traceability contains a rejected status." >&2
  exit 1
fi

if awk -F'|' '
  BEGIN { missing_reason = 0 }
  /^\|/ {
    status = $9
    notes = $10
    gsub(/^[ \t]+|[ \t]+$/, "", status)
    gsub(/^[ \t]+|[ \t]+$/, "", notes)
    if (status == "Not applicable" && notes == "") {
      missing_reason = 1
    }
  }
  END { exit missing_reason ? 0 : 1 }
' "$REQUIREMENT_TRACEABILITY"; then
  echo "Every Not applicable traceability row must include a reason in Notes." >&2
  exit 1
fi

code_fixable_count="$(
  awk -F'|' '
    $2 ~ /`CODEX_FIXABLE`/ {
      count = $3
      gsub(/[^0-9]/, "", count)
      print count
      exit
    }
  ' "$REQUIREMENT_TRACEABILITY"
)"
if [[ -z "$code_fixable_count" || "$code_fixable_count" != "0" ]]; then
  echo "CODEX_FIXABLE must be explicitly recorded as zero." >&2
  exit 1
fi

if ! grep -Fq -- "$STABLE_SOURCE_COMMIT" "$TEST_EVIDENCE"; then
  echo "Test evidence does not identify the stable source commit." >&2
  exit 1
fi
if ! grep -Fq -- "$STABLE_SOURCE_COMMIT" "$RELEASE_READINESS"; then
  echo "Release readiness does not identify the stable source commit." >&2
  exit 1
fi

mapfile -t expected_sha_lines < <(
  sed -nE 's/^- Stable candidate SHA-256: `([0-9A-Fa-f]{64})`$/\1/p' \
    "$TEST_EVIDENCE"
)
if [[ "${#expected_sha_lines[@]}" -ne 1 ]]; then
  echo "Test evidence does not contain one valid Stable candidate SHA-256 line." >&2
  exit 1
fi
expected_sha="${expected_sha_lines[0]}"
if ! grep -Fq -- "$expected_sha" "$RELEASE_READINESS"; then
  echo "Release readiness does not identify the expected stable candidate SHA-256." >&2
  exit 1
fi

printf '%s\n' "${expected_sha^^}"
