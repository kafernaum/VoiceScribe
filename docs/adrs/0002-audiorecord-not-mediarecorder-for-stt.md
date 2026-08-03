# ADR 0002: AudioRecord (not MediaRecorder) feeds the streaming STT pipeline

## Status
Accepted

## Context
Android offers two ways to capture microphone audio: `MediaRecorder`
(encodes straight to a compressed file) and `AudioRecord` (hands back raw
PCM buffers). VoiceScribe needs both: a saved audio file *and* a live
transcript while recording.

## Decision
`AudioRecordManager` (core:audio) wraps `AudioRecord` and feeds raw 16 kHz
mono PCM into the VAD -> STT pipeline (`StartRecordingUseCase`).
`MediaRecorderWrapper` wraps `MediaRecorder` separately, producing the
Opus/AAC file that gets saved and shared. In `RecordingMode.STREAMING` both
can run concurrently against the same physical microphone in practice on
most OEMs (two capture clients on `AudioRecord`-class sources), but the
architecture keeps them as two independent ports precisely so a given build
can choose to run only one depending on `RecordingConfig.mode`.

## Rationale
- `MediaRecorder` cannot hand you PCM — it only writes an encoded container
  to a file. There is no supported way to tap the encoder's input for a
  parallel STT engine.
- `AudioRecord` buffers are exactly what `SpeechRecognizer`-style engines and
  ONNX/LiteRT models expect: raw 16-bit PCM at a fixed sample rate.
- Opus/AAC compression (via `MediaRecorder`) is unsuitable for STT: STT
  engines are tuned against raw or lightly-processed PCM, and re-decoding a
  compressed file mid-stream would add latency that defeats the point of
  "streaming" recognition.

## Consequences
- Two separate Android capture APIs are active at once in streaming mode,
  which is more moving parts than a single-API design. `RecordingForegroundService`
  and the permission model treat both as one logical "recording session."
- File-mode recordings (`RecordingMode.FILE`) skip `AudioRecord` entirely and
  transcribe the finished file afterwards via `TranscribeFileUseCase`,
  avoiding the dual-capture complexity when live transcription isn't needed.
