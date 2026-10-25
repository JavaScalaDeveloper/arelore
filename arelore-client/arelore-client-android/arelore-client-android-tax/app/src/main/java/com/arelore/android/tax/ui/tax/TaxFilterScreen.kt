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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.data.INCOME_TYPES
import com.arelore.android.tax.ui.components.LoadingOverlay
import com.arelore.android.tax.ui.components.SectionTitle
import com.arelore.android.tax.ui.components.TaxTopBar
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TAX_YEARS = (2020..2026).toList().reversed()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxFilterScreen(
    defaultYear: Int = 2023,
    onBack: () -> Unit,
    onQuery: (year: Int, incomeTypes: List<String>) -> Unit
) {
    var year by remember { mutableIntStateOf(defaultYear) }
    var selectedTypes by remember { mutableStateOf(setOf(INCOME_TYPES.first())) }
    var showOther by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val yearSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
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
                        .clickable(enabled = !loading) { showYearPicker = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("年度", color = TaxTextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Text("$year", color = TaxTextSecondary, fontSize = 16.sp)
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TaxTextSecondary
                    )
                }

                Spacer(Modifier.height(8.dp))
                SectionTitle("请选择所得类型")

                Column(modifier = Modifier.background(Color.White)) {
                    INCOME_TYPES.forEachIndexed { index, type ->
                        val checked = type in selectedTypes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !loading) {
                                    selectedTypes = if (checked) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes + type
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (checked) TaxBlue else Color.Transparent)
                                    .border(
                                        1.5.dp,
                                        if (checked) TaxBlue else Color(0xFFCDD3DB),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(type, fontSize = 16.sp, color = TaxTextPrimary)
                        }
                        if (index < INCOME_TYPES.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 50.dp),
                                color = Color(0xFFF0F2F5)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !loading) { showOther = !showOther }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("其他类型", color = TaxBlue, fontSize = 15.sp)
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TaxBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showOther) {
                    Text(
                        "演示版暂未开放其他所得类型",
                        color = TaxTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        if (loading || selectedTypes.isEmpty()) return@Button
                        loading = true
                        scope.launch {
                            delay(750)
                            onQuery(year, selectedTypes.toList())
                        }
                    },
                    enabled = !loading && selectedTypes.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaxBlue)
                ) {
                    Text("查询", fontSize = 17.sp)
                }
            }
        }

        LoadingOverlay(visible = loading)

        if (showYearPicker) {
            ModalBottomSheet(
                onDismissRequest = { showYearPicker = false },
                sheetState = yearSheetState,
                containerColor = Color.White
            ) {
                Text(
                    "选择纳税年度",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TaxTextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn {
                    items(TAX_YEARS) { y ->
                        val selected = y == year
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    year = y
                                    showYearPicker = false
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$y",
                                fontSize = 16.sp,
                                color = if (selected) TaxBlue else TaxTextPrimary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TaxBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F2F5))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
