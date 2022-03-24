package com.ezstudio.volumebooster.test.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.adapter.AdapterSettings
import com.ezstudio.volumebooster.test.databinding.LayoutFragmentMainBinding
import com.ezstudio.volumebooster.test.dialog.DialogPremium
import com.ezstudio.volumebooster.test.item.ItemSetting
import com.ezstudio.volumebooster.test.service.LoudnessService
import com.ezstudio.volumebooster.test.utils.EqualizerUtils
import com.ezstudio.volumebooster.test.utils.KeyEqualizer
import com.ezstudio.volumebooster.test.utils.VibrationUtil
import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.adapter.BasePagerAdapter
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.baseproject.utils.ExtensionUtil.shareAppUrl
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.IAPUtils
import com.google.android.gms.ads.ez.observer.MySubject
import com.google.android.material.tabs.TabLayout
import org.koin.android.ext.android.inject


class FragmentMain(var audioManager: AudioManager?, var service: LoudnessService?) :
    BaseFragment<LayoutFragmentMainBinding>() {

    private val viewModel by inject<MusicActiveViewModel>()
    private var adapterSettings: AdapterSettings? = null
    private var listSettings = mutableListOf<ItemSetting>()
    private val fragmentSound = FragmentSound(service)
    private var dialogPremium: DialogPremium? = null
    override fun initView() {
        //
        viewModel.isLoading
        fragmentManager?.let {
            val adapter = BasePagerAdapter(it, 0).apply {
                addFragment(fragmentSound, "")
                addFragment(FragmentEqualizer(service), "")
                addFragment(FragmentMusic(service), "")
            }
            binding.viewPager.offscreenPageLimit = 3
            binding.viewPager.adapter = adapter
            binding.viewPager.setCurrentItem(0, true)
            binding.tab.setupWithViewPager(binding.viewPager)
            setupTabIcons()
            binding.tab.getTabAt(0)?.icon?.setColorFilter(
                Color.parseColor("#FF9500"),
                PorterDuff.Mode.SRC_IN
            )
        }
        //
        audioManager?.let {
            binding.icPlay.setImageResource(if (service?.mediaPlayer?.isPlaying == true) R.drawable.ic_pause_music else R.drawable.ic_play_music)
        }
        //run text
        binding.nameMusic.isSelected = true
        binding.nameAuthor.isSelected = true
        // settings
        adapterSettings = AdapterSettings(requireContext(), listSettings)
        binding.rclSettings.adapter = adapterSettings
        //
        binding.switchVibrator.isChecked =
            PreferencesUtils.getBoolean(VibrationUtil.KEY_VIBRATION, true)
        // check view Premium
        binding.layoutPremium.isVisible = !IAPUtils.getInstance().isPremium
    }

    override fun initData() {
        viewModel.isMusicActive.observe(this) {
            setUpViewPlayOrPause(it)
        }
        viewModel.nameMusicSong.observe(this) {
            binding.nameMusic.text = it
        }
        viewModel.nameAuthorSong.observe(this) {
            binding.nameAuthor.text = it
        }
        viewModel.imgSong.observe(this) {
            if (it != null) {
                binding.layoutImageMusic.visibility = View.VISIBLE
                Glide.with(requireContext()).load(it).into(binding.imgMusic)
            } else {
                binding.layoutImageMusic.visibility = View.VISIBLE
                Glide.with(requireContext()).load(R.drawable.ic_image_song).into(binding.imgMusic)
            }
        }
        // list settings
        listSettings.clear()
        listSettings.apply {
            add(ItemSetting(R.drawable.ic_share, getString(R.string.share)))
            add(ItemSetting(R.drawable.ic_more_app, getString(R.string.more_our_apps)))
            add(ItemSetting(R.drawable.ic_rate, getString(R.string.rate_us)))
        }
        if (!IAPUtils.getInstance().isPremium) {
            listSettings.add(
                ItemSetting(R.drawable.ic_premium_settings, getString(R.string.premium))
            )
        }
    }

    @SuppressLint("WrongConstant")
    override fun initListener() {
        binding.icPlay.setOnClickListener {
            service?.let {
                if (it.mediaPlayer == null) {
                    val path = PreferencesUtils.getString(KeyEqualizer.KEY_PATH_MUSIC, null)
                    it.onPlayMusic(path)
                } else {
                    if (it.mediaPlayer?.isPlaying == true) {
                        it.onPauseMusic()
                    } else {
                        it.onPlayMusic(null)
                    }
                }
            }
        }
        //
        binding.icNext.setOnClickListener {
            viewModel.actionNext.value = (true)
        }
        //
        binding.icBack.setOnClickListener {
            viewModel.actionBack.value = (true)
        }
        //
        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(Color.parseColor("#FF9500"), PorterDuff.Mode.SRC_IN);
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(Color.parseColor("#949392"), PorterDuff.Mode.SRC_IN);
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(Color.parseColor("#FF9500"), PorterDuff.Mode.SRC_IN);
            }
        })
        //
//        fragmentSound.touchVolume = {
//            binding.viewPager.isPagingEnabled = !it
//        }
        viewModel.isChangeSound.observe(requireActivity()) {
            binding.viewPager.isPagingEnabled = !it
        }
        // drawer
        binding.layoutMenu.setOnClickListener {
            binding.layoutDrawer.openDrawer(Gravity.START)
        }
        //
        adapterSettings?.onClickItem = {
            when (listSettings[it].name) {
                getString(R.string.vibration_setting) -> {
                    binding.layoutDrawer.closeDrawer(Gravity.START)
                }
                getString(R.string.share) -> {
                    binding.layoutDrawer.closeDrawer(Gravity.START)
                    shareAppUrl(requireContext(), "com.ezstudio.volumebooster")
                }
                getString(R.string.more_our_apps) -> {
                    binding.layoutDrawer.closeDrawer(Gravity.START)
                    openMoreApp()
                }
                getString(R.string.rate_us) -> {
                    binding.layoutDrawer.closeDrawer(Gravity.START)
                    rateUs()
                }
                getString(R.string.premium) -> {
                    binding.layoutDrawer.closeDrawer(Gravity.START)
                    showDialogPremium()
                }
            }
        }
        //
        binding.switchVibrator.setOnCheckedChangeListener { buttonView, isChecked ->
            PreferencesUtils.putBoolean(VibrationUtil.KEY_VIBRATION, isChecked)
        }
        //drawer
        binding.drawer.setOnClickListener {
        }
        //
        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { tab ->
                    EqualizerUtils.hideNavigation(requireActivity())
                    if (tab.position == 2) {
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            //
                            (requireActivity() as BaseActivity<*>).requestPermission(
                                {
                                    if (it) {
                                        viewModel.getDataMusicOffline(requireContext())
                                    } else {
                                        toast(getString(R.string.please_allow_permission))
                                    }
                                },
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        }
//
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        //
        binding.layoutPremium.setOnClickListener {
            showDialogPremium()
        }
        // check premium
        MySubject.getInstance().attach {
            if (IAPUtils.getInstance().isPremium) {
                listSettings.removeAt(3)
                adapterSettings?.notifyItemRemoved(3)
                binding.layoutPremium.isVisible = false
            }
        }
    }

    private fun showDialogPremium() {
        dialogPremium = DialogPremium(requireContext(), R.style.StyleDialogPremium)
        dialogPremium?.onClickMonth = {
            IAPUtils.getInstance()
                .callSubcriptions(requireActivity(), IAPUtils.KEY_PREMIUM_ONE_MONTH)
        }
        dialogPremium?.onClickSixMonth = {
            IAPUtils.getInstance()
                .callSubcriptions(requireActivity(), IAPUtils.KEY_PREMIUM_SIX_MONTHS)
        }
        dialogPremium?.onClickYear = {
            IAPUtils.getInstance()
                .callSubcriptions(requireActivity(), IAPUtils.KEY_PREMIUM_ONE_YEAR)
        }
        dialogPremium?.show()
    }

    private fun rateUs() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse("market://details?id=com.ezstudio.volumebooster")
        startActivity(intent)
    }

    private fun openMoreApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse("https://play.google.com/store/apps/developer?id=EZ+Mobi")
        startActivity(intent)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentMainBinding {
        return LayoutFragmentMainBinding.inflate(inflater, container, false)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupTabIcons() {
        binding.tab.getTabAt(0)?.icon = requireContext().getDrawable(R.drawable.ic_plate_selected)
        binding.tab.getTabAt(1)?.icon = requireContext().getDrawable(R.drawable.ic_wave_sound)
        binding.tab.getTabAt(2)?.icon = requireContext().getDrawable(R.drawable.ic_music)
    }

    private fun setUpViewPlayOrPause(isMusicActive: Boolean) {
        if (isMusicActive) {
            binding.icPlay.setImageResource(R.drawable.ic_pause_music)
        } else {
            binding.icPlay.setImageResource(R.drawable.ic_play_music)
        }
    }
}