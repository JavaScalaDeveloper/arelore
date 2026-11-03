import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Drawer,
  Form,
  Image,
  Input,
  List,
  Space,
  Tag,
  Typography,
  Upload,
  message,
} from 'antd';
import { CloseCircleFilled, PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { feedbackApi } from '../api/feedback';
import { shortErrorMessage } from '../utils/request';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

type PendingFile = {
  fileId: number;
  fileName: string;
  contentType: string;
  previewUrl: string;
};

const STATUS_MAP: Record<number, { color: string; label: string }> = {
  1: { color: 'orange', label: '待客服处理' },
  2: { color: 'blue', label: '处理中' },
  3: { color: 'purple', label: '待你回复' },
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

function PendingImageList({
  files,
  onRemove,
}: {
  files: PendingFile[];
  onRemove: (fileId: number) => void;
}) {
  if (!files.length) return null;
  return (
    <Image.PreviewGroup>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 10 }}>
        {files.map((f) => (
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
                onRemove(f.fileId);
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
  );
}

export default function FeedbackPage() {
  const [list, setList] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<any>(null);
  const [form] = Form.useForm();
  const [createFiles, setCreateFiles] = useState<PendingFile[]>([]);
  const [replyText, setReplyText] = useState('');
  const [replyFiles, setReplyFiles] = useState<PendingFile[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [previewMap, setPreviewMap] = useState<Record<number, string>>({});

  const loadList = async () => {
    setLoading(true);
    try {
      const res = await feedbackApi.page({ pageNum: 1, pageSize: 50 });
      setList(res.data?.list || []);
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadList();
  }, []);

  const loadPreviews = async (ticket: any) => {
    const map: Record<number, string> = {};
    for (const m of ticket?.messages || []) {
      for (const f of m.files || []) {
        if (!f?.fileId || map[f.fileId]) continue;
        try {
          const res = await feedbackApi.fileContent({ fileId: f.fileId });
          const ct = res.data?.contentType || 'image/png';
          map[f.fileId] = `data:${ct};base64,${res.data?.contentBase64 || ''}`;
        } catch {
          // ignore
        }
      }
    }
    setPreviewMap(map);
  };

  const openDetail = async (id: number) => {
    try {
      const res = await feedbackApi.detail({ id });
      setDetail(res.data);
      setReplyText('');
      setReplyFiles([]);
      setDetailOpen(true);
      await loadPreviews(res.data);
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    }
  };

  const uploadOne = async (
    file: File,
    setter: React.Dispatch<React.SetStateAction<PendingFile[]>>
  ) => {
    try {
      const contentBase64 = await fileToBase64(file);
      const res = await feedbackApi.upload({
        contentBase64,
        fileName: file.name,
        contentType: file.type || 'image/png',
      });
      if (res.data?.fileId) {
        setter((prev) => [
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
      message.error(shortErrorMessage(e));
    }
    return false;
  };

  const submitCreate = async () => {
    const values = await form.validateFields();
    if (!String(values.contentText || '').trim() && !createFiles.length) {
      message.warning('请填写内容或上传截图');
      return;
    }
    setSubmitting(true);
    try {
      await feedbackApi.create({
        productCode: 'word',
        contentText: String(values.contentText || '').trim(),
        files: toApiFiles(createFiles),
      });
      message.success('反馈已提交');
      setCreateOpen(false);
      form.resetFields();
      setCreateFiles([]);
      loadList();
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    } finally {
      setSubmitting(false);
    }
  };

  const sendReply = async () => {
    if (!detail?.id) return;
    if (!replyText.trim() && !replyFiles.length) {
      message.warning('请填写回复或上传截图');
      return;
    }
    setSubmitting(true);
    try {
      const res = await feedbackApi.reply({
        id: detail.id,
        contentText: replyText.trim(),
        files: toApiFiles(replyFiles),
      });
      setDetail(res.data);
      setReplyText('');
      setReplyFiles([]);
      await loadPreviews(res.data);
      message.success('已发送');
      loadList();
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Title level={3} style={{ margin: 0 }}>
          意见反馈
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            form.resetFields();
            setCreateFiles([]);
            setCreateOpen(true);
          }}
        >
          新建反馈
        </Button>
      </Space>
      <Paragraph type="secondary">每天最多提交 6 条工单；可与客服多轮沟通。</Paragraph>
      <List
        loading={loading}
        dataSource={list}
        locale={{ emptyText: '暂无反馈' }}
        renderItem={(item) => {
          const st = STATUS_MAP[item.status] || { color: 'default', label: String(item.status) };
          return (
            <Card
              size="small"
              style={{ marginBottom: 10, cursor: 'pointer' }}
              onClick={() => openDetail(item.id)}
            >
              <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                <Text strong ellipsis style={{ maxWidth: 220 }}>
                  {item.title || `工单 #${item.id}`}
                </Text>
                <Tag color={st.color}>{st.label}</Tag>
              </Space>
              <Text type="secondary" style={{ fontSize: 12 }}>
                {item.modifyTime}
              </Text>
            </Card>
          );
        }}
      />

      <Drawer
        title="新建反馈"
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        destroyOnClose
        extra={
          <Button type="primary" loading={submitting} onClick={submitCreate}>
            提交
          </Button>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item name="contentText" label="反馈内容">
            <TextArea rows={5} placeholder="请描述问题或建议…" maxLength={2000} showCount />
          </Form.Item>
        </Form>
        <Upload
          beforeUpload={(f) => uploadOne(f, setCreateFiles)}
          showUploadList={false}
          accept="image/*"
        >
          <Button icon={<UploadOutlined />}>上传截图</Button>
        </Upload>
        <PendingImageList
          files={createFiles}
          onRemove={(fileId) => setCreateFiles((prev) => prev.filter((x) => x.fileId !== fileId))}
        />
      </Drawer>

      <Drawer
        title={detail ? `工单 #${detail.id}` : '会话'}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        width={Math.min(480, typeof window !== 'undefined' ? window.innerWidth - 24 : 480)}
        destroyOnClose
      >
        {detail && (
          <>
            <Tag color={(STATUS_MAP[detail.status] || {}).color} style={{ marginBottom: 12 }}>
              {(STATUS_MAP[detail.status] || {}).label}
            </Tag>
            <div
              style={{
                maxHeight: 360,
                overflow: 'auto',
                marginBottom: 12,
                padding: 8,
                background: '#f7f7f7',
                borderRadius: 8,
              }}
            >
              <Image.PreviewGroup>
                {(detail.messages || []).map((m: any) => (
                  <div
                    key={m.id}
                    style={{
                      marginBottom: 10,
                      textAlign: m.senderType === 'USER' ? 'right' : 'left',
                    }}
                  >
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {m.senderType === 'USER' ? '我' : '客服'} · {m.modifyTime}
                    </Text>
                    {m.contentText ? (
                      <Paragraph
                        style={{
                          display: 'inline-block',
                          maxWidth: '85%',
                          margin: '4px 0',
                          padding: '8px 10px',
                          borderRadius: 8,
                          background: m.senderType === 'USER' ? '#e6f4ff' : '#fff',
                          textAlign: 'left',
                          whiteSpace: 'pre-wrap',
                        }}
                      >
                        {m.contentText}
                      </Paragraph>
                    ) : null}
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, justifyContent: m.senderType === 'USER' ? 'flex-end' : 'flex-start' }}>
                      {(m.files || []).map((f: any) =>
                        previewMap[f.fileId] ? (
                          <Image
                            key={f.fileId}
                            src={previewMap[f.fileId]}
                            alt={f.fileName || ''}
                            width={96}
                            height={96}
                            style={{ objectFit: 'cover', borderRadius: 6 }}
                          />
                        ) : null
                      )}
                    </div>
                  </div>
                ))}
              </Image.PreviewGroup>
            </div>
            {detail.status !== 4 && (
              <>
                <TextArea
                  rows={3}
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  placeholder="继续补充…"
                />
                <Space style={{ marginTop: 8 }}>
                  <Upload
                    beforeUpload={(f) => uploadOne(f, setReplyFiles)}
                    showUploadList={false}
                    accept="image/*"
                  >
                    <Button icon={<UploadOutlined />}>截图</Button>
                  </Upload>
                  <Button type="primary" loading={submitting} onClick={sendReply}>
                    发送
                  </Button>
                </Space>
                <PendingImageList
                  files={replyFiles}
                  onRemove={(fileId) => setReplyFiles((prev) => prev.filter((x) => x.fileId !== fileId))}
                />
              </>
            )}
          </>
        )}
      </Drawer>
    </div>
  );
}
