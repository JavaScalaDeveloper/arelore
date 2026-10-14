package com.arelore.android.tax.ui.income

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.data.DEFAULT_AGENT
import com.arelore.android.tax.data.MonthlyIncomeConfig
import com.arelore.android.tax.ui.components.TaxTopBar
import com.arelore.android.tax.ui.components.formatMoney
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeConfigScreen(
    configs: List<MonthlyIncomeConfig>,
    onBack: () -> Unit,
    onSave: (MonthlyIncomeConfig) -> Unit,
    onDelete: (year: Int, month: Int) -> Unit
) {
    var year by remember { mutableStateOf("2026") }
    var month by remember { mutableStateOf("10") }
    var amount by remember { mutableStateOf("30000") }
    var agent by remember { mutableStateOf(DEFAULT_AGENT) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TaxTopBar(title = "收入配置", onBack = onBack) },
        containerColor = TaxBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("添加工资薪金", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TaxTextPrimary)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it.filter(Char::isDigit).take(4) },
                            label = { Text("年度") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = month,
                            onValueChange = { month = it.filter(Char::isDigit).take(2) },
                            label = { Text("月份") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("收入金额（元）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = agent,
                        onValueChange = { agent = it },
                        label = { Text("扣缴义务人") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = Color(0xFFD93025), fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val y = year.toIntOrNull()
                            val m = month.toIntOrNull()
                            val a = amount.toDoubleOrNull()
                            when {
                                y == null || y < 2000 -> error = "请输入有效年度"
                                m == null || m !in 1..12 -> error = "月份须为 1–12"
                                a == null || a < 0 -> error = "请输入有效金额"
                                agent.isBlank() -> error = "请填写扣缴义务人"
                                else -> {
                                    error = null
                                    onSave(MonthlyIncomeConfig(y, m, a, agent.trim()))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TaxBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("保存")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("已配置月份", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TaxTextPrimary)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(configs, key = { "${it.year}-${it.month}" }) { cfg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cfg.period, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Text(cfg.agent, color = TaxTextSecondary, fontSize = 12.sp, maxLines = 1)
                                Text("${formatMoney(cfg.amount)}元", color = TaxTextPrimary, fontSize = 14.sp)
                            }
                            IconButton(onClick = { onDelete(cfg.year, cfg.month) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFD93025))
                            }
                        }
                    }
                }
            }
        }
    }
}
