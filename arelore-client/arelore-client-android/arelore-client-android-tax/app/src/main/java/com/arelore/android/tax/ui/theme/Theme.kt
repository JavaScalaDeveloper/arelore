package com.arelore.android.tax.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TaxBlue = Color(0xFF1B6BDE)
val TaxBlueLight = Color(0xFF4B8EF5)
val TaxBg = Color(0xFFF2F5F9)
val TaxOrange = Color(0xFFFF8A00)
val TaxTextSecondary = Color(0xFF8A9199)
val TaxTextPrimary = Color(0xFF1A1A1A)

private val colorScheme = lightColorScheme(
    primary = TaxBlue,
    onPrimary = Color.White,
    secondary = TaxBlueLight,
    background = TaxBg,
    surface = Color.White,
    onSurface = TaxTextPrimary,
    outline = Color(0xFFE5E8ED)
)

@Composable
fun TaxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
