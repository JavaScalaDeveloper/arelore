package com.arelore.android.prompt.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.prompt.data.AuthApi
import com.arelore.android.prompt.data.CopyField
import com.arelore.android.prompt.data.PromptApi
import com.arelore.android.prompt.data.PromptClientPref
import com.arelore.android.prompt.data.PromptPrefs
import com.arelore.android.prompt.data.ServerEnv
import com.arelore.android.prompt.ui.theme.PromptTextPrimary
import com.arelore.android.prompt.ui.theme.PromptTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FONT_SCALE_OPTIONS = listOf(
    0.85 to "小",
    1.00 to "标准",
    1.15 to "大",
    1.30 to "更大"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    prefs: PromptPrefs,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var settingsOpen by remember { mutableStateOf(false) }
    var loggingOut by remember { mutableStateOf(false) }

    val displayName = prefs.nickname.ifBlank { prefs.username }.ifBlank { "已登录用户" }
    val envLabel = ServerEnv.fromId(prefs.envId).label
    val fontLabel = FONT_SCALE_OPTIONS
        .firstOrNull { kotlin.math.abs(it.first - prefs.clientPref.fontScale) < 0.001 }
        ?.second
        ?: "自定义"
    val copyCount = prefs.clientPref.copyFields.size

    if (settingsOpen) {
        ProfileSettingsScreen(
            prefs = prefs,
            onBack = { settingsOpen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("个人中心") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PromptTextPrimary
                    )
                    if (prefs.username.isNotBlank() && prefs.username != displayName) {
                        Text(prefs.username, color = PromptTextSecondary, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "环境：$envLabel · 字号：$fontLabel · 复制项：$copyCount",
                        color = PromptTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsOpen = true },
                colors = CardDefaults.cardColors()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = PromptTextPrimary
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text("设置", fontWeight = FontWeight.Medium, color = PromptTextPrimary)
                        Text(
                            "字号、复制内容、服务端环境",
                            color = PromptTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "打开设置",
                        tint = PromptTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        loggingOut = true
                        try {
                            withContext(Dispatchers.IO) {
                                AuthApi(prefs.apiBaseUrl) { prefs.token }.logout()
                            }
                        } catch (_: Exception) {
                        } finally {
                            prefs.clearSession()
                            loggingOut = false
                            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                            onLoggedOut()
                        }
                    }
                },
                enabled = !loggingOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text(if (loggingOut) "退出中…" else "退出登录")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileSettingsScreen(
    prefs: PromptPrefs,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedEnv by remember { mutableStateOf(ServerEnv.fromId(prefs.envId)) }
    var customUrl by remember {
        mutableStateOf(prefs.customBaseUrl.ifBlank { PromptPrefs.DEFAULT_BASE })
    }
    var savingEnv by remember { mutableStateOf(false) }
    var savingPref by remember { mutableStateOf(false) }
    var loadingPref by remember { mutableStateOf(false) }

    var fontScale by remember { mutableStateOf(prefs.clientPref.fontScale) }
    var fontSizeSp by remember { mutableStateOf(prefs.clientPref.fontSizeSp) }
    var copyFields by remember { mutableStateOf(prefs.clientPref.copyFields.toSet()) }
    var copySep by remember {
        mutableStateOf(if (prefs.clientPref.copySep == "\n\n") "BLANK" else "LF")
    }

    fun api() = PromptApi(prefs.apiBaseUrl) { prefs.token }

    fun applyLocalPref(pref: PromptClientPref) {
        fontScale = pref.fontScale
        fontSizeSp = pref.fontSizeSp
        copyFields = pref.copyFields.toSet()
        copySep = if (pref.copySep == "\n\n") "BLANK" else "LF"
        prefs.clientPref = pref
    }

    LaunchedEffect(Unit) {
        loadingPref = true
        try {
            val remote = withContext(Dispatchers.IO) { api().getPref() }
            applyLocalPref(remote)
        } catch (_: Exception) {
        } finally {
            loadingPref = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("显示字号", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                if (loadingPref) "正在同步云端偏好…" else "影响列表标题与正文；保存后同步到服务端",
                color = PromptTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FONT_SCALE_OPTIONS.forEach { (scale, label) ->
                    FilterChip(
                        selected = kotlin.math.abs(fontScale - scale) < 0.001,
                        onClick = {
                            fontScale = scale
                            fontSizeSp = (14 * scale).toInt().coerceIn(12, 20)
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("复制内容", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                "「复制全部」按所选字段顺序拼接；至少保留一项",
                color = PromptTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CopyField.OPTIONS.forEach { (code, label) ->
                    FilterChip(
                        selected = copyFields.contains(code),
                        onClick = {
                            copyFields = copyFields.toMutableSet().also { set ->
                                if (set.contains(code)) {
                                    if (set.size > 1) set.remove(code)
                                } else {
                                    set.add(code)
                                }
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("字段分隔", color = PromptTextSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = copySep == "LF",
                    onClick = { copySep = "LF" },
                    label = { Text("换行") }
                )
                FilterChip(
                    selected = copySep == "BLANK",
                    onClick = { copySep = "BLANK" },
                    label = { Text("空行") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        savingPref = true
                        try {
                            val payload = PromptClientPref(
                                fontScale = fontScale,
                                fontSizeSp = fontSizeSp,
                                copyFields = CopyField.OPTIONS.map { it.first }
                                    .filter { copyFields.contains(it) }
                                    .ifEmpty { listOf(CopyField.PROMPT_TEXT) },
                                copySep = if (copySep == "BLANK") "\n\n" else "\n"
                            )
                            val saved = withContext(Dispatchers.IO) { api().savePref(payload) }
                            applyLocalPref(saved)
                            Toast.makeText(context, "偏好已保存", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                        } finally {
                            savingPref = false
                        }
                    }
                },
                enabled = !savingPref,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (savingPref) "保存中…" else "保存显示与复制偏好")
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("服务端环境", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                "切换后立即生效",
                color = PromptTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("自定义 API Base（含 /api）") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (selectedEnv == ServerEnv.CUSTOM && customUrl.trim().isEmpty()) {
                        Toast.makeText(context, "请填写自定义地址", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    savingEnv = true
                    prefs.applyEnv(selectedEnv, customUrl)
                    savingEnv = false
                    Toast.makeText(context, "已切换到 ${selectedEnv.label}", Toast.LENGTH_SHORT).show()
                },
                enabled = !savingEnv,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (savingEnv) "保存中…" else "保存环境")
            }
        }
    }
}
