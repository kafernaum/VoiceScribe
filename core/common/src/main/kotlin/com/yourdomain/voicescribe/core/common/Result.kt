package com.yourdomain.voicescribe.core.common

/**
 * A sealed result type used throughout the domain/data layers instead of
 * throwing exceptions across coroutine boundaries. Prefer this over Kotlin's
 * built-in `Result<T>` because [Failure] carries a typed, UI-displayable
 * [AppError] rather than an opaque [Throwable].
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

/** Typed, localizable error taxonomy — mapped to string resources at the UI layer. */
sealed class AppError(val message: String? = null, val cause: Throwable? = null) {
    data object PermissionDenied : AppError()
    data object MicrophoneUnavailable : AppError()
    data object SpeechModelNotDownloaded : AppError()
    data object InsufficientStorage : AppError()
    data object EncryptionKeyUnavailable : AppError()
    data class Unknown(val throwable: Throwable) : AppError(throwable.message, throwable)
}
