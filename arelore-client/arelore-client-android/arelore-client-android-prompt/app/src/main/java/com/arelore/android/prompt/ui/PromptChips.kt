package com.arelore.android.prompt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.prompt.data.PublishStatuses
import com.arelore.android.prompt.ui.theme.TagConfigBg
import com.arelore.android.prompt.ui.theme.TagConfigFg
import com.arelore.android.prompt.ui.theme.TagManualBg
import com.arelore.android.prompt.ui.theme.TagManualFg
import com.arelore.android.prompt.ui.theme.parseHexColor
import com.arelore.android.prompt.ui.theme.statusColors

@Composable
fun StatusBadge(code: String?) {
    val (bg, fg) = statusColors(code)
    Text(
        text = PublishStatuses.labelOf(code),
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun ConfigTagChip(name: String) {
    TagChip(name = name, bg = TagConfigBg, fg = TagConfigFg)
}

@Composable
fun ManualTagChip(name: String, colorHex: String? = null) {
    val fg = parseHexColor(colorHex, TagManualFg)
    val bg = if (colorHex.isNullOrBlank()) TagManualBg else fg.copy(alpha = 0.14f)
    TagChip(name = name, bg = bg, fg = fg)
}

@Composable
fun TagChip(
    name: String,
    bg: Color,
    fg: Color
) {
    Text(
        text = name,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    )
}
