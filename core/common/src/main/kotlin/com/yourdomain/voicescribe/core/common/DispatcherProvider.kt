package com.yourdomain.voicescribe.core.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Testable indirection over [kotlinx.coroutines.Dispatchers]. Production code
 * injects [DefaultDispatcherProvider]; tests inject a provider backed by
 * `kotlinx-coroutines-test`'s `StandardTestDispatcher` for every field.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}
