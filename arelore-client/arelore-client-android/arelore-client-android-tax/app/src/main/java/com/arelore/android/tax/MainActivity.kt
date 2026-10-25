package com.arelore.android.tax

import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arelore.android.tax.data.TaxRepository
import com.arelore.android.tax.ui.home.HomeScreen
import com.arelore.android.tax.ui.income.IncomeConfigScreen
import com.arelore.android.tax.ui.mine.MineScreen
import com.arelore.android.tax.ui.settings.FONT_SIZE_OPTIONS
import com.arelore.android.tax.ui.settings.FontSizeScreen
import com.arelore.android.tax.ui.tax.TaxFilterScreen
import com.arelore.android.tax.ui.tax.TaxListScreen
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextSecondary
import com.arelore.android.tax.ui.theme.TaxTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // API 35+ ignores statusBarColor; paint black ourselves + light status icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.BLACK),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.WHITE, AndroidColor.WHITE)
        )
        val repo = TaxRepository(applicationContext)
        repo.ensureSeeded()
        setContent {
            var fontScale by remember { mutableFloatStateOf(repo.fontScale()) }
            TaxTheme(fontScale = fontScale) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Opaque black notification bar (matches official 个税 App)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(Color.Black)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .consumeWindowInsets(WindowInsets.statusBars)
                    ) {
                        TaxApp(
                            repo = repo,
                            fontScale = fontScale,
                            onFontScaleChange = {
                                repo.setFontScale(it)
                                fontScale = it
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    Home("首页", Icons.Default.Home),
    Todo("待办", Icons.Default.Assignment),
    Handle("办&查", Icons.Default.Description),
    Message("消息", Icons.Default.ChatBubbleOutline),
    Mine("我的", Icons.Default.Person)
}

@Composable
fun TaxApp(
    repo: TaxRepository,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    var incomeTick by remember { mutableIntStateOf(0) }
    val fontLabel = FONT_SIZE_OPTIONS
        .firstOrNull { abs(it.scale - fontScale) < 0.001f }
        ?.label
        ?: "标准"

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        MainTab.entries.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = {
                                    if (tab == MainTab.Message) {
                                        BadgedBox(badge = { Badge { Text("17") } }) {
                                            Icon(tab.icon, contentDescription = tab.label)
                                        }
                                    } else {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                },
                                label = { Text(tab.label, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TaxBlue,
                                    selectedTextColor = TaxBlue,
                                    unselectedIconColor = TaxTextSecondary,
                                    unselectedTextColor = TaxTextSecondary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (MainTab.entries[selectedTab]) {
                        MainTab.Home -> HomeScreen(
                            specialDeductionCount = repo.profile().specialDeductionCount,
                            onOpenTaxDetail = { navController.navigate("tax_filter") }
                        )
                        MainTab.Mine -> MineScreen(
                            profile = repo.profile(),
                            fontScaleLabel = fontLabel,
                            onOpenIncomeConfig = { navController.navigate("income_config") },
                            onOpenFontSize = { navController.navigate("font_size") }
                        )
                        else -> PlaceholderTab(MainTab.entries[selectedTab].label)
                    }
                }
            }
        }

        composable("tax_filter") {
            TaxFilterScreen(
                onBack = { navController.popBackStack() },
                onQuery = { year, types ->
                    val joined = types.joinToString(",")
                    navController.navigate("tax_list/$year/${Uri.encode(joined)}") {
                        popUpTo("tax_filter") { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = "tax_list/{year}/{types}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("types") { type = NavType.StringType }
            )
        ) { entry ->
            val year = entry.arguments?.getInt("year") ?: 2025
            val types = (entry.arguments?.getString("types") ?: "工资薪金")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val records = repo.records(year, types)
            val (income, tax) = repo.summary(records)
            TaxListScreen(
                records = records,
                totalIncome = income,
                totalTax = tax,
                onBack = { navController.popBackStack() }
            )
        }

        composable("income_config") {
            @Suppress("UNUSED_EXPRESSION")
            incomeTick
            IncomeConfigScreen(
                configs = repo.allMonthlyConfigs(),
                onBack = { navController.popBackStack() },
                onSave = {
                    repo.saveMonthlyConfig(it)
                    incomeTick++
                },
                onDelete = { id ->
                    repo.deleteMonthlyConfig(id)
                    incomeTick++
                }
            )
        }

        composable("font_size") {
            FontSizeScreen(
                currentScale = fontScale,
                onBack = { navController.popBackStack() },
                onSelect = onFontScaleChange
            )
        }
    }
}

@Composable
private fun PlaceholderTab(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("$name（演示占位）", color = TaxTextSecondary)
    }
}
