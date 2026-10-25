package com.arelore.android.prompt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PromptBlue = Color(0xFF1B6BDE)
val PromptBg = Color(0xFFF5F7FA)
val PromptTextPrimary = Color(0xFF1A1A1A)
val PromptTextSecondary = Color(0xFF666666)

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
