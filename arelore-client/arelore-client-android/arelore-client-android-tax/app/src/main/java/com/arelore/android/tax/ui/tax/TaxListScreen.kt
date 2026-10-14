package com.arelore.android.tax.ui.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.data.IncomeRecord
import com.arelore.android.tax.ui.components.TaxTopBar
import com.arelore.android.tax.ui.components.formatMoney
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxListScreen(
    records: List<IncomeRecord>,
    totalIncome: Double,
    totalTax: Double,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TaxTopBar(
                title = "收入纳税明细",
                onBack = onBack,
                actions = {
                    TextButton(onClick = {}) {
                        Text("批量申诉", color = TaxBlue, fontSize = 14.sp)
                    }
                }
            )
        },
        containerColor = TaxBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7FA))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SummaryRow("收入合计", totalIncome, showInfo = true)
                Spacer(Modifier.height(6.dp))
                SummaryRow("已申报税额合计", totalTax)
            }

            if (records.isEmpty()) {
                Text(
                    "暂无明细，请先在「我的 → 收入配置」中添加工资",
                    color = TaxTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(records, key = { it.id }) { record ->
                        RecordItem(record)
                        HorizontalDivider(color = Color(0xFFEEF1F5))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, showInfo: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TaxTextSecondary, fontSize = 14.sp)
        if (showInfo) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = TaxTextSecondary,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .height(14.dp)
            )
            Text(":", color = TaxTextSecondary, fontSize = 14.sp)
        } else {
            Text(":", color = TaxTextSecondary, fontSize = 14.sp)
        }
        Spacer(Modifier.weight(1f))
        Text("${formatMoney(amount)}元", color = TaxTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecordItem(record: IncomeRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(record.incomeType, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TaxTextPrimary)
            Spacer(Modifier.weight(1f))
            Text(record.period, fontSize = 14.sp, color = TaxTextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        Text("所得项目小类：${record.subType}", fontSize = 13.sp, color = TaxTextSecondary)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "扣缴义务人：${truncate(record.agent, 18)}",
                fontSize = 13.sp,
                color = TaxTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TaxTextSecondary)
        }
        Spacer(Modifier.height(4.dp))
        Text("收入：${formatMoney(record.income)}元", fontSize = 13.sp, color = TaxTextSecondary)
        Spacer(Modifier.height(4.dp))
        Text("已申报税额：${formatMoney(record.tax)}元", fontSize = 13.sp, color = TaxTextSecondary)
    }
}

private fun truncate(text: String, max: Int): String =
    if (text.length <= max) text else text.take(max) + "..."
