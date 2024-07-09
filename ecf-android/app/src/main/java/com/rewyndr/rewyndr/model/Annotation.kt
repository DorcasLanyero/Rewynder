package com.rewyndr.rewyndr.model

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
class Annotation(){

    var id: Int = 0
    @Json(name = "annotateable_id") var annotateableId: Int = 0
    @Json(name = "annotateable_type") var annotateableType: String? = null
    var content: String? = null
    @Json(name = "attachment_url") var attachmentUrl: String? = null
    var author: User? = null
    @Json(name = "created_at") var createdAt: String? = null
    @Json(name = "updated_at") var updatedAt: String? = null
    @Json(name = "attachment_type") var attachmentType: String? = null

    fun hasImage(): Boolean {
        return attachmentType == "image"
    }

    fun hasAudio(): Boolean {
        return attachmentType != null && attachmentType == "audio"
    }

    val formattedCreatedAt: String
        get() {
            val formatFrom = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val formatToDate = SimpleDateFormat("MM/dd/yy", Locale.US)
            val formatToTime = SimpleDateFormat("h:mm aa z", Locale.US)
            val tzFrom = TimeZone.getTimeZone("UTC")
            val tzTo = TimeZone.getTimeZone("America/New_York")
            formatFrom.timeZone = tzFrom
            formatToDate.timeZone = tzTo
            formatToTime.timeZone = tzTo
            var date : String
            var time : String
            try {
                val intermediateDate = if (createdAt == null || createdAt!!.isEmpty()) Date(Long.MIN_VALUE) else formatFrom.parse(createdAt)
                date = formatToDate.format(intermediateDate)
                time = formatToTime.format(intermediateDate)
            } catch (e: ParseException) {
                Log.e(TAG, "Error parsing annotation created_at timestamp: " + e.message)
                return ""
            }
            return "$date at $time"
        }

    companion object {
        private const val TAG = "Annotation"
        @JvmStatic
        fun deserializeAnnotations(json: String?): ArrayList<Annotation> {
            val annotations = ArrayList<Annotation>()
            val a: JSONArray
            try {
                a = JSONArray(json)
                for (i in 0 until a.length()) {
                    annotations.add(Annotation(a.getJSONObject(i)))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error deserializing annotations: " + e.message)
            }
            return annotations
        }

        @JvmStatic
        fun deserializeAnnotations(arr: JSONArray): ArrayList<Annotation> {
            return deserializeAnnotations(arr.toString())
        }
    }

    constructor(data: JSONObject) : this() {
        try {
            id = data.getInt("id")
            annotateableId = data.getInt("annotateable_id")
            annotateableType = data.getString("annotateable_type")
            content = data.getString("content")
            attachmentUrl = data.getString("attachment_url")
            createdAt = data.getString("created_at")
            updatedAt = data.getString("updated_at")
            attachmentType = data.getString("attachment_type")
            if (!data.isNull("author")) {
                author = User(data.getJSONObject("author"))
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error deserializing procedure: " + e.message)
        }
    }
}