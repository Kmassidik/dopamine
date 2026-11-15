# TESTING

## Automated

```bash
./build.sh testDebugUnitTest      # JVM unit tests
./build.sh lintDebug
./build.sh                         # tests + lint + debug and release APKs
```

Current unit tests (21, all passing): `ContrastTest` (WCAG AA for every text pair, light and dark), `ManifestDiffTest` (new / changed / removed / unchanged, missing image re-fetch, unsafe path rejection), `LessonValidatorTest`, `ProfileNameTest`.

Planned: LaTeX tokenizer, SM-2, answer normalization and numeric equivalence, streak logic, hearts regeneration, XP, Room migrations (schemas in `app/schemas/`), update flow against MockWebServer (bad hash rollback, interrupted download), Compose UI test of a full lesson, rotation keeps state.

## On a device
```bash
adb devices                                                        # must say "device"
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb exec-out screencap -p > screenshot.png
```
Make sure the phone is unlocked first. Do not `pm clear` a phone that holds real progress (it erases the saved name and progress); use a throwaway name on a fresh install.

## Small-phone recipe (320x640dp, 1.3x font)

```bash
adb shell wm size 640x1280 && adb shell wm density 320      # = 320x640 dp
adb shell settings put system font_scale 1.3
# ... test every screen, rotate, then restore:
adb shell wm size reset && adb shell wm density reset && adb shell settings put system font_scale 1.0
```

An emulator `pixel_api35` exists on the laptop (`/dev/kvm` available; the host GPU is needed, SwiftShader crashes).

## Manual checklist

- [ ] Fresh install: onboarding asks the name, greeting uses it, relaunch keeps it
- [ ] Airplane mode launch; Update shows "Offline, using local content"
- [ ] Update with a bad hash must roll back (serve a tampered file locally)
- [ ] Update interrupted mid-download leaves content intact
- [ ] Settings: change name saves; Cancel discards
- [ ] Remove animations (developer option): icons stop bobbing, no squash
- [ ] Font scale 1.3x and 2.0x: no clipped text, nothing unreachable (home, category, settings, onboarding)
- [ ] 320x640dp portrait and landscape: no overlap, every screen scrolls
- [ ] Dark mode (once re-enabled)
- [ ] TalkBack pass through onboarding, home, category, settings (and later the lesson)
- [ ] Low-memory device or emulator
- [ ] Release APK: install, launch, update, no crash (R8)


## Design guard (screenshot tests)
Purpose: make sure the UI keeps the design and nothing sloppy slips in. Not a general UI test suite.

- Tests live in `app/src/screenshotTest/kotlin/app/dopamine/DesignScreenshots.kt` (Google's Compose Preview Screenshot Testing, built into AGP 9.5). Each `@PreviewTest @Preview` renders a piece of the design system (palette, type scale, buttons and chips, floating nav, loading screen, math, and a 320dp / 1.3x font case) and compares it pixel by pixel with the reference image in `app/src/screenshotTestDefaultDebug/reference/`.
- **Check:** `./build.sh testDebugScreenshotTestDefaultTestSuite`. On a mismatch the failure lists the expected image, the actual image and a diff image under `app/build/intermediates/debug/testDebugScreenshotTestDefaultTestSuite/results/`, and an HTML report under `app/build/reports/tests/`.
- **Change the design on purpose:** `./build.sh updateDebugScreenshotTestDefaultTestSuite`, look at the new images in `reference/` yourself, commit them, and say in the commit message why the look changed. Never update references just to make a failure go away.
- Record and validate on Linux (the Fedora laptop or CI). Fonts and anti-aliasing can differ on other systems.
- When you add a reusable component or a design token, add a preview for it. Screens that need a ViewModel are not covered yet; extract a stateless `XContent` composable first.
- Requires `android.experimental.enableScreenshotTest` and `android.experimental.testSuiteSupport` in `gradle.properties`, `unitTests.isIncludeAndroidResources = true`, and unit tests enabled for every variant (`androidComponents` block in `app/build.gradle.kts`). AGP prints "experimental" warnings; that is expected.
