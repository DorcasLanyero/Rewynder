package com.rewyndr.rewyndr.view

import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.ViewDataBinding
import com.rewyndr.rewyndr.BR

class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
    var data: T? = null

    fun bind(item: T) {
        data = item
        binding.setVariable(BR.item, item)
        binding.executePendingBindings()
    }

    fun getItem(): T? {
        return data
    }
}