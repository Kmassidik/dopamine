# AGENTS.md: rules for AI agents working on this repo

Read this first, then `HANDOVER.md` (state and roadmap) and `DECISIONS.md` (why). This file works for any agent (Codex, Claude, others).

## Hard rules (from the owner and the product brief)

1. **Kotlin only** for app code. **Jetpack Compose** UI. Single Activity, Compose Navigation, MVVM, **manual DI** (`AppContainer` in `App.kt`). No Hilt, no XML layouts beyond what the platform requires.
2. **No WebView anywhere**, including for math. Math is rendered natively (see `HANDOVER.md`, step "math renderer").
3. **No Firebase, Google Play Services, ads, analytics or crash reporters.** The only permission is `INTERNET`. Do not add others without a written reason in `DECISIONS.md`.
4. **Never crash on bad content.** Validate, log, skip, and show a gentle note. A failed update must never damage existing content.
5. **Content is data, never code.** No `eval`, no scripts, no dynamic loading.
6. Works on small, old phones: minSdk 24, 320x640dp portrait, 1.3x font scale, 2 GB RAM. Every screen scrolls if its content can overflow; no fixed heights for text; touch targets at least 48dp; primary actions near the bottom.
7. Honor the system "remove animations" setting. Read `LocalReducedMotion`; use `IconImage` and `bounceClick` which already do.
8. **Do not commit anything that attributes work to an AI.** No `Co-Authored-By` trailers, no "Generated with" lines, in commits or PRs. The owner asked for no AI contribution marks on their GitHub.
9. Record every non-obvious decision in `DECISIONS.md` (date, decision, options, reason, how to reverse). Justify every new dependency there.
10. Tell the truth in status reports: say what is done, partial, untested. Do not claim a feature works until you ran it.

## Repos and owner

- App repo: `github.com/Kmassidik/dopamine` (this one, branch `main`).
- Content repo: `github.com/Kmassidik/dopamine-content`.
- A different, older app, `skillfeed`, exists on the owner's laptop. **Leave it alone.**

## Environment (the owner's setup)

- Code is authored on a Mac (`~/Desktop/Apps/dopamine`) and **built on a Fedora laptop** reachable with `ssh kurnia-laptop` (repo at `~/Desktop/dopamine`, cloned over public HTTPS; its `gh` token is invalid, so push from the Mac, then `git pull` on the laptop).
- The Mac has no JDK or Android SDK. The laptop has the SDK at `~/Android/Sdk` and a Nix JDK 25. `build.sh` finds it (`/nix/store/*-openjdk-25*/lib/openjdk`) or uses `JAVA_HOME`.
- Do **not** change global settings such as `~/.gradle/gradle.properties` (it can override `skillfeed`'s JDK).
- Typical loop:

```bash
git add -A && git commit -m "..." && git push           # on the Mac
ssh kurnia-laptop 'cd ~/Desktop/dopamine && git pull -q && ./build.sh'
ssh kurnia-laptop '~/Android/Sdk/platform-tools/adb install -r ~/Desktop/dopamine/app/build/outputs/apk/debug/app-debug.apk'
```

- Tooling versions (see `gradle/libs.versions.toml`): AGP 9.5.1 (built-in Kotlin), Gradle 9.9.1, Kotlin 2.4.21, KSP 2.3.14, Compose BOM 2026.09.00, Room 2.8.5, OkHttp 5.5.1, compileSdk/targetSdk 37, minSdk 24. These were looked up from Maven, not guessed. Verify before changing.

## Commands

```bash
./build.sh                                  # test + lint + assembleDebug + assembleRelease
./build.sh testDebugUnitTest                # unit tests only
./build.sh lintDebug
```

Always run the unit tests and lint before you push. The build must stay green between commits.

## Code map

```
app/src/main/java/app/dopamine/
  App.kt, MainActivity.kt       Application + AppContainer (manual DI), single Activity
  domain/model/                 Content.kt (Manifest, Lesson, Question DTOs), Profile.kt (name rules)
  domain/content/               ManifestDiff.kt (diff + SafePath), LessonValidator.kt
  data/content/ContentStore.kt  filesDir/content: seed, read, stage, atomic swap, prefs (etag, last update)
  data/network/UpdateManager.kt The update flow (user initiated only)
  data/repo/                    ContentRepository (manifest StateFlow), ProfileRepository
  data/db/AppDatabase.kt        Room v1: ProfileEntity only so far. Schema JSON in app/schemas/
  ui/AppNav.kt                  Loading -> Onboarding -> Main nav (home, category/{id}, settings)
  ui/home, category, settings, onboarding/   screens + ViewModels
  ui/components/                Components.kt (IconImage, bounceClick, ChunkyButton, InkButton, StatChip, ...), Ribbon, NameField
  ui/theme/                     Palette (raw hex longs), Color, Type, Shape, Spacing, Motion, Theme
app/src/main/assets/content/    bundled seed, a copy of the content repo
app/src/main/res/drawable-nodpi/ 112 WebP icons (see ASSETS.md); res/drawable has only functional vector glyphs
tools/                          icon pipeline (make_assets_md.py, slice_sheet.py)
```

Layering: composables hold no business logic; ViewModels expose immutable `StateFlow`; repositories are the only layer that touches Room or files; domain code avoids Android imports.

## Conventions

- Strings go in `res/values/strings.xml` (plurals via `pluralStringResource`). No hard-coded user-facing text in Kotlin except update result messages in `SettingsViewModel` (move them to resources when you touch them).
- Colors come from `ui/theme` only (`Dopamine.colors`). Text on a colored fill uses `c.onAccent`. New color pairs must pass `ContrastTest` (WCAG AA 4.5:1); add the pair to the test.
- Room: **never use destructive migration.** Bump the version, write a `Migration`, and keep exported schemas in `app/schemas/`.
- Copy is short, calm and specific. Errors say what happened and what to do. No exclamation spam, no emoji as UI, no placeholder text.
- Commit small, with a clear message. Keep the project building at every commit.

## Pitfalls we already hit (save yourself the time)

- **adb "no permissions":** the laptop must have the owner logged into the desktop (not at the login screen); USB devices belong to the seat owner. Fix: log in locally, replug the phone.
- **`INSTALL_FAILED_USER_RESTRICTED` on MIUI:** enable *Install via USB* and *USB debugging (Security settings)* in Developer options, keep the phone unlocked, and tap Install within a few seconds. Fallback: `adb push` the APK to `/sdcard/Download/` and tap it on the phone.
- **Locked phone:** `input tap` and `input text` do nothing useful on the lock screen. Check `dumpsys window | grep isKeyguardShowing` first. Never try to bypass a lock.
- **Do not run `pm clear app.dopamine` on the owner's phone** unless asked: it deletes their saved name. It once overwrote their name with a test name. Test with a throwaway name only on a fresh install, or add a debug build with its own `applicationIdSuffix`.
- **Screenshots:** `adb exec-out screencap -p > file.png`; the phone is 1080x2400, previews are scaled 0.83 (multiply by 1.2 for tap coordinates).
- **`ssh` + zsh:** a line like `echo ===X` breaks in the remote zsh; quote separators.
- **Compose:** `matchParentSize` is a `BoxScope` member (no import). A stray edit once left `import app.dopamine`; read your imports.
- **Icon sheets:** generator backgrounds are never exactly the requested color; `slice_sheet.py` removes any flat background by flood-fill from the cell border, so do not assume magenta.
- Release builds are minified (R8). If you add reflection or a new serialization pattern, test `assembleRelease` on a device.

## Definition of done for a change

Builds, tests and lint pass; you ran it on a device or emulator if it is UI; `DECISIONS.md` updated if you decided something; `HANDOVER.md` status updated if scope moved; committed and pushed without AI attribution.
