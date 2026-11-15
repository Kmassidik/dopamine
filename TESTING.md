# TESTING

## Automated

```bash
./build.sh testDebugUnitTest      # JVM unit tests
./build.sh lintDebug
./build.sh                         # tests + lint + debug and release APKs
```

Current unit tests (21, all passing): `ContrastTest` (WCAG AA for every text pair, light and dark), `ManifestDiffTest` (new / changed / removed / unchanged, missing image re-fetch, unsafe path rejection), `LessonValidatorTest`, `ProfileNameTest`.

Planned (see `HANDOVER.md`): LaTeX tokenizer, SM-2, answer normalization and numeric equivalence, streak logic, hearts regeneration, XP, Room migrations (schemas in `app/schemas/`), update flow against MockWebServer (bad hash rollback, interrupted download), Compose UI test of a full lesson, rotation keeps state.

## On a device (the owner's setup)

```bash
ssh kurnia-laptop 'cd ~/Desktop/dopamine && git pull -q && ./build.sh assembleDebug'
ssh kurnia-laptop '~/Android/Sdk/platform-tools/adb install -r ~/Desktop/dopamine/app/build/outputs/apk/debug/app-debug.apk'
ssh kurnia-laptop '~/Android/Sdk/platform-tools/adb exec-out screencap -p > /tmp/s.png' && scp kurnia-laptop:/tmp/s.png .
```

Check `adb devices` shows `device` (not `unauthorized` or `no permissions`) and that the phone is unlocked. Do not `pm clear` the owner's phone (it erases their saved name).

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
