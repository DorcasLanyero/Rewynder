package com.rewyndr.rewyndr.model

import android.content.Context
import android.graphics.drawable.Drawable
import com.rewyndr.rewyndr.R

class SafetyIcon(val text: String,
                 val icon: Drawable) {
    override fun equals(other: Any?) : Boolean {
        val item = other as? SafetyIcon
        return item?.text == text && item.icon == icon
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + icon.hashCode()
        return result
    }

    companion object {
        fun getIcons(context: Context) : List<SafetyIcon> {
            return listOf(
                    SafetyIcon("health_hazard", context.getDrawable(R.drawable.safety_icon_health_hazard)!!),
                            SafetyIcon("gas_cylinder", context.getDrawable(R.drawable.safety_icon_gas_cylinder)!!),
                            SafetyIcon("bomb", context.getDrawable(R.drawable.safety_icon_bomb)!!),
                            SafetyIcon("flame", context.getDrawable(R.drawable.safety_icon_flame)!!),
                            SafetyIcon("skull", context.getDrawable(R.drawable.safety_icon_skull)!!),
                            SafetyIcon("exclamation", context.getDrawable(R.drawable.safety_icon_exclamation)!!),
                            SafetyIcon("corrosion", context.getDrawable(R.drawable.safety_icon_corrosion)!!),
                            SafetyIcon("flame_over_circle", context.getDrawable(R.drawable.safety_icon_flame_over_circle)!!)
                    )
        }
    }
}