package com.yourdomain.voicescribe.core.domain.di

import com.yourdomain.voicescribe.core.domain.usecase.AddBookmarkUseCase
import com.yourdomain.voicescribe.core.domain.usecase.DeleteRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.ExportRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.GetRecordingsUseCase
import com.yourdomain.voicescribe.core.domain.usecase.ObserveRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.PauseResumeRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.SearchRecordingsUseCase
import com.yourdomain.voicescribe.core.domain.usecase.StartRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.StopRecordingUseCase
import com.yourdomain.voicescribe.core.domain.usecase.ToggleFavoriteUseCase
import com.yourdomain.voicescribe.core.domain.usecase.TranscribeFileUseCase
import org.koin.dsl.module

/**
 * Wires every use case against the *ports* declared in core:domain. The
 * concrete adapters (Room repositories, AudioRecord/MediaRecorder, STT
 * sessions, the foreground service controller) are bound in core:data's
 * `dataModule` and core:audio's `audioModule`; this module never references
 * either, so core:domain stays dependency-free of Android.
 */
val useCaseModule = module {
    factory { StartRecordingUseCase(get(), get(), get(), get(), get()) }
    factory { StopRecordingUseCase(get(), get(), get()) }
    factory { PauseResumeRecordingUseCase(get(), get()) }
    factory { AddBookmarkUseCase(get()) }
    factory { GetRecordingsUseCase(get()) }
    factory { SearchRecordingsUseCase(get()) }
    factory { ExportRecordingUseCase(get(), get()) }
    factory { DeleteRecordingUseCase(get()) }
    factory { TranscribeFileUseCase(get(), get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { ObserveRecordingUseCase(get()) }
}
