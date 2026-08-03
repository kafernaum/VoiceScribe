# ADR 0005: core:common and core:domain are pure Kotlin/JVM modules

## Status
Accepted

## Context
Clean Architecture says the domain layer shouldn't depend on frameworks. In
an Android project it's easy to say that and still let `android.content.Context`
leak into a "domain" module because it's convenient. This project enforces
the boundary at the build-system level instead of by convention alone.

## Decision
`core:common` and `core:domain` apply `kotlin("jvm")`
(`org.jetbrains.kotlin.jvm`), not `com.android.library`. Neither module can
import anything from `android.*` — the Gradle module simply has no Android
classpath to import it from. All Android-specific ports (`AudioCaptureController`,
`SpeechRecognizerProvider`, `ForegroundServiceController`, etc.) are defined
in `core:domain` using only Kotlin/JVM types (`ShortArray`, `Flow<T>`,
`kotlin.Result`-style sealed classes) and implemented in `core:audio`/
`core:data`, which *are* Android library modules.

## Rationale
- **The boundary can't silently rot.** A reviewer forgetting Clean
  Architecture rules and importing `android.content.Context` into a use case
  gets a compile error, not a code-review nitpick.
- **Domain and use case unit tests run as plain JUnit5 on the JVM** — no
  Robolectric, no instrumented test, no emulator. `StartRecordingUseCaseTest`
  and `AddBookmarkUseCaseTest` run in milliseconds in any CI runner.
- **Koin's `koin-core` (pure JVM) lets DI wiring for use cases live inside
  `core:domain` itself** (`core/domain/di/UseCaseModule.kt`) — see
  0001-koin-over-hilt.md.

## Consequences
- Anything that seems like it "should" live in domain but needs an Android
  type (e.g. `Uri` for an export destination) has to be modeled abstractly
  instead (`ExportWriter.export(..., destinationUri: String)` takes a
  `String`, not a `Uri` — `core:domain` doesn't know what a `Uri` is).
- `java.util.UUID` and `System.currentTimeMillis()` are used directly in use
  cases (e.g. `StartRecordingUseCase`'s default `idGenerator`/`clock`
  parameters) since they're part of the JDK, not the Android SDK — this is
  intentional and fine under this rule, and both are injectable for tests.
