# Dopamine

Short, rewarding daily lessons on your phone, with **no ads, no accounts and no tracking**. Dopamine is a fully native Android app (Kotlin + Jetpack Compose) with 266 lessons in 14 courses: math from pre-algebra to precalculus, programming in C, Python and AI-assisted software engineering, science and electronics.

<p align="center">
  <img src="docs/screenshots/home.png" width="220" alt="Home screen with greeting, streak, XP and daily goal">
  <img src="docs/screenshots/subjects.png" width="220" alt="Subjects: Math, Science, Programming, Electronics, with lesson counts">
  <img src="docs/screenshots/diagram.png" width="220" alt="A geometry lesson with a right-triangle diagram and math">
</p>

*Screenshots are from a real Redmi Note 10S.*

## What you get

- **Learn, then practice.** Each lesson is a few short reading pages (text, worked examples that reveal one step at a time, tips, code) followed by questions with an explanation for every answer.
- **Unit notes and unit quizzes.** A cheat sheet for each unit, and a 10-question quiz (pass at 70%) that mixes questions from the whole unit.
- **Real math and code.** Math is drawn natively from LaTeX, geometry lessons carry SVG diagrams, and C and Python code appear in proper code blocks.
- **XP, streaks and a daily goal**, saved on your phone.
- **Private by design.** No ads, no accounts, no analytics, no WebView. The only permission is `INTERNET`, used only by the two update buttons.
- **Works offline after setup.** Tap **Update content** once to download lessons; nothing downloads in the background.

<p align="center">
  <img src="docs/screenshots/programming.png" width="170" alt="Programming courses">
  <img src="docs/screenshots/list.png" width="170" alt="A course with units, lessons, unit notes and unit quiz">
  <img src="docs/screenshots/mathex.png" width="170" alt="A worked example with revealed steps">
  <img src="docs/screenshots/code.png" width="170" alt="A C code block in a lesson">
  <img src="docs/screenshots/question.png" width="170" alt="A practice question">
  <img src="docs/screenshots/correct.png" width="170" alt="Feedback after a correct answer">
</p>

## Courses

| Subject | Courses | Lessons |
|---|---|---|
| Math | Pre-Algebra, Algebra 1, Geometry, Algebra 2, Precalculus | 123 |
| Programming | C Fundamentals, Arrays, Strings and Debugging, Algorithms in C, Memory and Pointers, Data Structures in C, Programming Basics (Python), AI Engineering | 137 |
| Science | Science Basics | 2 |
| Electronics | Electronics Basics | 4 |

Lessons live in a separate repository, [`Kmassidik/dopamine-content`](https://github.com/Kmassidik/dopamine-content), and arrive through the **Update content** button. A clean install has no lessons until you tap that button.

> **Status: early but usable.** Not built yet: spaced review, hearts, a final course test and settings such as dark theme. See the roadmap in the content repo for what is planned next.

## Try it (Android only)

Download `dopamine-0.3.2.apk` from the [latest release](https://github.com/Kmassidik/dopamine/releases/latest), open it on an Android 7.0+ phone (allow "install unknown apps" if asked), then open **Settings, Update content** once to download all lessons. There is no iPhone or web version. Future releases can be found with **Settings, Check for app update**.

**Upgrading from 0.3.0:** uninstall that debug-signed APK before installing 0.3.1; Android cannot upgrade across different signatures, and uninstalling erases its local progress. Releases from 0.3.1 onward use the same permanent signature and upgrade normally.

<p align="center">
  <img src="docs/screenshots/settings.png" width="220" alt="Settings with the Update content button">
</p>

## Documents

| File | Read it for |
|---|---|
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | How to build, test and send a change |
| [`DESIGN.md`](DESIGN.md) | Colors, type, shapes, motion, components |
| [`TESTING.md`](TESTING.md) | Test commands, the design guard (screenshot tests) and the manual checklist |

## Build and run

You need a JDK 21+ with `javac` (this project is built on JDK 25) and the Android SDK (platform 37, build-tools 35+).

```bash
./build.sh                    # unit tests + lint + debug and release APKs
./build.sh assembleDebug      # or any Gradle tasks
```

APKs land in `app/build/outputs/apk/{debug,release}/`. Release builds use the permanent private key configured in the root `local.properties`; without it, they fall back to the debug key for local contributor builds. Never commit a keystore or its passwords.

```properties
releaseStoreFile=/absolute/private/path/dopamine-release.jks
releaseStorePassword=...
releaseKeyAlias=dopamine
releaseKeyPassword=...
```

The same values can be supplied as `DOPAMINE_RELEASE_STORE_FILE`, `DOPAMINE_RELEASE_STORE_PASSWORD`, `DOPAMINE_RELEASE_KEY_ALIAS`, and `DOPAMINE_RELEASE_KEY_PASSWORD`. Back up the owner’s keystore and passwords: losing them means existing installs cannot accept future updates.

Install on a phone:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n app.dopamine/.MainActivity
```

On Xiaomi/MIUI, turn on *Developer options → Install via USB* and *USB debugging (Security settings)*, and tap Install on the phone promptly.

## How it works

```
 clean install         Update content (Settings)                 lessons, notes and practice
      |                         |                                          ^
  no lesson files  ->  filesDir/content  <-- swap <-- stage <-- verified downloads
```

- The app only reads `filesDir/content`. Clean installs start without lesson files; the first **Update content** downloads the complete manifest and all referenced files. Existing installs keep their current lessons.
- **Update** fetches `manifest.json` from the content repo (`BuildConfig.CONTENT_BASE_URL`), diffs lessons by id/version/sha256 (and re-fetches any file missing on the phone, for example after a file moved), downloads only what changed into a staged copy, verifies every file, deletes files the new manifest no longer references, and then swaps the staged folder in atomically. Any failure discards the stage and leaves current content untouched.
- **Check for app update** queries GitHub’s latest stable release only after a tap. If it is newer, **Open download** opens the release page in the browser; Android still requires the user to install the APK.
- Your name is stored on the phone in a Room (SQLite) database and used for the greeting.

## Repositories

- **App:** this repository.
- **Content:** [`dopamine-content`](https://github.com/Kmassidik/dopamine-content). Lesson JSON, SVG diagrams, JSON Schemas, and the manifest generator. To publish new lessons: edit files, run `python3 tools/build_manifest.py`, push to `main`, and tap Update in the app.

Manifest paths are relative to the content repo root and start with `content/` (for example `content/math/geometry/triangles/lesson-01.json`), because the app downloads `base URL + path`.

## Licenses

Fredoka and Nunito (SIL OFL 1.1, `licenses/Fredoka-OFL.txt`, `licenses/Nunito-OFL.txt`). Icons are generated by the owner. Libraries: AndroidX, Jetpack Compose, Room, Kotlin, kotlinx.serialization, kotlinx.coroutines (Apache 2.0); OkHttp (Apache 2.0).
