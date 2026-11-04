import React, { useCallback, useEffect, useState } from 'react';
import { Button, Card, Progress, Space, Tag, Typography, message } from 'antd';
import { useI18n, useLocaleNavigate } from '../../../locale';
import { wordApi } from '../api/word';
import WordSeoLanding from '../components/WordSeoLanding';
import { modeLabel, syncPreference } from '../utils/preference';
import { getPreference, getToken, setBookId, STUDY_MODE_LISTEN_SPELL } from '../utils/storage';
import { shortErrorMessage } from '../utils/request';

const { Title, Text, Paragraph } = Typography;

function mapCurrent(current: any) {
  if (!current?.bookCode) return null;
  const wordCount = current.wordCount || 0;
  const learnedCount = current.learnedCount || 0;
  return {
    code: current.bookCode,
    name: current.bookName || current.bookCode,
    learnTodo: current.learnTodo || 0,
    learnDone: current.learnDone || 0,
    reviewTodo: current.reviewTodo || 0,
    reviewDone: current.reviewDone || 0,
    wordCount,
    learnedCount,
    planDays: current.planDays || 0,
    remainingDays: current.remainingDays != null ? current.remainingDays : 0,
    progressPercent:
      current.progressPercent != null
        ? current.progressPercent
        : wordCount <= 0
          ? 0
          : Math.min(100, Math.round((learnedCount * 100) / wordCount)),
  };
}

export default function HomePage() {
  const navigate = useLocaleNavigate();
  const { t, locale } = useI18n();
  const [status, setStatus] = useState<'loading' | 'needLogin' | 'noBook' | 'error' | 'ready'>('loading');
  const [book, setBook] = useState<any>(null);
  const [studyModeLabel, setStudyModeLabel] = useState(modeLabel(getPreference().studyMode, locale));
  const [isListenMode, setIsListenMode] = useState(
    getPreference().studyMode === STUDY_MODE_LISTEN_SPELL
  );
  const [errorMessage, setErrorMessage] = useState('');

  const refreshPreference = useCallback(() => {
    const local = getPreference();
    setStudyModeLabel(modeLabel(local.studyMode, locale));
    setIsListenMode(local.studyMode === STUDY_MODE_LISTEN_SPELL);
    syncPreference()
      .then((pref) => {
        setStudyModeLabel(modeLabel(pref.studyMode, locale));
        setIsListenMode(pref.studyMode === STUDY_MODE_LISTEN_SPELL);
      })
      .catch(() => undefined);
  }, [locale]);

  const refreshHome = useCallback(() => {
    if (!getToken()) {
      setStatus('needLogin');
      setBook(null);
      return;
    }
    setStatus('loading');
    wordApi
      .currentBook()
      .then((res) => {
        const mapped = mapCurrent(res.data);
        if (!mapped) {
          setStatus('noBook');
          setBook(null);
          return;
        }
        setBookId(mapped.code);
        setBook(mapped);
        setStatus('ready');
      })
      .catch((err) => {
        setStatus('error');
        setErrorMessage(shortErrorMessage(err, t('word.home.loadBookFailed')));
      });
  }, [t]);

  useEffect(() => {
    refreshPreference();
    refreshHome();
  }, [refreshHome, refreshPreference]);

  if (status === 'needLogin') {
    return <WordSeoLanding />;
  }

  if (status === 'loading') {
    return (
      <div className="page">
        <Card className="state-card">{t('word.common.loading')}</Card>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>{t('word.home.networkError')}</Title>
          <Paragraph type="secondary">{errorMessage}</Paragraph>
          <Button onClick={refreshHome}>{t('word.common.retry')}</Button>
        </Card>
      </div>
    );
  }

  if (status === 'noBook') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>{t('word.home.noBookTitle')}</Title>
          <Paragraph type="secondary">{t('word.home.noBookHint')}</Paragraph>
          <Button type="primary" onClick={() => navigate('/word/books')}>
            {t('word.home.chooseBook')}
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <Title level={1} style={{ margin: 0, fontSize: 22 }}>
            {t('word.home.todayTitle')}
          </Title>
          <Text type="secondary" style={{ fontSize: 13 }}>
            {t('word.home.todaySubtitle')}
          </Text>
        </div>
        <Tag color={isListenMode ? 'processing' : 'default'}>{studyModeLabel}</Tag>
      </div>

      <div className="home-hero">
        <Card className="book-card">
          <Title level={4}>{book.name}</Title>
          <Progress percent={book.progressPercent} showInfo />
          <Space direction="vertical" size={4} style={{ width: '100%', marginTop: 12 }}>
            <Text>
              {t('word.home.learnStats', { todo: book.learnTodo, done: book.learnDone })}
            </Text>
            <Text>
              {t('word.home.reviewStats', { todo: book.reviewTodo, done: book.reviewDone })}
            </Text>
            <Text type="secondary">
              {t('word.home.planStats', {
                words: book.wordCount,
                days: book.planDays,
                remain: book.remainingDays,
              })}
            </Text>
          </Space>
        </Card>

        <Card>
          <Title level={5}>{t('word.home.quickActions')}</Title>
          <div className="home-actions">
            <Button
              type="primary"
              size="large"
              onClick={() => {
                if (!book?.code) {
                  message.warning(t('word.home.chooseBookFirst'));
                  return;
                }
                navigate('/word/study');
              }}
            >
              {t('word.home.startStudy')}
            </Button>
            <Button size="large" onClick={() => navigate('/word/books')}>
              {t('word.home.switchBook')}
            </Button>
            <Button size="large" onClick={() => navigate('/word/settings')}>
              {t('word.home.studySettings')}
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
