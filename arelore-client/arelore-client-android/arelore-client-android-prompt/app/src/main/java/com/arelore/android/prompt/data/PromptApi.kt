package com.arelore.android.prompt.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class PromptApi(
    private val baseUrl: String,
    private val tokenProvider: () -> String?
) {
    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun page(
        pageNum: Int,
        pageSize: Int,
        titleKeyword: String?,
        publishStatus: String?,
        tagName: String?
    ): PageData {
        val body = mutableMapOf<String, Any?>(
            "pageNum" to pageNum,
            "pageSize" to pageSize
        )
        if (!titleKeyword.isNullOrBlank()) body["titleKeyword"] = titleKeyword
        if (!publishStatus.isNullOrBlank()) body["publishStatus"] = publishStatus
        if (!tagName.isNullOrBlank()) body["tagName"] = tagName
        val type = object : TypeToken<ApiResult<PageData>>() {}.type
        val result: ApiResult<PageData> = post("/user/prompt/page", body, type)
        require(result.code == 200) { result.message ?: "请求失败" }
        return result.data ?: PageData()
    }

    fun list(titleKeyword: String?, publishStatus: String?, tagName: String?): List<AiPromptItem> {
        val body = mutableMapOf<String, Any?>()
        if (!titleKeyword.isNullOrBlank()) body["titleKeyword"] = titleKeyword
        if (!publishStatus.isNullOrBlank()) body["publishStatus"] = publishStatus
        if (!tagName.isNullOrBlank()) body["tagName"] = tagName
        val type = object : TypeToken<ApiResult<List<AiPromptItem>>>() {}.type
        val result: ApiResult<List<AiPromptItem>> = post("/user/prompt/list", body, type)
        require(result.code == 200) { result.message ?: "请求失败" }
        return result.data ?: emptyList()
    }

    private fun <T> post(path: String, body: Any, type: java.lang.reflect.Type): T {
        val url = baseUrl.trimEnd('/') + path
        val reqBody = gson.toJson(body).toRequestBody(jsonMedia)
        val builder = Request.Builder().url(url).post(reqBody)
        val token = tokenProvider()?.trim().orEmpty()
        if (token.isNotEmpty()) {
            builder.header("Authorization", if (token.startsWith("Bearer ")) token else "Bearer $token")
        }
        client.newCall(builder.build()).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            require(resp.isSuccessful) { "HTTP ${resp.code}: $text" }
            return gson.fromJson(text, type)
        }
    }
}
