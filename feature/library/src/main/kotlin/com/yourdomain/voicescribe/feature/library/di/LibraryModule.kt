package com.yourdomain.voicescribe.feature.library.di

import com.yourdomain.voicescribe.core.domain.port.ExportWriter
import com.yourdomain.voicescribe.feature.library.LibraryViewModel
import com.yourdomain.voicescribe.feature.library.export.DocxExporter
import com.yourdomain.voicescribe.feature.library.export.ExportManager
import com.yourdomain.voicescribe.feature.library.export.JsonExporter
import com.yourdomain.voicescribe.feature.library.export.PdfExporter
import com.yourdomain.voicescribe.feature.library.export.SrtVttExporter
import com.yourdomain.voicescribe.feature.library.export.TxtExporter
import com.yourdomain.voicescribe.feature.library.export.ZipExporter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val libraryFeatureModule = module {
    single { TxtExporter() }
    single { SrtVttExporter() }
    single { JsonExporter() }
    single { DocxExporter() }
    single { PdfExporter() }
    single { ZipExporter(get(), get(), get()) }
    single<ExportWriter> { ExportManager(androidContext(), get(), get(), get(), get(), get(), get()) }

    viewModel { LibraryViewModel(get(), get(), get(), get(), get()) }
}
