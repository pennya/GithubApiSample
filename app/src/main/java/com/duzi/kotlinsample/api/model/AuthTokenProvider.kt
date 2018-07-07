package com.duzi.kotlinsample.api.model

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by KIM on 2018-07-06.
 */

class AuthTokenProvider(applicationContext: Context) {

    companion object {
        val KEY_AUTH_TOKEN = "AUTH_TOKEN"
    }
    private var context: Context = applicationContext

    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply()
    }

    fun getToken(): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null)
    }
}