package com.arelore.android.prompt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arelore.android.prompt.data.PromptPrefs
import com.arelore.android.prompt.ui.PromptListScreen
import com.arelore.android.prompt.ui.SettingsScreen
import com.arelore.android.prompt.ui.theme.PromptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PromptPrefs(applicationContext)
        setContent {
            PromptTheme {
                val nav = rememberNavController()
                val promptPrefs = remember { prefs }
                NavHost(navController = nav, startDestination = "list") {
                    composable("list") {
                        PromptListScreen(
                            prefs = promptPrefs,
                            onOpenSettings = { nav.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            prefs = promptPrefs,
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
