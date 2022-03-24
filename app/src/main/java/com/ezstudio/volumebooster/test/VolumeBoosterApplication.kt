package com.ezstudio.volumebooster.test

import com.ezstudio.volumebooster.test.di.appModule
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.EzApplication
import com.orhanobut.hawk.Hawk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VolumeBoosterApplication : EzApplication() {
    override fun onCreate() {
        super.onCreate()
        PreferencesUtils.init(this)
        Hawk.init(this).build()
        setupKoin()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@VolumeBoosterApplication)
            modules(
                appModule
            )
        }
    }
}