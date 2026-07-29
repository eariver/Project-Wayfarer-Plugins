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
  echo \
    "Stable source-side publication requires explicit Owner authorization that Plugin-side publication prerequisites are cleared." \
    >&2
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

bash scripts/release/validate-stable-release-documents.sh
