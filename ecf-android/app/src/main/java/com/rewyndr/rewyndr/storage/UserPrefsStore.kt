package com.rewyndr.rewyndr.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.rewyndr.rewyndr.model.User
import com.squareup.moshi.Moshi
import org.json.JSONException

class UserPrefsStore(val context: Context) {
    companion object {
        private const val CURRENT_USER_STORE = "rewyndr-current-user-store"
        private const val CURRENT_USER_KEY = "currentUser"

        private const val TAG = "UserPrefsStore"
    }

    private fun getCurrentUserStore(): SharedPreferences? {
        return context.getSharedPreferences(CURRENT_USER_STORE, Context.MODE_PRIVATE)
    }

    fun getCurrentUser(): User? {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(User::class.java)
        val userJSON: String = getCurrentUserStore()!!.getString(CURRENT_USER_KEY, null)!!
        try {
            return adapter.fromJson(userJSON)
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing current user JSON: " + e.message)
        }
        return null
    }
}