package com.ezstudio.volumebooster.test.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.databinding.LayoutItemVolumeBinding
import com.ezstudio.volumebooster.test.item.ItemChangeVolume
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter

class AdapterSetVolume(var context: Context, list: MutableList<ItemChangeVolume>) :
    BaseRecyclerAdapter<ItemChangeVolume, AdapterSetVolume.ViewHolder>(context, list) {

    var onClickListener: ((Int) -> Unit)? = null

    inner class ViewHolder(var binding: LayoutItemVolumeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bindData(data: ItemChangeVolume) {
            binding.txtName.text = data.name
            binding.txtName.background =
                context.resources.getDrawable(if (data.isSelected) R.drawable.bg_item_volume_choose else R.drawable.bg_item_volume)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
        holder.itemView.setOnClickListener {
            onClickListener?.invoke(holder.adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutItemVolumeBinding.inflate(layoutInflater, parent, false))
    }
}