package com.yourdomain.voicescribe.feature.onboarding.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Requests [Manifest.permission.RECORD_AUDIO] using the plain
 * `ActivityResultContracts` API — no Accompanist Permissions dependency,
 * since one platform permission doesn't need a whole library.
 */
@Composable
fun PermissionRationaleScreen(onGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) onGranted() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("VoiceScribe needs microphone access to record and transcribe audio.")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Everything is processed on this device — audio is never uploaded.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val alreadyGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            if (alreadyGranted) onGranted() else launcher.launch(Manifest.permission.RECORD_AUDIO)
        }) {
            Text("Grant microphone access")
        }
    }
}
