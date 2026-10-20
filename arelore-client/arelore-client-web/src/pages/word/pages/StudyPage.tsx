import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Button, Card, Image, Space, Tag, Typography, message } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { StudyCard, wordApi } from '../api/word';
import { shortErrorMessage } from '../utils/request';
import {
  getPreference,
  STUDY_MODE_LISTEN_SPELL,
} from '../utils/storage';
import { playWordOnly, playWordThenSpell, type TtsSession } from '../utils/tts';

const { Title, Text, Paragraph } = Typography;

function formatElapsed(ms: number): string {
  const total = Math.max(0, Math.floor(ms / 1000));
  const m = String(Math.floor(total / 60)).padStart(2, '0');
  const s = String(total % 60).padStart(2, '0');
  return `${m}:${s}`;
}

export default function StudyPage() {
  const navigate = useNavigate();
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const sessionRef = useRef<TtsSession | null>(null);
  const answeringLock = useRef(false);
  const cardEpoch = useRef(0);
  const listenHoldAdvance = useRef(false);
  const listenPausedRef = useRef(false);
  const failsafeTimer = useRef<number | null>(null);
  const startedAt = useRef(Date.now());
  const answerRef = useRef<(
    rememberFlag: boolean,
    goNextImmediately: boolean,
    targetCard?: StudyCard | null,
    expectEpoch?: number,
    expectWordCode?: string
  ) => Promise<void>>(async () => undefined);
  const cardsRef = useRef<StudyCard[]>([]);
  const indexRef = useRef(0);

  const pref = getPreference();
  const [status, setStatus] = useState<'loading' | 'ready' | 'done' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [bookCode, setBookCode] = useState('');
  const [, setCards] = useState<StudyCard[]>([]);
  const [, setIndex] = useState(0);
  const [card, setCard] = useState<StudyCard | null>(null);
  const [revealed, setRevealed] = useState(false);
  const [hintShown, setHintShown] = useState(false);
  const [pendingRemember, setPendingRemember] = useState<boolean | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [playing, setPlaying] = useState(false);
  const [playingType, setPlayingType] = useState(2);
  const [voiceType, setVoiceType] = useState(2);
  const [listenPaused, setListenPaused] = useState(false);
  const [elapsedText, setElapsedText] = useState('00:00');
  const [learnDone, setLearnDone] = useState(0);
  const [reviewDone, setReviewDone] = useState(0);
  const [learnTotal, setLearnTotal] = useState(0);
  const [reviewTotal, setReviewTotal] = useState(0);
  const [pictureError, setPictureError] = useState(false);

  const isListenMode = pref.studyMode === STUDY_MODE_LISTEN_SPELL;

  const stopAudio = useCallback(() => {
    sessionRef.current?.stop();
    sessionRef.current = null;
    if (failsafeTimer.current) {
      window.clearTimeout(failsafeTimer.current);
      failsafeTimer.current = null;
    }
    setPlaying(false);
  }, []);

  const bindStoredMedia = useCallback(async (c: StudyCard | null) => {
    if (!c) return;
    const wordCode = c.wordCode;
    const ids = (c.pictureFileIds || []).filter(Boolean);
    const meaningAudioFileId = c.meaningAudioFileId;

    const [picUrls, meaningAudioUrl] = await Promise.all([
      ids.length
        ? Promise.all(
            ids.map(async (id) => {
              try {
                const res = await wordApi.pictureContent(id);
                const data = res.data;
                if (!data?.base64Data) return '';
                return `data:${data.contentType || 'image/jpeg'};base64,${data.base64Data}`;
              } catch {
                return '';
              }
            })
          ).then((list) => list.filter(Boolean))
        : Promise.resolve([] as string[]),
      meaningAudioFileId
        ? (async () => {
            try {
              const res = await wordApi.pictureContent(meaningAudioFileId);
              const data = res.data;
              if (!data?.base64Data) return '';
              return `data:${data.contentType || 'audio/mpeg'};base64,${data.base64Data}`;
            } catch {
              return '';
            }
          })()
        : Promise.resolve(''),
    ]);

    setCard((current) => {
      if (!current || current.wordCode !== wordCode) return current;
      const next = { ...current };
      if (picUrls.length) {
        next.picture = picUrls[0];
        next.pictures = picUrls;
      }
      if (meaningAudioUrl) {
        next.meaningAudioUrl = meaningAudioUrl;
      }
      return next;
    });
    if (picUrls.length) setPictureError(false);

    // 同步写回队列，听词播放时能拿到 meaningAudioUrl
    const queue = cardsRef.current;
    const idx = queue.findIndex((item) => item.wordCode === wordCode);
    if (idx >= 0) {
      const updated = { ...queue[idx] };
      if (picUrls.length) {
        updated.picture = picUrls[0];
        updated.pictures = picUrls;
      }
      if (meaningAudioUrl) {
        updated.meaningAudioUrl = meaningAudioUrl;
      }
      queue[idx] = updated;
    }
  }, []);

  const goNext = useCallback(
    (remembered: boolean) => {
      const queue = cardsRef.current;
      const currentIndex = indexRef.current;
      const nextCards = queue.slice();
      const current = nextCards[currentIndex];
      const wasLast = currentIndex >= nextCards.length - 1;
      nextCards.splice(currentIndex, 1);
      if (!remembered && current) nextCards.push(current);
      if (!nextCards.length) {
        cardsRef.current = [];
        indexRef.current = 0;
        setCards([]);
        setCard(null);
        setStatus('done');
        stopAudio();
        return;
      }
      let nextIndex = currentIndex;
      if (nextIndex >= nextCards.length || (!remembered && wasLast && nextCards.length > 1)) {
        nextIndex = 0;
      }
      cardEpoch.current += 1;
      cardsRef.current = nextCards;
      indexRef.current = nextIndex;
      setCards(nextCards);
      setIndex(nextIndex);
      setCard(nextCards[nextIndex]);
      setRevealed(false);
      setHintShown(false);
      setPendingRemember(null);
      setSubmitting(false);
      setPictureError(false);
      listenPausedRef.current = false;
      setListenPaused(false);
      listenHoldAdvance.current = false;
      answeringLock.current = false;
      bindStoredMedia(nextCards[nextIndex]);
    },
    [bindStoredMedia, stopAudio]
  );

  const answerAndReveal = useCallback(
    async (
      rememberFlag: boolean,
      goNextImmediately: boolean,
      targetCard?: StudyCard | null,
      expectEpoch?: number,
      expectWordCode?: string
    ) => {
      const current = targetCard || card;
      if (!current) return;
      if (expectEpoch != null && cardEpoch.current !== expectEpoch) return;
      if (expectWordCode && current.wordCode !== expectWordCode) return;
      if (revealed && goNextImmediately && pendingRemember != null) {
        goNext(!!pendingRemember);
        return;
      }
      if (answeringLock.current || submitting || revealed) return;

      answeringLock.current = true;
      stopAudio();
      setSubmitting(true);
      setRevealed(true);
      setHintShown(false);
      setPendingRemember(!!rememberFlag);
      setPictureError(false);

      try {
        const res = await wordApi.answer({
          bookCode,
          wordCode: current.wordCode,
          mode: current.mode,
          rememberFlag,
        });
        if (res.data) {
          if (res.data.learnDone != null) setLearnDone(res.data.learnDone);
          if (res.data.reviewDone != null) setReviewDone(res.data.reviewDone);
        }
        answeringLock.current = false;
        setSubmitting(false);
        if (goNextImmediately || isListenMode) {
          goNext(!!rememberFlag);
        }
      } catch (e: any) {
        answeringLock.current = false;
        setSubmitting(false);
        message.error(shortErrorMessage(e, '提交失败'));
      }
    },
    [bookCode, card, goNext, isListenMode, pendingRemember, revealed, stopAudio, submitting]
  );

  useEffect(() => {
    answerRef.current = answerAndReveal;
  }, [answerAndReveal]);

  const ensureMeaningAudioUrl = useCallback(async (target: StudyCard): Promise<string> => {
    const wordCode = target.wordCode;
    const live = cardsRef.current.find((item) => item.wordCode === wordCode) || target;
    if (live.meaningAudioUrl) return live.meaningAudioUrl;
    const fileId = live.meaningAudioFileId || target.meaningAudioFileId;
    if (!fileId) return '';
    try {
      const res = await wordApi.pictureContent(fileId);
      const data = res.data;
      if (!data?.base64Data) return '';
      const url = `data:${data.contentType || 'audio/mpeg'};base64,${data.base64Data}`;
      const idx = cardsRef.current.findIndex((item) => item.wordCode === wordCode);
      if (idx >= 0) {
        cardsRef.current[idx] = { ...cardsRef.current[idx], meaningAudioUrl: url };
      }
      setCard((current) =>
        current && current.wordCode === wordCode ? { ...current, meaningAudioUrl: url } : current
      );
      return url;
    } catch {
      return '';
    }
  }, []);

  const playWord = useCallback(
    (target: StudyCard, type: number) => {
      if (!audioRef.current || !target?.word) return;
      stopAudio();
      const vt = type === 1 ? 1 : 2;
      setPlayingType(vt);
      const epoch = cardEpoch.current;
      const wordCode = target.wordCode;

      if (isListenMode) {
        void (async () => {
          if (cardEpoch.current !== epoch || !audioRef.current) return;
          const live = cardsRef.current.find((item) => item.wordCode === wordCode) || target;
          const meaningAudioUrl = await ensureMeaningAudioUrl(live);
          if (cardEpoch.current !== epoch || !audioRef.current) return;
          sessionRef.current = playWordThenSpell(audioRef.current, target.word, vt, {
            wordRepeatCount: pref.wordRepeatCount,
            spellSpeed: pref.spellSpeed,
            letterVoice: pref.letterVoice,
            meaning: live.meaning || target.meaning || '',
            meaningAudioUrl,
            onStart: () => setPlaying(true),
            onEnd: () => {
              setPlaying(false);
              if (cardEpoch.current !== epoch) return;
              if (listenPausedRef.current) {
                listenHoldAdvance.current = true;
                return;
              }
              if (answeringLock.current) return;
              void answerRef.current(true, true, target, epoch, wordCode);
            },
          });
          failsafeTimer.current = window.setTimeout(() => {
            if (cardEpoch.current !== epoch) return;
            void answerRef.current(true, true, target, epoch, wordCode);
          }, meaningAudioUrl ? 120000 : 45000);
        })();
        return;
      }

      sessionRef.current = playWordOnly(audioRef.current, target.word, vt);
      setPlaying(true);
      window.setTimeout(() => setPlaying(false), 4000);
    },
    [ensureMeaningAudioUrl, isListenMode, pref, stopAudio]
  );

  const loadSession = useCallback(() => {
    setStatus('loading');
    wordApi
      .session()
      .then((res) => {
        const data = res.data || {};
        const list = data.cards || [];
        setBookCode(data.bookCode || '');
        setLearnDone(data.learnDone || 0);
        setReviewDone(data.reviewDone || 0);
        setLearnTotal(data.learnTotal || 0);
        setReviewTotal(data.reviewTotal || 0);
        setVoiceType(data.voiceType === 1 ? 1 : 2);
        if (!list.length) {
          setStatus('done');
          return;
        }
        startedAt.current = Date.now();
        cardEpoch.current += 1;
        cardsRef.current = list;
        indexRef.current = 0;
        setCards(list);
        setIndex(0);
        setCard(list[0]);
        setRevealed(false);
        setHintShown(false);
        setPendingRemember(null);
        setSubmitting(false);
        setPictureError(false);
        setListenPaused(false);
        setStatus('ready');
        bindStoredMedia(list[0]);
      })
      .catch((e) => {
        setStatus('error');
        setErrorMessage(shortErrorMessage(e, '加载学习队列失败'));
      });
  }, [bindStoredMedia]);

  useEffect(() => {
    audioRef.current = new Audio();
    loadSession();
    const timer = window.setInterval(() => {
      setElapsedText(formatElapsed(Date.now() - startedAt.current));
    }, 1000);
    return () => {
      window.clearInterval(timer);
      stopAudio();
      audioRef.current = null;
    };
  }, [loadSession, stopAudio]);

  useEffect(() => {
    if (status === 'ready' && card?.word) {
      const t = window.setTimeout(() => playWord(card, voiceType), 250);
      return () => window.clearTimeout(t);
    }
    return undefined;
  }, [status, card?.wordCode]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (status !== 'ready' || !card) {
      return undefined;
    }
    const onKeyDown = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement)?.tagName;
      if (tag === 'INPUT' || tag === 'TEXTAREA' || (e.target as HTMLElement)?.isContentEditable) {
        return;
      }
      if (isListenMode) {
        if (e.key === ' ' || e.key === 'Enter') {
          e.preventDefault();
          listenPausedRef.current = false;
          setListenPaused(false);
          void answerAndReveal(true, true);
        } else if (e.key === 'p' || e.key === 'P') {
          e.preventDefault();
          if (listenPaused) {
            listenPausedRef.current = false;
            setListenPaused(false);
            if (listenHoldAdvance.current) {
              listenHoldAdvance.current = false;
              void answerAndReveal(true, true);
            } else {
              playWord(card, voiceType);
            }
          } else {
            listenPausedRef.current = true;
            setListenPaused(true);
            stopAudio();
          }
        } else if (e.key === 'r' || e.key === 'R') {
          e.preventDefault();
          playWord(card, voiceType);
        } else if (e.key === 'ArrowLeft' || e.key === '1') {
          e.preventDefault();
          listenPausedRef.current = false;
          setListenPaused(false);
          void answerAndReveal(false, true);
        }
        return;
      }
      if (!revealed) {
        if (e.key === 'ArrowRight' || e.key === '2' || e.key === 'Enter') {
          e.preventDefault();
          void answerAndReveal(true, false);
        } else if (e.key === 'ArrowLeft' || e.key === '1') {
          e.preventDefault();
          void answerAndReveal(false, false);
        } else if (e.key === 'h' || e.key === 'H' || e.key === ' ') {
          e.preventDefault();
          setHintShown(true);
        }
      } else if (e.key === ' ' || e.key === 'Enter' || e.key === 'ArrowRight') {
        e.preventDefault();
        goNext(!!pendingRemember);
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [
    status,
    card,
    isListenMode,
    revealed,
    pendingRemember,
    listenPaused,
    voiceType,
    answerAndReveal,
    goNext,
    playWord,
    stopAudio,
  ]);

  if (status === 'loading') {
    return (
      <div className="page">
        <Card className="state-card">准备学习…</Card>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>无法开始</Title>
          <Paragraph type="secondary">{errorMessage}</Paragraph>
          <Button onClick={() => navigate('/word')}>返回</Button>
        </Card>
      </div>
    );
  }

  if (status === 'done') {
    return (
      <div className="page">
        <Card className="state-card">
          <Title level={4}>本轮完成</Title>
          <Paragraph>
            新学 {learnDone}/{learnTotal} · 复习 {reviewDone}/{reviewTotal}
          </Paragraph>
          <Button type="primary" onClick={() => navigate('/word')}>
            返回首页
          </Button>
        </Card>
      </div>
    );
  }

  if (!card) return null;

  return (
    <div className="page study-page">
      <div className="page-head">
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/word')}>
          返回首页
        </Button>
        <Space size={16}>
          <Text>新学 {learnDone}/{learnTotal}</Text>
          <Text>复习 {reviewDone}/{reviewTotal}</Text>
          <Text type="secondary">{elapsedText}</Text>
        </Space>
      </div>

      <Space style={{ marginBottom: 8 }}>
        <Tag color={card.mode === 'review' ? 'orange' : 'blue'}>
          {card.mode === 'review' ? '复习' : '新学'}
        </Tag>
        {isListenMode && <Tag color="processing">听词摸鱼</Tag>}
      </Space>

      <Card className="word-card">
        <div className="word">{card.word}</div>
        {card.phonetic && <div className="phonetic">/{card.phonetic}/</div>}
        <Space style={{ marginTop: 12 }}>
          <Button
            size="small"
            type={voiceType === 2 ? 'primary' : 'default'}
            loading={playing && playingType === 2}
            onClick={() => playWord(card, 2)}
          >
            美音
          </Button>
          <Button
            size="small"
            type={voiceType === 1 ? 'primary' : 'default'}
            loading={playing && playingType === 1}
            onClick={() => playWord(card, 1)}
          >
            英音
          </Button>
        </Space>

        {(isListenMode || revealed) && (
          <div className="reveal">
            {card.picture && !pictureError && (
              <Image
                src={card.picture}
                width="100%"
                style={{ maxHeight: 220, objectFit: 'contain', marginTop: 12 }}
                onError={() => setPictureError(true)}
              />
            )}
            <Paragraph style={{ marginTop: 12 }}>{card.meaning || '暂无释义'}</Paragraph>
            {card.mnemonic && <Paragraph type="secondary">记忆：{card.mnemonic}</Paragraph>}
          </div>
        )}

        {!isListenMode && hintShown && !revealed && (
          <div className="reveal">
            <Text type="secondary">例句提示</Text>
            <Paragraph>{card.exampleEn || '暂无英文例句'}</Paragraph>
          </div>
        )}

        {!isListenMode && !hintShown && !revealed && (
          <Paragraph type="secondary" style={{ marginTop: 16 }}>
            先回想词义；不确定可点「提示」
          </Paragraph>
        )}
      </Card>

      {isListenMode ? (
        <>
          <Space className="study-actions" wrap size={12}>
            <Button
              onClick={() => {
                if (listenPaused) {
                  listenPausedRef.current = false;
                  setListenPaused(false);
                  if (listenHoldAdvance.current) {
                    listenHoldAdvance.current = false;
                    void answerAndReveal(true, true);
                  } else {
                    playWord(card, voiceType);
                  }
                } else {
                  listenPausedRef.current = true;
                  setListenPaused(true);
                  stopAudio();
                }
              }}
            >
              {listenPaused ? '继续 (P)' : '暂停 (P)'}
            </Button>
            <Button onClick={() => playWord(card, voiceType)}>重听 (R)</Button>
            <Button
              onClick={() => {
                listenPausedRef.current = false;
                setListenPaused(false);
                void answerAndReveal(false, true);
              }}
            >
              稍后复习 (←)
            </Button>
            <Button
              type="primary"
              onClick={() => {
                listenPausedRef.current = false;
                setListenPaused(false);
                void answerAndReveal(true, true);
              }}
            >
              下一词 (Space)
            </Button>
          </Space>
          <div className="study-hint">键盘：P 暂停/继续 · R 重听 · ← 稍后复习 · Space/Enter 下一词</div>
        </>
      ) : (
        <>
          <Space className="study-actions" wrap size={12}>
            {!revealed ? (
              <>
                <Button onClick={() => setHintShown(true)}>提示 (H / Space)</Button>
                <Button danger onClick={() => void answerAndReveal(false, false)}>
                  不认识 (← / 1)
                </Button>
                <Button type="primary" onClick={() => void answerAndReveal(true, false)}>
                  认识 (→ / 2 / Enter)
                </Button>
              </>
            ) : (
              <Button type="primary" onClick={() => goNext(!!pendingRemember)}>
                下一词 (Space / Enter)
              </Button>
            )}
          </Space>
          <div className="study-hint">
            {!revealed
              ? '键盘：←/1 不认识 · →/2/Enter 认识 · H/Space 提示'
              : '键盘：Space / Enter / → 下一词'}
          </div>
        </>
      )}
    </div>
  );
}
