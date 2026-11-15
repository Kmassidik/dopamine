# HANDOVER

Snapshot as of **2026-11-16**. Read `AGENTS.md` first for rules and environment. Update this file whenever scope moves.

## 1. What exists and works (verified on a real phone: Redmi Note 10S, Android 13, 1080x2400)

| Area | State | Where |
|---|---|---|
| Content schemas, sample lessons, manifest generator, validator, CI (content repo) | Done | `dopamine-content` |
| Gradle project, version catalog, R8 release build | Done | `build.gradle.kts`, `app/build.gradle.kts` |
| Design system (white + green/yellow/pink, Zen Maru Gothic, WCAG AA tested) | Done | `ui/theme/`, `DESIGN.md` |
| 112 illustrated icons + Bot mascot + launcher icon, bob / press motion | Done | `res/drawable-nodpi/`, `ui/components/Components.kt` |
| Seed content on first launch, re-seed if folder missing or corrupt | Done | `ContentStore.ensureSeeded` |
| Onboarding: ask the name, store it in Room, greet by name | Done | `ui/onboarding`, `data/db`, `ProfileRepository` |
| Home: bento tiles, ribbon, course rows | Done (progress numbers are placeholders, see section 3) | `ui/home` |
| Category screen listing courses, units, lessons (question count, XP) | Done (lessons not tappable) | `ui/category` |
| Settings: change name, **Update content**, about | Done | `ui/settings` |
| Update flow: ETag, schema/min-app check, diff, staged download, sha256 + size + parse + validate, atomic swap, rollback, cancel, 3 retries | Core done, hardening missing (section 2, item 7) | `data/network/UpdateManager.kt` |
| Unit tests (21): contrast, manifest diff, safe paths, lesson validation, name rules | Pass | `app/src/test` |

Measured: release APK 1.67 MB; debug APK 14 MB (includes tooling); icon weight about 1.05 MB; only permission `INTERNET`. Not measured yet: cold start time, peak heap, behavior at 320x640dp and 1.3x font scale.

## 2. Roadmap (in this order; each item lists acceptance criteria)

### 1. Native math renderer (original "Step 3"). **Do this first, it unblocks the player.**
- Package `math/`: `LatexTokenizer` (split plain text and math: `$..$` inline, `$$..$$` block, `\$` escaped, unbalanced dollars handled), `LatexRenderer` (LaTeX to `Bitmap` off the main thread, LRU cache about 8 MB keyed by `(latex, textSizePx, colorArgb, displayMode)`), `MathText` composable (mixed text and inline formulas aligned to the text baseline using Compose inline content; block math centered and horizontally scrollable in its own container; theme colors and font scale respected; muted raw LaTeX while loading; muted monospace raw LaTeX plus a warning mark on parse failure, logged, never a crash), `FormulaTestScreen` (debug only, 25+ tricky formulas with sources).
- Candidate: `ru.noties:jlatexmath-android:0.2.0` (650 KB, native). **Last release 2020, so unmaintained.** Verify it on API 24 and with R8; if inadequate, vendor and patch it or write a small native layout for the subset we use. Record the outcome in `DECISIONS.md`. No WebView.
- Document unsupported commands in the README under "Known LaTeX limits" and add a short authoring cheat sheet to the content repo.
- Accept: 25 formulas render at 320dp width; typical inline formula under 30 ms, cached under 2 ms; unit tests for the tokenizer.

### 2. Lesson player and result screen (original Step 7)
- `domain/session/`: a pure-Kotlin state machine (questions, current index, answers, mistakes, hearts, re-queue of wrong questions once at the end). `ui/lesson/`: question UIs for all five types: `multiple_choice` (choices shuffled with a stable per-attempt seed; `answerIndex` refers to original order), `fill_blank`, `type_answer`, `match_pairs` (one heart lost per question at most), `order_steps` (tap-to-place is primary). Bottom feedback panel (green/pink plus icon plus text, never color alone) with the explanation; optional question image (SVG via Coil, add dependency with a `DECISIONS.md` note).
- Layout: top = close + progress + hearts; middle scrolls; bottom = Check/Continue, always reachable. Closing mid-lesson asks for confirmation and discards progress. State survives rotation and font changes (the Activity already handles configChanges); after process death, restart the lesson cleanly.
- Hearts: lose one per wrong answer, regenerate one per 30 minutes from stored timestamps, no background service. At zero: end with "practice to regain a heart" or wait.
- XP: lesson `xp` scaled by accuracy (100% full, 80% = 80%, minimum 50%). Record the formula in `DECISIONS.md`.
- Result screen: XP, accuracy, streak change, goal progress, review list. Make lesson rows in `ui/category/CategoryScreen.kt` tappable (unlock rule: one open lesson per course by order; locked rows say what unlocks them).
- Unknown question types are skipped (`KNOWN_QUESTION_TYPES` in `domain/model/Content.kt`).
- Accept: a full seed lesson completes with all five types at 320x640dp and 1.3x font scale; unit tests for the state machine, hearts and XP.

### 3. Answer checking and keypad (`domain/answer/`, `ui/lesson/Keypad`)
- Normalize (trim, collapse whitespace, case, unicode minus and times/divide, trailing zeros). Numeric equivalence through a small safe expression parser you write: `+ - * / ^`, parentheses, `sqrt`, `pi`, implicit multiplication; **no `eval`, no script engines**; reject unsafe or over-long input. `1/2`, `0.5` and `\frac{1}{2}` are equal. Compare with tolerance; integers exactly.
- In-app keypad (numeric / algebra / full layouts from the question's `keypad` field): large keys, labeled for TalkBack, live math preview (debounced).
- Accept: unit tests including fractions, decimals and hostile input.

### 4. Progress database (Room migration 1 to 2)
- Add `LessonProgress`, `QuestionStat`, `DailyStat`, `UpdateLog` (fields in the original brief; index foreign keys and `dueAt`). Write a real `Migration(1, 2)` and a migration test; schemas are exported to `app/schemas/`. Removed lessons are marked hidden, not deleted, so progress survives.
- Replace the placeholder zeros on Home (streak, XP, "0 of 20 XP", "0 of N done") with real data.
- Settings in DataStore (Preferences): daily goal (10/20/30/50), theme (system/light/dark), sound, haptics, reduced motion, content URL, hearts, `heartsUpdatedAt`, streak-freeze count. Currently the app is forced to light mode (`MainActivity`); wire the theme choice and re-enable dark (dark tokens already exist).

### 5. SM-2 review (`domain/srs/`)
- Grade q: first-try correct 5, correct after retry 3, wrong 1. If q < 3: repetitions 0, interval 1 day, lapses++; else repetitions++ and interval 1, then 6, then `round(interval * ease)`. `ease = max(1.3, ease + 0.1 - (5-q) * (0.08 + (5-q) * 0.02))`. `dueAt` uses local day boundaries.
- Review session: up to 10 due questions across categories, loaded from lesson files by id; drop stats for questions that no longer exist. Wire the Home "Review" tile (currently static "Nothing due"). Unit tests for lapses and the ease floor.

### 6. Streaks, goals, stats (`domain/streak/`, `ui/stats/`)
- Streak = consecutive local days with the daily goal met; one freeze earned per 7 streak days, max 2; handle time-zone and clock changes without double counting. Stats screen: XP per day (14 days) drawn with Compose Canvas (no chart library), accuracy by category, lessons completed, best streak. No guilt-based nagging; any notification is opt-in and single.

### 7. Update hardening (finish the core in `UpdateManager`)
- MockWebServer tests: bad hash rolls back, oversize file, unsafe path, redirect to http, 304, schema/min-app mismatch, interrupted download.
- Resume: skip already verified files in the stage; exponential backoff exists (3 tries) but is not tested; mobile-data warning above a configurable size; low-storage check before download; `UpdateLog` rows; concurrent downloads (small limit); max total download guard; content URL setting with validation and reset.
- Optional private-repo token stored with Android Keystore, off by default, never logged.

### 8. Remaining Settings and quality
- Clear progress (confirm), export/import progress as JSON via the system file picker, licenses screen, long-press version opens Formula Test.
- Global exception handler writing a **local-only** crash log file; StrictMode in debug; corrupt-database detection with a reset offer and backup.
- Accessibility: content descriptions for every meaningful icon and math element, full TalkBack path through a lesson, reduce-motion setting.
- Small-phone pass at 320x640dp and 1.3x/2.0x font scale (recipe in `TESTING.md`), landscape must not clip or crash.
- Instrumented tests: complete a lesson, rotation keeps state, update against MockWebServer.
- **CI is not set up yet.** Add `.github/workflows/ci.yml` (JDK, Gradle cache, lint, unit tests, assemble debug, upload artifact) and a tag `v*` release workflow. The runner needs `platforms;android-37.0`; verify the SDK package name before relying on it. Free runners only.

### 9. Content and assets
- Add English, Software engineering and Electronics courses to `dopamine-content` (the icons `ic_english`, `ic_swe`, `ic_electronics` already exist and `iconFor` maps them). More lessons per course; keep the bundled seed small.
- A dedicated science icon is missing: `ic_science` maps to the lab flask `ic_test` from the SWE sheet. Render one more icon and add it to a sheet's prompt list in `tools/make_assets_md.py`.
- Hanko-stamp and petal motion from the original brief were dropped with the old theme; the new motion language is in `DESIGN.md`.

## 3. Known issues and honest gaps

- **Lessons cannot be opened.** The rows in `CategoryScreen` are display-only.
- Home numbers are static placeholders: streak 0, XP 0, "0 of 20 XP", course progress, "Nothing due". Nothing is stored yet.
- App is **light-only** for now (forced in `MainActivity`).
- Not yet verified at 320x640dp or 1.3x font; developed on a 1080x2400 phone (about 360dp wide). The goal tile and bento layout are the most likely to be tight.
- Update result texts are hard-coded in `SettingsViewModel` (should be string resources).
- `ContentStore` keeps the update ETag and timestamp in SharedPreferences, not DataStore (deviation from the brief; fine, record when migrating settings).
- `allowBackup=false` (privacy). Progress backup will be explicit export/import.
- Release APK is signed with the debug key; fine for personal sideload only.
- `contentVersion` strings like `2026.11.15+3` are produced by `build_manifest.py`; any monotonic string is accepted by the app.
- The original brief asked for a Japanese washi/ink theme. The owner changed it to white + green/yellow/pink. Treat `DESIGN.md` and `DECISIONS.md` as authoritative over any older text.

## 4. Questions for the owner (ask, do not assume)

- Course order and which of English / SWE / Electronics comes first.
- Should the Bot mascot appear in the lesson player (feedback poses exist as `mascot_*` icons)?
- Sound effects: on or off by default, and any preferred free sources (record licenses).


## Update 2026-11-16: navigation and basic lesson player
- Flow is now Home -> subject (category) -> course -> lessons -> lesson player. Floating bottom bar (Home, Review, Settings) shows everywhere except inside a lesson.
- Lessons open and play all 5 question types. LaTeX is shown as plain text via `ui/lesson/PlainMath.kt` until the native renderer exists.
- Questions with an `image` are skipped for now (no SVG renderer yet).
- Results are not saved yet (no hearts, XP or streak persistence). Review tab is a placeholder.
