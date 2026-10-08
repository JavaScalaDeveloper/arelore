import React, { useEffect, useState } from 'react';
import { Button, Form, Image, Input, Modal, Space, Table, Tag, message } from 'antd';
import { CloudSyncOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { parseExtInfo, toHttpsUrl } from '../utils/wordMedia';

const DEFAULT_PAGE_SIZE = 20;

function pictureUrls(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const pics = Array.isArray(ext.pictures) ? ext.pictures : [];
  return pics
    .map((p: any) => toHttpsUrl(typeof p === 'string' ? p : p?.url))
    .filter(Boolean);
}

const WordBaseInfoManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    delete filters.pageNum;
    delete filters.pageSize;
    setLoading(true);
    try {
      const res = await adminApi.getWordBaseInfoList({
        ...filters,
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
      setDataSource(res.data?.list || []);
      setTotal(Number(res.data?.total) || 0);
      setPageNum(res.data?.pageNum || nextPageNum);
      setPageSize(res.data?.pageSize || nextPageSize);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  const handleSync = () => {
    Modal.confirm({
      title: '同步单词基础信息',
      content: '将分页扫描全部词条词形，从有道拉取配图等并写入基础表。词量大时较慢，请确认后继续。',
      okText: '开始同步',
      cancelText: '取消',
      onOk: async () => {
        setSyncing(true);
        try {
          const res = await adminApi.syncWordBaseInfo({});
          const d = res.data || {};
          message.success(
            `同步完成：扫描 ${d.scanned ?? 0}，去重 ${d.distinct ?? 0}，新增 ${d.inserted ?? 0}，更新 ${d.updated ?? 0}，失败 ${d.failed ?? 0}，跳过 ${d.skipped ?? 0}`
          );
          loadData({ pageNum: 1 });
        } catch (e: any) {
          message.error(e?.message || '同步失败');
        } finally {
          setSyncing(false);
        }
      },
    });
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h1 style={{ margin: 0 }}>单词基础信息</h1>
        <Button type="primary" icon={<CloudSyncOutlined />} loading={syncing} onClick={handleSync}>
          从词条同步
        </Button>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16 }}
        onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}
      >
        <Form.Item name="languageCode">
          <Input placeholder="语种 code" allowClear style={{ width: 120 }} />
        </Form.Item>
        <Form.Item name="word">
          <Input placeholder="词形前缀" allowClear style={{ width: 160 }} />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
            查询
          </Button>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading || syncing}
        dataSource={dataSource}
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
          { title: '语种', dataIndex: 'languageCode', width: 90 },
          { title: '词形', dataIndex: 'word', width: 160, ellipsis: true },
          {
            title: '配图',
            key: 'pictures',
            width: 220,
            render: (_, row) => {
              const urls = pictureUrls(row.extInfo);
              if (!urls.length) {
                return <Tag>无</Tag>;
              }
              return (
                <Image.PreviewGroup>
                  <Space size={4} wrap>
                    {urls.slice(0, 4).map((url) => (
                      <Image key={url} src={url} width={40} height={40} style={{ objectFit: 'cover' }} />
                    ))}
                  </Space>
                </Image.PreviewGroup>
              );
            },
          },
          {
            title: '音标',
            key: 'phone',
            width: 180,
            ellipsis: true,
            render: (_, row) => {
              const ext = parseExtInfo(row.extInfo);
              const parts = [
                ext.ukphone ? `英 ${ext.ukphone}` : '',
                ext.usphone ? `美 ${ext.usphone}` : '',
              ].filter(Boolean);
              return parts.join(' / ') || '-';
            },
          },
          {
            title: '来源/同步',
            key: 'source',
            width: 200,
            ellipsis: true,
            render: (_, row) => {
              const ext = parseExtInfo(row.extInfo);
              return [ext.source, ext.lastSyncTime].filter(Boolean).join(' · ') || '-';
            },
          },
          { title: '修改时间', dataIndex: 'modifyTime', width: 170 },
        ]}
      />
    </div>
  );
};

export default WordBaseInfoManagement;
