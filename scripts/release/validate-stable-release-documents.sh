#!/usr/bin/env bash
set -euo pipefail

: "${STABLE_SOURCE_COMMIT:?STABLE_SOURCE_COMMIT is required}"
: "${TEST_EVIDENCE:?TEST_EVIDENCE is required}"
: "${REQUIREMENT_TRACEABILITY:?REQUIREMENT_TRACEABILITY is required}"
: "${RELEASE_READINESS:?RELEASE_READINESS is required}"

if ! grep -Fxq -- "- Release gate: CLEARED" "$REQUIREMENT_TRACEABILITY"; then
  echo "Requirement traceability release gate is not CLEARED." >&2
  exit 1
fi

if ! grep -Fxq -- "- Release readiness: READY" "$RELEASE_READINESS"; then
  echo "Release readiness is not READY." >&2
  exit 1
fi

awk -F'|' '
  function trim(value) {
    gsub(/^[ \t]+|[ \t]+$/, "", value)
    return value
  }
  function fail(message) {
    print message > "/dev/stderr"
    errors++
  }
  BEGIN {
    allowed["Implemented"] = 1
    allowed["Automated test passed"] = 1
    allowed["Runtime test passed"] = 1
    allowed["Automated/runtime passed"] = 1
    allowed["Automated/client passed"] = 1
    allowed["Automated/headless runtime passed"] = 1
    allowed["Ready for publication"] = 1
    allowed["Project acceptance pending"] = 1
    allowed["Not applicable"] = 1

    classifications["PLUGIN_COMPLETE"] = 1
    classifications["CODEX_FIXABLE"] = 1
    classifications["READY_FOR_PUBLICATION"] = 1
    classifications["PROJECT_ACCEPTANCE_PENDING"] = 1
  }
  /^- Acceptance units: / {
    acceptance_units = $0
    sub(/^- Acceptance units: /, "", acceptance_units)
    acceptance_units_seen++
    if (acceptance_units !~ /^[0-9]+$/) {
      fail("Acceptance units must be one non-negative integer.")
    }
  }
  /^\|/ {
    identifier = trim($2)
    classification = identifier
    gsub(/`/, "", classification)

    if (classification in classifications) {
      classification_count = trim($3)
      if (classification_count !~ /^[0-9]+$/) {
        fail("Classification " classification " must have one numeric count.")
      } else {
        classification_value[classification] = classification_count + 0
      }
      classification_seen[classification]++
    }

    if (identifier ~ /^[A-Z]+-[0-9]{3}$/) {
      requirement_rows++
      status = trim($9)
      notes = trim($10)
      if (!(status in allowed)) {
        fail("Requirement " identifier " has an unapproved or empty status: [" status "].")
      }
      if (status == "Not applicable" && notes == "") {
        fail("Requirement " identifier " is Not applicable without a Notes reason.")
      }
      if (status == "Ready for publication") {
        ready_status_rows++
      }
      if (status == "Project acceptance pending") {
        project_status_rows++
      }
    }
  }
  END {
    if (acceptance_units_seen != 1) {
      fail("Traceability must contain exactly one Acceptance units value.")
    }
    if (requirement_rows != acceptance_units) {
      fail("Requirement row count does not equal Acceptance units.")
    }

    for (classification in classifications) {
      if (classification_seen[classification] != 1) {
        fail("Classification " classification " must occur exactly once.")
      }
    }

    if (classification_value["CODEX_FIXABLE"] != 0) {
      fail("CODEX_FIXABLE must be exactly zero.")
    }
    if (classification_value["READY_FOR_PUBLICATION"] != ready_status_rows) {
      fail("READY_FOR_PUBLICATION count does not match Ready for publication rows.")
    }
    if (classification_value["PROJECT_ACCEPTANCE_PENDING"] != project_status_rows) {
      fail("PROJECT_ACCEPTANCE_PENDING count does not match Project acceptance pending rows.")
    }
    classification_total = classification_value["PLUGIN_COMPLETE"] \
      + classification_value["READY_FOR_PUBLICATION"] \
      + classification_value["PROJECT_ACCEPTANCE_PENDING"]
    if (classification_total != acceptance_units) {
      fail("Classification totals do not equal Acceptance units.")
    }
    expected_plugin_complete = requirement_rows - ready_status_rows - project_status_rows
    if (classification_value["PLUGIN_COMPLETE"] != expected_plugin_complete) {
      fail("PLUGIN_COMPLETE does not equal rows outside the two remaining-work statuses.")
    }

    exit errors == 0 ? 0 : 1
  }
' "$REQUIREMENT_TRACEABILITY"

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
