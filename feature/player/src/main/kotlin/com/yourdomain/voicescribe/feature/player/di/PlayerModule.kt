package com.yourdomain.voicescribe.feature.player.di

import androidx.media3.exoplayer.ExoPlayer
import com.yourdomain.voicescribe.feature.player.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playerFeatureModule = module {
    // A fresh ExoPlayer per PlayerViewModel instance (which is itself scoped
    // to the screen's ViewModelStoreOwner by Koin's `viewModel {}` DSL).
    viewModel { PlayerViewModel(get(), get(), ExoPlayer.Builder(androidContext()).build()) }
}
