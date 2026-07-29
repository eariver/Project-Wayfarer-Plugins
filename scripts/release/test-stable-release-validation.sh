#!/usr/bin/env bash
set -euo pipefail

: "${STABLE_SOURCE_COMMIT:?STABLE_SOURCE_COMMIT is required}"
: "${TEST_EVIDENCE:?TEST_EVIDENCE is required}"
: "${REQUIREMENT_TRACEABILITY:?REQUIREMENT_TRACEABILITY is required}"
: "${RELEASE_READINESS:?RELEASE_READINESS is required}"

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
entry_validator="$script_dir/validate-stable-release-inputs.sh"
document_validator="$script_dir/validate-stable-release-documents.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT

expected_sha="$(
  sed -nE 's/^- Stable candidate SHA-256: `([0-9A-Fa-f]{64})`$/\1/p' \
    "$TEST_EVIDENCE"
)"
expected_sha="${expected_sha^^}"

positive_output="$(bash "$entry_validator")"
if [[ "$positive_output" != "$expected_sha" || "$positive_output" == *$'\n'* ]]; then
  echo "Positive validation did not output the expected stable SHA exactly once." >&2
  exit 1
fi
echo "PASS: positive stable release validation"

expect_document_failure() {
  local name="$1"
  local traceability_path="$2"
  local readiness_path="${3:-$RELEASE_READINESS}"
  if REQUIREMENT_TRACEABILITY="$traceability_path" \
      RELEASE_READINESS="$readiness_path" \
      bash "$document_validator" >/dev/null 2>&1; then
    echo "Negative validation unexpectedly passed: $name" >&2
    exit 1
  fi
  echo "PASS: rejected $name"
}

write_status_case() {
  local replacement="$1"
  local clear_notes="$2"
  local output_path="$3"
  awk -F'|' -v OFS='|' -v replacement="$replacement" -v clear_notes="$clear_notes" '
    function trim(value) {
      gsub(/^[ \t]+|[ \t]+$/, "", value)
      return value
    }
    BEGIN { changed = 0 }
    trim($2) ~ /^[A-Z]+-[0-9]{3}$/ && changed == 0 {
      $9 = " " replacement " "
      if (clear_notes == "true") {
        $10 = " "
      }
      changed = 1
    }
    { print }
    END { if (changed == 0) exit 2 }
  ' "$REQUIREMENT_TRACEABILITY" > "$output_path"
}

write_classification_case() {
  local classification="$1"
  local count="$2"
  local output_path="$3"
  awk -F'|' -v OFS='|' -v classification="$classification" -v count="$count" '
    BEGIN { changed = 0 }
    index($2, "`" classification "`") > 0 {
      $3 = " " count " "
      changed = 1
    }
    { print }
    END { if (changed == 0) exit 2 }
  ' "$REQUIREMENT_TRACEABILITY" > "$output_path"
}

write_status_case "In progress" "false" "$temp_root/in-progress.md"
expect_document_failure "In progress status" "$temp_root/in-progress.md"

write_status_case "Typo status" "false" "$temp_root/typo.md"
expect_document_failure "arbitrary status" "$temp_root/typo.md"

write_status_case "" "false" "$temp_root/empty.md"
expect_document_failure "empty status" "$temp_root/empty.md"

write_classification_case "READY_FOR_PUBLICATION" "999" "$temp_root/ready-count.md"
expect_document_failure \
  "READY_FOR_PUBLICATION classification mismatch" \
  "$temp_root/ready-count.md"

write_classification_case \
  "PROJECT_ACCEPTANCE_PENDING" \
  "999" \
  "$temp_root/project-count.md"
expect_document_failure \
  "PROJECT_ACCEPTANCE_PENDING classification mismatch" \
  "$temp_root/project-count.md"

write_classification_case "CODEX_FIXABLE" "1" "$temp_root/codex-fixable.md"
expect_document_failure "nonzero CODEX_FIXABLE" "$temp_root/codex-fixable.md"

sed \
  's/^- Release gate: CLEARED$/- Release gate: BLOCKED/' \
  "$REQUIREMENT_TRACEABILITY" > "$temp_root/release-gate.md"
expect_document_failure "non-CLEARED release gate" "$temp_root/release-gate.md"

sed \
  's/^- Release readiness: READY$/- Release readiness: BLOCKED/' \
  "$RELEASE_READINESS" > "$temp_root/readiness.md"
expect_document_failure \
  "non-READY release readiness" \
  "$REQUIREMENT_TRACEABILITY" \
  "$temp_root/readiness.md"

write_status_case "Not applicable" "true" "$temp_root/not-applicable.md"
expect_document_failure \
  "Not applicable status without Notes" \
  "$temp_root/not-applicable.md"

if REQUIREMENTS_CLEARED="false" bash "$entry_validator" >/dev/null 2>&1; then
  echo "Negative validation unexpectedly passed: requirements_cleared=false" >&2
  exit 1
fi
echo "PASS: rejected requirements_cleared=false"
