# Most consumer rules (Room, Koin, Media3, DataStore, SQLCipher) ship inside
# each library's own AAR via consumer-rules.pro and are applied
# automatically by R8 — this file only needs project-specific exceptions.

# Room entities/DAOs are referenced by generated code via reflection-free
# codegen (KSP), so no keep rules are required for them specifically.

# Keep Kotlin metadata for reflection-based debugging tools (Robolectric, etc.)
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
