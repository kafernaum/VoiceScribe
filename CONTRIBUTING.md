# Contributing to VoiceScribe

Thanks for considering a contribution. This document covers the conventions
the codebase already follows, so new code reads like it belongs.

## Project layout

Read `ARCHITECTURE.md` first if you haven't — it explains the module graph
(`core:*` vs `feature:*` vs `app`) and the MVI pattern every screen follows.
The short version for contributors: domain logic goes in `core:domain`
(pure Kotlin, no Android imports, no exceptions to this), Android
implementations of domain ports go in `core:data`/`core:audio`, and each
`feature:*` module owns exactly one screen area's UI + ViewModel.

## Kotlin conventions

- **Official Kotlin style**, enforced by `.editorconfig` at the repo root
  (`ktlint_official` code style). Run `./gradlew ktlintFormat` before
  committing if your IDE doesn't format on save.
- **No unjustified `!!`.** If a `!!` is truly unavoidable (e.g. a
  platform API that's `@Nullable` but documented as never actually null in
  the path you're using), add a `// SAFETY:` comment explaining why. The
  same goes for `lateinit var` — prefer constructor injection or nullable
  `var`s with clear ownership; if you do need `lateinit`, comment why a
  regular property doesn't work.
- **KDoc every public class, interface, and function** in `core:*` modules
  and every `UseCase`/`Repository`/port interface anywhere in the tree.
  Private/internal implementation details don't need it, but if a function
  does something non-obvious, a one-line comment beats silence.
- **Sealed types over enums with a `when` on the outside**, and `when`
  blocks over sealed types should not have an `else` branch — let the
  compiler catch missing cases when a new subtype is added.
- **Coroutines**: suspend functions and `Flow` builders belong in
  `core:domain`/`core:data`/`core:audio`; ViewModels collect flows in
  `viewModelScope`, never launch raw unstructured coroutines. Use
  `flowOn(Dispatchers.IO)` at the boundary where a flow starts doing
  blocking/IO work, not scattered throughout a chain.
- **No new third-party dependency without an ADR** if it changes an
  architectural boundary (a new DI framework, a new database, a new HTTP
  client — though per `PRIVACY_SECURITY.md` we don't expect to ever need
  the last one). A new leaf-level utility library doesn't need an ADR;
  swapping Koin for something else would.

## Git flow

- `main` is always releasable — CI (`.github/workflows/ci.yml`) must be
  green on `main` at all times.
- Branch names: `feature/<short-description>`, `fix/<short-description>`,
  `chore/<short-description>`, `docs/<short-description>`.
- Rebase your branch on `main` before opening a PR; we prefer a linear
  history and squash-merge PRs so `main`'s log stays one commit per
  logical change.
- Commit messages: imperative mood, short summary line (~50 chars), blank
  line, then body if needed explaining *why* not *what* (the diff already
  shows what). Example: `Add energy-based VAD fallback for pre-Silero devices`.
- Do not force-push to `main`. Force-pushing your own feature branch after
  a rebase is fine.

## Pull request checklist

Before opening a PR, confirm:

- [ ] `./gradlew ktlintCheck detekt` passes (style + static analysis).
- [ ] `./gradlew testDebugUnitTest` passes for every module you touched.
- [ ] New `UseCase`s, repository methods, and ViewModels have unit tests
      (JUnit5 + Turbine + MockK per `core:domain`/`core:data` conventions;
      see `StartRecordingUseCaseTest.kt` for the streaming-flow pattern and
      `RecordingRepositoryImplTest.kt` for the Robolectric+Room pattern).
- [ ] New Composables that carry real logic (not purely visual) have a
      `KoinModuleSmokeTest`-style DI resolution check if they pull from a
      new module, or a Compose UI test if their behavior is stateful.
- [ ] No new `MANAGE_EXTERNAL_STORAGE`, no new network permission, no new
      analytics/crash-reporting SDK — these violate the privacy guarantee
      documented in `PRIVACY_SECURITY.md` and will be rejected on sight.
- [ ] Public API changes in `core:domain` are reflected in `ARCHITECTURE.md`
      and, if the change is a genuine architectural trade-off, in a new
      `docs/adrs/NNNN-title.md`.
- [ ] `CHANGELOG`-worthy user-facing changes are mentioned in the PR
      description (there's no separate changelog file yet — the PR
      description is the source of truth until release notes are cut).

## PR template

Use this shape for the PR description (a `.github/PULL_REQUEST_TEMPLATE.md`
mirrors this so it's pre-filled automatically):

```markdown
## What

One or two sentences on what changed.

## Why

The motivation — a bug, a missing feature, a refactor that unblocks
something else. Link an issue if one exists.

## How

Anything a reviewer needs to know about the approach, especially if you
considered and rejected an alternative.

## Testing

What you ran locally: `./gradlew testDebugUnitTest`, specific manual
testing on a device/emulator, etc.

## Checklist

- [ ] Lint/static analysis passes
- [ ] Unit tests added/updated and passing
- [ ] No new cloud dependency, no `MANAGE_EXTERNAL_STORAGE`
- [ ] Docs/ADRs updated if this is an architectural change
```

## Code review expectations

- Reviewers should check module boundaries specifically: does a
  `feature:*` change accidentally add a dependency on `core:data` or
  `core:audio` instead of going through `core:domain`'s ports? The
  convention plugins (`build-logic/convention`) won't stop this at the
  Gradle level for every case, so it's a review-time check.
- Prefer requesting changes with a concrete suggestion over a vague "this
  could be cleaner" — this is a solo/small-team-friendly project and the
  goal is fast, specific feedback.

## Getting help

Open a GitHub issue with the `question` label, or start a draft PR early
and mark it as such if you want feedback on direction before finishing the
implementation.
