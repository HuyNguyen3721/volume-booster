package com.ezstudio.volumebooster.test.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.adapter.AdapterSetVolume
import com.ezstudio.volumebooster.test.broadcast.SettingsContentObserver
import com.ezstudio.volumebooster.test.databinding.LayoutFragmentSoundBinding
import com.ezstudio.volumebooster.test.item.ItemChangeVolume
import com.ezstudio.volumebooster.test.service.LoudnessService
import com.ezstudio.volumebooster.test.utils.PremiumUtils
import com.ezstudio.volumebooster.test.utils.RecycleViewUtils
import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.EzAdControl
import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import org.koin.android.ext.android.inject


class FragmentSound(var service: LoudnessService?) : BaseFragment<LayoutFragmentSoundBinding>() {
    private val listVolume = mutableListOf<ItemChangeVolume>()
    private var adapterChangeVolume: AdapterSetVolume? = null
    private var maxVolume = 0
    private lateinit var audioManager: AudioManager
    private var settingsContentObserver: SettingsContentObserver? = null
    private val viewModel by inject<MusicActiveViewModel>()
    private var visualizer: Visualizer? = null
    private var percent = 0

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        binding.seekBar.thumb =
            resources.getDrawable(if (Build.VERSION.SDK_INT >= 24) R.drawable.ic_seekbar_volume else R.drawable.ic_seekbar_volume_2)
        //
        adapterChangeVolume = AdapterSetVolume(requireContext(), listVolume)
        binding.rclSetVolume.adapter = adapterChangeVolume
        // clear anim rcl
        RecycleViewUtils.clearAnimation(binding.rclSetVolume)
        // create view round max min
        initLoudnessEnhancer()
        service?.laugh?.enabled = true
        service?.laugh?.targetGain?.let {
            viewModel.percentValue.value = ((it / 8000F) * 100).toInt()
            if (it >= 4000) {
                binding.viewVolume.rotate = (it / 8000F) * 240 - 120
            } else {
                binding.viewVolume.rotate = (it / 8000F) * 240 + 240
            }
            service?.createNotification()
        }
        audioManager = (requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        //registerSettingChange
        registerSettingChange()
        //init visualizer
        initVisualizer()
    }

    private fun initVisualizer() {
        (requireActivity() as BaseActivity<*>).requestPermission(complete = {
            if (it) {
                try {
                    visualizer ?: let {
                        visualizer = Visualizer(0)
                        visualizer?.enabled = false
                        visualizer?.captureSize = Visualizer.getCaptureSizeRange()[1]
                        visualizer?.setDataCaptureListener(object :
                            Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(
                                visualizer: Visualizer?,
                                waveform: ByteArray?,
                                samplingRate: Int
                            ) {
                            }

                            override fun onFftDataCapture(
                                visualizer: Visualizer?,
                                fft: ByteArray?,
                                samplingRate: Int
                            ) {
                                binding.wave1.apply {
                                    value = getValueWavesView(fft)
                                    invalidate()
                                }
                                binding.wave2.apply {
                                    value = getValueWavesView(fft)
                                    invalidate()
                                }
                            }

                        }, Visualizer.getMaxCaptureRate(), false, true)
                    }
                } catch (e: Exception) {
                }
                visualizer?.enabled = true
            }
            //
//            (requireActivity() as BaseActivity<*>).requestPermission(
//                complete = {
//                    if (it) {
//                        viewModel.getDataMusicOffline(requireContext())
//                    } else {
//                        toast(getString(R.string.please_allow_permission))
//                    }
//                },
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            )
        }, Manifest.permission.RECORD_AUDIO)
    }


    override fun initData() {
        binding.seekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.seekBar.max = maxVolume
        //
        listVolume.clear()
        listVolume.apply {
            add(ItemChangeVolume(getString(R.string.mute), 0, binding.seekBar.progress == 0))
            add(
                ItemChangeVolume(
                    getString(R.string.v_30_percent),
                    (maxVolume * 0.3F).toInt(),
                    binding.seekBar.progress == (maxVolume * 0.3).toInt()
                )
            )
            add(
                ItemChangeVolume(
                    getString(R.string.v_60_percent),
                    (maxVolume * 0.6F).toInt(),
                    binding.seekBar.progress == (maxVolume * 0.6).toInt()
                )
            )
            add(ItemChangeVolume(getString(R.string.v_100_percent), maxVolume, false))
            add(ItemChangeVolume(getString(R.string.v_125_percent), (8000 * 0.25F).toInt(), false))
            add(ItemChangeVolume(getString(R.string.v_150_percent), (8000 * 0.5F).toInt(), false))
            add(ItemChangeVolume(getString(R.string.v_175_percent), (8000 * 0.75F).toInt(), false))
            add(ItemChangeVolume(getString(R.string.max_bosster), 8000, false))
        }
    }

    private fun initLoudnessEnhancer() {
        try {
            if (service?.laugh == null) {
                service?.laugh?.release()
                service?.laugh = null
                service?.laugh = LoudnessEnhancer(
                    /*    if (service?.mediaPlayer?.isPlaying == true
                        ) service?.mediaPlayer?.audioSessionId ?: 0 else*/ 0
                )
                service?.laugh?.enabled = false
            }

        } catch (e: Exception) {

        }
    }

    override fun initListener() {
        //seekbar
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val indexVolume = seekBar.progress
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    indexVolume,
                    AudioManager.FLAG_PLAY_SOUND
                )
                if (indexVolume == maxVolume) {
                    if (percent == 0) {
                        service?.createNotification(100)
                    }
                    changeRclFromPercent(percent)
                } else {
                    //change rcl
                    var isChecked = false
                    for (item in listVolume) {
                        if (item.indexVolume == indexVolume) {
                            isChecked = true
                            item.isSelected = true
                            adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                            updateNotify(item.name)
                        } else {
                            if (item.isSelected) {
                                item.isSelected = false
                                adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                            }
                        }
                    }
                    if (!isChecked) {
                        service?.createNotification()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        //adapter
        adapterChangeVolume?.onClickListener = {
            val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
            // ads
            EzAdControl.getInstance(requireActivity()).setShowAdCallback(object : ShowAdCallback() {
                override fun onDisplay() {

                }

                override fun onDisplayFaild() {

                }

                override fun onClosed() {
                    if ((service?.mediaPlayer?.isPlaying == true) != isMusicPlaying) {
                        if (isMusicPlaying) {
                            service?.onPlayMusic(null)
                        } else {
                            service?.onPauseMusic()
                        }
                    }
                    // show dialog premium
                    val time = PreferencesUtils.getInteger(PremiumUtils.TIME_SHOW_ADS, 0)
                    if (time != 0 && time % 5 == 0) {
                        PremiumUtils.showDialogPremium(requireActivity())
                    }
                    PreferencesUtils.putInteger(PremiumUtils.TIME_SHOW_ADS, time + 1)
                }
            }).showAds()
            //
            val data = listVolume[it]
            // setTargetGain volume
            if (data.indexVolume > maxVolume) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    AudioManager.FLAG_PLAY_SOUND
                )
                //loudnessEnhancer
                initLoudnessEnhancer()
                if (service?.laugh != null) {
                    try {
                        service?.laugh?.setTargetGain(data.indexVolume)
                        service?.laugh?.enabled = true
                    } catch (e: UnsupportedOperationException) {

                    }
                }
                // change seekbar
                binding.seekBar.progress = maxVolume
            } else {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    data.indexVolume,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
            // notify adapter
            val state = data.isSelected
            for (item in listVolume) {
                if (item == data && !state) {
                    item.isSelected = !state
                    adapterChangeVolume?.notifyItemChanged(it)
                } else {
                    if (!state && item.isSelected) {
                        item.isSelected = state
                        adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                    }
                }
            }
            // change seeker
            when (data.name) {
                getString(R.string.v_30_percent) -> {
                    binding.viewVolume.rotate = 240F
                    binding.viewVolume.invalidate()
                    binding.seekBar.progress = (maxVolume * 0.3).toInt()
                }
                getString(R.string.v_60_percent) -> {
                    binding.viewVolume.rotate = 240F
                    binding.viewVolume.invalidate()
                    binding.seekBar.progress = (maxVolume * 0.6).toInt()
                }
                getString(R.string.v_100_percent) -> {
                    binding.viewVolume.rotate = 240F
                    binding.viewVolume.invalidate()
                    binding.seekBar.progress = maxVolume
                }
                getString(R.string.mute) -> {
                    binding.seekBar.progress = 0
                }
                getString(R.string.v_125_percent) -> {
                    binding.viewVolume.rotate = 300F
                    binding.viewVolume.invalidate()
                }
                getString(R.string.v_150_percent) -> {
                    binding.viewVolume.rotate = 360F
                    binding.viewVolume.invalidate()
                }
                getString(R.string.v_175_percent) -> {
                    binding.viewVolume.rotate = 60F
                    binding.viewVolume.invalidate()
                }
                getString(R.string.max_bosster) -> {
                    binding.viewVolume.rotate = 120F
                    binding.viewVolume.invalidate()
                }
            }
        }
        //  touch volume
        binding.viewVolume.onTouch = {
            viewModel.isChangeSound.value = (it)
        }
        // rotate volume
        binding.viewVolume.onPercent = {
            percent = ((it / 240F) * 100F).toInt()
            viewModel.percentValue.value = percent
            //
            initLoudnessEnhancer()
            if (service?.laugh != null) {
                try {
                    service?.laugh?.setTargetGain(((percent * 8000) / 100F).toInt())
                    service?.laugh?.enabled = true
                } catch (e: Exception) {
                    service?.laugh?.release()
                    service?.laugh = null
                }
            }
            //
            if (binding.seekBar.progress == maxVolume) {
                changeRclFromPercent(percent)
            } else {
                if (percent == 0) {
                    when (binding.seekBar.progress) {
                        (maxVolume * 0.3).toInt() -> {
                            changeVolumeRcl(getString(R.string.v_30_percent))
                        }
                        (maxVolume * 0.6).toInt() -> {
                            changeVolumeRcl(getString(R.string.v_60_percent))
                        }
                        0 -> {
                            changeVolumeRcl(getString(R.string.mute))
                        }
                    }
                } else {
                    changeVolumeRcl(null)
                }
            }
            //
            service?.createNotification()
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentSoundBinding {
        return LayoutFragmentSoundBinding.inflate(inflater, container, false)
    }

    private fun registerSettingChange() {
        settingsContentObserver = SettingsContentObserver(requireContext(), Handler())
        settingsContentObserver?.onChangeSettings = {
            binding.seekBar.progress = it
        }
        settingsContentObserver?.let {
            requireContext().contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI, true,
                it
            )
        }
    }

    private fun changeVolumeRcl(percentName: String?) {
        for (item in listVolume) {
            when (percentName) {
                item.name -> {
                    item.isSelected = true
                    adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                }
                "" -> {
                    if (item.name == getString(R.string.v_100_percent)) {
                        if (!item.isSelected) {
                            item.isSelected = true
                            adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                        }
                    } else {
                        if (item.isSelected) {
                            item.isSelected = false
                            adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                        }
                    }
                }
                null -> {
                    if (item.isSelected) {
                        item.isSelected = false
                        adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                    }
                }
                else -> {
                    if (item.isSelected) {
                        item.isSelected = false
                        adapterChangeVolume?.notifyItemChanged(listVolume.indexOf(item))
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
    }

    override fun onResume() {
        super.onResume()
        initVisualizer()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsContentObserver?.let {
            requireContext().contentResolver.unregisterContentObserver(it)
        }
        //
        if (visualizer != null) {
            Log.d("Huy", "onDestroy: ")
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
        }

        if (service?.laugh != null) {
            service?.laugh?.enabled = false
            service?.laugh?.release()
            service?.laugh = null
        }
    }

    private fun changeRclFromPercent(percent: Int) {
        when (percent) {
            25 -> {
                changeVolumeRcl(getString(R.string.v_125_percent))
            }
            50 -> {
                changeVolumeRcl(getString(R.string.v_150_percent))
            }
            75 -> {
                changeVolumeRcl(getString(R.string.v_175_percent))
            }
            100 -> {
                changeVolumeRcl(getString(R.string.max_bosster))
            }
            0 -> {
                changeVolumeRcl("")
            }
            else -> {
                changeVolumeRcl(null)
            }
        }
    }

    private fun getValueWavesView(fft: ByteArray?): Int {
        val wavesValue = (fft?.get(0) ?: 0).toInt()
        val getVolume =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return when (getVolume / maxVolume.toFloat()) {
            in 0.0..0.3 -> {
                (wavesValue * 0.3).toInt()
            }
            in 0.3000001..0.6 -> {
                (wavesValue * 0.6).toInt()
            }
            in 0.6000001..0.8 -> {
                (wavesValue * 0.8).toInt()
            }
            else -> {
                wavesValue
            }
        }
    }

    private fun updateNotify(name: String) {
        when (name) {
            activity?.getString(R.string.mute) -> {
                service?.createNotification(0)
            }
            activity?.getString(R.string.v_30_percent) -> {
                service?.createNotification(30)
            }
            activity?.getString(R.string.v_60_percent) -> {
                service?.createNotification(60)
            }
            activity?.getString(R.string.v_100_percent) -> {
                service?.createNotification(100)
            }
        }
    }
}