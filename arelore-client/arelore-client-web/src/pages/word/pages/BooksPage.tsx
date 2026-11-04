import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Radio, Select, Space, Spin, Switch, Typography, message } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useI18n, useLocaleNavigate } from '../../../locale';
import { wordApi } from '../api/word';
import { shortErrorMessage } from '../utils/request';
import { setBookId } from '../utils/storage';

const { Title, Text, Paragraph } = Typography;

const RATIO_PARTS = [
  { value: '1:1', reviewPart: 1 },
  { value: '1:2', reviewPart: 2 },
  { value: '1:3', reviewPart: 3 },
] as const;
const NEW_COUNT_OPTIONS = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
const LOOKBACK_OPTIONS = [1, 2, 3, 4, 5, 6, 7];

function calcPlan(wordCount: number, ratioValue: string, dailyNewCount: number) {
  const ratio = RATIO_PARTS.find((item) => item.value === ratioValue) || RATIO_PARTS[0];
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
  const { t } = useI18n();
  const ratioOptions = [
    { label: t('word.books.ratio11'), value: '1:1', reviewPart: 1 },
    { label: t('word.books.ratio12'), value: '1:2', reviewPart: 2 },
    { label: t('word.books.ratio13'), value: '1:3', reviewPart: 3 },
  ];
  const voiceOptions = [
    { label: t('word.books.usVoice'), value: 2 },
    { label: t('word.books.ukVoice'), value: 1 },
  ];
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
      message.success(t('word.books.planSaved'));
      navigate('/word');
    } catch (e: any) {
      message.error(shortErrorMessage(e, t('word.common.saveFailed')));
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
      ? t('word.books.pickLanguage')
      : step === 'category'
        ? t('word.books.pickCategory')
        : step === 'book'
          ? t('word.books.pickBook')
          : t('word.books.studyPlan');

  return (
    <div className="page">
      <div className="page-head">
        <Title level={3} style={{ margin: 0 }}>
          {title}
        </Title>
        {step !== 'language' && (
          <Button icon={<ArrowLeftOutlined />} onClick={onBack}>
            {t('word.books.backStep')}
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
                  {t('word.books.wordCount', { count: item.wordCount || 0 })}
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
          <Paragraph type="secondary">
            {t('word.books.totalWords', { count: selectedBook.wordCount || 0 })}
          </Paragraph>
          <Space direction="vertical" style={{ width: '100%' }} size={16}>
            <div>
              <Text>{t('word.books.dailyNew')}</Text>
              <div style={{ marginTop: 8 }}>
                <Select
                  style={{ width: 240 }}
                  value={dailyNewCount}
                  options={NEW_COUNT_OPTIONS.map((n) => ({
                    value: n,
                    label: t('word.books.wordsUnit', { n }),
                  }))}
                  onChange={setDailyNewCount}
                />
              </div>
            </div>
            <div>
              <Text>{t('word.books.ratio')}</Text>
              <div style={{ marginTop: 8 }}>
                <Select style={{ width: 240 }} value={ratio} options={ratioOptions} onChange={setRatio} />
              </div>
            </div>
            {showVoice && (
              <div>
                <Text>{t('word.books.voice')}</Text>
                <div style={{ marginTop: 8 }}>
                  <Radio.Group
                    value={voiceType}
                    options={voiceOptions}
                    onChange={(e) => setVoiceType(e.target.value)}
                  />
                </div>
              </div>
            )}
            <div>
              <Text>{t('word.books.lookback')}</Text>
              <div style={{ marginTop: 8 }}>
                <Select
                  style={{ width: 240 }}
                  value={reviewLookbackDays}
                  options={LOOKBACK_OPTIONS.map((n) => ({
                    value: n,
                    label: t('word.books.lookbackDays', { n }),
                  }))}
                  onChange={setReviewLookbackDays}
                />
              </div>
              <Paragraph type="secondary" style={{ marginTop: 8, marginBottom: 0 }}>
                {t('word.books.lookbackHint')}
              </Paragraph>
            </div>
            <div>
              <Text>{t('word.books.softLand')}</Text>
              <div style={{ marginTop: 8 }}>
                <Switch checked={softLandEnabled} onChange={setSoftLandEnabled} />
                <Text type="secondary" style={{ marginLeft: 12 }}>
                  {t('word.books.softLandHint')}
                </Text>
              </div>
            </div>
            <Paragraph type="secondary">
              {t('word.books.planSummary', {
                review: derived.dailyReviewCount,
                days: derived.planDays,
              })}
            </Paragraph>
            <Button type="primary" loading={saving} onClick={confirmPlan}>
              {t('word.books.confirmStart')}
            </Button>
          </Space>
        </Card>
      )}
    </div>
  );
}
