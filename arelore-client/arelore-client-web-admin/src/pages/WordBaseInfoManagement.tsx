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
import { CloudSyncOutlined, EyeOutlined, PictureOutlined, SearchOutlined, SoundOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { parseExtInfo, toHttpsUrl } from '../utils/wordMedia';

const DEFAULT_PAGE_SIZE = 20;
const { Paragraph, Text } = Typography;

type PictureRef = { key: string; url?: string; fileId?: number };

function pictureRefs(extInfo?: string): PictureRef[] {
  const ext = parseExtInfo(extInfo);
  const pics = Array.isArray(ext.pictures) ? ext.pictures : [];
  return pics
    .map((p: any, index: number) => {
      if (typeof p === 'string') {
        const url = toHttpsUrl(p);
        return url ? { key: url, url } : null;
      }
      const url = toHttpsUrl(p?.url);
      const fileId = p?.fileId != null ? Number(p.fileId) : undefined;
      if (!url && !fileId) {
        return null;
      }
      return { key: fileId ? `file-${fileId}` : url || String(index), url, fileId };
    })
    .filter(Boolean) as PictureRef[];
}

function pictureUrls(extInfo?: string): string[] {
  return pictureRefs(extInfo)
    .map((p) => p.url)
    .filter((url): url is string => !!url);
}

function StoredPicture({ item, width, height }: { item: PictureRef; width: number; height: number }) {
  const [src, setSrc] = useState(item.fileId ? '' : item.url || '');

  useEffect(() => {
    if (!item.fileId) {
      setSrc(item.url || '');
      return undefined;
    }
    let cancel = false;
    adminApi
      .getWordBaseInfoPictureContent({ id: item.fileId })
      .then((res) => {
        const data = res?.data;
        if (cancel) {
          return;
        }
        if (data?.base64Data) {
          setSrc(`data:${data.contentType || 'image/jpeg'};base64,${data.base64Data}`);
          return;
        }
        setSrc(item.url || '');
      })
      .catch(() => {
        if (!cancel) {
          setSrc(item.url || '');
        }
      });
    return () => {
      cancel = true;
    };
  }, [item.fileId, item.url]);

  if (!src) {
    return null;
  }
  return <Image src={src} width={width} height={height} style={{ objectFit: 'cover' }} />;
}

function meaningLines(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const meanings = Array.isArray(ext.meanings) ? ext.meanings : [];
  return meanings
    .map((m: any) => {
      const pos = m?.pos ? `${m.pos} ` : '';
      const trans = m?.trans || '';
      return `${pos}${trans}`.trim();
    })
    .filter(Boolean);
}

function exampleLines(extInfo?: string): { en: string; cn: string }[] {
  const ext = parseExtInfo(extInfo);
  const sentences = ext?.sentence?.sentences;
  if (!Array.isArray(sentences)) {
    return [];
  }
  return sentences
    .map((s: any) => ({
      en: String(s?.sContent || s?.sContent_eng || '').trim(),
      cn: String(s?.sCn || '').trim(),
    }))
    .filter((s: { en: string; cn: string }) => s.en || s.cn)
    .slice(0, 5);
}

function phraseLines(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const phrases = Array.isArray(ext.phrases) ? ext.phrases : [];
  return phrases
    .map((p: any) => {
      const en = p?.en || '';
      const cn = p?.cn || '';
      return cn ? `${en}（${cn}）` : en;
    })
    .filter(Boolean)
    .slice(0, 8);
}

function synonymLines(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const synonyms = Array.isArray(ext.synonyms) ? ext.synonyms : [];
  return synonyms
    .map((g: any) => {
      const pos = g?.pos ? `${g.pos} ` : '';
      const words = Array.isArray(g?.words) ? g.words.join('、') : '';
      const tran = g?.tran ? ` — ${g.tran}` : '';
      return `${pos}${words}${tran}`.trim();
    })
    .filter(Boolean)
    .slice(0, 6);
}

function relatedLines(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const related = Array.isArray(ext.relatedWords) ? ext.relatedWords : [];
  return related
    .map((g: any) => {
      const pos = g?.pos ? `${g.pos} ` : '';
      const words = Array.isArray(g?.words)
        ? g.words
            .map((w: any) => (w?.tran ? `${w.word}（${w.tran}）` : w?.word))
            .filter(Boolean)
            .join('、')
        : '';
      return `${pos}${words}`.trim();
    })
    .filter(Boolean)
    .slice(0, 6);
}

function meaningAudioFileId(extInfo?: string): number | undefined {
  const ext = parseExtInfo(extInfo);
  const id = ext?.meaningAudio?.fileId;
  if (id == null || id === '') {
    return undefined;
  }
  const n = Number(id);
  return Number.isFinite(n) && n > 0 ? n : undefined;
}

function tagList(extInfo?: string): string[] {
  const ext = parseExtInfo(extInfo);
  const tags = Array.isArray(ext.tags) ? ext.tags.filter(Boolean).map(String) : [];
  // 已有语音但尚未回写 tags 时，列表仍展示「有释义语音」
  const hasAudio = meaningAudioFileId(extInfo) != null;
  if (hasAudio && !tags.includes('has_meaning_audio')) {
    return [...tags.filter((t) => t !== 'no_meaning_audio'), 'has_meaning_audio'];
  }
  if (!hasAudio && tags.includes('has_meaning_audio')) {
    return [...tags.filter((t) => t !== 'has_meaning_audio'), 'no_meaning_audio'];
  }
  return tags;
}

const TAG_OPTIONS = [
  { value: 'no_picture', label: '无图' },
  { value: 'has_picture', label: '有图' },
  { value: 'unconfirmed', label: '未确认' },
  { value: 'confirmed', label: '已确认' },
  { value: 'source_youdao', label: '来源·有道' },
  { value: 'source_pixabay', label: '来源·Pixabay' },
  { value: 'source_none', label: '来源·无' },
  { value: 'has_meaning', label: '有释义' },
  { value: 'no_meaning', label: '无释义' },
  { value: 'has_example', label: '有例句' },
  { value: 'no_example', label: '无例句' },
  { value: 'has_meaning_audio', label: '有释义语音' },
  { value: 'no_meaning_audio', label: '无释义语音' },
  { value: 'sync_error', label: '同步失败' },
];

const TAG_COLOR: Record<string, string> = {
  no_picture: 'warning',
  has_picture: 'blue',
  has_meaning: 'blue',
  no_meaning: 'warning',
  has_example: 'geekblue',
  no_example: 'orange',
  has_meaning_audio: 'cyan',
  no_meaning_audio: 'default',
  confirmed: 'success',
  unconfirmed: 'default',
  sync_error: 'error',
};

/** 按 fileId 拉取并试听已落库释义语音 */
function MeaningAudioPreview({
  fileId,
  compact,
}: {
  fileId?: number;
  compact?: boolean;
}) {
  const [loading, setLoading] = useState(false);
  const [playing, setPlaying] = useState(false);
  const audioRef = React.useRef<HTMLAudioElement | null>(null);
  const urlRef = React.useRef('');

  useEffect(() => {
    urlRef.current = '';
    try {
      audioRef.current?.pause();
    } catch {
      /* ignore */
    }
    setPlaying(false);
  }, [fileId]);

  useEffect(() => {
    return () => {
      try {
        audioRef.current?.pause();
      } catch {
        /* ignore */
      }
      audioRef.current = null;
      urlRef.current = '';
    };
  }, []);

  if (!fileId) {
    return compact ? <Tag>无语音</Tag> : <Tag>尚未生成释义语音</Tag>;
  }

  const stop = () => {
    try {
      audioRef.current?.pause();
      if (audioRef.current) {
        audioRef.current.currentTime = 0;
      }
    } catch {
      /* ignore */
    }
    setPlaying(false);
  };

  const play = async () => {
    if (playing) {
      stop();
      return;
    }
    setLoading(true);
    try {
      let src = urlRef.current;
      if (!src) {
        const res = await adminApi.getWordBaseInfoPictureContent({ id: fileId });
        const data = res?.data;
        if (!data?.base64Data) {
          message.error('语音文件不存在');
          return;
        }
        src = `data:${data.contentType || 'audio/mpeg'};base64,${data.base64Data}`;
        urlRef.current = src;
      }
      if (!audioRef.current) {
        audioRef.current = new Audio();
      }
      const audio = audioRef.current;
      audio.onended = () => setPlaying(false);
      audio.onerror = () => {
        setPlaying(false);
        message.error('语音播放失败');
      };
      audio.src = src;
      await audio.play();
      setPlaying(true);
    } catch (e: any) {
      if (!e?.notified) {
        message.error(e?.message || '加载语音失败');
      }
      setPlaying(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      type={compact ? 'link' : 'default'}
      size={compact ? 'small' : 'middle'}
      icon={<SoundOutlined />}
      loading={loading}
      onClick={() => void play()}
    >
      {playing ? '停止' : '试听'}
    </Button>
  );
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
  const [batchSearching, setBatchSearching] = useState(false);
  const [batchMeaningAudio, setBatchMeaningAudio] = useState(false);
  const [adoptingPics, setAdoptingPics] = useState(false);
  const [candidates, setCandidates] = useState<any[]>([]);
  const [selectedUrls, setSelectedUrls] = useState<string[]>([]);
  const [activeStockSource, setActiveStockSource] = useState('');

  const loadLanguages = async () => {
    try {
      const res = await adminApi.getWordLanguageList({ pageNum: 1, pageSize: 100 });
      setLanguages(res.data?.list || []);
    } catch {
      // 错误已在 request 拦截器中提示
    }
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
    } catch {
      // 错误已在 request 拦截器中提示
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLanguages();
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  const openDetail = async (row: any, autoSearch = false) => {
    setDetailOpen(true);
    setYoudaoResult(null);
    setCandidates([]);
    setSelectedUrls([]);
    setActiveStockSource('');
    setDetailLoading(true);
    try {
      const res = await adminApi.getWordBaseInfoDetail({ id: row.id });
      const data = res.data || row;
      setDetail(data);
      if (autoSearch && data?.id) {
        setTimeout(() => searchStockPicturesById(data.id), 0);
      }
    } catch (e: any) {
      if (!e?.notified) {
        message.error(e?.message || '加载详情失败');
      }
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
    setActiveStockSource('');
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
      if (!e?.notified) {
        message.error(e?.message || '有道请求失败');
      }
    } finally {
      setYoudaoTesting(false);
    }
  };

  const searchStockPicturesById = async (id: number) => {
    setSearchingPics(true);
    setSelectedUrls([]);
    setCandidates([]);
    try {
      const res = await adminApi.searchWordBaseInfoPictures({ id });
      const list = res.data?.candidates || [];
      setCandidates(list);
      setActiveStockSource(res.data?.source || list[0]?.source || '');
      if (list.length) {
        message.success(
          `已搜到 ${list.length} 张候选（${res.data?.source || '图库'}，未落库）`
        );
      } else {
        message.warning('未搜到候选图');
      }
    } catch (e: any) {
      if (!e?.notified) {
        message.error(e?.message || '图库搜索失败');
      }
    } finally {
      setSearchingPics(false);
    }
  };

  const searchStockPictures = async () => {
    if (!detail?.id) {
      return;
    }
    await searchStockPicturesById(detail.id);
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
        pictureSource: activeStockSource || undefined,
      });
      setDetail(res.data || detail);
      setCandidates([]);
      setSelectedUrls([]);
      message.success('已采纳并打标锁定，自动同步不会覆盖');
      loadData();
    } catch (e: any) {
      if (!e?.notified) {
        message.error(e?.message || '采纳失败');
      }
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
      title: '从词条同步单词',
      content:
        '将分页扫描全部词条词形写入基础表；再对缺释义/缺例句的词查有道补释义、音标、双语例句、词组、近义、派生与词源摘要（带空格词跳过，不改已有配图与已有字段）。词量大时可能较久。配图请用「批量搜图」。',
      okText: '开始同步',
      cancelText: '取消',
      onOk: async () => {
        setSyncing(true);
        try {
          const res = await adminApi.syncWordBaseInfo({});
          const d = res.data || {};
          message.success(
            `同步完成：词形 扫描 ${d.scanned ?? 0}/去重 ${d.distinct ?? 0}/新增 ${d.inserted ?? 0}；释义 扫描 ${d.meaningScanned ?? 0}/写入 ${d.meaningFilled ?? 0}/失败 ${d.meaningFailed ?? 0}/跳过 ${d.meaningSkipped ?? 0}`
          );
          loadData({ pageNum: 1 });
        } catch (e: any) {
          if (!e?.notified) {
            message.error(e?.message || '同步失败');
          }
        } finally {
          setSyncing(false);
        }
      },
    });
  };

  const runBatchSearch = (source: 'YOUDAO' | 'PIXABAY') => {
    const values = queryForm.getFieldsValue();
    const isYoudao = source === 'YOUDAO';
    const tags = Array.isArray(values.tags) ? values.tags.filter(Boolean) : [];
    const tagText = tags.length
      ? tags.map((t: string) => TAG_OPTIONS.find((o) => o.value === t)?.label || t).join('、')
      : '未选标签';
    Modal.confirm({
      title: isYoudao ? '批量搜图（有道词典）' : '批量搜图（Pixabay）',
      content: isYoudao
        ? `按当前筛选扫描（标签：${tagText}），调用有道词典写入配图、释义、音标等。有道图写入后直接确认可用。词量大时可能较久。`
        : `按当前筛选扫描（标签：${tagText}），先向 Pixabay 重新搜图，再把新图片写入文件表（每词最多 4 张）。已过期的旧外链不会再下载。写入后未确认；采纳时未勾选的图会物理删除。`,
      okText: '开始搜图',
      cancelText: '取消',
      onOk: async () => {
        setBatchSearching(true);
        try {
          const res = await adminApi.batchSearchWordBaseInfoPictures({
            languageCode: values.languageCode || undefined,
            word: values.word || undefined,
            tags: tags.length ? tags : undefined,
            hasPicture: values.hasPicture,
            pictureConfirmed: values.pictureConfirmed,
            source,
            picturesPerWord: isYoudao ? undefined : 4,
          });
          const d = res.data || {};
          message.success(
            `批量搜图完成（${isYoudao ? '有道' : 'Pixabay'}）：扫描 ${d.scanned ?? 0}，写入 ${d.filled ?? 0}，失败 ${d.failed ?? 0}，跳过 ${d.skipped ?? 0}`
          );
          loadData({ pageNum: 1, filters: values });
        } catch (e: any) {
          if (!e?.notified) {
            message.error(e?.message || '批量搜图失败');
          }
        } finally {
          setBatchSearching(false);
        }
      },
    });
  };

  const runBatchMeaningAudio = () => {
    const values = queryForm.getFieldsValue();
    const tags = Array.isArray(values.tags) ? values.tags.filter(Boolean) : [];
    const tagText = tags.length
      ? tags.map((t: string) => TAG_OPTIONS.find((o) => o.value === t)?.label || t).join('、')
      : '未选标签';
    Modal.confirm({
      title: '批量生成释义语音',
      content: `按当前筛选（标签：${tagText}）调用本机 Qwen3-TTS（localhost:8880）生成整段释义语音，写入 common_binary_file。已有 fileId 且二进制仍存在、释义未变则跳过；文件已删会重新生成。请先确保本地 TTS 服务已启动。词量大时可能很久。`,
      okText: '开始生成',
      cancelText: '取消',
      onOk: async () => {
        setBatchMeaningAudio(true);
        try {
          const res = await adminApi.batchGenerateWordBaseInfoMeaningAudio({
            languageCode: values.languageCode || undefined,
            word: values.word || undefined,
            tags: tags.length ? tags : undefined,
            hasPicture: values.hasPicture,
            pictureConfirmed: values.pictureConfirmed,
          });
          const d = res.data || {};
          message.success(
            `释义语音完成（${d.voice || 'Qwen3'}）：扫描 ${d.scanned ?? 0}，写入 ${d.filled ?? 0}，失败 ${d.failed ?? 0}，跳过 ${d.skipped ?? 0}`
          );
          loadData({ pageNum: 1, filters: values });
        } catch (e: any) {
          if (!e?.notified) {
            message.error(e?.message || '批量生成释义语音失败');
          }
        } finally {
          setBatchMeaningAudio(false);
        }
      },
    });
  };

  const detailExt = parseExtInfo(detail?.extInfo);
  const detailPics = pictureRefs(detail?.extInfo);
  const detailMeanings = meaningLines(detail?.extInfo);
  const detailExamples = exampleLines(detail?.extInfo);
  const detailPhrases = phraseLines(detail?.extInfo);
  const detailSynonyms = synonymLines(detail?.extInfo);
  const detailRelated = relatedLines(detail?.extInfo);
  const detailEtym = typeof detailExt.etym === 'string' ? detailExt.etym.trim() : '';
  const detailTags = tagList(detail?.extInfo);
  const detailMeaningAudioId = meaningAudioFileId(detail?.extInfo);
  const pictureConfirmed = !!detailExt.pictureConfirmed;

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
        <h1 style={{ margin: 0 }}>单词基础信息</h1>
        <Space wrap>
          <Button
            type="default"
            icon={<SoundOutlined />}
            loading={batchMeaningAudio}
            onClick={runBatchMeaningAudio}
          >
            批量生成释义语音
          </Button>
          <Button
            type="default"
            icon={<PictureOutlined />}
            loading={batchSearching}
            onClick={() => runBatchSearch('YOUDAO')}
          >
            批量搜图（有道词典）
          </Button>
          <Button
            type="default"
            icon={<PictureOutlined />}
            loading={batchSearching}
            onClick={() => runBatchSearch('PIXABAY')}
          >
            批量搜图（Pixabay）
          </Button>
          <Button type="primary" icon={<CloudSyncOutlined />} loading={syncing} onClick={handleSync}>
            从词条同步
          </Button>
        </Space>
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
            style={{ width: 160 }}
            options={languages.map((item) => ({
              value: item.code,
              label: `${item.code} - ${item.name}`,
            }))}
          />
        </Form.Item>
        <Form.Item name="word">
          <Input placeholder="词形前缀" allowClear style={{ width: 140 }} />
        </Form.Item>
        <Form.Item name="tags">
          <Select
            mode="multiple"
            allowClear
            placeholder="标签（多选且）"
            style={{ minWidth: 260 }}
            maxTagCount="responsive"
            options={TAG_OPTIONS}
          />
        </Form.Item>
        <Form.Item name="hasPicture">
          <Select
            allowClear
            placeholder="是否有图"
            style={{ width: 120 }}
            options={[
              { value: false, label: '无图' },
              { value: true, label: '有图' },
            ]}
          />
        </Form.Item>
        <Form.Item name="pictureConfirmed">
          <Select
            allowClear
            placeholder="确认状态"
            style={{ width: 120 }}
            options={[
              { value: false, label: '未确认' },
              { value: true, label: '已确认' },
            ]}
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
            查询
          </Button>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading}
        dataSource={dataSource}
        scroll={{ x: 1650 }}
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
          { title: '语种', dataIndex: 'languageCode', width: 80 },
          { title: '词形', dataIndex: 'word', width: 140, ellipsis: true },
          {
            title: '标签',
            key: 'tags',
            width: 220,
            render: (_, row) => {
              const tags = tagList(row.extInfo);
              if (!tags.length) {
                return <Tag>未打标</Tag>;
              }
              return (
                <Space size={4} wrap>
                  {tags.map((t) => (
                    <Tag key={t} color={TAG_COLOR[t] || 'processing'}>
                      {TAG_OPTIONS.find((o) => o.value === t)?.label || t}
                    </Tag>
                  ))}
                </Space>
              );
            },
          },
          {
            title: '配图',
            key: 'pictures',
            width: 200,
            render: (_, row) => {
              const pics = pictureRefs(row.extInfo);
              const ext = parseExtInfo(row.extInfo);
              if (!pics.length) {
                return <Tag color="warning">无图</Tag>;
              }
              return (
                <Space size={4} wrap>
                  {ext.pictureSource && <Tag>{ext.pictureSource}</Tag>}
                  {ext.pictureConfirmed && <Tag color="success">已确认</Tag>}
                  <Image.PreviewGroup>
                    <Space size={4} wrap>
                      {pics.slice(0, 3).map((pic) => (
                        <StoredPicture key={pic.key} item={pic} width={40} height={40} />
                      ))}
                    </Space>
                  </Image.PreviewGroup>
                </Space>
              );
            },
          },
          {
            title: '释义',
            key: 'meanings',
            width: 240,
            ellipsis: true,
            render: (_, row) => {
              const lines = meaningLines(row.extInfo);
              return lines.length ? (
                <span title={lines.join('\n')}>{lines.slice(0, 2).join('；')}</span>
              ) : (
                '-'
              );
            },
          },
          {
            title: '释义语音',
            key: 'meaningAudio',
            width: 110,
            render: (_, row) => <MeaningAudioPreview fileId={meaningAudioFileId(row.extInfo)} compact />,
          },
          {
            title: '音标',
            key: 'phone',
            width: 160,
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
          { title: '修改时间', dataIndex: 'modifyTime', width: 170 },
          {
            title: '操作',
            key: 'actions',
            width: 160,
            fixed: 'right',
            render: (_, row) => {
              const noPic = pictureRefs(row.extInfo).length === 0;
              return (
                <Space size={0}>
                  <Button type="link" icon={<EyeOutlined />} onClick={() => openDetail(row)}>
                    详情
                  </Button>
                  {noPic && (
                    <Button type="link" icon={<PictureOutlined />} onClick={() => openDetail(row, true)}>
                      搜图
                    </Button>
                  )}
                </Space>
              );
            },
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
              <Descriptions.Item label="图片来源">{detailExt.pictureSource || '-'}</Descriptions.Item>
              <Descriptions.Item label="确认时间">{detailExt.pictureConfirmedAt || '-'}</Descriptions.Item>
              <Descriptions.Item label="词典来源">{detailExt.source || '-'}</Descriptions.Item>
              <Descriptions.Item label="最近成功同步" span={2}>
                {detailExt.lastSyncTime || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="标签" span={2}>
                {detailTags.length ? (
                  <Space size={4} wrap>
                    {detailTags.map((t) => (
                      <Tag key={t} color={TAG_COLOR[t] || 'processing'}>
                        {TAG_OPTIONS.find((o) => o.value === t)?.label || t}
                      </Tag>
                    ))}
                  </Space>
                ) : (
                  '-'
                )}
              </Descriptions.Item>
            </Descriptions>

            <div style={{ marginBottom: 16 }}>
              <Text strong>一词多义（审图参考）</Text>
              <div style={{ marginTop: 8 }}>
                {detailMeanings.length ? (
                  <ul style={{ margin: 0, paddingLeft: 20 }}>
                    {detailMeanings.map((line) => (
                      <li key={line}>{line}</li>
                    ))}
                  </ul>
                ) : (
                  <Tag>无释义</Tag>
                )}
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <Text strong>双语例句（有道同步 / 学习端展示）</Text>
              <div style={{ marginTop: 8 }}>
                {detailExamples.length ? (
                  <ul style={{ margin: 0, paddingLeft: 20 }}>
                    {detailExamples.map((ex) => (
                      <li key={ex.en || ex.cn}>
                        <div>{ex.en || '-'}</div>
                        {ex.cn ? <Text type="secondary">{ex.cn}</Text> : null}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <Tag>无例句（可「从词条同步」补全）</Tag>
                )}
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <Text strong>词组 / 近义 / 派生</Text>
              <div style={{ marginTop: 8 }}>
                {detailPhrases.length || detailSynonyms.length || detailRelated.length ? (
                  <>
                    {detailPhrases.length > 0 && (
                      <div style={{ marginBottom: 8 }}>
                        <Text type="secondary">词组：</Text>
                        {detailPhrases.join('；')}
                      </div>
                    )}
                    {detailSynonyms.length > 0 && (
                      <div style={{ marginBottom: 8 }}>
                        <Text type="secondary">近义：</Text>
                        <ul style={{ margin: '4px 0 0', paddingLeft: 20 }}>
                          {detailSynonyms.map((line) => (
                            <li key={line}>{line}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                    {detailRelated.length > 0 && (
                      <div>
                        <Text type="secondary">派生：</Text>
                        <ul style={{ margin: '4px 0 0', paddingLeft: 20 }}>
                          {detailRelated.map((line) => (
                            <li key={line}>{line}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </>
                ) : (
                  <Tag>暂无（同步后补全）</Tag>
                )}
              </div>
            </div>

            {detailEtym ? (
              <div style={{ marginBottom: 16 }}>
                <Text strong>词源摘要</Text>
                <Paragraph type="secondary" style={{ marginTop: 8, marginBottom: 0 }}>
                  {detailEtym}
                </Paragraph>
              </div>
            ) : null}

            <div style={{ marginBottom: 16 }}>
              <Space wrap>
                <Text strong>释义语音（学习端优先播放）</Text>
                <MeaningAudioPreview fileId={detailMeaningAudioId} />
              </Space>
              <div style={{ marginTop: 8 }}>
                {detailMeaningAudioId ? (
                  <Space wrap>
                    <Tag color="cyan">fileId {detailMeaningAudioId}</Tag>
                    {detailExt.meaningAudio?.voice && <Tag>{detailExt.meaningAudio.voice}</Tag>}
                    {detailExt.meaningAudio?.source && <Tag>{detailExt.meaningAudio.source}</Tag>}
                    {detailExt.meaningAudio?.updatedAt && (
                      <Text type="secondary">更新于 {detailExt.meaningAudio.updatedAt}</Text>
                    )}
                  </Space>
                ) : (
                  <Tag>尚未生成，可用顶部「批量生成释义语音」</Tag>
                )}
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <Text strong>正式配图（学习端使用）</Text>
              <div style={{ marginTop: 8 }}>
                {detailPics.length ? (
                  <Image.PreviewGroup>
                    <Space wrap>
                      {detailPics.map((pic) => (
                        <StoredPicture key={pic.key} item={pic} width={96} height={96} />
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
                <Text strong>图库补图（Pixabay）</Text>
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
                  采纳并打标（{selectedUrls.length}）
                </Button>
              </Space>
              <Paragraph type="secondary" style={{ marginBottom: 8 }}>
                对照上方释义勾选合适配图。批量搜到的 Pixabay 图已写入文件表；采纳后未勾选的图会物理删除。有道同步不会覆盖已确认配图。
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
                          {c.photographer || c.source || 'Pixabay'}
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
