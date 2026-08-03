# VoiceScribe

**On-device voice recording and transcription for Android. No cloud, no
accounts, no analytics — your audio and transcripts never leave the
device unless you explicitly export or share them.**

[![CI](https://img.shields.io/github/actions/workflow/status/yourorg/voicescribe/ci.yml?branch=main&label=CI)](.github/workflows/ci.yml)
[![Release](https://img.shields.io/github/actions/workflow/status/yourorg/voicescribe/release.yml?label=release)](.github/workflows/release.yml)
[![License: Apache 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Min SDK](https://img.shields.io/badge/minSdk-26-brightgreen)](app/build.gradle.kts)
[![Target SDK](https://img.shields.io/badge/targetSdk-35-brightgreen)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin)](gradle/libs.versions.toml)

> Replace the `yourorg/voicescribe` badge URLs above with your actual GitHub
> path once this repository has a real remote — they resolve to broken
> images until then.

## Screenshots

<!--
Add real screenshots/GIFs here before publishing. Suggested shots:
docs/images/recording-screen.png, docs/images/library-screen.png,
docs/images/player-screen.png, docs/images/settings-screen.png.
-->

| Recording | Library | Player | Settings |
|---|---|---|---|
| _screenshot placeholder_ | _screenshot placeholder_ | _screenshot placeholder_ | _screenshot placeholder_ |

## Features

- **Dual capture modes**: live streaming transcription (`AudioRecord` feeds
  the STT engine in real time) or file-based recording
  (`MediaRecorder`, compressed Opus/AAC) for later batch transcription.
- **Three on-device STT engines**, swappable in Settings — see
  `STT_ENGINES.md` for the full comparison and honest implementation-status
  notes: ML Kit GenAI (Gemini Nano, with automatic fallback), Android's
  built-in `SpeechRecognizer` (fully implemented), and a Whisper.cpp
  scaffold (structural stub, documented as such).
- **Voice activity detection** to skip silence during streaming
  transcription (energy-based fallback implemented; Silero VAD hook
  documented for a future release).
- **Full-text search** across transcripts (Room FTS4 — see
  `docs/adrs/0004-fts4-instead-of-fts5.md`).
- **Export** to `.txt`, `.srt`, `.vtt`, `.json`, `.docx`, `.pdf`, and `.zip`.
- **Encrypted at rest**: SQLCipher-backed Room database, passphrase held in
  `EncryptedSharedPreferences`/Android Keystore, optional biometric app
  lock. Full detail in `PRIVACY_SECURITY.md`.
- **No `MANAGE_EXTERNAL_STORAGE`, no `INTERNET` permission, no analytics.**
  Scoped storage only.
- Home screen widget, Quick Settings tile, app shortcuts, TalkBack support,
  dynamic color (Material You), and a Wear OS companion stub
  (`:wear` module).

## Architecture at a glance

Clean Architecture + MVI across Gradle feature modules. Read
`ARCHITECTURE.md` for the full module graph, sequence diagrams, and data
model (with Mermaid diagrams rendered inline). The short version:

```
app/                    composition root (Koin startup, navigation host)
core/common/             pure-Kotlin utilities, Result type
core/domain/              pure-Kotlin use cases, models, repository/port interfaces
core/data/                Room + SQLCipher repositories implementing domain ports
core/audio/               AudioRecord/MediaRecorder/STT/VAD/foreground service
feature/recording/        MVI screen: start/pause/stop, live transcript, waveform
feature/library/          MVI screen: search, filter, export, trash
feature/player/           MVI screen: playback, karaoke-style transcript highlight
feature/settings/         MVI screen: STT engine, quality, retention, biometric lock
feature/onboarding/       permission + model-download flow
wear/                     standalone Wear OS companion (stub)
build-logic/              Gradle convention plugins (shared module config)
```

Why each non-obvious choice was made lives in `docs/adrs/` — start with
`0001-koin-over-hilt.md` and `0002-audiorecord-not-mediarecorder-for-stt.md`
if you're new to the codebase.

## Requirements

- Android Studio Ladybug (2024.2.1) or newer, with AGP 8.6+ support.
- JDK 17 (bundled with recent Android Studio releases).
- Kotlin 2.0+ (managed by the version catalog, no separate install needed).
- An Android device or emulator running API 26+ (some STT/VAD paths behave
  best-effort below API 31 — see `STT_ENGINES.md`).

No Android SDK components beyond the standard platform/build-tools are
required — there is no NDK dependency in this scaffold today (the Whisper
LiteRT/ONNX path, once implemented, will add one — see `STT_ENGINES.md`).

## Building

```bash
git clone <this-repo-url>
cd VoiceScribe
./gradlew assembleDebug
```

Open the project root in Android Studio and let it sync — the module
structure and convention plugins in `build-logic/` are picked up
automatically via `settings.gradle.kts`'s `includeBuild("build-logic")`.

### Running tests

```bash
# Unit tests (JUnit5 + JUnit4/Robolectric via the vintage engine) for every module:
./gradlew test

# A single module:
./gradlew :core:domain:test

# Instrumented tests (needs a connected device/emulator):
./gradlew connectedDebugAndroidTest

# Lint + static analysis:
./gradlew lint ktlintCheck detekt
```

### Building a release AAB locally

```bash
export KEYSTORE_PATH=upload-keystore.jks   # relative to app/
export KEYSTORE_PASSWORD=...
export KEY_ALIAS=...
export KEY_PASSWORD=...
./gradlew bundleRelease
```

Without the keystore env vars set (and no `app/upload-keystore.jks`
present), `bundleRelease` still produces an **unsigned** AAB — useful for
verifying the build itself without needing real signing credentials on a
dev machine. See `app/build.gradle.kts`'s `signingConfigs` block for the
exact fallback logic.

## CI/CD

- **`.github/workflows/ci.yml`** — runs on every push/PR: lint, unit tests,
  `assembleDebug`, plus a separate instrumented-test job on a macOS runner
  via `reactivecircus/android-emulator-runner`.
- **`.github/workflows/release.yml`** — runs on `v*` tags: unit tests,
  signed `bundleRelease` (keystore decoded from the `UPLOAD_KEYSTORE_BASE64`
  secret), then `bundle exec fastlane deploy_internal` to push the AAB to
  the Play Console Internal Testing track.

See `RELEASE_CHECKLIST.md` for the full pre-release verification list,
rollout steps, monitoring guidance, and rollback procedure — read it
before cutting your first real release, not during an incident.

## Documentation index

| Document | Covers |
|---|---|
| `ARCHITECTURE.md` | Module graph, MVI shape, recording pipeline sequence diagram, data model |
| `STT_ENGINES.md` | The three STT backends, what's real vs. stubbed today, locale support |
| `PRIVACY_SECURITY.md` | Threat model, encryption at rest, Play Console Data Safety mapping |
| `CONTRIBUTING.md` | Kotlin conventions, git flow, PR checklist/template |
| `RELEASE_CHECKLIST.md` | Pre-release verification, rollout, monitoring, rollback |
| `docs/adrs/` | Architecture Decision Records for every non-obvious trade-off |

## Contributing

See `CONTRIBUTING.md`. Issues and PRs welcome — please read the module
boundary rules in `ARCHITECTURE.md` first, since the most common review
comment on external PRs is an accidental `feature:*` -> `core:data`
dependency that should have gone through `core:domain` instead.

## License

Apache License 2.0 — see `LICENSE`.
