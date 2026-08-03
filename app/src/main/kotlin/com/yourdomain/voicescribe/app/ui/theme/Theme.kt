package com.yourdomain.voicescribe.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(primary = VoiceBlue40, secondary = VoiceGreen40)
private val DarkColors = darkColorScheme(primary = VoiceBlue80, secondary = VoiceGreen80)

/**
 * Light/Dark/System theming with Material You dynamic color on API 31+,
 * matching both the "Thematisation" and "Dynamic Color" requirements.
 * [useDynamicColor] is wired to Settings -> `dynamicColorEnabled`.
 */
@Composable
fun VoiceScribeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoiceScribeTypography,
        content = content,
    )
}
