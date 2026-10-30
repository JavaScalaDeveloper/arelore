import React, { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Space, Table, Typography, message } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { useLocaleNavigate } from '../locale/navigation';

const DEFAULT_PAGE_SIZE = 20;
const { Text } = Typography;

const PromptPrefManagement: React.FC = () => {
  const navigate = useLocaleNavigate();
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();
  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<any>(null);

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    setLoading(true);
    try {
      const res = await adminApi.getPromptPrefPage({
        idStr: filters.idStr?.trim() || undefined,
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
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  const openDetail = async (row: any) => {
    try {
      const res = await adminApi.getPromptPrefDetail({
        id: row.id,
        idStr: row.idStr,
      });
      setDetail(res.data || row);
      setDetailOpen(true);
    } catch (e: any) {
      message.error(e?.message || '加载详情失败');
    }
  };

  const openUserPrompts = (row: any) => {
    const idStr = row.idStr || String(row.userId || '');
    if (!idStr) {
      message.warning('缺少用户 ID');
      return;
    }
    sessionStorage.setItem('prompt_admin_user_id_str', idStr);
    navigate('/prompt/user-prompts');
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>提示词用户偏好</h1>
        <Text type="secondary">user_ai_prompt_pref（字体/复制字段等在 ext_json）</Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16, rowGap: 12 }}
        onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}
      >
        <Form.Item name="idStr">
          <Input placeholder="用户 ID（完整数字）" allowClear style={{ width: 240 }} />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
              查询
            </Button>
            <Button
              onClick={() => {
                queryForm.resetFields();
                loadData({ pageNum: 1, pageSize, filters: {} });
              }}
            >
              重置
            </Button>
          </Space>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading}
        dataSource={dataSource}
        scroll={{ x: 1100 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, ps) => loadData({ pageNum: p, pageSize: ps }),
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          {
            title: '用户 ID',
            dataIndex: 'idStr',
            width: 200,
            render: (v, row) => (
              <Text code copyable={{ text: String(v || row.userId || '') }}>
                {v || String(row.userId || '-')}
              </Text>
            ),
          },
          { title: '字号倍率', dataIndex: 'fontScale', width: 100 },
          { title: '字号 sp', dataIndex: 'fontSizeSp', width: 90 },
          {
            title: '复制字段',
            dataIndex: 'copyFields',
            ellipsis: true,
            render: (v: string[]) => (Array.isArray(v) ? v.join(', ') : '-'),
          },
          { title: '状态', dataIndex: 'status', width: 100 },
          { title: '更新时间', dataIndex: 'modifyTime', width: 180 },
          {
            title: '操作',
            key: 'actions',
            width: 200,
            fixed: 'right' as const,
            render: (_: unknown, row: any) => (
              <Space>
                <Button type="link" size="small" onClick={() => openDetail(row)}>
                  详情
                </Button>
                <Button type="link" size="small" onClick={() => openUserPrompts(row)}>
                  看提示词
                </Button>
              </Space>
            ),
          },
        ]}
      />
      <Modal
        title="偏好详情"
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={null}
        width={640}
      >
        {detail ? (
          <pre style={{ whiteSpace: 'pre-wrap', margin: 0 }}>
            {JSON.stringify(
              {
                id: detail.id,
                idStr: detail.idStr,
                fontScale: detail.fontScale,
                fontSizeSp: detail.fontSizeSp,
                copyFields: detail.copyFields,
                copySep: detail.copySep,
                clientVersion: detail.clientVersion,
                status: detail.status,
                extJson: detail.extJson,
              },
              null,
              2
            )}
          </pre>
        ) : null}
      </Modal>
    </div>
  );
};

export default PromptPrefManagement;
