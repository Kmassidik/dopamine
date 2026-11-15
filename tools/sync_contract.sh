#!/usr/bin/env bash
# Copy the content repo's contract.json into the app's test resources.
# Uses a sibling checkout if there is one, otherwise downloads the published copy from GitHub.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
DEST=app/src/test/resources/contract.json
if [ -f ../dopamine-content/contract.json ]; then
  cp ../dopamine-content/contract.json "$DEST"
else
  curl -fsSL https://raw.githubusercontent.com/Kmassidik/dopamine-content/main/contract.json -o "$DEST"
fi
echo "updated $DEST"
