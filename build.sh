#!/usr/bin/env bash
# One-command build. Usage: ./build.sh [gradle tasks]   (default: tests + lint + debug + release APKs)
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
if [ -z "${JAVA_HOME:-}" ]; then
  # Prefer a Nix JDK 25 if present, else whatever java is on PATH.
  nixjdk=$(ls -d /nix/store/*-openjdk-25*/lib/openjdk 2>/dev/null | head -1 || true)
  [ -n "$nixjdk" ] && export JAVA_HOME="$nixjdk"
fi
echo "JAVA_HOME=${JAVA_HOME:-system}  ANDROID_HOME=$ANDROID_HOME"
[ -f local.properties ] || echo "sdk.dir=$ANDROID_HOME" > local.properties
if [ $# -eq 0 ]; then set -- testDebugUnitTest lintDebug assembleDebug assembleRelease; fi
./gradlew --no-daemon "$@"
