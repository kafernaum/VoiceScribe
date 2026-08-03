# ADR 0004: Room-native FTS4, not raw FTS5, for full-text search

## Status
Accepted

## Context
The original brief calls for FTS5. Room has first-class annotation support
(`@Fts3`/`@Fts4`) for generating and keeping an external-content virtual
table in sync via generated triggers. Room has no equivalent `@Fts5`
annotation — FTS5 is only reachable by hand-writing the
`CREATE VIRTUAL TABLE ... USING fts5(...)` SQL yourself (typically inside a
`RoomDatabase.Callback` or a `Migration`) and hand-writing the sync triggers
Room would otherwise generate for you.

## Decision
`RecordingFtsEntity` uses `@Fts4(contentEntity = RecordingEntity::class)`.

## Rationale
- FTS4 covers everything this app's search needs: token matching with
  prefix queries (`token*`, used by `RecordingRepositoryImpl.toFtsMatchQuery`)
  over title, transcript, and tags.
- FTS5's main advantages over FTS4 — the `bm25()` ranking function, custom
  tokenizers, and better prefix-index performance on very large corpora —
  don't move the needle for a single user's personal recordings library
  (realistically hundreds to low thousands of rows, not millions).
- Room generating the content-sync triggers automatically (because it
  understands `@Fts4`) removes an entire category of bug: a hand-rolled
  FTS5 table needs its own `INSERT`/`UPDATE`/`DELETE` triggers hand-maintained
  in lockstep with every schema migration, which is exactly the kind of
  boilerplate Room's annotation processor exists to eliminate.

## Consequences
- If ranking quality becomes a real product need (e.g. once transcripts get
  long enough that "most relevant" beats "most recent" as a sort default),
  revisit this ADR: migrating to FTS5 means a Room `Migration` that creates
  the virtual table with raw SQL and installs the sync triggers by hand,
  since Room's codegen won't do it.
- Search relevance today is "matches, most-recent-first" (or whatever
  `LibraryFilter.sortOrder` requests), not relevance-ranked.
