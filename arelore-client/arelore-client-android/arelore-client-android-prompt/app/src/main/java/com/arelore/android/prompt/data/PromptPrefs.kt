package com.arelore.android.prompt.data

import android.content.Context

class PromptPrefs(context: Context) {
    private val sp = context.getSharedPreferences("arelore_prompt", Context.MODE_PRIVATE)

    var envId: String
        get() = sp.getString(KEY_ENV, ServerEnv.LOCAL.id) ?: ServerEnv.LOCAL.id
        set(value) = sp.edit().putString(KEY_ENV, value.trim()).apply()

    var customBaseUrl: String
        get() = sp.getString(KEY_CUSTOM_BASE, "") ?: ""
        set(value) = sp.edit().putString(KEY_CUSTOM_BASE, value.trim()).apply()

    /** 兼容旧版直接存 api_base；优先按环境解析 */
    var apiBaseUrl: String
        get() {
            val env = ServerEnv.fromId(envId)
            return when (env) {
                ServerEnv.CUSTOM -> {
                    val custom = customBaseUrl.ifBlank {
                        sp.getString(KEY_BASE, ServerEnv.LOCAL.baseUrl) ?: ServerEnv.LOCAL.baseUrl
                    }
                    rewriteLoopbackForEmulator(custom.trimEnd('/'))
                }
                // 本机预设：始终用当前枚举 URL（10.0.2.2），避免旧包 127.0.0.1 残留
                else -> env.baseUrl
            }
        }
        set(value) {
            val trimmed = value.trim().trimEnd('/')
            sp.edit().putString(KEY_BASE, trimmed).apply()
            // 若手动写入完整 URL，切到自定义
            val matched = ServerEnv.entries.firstOrNull {
                it != ServerEnv.CUSTOM && it.baseUrl.equals(trimmed, ignoreCase = true)
            }
            if (matched != null) {
                envId = matched.id
            } else {
                envId = ServerEnv.CUSTOM.id
                customBaseUrl = trimmed
            }
        }

    var token: String
        get() = sp.getString(KEY_TOKEN, "") ?: ""
        set(value) = sp.edit().putString(KEY_TOKEN, value.trim()).apply()

    var nickname: String
        get() = sp.getString(KEY_NICKNAME, "") ?: ""
        set(value) = sp.edit().putString(KEY_NICKNAME, value.trim()).apply()

    var username: String
        get() = sp.getString(KEY_USERNAME, "") ?: ""
        set(value) = sp.edit().putString(KEY_USERNAME, value.trim()).apply()

    val isLoggedIn: Boolean
        get() = token.isNotBlank()

    var clientPref: PromptClientPref
        get() {
            val fieldsRaw = sp.getString(KEY_COPY_FIELDS, CopyField.PROMPT_TEXT) ?: CopyField.PROMPT_TEXT
            val fields = fieldsRaw.split(',')
                .map { it.trim().uppercase() }
                .filter { it.isNotEmpty() }
                .ifEmpty { listOf(CopyField.PROMPT_TEXT) }
            return PromptClientPref(
                fontScale = sp.getFloat(KEY_FONT_SCALE, 1.0f).toDouble(),
                fontSizeSp = sp.getInt(KEY_FONT_SIZE_SP, 14),
                copyFields = fields,
                copySep = sp.getString(KEY_COPY_SEP, "\n") ?: "\n"
            )
        }
        set(value) {
            sp.edit()
                .putFloat(KEY_FONT_SCALE, value.fontScale.toFloat())
                .putInt(KEY_FONT_SIZE_SP, value.fontSizeSp.coerceIn(10, 28))
                .putString(KEY_COPY_FIELDS, value.copyFields.joinToString(","))
                .putString(KEY_COPY_SEP, value.copySep)
                .apply()
        }

    fun applyEnv(env: ServerEnv, customUrl: String? = null) {
        envId = env.id
        if (env == ServerEnv.CUSTOM && !customUrl.isNullOrBlank()) {
            customBaseUrl = customUrl.trim().trimEnd('/')
            sp.edit().putString(KEY_BASE, customBaseUrl).apply()
        } else if (env != ServerEnv.CUSTOM) {
            // 强制写回预设 URL，避免旧版 127.0.0.1 残留在 KEY_BASE
            sp.edit().putString(KEY_BASE, env.baseUrl).apply()
        }
    }

    fun saveSession(token: String, nickname: String?, username: String?) {
        this.token = token
        if (!nickname.isNullOrBlank()) this.nickname = nickname
        if (!username.isNullOrBlank()) this.username = username
    }

    fun clearSession() {
        sp.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_NICKNAME)
            .remove(KEY_USERNAME)
            .apply()
    }

    companion object {
        private const val KEY_BASE = "api_base"
        private const val KEY_CUSTOM_BASE = "custom_api_base"
        private const val KEY_ENV = "server_env"
        private const val KEY_TOKEN = "token"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_USERNAME = "username"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_FONT_SIZE_SP = "font_size_sp"
        private const val KEY_COPY_FIELDS = "copy_fields"
        private const val KEY_COPY_SEP = "copy_sep"
        /** 模拟器访问宿主机本机服务；真机请改自定义局域网 IP */
        const val DEFAULT_BASE = "http://10.0.2.2:8081/api"

        /** 自定义地址若误填 127.0.0.1/localhost，在模拟器上自动改写为 10.0.2.2 */
        fun rewriteLoopbackForEmulator(url: String): String {
            val trimmed = url.trim().trimEnd('/')
            return trimmed
                .replace("://127.0.0.1", "://10.0.2.2")
                .replace("://localhost", "://10.0.2.2", ignoreCase = true)
        }
    }
}
