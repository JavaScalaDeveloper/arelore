import React, { useCallback, useEffect, useState } from 'react';
import { Button, Card, Progress, Space, Tag, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { wordApi } from '../api/word';
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
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'needLogin' | 'noBook' | 'error' | 'ready'>('loading');
  const [book, setBook] = useState<any>(null);
  const [studyModeLabel, setStudyModeLabel] = useState(modeLabel(getPreference().studyMode));
  const [isListenMode, setIsListenMode] = useState(
    getPreference().studyMode === STUDY_MODE_LISTEN_SPELL
  );
  const [errorMessage, setErrorMessage] = useState('');

  const refreshPreference = useCallback(() => {
    const local = getPreference();
    setStudyModeLabel(modeLabel(local.studyMode));
    setIsListenMode(local.studyMode === STUDY_MODE_LISTEN_SPELL);
    syncPreference()
      .then((pref) => {
        setStudyModeLabel(modeLabel(pref.studyMode));
        setIsListenMode(pref.studyMode === STUDY_MODE_LISTEN_SPELL);
      })
      .catch(() => undefined);
  }, []);

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
        setErrorMessage(shortErrorMessage(err, '加载当前词书失败'));
      });
  }, []);

  useEffect(() => {
    refreshPreference();
    refreshHome();
  }, [refreshHome, refreshPreference]);

  if (status === 'needLogin') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>请先登录</Title>
          <Paragraph type="secondary">登录后可选择单词书并开始学习</Paragraph>
          <Button type="primary" onClick={() => navigate('/word/login')}>
            邮箱登录
          </Button>
        </Card>
      </div>
    );
  }

  if (status === 'loading') {
    return (
      <div className="page">
        <Card className="state-card">加载中…</Card>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>网络不通</Title>
          <Paragraph type="secondary">{errorMessage}</Paragraph>
          <Button onClick={refreshHome}>重试</Button>
        </Card>
      </div>
    );
  }

  if (status === 'noBook') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>还没有单词书</Title>
          <Paragraph type="secondary">先选择一本词书并确认学习计划</Paragraph>
          <Button type="primary" onClick={() => navigate('/word/books')}>
            选择单词书
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-head">
        <Title level={3} style={{ margin: 0 }}>
          今日学习
        </Title>
        <Tag color={isListenMode ? 'processing' : 'default'}>{studyModeLabel}</Tag>
      </div>

      <div className="home-hero">
        <Card className="book-card">
          <Title level={4}>{book.name}</Title>
          <Progress percent={book.progressPercent} showInfo />
          <Space direction="vertical" size={4} style={{ width: '100%', marginTop: 12 }}>
            <Text>
              新学待学 {book.learnTodo} · 已学 {book.learnDone}
            </Text>
            <Text>
              复习待复 {book.reviewTodo} · 已复 {book.reviewDone}
            </Text>
            <Text type="secondary">
              共 {book.wordCount} 词 · 计划 {book.planDays} 天 · 剩余约 {book.remainingDays} 天
            </Text>
          </Space>
        </Card>

        <Card>
          <Title level={5}>快捷操作</Title>
          <div className="home-actions">
            <Button
              type="primary"
              size="large"
              onClick={() => {
                if (!book?.code) {
                  message.warning('请先选择单词书');
                  return;
                }
                navigate('/word/study');
              }}
            >
              开始学习
            </Button>
            <Button size="large" onClick={() => navigate('/word/books')}>
              切换词书
            </Button>
            <Button size="large" onClick={() => navigate('/word/settings')}>
              学习设置
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
