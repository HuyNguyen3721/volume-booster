package com.ezstudio.volumebooster.test.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.volumebooster.test.databinding.LayoutItemEqualizerBinding
import com.ezstudio.volumebooster.test.item.ItemEqualizer
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter

class AdapterEqualizer(var context: Context, list: MutableList<ItemEqualizer>) :
    BaseRecyclerAdapter<ItemEqualizer, AdapterEqualizer.ViewHolder>(context, list) {
    var onClickListener: ((Int) -> Unit)? = null
    var onClickDeleteListener: ((Int) -> Unit)? = null
    var onClickEditListener: ((Int) -> Unit)? = null

    inner class ViewHolder(var binding: LayoutItemEqualizerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(data: ItemEqualizer) {
            binding.name.text = data.name
            binding.name.setTextColor(Color.parseColor(if (data.isSelected) "#FF9500" else "#FFFFFF"))
            if (data.isCustom) {
                binding.icDelete.isVisible = true
                binding.icEdit.isVisible = true
            } else {
                binding.icDelete.isVisible = false
                binding.icEdit.isVisible = false
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
        holder.itemView.setOnClickListener {
            onClickListener?.invoke(holder.adapterPosition)
        }
        holder.binding.icDelete.setOnClickListener {
            onClickDeleteListener?.invoke(holder.adapterPosition)
        }
        holder.binding.icEdit.setOnClickListener {
            onClickEditListener?.invoke(holder.adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutItemEqualizerBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }
}