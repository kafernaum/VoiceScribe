package com.yourdomain.voicescribe.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

/**
 * Wear OS companion stub: start/stop buttons that message the phone app over
 * the Wearable Data Layer. **Companion stub**, not a full implementation —
 * per the project brief's own wording. To finish this:
 *
 * 1. On the phone side, register a `WearableListenerService` (in `:app`)
 *    that listens on [MESSAGE_PATH_START]/[MESSAGE_PATH_STOP] and forwards
 *    to `StartRecordingUseCase`/`StopRecordingUseCase`.
 * 2. Send recording state back down to the watch (elapsed time, level) via
 *    `DataClient` so this screen can show live status instead of just
 *    optimistic button state.
 */
class MainActivity : ComponentActivity() {

    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(
                onStart = { sendMessageToPhone(MESSAGE_PATH_START) },
                onStop = { sendMessageToPhone(MESSAGE_PATH_STOP) },
            )
        }
    }

    private fun sendMessageToPhone(path: String) {
        // TODO(#wear-data-layer): resolve the connected phone node id via
        // Wearable.getNodeClient(this).connectedNodes and send to each,
        // instead of a hard-coded broadcast path.
    }

    companion object {
        const val MESSAGE_PATH_START = "/voicescribe/start_recording"
        const val MESSAGE_PATH_STOP = "/voicescribe/stop_recording"
    }
}

@Composable
fun WearApp(onStart: () -> Unit, onStop: () -> Unit) {
    var isRecording by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(if (isRecording) "Recording…" else "VoiceScribe")
            Button(onClick = {
                isRecording = !isRecording
                if (isRecording) onStart() else onStop()
            }) {
                Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
            }
        }
    }
}
