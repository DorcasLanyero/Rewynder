package com.rewyndr.rewyndr.model

import android.util.Log
import com.google.gson.Gson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant


@JsonClass(generateAdapter = true)
class Procedure() {
    var id: Int = 0
    @Json(name="community_id") var communityId: Int = 0
    var name: String = ""
    var description: String = ""
    var status: String = ""
    @Json(name="image_url") var imageUrl: String = ""
    var steps: List<Step> = ArrayList()
    @Json(name="created_time") var createdTime: String = ""

    val timeCreated: Instant
        get() = Instant.parse(createdTime)
    val isPublished: Boolean
        get() = status == STATUS_PUBLISHED

    companion object {
        private const val TAG = "Procedure"

        // Status constants
        private const val STATUS_DRAFT = "draft"
        private const val STATUS_PUBLISHED = "published"
        private const val STATUS_ARCHIVED = "archived"

        // Static methods
        @JvmStatic
        val statuses = arrayOf(STATUS_DRAFT, STATUS_PUBLISHED, STATUS_ARCHIVED)

        @JvmStatic
        fun deserializeProcedures(json: String?): ArrayList<Procedure>? {
            val procedures: ArrayList<Procedure> = ArrayList()
            val a: JSONArray
            try {
                a = JSONArray(json)
                for (i in 0 until a.length()) {
                    procedures.add(Procedure(a.getJSONObject(i)))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error deserializing procedures: " + e.message)
            }
            return procedures
        }

        @JvmStatic
        fun deserializeProcedures(arr: JSONArray): ArrayList<Procedure>? {
            return deserializeProcedures(arr.toString())
        }
    }

    fun serialize(): String? {
        val gson = Gson()
        val json = gson.toJsonTree(this)
        return gson.toJson(json)
    }

    constructor(data: JSONObject) : this() {
        try {
            id = data.getInt("id")
            communityId = data.getInt("community_id")
            name = data.getString("name")
            description = data.getString("description")
            status = data.getString("status")
            steps = Step.deserializeSteps(data.getJSONArray("steps"))
            createdTime = data.getString("created_at")
            for (step in steps) {
                if (step.hasImage()) {
                    imageUrl = step.imageThumbnailUrl
                    break
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error deserializing procedure: " + e.message)
        }
    }
}