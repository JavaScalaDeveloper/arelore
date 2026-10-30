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

    fun detail(id: Long): AiPromptItem {
        val type = object : TypeToken<ApiResult<AiPromptItem>>() {}.type
        val result: ApiResult<AiPromptItem> = post("/user/prompt/detail", mapOf("id" to id), type)
        require(result.code == 200) { result.message ?: "加载详情失败" }
        return result.data ?: throw IllegalStateException("详情为空")
    }

    fun save(item: AiPromptItem): AiPromptItem {
        val body = mutableMapOf<String, Any?>()
        item.id?.let { body["id"] = it }
        body["title"] = item.title
        body["summary"] = item.summary
        body["promptText"] = item.promptText
        body["configTagList"] = item.configTagList ?: emptyList<String>()
        body["manualTagList"] = item.manualTagList ?: emptyList<String>()
        body["publishStatus"] = item.publishStatus ?: "DRAFT"
        val type = object : TypeToken<ApiResult<AiPromptItem>>() {}.type
        val result: ApiResult<AiPromptItem> = post("/user/prompt/save", body, type)
        require(result.code == 200) { result.message ?: "保存失败" }
        return result.data ?: item
    }

    fun getPref(): PromptClientPref {
        val text = postRaw("/user/prompt/pref/get", emptyMap<String, Any>())
        return parsePref(text)
    }

    fun savePref(pref: PromptClientPref): PromptClientPref {
        val body = mutableMapOf<String, Any?>(
            "fontScale" to pref.fontScale,
            "fontSizeSp" to pref.fontSizeSp,
            "copyFields" to pref.copyFields,
            "copySep" to pref.copySep
        )
        if (!pref.clientVersion.isNullOrBlank()) {
            body["clientVersion"] = pref.clientVersion
        }
        val text = postRaw("/user/prompt/pref/save", body)
        return parsePref(text)
    }

    fun reportVisit(client: String = "ANDROID") {
        postRaw("/user/prompt/visit/report", mapOf("client" to client))
    }

    private fun parsePref(text: String): PromptClientPref {
        val root = gson.fromJson(text, com.google.gson.JsonObject::class.java)
        val code = root.get("code")?.asInt ?: -1
        require(code == 200) { root.get("message")?.asString ?: "偏好请求失败" }
        val data = root.getAsJsonObject("data") ?: return PromptClientPref()
        val scale = data.get("fontScale")?.asDouble ?: 1.0
        val size = data.get("fontSizeSp")?.asInt ?: 14
        val sep = data.get("copySep")?.asString ?: "\n"
        val version = data.get("clientVersion")?.asString
        val fields = mutableListOf<String>()
        val arr = data.getAsJsonArray("copyFields")
        if (arr != null) {
            for (el in arr) {
                val v = el.asString?.trim().orEmpty()
                if (v.isNotEmpty()) fields.add(v.uppercase())
            }
        }
        return PromptClientPref(
            fontScale = scale,
            fontSizeSp = size,
            copyFields = fields.ifEmpty { listOf(CopyField.PROMPT_TEXT) },
            copySep = sep,
            clientVersion = version
        )
    }

    fun tagList(): List<AiPromptTag> {
        val text = postRaw("/user/prompt/tag/list", emptyMap<String, Any>())
        val root = gson.fromJson(text, com.google.gson.JsonObject::class.java)
        val code = root.get("code")?.asInt ?: -1
        require(code == 200) {
            root.get("message")?.asString ?: "加载标签失败"
        }
        val data = root.get("data") ?: return emptyList()
        if (data.isJsonNull) return emptyList()
        if (data.isJsonArray) {
            val type = object : TypeToken<List<AiPromptTag>>() {}.type
            return gson.fromJson(data, type) ?: emptyList()
        }
        // 兼容偶发包一层 list/records
        if (data.isJsonObject) {
            val obj = data.asJsonObject
            val arr = obj.get("list") ?: obj.get("records")
            if (arr != null && arr.isJsonArray) {
                val type = object : TypeToken<List<AiPromptTag>>() {}.type
                return gson.fromJson(arr, type) ?: emptyList()
            }
        }
        return emptyList()
    }

    private fun <T> post(path: String, body: Any, type: java.lang.reflect.Type): T {
        val text = postRaw(path, body)
        return gson.fromJson(text, type)
    }

    private fun postRaw(path: String, body: Any): String {
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
            return text
        }
    }
}
