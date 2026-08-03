#!/bin/bash
# Builds VoiceScribe Lite -- a minimal standalone recorder/player demo APK --
# using only classic AOSP command-line tools (aapt, javac, dx, zipalign,
# apksigner), no Gradle/AGP and no AndroidX/Compose/Room/Koin.
#
# Why this exists: the full VoiceScribe app (see the repo root) needs Google
# Maven + Maven Central to resolve AndroidX/Compose/Room/Koin/SQLCipher, so
# it can only be built with Android Studio or a CI runner that has normal
# internet access. This script instead builds a reduced demo -- record audio
# to a file, list recordings, play them back, nothing else -- entirely from
# tools available offline (Debian/Ubuntu's `android-sdk-build-tools`,
# `libandroid-23-java`, `aapt`, `zipalign`, `apksigner`, `dalvik-exchange`'s
# `dx`), so there is a genuinely installable APK even in network-restricted
# environments.
#
# Requirements (Debian/Ubuntu):
#   sudo apt-get install -y aapt libandroid-23-java android-sdk-platform-23 \
#       zipalign apksigner android-sdk-build-tools dalvik-exchange
#
# Usage: ./build.sh   (run from this directory)

set -euo pipefail
cd "$(dirname "$0")"

ANDROID_JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
DX=/usr/lib/android-sdk/build-tools/debian/dx

if [ ! -f "$ANDROID_JAR" ]; then
    echo "android.jar not found at $ANDROID_JAR -- install libandroid-23-java + android-sdk-platform-23" >&2
    exit 1
fi
if [ ! -x "$DX" ]; then
    echo "dx not found at $DX -- install dalvik-exchange (NOT the unrelated 'dx' package, which is OpenDX)" >&2
    exit 1
fi

rm -rf gen obj bin
mkdir -p gen obj bin

echo "== aapt: generating R.java + base resource APK =="
aapt package -f -m -J gen -M AndroidManifest.xml -S res -I "$ANDROID_JAR"

echo "== javac =="
javac -encoding UTF-8 -source 8 -target 8 \
    -bootclasspath "$ANDROID_JAR" -classpath "$ANDROID_JAR" \
    -d obj \
    gen/com/yourdomain/voicescribelite/R.java \
    src/com/yourdomain/voicescribelite/MainActivity.java

echo "== dx: dexing =="
"$DX" --dex --output=bin/classes.dex obj

echo "== aapt: packaging resources =="
aapt package -f -M AndroidManifest.xml -S res -I "$ANDROID_JAR" -F bin/app-unsigned.apk
zip -j bin/app-unsigned.apk bin/classes.dex

echo "== zipalign =="
zipalign -f -p 4 bin/app-unsigned.apk bin/app-aligned.apk

echo "== debug keystore (generated once, reused on rebuilds) =="
if [ ! -f bin/debug.keystore ]; then
    keytool -genkeypair -v \
        -keystore bin/debug.keystore \
        -alias androiddebugkey -storepass android -keypass android \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=VoiceScribe Lite Debug, OU=Dev, O=VoiceScribe, L=Unknown, S=Unknown, C=US"
fi

echo "== apksigner: signing =="
apksigner sign \
    --ks bin/debug.keystore --ks-key-alias androiddebugkey \
    --ks-pass pass:android --key-pass pass:android \
    --out bin/VoiceScribeLite.apk \
    bin/app-aligned.apk

echo "== verify =="
apksigner verify -v bin/VoiceScribeLite.apk
aapt dump badging bin/VoiceScribeLite.apk | head -6

echo ""
echo "Done: tools/lite-demo/bin/VoiceScribeLite.apk"
