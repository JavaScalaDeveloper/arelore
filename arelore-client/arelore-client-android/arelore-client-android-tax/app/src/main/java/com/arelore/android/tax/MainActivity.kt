package com.arelore.android.tax

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.arelore.android.tax.ui.tax.TaxFilterScreen
import com.arelore.android.tax.ui.tax.TaxListScreen
import com.arelore.android.tax.ui.theme.TaxBlue
import com.arelore.android.tax.ui.theme.TaxTextSecondary
import com.arelore.android.tax.ui.theme.TaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = TaxRepository(applicationContext)
        repo.ensureSeeded()
        setContent {
            TaxTheme {
                TaxApp(repo)
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
fun TaxApp(repo: TaxRepository) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    var incomeTick by remember { mutableIntStateOf(0) }

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
                            onOpenIncomeConfig = { navController.navigate("income_config") }
                        )
                        else -> PlaceholderTab(MainTab.entries[selectedTab].label)
                    }
                }
            }
        }

        composable("tax_filter") {
            TaxFilterScreen(
                onBack = { navController.popBackStack() },
                onQuery = { year, type ->
                    navController.navigate("tax_list/$year/${Uri.encode(type)}")
                }
            )
        }

        composable(
            route = "tax_list/{year}/{type}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { entry ->
            val year = entry.arguments?.getInt("year") ?: 2026
            val type = entry.arguments?.getString("type") ?: "工资薪金"
            val records = repo.records(year, type)
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
                onDelete = { y, m ->
                    repo.deleteMonthlyConfig(y, m)
                    incomeTick++
                }
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
