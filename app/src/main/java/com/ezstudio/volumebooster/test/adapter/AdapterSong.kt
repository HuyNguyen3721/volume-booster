package com.ezstudio.volumebooster.test.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.databinding.LayoutItemAdsBinding
import com.ezstudio.volumebooster.test.databinding.LayoutItemSongBinding
import com.ezstudio.volumebooster.test.item.ItemSong
import com.ezstudio.volumebooster.test.service.LoudnessService

class AdapterSong(
    var context: Context,
    var list: MutableList<ItemSong>,
    var service: LoudnessService?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onClickItem: ((Int) -> Unit)? = null

    inner class ViewHolder(var binding: LayoutItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(data: ItemSong) {
            Glide.with(context).load(data.resId ?: R.drawable.ic_image_song).into(binding.imgSong)
            binding.name.text = data.songName
            binding.author.text = data.singer
            if (data.isSelected) {
                binding.name.setTextColor(Color.parseColor("#FF9500"))
                binding.animationView.visibility = View.VISIBLE
                binding.animationView.setAnimation(R.raw.wave_music)
                if (service?.mediaPlayer?.isPlaying == true) {
                    binding.animationView.playAnimation()
                } else {
                    binding.animationView.pauseAnimation()
                }
            } else {
                binding.name.setTextColor(Color.parseColor("#FFFFFF"))
                binding.animationView.visibility = View.INVISIBLE
            }
        }
    }

    inner class ViewHolderAds(var bindingAds: LayoutItemAdsBinding) :
        RecyclerView.ViewHolder(bindingAds.root) {
        fun bindData(data: ItemSong) {
            data.ads?.let {
                if (it.parent != null) {
                    (it.parent as ViewGroup).removeView(it)
                }
                bindingAds.adsView.addView(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                ViewHolder(
                    LayoutItemSongBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            else -> {
                ViewHolderAds(
                    LayoutItemAdsBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        holder.apply {
            when (holder) {
                is ViewHolder -> {
                    holder.bindData(list[position])
                    holder.itemView.setOnClickListener {
                        onClickItem?.invoke(holder.adapterPosition)
                    }
                }
                is ViewHolderAds -> {
                    holder.bindData(data)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].ads) {
            null -> {
                1
            }
            else -> {
                0
            }
        }
    }
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bindData(list[position])
//        holder.itemView.setOnClickListener {
//            onClickItem?.invoke(holder.adapterPosition)
//        }
//    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(
//            LayoutItemSongBinding.inflate(layoutInflater, parent, false)
//        )
//    }
}