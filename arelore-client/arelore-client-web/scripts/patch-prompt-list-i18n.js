const fs = require('fs');
const path = require('path');
const p = path.join(__dirname, '../src/pages/prompt/PromptListPage.tsx');
let s = fs.readFileSync(p, 'utf8');

if (!s.includes("import { useI18n }")) {
  s = s.replace(
    "import {\n  AiPromptItem,\n  AiPromptTag,\n  promptApi,\n} from '../../api/prompt';",
    "import { useI18n } from '../../locale';\nimport {\n  AiPromptItem,\n  AiPromptTag,\n  promptApi,\n} from '../../api/prompt';"
  );
}

s = s.replace(
  /function promptsToMarkdown\(items: AiPromptItem\[\]\): string \{[\s\S]*?\.join\('\\n---\\n\\n'\);\n\}/,
  `function promptsToMarkdown(
  items: AiPromptItem[],
  labels: {
    untitled: (n: number) => string;
    noSummary: string;
    noPrompt: string;
    headingSummary: string;
    headingPrompt: string;
  }
): string {
  return items
    .map((item, i) => {
      const title = (item.title || '').trim() || labels.untitled(i + 1);
      const summary = (item.summary || '').trim() || labels.noSummary;
      const prompt = (item.promptText || '').trim() || labels.noPrompt;
      return [
        \`# \${title}\`,
        '',
        \`## \${labels.headingSummary}\`,
        '',
        summary,
        '',
        \`## \${labels.headingPrompt}\`,
        '',
        prompt,
        '',
      ].join('\\n');
    })
    .join('\\n---\\n\\n');
}`
);

s = s.replace(
  /const PUBLISH_OPTIONS = \[[\s\S]*?\];\n\n/,
  ''
);

s = s.replace(
  'const PromptListPage: React.FC = () => {\n  const [loading, setLoading] = useState(false);',
  `const PromptListPage: React.FC = () => {
  const { t } = useI18n();
  const publishOptions = useMemo(
    () => [
      { label: t('prompt.status.draft'), value: 'DRAFT' },
      { label: t('prompt.status.producing'), value: 'PRODUCING' },
      { label: t('prompt.status.published'), value: 'PUBLISHED' },
      { label: t('prompt.status.offline'), value: 'OFFLINE' },
    ],
    [t]
  );
  const [loading, setLoading] = useState(false);`
);

const reps = [
  ["message.error(e?.message || '加载失败，请先登录')", "message.error(e?.message || t('prompt.common.loadFailedLogin'))"],
  ["message.error('缺少 id，无法保存')", "message.error(t('prompt.list.missingId'))"],
  ["message.error(e?.message || opts?.errorTip || '保存失败')", "message.error(e?.message || opts?.errorTip || t('prompt.common.saveFailed'))"],
  ["errorTip: '打标保存失败'", "errorTip: t('prompt.list.tagSaveFailed')"],
  ["{ errorTip: '状态保存失败' }", "{ errorTip: t('prompt.list.statusSaveFailed') }"],
  ["message.success('已保存')", "message.success(t('prompt.common.saved'))"],
  ["message.error(e?.message || '保存失败')", "message.error(e?.message || t('prompt.common.saveFailed'))"],
  ["message.success('已删除')", "message.success(t('prompt.common.deleted'))"],
  ["message.error(e?.message || '删除失败')", "message.error(e?.message || t('prompt.common.deleteFailed'))"],
  ["PUBLISH_OPTIONS.find((o) => o.value === code)?.label || code || '未知'", "publishOptions.find((o) => o.value === code)?.label || code || t('prompt.common.unknown')"],
  ["message.error(e?.message || '加载详情失败')", "message.error(e?.message || t('prompt.list.loadDetailFailed'))"],
  ["message.success('已复制提示词')", "message.success(t('prompt.list.copiedPrompt'))"],
  ["message.error('复制失败')", "message.error(t('prompt.common.copyFailed'))"],
  ["message.success('已复制全部字段')", "message.success(t('prompt.list.copiedAll'))"],
  ["message.success(`已导出 ${list.length} 条 CSV（可用 Excel 打开）`)", "message.success(t('prompt.list.exportedCsv', { count: list.length }))"],
  ["message.error(e?.message || '导出失败')", "message.error(e?.message || t('prompt.list.exportFailed'))"],
  ["message.warning('当前查询条件下没有可导出的数据')", "message.warning(t('prompt.list.noExportData'))"],
  ["setMdContent(promptsToMarkdown(list));", `setMdContent(promptsToMarkdown(list, {
        untitled: (n) => t('prompt.list.mdUntitled', { n }),
        noSummary: t('prompt.list.mdNoSummary'),
        noPrompt: t('prompt.list.mdNoPrompt'),
        headingSummary: t('prompt.list.mdHeadingSummary'),
        headingPrompt: t('prompt.list.mdHeadingPrompt'),
      }));`],
  ["message.success('已开始下载 .md 文件')", "message.success(t('prompt.list.mdDownloading'))"],
  ["message.success('已复制 Markdown')", "message.success(t('prompt.list.mdCopied'))"],
  ["message.error('请先将 Excel 另存为 CSV 后再导入')", "message.error(t('prompt.list.excelToCsv'))"],
  ["message.error('文件中没有可导入的数据')", "message.error(t('prompt.list.importEmpty'))"],
  ["message.success(res.data?.message || '导入完成')", "message.success(res.data?.message || t('prompt.list.importDone'))"],
  ["message.error(e?.message || '导入失败，请确认 CSV 含 configTags（或 importTags/tags）列')", "message.error(e?.message || t('prompt.list.importFailed'))"],
  ["title: '标题（引流）'", "title: t('prompt.list.colTitle')"],
  ["title: '描述（引流）'", "title: t('prompt.list.colSummary')"],
  ["title: '配置标签'", "title: t('prompt.list.colConfigTags')"],
  ["title: '人工标签'", "title: t('prompt.list.colManualTags')"],
  ["title: '状态'", "title: t('prompt.common.status')"],
  ["title: 'UUID'", "title: t('prompt.list.colUuid')"],
  ["title: '操作'", "title: t('prompt.common.actions')"],
  ['placeholder="选择或输入后自动保存"', "placeholder={t('prompt.list.manualTagPlaceholder')}"],
  ["options={PUBLISH_OPTIONS}", "options={publishOptions}"],
  ["              提示词预览", "              {t('prompt.list.promptPreview')}"],
  ["                  提示词预览", "                  {t('prompt.list.promptPreview')}"],
  ["{prompt || '（无提示词）'}", "{prompt || t('prompt.list.emptyPrompt')}"],
  ["            详情", "            {t('prompt.common.detail')}"],
  ["            复制\n", "            {t('prompt.common.copy')}\n"],
  ["            复制全部", "            {t('prompt.list.copyAll')}"],
  ["            编辑\n", "            {t('prompt.common.edit')}\n"],
  ['<Popconfirm title="确认删除？" onConfirm={() => onDelete(row)}>', "<Popconfirm title={t('prompt.common.confirmDelete')} onConfirm={() => onDelete(row)}>"],
  ["              删除", "              {t('prompt.common.delete')}"],
  ["        提示词列表", "        {t('prompt.list.title')}"],
  ["        支持按标题 / 状态 / 标签筛选；列表内可打标、改状态；点「详情」查看全文。配置标签可被 CSV 覆盖，人工标签仅本页修改。", "        {t('prompt.list.lead')}"],
  ['placeholder="按标题搜索"', "placeholder={t('prompt.list.searchTitle')}"],
  ['placeholder="发布状态"', "placeholder={t('prompt.list.publishStatus')}"],
  ['placeholder="任意标签"', "placeholder={t('prompt.list.anyTag')}"],
  ["          查询", "          {t('prompt.common.search')}"],
  ["          新建", "          {t('prompt.common.create')}"],
  ["          导出 CSV", "          {t('prompt.list.exportCsv')}"],
  ["          导出 MD", "          {t('prompt.list.exportMd')}"],
  [">导入 CSV</Button>", ">{t('prompt.list.importCsv')}</Button>"],
  ['title="导出 Markdown 预览"', "title={t('prompt.list.mdPreviewTitle')}"],
  ["            关闭\n", "            {t('prompt.common.close')}\n"],
  ["            下载 .md", "            {t('prompt.list.downloadMd')}"],
  ["title={detailItem?.title || '提示词详情'}", "title={detailItem?.title || t('prompt.list.detailTitle')}"],
  ["            复制提示词", "            {t('prompt.list.copyPrompt')}"],
  ["                UUID：{detailItem.promptUuid}", "                {t('prompt.list.uuidLabel', { uuid: detailItem.promptUuid })}"],
  ["              描述\n", "              {t('prompt.list.description')}\n"],
  ["{(detailItem.summary || '').trim() || '（无）'}", "{(detailItem.summary || '').trim() || t('prompt.common.none')}"],
  ["            <Typography.Title level={5}>提示词</Typography.Title>", "            <Typography.Title level={5}>{t('prompt.list.promptLabel')}</Typography.Title>"],
  ["{(detailItem.promptText || '').trim() || '（无）'}", "{(detailItem.promptText || '').trim() || t('prompt.common.none')}"],
  ["title={editing ? '编辑提示词' : '新建提示词'}", "title={editing ? t('prompt.list.editTitle') : t('prompt.list.createTitle')}"],
  ['label="UUID（服务端生成，不可修改；导入覆盖用）"', "label={t('prompt.list.uuidField')}"],
  ['label="标题（发视频引流）"', "label={t('prompt.list.titleField')}"],
  ['placeholder="用于短视频标题，吸引点击"', "placeholder={t('prompt.list.titlePlaceholder')}"],
  ['label="描述（发视频引流）"', "label={t('prompt.list.summaryField')}"],
  ['extra="建议文末加话题，如 #英语口语 #学英语"', "extra={t('prompt.list.summaryExtra')}"],
  ['placeholder="用于视频简介/文案，末尾可加 #话题"', "placeholder={t('prompt.list.summaryPlaceholder')}"],
  ['label="提示词" rules={[{ required: true }]}', "label={t('prompt.list.promptLabel')} rules={[{ required: true }]}"],
  ['label="配置标签（可手打，CSV 亦可覆盖）"', "label={t('prompt.list.configTagsField')}"],
  ['extra="例如：小红书；或 抖音 + 视频号"', "extra={t('prompt.list.configTagsExtra')}"],
  ['placeholder="选择或输入配置标签"', "placeholder={t('prompt.list.configTagsPlaceholder')}"],
  ['label="人工标签"', "label={t('prompt.list.manualTagsField')}"],
  ['extra="仅编辑时可改；导入不会覆盖"', "extra={t('prompt.list.manualTagsExtra')}"],
  ['placeholder="选择或输入人工标签"', "placeholder={t('prompt.list.manualTagsPlaceholder')}"],
  ['label="发布状态" rules={[{ required: true }]}', "label={t('prompt.list.publishStatusField')} rules={[{ required: true }]}"],
];

// onCopyAll block
const copyAllOld = `  const onCopyAll = async (row: AiPromptItem) => {
    const text = [
      \`标题：\${row.title || ''}\`,
      \`描述：\${row.summary || ''}\`,
      \`状态：\${statusLabel(row.publishStatus)}\`,
      \`配置标签：\${(row.configTagList || []).join('、')}\`,
      \`人工标签：\${(row.manualTagList || []).join('、')}\`,
      \`提示词：\`,
      row.promptText || '',
    ].join('\\n');`;

const copyAllNew = `  const onCopyAll = async (row: AiPromptItem) => {
    const text = t('prompt.list.copyAllFields', {
      title: row.title || '',
      summary: row.summary || '',
      status: statusLabel(row.publishStatus),
      configTags: (row.configTagList || []).join(', '),
      manualTags: (row.manualTagList || []).join(', '),
      prompt: row.promptText || '',
    });`;

if (!s.includes(copyAllOld)) console.log('MISS onCopyAll');
else s = s.replace(copyAllOld, copyAllNew);

for (const [a, b] of reps) {
  if (!s.includes(a)) console.log('MISS', a.slice(0, 70));
  else s = s.split(a).join(b);
}

// Fix remaining bare Chinese button labels that may have been partially matched
const leftovers = s.match(/[\u4e00-\u9fff]+/g);
if (leftovers) {
  const uniq = [...new Set(leftovers)].filter((x) => !x.includes('prompt') && x.length < 20);
  console.log('remaining CJK sample:', uniq.slice(0, 40).join(' | '));
}

fs.writeFileSync(p, s);
console.log('patched PromptListPage');
