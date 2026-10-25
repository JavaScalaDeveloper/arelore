package com.arelore.android.tax.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.ui.components.TaxTopBar
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary
import kotlin.math.abs

data class FontSizeOption(
    val label: String,
    val hint: String,
    val scale: Float
)

val FONT_SIZE_OPTIONS = listOf(
    FontSizeOption("较小", "适合信息密集页面", 0.90f),
    FontSizeOption("标准", "与 Redmi Note 15 录屏一致（默认）", 1.00f),
    FontSizeOption("较大", "阅读更轻松", 1.15f),
    FontSizeOption("最大", "关怀阅读", 1.30f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeScreen(
    currentScale: Float,
    onBack: () -> Unit,
    onSelect: (Float) -> Unit
) {
    Scaffold(
        topBar = { TaxTopBar(title = "字体大小", onBack = onBack) },
        containerColor = TaxBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "预览",
                fontSize = 14.sp,
                color = TaxTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    "工资薪金",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TaxTextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text("所得项目小类：正常工资薪金", fontSize = 15.sp, color = TaxTextSecondary)
                Text("收入：30608.70元", fontSize = 15.sp, color = TaxTextSecondary)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "选择字体大小",
                fontSize = 14.sp,
                color = TaxTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            ) {
                FONT_SIZE_OPTIONS.forEachIndexed { index, option ->
                    val selected = abs(currentScale - option.scale) < 0.001f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.scale) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(option.label, fontSize = 16.sp, color = TaxTextPrimary)
                            Text(option.hint, fontSize = 12.sp, color = TaxTextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (selected) TaxBlue else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (selected) TaxBlue else Color(0xFFCDD3DB),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    if (index < FONT_SIZE_OPTIONS.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xFFF0F2F5),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
