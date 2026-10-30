import React, { useEffect, useState } from 'react';
import {
  Button,
  DatePicker,
  Form,
  Input,
  Select,
  Space,
  Table,
  Tabs,
  Typography,
} from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import { adminApi } from '../api/admin';

const DEFAULT_PAGE_SIZE = 20;
const { Text } = Typography;
const { RangePicker } = DatePicker;

const CLIENT_OPTIONS = [
  { label: '全部端', value: '' },
  { label: 'ANDROID', value: 'ANDROID' },
  { label: 'WEB', value: 'WEB' },
  { label: 'MINI', value: 'MINI' },
];

const PromptVisitManagement: React.FC = () => {
  const [tab, setTab] = useState('stats');
  const [stats, setStats] = useState<any[]>([]);
  const [rows, setRows] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();

  const buildPayload = (filters: any) => {
    const { visitDateRange, ...rest } = filters || {};
    let visitDateFrom: string | undefined;
    let visitDateTo: string | undefined;
    if (Array.isArray(visitDateRange) && visitDateRange[0] && visitDateRange[1]) {
      visitDateFrom = (visitDateRange[0] as Dayjs).format('YYYY-MM-DD');
      visitDateTo = (visitDateRange[1] as Dayjs).format('YYYY-MM-DD');
    }
    return {
      idStr: rest.idStr?.trim() || undefined,
      client: rest.client ? String(rest.client) : undefined,
      visitDateFrom,
      visitDateTo,
    };
  };

  const loadStats = async (filters?: any) => {
    setLoading(true);
    try {
      const res = await adminApi.getPromptVisitStats(buildPayload(filters ?? queryForm.getFieldsValue()));
      setStats(res.data || []);
    } catch {
      // interceptor
    } finally {
      setLoading(false);
    }
  };

  const loadPage = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = overrides?.filters ?? queryForm.getFieldsValue();
    setLoading(true);
    try {
      const res = await adminApi.getPromptVisitPage({
        ...buildPayload(filters),
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
      setRows(res.data?.list || []);
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
    loadStats({});
  }, []);

  const onSearch = (values: any) => {
    if (tab === 'stats') {
      loadStats(values);
    } else {
      loadPage({ pageNum: 1, pageSize, filters: values });
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>提示词 App 访问</h1>
        <Text type="secondary">
          user_ai_prompt_daily_visit · 日 PV=SUM(visit_count)，日 UV=去重用户数
        </Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16, rowGap: 12 }}
        onFinish={onSearch}
      >
        <Form.Item name="idStr">
          <Input placeholder="用户 ID（明细用）" allowClear style={{ width: 220 }} />
        </Form.Item>
        <Form.Item name="client">
          <Select allowClear placeholder="客户端" style={{ width: 140 }} options={CLIENT_OPTIONS} />
        </Form.Item>
        <Form.Item name="visitDateRange" label="访问日">
          <RangePicker allowClear style={{ width: 260 }} />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
              查询
            </Button>
            <Button
              onClick={() => {
                queryForm.resetFields();
                if (tab === 'stats') loadStats({});
                else loadPage({ pageNum: 1, pageSize, filters: {} });
              }}
            >
              重置
            </Button>
          </Space>
        </Form.Item>
      </Form>

      <Tabs
        activeKey={tab}
        onChange={(key) => {
          setTab(key);
          if (key === 'stats') loadStats();
          else loadPage({ pageNum: 1, pageSize });
        }}
        items={[
          {
            key: 'stats',
            label: '按日 PV/UV',
            children: (
              <Table
                rowKey={(r) => `${r.visitDate}-${r.client || 'ALL'}`}
                loading={loading}
                dataSource={stats}
                pagination={false}
                columns={[
                  { title: '访问日', dataIndex: 'visitDate', width: 140 },
                  { title: '端', dataIndex: 'client', width: 120, render: (v) => v || '全部' },
                  { title: 'PV', dataIndex: 'pv', width: 120 },
                  { title: 'UV', dataIndex: 'uv', width: 120 },
                ]}
              />
            ),
          },
          {
            key: 'detail',
            label: '访问明细',
            children: (
              <Table
                rowKey="id"
                loading={loading}
                dataSource={rows}
                scroll={{ x: 960 }}
                pagination={{
                  current: pageNum,
                  pageSize,
                  total,
                  showSizeChanger: true,
                  showTotal: (t) => `共 ${t} 条`,
                  onChange: (p, ps) => loadPage({ pageNum: p, pageSize: ps }),
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
                  { title: '访问日', dataIndex: 'visitDate', width: 120 },
                  { title: '端', dataIndex: 'client', width: 100 },
                  { title: '次数', dataIndex: 'visitCount', width: 90 },
                  { title: '更新时间', dataIndex: 'modifyTime', width: 180 },
                ]}
              />
            ),
          },
        ]}
      />
    </div>
  );
};

export default PromptVisitManagement;
