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
import { useI18n } from '../../locale';
import {
  AiPromptItem,
  AiPromptTag,
  promptApi,
} from '../../api/prompt';
import { csvToPrompts, downloadCsv, promptsToCsv } from './promptCsv';

function promptsToMarkdown(
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
        `# ${title}`,
        '',
        `## ${labels.headingSummary}`,
        '',
        summary,
        '',
        `## ${labels.headingPrompt}`,
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
      message.error(e?.message || t('prompt.common.loadFailedLogin'));
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
      message.error(t('prompt.list.missingId'));
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
      message.error(e?.message || opts?.errorTip || t('prompt.common.saveFailed'));
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
      errorTip: t('prompt.list.tagSaveFailed'),
    });
  };

  const onPublishStatusChange = async (row: AiPromptItem, nextStatus: string) => {
    const prev = row.publishStatus || 'DRAFT';
    if (!nextStatus || nextStatus === prev) return;
    await saveRowInline(row, { publishStatus: nextStatus }, { errorTip: t('prompt.list.statusSaveFailed') });
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
      message.success(t('prompt.common.saved'));
      setEditOpen(false);
      loadPage();
      loadTags();
    } catch (e: any) {
      message.error(e?.message || t('prompt.common.saveFailed'));
    }
  };

  const onDelete = async (row: AiPromptItem) => {
    if (!row.id) return;
    try {
      await promptApi.delete({ id: row.id });
      message.success(t('prompt.common.deleted'));
      loadPage();
    } catch (e: any) {
      message.error(e?.message || t('prompt.common.deleteFailed'));
    }
  };

  const statusLabel = (code?: string) =>
    publishOptions.find((o) => o.value === code)?.label || code || t('prompt.common.unknown');

  const openDetail = async (row: AiPromptItem) => {
    setDetailOpen(true);
    setDetailItem(row);
    if (!row.id) return;
    setDetailLoading(true);
    try {
      const res = await promptApi.detail({ id: row.id });
      setDetailItem(res.data || row);
    } catch (e: any) {
      message.error(e?.message || t('prompt.list.loadDetailFailed'));
    } finally {
      setDetailLoading(false);
    }
  };

  const onCopyPrompt = async (row: AiPromptItem) => {
    try {
      await copyText(row.promptText || '');
      message.success(t('prompt.list.copiedPrompt'));
    } catch {
      message.error(t('prompt.common.copyFailed'));
    }
  };

  const onCopyAll = async (row: AiPromptItem) => {
    const text = t('prompt.list.copyAllFields', {
      title: row.title || '',
      summary: row.summary || '',
      status: statusLabel(row.publishStatus),
      configTags: (row.configTagList || []).join(', '),
      manualTags: (row.manualTagList || []).join(', '),
      prompt: row.promptText || '',
    });
    try {
      await copyText(text);
      message.success(t('prompt.list.copiedAll'));
    } catch {
      message.error(t('prompt.common.copyFailed'));
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
      message.success(t('prompt.list.exportedCsv', { count: list.length }));
    } catch (e: any) {
      message.error(e?.message || t('prompt.list.exportFailed'));
    }
  };

  const onExportMd = async () => {
    setMdExporting(true);
    try {
      const res = await promptApi.exportList(exportFilter());
      const list = res.data || [];
      if (!list.length) {
        message.warning(t('prompt.list.noExportData'));
        return;
      }
      setMdContent(promptsToMarkdown(list, {
        untitled: (n) => t('prompt.list.mdUntitled', { n }),
        noSummary: t('prompt.list.mdNoSummary'),
        noPrompt: t('prompt.list.mdNoPrompt'),
        headingSummary: t('prompt.list.mdHeadingSummary'),
        headingPrompt: t('prompt.list.mdHeadingPrompt'),
      }));
      setMdOpen(true);
    } catch (e: any) {
      message.error(e?.message || t('prompt.list.exportFailed'));
    } finally {
      setMdExporting(false);
    }
  };

  const onDownloadMd = () => {
    downloadMarkdown(`ai-prompts-${Date.now()}.md`, mdContent);
    message.success(t('prompt.list.mdDownloading'));
  };

  const onCopyMd = async () => {
    try {
      await copyText(mdContent);
      message.success(t('prompt.list.mdCopied'));
    } catch {
      message.error(t('prompt.common.copyFailed'));
    }
  };

  const onImportFile = async (file: File) => {
    try {
      const name = (file.name || '').toLowerCase();
      if (name.endsWith('.xlsx') || name.endsWith('.xls')) {
        message.error(t('prompt.list.excelToCsv'));
        return false;
      }
      const text = await file.text();
      const items = csvToPrompts(text);
      if (!items.length) {
        message.error(t('prompt.list.importEmpty'));
        return false;
      }
      const res = await promptApi.importBatch({ items });
      message.success(res.data?.message || t('prompt.list.importDone'));
      loadPage();
      loadTags();
    } catch (e: any) {
      message.error(e?.message || t('prompt.list.importFailed'));
    }
    return false;
  };

  const columns = [
    {
      title: t('prompt.list.colTitle'),
      dataIndex: 'title',
      ellipsis: true,
      width: 200,
      render: (title: string, row: AiPromptItem) => {
        const prompt = (row.promptText || '').trim();
        const preview = (
          <div style={{ maxWidth: 420, maxHeight: 360, overflow: 'auto' }}>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              {t('prompt.list.promptPreview')}
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
              {prompt || t('prompt.list.emptyPrompt')}
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
      title: t('prompt.list.colSummary'),
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
                  {t('prompt.list.promptPreview')}
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
      title: t('prompt.list.colConfigTags'),
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
      title: t('prompt.list.colManualTags'),
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
            placeholder={t('prompt.list.manualTagPlaceholder')}
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
      title: t('prompt.common.status'),
      dataIndex: 'publishStatus',
      width: 120,
      render: (_: string, row: AiPromptItem) => {
        const key = String(row.id || '');
        const saving = !!savingRowIds[key];
        return (
          <Select
            size="small"
            style={{ width: '100%', minWidth: 96 }}
            options={publishOptions}
            value={row.publishStatus || 'DRAFT'}
            loading={saving}
            disabled={!row.id || saving}
            onChange={(v) => onPublishStatusChange(row, v)}
          />
        );
      },
    },
    {
      title: t('prompt.list.colUuid'),
      dataIndex: 'promptUuid',
      width: 120,
      ellipsis: true,
      render: (v: string) => v || '-',
    },
    {
      title: t('prompt.common.actions'),
      key: 'action',
      width: 320,
      fixed: 'right' as const,
      render: (_: unknown, row: AiPromptItem) => (
        <Space wrap>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(row)}>
            {t('prompt.common.detail')}
          </Button>
          <Button type="link" size="small" icon={<CopyOutlined />} onClick={() => onCopyPrompt(row)}>
            {t('prompt.common.copy')}
          </Button>
          <Button type="link" size="small" onClick={() => onCopyAll(row)}>
            {t('prompt.list.copyAll')}
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(row)}>
            {t('prompt.common.edit')}
          </Button>
          <Popconfirm title={t('prompt.common.confirmDelete')} onConfirm={() => onDelete(row)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              {t('prompt.common.delete')}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        {t('prompt.list.title')}
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
        {t('prompt.list.lead')}
      </Typography.Paragraph>
      <Space style={{ marginBottom: 12 }} wrap>
        <Input
          placeholder={t('prompt.list.searchTitle')}
          allowClear
          style={{ width: 220 }}
          value={titleKeyword}
          onChange={(e) => setTitleKeyword(e.target.value)}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder={t('prompt.list.publishStatus')}
          style={{ width: 140 }}
          options={publishOptions}
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
          placeholder={t('prompt.list.anyTag')}
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
          {t('prompt.common.search')}
        </Button>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          {t('prompt.common.create')}
        </Button>
        <Button icon={<ExportOutlined />} onClick={onExport}>
          {t('prompt.list.exportCsv')}
        </Button>
        <Button icon={<ExportOutlined />} loading={mdExporting} onClick={onExportMd}>
          {t('prompt.list.exportMd')}
        </Button>
        <Upload accept=".csv,text/csv" showUploadList={false} beforeUpload={onImportFile}>
          <Button icon={<ImportOutlined />}>{t('prompt.list.importCsv')}</Button>
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
        title={t('prompt.list.mdPreviewTitle')}
        open={mdOpen}
        onCancel={() => setMdOpen(false)}
        width={800}
        destroyOnClose
        footer={[
          <Button key="close" onClick={() => setMdOpen(false)}>
            {t('prompt.common.close')}
          </Button>,
          <Button key="copy" icon={<CopyOutlined />} onClick={onCopyMd}>
            {t('prompt.common.copy')}
          </Button>,
          <Button key="download" type="primary" icon={<ExportOutlined />} onClick={onDownloadMd}>
            {t('prompt.list.downloadMd')}
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
        title={detailItem?.title || t('prompt.list.detailTitle')}
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
            {t('prompt.list.copyPrompt')}
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
            {t('prompt.common.edit')}
          </Button>,
          <Button
            key="close"
            onClick={() => {
              setDetailOpen(false);
              setDetailItem(null);
            }}
          >
            {t('prompt.common.close')}
          </Button>,
        ]}
      >
        {detailItem ? (
          <div style={{ maxHeight: '70vh', overflow: 'auto' }}>
            <Space wrap style={{ marginBottom: 12 }}>
              <Tag color="processing">{statusLabel(detailItem.publishStatus)}</Tag>
              {(detailItem.configTagList || []).map((tag) => (
                <Tag key={`c-${tag}`} color="blue">
                  {tag}
                </Tag>
              ))}
              {(detailItem.manualTagList || []).map((tag) => (
                <Tag key={`m-${tag}`} color={manualTagColorMap.get(tag)}>
                  {tag}
                </Tag>
              ))}
            </Space>
            {detailItem.promptUuid ? (
              <Typography.Paragraph type="secondary" style={{ marginBottom: 8 }}>
                {t('prompt.list.uuidLabel', { uuid: detailItem.promptUuid })}
              </Typography.Paragraph>
            ) : null}
            <Typography.Title level={5} style={{ marginTop: 0 }}>
              {t('prompt.list.description')}
            </Typography.Title>
            <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
              {(detailItem.summary || '').trim() || t('prompt.common.none')}
            </Typography.Paragraph>
            <Typography.Title level={5}>{t('prompt.list.promptLabel')}</Typography.Title>
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
              {(detailItem.promptText || '').trim() || t('prompt.common.none')}
            </pre>
          </div>
        ) : null}
      </Modal>

      <Modal
        title={editing ? t('prompt.list.editTitle') : t('prompt.list.createTitle')}
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={onSave}
        width={720}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          {editing ? (
            <Form.Item label={t('prompt.list.uuidField')}>
              <Input value={editing.promptUuid || ''} readOnly disabled />
            </Form.Item>
          ) : null}
          <Form.Item
            name="title"
            label={t('prompt.list.titleField')}
            rules={[{ required: true, max: 255 }]}
          >
            <Input maxLength={255} placeholder={t('prompt.list.titlePlaceholder')} />
          </Form.Item>
          <Form.Item
            name="summary"
            label={t('prompt.list.summaryField')}
            extra={t('prompt.list.summaryExtra')}
          >
            <Input.TextArea rows={3} maxLength={1024} placeholder={t('prompt.list.summaryPlaceholder')} />
          </Form.Item>
          <Form.Item name="promptText" label={t('prompt.list.promptLabel')} rules={[{ required: true }]}>
            <Input.TextArea rows={8} showCount maxLength={65535} />
          </Form.Item>
          <Form.Item
            name="configTagList"
            label={t('prompt.list.configTagsField')}
            extra={t('prompt.list.configTagsExtra')}
          >
            <Select mode="tags" options={configTagOptions} placeholder={t('prompt.list.configTagsPlaceholder')} />
          </Form.Item>
          {editing ? (
            <>
              <Form.Item
                name="manualTagList"
                label={t('prompt.list.manualTagsField')}
                extra={t('prompt.list.manualTagsExtra')}
              >
                <Select mode="tags" options={manualTagOptions} placeholder={t('prompt.list.manualTagsPlaceholder')} />
              </Form.Item>
              <Form.Item name="publishStatus" label={t('prompt.list.publishStatusField')} rules={[{ required: true }]}>
                <Select options={publishOptions} />
              </Form.Item>
            </>
          ) : null}
        </Form>
      </Modal>
    </div>
  );
};

export default PromptListPage;
