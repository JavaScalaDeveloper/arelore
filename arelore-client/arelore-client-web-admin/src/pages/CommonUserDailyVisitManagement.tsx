import React, { useEffect, useState } from 'react';
import {
  Button,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Space,
  Table,
  Typography,
  message,
} from 'antd';
import { DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import type { Key } from 'react';
import { adminApi } from '../api/admin';

const DEFAULT_PAGE_SIZE = 20;
const { Text } = Typography;
const { RangePicker } = DatePicker;

const CommonUserDailyVisitManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [queryForm] = Form.useForm();

  const buildPayload = (filters: any) => {
    const { visitDateRange, ...rest } = filters || {};
    let visitDateFrom: string | undefined;
    let visitDateTo: string | undefined;
    if (Array.isArray(visitDateRange) && visitDateRange[0] && visitDateRange[1]) {
      visitDateFrom = (visitDateRange[0] as Dayjs).format('YYYY-MM-DD');
      visitDateTo = (visitDateRange[1] as Dayjs).format('YYYY-MM-DD');
    }
    const idStr = rest.idStr != null && String(rest.idStr).trim() ? String(rest.idStr).trim() : undefined;
    return {
      id: rest.id || undefined,
      idStr,
      visitDateFrom,
      visitDateTo,
    };
  };

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    setLoading(true);
    try {
      const res = await adminApi.getCommonUserDailyVisitList({
        ...buildPayload(filters),
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
      setDataSource(res.data?.list || []);
      setTotal(Number(res.data?.total) || 0);
      setPageNum(res.data?.pageNum || nextPageNum);
      setPageSize(res.data?.pageSize || nextPageSize);
      setSelectedRowKeys([]);
    } catch {
      // 拦截器已提示
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  const confirmDeleteOne = (row: any) => {
    Modal.confirm({
      title: '确认删除',
      content: `删除用户 ${row.idStr || row.userId} 在 ${row.visitDate} 的访问记录（访问 ${row.visitCount ?? 0} 次）？`,
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        setDeleting(true);
        try {
          await adminApi.deleteCommonUserDailyVisit({ id: row.id });
          message.success('删除成功');
          loadData();
        } catch (e: any) {
          if (!e?.notified) {
            message.error(e?.message || '删除失败');
          }
        } finally {
          setDeleting(false);
        }
      },
    });
  };

  const confirmBatchDelete = () => {
    const ids = selectedRowKeys.map((k) => Number(k)).filter((n) => Number.isFinite(n) && n > 0);
    if (!ids.length) {
      message.warning('请先勾选要删除的记录');
      return;
    }
    Modal.confirm({
      title: '批量删除',
      content: `将删除选中的 ${ids.length} 条日访问记录。`,
      okText: '删除',
      okType: 'danger',
      onOk: async () => {
        setDeleting(true);
        try {
          const res = await adminApi.batchDeleteCommonUserDailyVisits({ ids });
          message.success(res.message || `已删除 ${ids.length} 条`);
          loadData();
        } catch (e: any) {
          if (!e?.notified) {
            message.error(e?.message || '批量删除失败');
          }
        } finally {
          setDeleting(false);
        }
      },
    });
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>C 端日访问统计</h1>
        <Text type="secondary">
          arelore_common.common_user_daily_visit（按用户 + 自然日聚合；C 端已登录请求异步累加）
        </Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16, rowGap: 12 }}
        onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}
      >
        <Form.Item name="id">
          <InputNumber placeholder="记录 ID" min={1} style={{ width: 120 }} controls={false} />
        </Form.Item>
        <Form.Item name="idStr">
          <Input placeholder="用户 ID（完整数字）" allowClear style={{ width: 220 }} />
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
                loadData({ pageNum: 1, pageSize, filters: {} });
              }}
            >
              重置
            </Button>
            <Button
              danger
              icon={<DeleteOutlined />}
              disabled={!selectedRowKeys.length}
              loading={deleting}
              onClick={confirmBatchDelete}
            >
              批量删除{selectedRowKeys.length ? `（${selectedRowKeys.length}）` : ''}
            </Button>
          </Space>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading || deleting}
        dataSource={dataSource}
        scroll={{ x: 960 }}
        rowSelection={{
          selectedRowKeys,
          onChange: (keys) => setSelectedRowKeys(keys),
          preserveSelectedRowKeys: true,
        }}
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
          {
            title: '用户 ID',
            dataIndex: 'idStr',
            width: 220,
            ellipsis: true,
            render: (v, row) => (
              <Text code copyable={{ text: String(v || row.userId || '') }}>
                {v || String(row.userId || '-')}
              </Text>
            ),
          },
          { title: '访问日', dataIndex: 'visitDate', width: 130 },
          { title: '访问次数', dataIndex: 'visitCount', width: 110 },
          { title: '更新时间', dataIndex: 'modifyTime', width: 180 },
          {
            title: '操作',
            key: 'actions',
            width: 100,
            fixed: 'right',
            render: (_, row) => (
              <Button type="link" danger icon={<DeleteOutlined />} onClick={() => confirmDeleteOne(row)}>
                删除
              </Button>
            ),
          },
        ]}
      />
    </div>
  );
};

export default CommonUserDailyVisitManagement;
