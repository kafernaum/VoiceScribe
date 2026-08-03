# Release checklist

This is the step-by-step for cutting a VoiceScribe release, from
pre-release verification through Play Console rollout, monitoring, and
rollback. It cross-references the CI/CD workflows
(`.github/workflows/release.yml`), the fastlane lanes (`fastlane/Fastfile`),
and the constraints documented in `STT_ENGINES.md`/`PRIVACY_SECURITY.md`.

## 1. Pre-release verification

- [ ] `main` is green on CI (`.github/workflows/ci.yml`): lint, unit tests,
      `assembleDebug`, and the instrumented-test job all passing.
- [ ] `./gradlew ktlintCheck detekt testDebugUnitTest` passes locally as a
      final sanity check before tagging.
- [ ] Manually smoke-test on at least one physical device (not just an
      emulator) covering:
  - [ ] Start/pause/resume/stop a streaming recording end-to-end.
  - [ ] Start and finish a file-mode (`MediaRecorder`) recording.
  - [ ] All three `SttEngine` options selectable in Settings without a
        crash (remember `ML_KIT_GENAI_AUTO` and `WHISPER_CPP_LOCAL` are
        documented fallback/stub paths per `STT_ENGINES.md` — verify they
        *fail closed* gracefully, not that they produce real Gemini
        Nano/Whisper output).
  - [ ] Export a recording to each format (`.txt`, `.srt`, `.vtt`, `.json`,
        `.docx`, `.pdf`, `.zip`) and open each exported file in a
        third-party app to confirm it's valid, not just that the export
        call returned success.
  - [ ] Kill the app while a foreground recording is active; confirm the
        notification persists and recording continues (Android 14+
        `FOREGROUND_SERVICE_TYPE_MICROPHONE` behavior).
  - [ ] TalkBack on: navigate the recording screen and library list purely
        by swipe/explore-by-touch.
  - [ ] Home screen widget and Quick Settings tile both launch the app and
        reflect current recording state.
- [ ] Check the release AAB's size: base APK should stay under the ~50 MB
      budget referenced in `STT_ENGINES.md` (this matters most once a real
      Whisper model is bundled via Play Asset Delivery — verify it's
      on-demand delivery, not baked into the base module).
- [ ] Re-read `PRIVACY_SECURITY.md`'s "what this scaffold does not
      implement" section and confirm the Data Safety Form in Play Console
      still matches reality (no new SDK quietly added a network call).
- [ ] Bump `versionCode`/`versionName` in `app/build.gradle.kts`.
- [ ] Update `CHANGELOG`/release notes (Play Console release notes per
      locale, `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt`
      if using fastlane's metadata layout).

## 2. Secrets and signing

Confirm these secrets are current in the GitHub repo's Actions secrets
(consumed by `.github/workflows/release.yml`):

- [ ] `UPLOAD_KEYSTORE_BASE64` — base64-encoded upload keystore.
- [ ] `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — matching the
      keystore above.
- [ ] `PLAY_CONSOLE_JSON_KEY` — service account JSON with Play Console API
      access, scoped to this app only.
- [ ] Confirm the service account still has "Release to production/testing
      tracks" permission in Play Console (Play Console permissions expire
      or get revoked more often than people expect — check this before you
      need it, not during an incident).

## 3. Tag and build

- [ ] Merge the release branch to `main`.
- [ ] Tag: `git tag vX.Y.Z && git push origin vX.Y.Z` — this triggers
      `.github/workflows/release.yml`.
- [ ] Watch the workflow run: `testReleaseUnitTest` → `bundleRelease`
      (signed with the decoded keystore) → `bundle exec fastlane
      deploy_internal` → AAB uploaded as a workflow artifact.
- [ ] Confirm the workflow's `if: always()` cleanup step actually removed
      the decoded keystore/JSON key from the runner's disk (check the
      job log for the cleanup step, don't just assume).

## 4. Rollout

- [ ] `deploy_internal` (fastlane lane) lands the build on the Internal
      Testing track first — confirm it's visible there in Play Console
      before doing anything else.
- [ ] Internal testing sign-off (even if that's just you) covering the
      smoke-test list in step 1 again, on the actual signed release build
      this time, not a debug build.
- [ ] Promote to Closed Testing (if you use one) or directly to Production
      using the `promote_to_production` fastlane lane, with a **staged
      rollout percentage** (start at 10-20%, not 100%) — Play Console
      supports staged rollout natively; use it every time.
- [ ] Watch the staged rollout for at least 24-48 hours before increasing
      the percentage.

## 5. Monitoring post-release

VoiceScribe intentionally has no crash reporter or analytics SDK (see
`PRIVACY_SECURITY.md` — this is a product privacy guarantee, not an
oversight), so post-release monitoring relies on Play Console's own
signals instead of a third-party dashboard:

- [ ] Play Console → **Android vitals**: watch crash rate, ANR rate, and
      excessive wakeups/battery usage for the new version specifically
      (filter by version code).
- [ ] Play Console → **Ratings and reviews**: filter to the new version;
      watch for a spike in low ratings mentioning recording, transcription,
      or crashes.
- [ ] Play Console → **Statistics**: install/uninstall rate for the new
      version relative to baseline.
- [ ] If you have any beta users you communicate with directly, ping them
      explicitly after a rollout — this app has no in-app feedback
      mechanism, so direct outreach is the fastest signal.

## 6. Rollback

If Android vitals or reviews show a serious regression during the staged
rollout:

- [ ] **Halt the staged rollout** in Play Console immediately (this stops
      the percentage from increasing further; it does not un-install from
      existing users).
- [ ] If the regression is severe (crash loop, data loss), use Play
      Console's **"Release" → "Halt rollout"**, then prepare a new patch
      release with `versionCode` incremented again — Play Console does not
      support reverting to a previously-published version code, only
      rolling forward with a fix.
- [ ] For a patch release, skip straight back to step 1 of this checklist
      with the fix; do not shortcut verification just because it's urgent
      — a rushed rollback release that introduces a second bug is worse
      than a slower correct one.
- [ ] Document the incident: what broke, what the fix was, what check
      should have caught it in step 1's smoke test list (and add it there
      if it's missing) — this checklist should get stricter over time, not
      stay static.

## 7. Post-rollout cleanup

- [ ] Once fully rolled out (100%) and stable for a few days, delete the
      downloaded AAB workflow artifact if you don't need it archived
      elsewhere.
- [ ] Close the milestone/issue tracking this release, if you use GitHub
      Milestones.
