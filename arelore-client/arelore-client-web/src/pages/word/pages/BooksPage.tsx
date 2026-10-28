import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Radio, Select, Space, Spin, Switch, Typography, message } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useLocaleNavigate } from '../../../locale';
import { wordApi } from '../api/word';
import { shortErrorMessage } from '../utils/request';
import { setBookId } from '../utils/storage';

const { Title, Text, Paragraph } = Typography;

const RATIO_OPTIONS = [
  { label: '新学:复习 = 1:1', value: '1:1', reviewPart: 1 },
  { label: '新学:复习 = 1:2', value: '1:2', reviewPart: 2 },
  { label: '新学:复习 = 1:3', value: '1:3', reviewPart: 3 },
];
const NEW_COUNT_OPTIONS = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
const LOOKBACK_OPTIONS = [1, 2, 3, 4, 5, 6, 7];
const VOICE_OPTIONS = [
  { label: '美音', value: 2 },
  { label: '英音', value: 1 },
];

function calcPlan(wordCount: number, ratioValue: string, dailyNewCount: number) {
  const ratio = RATIO_OPTIONS.find((item) => item.value === ratioValue) || RATIO_OPTIONS[0];
  const dailyReviewCount = dailyNewCount * ratio.reviewPart;
  const planDays = dailyNewCount > 0 ? Math.ceil((wordCount || 0) / dailyNewCount) : 0;
  return { dailyReviewCount, planDays };
}

function parseBookExt(extInfo: any) {
  if (!extInfo) return { cover: '', originName: '' };
  let raw = extInfo;
  if (typeof extInfo === 'string') {
    try {
      raw = JSON.parse(extInfo);
    } catch {
      return { cover: '', originName: '' };
    }
  }
  const cover = typeof raw.cover === 'string' ? raw.cover.trim() : '';
  const origin = raw.bookOrigin;
  let originName = '';
  if (origin && typeof origin === 'object') originName = (origin.originName || '').trim();
  else if (typeof origin === 'string') originName = origin.trim();
  return { cover, originName };
}

export default function BooksPage() {
  const navigate = useLocaleNavigate();
  const [step, setStep] = useState<'language' | 'category' | 'book' | 'plan'>('language');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [languages, setLanguages] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [books, setBooks] = useState<any[]>([]);
  const [language, setLanguage] = useState<any>(null);
  const [, setCategory] = useState<any>(null);
  const [selectedBook, setSelectedBook] = useState<any>(null);
  const [ratio, setRatio] = useState('1:1');
  const [dailyNewCount, setDailyNewCount] = useState(10);
  const [voiceType, setVoiceType] = useState(2);
  const [reviewLookbackDays, setReviewLookbackDays] = useState(3);
  const [softLandEnabled, setSoftLandEnabled] = useState(true);

  const derived = useMemo(
    () => calcPlan(selectedBook?.wordCount || 0, ratio, dailyNewCount),
    [selectedBook, ratio, dailyNewCount]
  );
  const showVoice = language?.code === 'EN';

  const loadLanguages = async (autoEnter = true) => {
    setLoading(true);
    setStep('language');
    setLanguage(null);
    setCategory(null);
    setSelectedBook(null);
    try {
      const res = await wordApi.languages();
      const list = res.data || [];
      setLanguages(list);
      if (autoEnter && list.length === 1) {
        await selectLanguage(list[0]);
      }
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  const selectLanguage = async (lang: any) => {
    setLanguage(lang);
    setLoading(true);
    setStep('category');
    setCategory(null);
    setSelectedBook(null);
    try {
      const res = await wordApi.categories(lang.code);
      setCategories(res.data || []);
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  const selectCategory = async (cat: any) => {
    if (!language) return;
    setCategory(cat);
    setLoading(true);
    setStep('book');
    setSelectedBook(null);
    try {
      const res = await wordApi.books(language.code, cat.code);
      const list = (res.data || []).map((item: any) => {
        const parsed = parseBookExt(item.extInfo);
        return {
          ...item,
          cover: (item.cover || parsed.cover || '').trim(),
          originName: (item.originName || parsed.originName || '').trim(),
        };
      });
      setBooks(list);
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  const openPlan = async (book: any) => {
    setSelectedBook(book);
    setStep('plan');
    setRatio('1:1');
    setDailyNewCount(10);
    setVoiceType(2);
    setReviewLookbackDays(3);
    setSoftLandEnabled(true);
    try {
      const res = await wordApi.planGet(book.code);
      const plan = res.data;
      if (!plan) return;
      if (plan.newReviewRatio) setRatio(plan.newReviewRatio);
      if (plan.dailyNewCount) setDailyNewCount(plan.dailyNewCount);
      if (plan.extInfo) {
        try {
          const ext = typeof plan.extInfo === 'string' ? JSON.parse(plan.extInfo) : plan.extInfo;
          if (ext?.voiceType === 1 || ext?.voiceType === 2) setVoiceType(ext.voiceType);
          const lookback = Number(ext?.reviewLookbackDays);
          if (lookback >= 1 && lookback <= 7) setReviewLookbackDays(lookback);
          if (ext && Object.prototype.hasOwnProperty.call(ext, 'softLandEnabled')) {
            setSoftLandEnabled(ext.softLandEnabled !== false);
          }
        } catch {
          /* ignore */
        }
      }
    } catch {
      /* ignore */
    }
  };

  const onBack = () => {
    if (step === 'plan') {
      setStep('book');
      return;
    }
    if (step === 'book') {
      setStep('category');
      return;
    }
    if (step === 'category') {
      loadLanguages(false);
      return;
    }
    navigate('/word');
  };

  const confirmPlan = async () => {
    if (!selectedBook?.code || saving) return;
    setSaving(true);
    try {
      const ext: Record<string, unknown> = {
        reviewLookbackDays,
        softLandEnabled,
      };
      if (showVoice) {
        ext.voiceType = voiceType;
      }
      const payload: any = {
        bookCode: selectedBook.code,
        newReviewRatio: ratio,
        dailyNewCount,
        extInfo: JSON.stringify(ext),
      };
      await wordApi.planConfirm(payload);
      setBookId(selectedBook.code);
      message.success('计划已保存');
      navigate('/word');
    } catch (e: any) {
      message.error(shortErrorMessage(e, '保存失败'));
    } finally {
      setSaving(false);
    }
  };

  useEffect(() => {
    loadLanguages(true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const title =
    step === 'language'
      ? '选择语种'
      : step === 'category'
        ? '选择分类'
        : step === 'book'
          ? '选择单词本'
          : '学习计划';

  return (
    <div className="page">
      <div className="page-head">
        <Title level={3} style={{ margin: 0 }}>
          {title}
        </Title>
        {step !== 'language' && (
          <Button icon={<ArrowLeftOutlined />} onClick={onBack}>
            返回上一步
          </Button>
        )}
      </div>

      {loading && step !== 'plan' ? (
        <div style={{ padding: 48, textAlign: 'center' }}>
          <Spin />
        </div>
      ) : null}

      {!loading && step === 'language' && (
        <div className="books-grid">
          {languages.map((item) => (
            <div key={item.code} className="book-pick-card" onClick={() => selectLanguage(item)}>
              <Text strong>
                {item.code} · {item.name}
              </Text>
            </div>
          ))}
        </div>
      )}

      {!loading && step === 'category' && (
        <div className="books-grid">
          {categories.map((item) => (
            <div
              key={item.code || item.name}
              className="book-pick-card"
              onClick={() => selectCategory(item)}
            >
              <Text strong>{item.name || item.code}</Text>
            </div>
          ))}
        </div>
      )}

      {!loading && step === 'book' && (
        <div className="books-grid">
          {books.map((item) => (
            <div key={item.code} className="book-pick-card" onClick={() => openPlan(item)}>
              <Text strong>{item.name || item.code}</Text>
              <div>
                <Text type="secondary">
                  {item.wordCount || 0} 词
                  {item.originName ? ` · ${item.originName}` : ''}
                </Text>
              </div>
            </div>
          ))}
        </div>
      )}

      {step === 'plan' && selectedBook && (
        <Card className="plan-form" style={{ maxWidth: 520 }}>
          <Title level={5}>{selectedBook.name || selectedBook.code}</Title>
          <Paragraph type="secondary">共 {selectedBook.wordCount || 0} 词</Paragraph>
          <Space direction="vertical" style={{ width: '100%' }} size={16}>
            <div>
              <Text>每日新学</Text>
              <div style={{ marginTop: 8 }}>
                <Select
                  style={{ width: 240 }}
                  value={dailyNewCount}
                  options={NEW_COUNT_OPTIONS.map((n) => ({ value: n, label: `${n} 词` }))}
                  onChange={setDailyNewCount}
                />
              </div>
            </div>
            <div>
              <Text>新学/复习比例</Text>
              <div style={{ marginTop: 8 }}>
                <Select style={{ width: 240 }} value={ratio} options={RATIO_OPTIONS} onChange={setRatio} />
              </div>
            </div>
            {showVoice && (
              <div>
                <Text>发音</Text>
                <div style={{ marginTop: 8 }}>
                  <Radio.Group
                    value={voiceType}
                    options={VOICE_OPTIONS}
                    onChange={(e) => setVoiceType(e.target.value)}
                  />
                </div>
              </div>
            )}
            <div>
              <Text>复习回溯天数</Text>
              <div style={{ marginTop: 8 }}>
                <Select
                  style={{ width: 240 }}
                  value={reviewLookbackDays}
                  options={LOOKBACK_OPTIONS.map((n) => ({
                    value: n,
                    label: `最近 ${n} 个学习日`,
                  }))}
                  onChange={setReviewLookbackDays}
                />
              </div>
              <Paragraph type="secondary" style={{ marginTop: 8, marginBottom: 0 }}>
                从最近 N 个有新学的日子里抽复习词，再与「每日复习」上限取小
              </Paragraph>
            </div>
            <div>
              <Text>断签软着陆</Text>
              <div style={{ marginTop: 8 }}>
                <Switch checked={softLandEnabled} onChange={setSoftLandEnabled} />
                <Text type="secondary" style={{ marginLeft: 12 }}>
                  断签 ≥2 天时，今日复习压到约每日新学量
                </Text>
              </div>
            </div>
            <Paragraph type="secondary">
              每日复习上限约 {derived.dailyReviewCount} 词 · 预计 {derived.planDays} 天完成
            </Paragraph>
            <Button type="primary" loading={saving} onClick={confirmPlan}>
              确认并开始
            </Button>
          </Space>
        </Card>
      )}
    </div>
  );
}
