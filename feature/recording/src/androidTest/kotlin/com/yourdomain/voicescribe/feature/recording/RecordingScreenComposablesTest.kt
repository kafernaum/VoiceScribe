package com.yourdomain.voicescribe.feature.recording

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourdomain.voicescribe.core.domain.model.TranscriptSegment
import com.yourdomain.voicescribe.feature.recording.ui.LiveTranscriptView
import com.yourdomain.voicescribe.feature.recording.ui.RecordingTopBar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the recording screen's stateless composables directly (no Koin,
 * no ViewModel) — the fastest, least flaky layer of Compose UI test, per
 * RELEASE_CHECKLIST.md's testing guidance.
 */
@RunWith(AndroidJUnit4::class)
class RecordingScreenComposablesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recordingTopBar_showsPhaseAndElapsedTime() {
        composeTestRule.setContent {
            RecordingTopBar(
                state = RecordingUiState(phase = RecordingUiState.Phase.RECORDING, elapsedMs = 65_000L),
                onClose = {},
            )
        }

        composeTestRule.onNodeWithText("Recording  00:01:05").assertExists()
    }

    @Test
    fun liveTranscriptView_rendersFinalSegmentAndPartialText() {
        composeTestRule.setContent {
            LiveTranscriptView(
                segments = listOf(
                    TranscriptSegment(id = "s1", startMs = 0, endMs = 1_000, text = "Hello there", confidence = 0.9f, isFinal = true),
                ),
                partialText = "and welcome",
                locale = "en-US",
            )
        }

        composeTestRule.onNodeWithText("Hello there").assertExists()
        composeTestRule.onNodeWithText("and welcome").assertExists()
    }
}
