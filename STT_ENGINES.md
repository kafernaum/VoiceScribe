# Speech-to-text engines

VoiceScribe supports three on-device STT backends, selectable in
Settings and modeled as `SttEngine` (`core:domain`). All three run
entirely on the device — see `PRIVACY_SECURITY.md` for the no-cloud
guarantee this implies. The abstraction that makes swapping between them
possible is documented in `docs/adrs/0003-stt-engine-abstraction.md`.

## Comparison matrix

| | `ML_KIT_GENAI_AUTO` | `SYSTEM_ON_DEVICE` | `WHISPER_CPP_LOCAL` |
|---|---|---|---|
| Backing API | ML Kit GenAI Speech Recognition (Gemini Nano) with fallback | `android.speech.SpeechRecognizer` | Whisper tiny/base (INT8) via LiteRT/ONNX Runtime |
| Min API level | 34 for Gemini Nano path; falls back below that | 31 for guaranteed on-device (`createOnDeviceSpeechRecognizer`); best-effort on 26-30 | 26 (pure on-device inference, no OS dependency) |
| Device requirement | Gemini Nano: allow-listed Pixel/flagship devices only, today | Any device with Google's speech services | Any device — model is bundled/downloaded by the app itself |
| Streaming (live) transcription | Yes (via fallback today; see below) | Yes | No — batch/file only in this scaffold |
| Batch (file) transcription | Yes (via fallback today) | **No** — the OS recognizer is mic-first and can't take a file | Designed for this; not implemented in this scaffold (see below) |
| Word-level timestamps | Depends on fallback; not populated today | Not exposed by the platform API | Yes, once implemented — Whisper reports token timing |
| Implementation status | **Working today** via automatic fallback; real Gemini Nano call is a marked TODO | **Fully implemented** | **Structural stub only** — see below |

## `ML_KIT_GENAI_AUTO` — why it's a "fallback that works today"

At the time this project was generated, `com.google.mlkit:genai-speech-recognition`
is an early-access API gated behind allow-listing, and Google's own docs
note the API surface can still change before general availability
(check <https://developers.google.com/ml-kit/genai/speech-recognition> for
the current state). Rather than write code against an API surface that
might not match what ships, `MlKitGenaiSpeechRecognizerSession`
(`core/audio/stt/`) does the following:

1. Checks a conservative `isGeminiNanoSupported()` gate (currently just an
   OS-version check — replace with the real capability check once
   integrated).
2. Delegates every call to `SystemOnDeviceSpeechRecognizerSession`.

This means selecting `ML_KIT_GENAI_AUTO` in Settings **works correctly
today** — recording and live transcription function exactly as they would
under `SYSTEM_ON_DEVICE` — and upgrading to real Gemini Nano inference later
is a small, isolated change inside one file, not a redesign. The TODO
comments in that file mark exactly where the real ML Kit GenAI SDK calls
belong.

## `SYSTEM_ON_DEVICE` — fully implemented

`SystemOnDeviceSpeechRecognizerSession` wraps `android.speech.SpeechRecognizer`:
- API 31+: `SpeechRecognizer.createOnDeviceSpeechRecognizer(context)`, which
  Android documents as guaranteed to run on-device.
- API 26-30: `SpeechRecognizer.createSpeechRecognizer(context)` with
  `RecognizerIntent.EXTRA_PREFER_OFFLINE = true`. This is **best-effort**:
  the platform does not guarantee an offline path on every OEM build below
  API 31, only that the app has asked for one.

Known platform limitation: this API is mic-first (`RecognitionListener`
callbacks tied to `startListening()`), so it cannot transcribe an
already-recorded file — `transcribeFile()` returns
`AppError.Unknown(UnsupportedOperationException(...))` for this engine by
design. Use `ML_KIT_GENAI_AUTO` or `WHISPER_CPP_LOCAL` for file-mode
(`RecordingMode.FILE`) recordings.

## `WHISPER_CPP_LOCAL` — structural stub, not implemented

`WhisperCppSpeechRecognizerSession` exists to show where a fully
offline-bundled model would plug in, but does not run real inference:
`ensureModelDownloaded()` reports `0f` (never ready) and
`transcribeFile()` returns `AppError.SpeechModelNotDownloaded`. Finishing it
requires:

1. Converting a Whisper checkpoint (tiny or base) to INT8 LiteRT (`.tflite`)
   or ONNX using `whisper.cpp`'s or `openai-whisper`'s export tooling.
2. Shipping the model via **Play Asset Delivery** on-demand delivery, so the
   base APK stays under the 50 MB budget in `RELEASE_CHECKLIST.md` — do not
   bundle a multi-hundred-MB model directly in the APK/AAB.
3. Implementing mel-spectrogram feature extraction + encoder/decoder
   inference in place of the stubbed methods.

This mirrors `SileroVadProcessor` (`core/audio/vad/`), which has the same
"real model binary can't be generated as source code" limitation — see that
file's KDoc for the equivalent integration steps for VAD.

## Locale support

All three engines accept a BCP-47 locale string (`RecordingConfig.locale`).
`SYSTEM_ON_DEVICE`'s actual language coverage depends on which languages the
user has downloaded into Android's on-device speech recognition settings;
the app does not manage that download itself for this engine (unlike ML Kit
GenAI or a bundled Whisper model, where the app is responsible for the
model/language download progress shown in onboarding).
