# ADR 0003: STT engines behind a port + factory, not a single hardcoded API

## Status
Accepted

## Context
Three speech-to-text approaches are in scope: ML Kit GenAI (Gemini Nano with
a traditional-model fallback), the system `SpeechRecognizer`, and a
locally-bundled Whisper model. Their availability and capabilities vary by
device, OS version, and allow-listing status, and the ML Kit GenAI Speech
Recognition API is an early-access surface that may still change before
general availability.

## Decision
`core:domain` defines `SpeechRecognizerSession` + `SpeechRecognizerProvider`
as ports. `core:audio` provides one adapter per engine
(`MlKitGenaiSpeechRecognizerSession`, `SystemOnDeviceSpeechRecognizerSession`,
`WhisperCppSpeechRecognizerSession`) plus a `SpeechRecognizerFactory` that
picks the adapter matching `SttEngine`. Nothing above `core:audio` — no use
case, no ViewModel — references a concrete engine class.

## Rationale
- **Isolates an unstable, gated API.** `MlKitGenaiSpeechRecognizerSession`
  currently delegates to the system recognizer and documents exactly where
  to add the real Gemini Nano call once the app is allow-listed (see
  STT_ENGINES.md). Because it sits behind the same port as every other
  engine, that swap is a one-file change.
- **Graceful degradation is structural, not incidental.** `ML_KIT_GENAI_AUTO`
  falling back to `SYSTEM_ON_DEVICE` is implemented as one adapter wrapping
  another adapter of the same interface — not a chain of `if` statements
  scattered through the recording flow.
- **Testability.** `StartRecordingUseCaseTest` fakes `SpeechRecognizerSession`
  directly; it never touches `android.speech.SpeechRecognizer` or any
  ML Kit class, so the use case's business logic is verified independent of
  which engine is actually installed on a test device.

## Consequences
- Adding a fourth engine (e.g. a cloud-optional mode, if ever desired) means
  writing one new `SpeechRecognizerSession` implementation and one new
  `SttEngine` enum entry — no changes to `StartRecordingUseCase`,
  `RecordingViewModel`, or the Settings UI beyond the engine picker list.
- The abstraction has a cost: per-word timestamps aren't uniformly available
  (`android.speech.SpeechRecognizer` doesn't report them at all), so
  `TranscriptSegment.words` is allowed to be empty depending on the active
  engine — callers (the player's karaoke view) must treat it as optional.
