package com.rewyndr.rewyndr.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rewyndr.rewyndr.R
import com.rewyndr.rewyndr.interfaces.ISafetyIconSelectionObserver
import com.rewyndr.rewyndr.model.SafetyIcon
import com.rewyndr.rewyndr.view.DataBindingViewHolder

class SafetyIconAdapter(private val mContext: Context,
                        private val selectedTag: String?) : ListAdapter<SafetyIcon, DataBindingViewHolder<SafetyIcon>>(ComparisonCallback()) {
    private val observers = ArrayList<ISafetyIconSelectionObserver>()
    private val icons = SafetyIcon.getIcons(mContext)

    private var selectedViewHolder: DataBindingViewHolder<SafetyIcon>? = null

    override fun onBindViewHolder(holder: DataBindingViewHolder<SafetyIcon>, position: Int) {
        val currentIcon = icons[position]
        holder.bind(currentIcon)

        if(selectedTag == currentIcon.text){
            val container = holder.itemView.findViewById<LinearLayout>(R.id.safety_icon_container)
            container.setBackgroundColor(mContext.getColor(R.color.brandBackground))
            selectedViewHolder = holder

            updateObservers(getSelectedIcon())
        }

        holder.itemView.setOnClickListener {
            if (selectedViewHolder != null) {
                val oldContainer = selectedViewHolder!!.itemView.findViewById<LinearLayout>(R.id.safety_icon_container)
                oldContainer.setBackgroundResource(R.color.brandLightBackground)
            }

            val container = holder.itemView.findViewById<LinearLayout>(R.id.safety_icon_container)

            if(selectedViewHolder == holder){
                selectedViewHolder = null
                container.setBackgroundColor(mContext.getColor(R.color.brandLightBackground))
            }else {
                selectedViewHolder = holder
                container.setBackgroundColor(mContext.getColor(R.color.brandBackground))
            }

            updateObservers(getSelectedIcon())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder<SafetyIcon> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(mContext), viewType, parent, false)
        return DataBindingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return icons.size
    }

    private fun getSelectedIcon(): String {
        return selectedViewHolder?.getItem()?.text ?: ""
    }

    fun registerObserver(obs: ISafetyIconSelectionObserver){
        observers.add(obs)
    }

    private fun updateObservers(iconStr: String){
        for(observer in observers){
            observer.selectionUpdated(iconStr)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.option_safety_icon
    }

    private class ComparisonCallback : DiffUtil.ItemCallback<SafetyIcon>() {
        override fun areItemsTheSame(oldItem: SafetyIcon, newItem: SafetyIcon): Boolean {
            return oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: SafetyIcon, newItem: SafetyIcon): Boolean {
            return oldItem == newItem
        }
    }
}