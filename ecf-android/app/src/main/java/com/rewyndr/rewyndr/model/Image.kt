package com.rewyndr.rewyndr.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.rewyndr.rewyndr.boundary.ImageProcessor
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONException
import org.json.JSONObject
import java.util.*


@JsonClass(generateAdapter = true)
class Image() {
    companion object {
        private const val TAG: String = "Image"
    }

    // Members
    var id = 0
    var url: String? = null
    @Json(name="thumbnail_url") var thumbnailUrl: String? = null
    @Json(name="created_at") var createdAt: String? = null
    @Json(name="updated_at") var updatedAt: String? = null
    var tags: List<Tag> = ArrayList<Tag>()

    // Instance methods
    var annotations: List<Annotation> = ArrayList<Annotation>()

    // Bitmaps
    @Transient
    var largeBitmap: Bitmap? = null

    @Transient
    var darkenLargeBitmap: Bitmap? = null
        get() {
            if (field == null) {
                field = ImageProcessor.generateDarkenImage(
                        largeBitmap, 100)
            }
            return field
        }
        private set

    @Transient
    private var darkenTaggedLargeBitmap: Bitmap? = null

    @Transient
    private var taggedLargeBitmap: Bitmap? = null

    // Constructors
    constructor(data: JSONObject) : this() {
        try {
            id = data.getInt("id")
            url = data.getString("url")
            thumbnailUrl = data.getString("thumbnail_url")
            createdAt = data.getString("created_at")
            updatedAt = data.getString("updated_at")
            tags = Tag.deserializeTags(data.getJSONArray("tags"))
            val list = ArrayList<Annotation>()
            list.addAll(Annotation.deserializeAnnotations(data.getJSONArray("annotations")))
            for (t in tags) {
                list.addAll(t.annotations)
            }
            list.sortWith { annotation: Annotation?, t1: Annotation? ->
                    annotation!!.createdAt!!.compareTo(t1!!.createdAt!!)
            }
            annotations = list
        } catch (e: JSONException) {
            Log.e(TAG, "Error deserializing image: " + e.message)
        }
    }

    constructor(url: String?) : this() {
        id = 0
        this.url = url
        thumbnailUrl = url
        createdAt = ""
        updatedAt = ""
        tags = ArrayList()
        annotations = ArrayList()
    }

    fun getDarkenTaggedLargeBitmap(context: Context?): Bitmap? {
        if (darkenTaggedLargeBitmap == null) {
            darkenTaggedLargeBitmap = ImageProcessor.generateDarkenImage(getTaggedLargeBitmap(context), 100)
        }
        return darkenTaggedLargeBitmap
    }

    fun getTaggedLargeBitmap(context: Context?): Bitmap? {
        if (taggedLargeBitmap == null) {
            taggedLargeBitmap = ImageProcessor.generateTaggedBitmap(context, largeBitmap, tags)
        }
        return taggedLargeBitmap
    }

    fun getAllAnnotations() : List<Annotation> {
        val list = mutableListOf<Annotation>()

        for(tag in tags){
            list.addAll(tag.annotations)
        }

        list.addAll(annotations)

        return list.sortedWith { a1 : Annotation, a2 : Annotation ->
            a1.createdAt!!.compareTo(a2.createdAt!!)
        }
    }
}