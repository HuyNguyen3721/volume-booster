package com.ezstudio.volumebooster.test.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.databinding.LayoutFragmentWaveSoundBinding
import com.ezstudio.volumebooster.test.dialog.DialogEqualizer
import com.ezstudio.volumebooster.test.dialog.DialogSaveNameEqualizer
import com.ezstudio.volumebooster.test.item.ItemCustomEqualizer
import com.ezstudio.volumebooster.test.item.ItemEqualizer
import com.ezstudio.volumebooster.test.service.LoudnessService
import com.ezstudio.volumebooster.test.utils.EqualizerUtils
import com.ezstudio.volumebooster.test.utils.EqualizerUtils.getListEqualizer
import com.ezstudio.volumebooster.test.utils.KeyEqualizer
import com.ezstudio.volumebooster.test.utils.PremiumUtils
import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.baseproject.utils.KeyboardUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.EzAdControl
import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import com.orhanobut.hawk.Hawk
import kotlin.math.roundToInt


class FragmentEqualizer(var service: LoudnessService?) :
    BaseFragment<LayoutFragmentWaveSoundBinding>() {
    private var dialogEqualizer: DialogEqualizer? = null
    private var dialogSaveNameEqualizer: DialogSaveNameEqualizer? = null
    private var isEnableVolume: Boolean = false
    private var lowerEqualizerBandLevel: Short = 0
    private var upperEqualizerBandLevel: Short = 0
    private lateinit var equalizerSave: ItemEqualizer
    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(MusicActiveViewModel::class.java)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        // set up mEqualizer
//        initVirtualization()
        //
        equalizerSave = EqualizerUtils.getEqualizerSave(requireContext())
        binding.categoryNameEqualizer.text = equalizerSave.name
        binding.seekbar1.apply {
            isEnabled = false
            progress = equalizerSave.value60Hz
            progressDrawable = resources.getDrawable(R.drawable.seekbar_drawable_progress_disable)
        }
        binding.seekbar2.apply {
            isEnabled = false
            progress = equalizerSave.value230Hz
            progressDrawable = resources.getDrawable(R.drawable.seekbar_drawable_progress_disable)
        }
        binding.seekbar3.apply {
            isEnabled = false
            progress = equalizerSave.value910Hz
            progressDrawable = resources.getDrawable(R.drawable.seekbar_drawable_progress_disable)
        }
        binding.seekbar4.apply {
            isEnabled = false
            progress = equalizerSave.value3kHz
            progressDrawable = resources.getDrawable(R.drawable.seekbar_drawable_progress_disable)
        }
        binding.seekbar5.apply {
            isEnabled = false
            progress = equalizerSave.value14kHz
            progressDrawable = resources.getDrawable(R.drawable.seekbar_drawable_progress_disable)
        }
        // invalidate width height
        setBoundSeekbar(binding.seekbar1.progressDrawable.copyBounds())
        //
        binding.viewVolumeVirtualization.apply {
            isEnable = false
            color = Color.parseColor("#80949392")
            invalidate()
        }
        binding.viewVolumeBass.apply {
            isEnable = false
            color = Color.parseColor("#80949392")
            invalidate()
        }
        //
        binding.categoryNameEqualizer.setTextColor(Color.parseColor("#80949392"))
        binding.icCategory.setColorFilter(
            Color.parseColor("#80949392"),
            PorterDuff.Mode.SRC_IN
        )
        binding.icSave.setColorFilter(
            Color.parseColor("#80949392"),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun initData() {

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initListener() {
        // category
        binding.layoutCategory.setOnClickListener {
            if (!aVoidDoubleClick() && isEnableVolume) {
                dialogEqualizer = DialogEqualizer(requireActivity(), R.style.dialog)
                dialogEqualizer?.apply {
                    show()
                }
                dialogEqualizer?.onClickItem = { item ->
                    val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
                    // ads
                    EzAdControl.getInstance(requireActivity())
                        .setShowAdCallback(object : ShowAdCallback() {
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
                                val time =
                                    PreferencesUtils.getInteger(PremiumUtils.TIME_SHOW_ADS, 0)
                                if (time != 0 && time % 5 == 0) {
                                    PremiumUtils.showDialogPremium(requireActivity())
                                }
                                PreferencesUtils.putInteger(PremiumUtils.TIME_SHOW_ADS, time + 1)
                            }

                        }).showAds()

                    binding.categoryNameEqualizer.text = item.name
                    binding.seekbar1.progress = item.value60Hz
                    binding.seekbar2.progress = item.value230Hz
                    binding.seekbar3.progress = item.value910Hz
                    binding.seekbar4.progress = item.value3kHz
                    binding.seekbar5.progress = item.value14kHz
                }
                dialogEqualizer?.onDismiss = {
                    KeyboardUtils.hideSoftKeyboardToggleSoft(requireActivity())
                    EqualizerUtils.hideNavigation(requireActivity())
                }
                dialogEqualizer?.onFocus = {
                    KeyboardUtils.showSoftKeyboard(requireActivity())
                }
                dialogEqualizer?.setOnDismissListener {
                    binding.categoryNameEqualizer.text =
                        EqualizerUtils.getNameEqualizerSave(requireContext())
                }
                dialogEqualizer?.onEdited = {
                    // ads
                    val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
                    EzAdControl.getInstance(requireActivity())
                        .setShowAdCallback(object : ShowAdCallback() {
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
                                val time =
                                    PreferencesUtils.getInteger(PremiumUtils.TIME_SHOW_ADS, 0)
                                if (time != 0 && time % 5 == 0) {
                                    PremiumUtils.showDialogPremium(requireActivity())
                                }
                                PreferencesUtils.putInteger(PremiumUtils.TIME_SHOW_ADS, time + 1)
                            }
                        })
                        .showAds()
                }
                dialogEqualizer?.onDeleted = {
                    val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
                    // ads
                    EzAdControl.getInstance(requireActivity())
                        .setShowAdCallback(object : ShowAdCallback() {
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
                                val time =
                                    PreferencesUtils.getInteger(PremiumUtils.TIME_SHOW_ADS, 0)
                                if (time != 0 && time % 5 == 0) {
                                    PremiumUtils.showDialogPremium(requireActivity())
                                }
                                PreferencesUtils.putInteger(PremiumUtils.TIME_SHOW_ADS, time + 1)
                            }

                        }).showAds()
                }
            }

        }
        //seekb bar

        //1
        binding.seekbar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    service?.mEqualizer?.setBandLevel(
                        0,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                } catch (e: UnsupportedOperationException) {
                    toast(getString(R.string.unsupported_device))
                } catch (r: java.lang.RuntimeException) {
                    toast(getString(R.string.unsupported_device))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.isChangeSound.value = (true)
                viewModel.isCustom.value = (true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveCustomEqualizerNoName()
                viewModel.isChangeSound.value = (false)
            }

        })
        //2
        binding.seekbar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    service?.mEqualizer?.setBandLevel(
                        1,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                } catch (e: UnsupportedOperationException) {
                    toast(getString(R.string.unsupported_device))
                } catch (r: java.lang.RuntimeException) {
                    toast(getString(R.string.unsupported_device))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.isChangeSound.value = (true)
                viewModel.isCustom.value = (true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveCustomEqualizerNoName()
                viewModel.isChangeSound.value = (false)

            }

        })
        //3
        binding.seekbar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    service?.mEqualizer?.setBandLevel(
                        2,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                } catch (e: UnsupportedOperationException) {
                    toast(getString(R.string.unsupported_device))
                } catch (r: java.lang.RuntimeException) {
                    toast(getString(R.string.unsupported_device))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.isChangeSound.value = (true)
                viewModel.isCustom.value = (true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveCustomEqualizerNoName()
                viewModel.isChangeSound.value = (false)

            }

        })
        //4
        binding.seekbar4.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    service?.mEqualizer?.setBandLevel(
                        3,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                } catch (e: UnsupportedOperationException) {
                    toast(getString(R.string.unsupported_device))
                } catch (r: java.lang.RuntimeException) {
                    toast(getString(R.string.unsupported_device))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.isChangeSound.value = (true)
                viewModel.isCustom.value = (true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveCustomEqualizerNoName()
                viewModel.isChangeSound.value = (false)

            }

        })
        //5
        binding.seekbar5.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    service?.mEqualizer?.setBandLevel(
                        4,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                } catch (e: UnsupportedOperationException) {
                    toast(getString(R.string.unsupported_device))
                } catch (r: java.lang.RuntimeException) {
                    toast(getString(R.string.unsupported_device))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.isChangeSound.value = (true)
                viewModel.isCustom.value = (true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveCustomEqualizerNoName()
                viewModel.isChangeSound.value = (false)
            }

        })

        // view volume
        binding.viewVolumeBass.onTouch = {
            viewModel.isChangeSound.value = (it)
        }
        binding.viewVolumeVirtualization.onTouch = {
            viewModel.isChangeSound.value = (it)
        }
        // set name equalizer
        viewModel.isCustom.observe(requireActivity()) {
            binding.categoryNameEqualizer.text = getString(R.string.custom)
            PreferencesUtils.putString(KeyEqualizer.KEY_EQUALIZER_NAME, getString(R.string.custom))
        }
        // save
        binding.layoutSave.setOnClickListener {
            if (!aVoidDoubleClick() && isEnableVolume) {
                if (binding.categoryNameEqualizer.text == getString(R.string.custom)) {
                    KeyboardUtils.showSoftKeyboard(requireActivity())
                    dialogSaveNameEqualizer =
                        DialogSaveNameEqualizer(requireContext(), R.style.StyleDialog)
                    dialogSaveNameEqualizer?.show()
                    dialogSaveNameEqualizer?.listenerYes = {
                        if (!checkDuplicateName(it)) {
                            binding.categoryNameEqualizer.text = it
                            val listCustom = Hawk.get(
                                KeyEqualizer.KEY_EQUALIZER_CUSTOM,
                                mutableListOf<ItemCustomEqualizer>()
                            )
                            val v60 = binding.seekbar1.progress
                            val v230 = binding.seekbar2.progress
                            val v910 = binding.seekbar3.progress
                            val v3k = binding.seekbar4.progress
                            val v14k = binding.seekbar5.progress
                            val itemCustom =
                                ItemCustomEqualizer(it, mutableListOf(v60, v230, v910, v3k, v14k))
                            listCustom.add(itemCustom)
                            for (item in listCustom) {
                                if (item.name == getString(R.string.custom)) {
                                    listCustom.remove(item)
                                    break
                                }
                            }
                            Hawk.put(KeyEqualizer.KEY_EQUALIZER_CUSTOM, listCustom)
                            toast(getString(R.string.saved))
                            dialogSaveNameEqualizer?.dismiss()
                            // ads
                            val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
                            EzAdControl.getInstance(requireActivity())
                                .setShowAdCallback(object : ShowAdCallback() {
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
                                        val time = PreferencesUtils.getInteger(
                                            PremiumUtils.TIME_SHOW_ADS,
                                            0
                                        )
                                        if (time != 0 && time % 5 == 0) {
                                            PremiumUtils.showDialogPremium(requireActivity())
                                        }
                                        PreferencesUtils.putInteger(
                                            PremiumUtils.TIME_SHOW_ADS,
                                            time + 1
                                        )
                                    }
                                }).showAds()
                        } else {
                            toast(getString(R.string.exists_already))
                        }
                    }
                    dialogSaveNameEqualizer?.setOnDismissListener {
                        KeyboardUtils.hideSoftKeyboardToggleSoft(requireActivity())
                        EqualizerUtils.hideNavigation(requireActivity())
                    }
                } else {
                    toast(getString(R.string.can_not_save))
                }
            }
        }
        // tune off on
        binding.icOff.setOnClickListener {
            if (!aVoidDoubleClick()) {
                //ads
                val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
                EzAdControl.getInstance(requireActivity())
                    .setShowAdCallback(object : ShowAdCallback() {
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
                isEnableVolume = !isEnableVolume
                if (isEnableVolume) {
                    initEqualizer()
                    initBassBoost()
                    initVirtualization()
                } else {
                    service?.mEqualizer?.enabled = false
                    service?.bassBoost?.enabled = false
                    service?.mEqualizer?.release()
                    service?.bassBoost?.release()
                    service?.mEqualizer = null
                    service?.bassBoost = null
                }
                //
                binding.state.setImageResource(if (isEnableVolume) R.drawable.ic_state_on_equalizer else R.drawable.ic_state_off_equalizer)
                binding.apply {
                    seekbar1.isEnabled = isEnableVolume
                    seekbar2.isEnabled = isEnableVolume
                    seekbar3.isEnabled = isEnableVolume
                    seekbar4.isEnabled = isEnableVolume
                    seekbar5.isEnabled = isEnableVolume
                    //
                    seekbar1.progressDrawable =
                        resources.getDrawable(if (isEnableVolume) R.drawable.seekbar_drawable_progress_enable else R.drawable.seekbar_drawable_progress_disable)
                    seekbar2.progressDrawable =
                        resources.getDrawable(if (isEnableVolume) R.drawable.seekbar_drawable_progress_enable else R.drawable.seekbar_drawable_progress_disable)
                    seekbar3.progressDrawable =
                        resources.getDrawable(if (isEnableVolume) R.drawable.seekbar_drawable_progress_enable else R.drawable.seekbar_drawable_progress_disable)
                    seekbar4.progressDrawable =
                        resources.getDrawable(if (isEnableVolume) R.drawable.seekbar_drawable_progress_enable else R.drawable.seekbar_drawable_progress_disable)
                    seekbar5.progressDrawable =
                        resources.getDrawable(if (isEnableVolume) R.drawable.seekbar_drawable_progress_enable else R.drawable.seekbar_drawable_progress_disable)
                    // invalidate width height
                    setBoundSeekbar(binding.seekbar1.progressDrawable.copyBounds())
                }
                //
                binding.viewVolumeBass.apply {
                    isEnable = isEnableVolume
                    color = Color.parseColor(if (isEnableVolume) "#FF9500" else "#80949392")
                    invalidate()
                }
                binding.viewVolumeVirtualization.apply {
                    isEnable = isEnableVolume
                    color = Color.parseColor(if (isEnableVolume) "#FF9500" else "#80949392")
                    invalidate()
                }

                //
                binding.categoryNameEqualizer.setTextColor(Color.parseColor(if (isEnableVolume) "#FFFFFF" else "#80949392"))
                binding.icCategory.setColorFilter(
                    Color.parseColor(if (isEnableVolume) "#FFFFFF" else "#80949392"),
                    PorterDuff.Mode.SRC_IN
                )
                binding.icSave.setColorFilter(
                    Color.parseColor(if (isEnableVolume) "#FFFFFF" else "#80949392"),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }

        // bass booster
        binding.viewVolumeBass.onPercent = {
            try {
                service?.bassBoost?.enabled = true
                val percent = ((it / 240F))
                service?.bassBoost?.setStrength((percent * 1000).toInt().toShort())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Virtualization
        binding.viewVolumeVirtualization.onPercent = {
            try {
                service?.virtualization?.enabled = true
                val percent = ((it / 240F))
                service?.virtualization?.preset = ((percent * 6).toInt().toShort())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initEqualizer() {
        try {
            if (service?.mEqualizer == null) {
                service?.mEqualizer = Equalizer(
                    Int.MAX_VALUE,
                    /*     if (service?.mediaPlayer?.isPlaying == true
                         ) service?.mediaPlayer?.audioSessionId ?: 0 else*/ 0
                )
                service?.mEqualizer?.enabled = true
                for (i in 0 until 5) {
                    service?.mEqualizer!!.setBandLevel(
                        i.toShort(),
                        0
                    )
                }
            }
//         seekbar
            lowerEqualizerBandLevel = service?.mEqualizer?.bandLevelRange?.get(0) ?: 0
            upperEqualizerBandLevel = service?.mEqualizer?.bandLevelRange?.get(1) ?: 0
            val maxProcess = upperEqualizerBandLevel - lowerEqualizerBandLevel
            binding.apply {
                seekbar1.max = maxProcess
                seekbar2.max = maxProcess
                seekbar3.max = maxProcess
                seekbar4.max = maxProcess
                seekbar5.max = maxProcess
            }
            //
            service?.mEqualizer?.setBandLevel(
                0,
                (equalizerSave.value60Hz + lowerEqualizerBandLevel).toShort()
            )
            service?.mEqualizer?.setBandLevel(
                1,
                (equalizerSave.value230Hz + lowerEqualizerBandLevel).toShort()
            )
            service?.mEqualizer?.setBandLevel(
                2,
                (equalizerSave.value910Hz + lowerEqualizerBandLevel).toShort()
            )
            service?.mEqualizer?.setBandLevel(
                3,
                (equalizerSave.value3kHz + lowerEqualizerBandLevel).toShort()
            )
            service?.mEqualizer?.setBandLevel(
                4,
                (equalizerSave.value14kHz + lowerEqualizerBandLevel).toShort()
            )
        } catch (e: IllegalArgumentException) {
        } catch (e: IllegalStateException) {
        } catch (e: RuntimeException) {
        } catch (e: UnsupportedOperationException) {
            service?.mEqualizer?.enabled = false
            service?.mEqualizer?.release()
            service?.mEqualizer = null
        }

    }

    private fun initBassBoost() {
        try {
            if (service?.bassBoost == null) {
                service?.bassBoost = BassBoost(
                    Int.MAX_VALUE, /*if (service?.mediaPlayer?.isPlaying == true
                    ) service?.mediaPlayer?.audioSessionId ?: 0 else*/ 0
                )
                service?.bassBoost?.enabled = true
            }
            service?.bassBoost?.roundedStrength?.let {
                if (it >= 500) {
                    binding.viewVolumeBass.rotate = (it / 1000F) * 240 - 120
                } else {
                    binding.viewVolumeBass.rotate = (it / 1000F) * 240 + 240
                }
            }
            binding.viewVolumeBass.invalidate()
        } catch (e: IllegalArgumentException) {
        } catch (e: IllegalStateException) {
        } catch (e: RuntimeException) {
        } catch (e: UnsupportedOperationException) {
            service?.bassBoost?.enabled = false
            service?.bassBoost?.release()
            service?.bassBoost = null
        }
    }

    private fun initVirtualization() {
        try {
            if (service?.virtualization == null) {
                service?.virtualization = PresetReverb(
                    Int.MAX_VALUE, /*if (service?.mediaPlayer?.isPlaying == true
                    ) service?.mediaPlayer?.audioSessionId ?: 0 else*/ 0
                )
                service?.virtualization?.enabled = true
            }
            service?.virtualization?.preset?.let {
                if (it >= 3) {
                    binding.viewVolumeVirtualization.rotate = (it / 6F) * 240 - 120
                } else {
                    binding.viewVolumeVirtualization.rotate = (it / 6F) * 240 + 240
                }
            }
            binding.viewVolumeVirtualization.invalidate()
        } catch (e: IllegalArgumentException) {
        } catch (e: IllegalStateException) {
        } catch (e: RuntimeException) {
        } catch (e: UnsupportedOperationException) {
            service?.virtualization?.enabled = false
            service?.virtualization?.release()
            service?.virtualization = null
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentWaveSoundBinding {
        return LayoutFragmentWaveSoundBinding.inflate(layoutInflater, container, false)
    }

    private fun checkDuplicateName(name: String): Boolean {
        for (item in getListEqualizer(requireContext())) {
            if (item.name == name) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        service?.releaseEqualizer()
    }

    private fun saveCustomEqualizerNoName() {
        val v60 = binding.seekbar1.progress
        val v230 = binding.seekbar2.progress
        val v910 = binding.seekbar3.progress
        val v3k = binding.seekbar4.progress
        val v14k = binding.seekbar5.progress
        val itemCustom =
            ItemCustomEqualizer(
                getString(R.string.custom),
                mutableListOf(v60, v230, v910, v3k, v14k)
            )
        //add
        val listCustom =
            Hawk.get(KeyEqualizer.KEY_EQUALIZER_CUSTOM, mutableListOf<ItemCustomEqualizer>())

        for (item in listCustom) {
            if (item.name == itemCustom.name) {
                item.listValues.clear()
                item.listValues.addAll(itemCustom.listValues)
                Hawk.put(KeyEqualizer.KEY_EQUALIZER_CUSTOM, listCustom)
                return
            }
        }
        //
        listCustom.add(itemCustom)
        Hawk.put(KeyEqualizer.KEY_EQUALIZER_CUSTOM, listCustom)

    }

    private fun setBoundSeekbar(bounds: Rect) {
        // converting 15 dp to pixels
        binding.apply {
            val desiredHeight = resources.getDimensionPixelOffset(R.dimen._15sdp)
            val actualTop =
                ((bounds.bottom - bounds.top) / 2.0 - desiredHeight / 2.0).roundToInt().toInt()
            seekbar1.progressDrawable.setBounds(
                bounds.left,
                actualTop,
                bounds.right,
                actualTop + desiredHeight
            )
            seekbar2.progressDrawable.setBounds(
                bounds.left,
                actualTop,
                bounds.right,
                actualTop + desiredHeight
            )
            seekbar3.progressDrawable.setBounds(
                bounds.left,
                actualTop,
                bounds.right,
                actualTop + desiredHeight
            )
            seekbar4.progressDrawable.setBounds(
                bounds.left,
                actualTop,
                bounds.right,
                actualTop + desiredHeight
            )
            seekbar5.progressDrawable.setBounds(
                bounds.left,
                actualTop,
                bounds.right,
                actualTop + desiredHeight
            )
        }
    }
}