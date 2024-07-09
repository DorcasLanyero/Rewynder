package com.rewyndr.rewyndr.serialization

import android.graphics.Point
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class PointJsonAdapter {
    @FromJson fun fromJson(point: com.rewyndr.rewyndr.model.Point) : Point {
        return Point(point.x.toInt(), point.y.toInt())
    }

    @ToJson
    fun toJson(point: Point) : com.rewyndr.rewyndr.model.Point {
        return com.rewyndr.rewyndr.model.Point(point.x.toFloat(), point.y.toFloat())
    }
}