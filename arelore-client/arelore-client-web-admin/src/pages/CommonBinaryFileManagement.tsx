import React, { useEffect, useState } from 'react';
import {
  Button,
  DatePicker,
  Descriptions,
  Form,
  Image,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { DeleteOutlined, EyeOutlined, SearchOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import type { Key } from 'react';
import { adminApi } from '../api/admin';

const DEFAULT_PAGE_SIZE = 20;
const { Text, Paragraph } = Typography;
const { RangePicker } = DatePicker;

const DATETIME_FMT = 'YYYY-MM-DD HH:mm:ss';

const SORT_OPTIONS = [
  { value: 'createTime', label: '创建时间' },
  { value: 'modifyTime', label: '修改时间' },
  { value: 'fileSize', label: '文件大小' },
];

function formatSizeFallback(bytes?: number | null, text?: string) {
  if (text) return text;
  if (bytes == null || !Number.isFinite(Number(bytes))) return '-';
  const n = Number(bytes);
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(n >= 10240 ? 0 : 1)} KB`;
  if (n < 1024 * 1024 * 1024) return `${(n / 1024 / 1024).toFixed(n >= 10485760 ? 0 : 2)} MB`;
  return `${(n / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

function rangeToBounds(range?: [Dayjs, Dayjs] | null): { from?: string; to?: string } {
  if (!range || !range[0] || !range[1]) {
    return {};
  }
  return {
    from: range[0].startOf('day').format(DATETIME_FMT),
    to: range[1].endOf('day').format(DATETIME_FMT),
  };
}

/** 把表单值转成后端 list 入参 */
function buildListPayload(filters: any) {
  const {
    createTimeRange,
    modifyTimeRange,
    sortFields,
    fileSizeMin,
    fileSizeMax,
    ...rest
  } = filters || {};
  const createBounds = rangeToBounds(createTimeRange);
  const modifyBounds = rangeToBounds(modifyTimeRange);
  const sorts = Array.isArray(sortFields) ? sortFields.filter(Boolean) : [];
  return {
    ...rest,
    createTimeFrom: createBounds.from,
    createTimeTo: createBounds.to,
    modifyTimeFrom: modifyBounds.from,
    modifyTimeTo: modifyBounds.to,
    fileSizeMin: fileSizeMin == null || fileSizeMin === '' ? undefined : Number(fileSizeMin),
    fileSizeMax: fileSizeMax == null || fileSizeMax === '' ? undefined : Number(fileSizeMax),
    sortFields: sorts.length ? sorts : undefined,
  };
}

const CommonBinaryFileManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<any | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [deleting, setDeleting] = useState(false);

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    setLoading(true);
    try {
      const res = await adminApi.getCommonBinaryFileList({
        ...buildListPayload(filters),
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

  const confirmDeleteOne = (row: any) => {
    Modal.confirm({
      title: '确认删除',
      content: `将物理删除二进制文件 #${row.id}${row.fileSizeText ? `（${row.fileSizeText}）` : ''}。引用它的词条试听会失败，可用「批量生成释义语音」重新生成。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        setDeleting(true);
        try {
          await adminApi.deleteCommonBinaryFile({ id: row.id });
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
      content: `将物理删除选中的 ${ids.length} 条二进制文件。词条侧若仍引用这些 fileId，「试听」会报文件不存在；可用「批量生成释义语音」补回。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        setDeleting(true);
        try {
          const res = await adminApi.batchDeleteCommonBinaryFiles({ ids });
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

  useEffect(() => {
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  const openDetail = async (row: any) => {
    setDetailOpen(true);
    setDetail(null);
    setDetailLoading(true);
    try {
      const res = await adminApi.getCommonBinaryFileDetail({ id: row.id });
      setDetail(res.data || null);
    } catch (e: any) {
      if (!e?.notified) {
        message.error(e?.message || '加载详情失败');
      }
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  };

  const previewSrc =
    detail?.base64Data && detail?.contentType
      ? `data:${detail.contentType};base64,${detail.base64Data}`
      : detail?.base64Data
        ? `data:application/octet-stream;base64,${detail.base64Data}`
        : '';

  const isImage = String(detail?.contentType || '').startsWith('image/');
  const isAudio = String(detail?.contentType || '').startsWith('audio/');

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>二进制文件</h1>
        <Text type="secondary">arelore_common.common_binary_file（列表不含文件内容）</Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16, rowGap: 12 }}
        onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}
        initialValues={{ sortFields: [] }}
      >
        <Form.Item name="id">
          <InputNumber placeholder="ID" min={1} style={{ width: 120 }} controls={false} />
        </Form.Item>
        <Form.Item name="sourceFilter">
          <Select
            allowClear
            placeholder="来源"
            style={{ width: 160 }}
            options={[
              { value: 'PIXABAY', label: 'PIXABAY' },
              { value: 'YOUDAO', label: 'YOUDAO' },
              { value: 'QWEN3_TTS', label: 'QWEN3_TTS' },
            ]}
          />
        </Form.Item>
        <Form.Item name="contentType">
          <Input placeholder="contentType 模糊" allowClear style={{ width: 160 }} />
        </Form.Item>
        <Form.Item name="status">
          <Select
            allowClear
            placeholder="状态"
            style={{ width: 100 }}
            options={[
              { value: 1, label: '有效' },
              { value: 0, label: '无效' },
            ]}
          />
        </Form.Item>
        <Form.Item name="createTimeRange" label="创建时间">
          <RangePicker allowClear style={{ width: 260 }} />
        </Form.Item>
        <Form.Item name="modifyTimeRange" label="修改时间">
          <RangePicker allowClear style={{ width: 260 }} />
        </Form.Item>
        <Form.Item name="fileSizeMin" label="大小≥">
          <InputNumber
            placeholder="字节"
            min={0}
            style={{ width: 120 }}
            controls={false}
          />
        </Form.Item>
        <Form.Item name="fileSizeMax" label="大小≤">
          <InputNumber
            placeholder="字节"
            min={0}
            style={{ width: 120 }}
            controls={false}
          />
        </Form.Item>
        <Form.Item name="sortFields" label="排序">
          <Select
            mode="multiple"
            allowClear
            placeholder="默认按 ID 降序"
            style={{ minWidth: 220 }}
            options={SORT_OPTIONS}
            maxTagCount="responsive"
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
              查询
            </Button>
            <Button
              onClick={() => {
                queryForm.resetFields();
                loadData({ pageNum: 1, pageSize, filters: { sortFields: [] } });
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
        scroll={{ x: 1100 }}
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
            title: '来源',
            dataIndex: 'source',
            width: 120,
            render: (v) => (v ? <Tag>{v}</Tag> : '-'),
          },
          {
            title: '类型',
            dataIndex: 'contentType',
            width: 140,
            ellipsis: true,
            render: (v) => v || '-',
          },
          {
            title: '大小',
            key: 'fileSize',
            width: 110,
            render: (_, row) => formatSizeFallback(row.fileSize, row.fileSizeText),
          },
          {
            title: '引用',
            dataIndex: 'refCount',
            width: 70,
            render: (v) => (v == null ? '-' : v),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 80,
            render: (v) =>
              v === 1 ? <Tag color="success">有效</Tag> : <Tag>无效({String(v)})</Tag>,
          },
          {
            title: '哈希',
            dataIndex: 'hashHex',
            width: 160,
            ellipsis: true,
            render: (v) => (v ? <Text code copyable={{ text: v }}>{String(v).slice(0, 12)}…</Text> : '-'),
          },
          { title: '创建时间', dataIndex: 'createTime', width: 170 },
          { title: '修改时间', dataIndex: 'modifyTime', width: 170 },
          {
            title: '操作',
            key: 'actions',
            width: 160,
            fixed: 'right',
            render: (_, row) => (
              <Space size={0}>
                <Button type="link" icon={<EyeOutlined />} onClick={() => openDetail(row)}>
                  详情
                </Button>
                <Button
                  type="link"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => confirmDeleteOne(row)}
                >
                  删除
                </Button>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        title={detail ? `二进制文件 · #${detail.id}` : '二进制文件详情'}
        open={detailOpen}
        onCancel={() => {
          setDetailOpen(false);
          setDetail(null);
        }}
        footer={
          detail ? (
            <Space>
              <Button
                danger
                icon={<DeleteOutlined />}
                loading={deleting}
                onClick={() => {
                  setDetailOpen(false);
                  confirmDeleteOne(detail);
                }}
              >
                删除
              </Button>
              <Button
                onClick={() => {
                  setDetailOpen(false);
                  setDetail(null);
                }}
              >
                关闭
              </Button>
            </Space>
          ) : null
        }
        width={820}
        destroyOnClose
        confirmLoading={detailLoading}
      >
        {detailLoading && !detail ? (
          <Text type="secondary">加载中…</Text>
        ) : detail ? (
          <>
            <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="状态">
                {detail.status === 1 ? <Tag color="success">有效</Tag> : <Tag>无效</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="来源">{detail.source || '-'}</Descriptions.Item>
              <Descriptions.Item label="类型">{detail.contentType || '-'}</Descriptions.Item>
              <Descriptions.Item label="大小">
                {formatSizeFallback(detail.fileSize, detail.fileSizeText)}
                {detail.fileSize != null ? (
                  <Text type="secondary">（{detail.fileSize} 字节）</Text>
                ) : null}
              </Descriptions.Item>
              <Descriptions.Item label="引用">{detail.refCount ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{detail.createTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="修改时间">{detail.modifyTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="哈希" span={2}>
                {detail.hashHex ? (
                  <Paragraph copyable style={{ marginBottom: 0 }}>
                    <Text code>{detail.hashHex}</Text>
                  </Paragraph>
                ) : (
                  '-'
                )}
              </Descriptions.Item>
              <Descriptions.Item label="ext_json" span={2}>
                <Paragraph
                  copyable={!!detail.extJson}
                  style={{ marginBottom: 0, maxHeight: 120, overflow: 'auto' }}
                >
                  <Text code>{detail.extJson || '-'}</Text>
                </Paragraph>
              </Descriptions.Item>
            </Descriptions>

            <Text strong>文件内容预览</Text>
            <div style={{ marginTop: 8, padding: 12, background: '#fafafa', borderRadius: 8 }}>
              {!previewSrc ? (
                <Tag>无文件内容</Tag>
              ) : isImage ? (
                <Image src={previewSrc} style={{ maxWidth: '100%', maxHeight: 420 }} />
              ) : isAudio ? (
                <audio controls src={previewSrc} style={{ width: '100%' }} />
              ) : (
                <Space direction="vertical">
                  <Tag>非图片/音频，可下载查看</Tag>
                  <a href={previewSrc} download={`file-${detail.id}.bin`}>
                    下载文件
                  </a>
                </Space>
              )}
            </div>
          </>
        ) : null}
      </Modal>
    </div>
  );
};

export default CommonBinaryFileManagement;
