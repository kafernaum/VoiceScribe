# Privacy & security

## The core guarantee: 100% on-device

VoiceScribe never sends audio or transcript data off the device, with one
exception the user always initiates explicitly: sharing/exporting a file
through Android's normal share sheet or Storage Access Framework picker
(`feature:library`'s export flow). There is no analytics SDK, no crash
reporter, and no network client anywhere in this codebase — grep for
`INTERNET` in `AndroidManifest.xml` across every module and you will not
find the permission requested at all.

This is a product requirement, not just a privacy preference: every STT
engine in `STT_ENGINES.md` is explicitly on-device (Gemini Nano via AICore,
the platform's on-device `SpeechRecognizer`, or a locally-bundled Whisper
model) — there is no cloud STT fallback path in this codebase to
accidentally enable.

## Threat model

| Asset | Threat | Mitigation |
|---|---|---|
| Recorded audio files | Device theft/loss, another app reading app-private storage | Scoped storage only (app-private `files/recordings/`), no `MANAGE_EXTERNAL_STORAGE`; SQLCipher-encrypted metadata (see below) |
| Transcript text & metadata | Same as above, plus SQL injection via search | Room (parameterized queries only — `RecordingDao`'s `@Query` methods never string-concatenate user input into SQL); database file encrypted via SQLCipher |
| The encryption passphrase itself | Extraction from a rooted/compromised device | Passphrase is a random 256-bit value stored only inside `EncryptedSharedPreferences`, whose own key lives in the Android Keystore (StrongBox-backed where available) — see `DatabasePassphraseKeystore` |
| Exported files (.docx/.pdf/.zip/etc.) | User shares the wrong file, or a malicious app intercepts a `content://` URI | Exports go through `FileProvider` with `grantUriPermissions` scoped to the receiving app only, never a world-readable path |
| "Incognito" sessions | Data persisting when the user explicitly asked for it not to | Incognito mode (Settings) skips the repository insert/update calls entirely for that session — nothing is written, not written-then-deleted |
| Auto-backup exfiltration | Android Auto Backup / device transfer silently including the encrypted DB or its passphrase | Both the Room database file and `voicescribe_secure_prefs.xml` are explicitly excluded in `xml/backup_rules.xml` and `xml/data_extraction_rules.xml` — see the comments there for why restoring an encrypted DB onto a different device's Keystore would be unrecoverable anyway |

## Encryption at rest

- **Database**: Room, opened through SQLCipher's `SupportFactory`
  (`VoiceScribeDatabase.build`). The passphrase is a random 256-bit value,
  generated once and stored in `EncryptedSharedPreferences`
  (`DatabasePassphraseKeystore`), which is itself backed by a Keystore-held
  AES256-GCM master key (`MasterKey.KeyScheme.AES256_GCM`).
- **Optional app-level lock**: Settings -> "Require biometric unlock" gates
  *opening the app UI* behind `BiometricPrompt`, layered on top of (not
  instead of) the at-rest database encryption above. Losing a biometric
  check does not, by itself, decrypt anything — it's a UX gate implemented
  in `feature:settings`/`feature:onboarding`, separate from the SQLCipher
  passphrase.
- **Per-recording encryption flag**: `Recording.isEncrypted` /
  `RecordingEntity.isEncrypted` exists so a future release could support
  mixed encrypted/unencrypted recordings (e.g. for a "quick voice memo, don't
  bother encrypting" fast path); today `RecordingConfig.encryptAtRest`
  defaults to `true` for every new recording.

## Data Safety Form mapping (Play Console)

When filling out the Play Console Data Safety section, this app's actual
behavior maps to:

- **Data collected**: audio (microphone), user-generated text (transcripts).
- **Data shared with third parties**: none.
- **Data processing location**: on-device only.
- **Data encrypted in transit**: not applicable — nothing is transmitted.
- **Data encrypted at rest**: yes (SQLCipher; see above).
- **Users can request data deletion**: yes, in-app (delete/trash, or export
  then delete — no server-side data exists to separately delete).
- **Permissions declared and why**: `RECORD_AUDIO` (core recording feature),
  `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MICROPHONE` (keep recording
  alive while backgrounded), `POST_NOTIFICATIONS` (the recording-in-progress
  notification), `READ_MEDIA_AUDIO` (scoped media access, API 33+),
  `USE_BIOMETRIC` (optional app lock).

## Auto-suppression / retention

`SettingsRepository.autoDeleteTrashDays` (default 30) drives
`DeleteRecordingUseCase.purgeExpired`, which permanently deletes anything
moved to trash more than N days ago. This should be invoked from a periodic
`WorkManager` job in a production build (the `androidx.work:work-runtime-ktx`
dependency is already in the version catalog for exactly this) — wiring
that job is a natural next step beyond this scaffold's scope.

## What this scaffold does *not* implement

Being direct about gaps matters more here than anywhere else in the repo:

- Real Gemini Nano inference (`ML_KIT_GENAI_AUTO` currently falls back — see
  `STT_ENGINES.md`) and the Whisper local engine are not wired to real
  models; both fail closed (fall back or report "not ready") rather than
  silently pretending to work.
- The periodic trash-purge `WorkManager` job described above is not
  scheduled anywhere yet — `DeleteRecordingUseCase.purgeExpired` exists and
  is unit-testable, but nothing currently calls it on a timer.
- Export sharing relies on the OS share sheet's own permission model; this
  project does not add extra confirmation dialogs before sharing a
  recording, on the assumption that the OS share sheet itself is the user's
  explicit consent step.
