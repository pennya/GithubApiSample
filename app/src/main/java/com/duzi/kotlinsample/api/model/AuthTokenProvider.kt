package com.duzi.kotlinsample.api.model

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by KIM on 2018-07-06.
 */

class AuthTokenProvider(private val context: Context) {

    companion object {
        private val KEY_AUTH_TOKEN = "AUTH_TOKEN"
    }

    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply()
    }

    // read only
    val token: String?
        get() = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null)
}