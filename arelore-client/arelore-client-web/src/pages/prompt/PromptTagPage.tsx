import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Button,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { useI18n } from '../../locale';
import { AiPromptTag, promptApi } from '../../api/prompt';
import { TAG_COLOR_OPTIONS } from './tagColors';

const PromptTagPage: React.FC = () => {
  const { t } = useI18n();
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<AiPromptTag[]>([]);
  const [nameKeyword, setNameKeyword] = useState('');
  const [tagTypeFilter, setTagTypeFilter] = useState<string | undefined>();
  const [query, setQuery] = useState<{ name?: string; tagType?: string }>({});
  const [editOpen, setEditOpen] = useState(false);
  const [editing, setEditing] = useState<AiPromptTag | null>(null);
  const [form] = Form.useForm();

  const tagTypeOptions = useMemo(
    () => [
      { label: t('prompt.tags.typeConfigFull'), value: 'CONFIG' },
      { label: t('prompt.tags.typeManualFull'), value: 'MANUAL' },
    ],
    [t]
  );

  const colorSelectOptions = useMemo(
    () => [
      { value: '', label: t('prompt.tags.colorDefault') as React.ReactNode },
      ...TAG_COLOR_OPTIONS.map((c) => ({
        value: c.value,
        label: (
          <Tag color={c.value} style={{ marginInlineEnd: 0 }}>
            {c.label}
          </Tag>
        ) as React.ReactNode,
      })),
    ],
    [t]
  );

  const loadTags = useCallback(async () => {
    setLoading(true);
    try {
      const res = await promptApi.tagList({
        name: query.name || undefined,
        tagType: query.tagType || undefined,
      });
      setRows((res.data || []).filter((tag) => tag.status !== 'DELETED'));
    } catch (e: any) {
      message.error(e?.message || t('prompt.tags.loadFailed'));
    } finally {
      setLoading(false);
    }
  }, [query, t]);

  const onSearch = () => {
    setQuery({
      name: nameKeyword.trim() || undefined,
      tagType: tagTypeFilter || undefined,
    });
  };

  useEffect(() => {
    loadTags();
  }, [loadTags]);

  const openCreate = () => {
    setEditing(null);
    form.setFieldsValue({ name: '', color: '', sortOrder: 0, tagType: 'MANUAL' });
    setEditOpen(true);
  };

  const openEdit = (row: AiPromptTag) => {
    setEditing(row);
    form.setFieldsValue({
      name: row.name,
      color: row.color || '',
      sortOrder: row.sortOrder ?? 0,
      tagType: row.tagType || 'MANUAL',
    });
    setEditOpen(true);
  };

  const onSave = async () => {
    const values = await form.validateFields();
    try {
      await promptApi.tagSave({
        id: editing?.id,
        name: values.name,
        tagType: values.tagType,
        color: values.color || '',
        sortOrder: values.sortOrder ?? 0,
        status: 'NORMAL',
      });
      message.success(t('prompt.common.saved'));
      setEditOpen(false);
      loadTags();
    } catch (e: any) {
      message.error(e?.message || t('prompt.common.saveFailed'));
    }
  };

  const onDelete = async (row: AiPromptTag) => {
    if (!row.id) return;
    try {
      await promptApi.tagDelete({ id: row.id });
      message.success(t('prompt.common.deleted'));
      loadTags();
    } catch (e: any) {
      message.error(e?.message || t('prompt.common.deleteFailed'));
    }
  };

  const columns = [
    {
      title: t('prompt.tags.colType'),
      dataIndex: 'tagType',
      width: 110,
      render: (v: string) =>
        v === 'CONFIG' ? (
          <Tag color="blue">{t('prompt.tags.typeConfig')}</Tag>
        ) : (
          <Tag>{t('prompt.tags.typeManual')}</Tag>
        ),
    },
    {
      title: t('prompt.tags.colName'),
      dataIndex: 'name',
      render: (_: string, row: AiPromptTag) => (
        <Tag color={row.color || undefined}>{row.name}</Tag>
      ),
    },
    {
      title: t('prompt.tags.colColor'),
      dataIndex: 'color',
      width: 160,
      render: (v: string) => (v ? <Tag color={v}>{v}</Tag> : '-'),
    },
    {
      title: t('prompt.tags.colSort'),
      dataIndex: 'sortOrder',
      width: 100,
    },
    {
      title: t('prompt.tags.colStatus'),
      dataIndex: 'status',
      width: 100,
      render: (v: string) => (v === 'DISABLED' ? t('prompt.tags.disabled') : t('prompt.tags.enabled')),
    },
    {
      title: t('prompt.common.actions'),
      key: 'action',
      width: 160,
      render: (_: unknown, row: AiPromptTag) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(row)}>
            {t('prompt.common.edit')}
          </Button>
          <Popconfirm title={t('prompt.tags.confirmDelete')} onConfirm={() => onDelete(row)}>
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
        {t('prompt.tags.title')}
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
        {t('prompt.tags.lead')}
      </Typography.Paragraph>
      <Space style={{ marginBottom: 16 }} wrap>
        <Input
          placeholder={t('prompt.tags.searchName')}
          allowClear
          style={{ width: 220 }}
          value={nameKeyword}
          onChange={(e) => setNameKeyword(e.target.value)}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder={t('prompt.tags.tagType')}
          style={{ width: 140 }}
          options={tagTypeOptions}
          value={tagTypeFilter}
          onChange={(v) => setTagTypeFilter(v)}
        />
        <Button type="primary" icon={<SearchOutlined />} onClick={onSearch}>
          {t('prompt.common.search')}
        </Button>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          {t('prompt.tags.createTag')}
        </Button>
      </Space>

      <Table
        rowKey={(r) => String(r.id || `${r.tagType}-${r.name}`)}
        loading={loading}
        columns={columns}
        dataSource={rows}
        pagination={false}
      />

      <Modal
        title={editing ? t('prompt.tags.editTitle') : t('prompt.tags.createTitle')}
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={onSave}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="tagType" label={t('prompt.tags.tagType')} rules={[{ required: true }]}>
            <Select options={tagTypeOptions} disabled={!!editing} />
          </Form.Item>
          <Form.Item name="name" label={t('prompt.tags.nameField')} rules={[{ required: true, max: 64 }]}>
            <Input maxLength={64} disabled={!!editing} />
          </Form.Item>
          <Form.Item name="color" label={t('prompt.tags.colorField')} extra={t('prompt.tags.colorExtra')}>
            <Select
              allowClear
              placeholder={t('prompt.tags.colorPlaceholder')}
              options={
                editing?.color && !colorSelectOptions.some((o) => o.value === editing.color)
                  ? [
                      ...colorSelectOptions,
                      {
                        value: editing.color,
                        label: (
                          <Tag color={editing.color} style={{ marginInlineEnd: 0 }}>
                            {editing.color}
                          </Tag>
                        ),
                      },
                    ]
                  : colorSelectOptions
              }
            />
          </Form.Item>
          <Form.Item name="sortOrder" label={t('prompt.tags.sortField')}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PromptTagPage;
