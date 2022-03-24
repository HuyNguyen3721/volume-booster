package com.ezstudio.volumebooster.test.activity

import android.view.LayoutInflater
import com.ezstudio.volumebooster.test.databinding.ActivitySplashBinding
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.extensions.launchActivity
import com.google.android.gms.ads.ez.AdFactoryListener
import com.google.android.gms.ads.ez.EzAdControl
import com.google.android.gms.ads.ez.LogUtils
import com.google.android.gms.ads.ez.admob.AdmobOpenAdUtils

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun initView() {
        // full screen
        setAppActivityFullScreenOver(this)
        //
        loadOpenAds()
    }

    private fun loadOpenAds() {
        AdmobOpenAdUtils.getInstance(this).setAdListener(object : AdFactoryListener() {
            override fun onError() {
                LogUtils.logString(SplashActivity::class.java, "onError")
                openMain()
            }

            override fun onLoaded() {
                LogUtils.logString(SplashActivity::class.java, "onLoaded")
                // show ads ngay khi loaded
                AdmobOpenAdUtils.getInstance(this@SplashActivity).showAdIfAvailable(false)
            }

            override fun onDisplay() {
                super.onDisplay()
                LogUtils.logString(SplashActivity::class.java, "onDisplay")
            }

            override fun onDisplayFaild() {
                super.onDisplayFaild()
                LogUtils.logString(SplashActivity::class.java, "onDisplayFaild")
                openMain()

            }

            override fun onClosed() {
                super.onClosed()
                // tam thoi bo viec load lai ads thi dismis
                LogUtils.logString(SplashActivity::class.java, "onClosed")
                openMain()
            }
        }).loadAd()
    }

    private fun openMain() {
        launchActivity<MainActivity> { }
        finish()
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun viewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

}