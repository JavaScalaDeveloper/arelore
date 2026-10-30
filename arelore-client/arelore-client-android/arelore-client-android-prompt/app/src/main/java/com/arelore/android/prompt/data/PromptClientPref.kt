package com.arelore.android.prompt.data

data class PromptClientPref(
    val fontScale: Double = 1.0,
    val fontSizeSp: Int = 14,
    val copyFields: List<String> = listOf(CopyField.PROMPT_TEXT),
    val copySep: String = "\n",
    val clientVersion: String? = null
)

object CopyField {
    const val TITLE = "TITLE"
    const val SUMMARY = "SUMMARY"
    const val PROMPT_TEXT = "PROMPT_TEXT"
    const val PUBLISH_STATUS = "PUBLISH_STATUS"
    const val CONFIG_TAGS = "CONFIG_TAGS"
    const val MANUAL_TAGS = "MANUAL_TAGS"
    const val PROMPT_UUID = "PROMPT_UUID"

    val OPTIONS = listOf(
        TITLE to "标题",
        SUMMARY to "描述",
        PROMPT_TEXT to "提示词",
        PUBLISH_STATUS to "状态",
        CONFIG_TAGS to "配置标签",
        MANUAL_TAGS to "人工标签",
        PROMPT_UUID to "UUID"
    )
}

fun buildCopyText(item: AiPromptItem, pref: PromptClientPref): String {
    val fields = pref.copyFields.ifEmpty { listOf(CopyField.PROMPT_TEXT) }
    val sep = pref.copySep.ifEmpty { "\n" }
    val parts = mutableListOf<String>()
    for (field in fields) {
        when (field) {
            CopyField.TITLE -> parts.add("标题：${item.title.orEmpty()}")
            CopyField.SUMMARY -> parts.add("描述：${item.summary.orEmpty()}")
            CopyField.PROMPT_TEXT -> parts.add("提示词：\n${item.promptText.orEmpty()}")
            CopyField.PUBLISH_STATUS -> parts.add("状态：${item.statusLabel()}")
            CopyField.CONFIG_TAGS -> parts.add("配置标签：${item.resolvedConfigTags().joinToString("、")}")
            CopyField.MANUAL_TAGS -> parts.add("人工标签：${item.resolvedManualTags().joinToString("、")}")
            CopyField.PROMPT_UUID -> parts.add("UUID：${item.promptUuid.orEmpty()}")
        }
    }
    return parts.joinToString(sep)
}
