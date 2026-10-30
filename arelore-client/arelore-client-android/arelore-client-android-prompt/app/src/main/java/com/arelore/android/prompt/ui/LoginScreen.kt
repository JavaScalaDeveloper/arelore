package com.arelore.android.prompt.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.prompt.data.AuthApi
import com.arelore.android.prompt.data.PromptPrefs
import com.arelore.android.prompt.data.ServerEnv
import com.arelore.android.prompt.ui.theme.PromptTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    prefs: PromptPrefs,
    onLoggedIn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var account by remember { mutableStateOf(prefs.username) }
    var password by remember { mutableStateOf("") }
    var selectedEnv by remember { mutableStateOf(ServerEnv.fromId(prefs.envId)) }
    var customUrl by remember {
        mutableStateOf(
            prefs.customBaseUrl.ifBlank { PromptPrefs.DEFAULT_BASE }
        )
    }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun currentBase(): String =
        if (selectedEnv == ServerEnv.CUSTOM) {
            customUrl.trim().trimEnd('/')
        } else {
            selectedEnv.baseUrl
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Arelore 提示词", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "登录后可浏览与复制 AI 提示词",
            color = PromptTextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))

        Text("服务环境", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ServerEnv.LOCAL, ServerEnv.TEST, ServerEnv.PRD, ServerEnv.CUSTOM).forEach { env ->
                FilterChip(
                    selected = selectedEnv == env,
                    onClick = { selectedEnv = env },
                    label = { Text(env.label) }
                )
            }
        }
        Text(
            selectedEnv.hint,
            color = PromptTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (selectedEnv == ServerEnv.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customUrl,
                onValueChange = { customUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("API Base（含 /api）") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = account,
            onValueChange = { account = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("邮箱或用户名") },
            supportingText = { Text("含 @ 走邮箱登录，否则走账号登录") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (error != null) {
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    try {
                        prefs.applyEnv(selectedEnv, customUrl)
                        val base = currentBase()
                        require(base.isNotBlank()) { "请填写 API Base" }
                        val data = withContext(Dispatchers.IO) {
                            AuthApi(base).login(account, password)
                        }
                        prefs.saveSession(
                            token = data.token.orEmpty(),
                            nickname = data.user?.nickname,
                            username = data.user?.username ?: account.trim()
                        )
                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                        onLoggedIn()
                    } catch (e: Exception) {
                        error = e.message ?: "登录失败"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "登录中…" else "登录")
        }
    }
}
