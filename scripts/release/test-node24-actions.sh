#!/usr/bin/env bash
set -euo pipefail

workflows=(.github/workflows/*.yml)

if grep -En \
  'uses: (actions/checkout@v[1-6]|actions/setup-java@v[1-4]|gradle/actions/setup-gradle@v[1-5]|actions/upload-artifact@v[1-6]|actions/download-artifact@v[1-7])([^0-9]|$)' \
  "${workflows[@]}"; then
  echo "A workflow still references a pre-Node-24 action major." >&2
  exit 1
fi

for required in \
  'actions/checkout@v7' \
  'actions/setup-java@v5' \
  'gradle/actions/setup-gradle@v6' \
  'actions/upload-artifact@v7' \
  'actions/download-artifact@v8'
do
  grep -Fq "uses: $required" "${workflows[@]}" \
    || {
      echo "Required Node-24 action is absent: $required" >&2
      exit 1
    }
done

echo "PASS: workflow action majors use the verified Node 24 lines"
