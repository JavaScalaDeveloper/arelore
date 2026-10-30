package com.arelore.android.prompt.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arelore.android.prompt.data.AiPromptItem
import com.arelore.android.prompt.data.AiPromptTag
import com.arelore.android.prompt.data.PromptApi
import com.arelore.android.prompt.data.PromptPrefs
import com.arelore.android.prompt.data.PublishStatuses
import com.arelore.android.prompt.data.buildCopyText
import com.arelore.android.prompt.ui.theme.PromptTextPrimary
import com.arelore.android.prompt.ui.theme.PromptTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PromptListScreen(
    prefs: PromptPrefs
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var keyword by remember { mutableStateOf("") }
    var publishStatus by remember { mutableStateOf<String?>(null) }
    var tagName by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<AiPromptItem>>(emptyList()) }
    var tags by remember { mutableStateOf<List<AiPromptTag>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var detailItem by remember { mutableStateOf<AiPromptItem?>(null) }
    var tagEditItem by remember { mutableStateOf<AiPromptItem?>(null) }
    var statusEditItem by remember { mutableStateOf<AiPromptItem?>(null) }
    var saving by remember { mutableStateOf(false) }
    var tagDialogLoading by remember { mutableStateOf(false) }
    var statusFilterExpanded by remember { mutableStateOf(false) }
    var tagFilterExpanded by remember { mutableStateOf(false) }
    var filtersVisible by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var clientPref by remember { mutableStateOf(prefs.clientPref) }

    // 下滑隐藏查询条件，上滑（或回到顶部）展开，腾出列表空间
    LaunchedEffect(listState) {
        var lastIndex = 0
        var lastOffset = 0
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                val scrollingDown =
                    index > lastIndex || (index == lastIndex && offset > lastOffset + 10)
                val scrollingUp =
                    index < lastIndex || (index == lastIndex && offset < lastOffset - 10)
                when {
                    index == 0 && offset < 8 -> filtersVisible = true
                    scrollingDown -> {
                        filtersVisible = false
                        statusFilterExpanded = false
                        tagFilterExpanded = false
                    }
                    scrollingUp -> filtersVisible = true
                }
                lastIndex = index
                lastOffset = offset
            }
    }

    fun api() = PromptApi(prefs.apiBaseUrl) { prefs.token }

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                val page = withContext(Dispatchers.IO) {
                    api().page(1, 50, keyword.ifBlank { null }, publishStatus, tagName)
                }
                items = page.items()
            } catch (e: Exception) {
                error = e.message ?: "加载失败"
                items = emptyList()
            } finally {
                loading = false
            }
        }
    }

    suspend fun fetchTags(): List<AiPromptTag> =
        withContext(Dispatchers.IO) { api().tagList() }
            .filter { it.status != "DELETED" && !it.name.isNullOrBlank() }

    fun loadTags(showError: Boolean = false) {
        scope.launch {
            try {
                tags = fetchTags()
            } catch (e: Exception) {
                if (showError) {
                    Toast.makeText(context, e.message ?: "加载标签失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun patchSave(row: AiPromptItem, patch: AiPromptItem, okTip: String) {
        if (row.id == null) {
            Toast.makeText(context, "缺少 id", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            saving = true
            try {
                val saved = withContext(Dispatchers.IO) {
                    api().save(
                        row.copy(
                            manualTagList = patch.manualTagList ?: row.resolvedManualTags(),
                            publishStatus = patch.publishStatus ?: row.publishStatus,
                            configTagList = row.resolvedConfigTags(),
                            title = row.title,
                            summary = row.summary,
                            promptText = row.promptText
                        )
                    )
                }
                items = items.map { if (it.id == row.id) mergeSaved(it, saved) else it }
                Toast.makeText(context, okTip, Toast.LENGTH_SHORT).show()
                loadTags()
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "保存失败", Toast.LENGTH_SHORT).show()
            } finally {
                saving = false
            }
        }
    }

    fun openDetail(row: AiPromptItem) {
        scope.launch {
            try {
                val full = if (row.id != null) {
                    withContext(Dispatchers.IO) { api().detail(row.id) }
                } else {
                    row
                }
                detailItem = full
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "加载详情失败", Toast.LENGTH_SHORT).show()
                detailItem = row
            }
        }
    }

    fun openTagEdit(row: AiPromptItem) {
        tagEditItem = row
        scope.launch {
            tagDialogLoading = true
            try {
                tags = fetchTags()
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "加载标签失败", Toast.LENGTH_SHORT).show()
            } finally {
                tagDialogLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
        loadTags(showError = true)
        scope.launch {
            try {
                val remote = withContext(Dispatchers.IO) { api().getPref() }
                clientPref = remote
                prefs.clientPref = remote
            } catch (_: Exception) {
            }
            try {
                withContext(Dispatchers.IO) { api().reportVisit("ANDROID") }
            } catch (_: Exception) {
            }
        }
    }

    val titleSp = (clientPref.fontSizeSp + 3).coerceIn(14, 24).sp
    val bodySp = clientPref.fontSizeSp.coerceIn(12, 20).sp

    val tagColorMap = remember(tags) {
        tags.mapNotNull { t ->
            val name = t.name?.trim().orEmpty()
            if (name.isEmpty()) null else name to t.color
        }.toMap()
    }

    val tagFilterOptions = remember(tags, items) {
        val set = LinkedHashSet<String>()
        tags.mapNotNull { it.name?.trim() }.filter { it.isNotEmpty() }.forEach { set.add(it) }
        items.flatMap { it.displayTags() }.forEach { set.add(it) }
        set.toList().sorted()
    }

    val manualTagOptions = remember(tags, items, tagEditItem) {
        buildManualTagOptions(tags, items, tagEditItem)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 提示词") },
                actions = {
                    TextButton(onClick = {
                        refresh()
                        loadTags()
                    }) { Text(if (loading) "…" else "刷新") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            AnimatedVisibility(
                visible = filtersVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "标题",
                            color = PromptTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = { keyword = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("关键词") }
                        )
                        Button(onClick = { refresh() }) { Text("查询") }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        CollapsibleFilterSection(
                            title = "状态",
                            summary = publishStatus?.let { PublishStatuses.labelOf(it) } ?: "全部",
                            expanded = statusFilterExpanded,
                            onToggle = { statusFilterExpanded = !statusFilterExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = publishStatus == null,
                                    onClick = {
                                        publishStatus = null
                                        refresh()
                                    },
                                    label = { Text("全部") }
                                )
                                PublishStatuses.OPTIONS.forEach { (code, label) ->
                                    FilterChip(
                                        selected = publishStatus == code,
                                        onClick = {
                                            publishStatus = code
                                            refresh()
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                        CollapsibleFilterSection(
                            title = "标签",
                            summary = tagName ?: "全部",
                            expanded = tagFilterExpanded,
                            onToggle = { tagFilterExpanded = !tagFilterExpanded },
                            contentMaxHeight = 160.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = tagName == null,
                                    onClick = {
                                        tagName = null
                                        refresh()
                                    },
                                    label = { Text("全部") }
                                )
                                tagFilterOptions.forEach { name ->
                                    FilterChip(
                                        selected = tagName == name,
                                        onClick = {
                                            tagName = name
                                            refresh()
                                        },
                                        label = { Text(name) }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            if (error != null) {
                Text(error!!, color = PromptTextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id ?: it.promptUuid ?: it.title.orEmpty() }) { item ->
                    PromptCard(
                        item = item,
                        tagColorMap = tagColorMap,
                        titleSize = titleSp,
                        bodySize = bodySp,
                        onDetail = { openDetail(item) },
                        onTag = { openTagEdit(item) },
                        onStatus = { statusEditItem = item },
                        onCopyPrompt = {
                            copyToClipboard(context, item.promptText.orEmpty(), "已复制提示词")
                        },
                        onCopyAll = {
                            copyToClipboard(
                                context,
                                buildCopyText(item, clientPref),
                                "已按偏好复制"
                            )
                        }
                    )
                }
            }
        }
    }

    detailItem?.let { item ->
        DetailDialog(
            item = item,
            tagColorMap = tagColorMap,
            onDismiss = { detailItem = null },
            onCopy = {
                copyToClipboard(context, item.promptText.orEmpty(), "已复制提示词")
            }
        )
    }

    tagEditItem?.let { item ->
        TagEditDialog(
            item = item,
            options = manualTagOptions,
            loading = tagDialogLoading,
            saving = saving,
            onDismiss = { tagEditItem = null },
            onSave = { next ->
                patchSave(item, item.copy(manualTagList = next), "标签已保存")
                tagEditItem = null
            }
        )
    }

    statusEditItem?.let { item ->
        StatusEditDialog(
            item = item,
            saving = saving,
            onDismiss = { statusEditItem = null },
            onSave = { next ->
                patchSave(item, item.copy(publishStatus = next), "状态已更新")
                statusEditItem = null
            }
        )
    }
}

@Composable
private fun CollapsibleFilterSection(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    contentMaxHeight: Dp? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PromptTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = summary,
                    color = PromptTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = PromptTextSecondary
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val bodyModifier = if (contentMaxHeight != null) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = contentMaxHeight)
                    .verticalScroll(rememberScrollState())
            } else {
                Modifier.fillMaxWidth()
            }
            Column(modifier = bodyModifier.padding(bottom = 4.dp)) {
                content()
            }
        }
    }
}

/**
 * 打标候选：标签库全部名称（含 CONFIG/MANUAL）+ 列表已出现的标签。
 * 保存时仍写入 manualTagList；候选不过滤 CONFIG，避免库里只有平台标签时下拉为空。
 */
private fun buildManualTagOptions(
    catalog: List<AiPromptTag>,
    items: List<AiPromptItem>,
    current: AiPromptItem?
): List<TagOption> {
    val map = LinkedHashMap<String, String?>()
    catalog.forEach { tag ->
        val name = tag.name?.trim().orEmpty()
        if (name.isEmpty()) return@forEach
        map[name] = tag.color
    }
    items.forEach { row ->
        row.displayTags().forEach { name -> map.putIfAbsent(name, null) }
    }
    current?.displayTags()?.forEach { name -> map.putIfAbsent(name, null) }
    return map.entries
        .sortedBy { it.key }
        .map { TagOption(name = it.key, color = it.value) }
}

private data class TagOption(val name: String, val color: String?)

private fun mergeSaved(old: AiPromptItem, saved: AiPromptItem): AiPromptItem =
    old.copy(
        title = saved.title ?: old.title,
        summary = saved.summary ?: old.summary,
        promptText = saved.promptText ?: old.promptText,
        configTagList = saved.configTagList ?: old.configTagList,
        manualTagList = saved.manualTagList ?: old.manualTagList,
        configTags = saved.configTags ?: old.configTags,
        manualTags = saved.manualTags ?: old.manualTags,
        publishStatus = saved.publishStatus ?: old.publishStatus,
        promptUuid = saved.promptUuid ?: old.promptUuid
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromptCard(
    item: AiPromptItem,
    tagColorMap: Map<String, String?>,
    titleSize: TextUnit,
    bodySize: TextUnit,
    onDetail: () -> Unit,
    onTag: () -> Unit,
    onStatus: () -> Unit,
    onCopyPrompt: () -> Unit,
    onCopyAll: () -> Unit
) {
    val configTags = item.resolvedConfigTags()
    val manualTags = item.resolvedManualTags()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetail),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.title.orEmpty().ifBlank { "(无标题)" },
                    fontSize = titleSize,
                    fontWeight = FontWeight.SemiBold,
                    color = PromptTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(item.publishStatus)
            }
            if (!item.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(item.summary, color = PromptTextSecondary, fontSize = bodySize, maxLines = 2)
            }
            if (configTags.isNotEmpty() || manualTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    configTags.forEach { ConfigTagChip(it) }
                    manualTags.forEach { ManualTagChip(it, tagColorMap[it]) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.promptText.orEmpty(),
                color = PromptTextSecondary,
                fontSize = bodySize,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                TextButton(onClick = onDetail) { Text("详情") }
                TextButton(onClick = onTag) { Text("打标") }
                TextButton(onClick = onStatus) { Text("状态") }
                TextButton(onClick = onCopyPrompt) { Text("复制") }
                TextButton(onClick = onCopyAll) { Text("复制全部") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailDialog(
    item: AiPromptItem,
    tagColorMap: Map<String, String?>,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    val configTags = item.resolvedConfigTags()
    val manualTags = item.resolvedManualTags()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onCopy) { Text("复制提示词") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
        title = { Text(item.title.orEmpty().ifBlank { "详情" }) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                StatusBadge(item.publishStatus)
                if (!item.promptUuid.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("UUID：${item.promptUuid}", color = PromptTextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("描述", fontWeight = FontWeight.SemiBold)
                Text(item.summary.orEmpty().ifBlank { "（无）" }, color = PromptTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Text("配置标签", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                if (configTags.isEmpty()) {
                    Text("（无）", color = PromptTextSecondary)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        configTags.forEach { ConfigTagChip(it) }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("人工标签", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                if (manualTags.isEmpty()) {
                    Text("（无）", color = PromptTextSecondary)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        manualTags.forEach { ManualTagChip(it, tagColorMap[it]) }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("提示词", fontWeight = FontWeight.SemiBold)
                Text(
                    item.promptText.orEmpty().ifBlank { "（无）" },
                    color = PromptTextPrimary,
                    fontSize = 14.sp
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditDialog(
    item: AiPromptItem,
    options: List<TagOption>,
    loading: Boolean,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var selected by remember(item.id) {
        mutableStateOf(item.resolvedManualTags().toMutableSet())
    }
    var custom by remember { mutableStateOf("") }

    fun toggle(name: String) {
        selected = selected.toMutableSet().also { set ->
            if (set.contains(name)) set.remove(name) else set.add(name)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !saving && !loading,
                onClick = {
                    val extra = custom.split(',', '，', ';', '；', ' ')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    onSave((selected + extra).distinct().sorted())
                }
            ) { Text(if (saving) "保存中…" else "保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        title = { Text("打标：${item.title.orEmpty().ifBlank { "未命名" }}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    when {
                        loading -> "正在加载标签…"
                        options.isEmpty() -> "暂无标签候选，可在下方直接输入"
                        else -> "点选标签（可多选，已选 ${selected.size}）"
                    },
                    color = PromptTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (options.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { opt ->
                            FilterChip(
                                selected = selected.contains(opt.name),
                                onClick = { toggle(opt.name) },
                                label = { Text(opt.name) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("追加新标签（逗号分隔）") },
                    singleLine = true
                )
            }
        }
    )
}

@Composable
private fun StatusEditDialog(
    item: AiPromptItem,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selected by remember { mutableStateOf(item.publishStatus ?: "DRAFT") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(enabled = !saving, onClick = { onSave(selected) }) {
                Text(if (saving) "保存中…" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        title = { Text("修改状态") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PublishStatuses.OPTIONS.forEach { (code, _) ->
                    val chosen = selected == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = code }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(checked = chosen, onCheckedChange = { selected = code })
                        StatusBadge(code)
                    }
                }
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String, toast: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("ai_prompt", text))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}
