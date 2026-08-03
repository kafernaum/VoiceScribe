# ADR 0001: Koin over Hilt for dependency injection

## Status
Accepted

## Context
The brief allows either Koin or Hilt. Both are legitimate choices for a
Compose-first, multi-module Android app in 2025-2026.

## Decision
Use Koin.

## Rationale
- **No annotation processing / KSP for DI itself.** Hilt requires Dagger's
  annotation processor across every module that contributes bindings, which
  measurably slows incremental builds in a multi-module project this size.
  Koin's `module { }` DSL is plain Kotlin, resolved at runtime.
- **`core:domain` can stay pure Kotlin/JVM.** Koin's `koin-core` artifact has
  zero Android dependency, so use cases can be wired via Koin
  (`core/domain/di/UseCaseModule.kt`) in the same module they're defined in.
  Hilt's `@Inject` constructor injection requires the Android Gradle plugin
  (or at least `dagger` + the AndroidX Hilt Gradle plugin) wherever
  `@InstallIn` is used, which would have pushed DI wiring for domain-layer
  classes out of `core:domain` and into an Android-aware module — undermining
  the "domain has zero Android dependency" boundary this project is built
  around (see 0005-domain-is-pure-kotlin.md).
- **Faster iteration while this scaffold's architecture was still settling.**
  Compile-time-safety is Hilt's strongest argument, and it is a real
  advantage; the trade-off here is deliberate, not a claim that Koin is
  strictly better in general.

## Consequences
- DI errors (a missing binding, a typo'd `get<T>()`) surface at runtime
  instead of compile time. `KoinModuleSmokeTest` (`app/src/test`) exists
  specifically to catch that class of bug in CI before it reaches a device.
- If the team later wants Dagger's compile-time graph validation, migrating
  is incremental: each `module { }` block maps close to 1:1 onto a Hilt
  `@Module` — there is no architectural rework required, only a DI-framework
  swap.
