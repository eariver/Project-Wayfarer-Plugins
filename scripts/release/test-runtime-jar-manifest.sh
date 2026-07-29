#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
creator="$script_dir/create-runtime-jar-manifest.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT

mkdir -p "$temp_root/source/plugins/wayfarer-main/build/libs"
mkdir -p "$temp_root/source/plugins/wayfarer-frontier/build/libs"
printf 'main\n' > "$temp_root/source/plugins/wayfarer-main/build/libs/main.jar"
printf 'frontier\n' > "$temp_root/source/plugins/wayfarer-frontier/build/libs/frontier.jar"
main_hash="$(sha256sum "$temp_root/source/plugins/wayfarer-main/build/libs/main.jar" | awk '{ print toupper($1) }')"
frontier_hash="$(sha256sum "$temp_root/source/plugins/wayfarer-frontier/build/libs/frontier.jar" | awk '{ print toupper($1) }')"
cat > "$temp_root/evidence.md" <<EOF
- Wayfarer_Main stable candidate SHA-256: \`$main_hash\`
- Wayfarer_Frontier stable candidate SHA-256: \`$frontier_hash\`
EOF

(
  cd "$temp_root/source"
  RELEASE_VERSION=V0.0.2 \
    RELEASE_SCOPE=main-frontier \
    TEST_EVIDENCE="$temp_root/evidence.md" \
    OUTPUT_MANIFEST="$temp_root/runtime.tsv" \
    bash "$creator"
)
[[ "$(wc -l < "$temp_root/runtime.tsv" | tr -d ' ')" -eq 2 ]]
grep -Fq "Wayfarer_Main-V0.0.2.jar|" "$temp_root/runtime.tsv"
grep -Fq "Wayfarer_Frontier-V0.0.2.jar|" "$temp_root/runtime.tsv"

sed -i '/Wayfarer_Frontier/d' "$temp_root/evidence.md"
if (
  cd "$temp_root/source"
  RELEASE_VERSION=V0.0.2 \
    RELEASE_SCOPE=main-frontier \
    TEST_EVIDENCE="$temp_root/evidence.md" \
    OUTPUT_MANIFEST="$temp_root/invalid.tsv" \
    bash "$creator" >/dev/null 2>&1
); then
  echo "Missing expected artifact hash was accepted." >&2
  exit 1
fi

echo "PASS: runtime JAR manifest binds every scoped artifact to evidence"
