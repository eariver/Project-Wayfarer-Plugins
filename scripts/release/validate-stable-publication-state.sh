#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "Stable publication state error: $*" >&2
  exit 1
}

: "${TAG:?TAG is required}"
: "${STABLE_SOURCE_COMMIT:?STABLE_SOURCE_COMMIT is required}"

[[ "$TAG" =~ ^V0\.0\.[1-9][0-9]*$ ]] \
  || fail "TAG must be a stable V0.0.x version."
[[ "$STABLE_SOURCE_COMMIT" =~ ^[0-9a-f]{40}$ ]] \
  || fail "STABLE_SOURCE_COMMIT must be an exact lowercase commit SHA."
git cat-file -e "${STABLE_SOURCE_COMMIT}^{commit}" 2>/dev/null \
  || fail "Stable source commit does not exist."
git show-ref --verify --quiet refs/remotes/origin/main \
  || fail "origin/main is unavailable for stable-source validation."
git merge-base --is-ancestor "$STABLE_SOURCE_COMMIT" origin/main \
  || fail "Stable source commit is not contained in origin/main."

release_exists() {
  case "${RELEASE_EXISTS_OVERRIDE:-}" in
    true)
      return 0
      ;;
    false)
      return 1
      ;;
    "")
      command -v gh >/dev/null 2>&1 \
        || fail "gh is required when RELEASE_EXISTS_OVERRIDE is not supplied."
      [[ -n "${GITHUB_REPOSITORY:-}" ]] \
        || fail "GITHUB_REPOSITORY is required for GitHub Release lookup."
      lookup_output="$(mktemp)"
      if gh api -i \
          "repos/${GITHUB_REPOSITORY}/releases/tags/${TAG}" \
          >"$lookup_output" 2>&1; then
        rm -f -- "$lookup_output"
        return 0
      fi
      if grep -Eq '(^|[[:space:]])404([[:space:]]|$)' "$lookup_output"; then
        rm -f -- "$lookup_output"
        return 1
      fi
      lookup_error="$(tr '\n' ' ' < "$lookup_output")"
      rm -f -- "$lookup_output"
      fail "GitHub Release lookup failed without an authoritative 404: $lookup_error"
      ;;
    *)
      fail "RELEASE_EXISTS_OVERRIDE must be true, false, or unset."
      ;;
  esac
}

if release_exists; then
  fail "GitHub Release $TAG already exists and will not be overwritten."
fi

tag_ref="refs/tags/$TAG"
if ! git show-ref --verify --quiet "$tag_ref"; then
  printf '%s\n' "new"
  exit 0
fi

tag_type="$(git cat-file -t "$tag_ref" 2>/dev/null)" \
  || fail "Existing tag cannot be inspected: $TAG"
[[ "$tag_type" == "tag" ]] \
  || fail "Existing tag must be an annotated tag: $TAG"

tag_target="$(git rev-parse "${tag_ref}^{commit}" 2>/dev/null)" \
  || fail "Existing annotated tag cannot be dereferenced to a commit: $TAG"
[[ "$tag_target" == "$STABLE_SOURCE_COMMIT" ]] \
  || fail "Existing annotated tag points to $tag_target, expected $STABLE_SOURCE_COMMIT."

printf '%s\n' "recover-existing-tag"
