package com.rewyndr.rewyndr.model
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray

import org.json.JSONException
import org.json.JSONObject

@JsonClass(generateAdapter = true)
class Step() {
    var id : Int = 0
    @Json(name="procedure_id") var procedureId : Int = 0
    var name : String = ""
    var description : String = ""
    @Json(name="require_image_on_pass") var requireImageOnPass : Boolean = false
    var image : Image? = null
    var procedures : List<Procedure> = ArrayList()
    @Json(name="execution_status") var executionStatus : String? = ""
    var comment : String = ""
    @Json(name="created_at") var createdAt : String? = ""
    @Json(name="updated_at") var updatedAt : String? = ""

    val imageId: Int
        get() = image!!.id

    val imageUrl: String
        get() = if (image == null) "" else image!!.url!!

    val imageThumbnailUrl: String
        get() = image!!.thumbnailUrl!!

    fun hasImage(): Boolean {
        return imageUrl.isNotEmpty()
    }

    val isComplete: Boolean
        get() = executionStatus == "pass" || executionStatus == "fail"

    companion object {
        private const val TAG = "Step"

        @JvmStatic
        fun deserializeSteps(json: String): ArrayList<Step> {
            val steps: ArrayList<Step> = ArrayList()
            val a: JSONArray
            try {
                a = JSONArray(json)
                for (i in 0 until a.length()) {
                    steps.add(Step(a.getJSONObject(i)))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error deserializing steps: " + e.message)
            }
            return steps
        }

        @JvmStatic
        fun deserializeSteps(arr: JSONArray): ArrayList<Step> {
            return deserializeSteps(arr.toString())
        }
    }

    constructor(data: JSONObject) : this() {
        try {
            id = data.getInt("id")
            procedureId = data.getInt("procedure_id")
            name = data.getString("name")
            description = data.getString("description")
            requireImageOnPass = data.getBoolean("require_image_on_pass")
            this.createdAt = data.getString("created_at")
            this.updatedAt = data.getString("updated_at")
            executionStatus = data.getString("execution_status")
            if (!data.isNull("image")) {
                image = Image(data.getJSONObject("image"))
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error deserializing step: " + e.message)
        }
    }
}