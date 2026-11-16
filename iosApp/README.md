# iosApp

SwiftUI host for the shared Compose Multiplatform UI (`shared/`). The Xcode project is hand-written.

Needs: Xcode, a JDK 17+ (`JAVA_HOME`), and the Gradle/Kotlin Native downloads (first build is slow).

- Simulator, debug: `iosApp/build-simulator.sh`, then `xcrun simctl install booted <app>` and `xcrun simctl launch booted app.dopamine`.
- Shareable artifacts: `DIST=/some/folder iosApp/build-ipa.sh` makes `Dopamine-unsigned.ipa` (device, arm64, unsigned, for AltStore or
  Sideloadly) and `Dopamine-simulator.zip` (simulator, arm64) plus sha256 files.
- Bundle id `app.dopamine`, version 0.3.0 (3), minimum iOS 15.0. The app icon is `iosApp/Assets.xcassets/AppIcon.appiconset`
  (the Android launcher look: yellow tile with the Bot).
- Test hooks (env vars with `SIMCTL_CHILD_` prefix): see `MainViewController.kt`.
