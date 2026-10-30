package com.arelore.android.prompt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PromptBlue = Color(0xFF1B6BDE)
val PromptBg = Color(0xFFF5F7FA)
val PromptTextPrimary = Color(0xFF1A1A1A)
val PromptTextSecondary = Color(0xFF666666)

/** 配置标签：偏蓝 */
val TagConfigBg = Color(0xFFE8F1FC)
val TagConfigFg = Color(0xFF1B6BDE)

/** 人工标签：偏青绿 */
val TagManualBg = Color(0xFFE6F6F1)
val TagManualFg = Color(0xFF0F766E)

val StatusDraftBg = Color(0xFFF0F2F5)
val StatusDraftFg = Color(0xFF5B6470)
val StatusProducingBg = Color(0xFFFFF4E5)
val StatusProducingFg = Color(0xFFB45309)
val StatusPublishedBg = Color(0xFFE8F8EF)
val StatusPublishedFg = Color(0xFF15803D)
val StatusOfflineBg = Color(0xFFFEECEC)
val StatusOfflineFg = Color(0xFFB91C1C)

private val colors = lightColorScheme(
    primary = PromptBlue,
    onPrimary = Color.White,
    background = PromptBg,
    surface = Color.White,
    onSurface = PromptTextPrimary
)

@Composable
fun PromptTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}

fun statusColors(code: String?): Pair<Color, Color> = when (code) {
    "PRODUCING" -> StatusProducingBg to StatusProducingFg
    "PUBLISHED" -> StatusPublishedBg to StatusPublishedFg
    "OFFLINE" -> StatusOfflineBg to StatusOfflineFg
    else -> StatusDraftBg to StatusDraftFg
}

fun parseHexColor(hex: String?, fallback: Color): Color {
    val raw = hex?.trim().orEmpty()
    if (raw.isEmpty()) return fallback
    return try {
        val cleaned = raw.removePrefix("#")
        val value = when (cleaned.length) {
            6 -> cleaned.toLong(16) or 0xFF000000L
            8 -> cleaned.toLong(16)
            else -> return fallback
        }
        Color(value)
    } catch (_: Exception) {
        fallback
    }
}
