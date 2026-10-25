package com.arelore.android.tax.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import java.util.Locale

/** No thousand separator, always 2 decimal places — e.g. 111111.11 */
fun formatMoney(value: Double): String = String.format(Locale.US, "%.2f", value)

/**
 * Thin iOS-style left chevron, height aligned with 「返回」 text
 * (matches official 个税 App top-left back icon).
 */
@Composable
fun BackChevron(
    modifier: Modifier = Modifier,
    color: Color = TaxBlue,
    strokeWidth: Dp = 1.8.dp
) {
    ThinChevron(
        modifier = modifier.size(width = 11.dp, height = 18.dp),
        color = color,
        strokeWidth = strokeWidth,
        pointLeft = true
    )
}

/** Thin list-row chevron `>` matching official 个税 App. */
@Composable
fun ForwardChevron(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFC8C8C8),
    strokeWidth: Dp = 1.6.dp
) {
    ThinChevron(
        modifier = modifier.size(width = 8.dp, height = 14.dp),
        color = color,
        strokeWidth = strokeWidth,
        pointLeft = false
    )
}

@Composable
private fun ThinChevron(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
    pointLeft: Boolean
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val path = Path().apply {
            if (pointLeft) {
                moveTo(size.width * 0.78f, size.height * 0.12f)
                lineTo(size.width * 0.22f, size.height * 0.50f)
                lineTo(size.width * 0.78f, size.height * 0.88f)
            } else {
                moveTo(size.width * 0.22f, size.height * 0.12f)
                lineTo(size.width * 0.78f, size.height * 0.50f)
                lineTo(size.width * 0.22f, size.height * 0.88f)
            }
        }
        drawPath(path = path, color = color, style = stroke)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        // Status bar is painted black at Activity root; do not draw white into it
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TaxTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        },
        navigationIcon = {
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(start = 10.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackChevron()
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("返回", color = TaxBlue, fontSize = 16.sp)
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = TaxTextPrimary
        )
    )
}

@Composable
fun SectionTitle(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
        ) {
            drawRoundRect(color = TaxBlue, cornerRadius = CornerRadius(2.dp.toPx()))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TaxTextPrimary)
    }
}
