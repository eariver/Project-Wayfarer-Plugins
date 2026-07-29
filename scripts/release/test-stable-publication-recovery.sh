#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
validator="$script_dir/validate-stable-publication-state.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT
repo="$temp_root/repo"

git init -q -b main "$repo"
git -C "$repo" config user.name "Stable Publication Test"
git -C "$repo" config user.email "stable-publication-test@example.invalid"
printf 'stable\n' > "$repo/state.txt"
git -C "$repo" add state.txt
git -C "$repo" commit -q -m "stable"
stable_source="$(git -C "$repo" rev-parse HEAD)"
git -C "$repo" update-ref refs/remotes/origin/main "$stable_source"

printf 'wrong\n' >> "$repo/state.txt"
git -C "$repo" commit -q -am "wrong source fixture"
wrong_source="$(git -C "$repo" rev-parse HEAD)"

run_validator() {
  (
    cd "$repo"
    TAG="V0.0.1" \
      STABLE_SOURCE_COMMIT="${1:-$stable_source}" \
      RELEASE_EXISTS_OVERRIDE="${2:-false}" \
      bash "$validator"
  )
}

expect_failure() {
  local name="$1"
  shift
  if "$@" >/dev/null 2>&1; then
    echo "Negative publication-state validation unexpectedly passed: $name" >&2
    exit 1
  fi
  echo "PASS: rejected $name"
}

git -C "$repo" update-ref refs/remotes/origin/main "$wrong_source"
new_mode="$(run_validator)"
[[ "$new_mode" == "new" ]] || {
  echo "No-tag state did not return mode=new." >&2
  exit 1
}
echo "PASS: no tag and no release returned mode=new"

git -C "$repo" tag -a V0.0.1 "$stable_source" -m "stable annotated tag"
recovery_mode="$(run_validator)"
[[ "$recovery_mode" == "recover-existing-tag" ]] || {
  echo "Correct existing annotated tag did not return recovery mode." >&2
  exit 1
}
echo "PASS: correct annotated tag returned recover-existing-tag"

git -C "$repo" tag -d V0.0.1 >/dev/null
git -C "$repo" tag V0.0.1 "$stable_source"
expect_failure "lightweight tag" run_validator

git -C "$repo" tag -d V0.0.1 >/dev/null
git -C "$repo" tag -a V0.0.1 "$wrong_source" -m "wrong annotated tag"
expect_failure "wrong-source annotated tag" run_validator

git -C "$repo" tag -d V0.0.1 >/dev/null
blob_target="$(git -C "$repo" hash-object -w state.txt)"
git -C "$repo" tag -a V0.0.1 "$blob_target" -m "non-commit annotated tag"
expect_failure "annotated tag that cannot dereference to commit" run_validator

git -C "$repo" tag -d V0.0.1 >/dev/null
expect_failure "existing GitHub Release" run_validator "$stable_source" "true"
expect_failure "malformed stable source" run_validator "not-a-commit" "false"
expect_failure \
  "missing stable source commit" \
  run_validator \
  "1111111111111111111111111111111111111111" \
  "false"

git -C "$repo" update-ref refs/remotes/origin/main "$stable_source"
expect_failure "stable source outside origin/main" run_validator "$wrong_source" "false"

grep -Fq \
  'bash scripts/release/test-stable-publication-recovery.sh' \
  .github/workflows/ci.yml \
  || {
    echo "CI does not run the stable publication recovery test." >&2
    exit 1
  }
validator_call_count="$(
  grep -Fc \
    'validate-stable-publication-state.sh' \
    .github/workflows/release.yml
)"
[[ "$validator_call_count" -ge 3 ]] || {
  echo "Release workflow does not validate publication state in Build and Publish boundaries." >&2
  exit 1
}
echo "PASS: workflow reuses publication-state validation"
