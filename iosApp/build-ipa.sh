#!/bin/sh
# Builds the two shareable iOS artifacts, both unsigned:
#   Dopamine-unsigned.ipa    device build (iphoneos, arm64) for sideloading tools (AltStore, Sideloadly) that sign it with a free Apple ID
#   Dopamine-simulator.zip   simulator build (iphonesimulator, arm64) for a Mac with Xcode
# Usage: JAVA_HOME=<jdk 17+> [DIST=/where/to/put/them] iosApp/build-ipa.sh
# The Kotlin framework is built by the Xcode "Build the shared Kotlin framework" phase (Gradle). The first run
# downloads Kotlin/Native (about 1 GB) and compiles Release, so it takes several minutes.
set -eu
cd "$(dirname "$0")"
DIST="${DIST:-$(pwd)/../build/dist}"
# The Release link of the Kotlin framework needs more than the default 2 GB of heap.
export GRADLE_FLAGS="${GRADLE_FLAGS:---no-daemon -Dorg.gradle.jvmargs=-Xmx6g}"
mkdir -p "$DIST"
DIST="$(cd "$DIST" && pwd)"

build() { # $1 = sdk, $2 = derived data dir
  xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Release \
    -sdk "$1" -derivedDataPath "$2" \
    CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" ARCHS=arm64 ONLY_ACTIVE_ARCH=NO build
}

# --- device (.ipa) ---
build iphoneos ../build/ios-device
APP=../build/ios-device/Build/Products/Release-iphoneos/Dopamine.app
rm -rf "$DIST/Payload" "$DIST/Dopamine-unsigned.ipa"
mkdir -p "$DIST/Payload"
cp -R "$APP" "$DIST/Payload/Dopamine.app"
(cd "$DIST" && zip -qr -y Dopamine-unsigned.ipa Payload && rm -rf Payload)

# --- simulator (.zip) ---
build iphonesimulator ../build/ios-simulator
SIM=../build/ios-simulator/Build/Products/Release-iphonesimulator/Dopamine.app
rm -f "$DIST/Dopamine-simulator.zip"
(cd "$(dirname "$SIM")" && zip -qr -y "$DIST/Dopamine-simulator.zip" Dopamine.app)

(cd "$DIST" && shasum -a 256 Dopamine-unsigned.ipa > Dopamine-unsigned.ipa.sha256 && shasum -a 256 Dopamine-simulator.zip > Dopamine-simulator.zip.sha256)
ls -l "$DIST"
