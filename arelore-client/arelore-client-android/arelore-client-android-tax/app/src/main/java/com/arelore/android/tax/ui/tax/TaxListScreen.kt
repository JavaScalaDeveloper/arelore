package com.arelore.android.tax.ui.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.data.IncomeRecord
import com.arelore.android.tax.ui.components.ForwardChevron
import com.arelore.android.tax.ui.components.TaxTopBar
import com.arelore.android.tax.ui.components.formatMoney
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary

/** Light lavender-grey band matching 个税 App list gaps. */
private val GapBg = Color(0xFFEFF1F5)
private val Hairline = Color(0xFFE6E8ED)

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
            Column {
                TaxTopBar(
                    title = "收入纳税明细",
                    onBack = onBack,
                    actions = {
                        TextButton(onClick = {}) {
                            Text("批量申诉", color = TaxBlue, fontSize = 16.sp)
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Hairline)
            }
        },
        containerColor = GapBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GreyGap(heightDp = 8)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                SummaryRow(
                    label = "收入合计",
                    amount = totalIncome,
                    showInfo = true
                )
                // Thin grey interval between the two summary rows (full width)
                HorizontalDivider(thickness = 0.5.dp, color = Hairline)
                SummaryRow(
                    label = "已申报税额合计",
                    amount = totalTax,
                    showInfo = false
                )
            }

            GreyGap(heightDp = 10)

            if (records.isEmpty()) {
                Text(
                    "暂无明细，请先在「我的 → 收入配置」中添加工资",
                    color = TaxTextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(records, key = { it.id }) { record ->
                        RecordItem(record)
                        GreyGap(heightDp = 8)
                    }
                }
            }
        }
    }
}

@Composable
private fun GreyGap(heightDp: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(GapBg)
    )
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    showInfo: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TaxTextPrimary, fontSize = 15.sp)
        if (showInfo) {
            Spacer(modifier = Modifier.width(4.dp))
            InfoDot()
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text("：", color = TaxTextPrimary, fontSize = 15.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${formatMoney(amount)}元",
            color = TaxTextPrimary,
            fontSize = 15.sp
        )
    }
}

/** Light-blue outline circle + "?" matching official 个税 App. */
@Composable
private fun InfoDot() {
    Box(
        modifier = Modifier
            .size(14.dp)
            .border(width = 1.2.dp, color = TaxBlue, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "?",
            color = TaxBlue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 10.sp
        )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.incomeType,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TaxTextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = record.period,
                fontSize = 15.sp,
                color = TaxTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "所得项目小类：${record.subType}",
            fontSize = 15.sp,
            color = TaxTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "扣缴义务人：${record.agent}",
                fontSize = 15.sp,
                color = TaxTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            ForwardChevron()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "收入：${formatMoney(record.income)}元",
            fontSize = 15.sp,
            color = TaxTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "已申报税额：${formatMoney(record.tax)}元",
            fontSize = 15.sp,
            color = TaxTextSecondary
        )
    }
}
