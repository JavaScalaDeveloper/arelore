package com.arelore.android.tax.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxOrange
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary

@Composable
fun HomeScreen(
    specialDeductionCount: Int,
    onOpenTaxDetail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TaxBg)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF5BA3F5), Color(0xFF2E7FE0)))
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TaxTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "请输入想搜索的功能/服务",
                    color = TaxTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TaxTextSecondary, modifier = Modifier.size(20.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF8E8))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TaxOrange, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "税务总局关于2025年度个人所得税综合所得汇算清缴的公告",
                color = Color(0xFF8A6A2A),
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F2FF)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("进入专题页 >", color = TaxBlue, fontSize = 12.sp)
                }
                Text("2025综合所得年度汇算", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TaxTextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("您已提交申报并完成退税", color = TaxTextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaxBlue),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("查看记录")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "重点服务推荐",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TaxTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFFF4D4F), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("专项附加扣除", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Text("相关政策 >", color = TaxBlue, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    buildAnnotatedString {
                        append("您已填报 ")
                        withStyle(SpanStyle(color = TaxOrange, fontSize = 22.sp, fontWeight = FontWeight.Bold)) {
                            append("$specialDeductionCount")
                        }
                        append(" 项专项附加扣除")
                    },
                    color = TaxTextPrimary,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaxBlue)
                    ) { Text("填报") }
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaxOrange)
                    ) { Text("我要查询") }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ServiceIcon(Icons.Default.Calculate, Color(0xFF3B82F6), "综合所得年度汇算", "申报与查询综合所得...") {}
            ServiceIcon(Icons.Default.Wallet, Color(0xFF8B5CF6), "收入纳税明细", "查看个人所得税纳税...") { onOpenTaxDetail() }
            ServiceIcon(Icons.Default.Description, Color(0xFF06B6D4), "纳税记录开具", "生成或查看纳税记录") {}
            ServiceIcon(Icons.Default.GridView, Color(0xFF3B82F6), "更多服务", "设置在主页显示的常用...") {}
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ServiceIcon(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TaxTextPrimary, maxLines = 2)
        Text(subtitle, fontSize = 10.sp, color = TaxTextSecondary, maxLines = 1)
    }
}
