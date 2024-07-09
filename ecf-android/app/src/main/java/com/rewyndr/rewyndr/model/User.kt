package com.rewyndr.rewyndr.model

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONException
import org.json.JSONObject

@JsonClass(generateAdapter = true)
class User() {
    // Members
    var id = 0

    // Instance methods
    @Json(name="first_name")var firstName: String? = null
    @Json(name="last_name") var lastName: String? = null
    var email: String? = null
    var username: String? = null
    var token: String? = null
    var role: String? = null
    val fullName: String
        get() = "$firstName $lastName"
    val isOperator: Boolean
        get() = role == "operator"

    companion object {
        private const val TAG = "User"
    }

    // Constructors
    constructor(data: JSONObject) : this() {
        try {
            id = data.getInt("id")
            firstName = data.getString("first_name")
            lastName = data.getString("last_name")
            username = data.getString("username")
            role = data.getString("role")
            if (data.has("email")) {
                email = data.getString("email")
            }
            if (data.has("token")) {
                token = data.getString("token")
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error deserializing user: " + e.message)
        }
    }
}
