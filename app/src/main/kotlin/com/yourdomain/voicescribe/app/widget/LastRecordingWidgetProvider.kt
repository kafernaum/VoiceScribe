package com.yourdomain.voicescribe.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.yourdomain.voicescribe.app.R

/**
 * Classic `RemoteViews`-based home screen widget (rather than Jetpack
 * Glance) to avoid an extra dependency for what is, today, a single static
 * status line + tap-to-launch action. Swap to Glance if/when the widget
 * grows richer interactive state.
 */
class LastRecordingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId -> updateWidget(context, appWidgetManager, widgetId) }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_last_recording)
        views.setTextViewText(R.id.widget_status_text, context.getString(R.string.widget_tap_to_record))

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent != null) {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
