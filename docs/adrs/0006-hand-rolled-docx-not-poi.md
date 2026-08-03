# ADR 0006: Hand-rolled minimal OOXML writer instead of Apache POI for .docx export

## Status
Accepted

## Context
Apache POI is the default answer for "generate a .docx from Java/Kotlin."
Its `.docx` (XWPF) support, however, pulls in parts of `org.apache.poi.util`
and image-handling code that transitively depend on `java.awt.*`, which does
not exist on Android (`java.awt` is part of desktop Java, not the Android
runtime). In practice this means POI's XWPF classes throw
`NoClassDefFoundError` at runtime on-device for some code paths — a known,
long-standing pain point for anyone shipping POI inside an Android APK
rather than a JVM backend.

## Decision
`DocxExporter` (feature:library) writes a `.docx` by hand: a `.docx` file
*is* a zip archive of a few XML parts
(`[Content_Types].xml`, `_rels/.rels`, `word/document.xml`), and this
project's export needs (a title, a metadata line, one paragraph per
transcript segment, no tables/images/track-changes) fit comfortably within a
hand-written OOXML document body.

## Rationale
- Zero risk of `java.awt` classes appearing on the Android runtime classpath,
  since nothing beyond `java.util.zip` (part of the Android SDK) is used.
- No new Gradle dependency, smaller APK, no risk of a future POI upgrade
  reintroducing an AWT dependency this project doesn't test for.
- The output is a real, valid OOXML document any version of Word or Google
  Docs can open — this is not a proprietary or "good enough" format, it's
  the same format POI would produce, just assembled directly.

## Consequences
- Rich formatting (styled tables, embedded images, track changes/comments)
  is out of scope for this exporter as written. If a future requirement
  needs those, either extend `DocxExporter`'s XML templates directly (it's
  plain string building, easy to extend incrementally) or reconsider POI
  behind a server-side/desktop export path instead of on-device.
- `PdfExporter` makes the same kind of call in reverse: it uses the
  platform's own `android.graphics.pdf.PdfDocument` rather than a
  third-party PDF library, sidestepping both the AWT problem and any
  PDF-library licensing question (e.g. iText's AGPL terms) entirely.
