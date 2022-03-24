package com.ezstudio.volumebooster.test.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.volumebooster.test.databinding.LayoutItemSettingsBinding
import com.ezstudio.volumebooster.test.item.ItemSetting
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter

class AdapterSettings(var context: Context, list: MutableList<ItemSetting>) :
    BaseRecyclerAdapter<ItemSetting, AdapterSettings.ViewHolder>(context, list) {
    var onClickItem: ((Int) -> Unit)? = null

    inner class ViewHolder(var binding: LayoutItemSettingsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(data: ItemSetting) {
            binding.icIcon.setImageResource(data.resId)
            binding.name.text = data.name
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
        holder.itemView.setOnClickListener {
            onClickItem?.invoke(holder.adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutItemSettingsBinding.inflate(layoutInflater, parent, false))
    }
}