import React, { useEffect, useState } from 'react';
import {
  Button,
  Checkbox,
  Descriptions,
  Form,
  Image,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { CloudSyncOutlined, EyeOutlined, PictureOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { parseExtInfo, toHttpsUrl } from '../utils/wordMedia';

const DEFAULT_PAGE_SIZE = 20;
const { Paragraph, Text } = Typography;

function pictureUrls(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const pics = Array.isArray(ext.pictures) ? ext.pictures : [];
  return pics
    .map((p: any) => toHttpsUrl(typeof p === 'string' ? p : p?.url))
    .filter(Boolean);
}

const WordBaseInfoManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [languages, setLanguages] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<any | null>(null);
  const [youdaoTesting, setYoudaoTesting] = useState(false);
  const [youdaoResult, setYoudaoResult] = useState<any | null>(null);

  const [searchingPics, setSearchingPics] = useState(false);
  const [adoptingPics, setAdoptingPics] = useState(false);
  const [candidates, setCandidates] = useState<any[]>([]);
  const [selectedUrls, setSelectedUrls] = useState<string[]>([]);

  const loadLanguages = async () => {
    const res = await adminApi.getWordLanguageList({ pageNum: 1, pageSize: 100 });
    setLanguages(res.data?.list || []);
  };

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
    loadLanguages();
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  const openDetail = async (row: any) => {
    setDetailOpen(true);
    setYoudaoResult(null);
    setCandidates([]);
    setSelectedUrls([]);
    setDetailLoading(true);
    try {
      const res = await adminApi.getWordBaseInfoDetail({ id: row.id });
      setDetail(res.data || row);
    } catch (e: any) {
      message.error(e?.message || '加载详情失败');
      setDetail(row);
    } finally {
      setDetailLoading(false);
    }
  };

  const closeDetail = () => {
    setDetailOpen(false);
    setDetail(null);
    setYoudaoResult(null);
    setCandidates([]);
    setSelectedUrls([]);
  };

  const testYoudao = async () => {
    const word = detail?.word;
    if (!word) {
      message.warning('无词形可测');
      return;
    }
    setYoudaoTesting(true);
    try {
      const res = await adminApi.testWordBaseInfoYoudao({ word });
      setYoudaoResult(res.data || null);
      if (res.data?.success) {
        message.success(`有道请求成功（${res.data.elapsedMs ?? '-'}ms）`);
      } else {
        message.error(res.data?.error || '有道请求失败');
      }
    } catch (e: any) {
      message.error(e?.message || '有道请求失败');
    } finally {
      setYoudaoTesting(false);
    }
  };

  const searchStockPictures = async () => {
    if (!detail?.id) {
      return;
    }
    setSearchingPics(true);
    setSelectedUrls([]);
    setCandidates([]);
    try {
      const res = await adminApi.searchWordBaseInfoPictures({ id: detail.id });
      const list = res.data?.candidates || [];
      setCandidates(list);
      if (list.length) {
        message.success(`已搜到 ${list.length} 张候选（未落库，未采纳下次不会保留）`);
      } else {
        message.warning('未搜到候选图');
      }
    } catch (e: any) {
      message.error(e?.message || '图库搜索失败，请确认已配置 Unsplash Access Key');
    } finally {
      setSearchingPics(false);
    }
  };

  const adoptSelected = async () => {
    if (!detail?.id) {
      return;
    }
    if (!selectedUrls.length) {
      message.warning('请先勾选要采纳的图片');
      return;
    }
    setAdoptingPics(true);
    try {
      const res = await adminApi.adoptWordBaseInfoPictures({
        id: detail.id,
        pictureUrls: selectedUrls,
      });
      setDetail(res.data || detail);
      setCandidates([]);
      setSelectedUrls([]);
      message.success('已采纳并锁定，自动同步不会覆盖');
      loadData();
    } catch (e: any) {
      message.error(e?.message || '采纳失败');
    } finally {
      setAdoptingPics(false);
    }
  };

  const toggleCandidate = (url: string, checked: boolean) => {
    setSelectedUrls((prev) => {
      if (checked) {
        return prev.includes(url) ? prev : [...prev, url];
      }
      return prev.filter((u) => u !== url);
    });
  };

  const handleSync = () => {
    Modal.confirm({
      title: '同步单词基础信息',
      content: '将分页扫描全部词条词形，从有道拉取配图等并写入基础表。已人工确认的配图不会被覆盖。',
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

  const detailExt = parseExtInfo(detail?.extInfo);
  const detailPics = pictureUrls(detail?.extInfo);
  const pictureConfirmed = !!detailExt.pictureConfirmed;

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
          <Select
            allowClear
            showSearch
            optionFilterProp="label"
            placeholder="语种"
            style={{ width: 180 }}
            options={languages.map((item) => ({
              value: item.code,
              label: `${item.code} - ${item.name}`,
            }))}
          />
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
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '语种', dataIndex: 'languageCode', width: 90 },
          { title: '词形', dataIndex: 'word', width: 160, ellipsis: true },
          {
            title: '配图',
            key: 'pictures',
            width: 220,
            render: (_, row) => {
              const urls = pictureUrls(row.extInfo);
              const confirmed = !!parseExtInfo(row.extInfo).pictureConfirmed;
              if (!urls.length) {
                return <Tag>无</Tag>;
              }
              return (
                <Space size={4} wrap>
                  {confirmed && <Tag color="success">已确认</Tag>}
                  <Image.PreviewGroup>
                    <Space size={4} wrap>
                      {urls.slice(0, 4).map((url) => (
                        <Image key={url} src={url} width={40} height={40} style={{ objectFit: 'cover' }} />
                      ))}
                    </Space>
                  </Image.PreviewGroup>
                </Space>
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
              if (ext.syncError) {
                return (
                  <span title={String(ext.syncError)} style={{ color: '#cf1322' }}>
                    {[ext.source, ext.lastFailTime || ext.lastSyncTime].filter(Boolean).join(' · ')}
                    {` · ${ext.syncError}`}
                  </span>
                );
              }
              return [ext.source, ext.lastSyncTime].filter(Boolean).join(' · ') || '-';
            },
          },
          { title: '修改时间', dataIndex: 'modifyTime', width: 170 },
          {
            title: '操作',
            key: 'actions',
            width: 100,
            fixed: 'right',
            render: (_, row) => (
              <Button type="link" icon={<EyeOutlined />} onClick={() => openDetail(row)}>
                详情
              </Button>
            ),
          },
        ]}
      />

      <Modal
        title={detail ? `基础信息详情 · ${detail.word}` : '基础信息详情'}
        open={detailOpen}
        onCancel={closeDetail}
        footer={null}
        width={900}
        destroyOnClose
        confirmLoading={detailLoading}
      >
        {detail && (
          <>
            <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="语种">{detail.languageCode}</Descriptions.Item>
              <Descriptions.Item label="词形" span={2}>
                {detail.word}
              </Descriptions.Item>
              <Descriptions.Item label="英音">{detailExt.ukphone || '-'}</Descriptions.Item>
              <Descriptions.Item label="美音">{detailExt.usphone || '-'}</Descriptions.Item>
              <Descriptions.Item label="配图状态">
                {pictureConfirmed ? <Tag color="success">已人工确认（锁定）</Tag> : <Tag>未确认</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="确认时间">{detailExt.pictureConfirmedAt || '-'}</Descriptions.Item>
              <Descriptions.Item label="来源">{detailExt.source || '-'}</Descriptions.Item>
              <Descriptions.Item label="最近成功同步">{detailExt.lastSyncTime || '-'}</Descriptions.Item>
            </Descriptions>

            <div style={{ marginBottom: 16 }}>
              <Text strong>正式配图（学习端使用）</Text>
              <div style={{ marginTop: 8 }}>
                {detailPics.length ? (
                  <Image.PreviewGroup>
                    <Space wrap>
                      {detailPics.map((url) => (
                        <Image key={url} src={url} width={96} height={96} style={{ objectFit: 'cover' }} />
                      ))}
                    </Space>
                  </Image.PreviewGroup>
                ) : (
                  <Tag>无配图</Tag>
                )}
              </div>
            </div>

            <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', borderRadius: 8 }}>
              <Space style={{ marginBottom: 8 }} wrap>
                <Text strong>图库补图（Unsplash）</Text>
                <Button
                  type="primary"
                  icon={<PictureOutlined />}
                  loading={searchingPics}
                  onClick={searchStockPictures}
                >
                  搜索至少 4 张候选
                </Button>
                <Button
                  type="default"
                  disabled={!selectedUrls.length}
                  loading={adoptingPics}
                  onClick={adoptSelected}
                >
                  采纳选中（{selectedUrls.length}）
                </Button>
              </Space>
              <Paragraph type="secondary" style={{ marginBottom: 8 }}>
                候选仅本次展示、不落库；未采纳下次搜图不会保留。请配置 Unsplash Access Key（
                <a href="https://unsplash.com/developers" target="_blank" rel="noreferrer">申请</a>
                ）。无水印、可免费使用。已确认配图不会被有道同步覆盖。
              </Paragraph>
              {candidates.length > 0 && (
                <Space wrap size={12}>
                  {candidates.map((c) => {
                    const url = toHttpsUrl(c.url);
                    const thumb = toHttpsUrl(c.thumbUrl || c.url);
                    const checked = selectedUrls.includes(url);
                    return (
                      <div
                        key={url}
                        style={{
                          width: 140,
                          border: checked ? '2px solid #1677ff' : '1px solid #e5e7eb',
                          borderRadius: 8,
                          padding: 8,
                          background: '#fff',
                        }}
                      >
                        <Checkbox
                          checked={checked}
                          onChange={(e) => toggleCandidate(url, e.target.checked)}
                          style={{ marginBottom: 6 }}
                        >
                          选中
                        </Checkbox>
                        <Image src={thumb} width="100%" height={90} style={{ objectFit: 'cover' }} />
                        <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }} title={c.photographer}>
                          {c.photographer || 'Pexels'}
                        </div>
                      </div>
                    );
                  })}
                </Space>
              )}
            </div>

            <div style={{ marginBottom: 8 }}>
              <Text strong>外部接口测试</Text>
              <div style={{ marginTop: 8 }}>
                <Space wrap>
                  <Button loading={youdaoTesting} onClick={testYoudao}>
                    测试有道 jsonapi
                  </Button>
                  <Button
                    href={`https://dict.youdao.com/jsonapi?q=${encodeURIComponent(detail.word || '')}`}
                    target="_blank"
                    rel="noreferrer"
                  >
                    浏览器打开有道
                  </Button>
                </Space>
              </div>
            </div>

            {youdaoResult && (
              <div style={{ marginTop: 12 }}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="成功">
                    {youdaoResult.success ? <Tag color="success">是</Tag> : <Tag color="error">否</Tag>}
                  </Descriptions.Item>
                  <Descriptions.Item label="含 pic_dict">
                    {youdaoResult.hasPicDict ? <Tag color="blue">是</Tag> : <Tag>否</Tag>}
                  </Descriptions.Item>
                  <Descriptions.Item label="配图数">{youdaoResult.pictureCount ?? 0}</Descriptions.Item>
                  <Descriptions.Item label="说明" span={1}>
                    {youdaoResult.note || youdaoResult.error || '-'}
                  </Descriptions.Item>
                </Descriptions>
              </div>
            )}

            <div style={{ marginTop: 16 }}>
              <Text strong>extInfo</Text>
              <Paragraph
                copyable
                style={{
                  marginTop: 8,
                  background: '#f5f5f5',
                  padding: 12,
                  borderRadius: 6,
                  maxHeight: 160,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {detail.extInfo || '{}'}
              </Paragraph>
            </div>
          </>
        )}
      </Modal>
    </div>
  );
};

export default WordBaseInfoManagement;
