package com.arelore.android.prompt.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AuthApi(
    private val baseUrl: String,
    private val tokenProvider: () -> String? = { null }
) {
    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 账号含 @ → 邮箱密码登录；否则 → 移动端用户名密码登录。
     * 密码均以 sha256 摘要提交。
     */
    fun login(account: String, password: String): AuthLoginData {
        val trimmed = account.trim()
        require(trimmed.isNotEmpty()) { "请输入账号" }
        require(password.isNotEmpty()) { "请输入密码" }
        val hash = sha256Hex(password)
        val data = if (trimmed.contains('@')) {
            emailPasswordLogin(trimmed, hash)
        } else {
            mobileLogin(trimmed, hash)
        }
        require(!data.token.isNullOrBlank()) { "登录成功但未返回 token" }
        return data
    }

    fun emailPasswordLogin(email: String, passwordHash: String): AuthLoginData {
        val type = object : TypeToken<ApiResult<AuthLoginData>>() {}.type
        val result: ApiResult<AuthLoginData> = post(
            "/user/auth/email/login",
            mapOf("email" to email, "passwordHash" to passwordHash),
            type,
            withAuth = false
        )
        require(result.code == 200) { result.message ?: "登录失败" }
        return result.data ?: throw IllegalStateException("登录响应为空")
    }

    fun mobileLogin(username: String, passwordHash: String): AuthLoginData {
        val type = object : TypeToken<ApiResult<AuthLoginData>>() {}.type
        val result: ApiResult<AuthLoginData> = post(
            "/user/auth/mobile/login",
            mapOf(
                "username" to username,
                "passwordHash" to passwordHash
            ),
            type,
            withAuth = false
        )
        require(result.code == 200) { result.message ?: "登录失败" }
        return result.data ?: throw IllegalStateException("登录响应为空")
    }

    fun currentUser(): AuthUserInfo {
        val type = object : TypeToken<ApiResult<AuthUserInfo>>() {}.type
        val result: ApiResult<AuthUserInfo> = post("/user/auth/current", emptyMap<String, Any>(), type, withAuth = true)
        require(result.code == 200) { result.message ?: "获取用户失败" }
        return result.data ?: throw IllegalStateException("用户信息为空")
    }

    fun logout() {
        try {
            val type = object : TypeToken<ApiResult<Any?>>() {}.type
            post<ApiResult<Any?>>("/user/auth/logout", emptyMap<String, Any>(), type, withAuth = true)
        } catch (_: Exception) {
            // 本地仍清除会话
        }
    }

    private fun <T> post(
        path: String,
        body: Any,
        type: java.lang.reflect.Type,
        withAuth: Boolean
    ): T {
        val url = baseUrl.trimEnd('/') + path
        val reqBody = gson.toJson(body).toRequestBody(jsonMedia)
        val builder = Request.Builder().url(url).post(reqBody)
        if (withAuth) {
            val token = tokenProvider()?.trim().orEmpty()
            if (token.isNotEmpty()) {
                builder.header("Authorization", if (token.startsWith("Bearer ")) token else "Bearer $token")
            }
        }
        client.newCall(builder.build()).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            require(resp.isSuccessful) { "HTTP ${resp.code}: $text" }
            return gson.fromJson(text, type)
        }
    }
}
