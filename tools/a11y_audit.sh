#!/usr/bin/env bash
# Audit the screen currently showing on a connected phone. Usage: tools/a11y_audit.sh [adb path]
set -euo pipefail
ADB="${1:-adb}"
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
tmp="$(mktemp)"
"$ADB" shell uiautomator dump /sdcard/a11y.xml >/dev/null
"$ADB" exec-out cat /sdcard/a11y.xml > "$tmp"
density="$("$ADB" shell wm density | grep -oE '[0-9]+' | tail -1)"
python3 "$here/a11y_audit.py" "$tmp" --density "$density"
