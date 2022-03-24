package com.ezstudio.volumebooster.test.di

import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { MusicActiveViewModel(androidApplication()) }
}