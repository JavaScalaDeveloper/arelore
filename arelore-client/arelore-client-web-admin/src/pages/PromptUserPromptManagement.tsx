import React, { useEffect, useState } from 'react';
import { Button, Form, Input, Space, Table, Tag, Typography, message } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';

const DEFAULT_PAGE_SIZE = 20;
const { Text } = Typography;

const PromptUserPromptManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    const idStr = String(filters.idStr || '').trim();
    if (!idStr) {
      message.warning('请填写用户 ID');
      return;
    }
    setLoading(true);
    try {
      const res = await adminApi.getPromptUserPromptPage({
        idStr,
        titleKeyword: filters.titleKeyword?.trim() || undefined,
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
      setDataSource(res.data?.list || []);
      setTotal(Number(res.data?.total) || 0);
      setPageNum(res.data?.pageNum || nextPageNum);
      setPageSize(res.data?.pageSize || nextPageSize);
    } catch {
      // interceptor
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const cached = sessionStorage.getItem('prompt_admin_user_id_str');
    if (cached) {
      queryForm.setFieldsValue({ idStr: cached });
      loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: { idStr: cached } });
    }
    const handler = (e: Event) => {
      const idStr = (e as CustomEvent).detail?.idStr;
      if (idStr) {
        queryForm.setFieldsValue({ idStr });
        loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: { idStr } });
      }
    };
    window.addEventListener('prompt-admin-goto-user-prompts', handler);
    return () => window.removeEventListener('prompt-admin-goto-user-prompts', handler);
  }, []);

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>用户提示词</h1>
        <Text type="secondary">按用户查看其 user_ai_prompt 配置内容</Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16, rowGap: 12 }}
        onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}
      >
        <Form.Item name="idStr" rules={[{ required: true, message: '必填' }]}>
          <Input placeholder="用户 ID（完整数字）" allowClear style={{ width: 240 }} />
        </Form.Item>
        <Form.Item name="titleKeyword">
          <Input placeholder="标题关键词" allowClear style={{ width: 180 }} />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
              查询
            </Button>
            <Button
              onClick={() => {
                queryForm.resetFields();
                setDataSource([]);
                setTotal(0);
              }}
            >
              重置
            </Button>
          </Space>
        </Form.Item>
      </Form>
      <Table
        rowKey={(r) => String(r.id || r.promptUuid)}
        loading={loading}
        dataSource={dataSource}
        scroll={{ x: 1200 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, ps) => loadData({ pageNum: p, pageSize: ps }),
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 90 },
          { title: '标题', dataIndex: 'title', width: 200, ellipsis: true },
          { title: '状态', dataIndex: 'publishStatus', width: 110 },
          {
            title: '配置标签',
            dataIndex: 'configTagList',
            width: 160,
            render: (list: string[]) =>
              (list || []).length
                ? list.map((t) => (
                    <Tag key={t} color="blue">
                      {t}
                    </Tag>
                  ))
                : '-',
          },
          {
            title: '人工标签',
            dataIndex: 'manualTagList',
            width: 160,
            render: (list: string[]) =>
              (list || []).length
                ? list.map((t) => <Tag key={t}>{t}</Tag>)
                : '-',
          },
          { title: 'UUID', dataIndex: 'promptUuid', width: 140, ellipsis: true },
          { title: '更新时间', dataIndex: 'modifyTime', width: 180 },
        ]}
      />
    </div>
  );
};

export default PromptUserPromptManagement;
