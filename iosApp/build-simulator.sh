#!/bin/sh
# Builds the iPhone app for the iOS simulator (no code signing). Usage: iosApp/build-simulator.sh
# Needs JAVA_HOME (JDK 17+) in the environment; on the owner's Mac see knowledge/IOS-PORT-NOTES.md.
set -eu
cd "$(dirname "$0")"
export GRADLE_FLAGS="${GRADLE_FLAGS:---no-daemon}"
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath ../build/ios-derived CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
echo "App: $(pwd)/../build/ios-derived/Build/Products/Debug-iphonesimulator/Dopamine.app"
