package com.arelore.android.prompt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arelore.android.prompt.data.PromptPrefs
import com.arelore.android.prompt.ui.LoginScreen
import com.arelore.android.prompt.ui.ProfileScreen
import com.arelore.android.prompt.ui.PromptListScreen
import com.arelore.android.prompt.ui.theme.PromptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PromptPrefs(applicationContext)
        setContent {
            PromptTheme {
                val promptPrefs = remember { prefs }
                var loggedIn by remember { mutableStateOf(promptPrefs.isLoggedIn) }

                if (!loggedIn) {
                    LoginScreen(
                        prefs = promptPrefs,
                        onLoggedIn = { loggedIn = true }
                    )
                } else {
                    val nav = rememberNavController()
                    val backStack by nav.currentBackStackEntryAsState()
                    val current = backStack?.destination?.route ?: "prompts"

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = current == "prompts",
                                    onClick = {
                                        if (current != "prompts") {
                                            nav.navigate("prompts") {
                                                popUpTo("prompts") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.List, contentDescription = "提示词") },
                                    label = { Text("提示词") }
                                )
                                NavigationBarItem(
                                    selected = current == "profile",
                                    onClick = {
                                        if (current != "profile") {
                                            nav.navigate("profile") {
                                                popUpTo("prompts") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                                    label = { Text("我的") }
                                )
                            }
                        }
                    ) { padding ->
                        NavHost(
                            navController = nav,
                            startDestination = "prompts",
                            modifier = Modifier.padding(padding)
                        ) {
                            composable("prompts") {
                                PromptListScreen(prefs = promptPrefs)
                            }
                            composable("profile") {
                                ProfileScreen(
                                    prefs = promptPrefs,
                                    onLoggedOut = { loggedIn = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
