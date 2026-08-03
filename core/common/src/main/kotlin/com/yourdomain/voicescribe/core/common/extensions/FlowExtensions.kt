package com.yourdomain.voicescribe.core.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

/**
 * Runs [Flow] collection on [context] and swallows exceptions into [onError],
 * so a single malfunctioning pipeline stage (e.g. a VAD hiccup) can't crash
 * the whole recording session.
 */
fun <T> Flow<T>.flowOnSafely(
    context: CoroutineContext,
    onError: suspend (Throwable) -> Unit = {},
): Flow<T> = this
    .flowOn(context)
    .catch { throwable -> onError(throwable) }

/** Splits a millisecond duration into `HH:MM:SS.mmm`, used across export formats. */
fun Long.toTimestamp(includeMillis: Boolean = true): String {
    val hours = this / 3_600_000
    val minutes = (this % 3_600_000) / 60_000
    val seconds = (this % 60_000) / 1_000
    val millis = this % 1_000
    return if (includeMillis) {
        "%02d:%02d:%02d.%03d".format(hours, minutes, seconds, millis)
    } else {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

/** SRT uses a comma instead of a dot before milliseconds. */
fun Long.toSrtTimestamp(): String = toTimestamp(includeMillis = true).replace('.', ',')
