#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
assembler="$script_dir/assemble-scoped-stable-package.sh"
temp_root="$(mktemp -d)"
trap 'rm -rf -- "$temp_root"' EXIT

mkdir -p "$temp_root/snapshot/assets"
printf 'test evidence\n' > "$temp_root/snapshot/assets/TEST_SERVER_EVIDENCE.md"
printf 'dependencies\n' > "$temp_root/dependencies.toml"
printf 'main\n' > "$temp_root/main.jar"
printf 'frontier\n' > "$temp_root/frontier.jar"
main_hash="$(sha256sum "$temp_root/main.jar" | awk '{ print toupper($1) }')"
frontier_hash="$(sha256sum "$temp_root/frontier.jar" | awk '{ print toupper($1) }')"
cat > "$temp_root/runtime.tsv" <<EOF
Wayfarer_Main-V0.0.2.jar|$temp_root/main.jar|$main_hash
Wayfarer_Frontier-V0.0.2.jar|$temp_root/frontier.jar|$frontier_hash
EOF

export PACKAGE_ROOT="$temp_root/package"
export RELEASE_VERSION=V0.0.2
export TAG=V0.0.2
export RELEASE_SCOPE=main-frontier
export PRODUCT_SOURCE_COMMIT=1111111111111111111111111111111111111111
export HANDOFF_SOURCE_COMMIT=2222222222222222222222222222222222222222
export RUNTIME_JAR_MANIFEST="$temp_root/runtime.tsv"
export SNAPSHOT_DIR="$temp_root/snapshot"
export DEPENDENCY_VERSIONS_SOURCE="$temp_root/dependencies.toml"
export GITHUB_REPOSITORY=eariver/Project-Wayfarer-Plugins

bash "$assembler" assemble
bash "$assembler" verify

if find "$PACKAGE_ROOT/assets" -maxdepth 1 -name 'Wayfarer_Core-*.jar' | grep -q .; then
  echo "main-frontier package unexpectedly contains Core." >&2
  exit 1
fi

printf 'tamper\n' >> "$PACKAGE_ROOT/assets/Wayfarer_Main-V0.0.2.jar"
if bash "$assembler" verify >/dev/null 2>&1; then
  echo "Tampered scoped package unexpectedly passed." >&2
  exit 1
fi

echo "PASS: scoped stable package assembly and verification"
