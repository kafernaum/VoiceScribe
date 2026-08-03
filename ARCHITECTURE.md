# Architecture

VoiceScribe follows Clean Architecture (domain / data / presentation) with
an MVI presentation layer, split into Gradle modules along both axes:
`core:*` for shared, feature-agnostic layers, `feature:*` for one screen
area each. Module boundaries are enforced by the build graph, not just by
convention — see `docs/adrs/0005-domain-is-pure-kotlin.md`.

## Module graph

```mermaid
graph TD
    app["app (composition root)"] --> recording["feature:recording"]
    app --> library["feature:library"]
    app --> player["feature:player"]
    app --> settings["feature:settings"]
    app --> onboarding["feature:onboarding"]
    app --> data["core:data"]
    app --> audio["core:audio"]

    recording --> common["core:common"]
    recording --> domain["core:domain"]
    library --> common
    library --> domain
    player --> common
    player --> domain
    settings --> common
    settings --> domain
    onboarding --> common
    onboarding --> domain

    data --> domain
    data --> common
    audio --> domain
    audio --> common
    domain --> common

    wear["wear (companion, standalone app)"] -.optional data layer sync.-> app

    style domain fill:#4D7EBF,color:#fff
    style common fill:#4D7EBF,color:#fff
    style data fill:#4E8264,color:#fff
    style audio fill:#4E8264,color:#fff
```

Rules the graph is meant to make visually obvious:

- `feature:*` modules depend only on `core:common` and `core:domain`. They
  never depend on `core:data` or `core:audio` directly — those are wired in
  at runtime through Koin, from `app`'s composition root
  (`VoiceScribeApplication`). A feature module cannot accidentally reach
  into Room or `AudioRecord`.
- `core:domain` and `core:common` depend on nothing Android. They're plain
  `kotlin("jvm")` Gradle modules; the dependency arrows into them are the
  only thing every other module has in common.
- `core:data` and `core:audio` are peers: both implement domain ports
  (`RecordingRepository`/`SettingsRepository` and
  `AudioCaptureController`/`SpeechRecognizerProvider`/`ForegroundServiceController`
  respectively) and neither depends on the other.

## The recording pipeline (streaming mode)

```mermaid
sequenceDiagram
    participant UI as RecordingScreen (Compose)
    participant VM as RecordingViewModel (MVI)
    participant UC as StartRecordingUseCase
    participant AR as AudioRecordManager
    participant VAD as VadProcessor
    participant STT as SpeechRecognizerSession
    participant DB as RecordingRepository (Room)
    participant FG as ForegroundServiceController

    UI->>VM: onIntent(StartRecording(config))
    VM->>UC: invoke(config)
    UC->>STT: ensureModelDownloaded()
    UC->>DB: insert(Recording.createNew(...))
    UC->>FG: startRecording(id)
    UC->>AR: start(quality)
    loop every PCM chunk
        AR-->>UC: PcmChunk(samples, rmsLevel)
        UC->>VAD: classify(chunk)
        VAD-->>UC: SPEECH_* or SILENCE
        UC->>STT: transcribeStreaming(pcm) [if not silence]
        STT-->>UC: TranscriptSegment (partial/final)
        UC->>DB: appendOrUpdateSegment(id, segment)
        UC-->>VM: RecordingSessionState.Recording(...)
        VM-->>UI: RecordingUiState (StateFlow)
    end
    UI->>VM: onIntent(Stop)
    VM->>UC: (via StopRecordingUseCase) stop()
    UC->>AR: stop()
    UC->>FG: stop()
```

## MVI shape (feature:recording, feature:library — same pattern everywhere)

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> Preparing: StartRecording intent
    Preparing --> Recording: model ready, AudioRecord started
    Recording --> Recording: transcript segment / VU level update
    Recording --> Recording: PauseOrResume intent (isPaused toggles)
    Recording --> Stopped: Stop intent
    Preparing --> Error: model/session failure
    Recording --> Error: capture or STT failure
    Stopped --> [*]
    Error --> [*]
```

Every screen follows the same three-type shape:
`*Intent` (what the user/system asked for) -> a reducer inside the
ViewModel -> `*UiState` (one immutable snapshot, exposed as `StateFlow`) plus
`*Effect` (one-shot events — navigation, snackbars — exposed as a
`Channel`-backed `Flow` so they aren't redelivered on configuration change).

## Data model

```mermaid
erDiagram
    RECORDING ||--o{ BOOKMARK : has
    RECORDING ||--o{ TRANSCRIPT_SEGMENT : has
    RECORDING ||--|| RECORDING_FTS : "indexed by (external content)"

    RECORDING {
        string id PK
        string title
        string filePath
        long durationMs
        long sizeBytes
        long createdAtEpochMs
        string language
        string engine
        string mode
        boolean isEncrypted
        boolean isFavorite
        boolean isTrashed
        string tagsCsv
        string searchableText
    }
    BOOKMARK {
        string id PK
        string recordingId FK
        long positionMs
        string label
        boolean isAutoDetected
    }
    TRANSCRIPT_SEGMENT {
        string id PK
        string recordingId FK
        long startMs
        long endMs
        string text
        float confidence
        boolean isFinal
        string wordsEncoded
    }
```

See `docs/adrs/0004-fts4-instead-of-fts5.md` for why search is Room-native
FTS4 rather than raw FTS5, and `core/data`'s `RecordingRepositoryImpl` KDoc
for why list queries return recordings without their bookmarks/segments
hydrated (an intentional N+1 avoidance).

## Why these specific trade-offs

Every non-obvious architectural choice in this codebase has a short,
dedicated writeup in `docs/adrs/`:

- **0001** — Koin over Hilt
- **0002** — AudioRecord (not MediaRecorder) feeds the STT pipeline
- **0003** — STT engines behind a port + factory (isolates the early-access ML Kit GenAI API)
- **0004** — Room-native FTS4 instead of hand-rolled FTS5
- **0005** — `core:common`/`core:domain` are pure Kotlin/JVM, enforced by the build graph
- **0006** — hand-rolled `.docx`/PDF export instead of Apache POI/iText

Read `STT_ENGINES.md` and `PRIVACY_SECURITY.md` alongside this document —
together the three cover "how it's built," "how speech recognition is
chosen," and "what happens to the user's data," respectively.
