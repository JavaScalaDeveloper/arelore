import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Button,
  Form,
  Input,
  Modal,
  Popover,
  Select,
  Space,
  Table,
  Tag,
  message,
  Popconfirm,
  Upload,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  CopyOutlined,
  EditOutlined,
  DeleteOutlined,
  ExportOutlined,
  ImportOutlined,
  SearchOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import {
  AiPromptItem,
  AiPromptTag,
  promptApi,
} from '../../api/prompt';
import { csvToPrompts, downloadCsv, promptsToCsv } from './promptCsv';

function promptsToMarkdown(items: AiPromptItem[]): string {
  return items
    .map((item, i) => {
      const title = (item.title || '').trim() || `未命名-${i + 1}`;
      const summary = (item.summary || '').trim() || '（无描述）';
      const prompt = (item.promptText || '').trim() || '（无提示词）';
      return [
        `# ${title}`,
        '',
        '## 描述',
        '',
        summary,
        '',
        '## 提示词',
        '',
        prompt,
        '',
      ].join('\n');
    })
    .join('\n---\n\n');
}

function downloadMarkdown(filename: string, content: string) {
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

const PUBLISH_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '制作中', value: 'PRODUCING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下架', value: 'OFFLINE' },
];

function copyText(text: string) {
  if (navigator.clipboard?.writeText) {
    return navigator.clipboard.writeText(text);
  }
  const ta = document.createElement('textarea');
  ta.value = text;
  document.body.appendChild(ta);
  ta.select();
  document.execCommand('copy');
  document.body.removeChild(ta);
  return Promise.resolve();
}

const PromptListPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<AiPromptItem[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [titleKeyword, setTitleKeyword] = useState('');
  const [publishStatus, setPublishStatus] = useState<string | undefined>();
  const [tagName, setTagName] = useState<string | undefined>();
  const [query, setQuery] = useState<{
    titleKeyword?: string;
    publishStatus?: string;
    tagName?: string;
  }>({});

  const [editOpen, setEditOpen] = useState(false);
  const [editing, setEditing] = useState<AiPromptItem | null>(null);
  const [form] = Form.useForm();
  const [tags, setTags] = useState<AiPromptTag[]>([]);
  const [mdOpen, setMdOpen] = useState(false);
  const [mdContent, setMdContent] = useState('');
  const [mdExporting, setMdExporting] = useState(false);
  const [savingRowIds, setSavingRowIds] = useState<Record<string, boolean>>({});
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailItem, setDetailItem] = useState<AiPromptItem | null>(null);

  const loadTags = useCallback(async () => {
    try {
      const res = await promptApi.tagList({});
      setTags(res.data || []);
    } catch {
      // 列表页标签仅作筛选项，失败不打断
    }
  }, []);

  const loadPage = useCallback(async () => {
    setLoading(true);
    try {
      const res = await promptApi.page({
        pageNum,
        pageSize,
        titleKeyword: query.titleKeyword || undefined,
        publishStatus: query.publishStatus || undefined,
        tagName: query.tagName || undefined,
      });
      const data = res.data || {};
      setRows(data.list || data.records || []);
      setTotal(data.total || 0);
    } catch (e: any) {
      message.error(e?.message || '加载失败，请先登录');
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, query]);

  const onSearch = () => {
    setPageNum(1);
    setQuery({
      titleKeyword: titleKeyword.trim() || undefined,
      publishStatus: publishStatus || undefined,
      tagName: tagName || undefined,
    });
  };

  useEffect(() => {
    loadPage();
  }, [loadPage]);

  useEffect(() => {
    loadTags();
  }, [loadTags]);

  const configTagOptions = useMemo(
    () =>
      (tags || [])
        .filter((t) => t.status !== 'DELETED' && t.name && t.tagType === 'CONFIG')
        .map((t) => ({ label: t.name!, value: t.name! })),
    [tags]
  );

  const manualTagOptions = useMemo(
    () =>
      (tags || [])
        .filter((t) => t.status !== 'DELETED' && t.name && t.tagType !== 'CONFIG')
        .map((t) => ({
          label: t.name!,
          value: t.name!,
          color: t.color || undefined,
        })),
    [tags]
  );

  const manualTagColorMap = useMemo(() => {
    const map = new Map<string, string>();
    manualTagOptions.forEach((o) => {
      if (o.color) map.set(o.value, o.color);
    });
    return map;
  }, [manualTagOptions]);

  const saveRowInline = async (
    row: AiPromptItem,
    patch: Partial<Pick<AiPromptItem, 'manualTagList' | 'publishStatus'>>,
    opts?: { refreshTags?: boolean; errorTip?: string }
  ) => {
    if (!row.id) {
      message.error('缺少 id，无法保存');
      return;
    }
    const key = String(row.id);
    const prev = {
      manualTagList: row.manualTagList || [],
      publishStatus: row.publishStatus || 'DRAFT',
    };
    const nextManual =
      patch.manualTagList !== undefined ? patch.manualTagList : prev.manualTagList;
    const nextStatus =
      patch.publishStatus !== undefined ? patch.publishStatus : prev.publishStatus;

    setRows((list) =>
      list.map((r) =>
        r.id === row.id
          ? { ...r, manualTagList: nextManual, publishStatus: nextStatus }
          : r
      )
    );
    setSavingRowIds((m) => ({ ...m, [key]: true }));
    try {
      await promptApi.save({
        id: row.id,
        title: row.title,
        summary: row.summary,
        promptText: row.promptText,
        configTagList: row.configTagList || [],
        manualTagList: nextManual,
        publishStatus: nextStatus,
      });
      if (opts?.refreshTags) {
        loadTags();
      }
    } catch (e: any) {
      setRows((list) =>
        list.map((r) =>
          r.id === row.id
            ? { ...r, manualTagList: prev.manualTagList, publishStatus: prev.publishStatus }
            : r
        )
      );
      message.error(e?.message || opts?.errorTip || '保存失败');
    } finally {
      setSavingRowIds((m) => {
        const next = { ...m };
        delete next[key];
        return next;
      });
    }
  };

  const onManualTagsChange = async (row: AiPromptItem, nextTags: string[]) => {
    const prev = row.manualTagList || [];
    const normalized = Array.from(
      new Set((nextTags || []).map((t) => String(t).trim()).filter(Boolean))
    );
    const same =
      prev.length === normalized.length && prev.every((t, i) => t === normalized[i]);
    if (same) return;
    await saveRowInline(row, { manualTagList: normalized }, {
      refreshTags: true,
      errorTip: '打标保存失败',
    });
  };

  const onPublishStatusChange = async (row: AiPromptItem, nextStatus: string) => {
    const prev = row.publishStatus || 'DRAFT';
    if (!nextStatus || nextStatus === prev) return;
    await saveRowInline(row, { publishStatus: nextStatus }, { errorTip: '状态保存失败' });
  };

  const allTagOptions = useMemo(() => {
    const map = new Map<string, string>();
    [...configTagOptions, ...manualTagOptions].forEach((o) => map.set(o.value, o.label));
    return Array.from(map.entries()).map(([value, label]) => ({ value, label }));
  }, [configTagOptions, manualTagOptions]);

  const openCreate = () => {
    setEditing(null);
    form.setFieldsValue({
      title: '',
      summary: '',
      promptText: '',
      configTagList: [],
    });
    setEditOpen(true);
  };

  const openEdit = (row: AiPromptItem) => {
    setEditing(row);
    form.setFieldsValue({
      title: row.title,
      summary: row.summary || '',
      promptText: row.promptText,
      configTagList: row.configTagList || [],
      manualTagList: row.manualTagList || [],
      publishStatus: row.publishStatus || 'DRAFT',
    });
    setEditOpen(true);
  };

  const onSave = async () => {
    const values = await form.validateFields();
    try {
      const isEdit = !!editing?.id;
      await promptApi.save({
        id: editing?.id,
        // promptUuid 仅服务端生成且不可改；编辑靠 id
        title: values.title,
        summary: values.summary,
        promptText: values.promptText,
        configTagList: values.configTagList || [],
        ...(isEdit
          ? {
              manualTagList: values.manualTagList || [],
              publishStatus: values.publishStatus,
            }
          : {}),
      });
      message.success('已保存');
      setEditOpen(false);
      loadPage();
      loadTags();
    } catch (e: any) {
      message.error(e?.message || '保存失败');
    }
  };

  const onDelete = async (row: AiPromptItem) => {
    if (!row.id) return;
    try {
      await promptApi.delete({ id: row.id });
      message.success('已删除');
      loadPage();
    } catch (e: any) {
      message.error(e?.message || '删除失败');
    }
  };

  const statusLabel = (code?: string) =>
    PUBLISH_OPTIONS.find((o) => o.value === code)?.label || code || '未知';

  const openDetail = async (row: AiPromptItem) => {
    setDetailOpen(true);
    setDetailItem(row);
    if (!row.id) return;
    setDetailLoading(true);
    try {
      const res = await promptApi.detail({ id: row.id });
      setDetailItem(res.data || row);
    } catch (e: any) {
      message.error(e?.message || '加载详情失败');
    } finally {
      setDetailLoading(false);
    }
  };

  const onCopyPrompt = async (row: AiPromptItem) => {
    try {
      await copyText(row.promptText || '');
      message.success('已复制提示词');
    } catch {
      message.error('复制失败');
    }
  };

  const onCopyAll = async (row: AiPromptItem) => {
    const text = [
      `标题：${row.title || ''}`,
      `描述：${row.summary || ''}`,
      `状态：${statusLabel(row.publishStatus)}`,
      `配置标签：${(row.configTagList || []).join('、')}`,
      `人工标签：${(row.manualTagList || []).join('、')}`,
      `提示词：`,
      row.promptText || '',
    ].join('\n');
    try {
      await copyText(text);
      message.success('已复制全部字段');
    } catch {
      message.error('复制失败');
    }
  };

  const exportFilter = () => ({
    publishStatus: query.publishStatus || undefined,
    tagName: query.tagName || undefined,
    titleKeyword: query.titleKeyword || undefined,
  });

  const onExport = async () => {
    try {
      const res = await promptApi.exportList(exportFilter());
      const list = res.data || [];
      downloadCsv(`ai-prompts-${Date.now()}.csv`, promptsToCsv(list));
      message.success(`已导出 ${list.length} 条 CSV（可用 Excel 打开）`);
    } catch (e: any) {
      message.error(e?.message || '导出失败');
    }
  };

  const onExportMd = async () => {
    setMdExporting(true);
    try {
      const res = await promptApi.exportList(exportFilter());
      const list = res.data || [];
      if (!list.length) {
        message.warning('当前查询条件下没有可导出的数据');
        return;
      }
      setMdContent(promptsToMarkdown(list));
      setMdOpen(true);
    } catch (e: any) {
      message.error(e?.message || '导出失败');
    } finally {
      setMdExporting(false);
    }
  };

  const onDownloadMd = () => {
    downloadMarkdown(`ai-prompts-${Date.now()}.md`, mdContent);
    message.success('已开始下载 .md 文件');
  };

  const onCopyMd = async () => {
    try {
      await copyText(mdContent);
      message.success('已复制 Markdown');
    } catch {
      message.error('复制失败');
    }
  };

  const onImportFile = async (file: File) => {
    try {
      const name = (file.name || '').toLowerCase();
      if (name.endsWith('.xlsx') || name.endsWith('.xls')) {
        message.error('请先将 Excel 另存为 CSV 后再导入');
        return false;
      }
      const text = await file.text();
      const items = csvToPrompts(text);
      if (!items.length) {
        message.error('文件中没有可导入的数据');
        return false;
      }
      const res = await promptApi.importBatch({ items });
      message.success(res.data?.message || '导入完成');
      loadPage();
      loadTags();
    } catch (e: any) {
      message.error(e?.message || '导入失败，请确认 CSV 含 configTags（或 importTags/tags）列');
    }
    return false;
  };

  const columns = [
    {
      title: '标题（引流）',
      dataIndex: 'title',
      ellipsis: true,
      width: 200,
      render: (title: string, row: AiPromptItem) => {
        const prompt = (row.promptText || '').trim();
        const preview = (
          <div style={{ maxWidth: 420, maxHeight: 360, overflow: 'auto' }}>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              提示词预览
            </Typography.Text>
            <pre
              style={{
                margin: '8px 0 0',
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word',
                fontSize: 12,
                lineHeight: 1.55,
                fontFamily: 'inherit',
              }}
            >
              {prompt || '（无提示词）'}
            </pre>
          </div>
        );
        return (
          <Popover content={preview} trigger="hover" placement="rightTop" mouseEnterDelay={0.25}>
            <span style={{ cursor: 'default' }}>{title || '-'}</span>
          </Popover>
        );
      },
    },
    {
      title: '描述（引流）',
      dataIndex: 'summary',
      ellipsis: true,
      width: 180,
      render: (v: string, row: AiPromptItem) => {
        const prompt = (row.promptText || '').trim();
        const cell = v || '-';
        if (!prompt) {
          return cell;
        }
        return (
          <Popover
            content={
              <div style={{ maxWidth: 420, maxHeight: 360, overflow: 'auto' }}>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  提示词预览
                </Typography.Text>
                <pre
                  style={{
                    margin: '8px 0 0',
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    fontSize: 12,
                    lineHeight: 1.55,
                    fontFamily: 'inherit',
                  }}
                >
                  {prompt}
                </pre>
              </div>
            }
            trigger="hover"
            placement="rightTop"
            mouseEnterDelay={0.25}
          >
            <span style={{ cursor: 'default' }}>{cell}</span>
          </Popover>
        );
      },
    },
    {
      title: '配置标签',
      dataIndex: 'configTagList',
      width: 140,
      render: (list: string[]) =>
        (list || []).length
          ? list.map((t) => (
              <Tag key={t} color="blue" style={{ marginBottom: 4 }}>
                {t}
              </Tag>
            ))
          : '-',
    },
    {
      title: '人工标签',
      dataIndex: 'manualTagList',
      width: 240,
      render: (_: string[], row: AiPromptItem) => {
        const key = String(row.id || '');
        const saving = !!savingRowIds[key];
        return (
          <Select
            mode="tags"
            size="small"
            style={{ width: '100%', minWidth: 180 }}
            placeholder="选择或输入后自动保存"
            value={row.manualTagList || []}
            options={manualTagOptions}
            loading={saving}
            disabled={!row.id || saving}
            maxTagCount="responsive"
            tokenSeparators={[',', '，']}
            optionRender={(option) => {
              const name = String(option.value ?? '');
              const color = manualTagColorMap.get(name);
              return <Tag color={color}>{name}</Tag>;
            }}
            tagRender={(props) => {
              const { label, value, closable, onClose } = props;
              const color = manualTagColorMap.get(String(value));
              return (
                <Tag
                  color={color}
                  closable={closable}
                  onClose={onClose}
                  style={{ marginInlineEnd: 4 }}
                >
                  {label}
                </Tag>
              );
            }}
            onChange={(vals) => onManualTagsChange(row, vals as string[])}
          />
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'publishStatus',
      width: 120,
      render: (_: string, row: AiPromptItem) => {
        const key = String(row.id || '');
        const saving = !!savingRowIds[key];
        return (
          <Select
            size="small"
            style={{ width: '100%', minWidth: 96 }}
            options={PUBLISH_OPTIONS}
            value={row.publishStatus || 'DRAFT'}
            loading={saving}
            disabled={!row.id || saving}
            onChange={(v) => onPublishStatusChange(row, v)}
          />
        );
      },
    },
    {
      title: 'UUID',
      dataIndex: 'promptUuid',
      width: 120,
      ellipsis: true,
      render: (v: string) => v || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 320,
      fixed: 'right' as const,
      render: (_: unknown, row: AiPromptItem) => (
        <Space wrap>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(row)}>
            详情
          </Button>
          <Button type="link" size="small" icon={<CopyOutlined />} onClick={() => onCopyPrompt(row)}>
            复制
          </Button>
          <Button type="link" size="small" onClick={() => onCopyAll(row)}>
            复制全部
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(row)}>
            编辑
          </Button>
          <Popconfirm title="确认删除？" onConfirm={() => onDelete(row)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        提示词列表
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
        支持按标题 / 状态 / 标签筛选；列表内可打标、改状态；点「详情」查看全文。配置标签可被 CSV 覆盖，人工标签仅本页修改。
      </Typography.Paragraph>
      <Space style={{ marginBottom: 12 }} wrap>
        <Input
          placeholder="按标题搜索"
          allowClear
          style={{ width: 220 }}
          value={titleKeyword}
          onChange={(e) => setTitleKeyword(e.target.value)}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder="发布状态"
          style={{ width: 140 }}
          options={PUBLISH_OPTIONS}
          value={publishStatus}
          onChange={(v) => {
            setPublishStatus(v);
            setPageNum(1);
            setQuery({
              titleKeyword: titleKeyword.trim() || undefined,
              publishStatus: v || undefined,
              tagName: tagName || undefined,
            });
          }}
        />
        <Select
          allowClear
          showSearch
          optionFilterProp="label"
          placeholder="任意标签"
          style={{ width: 180 }}
          options={allTagOptions}
          value={tagName}
          onChange={(v) => {
            setTagName(v);
            setPageNum(1);
            setQuery({
              titleKeyword: titleKeyword.trim() || undefined,
              publishStatus: publishStatus || undefined,
              tagName: v || undefined,
            });
          }}
        />
        <Button type="primary" icon={<SearchOutlined />} onClick={onSearch}>
          查询
        </Button>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          新建
        </Button>
        <Button icon={<ExportOutlined />} onClick={onExport}>
          导出 CSV
        </Button>
        <Button icon={<ExportOutlined />} loading={mdExporting} onClick={onExportMd}>
          导出 MD
        </Button>
        <Upload accept=".csv,text/csv" showUploadList={false} beforeUpload={onImportFile}>
          <Button icon={<ImportOutlined />}>导入 CSV</Button>
        </Upload>
      </Space>

      <Table
        rowKey={(r) => String(r.id || r.promptUuid)}
        loading={loading}
        columns={columns}
        dataSource={rows}
        scroll={{ x: 1300 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: [5, 10, 20, 50, 100, 200],
          onChange: (p, ps) => {
            setPageNum(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title="导出 Markdown 预览"
        open={mdOpen}
        onCancel={() => setMdOpen(false)}
        width={800}
        destroyOnClose
        footer={[
          <Button key="close" onClick={() => setMdOpen(false)}>
            关闭
          </Button>,
          <Button key="copy" icon={<CopyOutlined />} onClick={onCopyMd}>
            复制
          </Button>,
          <Button key="download" type="primary" icon={<ExportOutlined />} onClick={onDownloadMd}>
            下载 .md
          </Button>,
        ]}
      >
        <Input.TextArea
          value={mdContent}
          readOnly
          autoSize={{ minRows: 16, maxRows: 28 }}
          style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace' }}
        />
      </Modal>

      <Modal
        title={detailItem?.title || '提示词详情'}
        open={detailOpen}
        onCancel={() => {
          setDetailOpen(false);
          setDetailItem(null);
        }}
        width={760}
        destroyOnClose
        confirmLoading={detailLoading}
        footer={[
          <Button
            key="copy"
            icon={<CopyOutlined />}
            onClick={() => detailItem && onCopyPrompt(detailItem)}
          >
            复制提示词
          </Button>,
          <Button
            key="edit"
            type="primary"
            icon={<EditOutlined />}
            onClick={() => {
              if (!detailItem) return;
              setDetailOpen(false);
              openEdit(detailItem);
            }}
          >
            编辑
          </Button>,
          <Button
            key="close"
            onClick={() => {
              setDetailOpen(false);
              setDetailItem(null);
            }}
          >
            关闭
          </Button>,
        ]}
      >
        {detailItem ? (
          <div style={{ maxHeight: '70vh', overflow: 'auto' }}>
            <Space wrap style={{ marginBottom: 12 }}>
              <Tag color="processing">{statusLabel(detailItem.publishStatus)}</Tag>
              {(detailItem.configTagList || []).map((t) => (
                <Tag key={`c-${t}`} color="blue">
                  {t}
                </Tag>
              ))}
              {(detailItem.manualTagList || []).map((t) => (
                <Tag key={`m-${t}`} color={manualTagColorMap.get(t)}>
                  {t}
                </Tag>
              ))}
            </Space>
            {detailItem.promptUuid ? (
              <Typography.Paragraph type="secondary" style={{ marginBottom: 8 }}>
                UUID：{detailItem.promptUuid}
              </Typography.Paragraph>
            ) : null}
            <Typography.Title level={5} style={{ marginTop: 0 }}>
              描述
            </Typography.Title>
            <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
              {(detailItem.summary || '').trim() || '（无）'}
            </Typography.Paragraph>
            <Typography.Title level={5}>提示词</Typography.Title>
            <pre
              style={{
                margin: 0,
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word',
                fontSize: 13,
                lineHeight: 1.6,
                fontFamily: 'inherit',
                background: '#fafafa',
                padding: 12,
                borderRadius: 6,
              }}
            >
              {(detailItem.promptText || '').trim() || '（无）'}
            </pre>
          </div>
        ) : null}
      </Modal>

      <Modal
        title={editing ? '编辑提示词' : '新建提示词'}
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={onSave}
        width={720}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          {editing ? (
            <Form.Item label="UUID（服务端生成，不可修改；导入覆盖用）">
              <Input value={editing.promptUuid || ''} readOnly disabled />
            </Form.Item>
          ) : null}
          <Form.Item
            name="title"
            label="标题（发视频引流）"
            rules={[{ required: true, max: 255 }]}
          >
            <Input maxLength={255} placeholder="用于短视频标题，吸引点击" />
          </Form.Item>
          <Form.Item
            name="summary"
            label="描述（发视频引流）"
            extra="建议文末加话题，如 #英语口语 #学英语"
          >
            <Input.TextArea rows={3} maxLength={1024} placeholder="用于视频简介/文案，末尾可加 #话题" />
          </Form.Item>
          <Form.Item name="promptText" label="提示词" rules={[{ required: true }]}>
            <Input.TextArea rows={8} showCount maxLength={65535} />
          </Form.Item>
          <Form.Item
            name="configTagList"
            label="配置标签（可手打，CSV 亦可覆盖）"
            extra="例如：小红书；或 抖音 + 视频号"
          >
            <Select mode="tags" options={configTagOptions} placeholder="选择或输入配置标签" />
          </Form.Item>
          {editing ? (
            <>
              <Form.Item
                name="manualTagList"
                label="人工标签"
                extra="仅编辑时可改；导入不会覆盖"
              >
                <Select mode="tags" options={manualTagOptions} placeholder="选择或输入人工标签" />
              </Form.Item>
              <Form.Item name="publishStatus" label="发布状态" rules={[{ required: true }]}>
                <Select options={PUBLISH_OPTIONS} />
              </Form.Item>
            </>
          ) : null}
        </Form>
      </Modal>
    </div>
  );
};

export default PromptListPage;
