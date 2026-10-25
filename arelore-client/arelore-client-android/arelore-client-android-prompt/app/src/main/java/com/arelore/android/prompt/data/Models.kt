package com.arelore.android.prompt.data

data class AiPromptItem(
    val id: Long? = null,
    val promptUuid: String? = null,
    val title: String? = null,
    val promptText: String? = null,
    val summary: String? = null,
    val configTagList: List<String>? = null,
    val manualTagList: List<String>? = null,
    /** 服务端合并列表，兼容旧字段 */
    val tagList: List<String>? = null,
    val publishStatus: String? = null,
    val status: String? = null
) {
    fun displayTags(): List<String> {
        val merged = LinkedHashSet<String>()
        configTagList?.forEach { if (it.isNotBlank()) merged.add(it) }
        manualTagList?.forEach { if (it.isNotBlank()) merged.add(it) }
        if (merged.isEmpty()) {
            tagList?.forEach { if (it.isNotBlank()) merged.add(it) }
        }
        return merged.toList()
    }
}

data class ApiResult<T>(
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null
)

data class PageData(
    val list: List<AiPromptItem>? = null,
    val total: Long? = null,
    val pageNum: Int? = null,
    val pageSize: Int? = null
)
