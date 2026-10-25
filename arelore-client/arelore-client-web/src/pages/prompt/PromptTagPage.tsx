import React, { useCallback, useEffect, useState } from 'react';
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
import { AiPromptTag, promptApi } from '../../api/prompt';
import { TAG_COLOR_OPTIONS } from './tagColors';

const TAG_TYPE_OPTIONS = [
  { label: '配置标签', value: 'CONFIG' },
  { label: '人工标签', value: 'MANUAL' },
];

const COLOR_SELECT_OPTIONS = [
  { value: '', label: '默认（无色）' as React.ReactNode },
  ...TAG_COLOR_OPTIONS.map((c) => ({
    value: c.value,
    label: (
      <Tag color={c.value} style={{ marginInlineEnd: 0 }}>
        {c.label}
      </Tag>
    ) as React.ReactNode,
  })),
];

const PromptTagPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<AiPromptTag[]>([]);
  const [nameKeyword, setNameKeyword] = useState('');
  const [tagTypeFilter, setTagTypeFilter] = useState<string | undefined>();
  const [query, setQuery] = useState<{ name?: string; tagType?: string }>({});
  const [editOpen, setEditOpen] = useState(false);
  const [editing, setEditing] = useState<AiPromptTag | null>(null);
  const [form] = Form.useForm();

  const loadTags = useCallback(async () => {
    setLoading(true);
    try {
      const res = await promptApi.tagList({
        name: query.name || undefined,
        tagType: query.tagType || undefined,
      });
      setRows((res.data || []).filter((t) => t.status !== 'DELETED'));
    } catch (e: any) {
      message.error(e?.message || '加载标签失败，请先登录');
    } finally {
      setLoading(false);
    }
  }, [query]);

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
      message.success('已保存');
      setEditOpen(false);
      loadTags();
    } catch (e: any) {
      message.error(e?.message || '保存失败');
    }
  };

  const onDelete = async (row: AiPromptTag) => {
    if (!row.id) return;
    try {
      await promptApi.tagDelete({ id: row.id });
      message.success('已删除');
      loadTags();
    } catch (e: any) {
      message.error(e?.message || '删除失败');
    }
  };

  const columns = [
    {
      title: '类型',
      dataIndex: 'tagType',
      width: 110,
      render: (v: string) =>
        v === 'CONFIG' ? <Tag color="blue">配置</Tag> : <Tag>人工</Tag>,
    },
    {
      title: '标签',
      dataIndex: 'name',
      render: (_: string, row: AiPromptTag) => (
        <Tag color={row.color || undefined}>{row.name}</Tag>
      ),
    },
    {
      title: '色值',
      dataIndex: 'color',
      width: 160,
      render: (v: string) =>
        v ? (
          <Tag color={v}>{v}</Tag>
        ) : (
          '-'
        ),
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (v: string) => (v === 'DISABLED' ? '停用' : '启用'),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      render: (_: unknown, row: AiPromptTag) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(row)}>
            编辑
          </Button>
          <Popconfirm title="确认删除该标签？" onConfirm={() => onDelete(row)}>
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
        标签管理
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
        配置标签：可手打也可随 CSV 导入自动补齐。人工标签：仅在此或编辑页维护，导入不覆盖。
      </Typography.Paragraph>
      <Space style={{ marginBottom: 16 }} wrap>
        <Input
          placeholder="按标签名搜索"
          allowClear
          style={{ width: 220 }}
          value={nameKeyword}
          onChange={(e) => setNameKeyword(e.target.value)}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder="标签类型"
          style={{ width: 140 }}
          options={TAG_TYPE_OPTIONS}
          value={tagTypeFilter}
          onChange={(v) => setTagTypeFilter(v)}
        />
        <Button type="primary" icon={<SearchOutlined />} onClick={onSearch}>
          查询
        </Button>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          新建标签
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
        title={editing ? '编辑标签' : '新建标签'}
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={onSave}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="tagType" label="标签类型" rules={[{ required: true }]}>
            <Select options={TAG_TYPE_OPTIONS} disabled={!!editing} />
          </Form.Item>
          <Form.Item name="name" label="标签名" rules={[{ required: true, max: 64 }]}>
            <Input maxLength={64} disabled={!!editing} />
          </Form.Item>
          <Form.Item name="color" label="颜色" extra="对应 Ant Design Tag 预设色，可空">
            <Select
              allowClear
              placeholder="选择颜色"
              options={
                editing?.color &&
                !COLOR_SELECT_OPTIONS.some((o) => o.value === editing.color)
                  ? [
                      ...COLOR_SELECT_OPTIONS,
                      {
                        value: editing.color,
                        label: (
                          <Tag color={editing.color} style={{ marginInlineEnd: 0 }}>
                            {editing.color}
                          </Tag>
                        ),
                      },
                    ]
                  : COLOR_SELECT_OPTIONS
              }
            />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序（越小越靠前）">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PromptTagPage;
