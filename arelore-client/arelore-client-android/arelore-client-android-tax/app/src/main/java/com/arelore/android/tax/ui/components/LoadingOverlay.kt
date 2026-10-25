package com.arelore.android.tax.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * Centered grey translucent rounded square + white iOS-style 12-bar spinner
 * (matches official 个税 App query loading).
 */
@Composable
fun LoadingOverlay(visible: Boolean) {
    if (!visible) return
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color(0x99000000), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            IosStyleSpinner(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun IosStyleSpinner(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "tax_loading")
    val frame by transition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_frame"
    )

    Canvas(modifier = modifier) {
        val barCount = 12
        val active = frame.toInt() % barCount
        val barWidth = size.minDimension * 0.10f
        val barHeight = size.minDimension * 0.28f
        val corner = CornerRadius(barWidth / 2f, barWidth / 2f)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension * 0.36f

        for (i in 0 until barCount) {
            // Trail: brightest near the "head", fading around the circle
            val distance = (i - active + barCount) % barCount
            val alpha = 1f - distance / barCount.toFloat()
            val angleDeg = i * (360f / barCount)
            rotate(degrees = angleDeg, pivot = Offset(cx, cy)) {
                drawRoundRect(
                    color = Color.White.copy(alpha = alpha.coerceIn(0.15f, 1f)),
                    topLeft = Offset(cx - barWidth / 2f, cy - radius - barHeight / 2f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner
                )
            }
        }
    }
}
