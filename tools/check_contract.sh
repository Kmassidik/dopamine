#!/usr/bin/env bash
# Fails when the vendored contract.json differs from the one published in the content repo (main).
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
curl -fsSL https://raw.githubusercontent.com/Kmassidik/dopamine-content/main/contract.json | diff -u app/src/test/resources/contract.json - \
  && echo "contract.json is in sync with the content repo"
