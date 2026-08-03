package com.yourdomain.voicescribe.core.audio.di

import com.yourdomain.voicescribe.core.audio.AudioRecordManager
import com.yourdomain.voicescribe.core.audio.MediaRecorderWrapper
import com.yourdomain.voicescribe.core.audio.service.ForegroundServiceControllerImpl
import com.yourdomain.voicescribe.core.audio.stt.SpeechRecognizerFactory
import com.yourdomain.voicescribe.core.audio.vad.EnergyBasedVadProcessor
import com.yourdomain.voicescribe.core.audio.vad.SileroVadProcessor
import com.yourdomain.voicescribe.core.domain.port.AudioCaptureController
import com.yourdomain.voicescribe.core.domain.port.FileRecorderController
import com.yourdomain.voicescribe.core.domain.port.ForegroundServiceController
import com.yourdomain.voicescribe.core.domain.port.SpeechRecognizerProvider
import com.yourdomain.voicescribe.core.domain.port.VadProcessor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val VAD_ENERGY = named("vad_energy")
val VAD_SILERO = named("vad_silero")

val audioModule = module {
    single<AudioCaptureController> { AudioRecordManager(androidContext()) }
    single<FileRecorderController> { MediaRecorderWrapper(androidContext()) }

    single<VadProcessor>(VAD_ENERGY) { EnergyBasedVadProcessor() }
    single<VadProcessor>(VAD_SILERO) { SileroVadProcessor(androidContext(), fallback = get(VAD_ENERGY)) }
    // Default binding used by StartRecordingUseCase; Settings can request a
    // named qualifier directly if it needs to let the user pick explicitly.
    single<VadProcessor> { get(VAD_ENERGY) }

    single<ForegroundServiceController> { ForegroundServiceControllerImpl(androidContext()) }
    single<SpeechRecognizerProvider> { SpeechRecognizerFactory(androidContext()) }
}
