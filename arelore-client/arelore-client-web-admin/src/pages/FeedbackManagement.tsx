import React, { useEffect, useState } from 'react';
import {
  Button,
  Drawer,
  Form,
  Image,
  Input,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  Upload,
  message,
} from 'antd';
import { CloseCircleFilled, MessageOutlined, SearchOutlined, UploadOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';

const { Text, Paragraph } = Typography;
const { TextArea } = Input;

type PendingFile = {
  fileId: number;
  fileName: string;
  contentType: string;
  previewUrl: string;
};

const STATUS_MAP: Record<number, { color: string; label: string }> = {
  1: { color: 'orange', label: '待处理' },
  2: { color: 'blue', label: '处理中' },
  3: { color: 'purple', label: '待用户回复' },
  4: { color: 'default', label: '已关闭' },
};

function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result || ''));
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function toApiFiles(files: PendingFile[]) {
  return files.map(({ fileId, fileName, contentType }) => ({ fileId, fileName, contentType }));
}

const FeedbackManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [queryForm] = Form.useForm();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [detail, setDetail] = useState<any>(null);
  const [replyText, setReplyText] = useState('');
  const [replyFiles, setReplyFiles] = useState<PendingFile[]>([]);
  const [replying, setReplying] = useState(false);
  const [previewMap, setPreviewMap] = useState<Record<number, string>>({});

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    setLoading(true);
    try {
      const idNum = filters.id != null && String(filters.id).trim() !== ''
        ? Number(filters.id)
        : undefined;
      const res = await adminApi.getFeedbackTicketPage({
        id: Number.isFinite(idNum as number) ? idNum : undefined,
        userIdStr: filters.userIdStr?.trim() || undefined,
        productCode: filters.productCode || undefined,
        status: filters.status,
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
      setDataSource(res.data?.list || []);
      setTotal(Number(res.data?.total) || 0);
      setPageNum(res.data?.pageNum || nextPageNum);
      setPageSize(res.data?.pageSize || nextPageSize);
    } catch {
      // toasted
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData({ pageNum: 1, pageSize: 20, filters: {} });
  }, []);

  const loadPreviews = async (ticket: any) => {
    const map: Record<number, string> = {};
    const msgs = ticket?.messages || [];
    for (const m of msgs) {
      for (const f of m.files || []) {
        if (!f?.fileId || map[f.fileId]) continue;
        try {
          const res = await adminApi.getFeedbackFileContent({ fileId: f.fileId });
          const ct = res.data?.contentType || 'image/png';
          map[f.fileId] = `data:${ct};base64,${res.data?.contentBase64 || ''}`;
        } catch {
          // ignore missing
        }
      }
    }
    setPreviewMap(map);
  };

  const openDetail = async (row: any) => {
    try {
      const res = await adminApi.getFeedbackTicketDetail({ id: row.id });
      setDetail(res.data);
      setReplyText('');
      setReplyFiles([]);
      setDrawerOpen(true);
      await loadPreviews(res.data);
    } catch {
      // toasted
    }
  };

  const refreshDetail = async () => {
    if (!detail?.id) return;
    const res = await adminApi.getFeedbackTicketDetail({ id: detail.id });
    setDetail(res.data);
    await loadPreviews(res.data);
    loadData();
  };

  const onUpload = async (file: File) => {
    try {
      const contentBase64 = await fileToBase64(file);
      const res = await adminApi.uploadFeedbackFile({
        contentBase64,
        fileName: file.name,
        contentType: file.type || 'image/png',
      });
      if (res.data?.fileId) {
        setReplyFiles((prev) => [
          ...prev,
          {
            fileId: res.data.fileId,
            fileName: res.data.fileName || file.name,
            contentType: res.data.contentType || file.type || 'image/png',
            previewUrl: contentBase64,
          },
        ]);
        message.success('图片已上传');
      }
    } catch (e: any) {
      if (!e?.notified) message.error(e?.message || '上传失败');
    }
    return false;
  };

  const sendReply = async () => {
    if (!detail?.id) return;
    if (!replyText.trim() && !replyFiles.length) {
      message.warning('请填写回复或上传截图');
      return;
    }
    setReplying(true);
    try {
      await adminApi.replyFeedbackTicket({
        id: detail.id,
        contentText: replyText.trim(),
        files: toApiFiles(replyFiles),
      });
      message.success('已回复');
      setReplyText('');
      setReplyFiles([]);
      await refreshDetail();
    } catch (e: any) {
      if (!e?.notified) message.error(e?.message || '回复失败');
    } finally {
      setReplying(false);
    }
  };

  const closeTicket = async () => {
    if (!detail?.id) return;
    try {
      await adminApi.closeFeedbackTicket({ id: detail.id });
      message.success('已关闭');
      await refreshDetail();
    } catch (e: any) {
      if (!e?.notified) message.error(e?.message || '关闭失败');
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>意见反馈工单</h1>
        <Text type="secondary">跨产品客服工单：查看、回复、关闭</Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16, rowGap: 12 }}
        onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}
      >
        <Form.Item name="id">
          <Input placeholder="工单 ID" allowClear style={{ width: 120 }} />
        </Form.Item>
        <Form.Item name="userIdStr">
          <Input placeholder="用户 ID" allowClear style={{ width: 200 }} />
        </Form.Item>
        <Form.Item name="productCode">
          <Select
            allowClear
            placeholder="产品"
            style={{ width: 140 }}
            options={[
              { value: 'word', label: 'word' },
              { value: 'prompt', label: 'prompt' },
              { value: 'site', label: 'site' },
            ]}
          />
        </Form.Item>
        <Form.Item name="status">
          <Select
            allowClear
            placeholder="状态"
            style={{ width: 140 }}
            options={Object.entries(STATUS_MAP).map(([k, v]) => ({ value: Number(k), label: v.label }))}
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
        scroll={{ x: 1000 }}
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
          { title: '产品', dataIndex: 'productCode', width: 90 },
          {
            title: '用户',
            dataIndex: 'idStr',
            width: 180,
            ellipsis: true,
            render: (v, row) => v || String(row.userId || '-'),
          },
          { title: '标题', dataIndex: 'title', ellipsis: true },
          {
            title: '状态',
            dataIndex: 'status',
            width: 110,
            render: (s: number) => {
              const m = STATUS_MAP[s] || { color: 'default', label: String(s) };
              return <Tag color={m.color}>{m.label}</Tag>;
            },
          },
          { title: '更新时间', dataIndex: 'modifyTime', width: 180 },
          {
            title: '操作',
            width: 100,
            fixed: 'right',
            render: (_, row) => (
              <Button type="link" icon={<MessageOutlined />} onClick={() => openDetail(row)}>
                会话
              </Button>
            ),
          },
        ]}
      />

      <Drawer
        title={detail ? `工单 #${detail.id} · ${detail.productCode}` : '工单详情'}
        width={560}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        destroyOnClose
      >
        {detail && (
          <>
            <Space style={{ marginBottom: 12 }}>
              <Tag color={(STATUS_MAP[detail.status] || {}).color}>
                {(STATUS_MAP[detail.status] || {}).label || detail.status}
              </Tag>
              <Text type="secondary">用户 {detail.idStr || detail.userId}</Text>
              {detail.status !== 4 && (
                <Button size="small" danger onClick={closeTicket}>
                  关闭工单
                </Button>
              )}
            </Space>
            <div
              style={{
                maxHeight: 420,
                overflow: 'auto',
                border: '1px solid #f0f0f0',
                borderRadius: 8,
                padding: 12,
                marginBottom: 16,
                background: '#fafafa',
              }}
            >
              {(detail.messages || []).map((m: any) => (
                <div
                  key={m.id}
                  style={{
                    marginBottom: 12,
                    textAlign: m.senderType === 'ADMIN' ? 'right' : 'left',
                  }}
                >
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {m.senderType === 'ADMIN' ? '客服' : '用户'} · {m.modifyTime}
                  </Text>
                  {m.contentText ? (
                    <Paragraph
                      style={{
                        display: 'inline-block',
                        maxWidth: '85%',
                        margin: '4px 0',
                        padding: '8px 12px',
                        borderRadius: 8,
                        background: m.senderType === 'ADMIN' ? '#e6f4ff' : '#fff',
                        textAlign: 'left',
                        whiteSpace: 'pre-wrap',
                      }}
                    >
                      {m.contentText}
                    </Paragraph>
                  ) : null}
                  <div
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: 6,
                      justifyContent: m.senderType === 'ADMIN' ? 'flex-end' : 'flex-start',
                      marginTop: 4,
                    }}
                  >
                    <Image.PreviewGroup>
                      {(m.files || []).map((f: any) =>
                        previewMap[f.fileId] ? (
                          <Image
                            key={f.fileId}
                            src={previewMap[f.fileId]}
                            alt={f.fileName || 'img'}
                            width={96}
                            height={96}
                            style={{ objectFit: 'cover', borderRadius: 6 }}
                          />
                        ) : (
                          <Text key={f.fileId} type="secondary">
                            附件 #{f.fileId}
                          </Text>
                        )
                      )}
                    </Image.PreviewGroup>
                  </div>
                </div>
              ))}
            </div>
            {detail.status !== 4 && (
              <>
                <TextArea
                  rows={4}
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  placeholder="输入客服回复…"
                />
                <Space style={{ marginTop: 8 }}>
                  <Upload beforeUpload={onUpload} showUploadList={false} accept="image/*">
                    <Button icon={<UploadOutlined />}>上传截图</Button>
                  </Upload>
                  <Button type="primary" loading={replying} onClick={sendReply}>
                    发送回复
                  </Button>
                </Space>
                {replyFiles.length > 0 && (
                  <Image.PreviewGroup>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 10 }}>
                      {replyFiles.map((f) => (
                        <div key={f.fileId} style={{ position: 'relative', width: 88, height: 88 }}>
                          <Image
                            src={f.previewUrl}
                            alt={f.fileName}
                            width={88}
                            height={88}
                            style={{ objectFit: 'cover', borderRadius: 8, border: '1px solid #eee' }}
                          />
                          <CloseCircleFilled
                            onClick={(e) => {
                              e.stopPropagation();
                              setReplyFiles((prev) => prev.filter((x) => x.fileId !== f.fileId));
                            }}
                            style={{
                              position: 'absolute',
                              top: -6,
                              right: -6,
                              fontSize: 18,
                              color: 'rgba(0,0,0,0.55)',
                              background: '#fff',
                              borderRadius: '50%',
                              cursor: 'pointer',
                            }}
                          />
                        </div>
                      ))}
                    </div>
                  </Image.PreviewGroup>
                )}
              </>
            )}
          </>
        )}
      </Drawer>
    </div>
  );
};

export default FeedbackManagement;
