package com.ezstudio.volumebooster.test.activity

import android.Manifest
import android.content.*
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.databinding.ActivityMainBinding
import com.ezstudio.volumebooster.test.fragment.FragmentMain
import com.ezstudio.volumebooster.test.service.LoudnessService
import com.ezstudio.volumebooster.test.utils.EqualizerUtils
import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.dialog.rate.DialogRating
import com.ezteam.baseproject.dialog.rate.DialogRatingState
import com.ezteam.baseproject.extensions.getHeightStatusBar



class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var audioManager: AudioManager? = null
    private var conection: ServiceConnection? = null
    var service: LoudnessService? = null
    private val viewModel by lazy {
        ViewModelProvider(this).get(MusicActiveViewModel::class.java)
    }

    override fun initView() {
        binding.layout.setPadding(0, getHeightStatusBar(), 0, 0)
        // full screen
        setAppActivityFullScreenOver(this)
//
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // request persision recoder audio
        requestPermission({
            if (it) {
                connection()
                if (!LoudnessService.isRunning) {
                    openServiceUnBound()
                }
            } else {
                finishAffinity()
            }
        }, Manifest.permission.RECORD_AUDIO)
//
    }

    override fun initData() {
    }

    override fun initListener() {

    }

    override fun viewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    private fun fragmentMain(audioManager: AudioManager?, service: LoudnessService?) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = FragmentMain(audioManager, service)
        transaction.add(R.id.layout_fragment, fragment, "MAIN")
        transaction.commitAllowingStateLoss()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }

    override fun onResume() {
        EqualizerUtils.hideNavigation(this)
        super.onResume()
    }


    private fun connection() {
        val intent = Intent(this, LoudnessService::class.java)
        conection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
                val binder = serviceBinder as LoudnessService.MyBinder
                service = binder.service
                fragmentMain(audioManager, service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }// gửi yêu càu kết nối
        this.bindService(intent, conection!!, Context.BIND_AUTO_CREATE)
    }

    private fun openServiceUnBound() {
        val intent = Intent()
        intent.setClass(this, LoudnessService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
    }

    override fun onBackPressed() {
        showAppRating(true) {
            finishAffinity()
        }
    }

    private fun showAppRating(isHardShow: Boolean, complete: (Boolean) -> Unit) {
        DialogRating.ExtendBuilder(this)
            .setHardShow(isHardShow)
            .setListener { status ->
                when (status) {
                    DialogRatingState.RATE_BAD -> {
                        toast(resources.getString(R.string.thank_for_rate))
                        complete(false)
                    }
                    DialogRatingState.RATE_GOOD -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data =
                            Uri.parse("market://details?id=$packageName")
                        startActivity(intent)
                        complete(true)
                    }
                    DialogRatingState.COUNT_TIME -> complete(false)
                }
            }
            .build()
            .show()
    }


}