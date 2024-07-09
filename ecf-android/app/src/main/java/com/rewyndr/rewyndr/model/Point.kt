package com.rewyndr.rewyndr.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Point(val x: Float,
                 val y: Float) {
    companion object {
        fun fromAndroidPoint(point: android.graphics.Point) : Point{
            return Point(point.x.toFloat(), point.y.toFloat())
        }
    }
}