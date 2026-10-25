package com.arelore.android.tax.ui.mine

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.tax.data.Profile
import com.arelore.android.tax.ui.theme.TaxBg
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextPrimary
import com.arelore.android.tax.ui.theme.TaxTextSecondary

@Composable
fun MineScreen(
    profile: Profile,
    fontScaleLabel: String,
    onOpenIncomeConfig: () -> Unit,
    onOpenFontSize: () -> Unit
) {
    var maskId by remember { mutableStateOf(false) }
    val displayId = if (maskId) {
        profile.taxpayerId.take(4) + "**********" + profile.taxpayerId.takeLast(4)
    } else {
        profile.taxpayerId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TaxBg)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF3B8EF0), Color(0xFF1B6BDE)))
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TaxBlue, modifier = Modifier.size(44.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-36).dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFFFB347), Color(0xFFFFD180)))
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TaxTextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("纳税人识别号 $displayId", fontSize = 12.sp, color = Color(0xFF5A4630))
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF5A4630),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { maskId = !maskId }
                            )
                        }
                    }
                    Text(
                        "个人信息 >",
                        color = Color(0xFF5A4630),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.55f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickItem(Icons.Default.Home, Color(0xFFFF6B6B), "家庭成员", "添加") {}
                QuickItem(Icons.Default.Work, Color(0xFFFF8A00), "任职受雇", "${profile.employerCount}家") {}
                QuickItem(Icons.Default.AccountBalanceWallet, Color(0xFF20B2AA), "银行卡", "${profile.bankCardCount}张") {}
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-8).dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                MenuRow(Icons.Default.AccountBalanceWallet, "收入配置（模拟）", onClick = onOpenIncomeConfig)
                HorizontalDivider(color = Color(0xFFF0F2F5))
                MenuRow(
                    Icons.Default.FormatSize,
                    "字体大小",
                    trailing = fontScaleLabel,
                    onClick = onOpenFontSize
                )
                HorizontalDivider(color = Color(0xFFF0F2F5))
                MenuRow(Icons.Default.FavoriteBorder, "切换关怀版")
                HorizontalDivider(color = Color(0xFFF0F2F5))
                MenuRow(Icons.Default.Security, "安全中心")
                HorizontalDivider(color = Color(0xFFF0F2F5))
                MenuRow(Icons.Default.Lightbulb, "帮助中心")
                HorizontalDivider(color = Color(0xFFF0F2F5))
                MenuRow(Icons.Default.HeadsetMic, "我要咨询")
                HorizontalDivider(color = Color(0xFFF0F2F5))
                MenuRow(Icons.Default.Info, "关于&更新")
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TaxBlue)
        ) {
            Text("退出登录", fontSize = 16.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QuickItem(icon: ImageVector, tint: Color, title: String, action: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 13.sp, color = TaxTextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            action,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(TaxBlue)
                .padding(horizontal = 12.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TaxTextPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp, color = TaxTextPrimary)
        if (trailing != null) {
            Text(trailing, fontSize = 14.sp, color = TaxTextSecondary)
            Spacer(Modifier.width(4.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TaxTextSecondary)
    }
}
