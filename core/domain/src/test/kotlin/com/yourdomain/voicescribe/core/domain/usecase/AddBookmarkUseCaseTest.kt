package com.yourdomain.voicescribe.core.domain.usecase

import com.yourdomain.voicescribe.core.common.AppResult
import com.yourdomain.voicescribe.core.domain.model.Bookmark
import com.yourdomain.voicescribe.core.domain.repository.RecordingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AddBookmarkUseCaseTest {

    private val repository = mockk<RecordingRepository>()
    private val useCase = AddBookmarkUseCase(repository, idGenerator = { "bookmark-1" })

    @Test
    fun `invoke inserts a bookmark with the injected id generator`() = runTest {
        coEvery { repository.addBookmark(any()) } returns AppResult.Success(Unit)

        val result = useCase(recordingId = "rec-1", positionMs = 5_000L, label = "Intro")

        assertTrue(result is AppResult.Success)
        coVerify {
            repository.addBookmark(
                Bookmark(id = "bookmark-1", recordingId = "rec-1", positionMs = 5_000L, label = "Intro", isAutoDetected = false),
            )
        }
    }

    @Test
    fun `invoke surfaces repository failure`() = runTest {
        val failure = AppResult.Failure(com.yourdomain.voicescribe.core.common.AppError.Unknown(IllegalStateException("db closed")))
        coEvery { repository.addBookmark(any()) } returns failure

        val result = useCase(recordingId = "rec-1", positionMs = 0L)

        assertTrue(result is AppResult.Failure)
    }
}
