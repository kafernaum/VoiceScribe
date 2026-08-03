package com.yourdomain.voicescribe.feature.recording.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.yourdomain.voicescribe.feature.recording.R

/**
 * Quick Settings tile for "start a new recording in one tap". Launches the
 * app's launcher Activity via [android.content.pm.PackageManager]
 * (`getLaunchIntentForPackage`) rather than referencing `:app`'s
 * `MainActivity` directly, which would create a reverse module dependency
 * (`:app` already depends on `:feature:recording`, not the other way round).
 */
class RecordingTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.tile_label)
            updateTile()
        }
    }

    @Suppress("DEPRECATION")
    override fun onClick() {
        super.onClick()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        launchIntent.putExtra(EXTRA_AUTOSTART_RECORDING, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(launchIntent)
        }
    }

    companion object {
        const val EXTRA_AUTOSTART_RECORDING = "extra_autostart_recording"
    }
}
