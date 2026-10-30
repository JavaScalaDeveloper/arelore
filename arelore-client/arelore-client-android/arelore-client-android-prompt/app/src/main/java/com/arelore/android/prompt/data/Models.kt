package com.arelore.android.prompt.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AiPromptItem(
    val id: Long? = null,
    val promptUuid: String? = null,
    val title: String? = null,
    val promptText: String? = null,
    val summary: String? = null,
    val configTagList: List<String>? = null,
    val manualTagList: List<String>? = null,
    /** 服务端原始 JSON 字符串，列表字段缺失时兜底解析 */
    val configTags: String? = null,
    val manualTags: String? = null,
    /** 服务端合并列表，兼容旧字段 */
    val tagList: List<String>? = null,
    val publishStatus: String? = null,
    val status: String? = null,
    val createTime: String? = null,
    val modifyTime: String? = null
) {
    fun resolvedConfigTags(): List<String> =
        normalizeNames(configTagList).ifEmpty { parseTagJson(configTags) }

    fun resolvedManualTags(): List<String> =
        normalizeNames(manualTagList).ifEmpty { parseTagJson(manualTags) }

    fun displayTags(): List<String> {
        val merged = LinkedHashSet<String>()
        resolvedConfigTags().forEach { merged.add(it) }
        resolvedManualTags().forEach { merged.add(it) }
        if (merged.isEmpty()) {
            normalizeNames(tagList).forEach { merged.add(it) }
        }
        return merged.toList()
    }

    fun statusLabel(): String = PublishStatuses.labelOf(publishStatus)

    companion object {
        private val gson = Gson()

        fun normalizeNames(list: List<String>?): List<String> =
            list.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        fun parseTagJson(raw: String?): List<String> {
            val text = raw?.trim().orEmpty()
            if (text.isEmpty()) return emptyList()
            return try {
                val type = object : TypeToken<List<String>>() {}.type
                normalizeNames(gson.fromJson(text, type))
            } catch (_: Exception) {
                normalizeNames(text.split(',', '，', ';', '；'))
            }
        }
    }
}

data class AiPromptTag(
    val id: Long? = null,
    val tagType: String? = null,
    val name: String? = null,
    val color: String? = null,
    val sortOrder: Int? = null,
    val status: String? = null
)

data class ApiResult<T>(
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null
)

data class PageData(
    val list: List<AiPromptItem>? = null,
    val records: List<AiPromptItem>? = null,
    val total: Long? = null,
    val pageNum: Int? = null,
    val pageSize: Int? = null
) {
    fun items(): List<AiPromptItem> = list ?: records ?: emptyList()
}

object PublishStatuses {
    val OPTIONS = listOf(
        "DRAFT" to "草稿",
        "PRODUCING" to "制作中",
        "PUBLISHED" to "已发布",
        "OFFLINE" to "已下架"
    )

    fun labelOf(code: String?): String =
        OPTIONS.firstOrNull { it.first == code }?.second ?: (code?.ifBlank { "未知" } ?: "未知")
}
