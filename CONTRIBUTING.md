# Contributing

Thanks for helping. This is a small, offline-first Android app, so a few rules keep it small and calm.

## Ground rules
- Kotlin and Jetpack Compose only. No WebView, no ads, no analytics, no Firebase or Google Play Services. The only permission is `INTERNET` (used by one button).
- Math is drawn natively (`ui/math/`). Content is data, never code.
- Never crash on bad content: validate, log, skip, show a gentle note.
- Strings go in `res/values/strings.xml`. Colors come from `ui/theme`. Touch targets are at least 48dp, and every screen must work at 320dp wide with a 1.3x font.
- Room: no destructive migrations. Bump the version, write a `Migration`, and commit the exported schema in `app/schemas/`.
- New dependencies need a clear reason in the pull request.

## Build and test
```bash
./build.sh                                          # tests + lint + debug and release APKs
./build.sh testDebugUnitTest lintDebug              # the quick check
./build.sh testDebugScreenshotTestDefaultTestSuite  # the design guard (see TESTING.md)
```
You need a JDK 21+ and the Android SDK (platform 37). All checks must pass before you open a pull request.

## The design guard
Screenshot tests compare the design system with reference images. If your change alters the look on purpose, update the references (`updateDebugScreenshotTestDefaultTestSuite`), look at them yourself, commit them and say why in the pull request. If it fails and you did not mean to change the look, fix the change, not the images.

## Content
Lessons live in [dopamine-content](https://github.com/Kmassidik/dopamine-content). Read its `STANDARD.md`; CI there rejects content that does not fit the layout.

## Commits and pull requests
Small commits with a clear message, and the project builds at every commit. Describe what changed and how you checked it (screenshots help for UI).
