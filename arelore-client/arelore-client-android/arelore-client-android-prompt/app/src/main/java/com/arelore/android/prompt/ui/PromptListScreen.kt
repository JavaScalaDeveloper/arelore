package com.arelore.android.prompt.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.prompt.data.AiPromptItem
import com.arelore.android.prompt.data.PromptApi
import com.arelore.android.prompt.data.PromptPrefs
import com.arelore.android.prompt.ui.theme.PromptTextPrimary
import com.arelore.android.prompt.ui.theme.PromptTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptListScreen(
    prefs: PromptPrefs,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var keyword by remember { mutableStateOf("") }
    var publishStatus by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<AiPromptItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                val api = PromptApi(prefs.apiBaseUrl) { prefs.token }
                val page = withContext(Dispatchers.IO) {
                    api.page(1, 50, keyword.ifBlank { null }, publishStatus, null)
                }
                items = page.list.orEmpty()
            } catch (e: Exception) {
                error = e.message ?: "加载失败"
                items = emptyList()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 提示词") },
                actions = {
                    TextButton(onClick = onOpenSettings) { Text("设置") }
                    TextButton(onClick = { refresh() }) { Text(if (loading) "…" else "刷新") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("搜索标题") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "全部", "PUBLISHED" to "已发布", "DRAFT" to "草稿", "OFFLINE" to "已下架")
                    .forEach { (value, label) ->
                        FilterChip(
                            selected = publishStatus == value,
                            onClick = {
                                publishStatus = value
                                refresh()
                            },
                            label = { Text(label) }
                        )
                    }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { refresh() }, modifier = Modifier.fillMaxWidth()) {
                Text("查询")
            }
            if (error != null) {
                Text(error!!, color = PromptTextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id ?: it.promptUuid ?: it.title.orEmpty() }) { item ->
                    PromptCard(
                        item = item,
                        onCopyPrompt = {
                            copyToClipboard(context, item.promptText.orEmpty(), "已复制提示词")
                        },
                        onCopyAll = {
                            val text = buildString {
                                append("标题：").append(item.title.orEmpty()).append('\n')
                                append("简述：").append(item.summary.orEmpty()).append('\n')
                                append("配置标签：").append(item.configTagList.orEmpty().joinToString("、")).append('\n')
                                append("人工标签：").append(item.manualTagList.orEmpty().joinToString("、")).append('\n')
                                append("提示词：\n").append(item.promptText.orEmpty())
                            }
                            copyToClipboard(context, text, "已复制全部")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptCard(
    item: AiPromptItem,
    onCopyPrompt: () -> Unit,
    onCopyAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.title.orEmpty().ifBlank { "(无标题)" },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = PromptTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!item.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.summary, color = PromptTextSecondary, fontSize = 13.sp, maxLines = 2)
            }
            val tags = item.displayTags()
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(tags.joinToString(" · "), color = PromptTextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.promptText.orEmpty(),
                color = PromptTextSecondary,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onCopyPrompt)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCopyPrompt) { Text("复制提示词") }
                TextButton(onClick = onCopyAll) { Text("复制全部") }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toast: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("ai_prompt", text))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}
