package com.arelore.android.tax.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val TaxBlue = Color(0xFF1B6BDE)
val TaxBlueLight = Color(0xFF4B8EF5)
val TaxBg = Color(0xFFF2F5F9)
val TaxOrange = Color(0xFFFF8A00)
val TaxTextSecondary = Color(0xFF666666)
val TaxTextPrimary = Color(0xFF1A1A1A)
val TaxTextMuted = Color(0xFF999999)
val TaxDivider = Color(0xFFEEF1F5)

val LocalAppFontScale = staticCompositionLocalOf { 1.0f }

private val colorScheme = lightColorScheme(
    primary = TaxBlue,
    onPrimary = Color.White,
    secondary = TaxBlueLight,
    background = TaxBg,
    surface = Color.White,
    onSurface = TaxTextPrimary,
    outline = Color(0xFFE5E8ED)
)

/**
 * Baseline sp sizes are tuned for Redmi Note 15 screenshots.
 * [fontScale] multiplies text via Density.fontScale (标准 = 1.0).
 */
@Composable
fun TaxTheme(
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalAppFontScale provides fontScale,
        LocalDensity provides Density(
            density = density.density,
            fontScale = fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
