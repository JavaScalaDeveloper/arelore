package com.arelore.android.prompt.data

import android.content.Context

class PromptPrefs(context: Context) {
    private val sp = context.getSharedPreferences("arelore_prompt", Context.MODE_PRIVATE)

    var apiBaseUrl: String
        get() = sp.getString(KEY_BASE, DEFAULT_BASE) ?: DEFAULT_BASE
        set(value) = sp.edit().putString(KEY_BASE, value.trim()).apply()

    var token: String
        get() = sp.getString(KEY_TOKEN, "") ?: ""
        set(value) = sp.edit().putString(KEY_TOKEN, value.trim()).apply()

    companion object {
        private const val KEY_BASE = "api_base"
        private const val KEY_TOKEN = "token"
        const val DEFAULT_BASE = "http://10.0.2.2:8081/api"
    }
}
