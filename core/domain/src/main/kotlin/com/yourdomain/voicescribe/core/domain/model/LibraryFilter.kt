package com.yourdomain.voicescribe.core.domain.model

enum class SortField { DATE, DURATION, TITLE, SIZE }
enum class SortDirection { ASCENDING, DESCENDING }

data class SortOrder(
    val field: SortField = SortField.DATE,
    val direction: SortDirection = SortDirection.DESCENDING,
)

data class LibraryFilter(
    val query: String = "",
    val languages: Set<String> = emptySet(),
    val engines: Set<SttEngine> = emptySet(),
    val favoritesOnly: Boolean = false,
    val encryptedOnly: Boolean = false,
    val includeTrashed: Boolean = false,
    val dateRangeEpochMs: LongRange? = null,
    val sortOrder: SortOrder = SortOrder(),
)
