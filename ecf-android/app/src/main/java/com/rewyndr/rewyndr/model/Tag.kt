package com.rewyndr.rewyndr.model

import android.graphics.Point
import android.graphics.RectF
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
class Tag() {
    // Members
    var id = 0
    @Json(name="image_id") var imageId = 0
    var name: String? = null
    @Json(name="boundary_type") var boundaryType: String? = null
    @Json(name="boundary_color") var boundaryColor: String? = null
    var icon: String? = null
    @Json(name="created_at") var createdAt: String? = null
    @Json(name="updated_at") var updatedAt: String? = null
    var points: List<Point>? = null
    var annotations: List<Annotation> = ArrayList<Annotation>()

    // Instance methods
    private fun deserializePoints(points: JSONArray): ArrayList<Point> {
        val pointList = ArrayList<Point>()
        for (i in 0 until points.length()) {
            try {
                val pointJSON = points.getJSONObject(i)
                pointList.add(Point(pointJSON.getInt("x"), pointJSON.getInt("y")))
            } catch (e: JSONException) {
                Log.e(TAG, "Error deserializing point list")
            }
        }
        return pointList
    }

    fun contains(x: Float, y: Float): Boolean {
        var isInPolygon = false
        var x2 = points!![points!!.size - 1].x
        var y2 = points!![points!!.size - 1].y
        var x1 : Int
        var y1 : Int
        var i = 0
        while (i <= points!!.size - 1) {
            x1 = points!![i].x
            y1 = points!![i].y
            if (y1 < y && y2 >= y || y1 >= y && y2 < y) {
                if ((y - y1) / (y2 - y1) * (x2 - x1) < x - x1) {
                    isInPolygon = !isInPolygon
                }
            }
            x2 = x1
            y2 = y1
            i++
        }
        return isInPolygon
    }

    // Box tag methods
    val x: Int
        get() = points!![0].x

    val y: Int
        get() = points!![0].y
    val left: Int
        get() = x
    val top: Int
        get() = y
    val right: Int
        get() = points!![1].x
    val bottom: Int
        get() = points!![2].y
    val width: Int
        get() = right - left
    val height: Int
        get() = bottom - top

    fun getCreatedDate() : Date {
        val formatFrom = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return formatFrom.parse(createdAt)
    }

    fun rectangle(): RectF {
        return RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    companion object {
        private const val TAG = "Tag"

        @JvmStatic
        fun deserializeTags(json: String?): ArrayList<Tag> {
            val tags = ArrayList<Tag>()
            val a: JSONArray
            try {
                a = JSONArray(json)
                for (i in 0 until a.length()) {
                    tags.add(Tag(a.getJSONObject(i)))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error deserializing steps: " + e.message)
            }
            return tags
        }

        @JvmStatic
        fun deserializeTags(arr: JSONArray): ArrayList<Tag> {
            return deserializeTags(arr.toString())
        }
    }

    // Constructors
    constructor(tagData: JSONObject) : this() {
        try {
            id = tagData.getInt("id")
            imageId = tagData.getInt("image_id")
            name = tagData.getString("name")
            boundaryType = tagData.getString("boundary_type")
            boundaryColor = tagData.getString("boundary_color")
            icon = tagData.getString("icon")
            points = deserializePoints(tagData.getJSONArray("points"))
            createdAt = tagData.getString("created_at")
            updatedAt = tagData.getString("updated_at")
            annotations = Annotation.deserializeAnnotations(tagData.getJSONArray("annotations"))
            annotations.sortedWith { annotation: Annotation, t1: Annotation ->
                annotation.createdAt!!.compareTo(t1.createdAt!!)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error deserializing tag: " + e.message)
        }
    }

    constructor(tag: Tag) : this() {
        id = tag.id
        imageId = tag.imageId
        name = tag.name
        boundaryType = tag.boundaryType
        boundaryColor = tag.boundaryColor
        icon = tag.icon
        points = tag.points
        createdAt = tag.createdAt
        updatedAt = tag.updatedAt
        annotations = tag.annotations
    }

    constructor(points: List<Point>, info: TagFormInformation, step: Step) : this(){
        imageId = step.imageId
        name = info.name
        boundaryType = if(points.size == 4) "box" else "smart"
        boundaryColor = info.color
        icon = info.icon
        this.points = points
    }
}