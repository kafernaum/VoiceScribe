# VoiceScribe Lite (offline demo build)

A minimal, standalone audio recorder/player, built and signed entirely with
classic AOSP command-line tools -- **not** part of the Gradle multi-module
VoiceScribe project in the rest of this repo, and not built with Gradle at
all.

## Why this exists

The full VoiceScribe app (repo root) is built on AndroidX, Jetpack Compose,
Room, Koin, and SQLCipher -- all resolved from Google Maven / Maven Central.
In network-restricted environments (no access to `dl.google.com` /
`repo.maven.apache.org` / the Gradle Plugin Portal), that dependency graph
simply cannot be fetched, so `./gradlew assembleDebug` cannot succeed there
no matter what.

This directory is a fallback: a tiny app using only the plain Android
framework (`MediaRecorder`, `MediaPlayer`, classic `View`s -- no AndroidX,
no Compose, no third-party libraries at all), built by hand with:

- `aapt` (resource compilation + APK packaging, generates `R.java`)
- `javac` against Debian's packaged `android.jar` (API 23, from
  `libandroid-23-java` / `android-sdk-platform-23`)
- `dx` (dex compiler, from the `dalvik-exchange` package -- **not** the
  unrelated `dx` apt package, which is IBM/OpenDX visualization software
  that happens to share the name)
- `zipalign` + `apksigner` (both available as standalone Debian/Ubuntu
  packages)

All of the above are ordinary `apt` packages on Debian/Ubuntu (they come
from Debian's Android Tools Team packaging of AOSP source, not from Google's
SDK distribution), so this whole pipeline runs with zero access to Google
Maven or Maven Central.

## What it does and doesn't do

Does: record audio to an `.m4a` file in the app's private external-files
directory, list past recordings sorted newest-first, tap to play/stop
playback, request `RECORD_AUDIO` at runtime.

Doesn't: transcription/STT, a database, search, encryption, export formats,
Compose UI, background/foreground-service recording, or anything else from
the full spec. It is a toolchain smoke test with a real, usable core
feature -- not a reduced version of the real app's architecture.

## Building

```bash
sudo apt-get install -y aapt libandroid-23-java android-sdk-platform-23 \
    zipalign apksigner android-sdk-build-tools dalvik-exchange
cd tools/lite-demo
./build.sh
```

Output: `bin/VoiceScribeLite.apk`, signed (v1/v2/v3) with a freshly
generated debug keystore (`bin/debug.keystore`, password `android` --
this is a throwaway debug key, not for any real release).

## Installing

```bash
adb install -r bin/VoiceScribeLite.apk
```

or copy the APK to the phone and open it directly (enable "install unknown
apps" for whichever app you use to open it -- file manager, browser, etc.).
`minSdkVersion`/`targetSdkVersion` are both 23 (Marshmallow), so it installs
on essentially any phone in real-world use today.
