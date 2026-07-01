package com.example.hoof_care_02.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {
    private const val PREFS_NAME = "hoof_care_prefs"
    private const val AUTH_TOKEN = "auth_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuthToken(token: String) {
        if (::prefs.isInitialized) {
            val editor = prefs.edit()
            editor.putString(AUTH_TOKEN, token)
            editor.apply()
        }
    }

    fun getAuthToken(): String? {
        return if (::prefs.isInitialized) {
            prefs.getString(AUTH_TOKEN, null)
        } else {
            null
        }
    }

    fun clearAuthToken() {
        if (::prefs.isInitialized) {
            val editor = prefs.edit()
            editor.remove(AUTH_TOKEN)
            editor.apply()
        }
    }
}