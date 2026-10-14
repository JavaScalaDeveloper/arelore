package com.arelore.android.tax.ui.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.data.INCOME_TYPES
import com.arelore.android.tax.ui.components.SectionTitle
import com.arelore.android.tax.ui.components.TaxTopBar
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxFilterScreen(
    defaultYear: Int = 2026,
    onBack: () -> Unit,
    onQuery: (year: Int, incomeType: String) -> Unit
) {
    var year by remember { mutableIntStateOf(defaultYear) }
    var selectedType by remember { mutableStateOf(INCOME_TYPES.first()) }
    var showOther by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TaxTopBar(title = "收入纳税明细", onBack = onBack) },
        containerColor = TaxBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SectionTitle("请选择纳税年度")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable {
                        year = if (year >= 2026) 2025 else year + 1
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("年度", color = TaxTextPrimary, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Text("$year", color = TaxTextSecondary, fontSize = 15.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TaxTextSecondary)
            }

            Spacer(Modifier.height(8.dp))
            SectionTitle("请选择所得类型")

            Column(modifier = Modifier.background(Color.White)) {
                INCOME_TYPES.forEachIndexed { index, type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (selectedType == type) TaxBlue else Color.Transparent)
                                .border(1.5.dp, if (selectedType == type) TaxBlue else Color(0xFFCDD3DB), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedType == type) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(type, fontSize = 15.sp, color = TaxTextPrimary)
                    }
                    if (index < INCOME_TYPES.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp), color = Color(0xFFF0F2F5))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOther = !showOther }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("其他类型", color = TaxBlue, fontSize = 14.sp)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TaxBlue, modifier = Modifier.size(18.dp))
            }

            if (showOther) {
                Text(
                    "演示版暂未开放其他所得类型",
                    color = TaxTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onQuery(year, selectedType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TaxBlue)
            ) {
                Text("查询", fontSize = 16.sp)
            }
        }
    }
}
