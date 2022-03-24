package com.ezstudio.volumebooster.test.fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.adapter.AdapterSong
import com.ezstudio.volumebooster.test.databinding.LayoutFragmentMusicBinding
import com.ezstudio.volumebooster.test.item.ItemSong
import com.ezstudio.volumebooster.test.service.LoudnessService
import com.ezstudio.volumebooster.test.utils.KeyEqualizer
import com.ezstudio.volumebooster.test.utils.PremiumUtils
import com.ezstudio.volumebooster.test.utils.RecycleViewUtils
import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.EzAdControl
import com.google.android.gms.ads.ez.listenner.NativeAdListener
import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.koin.android.ext.android.inject

class FragmentMusic(var service: LoudnessService?) : BaseFragment<LayoutFragmentMusicBinding>() {
    private var adapterSong: AdapterSong? = null
    private val listSong = mutableListOf<ItemSong>()
    private val viewModel by inject<MusicActiveViewModel>()
    override fun initView() {
        adapterSong = AdapterSong(requireContext(), listSong, service)
        binding.rclSong.adapter = adapterSong
        // clear animation
        RecycleViewUtils.clearAnimation(binding.rclSong)
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(
            requireContext(),
            R.layout.native_admob_item_song,
            object : NativeAdListener() {
                override fun onError() {
                }

                override fun onLoaded(nativeAd: RelativeLayout?) {
                    Log.d("Huy", "initListener: load Ads1")
                    addAdsToList(nativeAd)
                }

                override fun onClickAd() {
                }
            })
    }

    override fun initData() {

    }

    override fun initListener() {
        viewModel.listItemSong.observe(requireActivity()) {
            it?.let {
                binding.viewLoading.visibility = View.INVISIBLE
                listSong.clear()
                if (it.isNullOrEmpty()) {
                    binding.txtNoSong.visibility = View.VISIBLE
                    binding.rclSong.visibility = View.INVISIBLE
                } else {
                    binding.rclSong.visibility = View.VISIBLE
                    binding.txtNoSong.visibility = View.INVISIBLE
                }
                val path = PreferencesUtils.getString(KeyEqualizer.KEY_PATH_MUSIC, null)
                for (item in it) {
                    if (item.path == path) {
                        item.isSelected = true
                    }
                }
                listSong.addAll(it)
                // sort
                listSong.sortWith { o1, o2 ->
                    ((o1.songName ?: "a").toCharArray()[0].lowercase()
                        .compareTo(
                            (o2.songName ?: "a").toCharArray()[0].lowercase()
                        ))
                }
                adapterSong?.notifyDataSetChanged()
//                    //load ads
                loadAds()
            }
        }
        adapterSong?.onClickItem = {
            val isMusicPlaying = service?.mediaPlayer?.isPlaying ?: false
            //ads
            EzAdControl.getInstance(requireActivity()).setShowAdCallback(object : ShowAdCallback() {
                override fun onDisplay() {

                }

                override fun onDisplayFaild() {

                }

                override fun onClosed() {
                    //
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
            selectedMusic(it)
        }

//        viewModel.isDestroyMusicView.observe(requireActivity()) {
////            if (it && !listSong.isNullOrEmpty()) {
////                listSong.clear()
////                adapterSong?.notifyDataSetChanged()
//////                viewModel.listItemSong.value = null
////                binding.rclSong.visibility = View.INVISIBLE
////                binding.txtNoSong.visibility = View.VISIBLE
////            }
//        }
        //
        viewModel.isMusicActive.observe(requireActivity()) {
            for (item in listSong) {
                if (item.isSelected) {
                    adapterSong?.notifyItemChanged(listSong.indexOf(item))
                    break
                }
            }
        }
        // next
        viewModel.actionNext.observe(requireActivity()) {
            if (it) {
                for (i in 0 until listSong.size) {
                    if (listSong[i].isSelected) {
                        if (i + 1 < listSong.size) {
                            selectedMusic(i + 1)
                        } else {
                            selectedMusic(0)
                        }
                        break
                    }
                }
            }
        }
        // back
        viewModel.actionBack.observe(requireActivity()) {

            if (it) {
                for (i in 0 until listSong.size) {
                    if (listSong[i].isSelected) {
                        if (i - 1 < 0) {
                            selectedMusic(listSong.size - 1)
                        } else {
                            selectedMusic(i - 1)
                        }
                        break
                    }
                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentMusicBinding {
        return LayoutFragmentMusicBinding.inflate(layoutInflater, container, false)
    }

    private fun addAdsToList(nativeAd: RelativeLayout?) {
        val fileModel = ItemSong(null, null, null, null, false, nativeAd)
        if (listSong.size == 1) {
            listSong.add(1, fileModel)
            adapterSong?.notifyItemInserted(1)
        } else if (listSong.size >= 2) {
            listSong.add(2, fileModel)
            adapterSong?.notifyItemInserted(2)
        }
    }

    private fun selectedMusic(position: Int) {
        if (position >= 0) {
            val data = listSong[position]
            // rename control music
            viewModel.nameMusicSong.value = (data.songName)
            viewModel.nameAuthorSong.value = (data.singer)
            viewModel.imgSong.value = (data.resId)
            //
            data.path?.let { path ->
                PreferencesUtils.putString(KeyEqualizer.KEY_PATH_MUSIC, path)
                service?.onPlayMusic(path) { complete ->
                    if (complete) {
                        for (item in listSong) {
                            if (item == data) {
                                item.isSelected = true
                                adapterSong?.notifyItemChanged(position)
                            } else {
                                if (item.isSelected) {
                                    item.isSelected = false
                                    adapterSong?.notifyItemChanged(listSong.indexOf(item))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.listItemSong.value = null
    }
}