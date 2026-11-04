import React, { useEffect, useRef, useState } from 'react';
import { Button, Card, Slider, Space, Tag, Typography, message } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useI18n, useLocaleNavigate } from '../../../locale';
import { LETTER_VOICES, normalizeLetterVoice } from '../utils/letterVoices';
import { modeLabel } from '../utils/preference';
import request, { shortErrorMessage } from '../utils/request';
import {
  getPreference,
  getToken,
  setPreference,
  STUDY_MODE_DICTATION,
  STUDY_MODE_LISTEN_SPELL,
  STUDY_MODE_SEE_MEANING,
  STUDY_MODE_SEE_PICK_IMAGE,
  type WordPreference,
} from '../utils/storage';
import { playWordThenSpell, type TtsSession } from '../utils/tts';

const { Title, Text, Paragraph } = Typography;

export default function SettingsPage() {
  const navigate = useLocaleNavigate();
  const { t, locale } = useI18n();
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const sessionRef = useRef<TtsSession | null>(null);
  const [pref, setPref] = useState<WordPreference>(getPreference());
  const [saving, setSaving] = useState(false);
  const [previewing, setPreviewing] = useState(false);

  useEffect(() => {
    audioRef.current = new Audio();
    return () => {
      sessionRef.current?.stop();
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  const stopPreview = () => {
    sessionRef.current?.stop();
    sessionRef.current = null;
    setPreviewing(false);
  };

  const preview = (next?: Partial<WordPreference>) => {
    if (!audioRef.current) return;
    stopPreview();
    const p = { ...pref, ...next };
    setPreviewing(true);
    sessionRef.current = playWordThenSpell(audioRef.current, 'hello', 2, {
      wordRepeatCount: 1,
      spellSpeed: p.spellSpeed,
      letterVoice: p.letterVoice,
      meaning: '',
      onEnd: () => {
        sessionRef.current = null;
        setPreviewing(false);
      },
    });
  };

  const persist = async (partial: Partial<WordPreference>, opts?: { skipRemote?: boolean; silent?: boolean }) => {
    const prev = getPreference();
    const next = setPreference(partial);
    setPref(next);
    if (opts?.skipRemote) {
      return;
    }
    if (!getToken()) {
      if (!opts?.silent) message.success(t('word.common.savedLocal'));
      return;
    }
    setSaving(true);
    try {
      const res: any = await request.post('/user/word/preference/update', {
        studyMode: next.studyMode,
        spellSpeed: next.spellSpeed,
        wordRepeatCount: next.wordRepeatCount,
      });
      const saved = setPreference({
        studyMode: res.data?.studyMode,
        spellSpeed: res.data?.spellSpeed,
        wordRepeatCount: res.data?.wordRepeatCount,
        letterVoice: next.letterVoice,
        playbackVolume: next.playbackVolume,
      });
      setPref(saved);
      if (!opts?.silent) message.success(t('word.common.saved'));
    } catch (e: any) {
      setPreference(prev);
      setPref(prev);
      message.error(shortErrorMessage(e, t('word.common.saveFailed')));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <Title level={3} style={{ margin: 0 }}>
          {t('word.settings.title')}
        </Title>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/word')}>
          {t('word.common.backHome')}
        </Button>
      </div>

      <Card title={t('word.settings.modesTitle')} style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          {[
            { mode: STUDY_MODE_LISTEN_SPELL, tip: t('word.settings.tipListen') },
            { mode: STUDY_MODE_SEE_MEANING, tip: t('word.settings.tipMeaning') },
            { mode: STUDY_MODE_DICTATION, tip: t('word.settings.tipDictation') },
            { mode: STUDY_MODE_SEE_PICK_IMAGE, tip: t('word.settings.tipPickImage'), disabled: true },
          ].map((item) => (
            <div
              key={item.mode}
              className={`option-item ${pref.studyMode === item.mode ? 'active' : ''} ${item.disabled ? 'disabled' : ''}`}
              onClick={() => {
                if (item.disabled) {
                  message.info(t('word.settings.pickImageClosed'));
                  return;
                }
                if (item.mode !== pref.studyMode) persist({ studyMode: item.mode as any });
              }}
            >
              <div>
                <Text strong>{modeLabel(item.mode, locale)}</Text>
                <Paragraph type="secondary" style={{ margin: 0 }}>
                  {item.tip}
                </Paragraph>
              </div>
              {pref.studyMode === item.mode && <Tag color="blue">{t('word.settings.current')}</Tag>}
            </div>
          ))}
        </Space>
      </Card>

      {pref.studyMode === STUDY_MODE_LISTEN_SPELL && (
        <Card title={t('word.settings.listenOptions')}>
          <div className="field">
            <div className="field-head">
              <Text>{t('word.settings.spellSpeed')}</Text>
              <Text>{pref.spellSpeed}</Text>
            </div>
            <Paragraph type="secondary">{t('word.settings.spellSpeedHint')}</Paragraph>
            {previewing && <Paragraph type="secondary">{t('word.settings.previewing')}</Paragraph>}
            <Slider
              min={0}
              max={10}
              value={pref.spellSpeed}
              onChange={(v) => setPref((p) => ({ ...p, spellSpeed: v }))}
              onAfterChange={(v) => {
                persist({ spellSpeed: v }, { silent: true });
                preview({ spellSpeed: v });
              }}
            />
          </div>

          <div className="field">
            <div className="field-head">
              <Text>{t('word.settings.letterVoice')}</Text>
            </div>
            <Space wrap>
              <Button
                type={pref.letterVoice === 'random' ? 'primary' : 'default'}
                onClick={() => {
                  persist({ letterVoice: 'random' }, { skipRemote: true, silent: true });
                  preview({ letterVoice: 'random' });
                }}
              >
                {t('word.settings.randomVoice')}
              </Button>
              {LETTER_VOICES.map((v) => (
                <Button
                  key={v.id}
                  type={pref.letterVoice === v.id ? 'primary' : 'default'}
                  onClick={() => {
                    const letterVoice = normalizeLetterVoice(v.id);
                    persist({ letterVoice }, { skipRemote: true, silent: true });
                    preview({ letterVoice });
                  }}
                >
                  {v.label}
                </Button>
              ))}
            </Space>
          </div>

          <div className="field">
            <div className="field-head">
              <Text>{t('word.settings.repeatCount')}</Text>
              <Text>{t('word.settings.times', { n: pref.wordRepeatCount })}</Text>
            </div>
            <Space>
              {[1, 2, 3].map((n) => (
                <Button
                  key={n}
                  type={pref.wordRepeatCount === n ? 'primary' : 'default'}
                  onClick={() => persist({ wordRepeatCount: n }, { silent: true })}
                >
                  {n}
                </Button>
              ))}
            </Space>
          </div>
        </Card>
      )}

      <Card title={t('word.settings.volumeTitle')} style={{ marginTop: 16, marginBottom: 16 }}>
        <div className="field">
          <div className="field-head">
            <Text>{t('word.settings.volume')}</Text>
            <Text>{pref.playbackVolume}%</Text>
          </div>
          <Paragraph type="secondary">{t('word.settings.volumeHint')}</Paragraph>
          {previewing && <Paragraph type="secondary">{t('word.settings.previewing')}</Paragraph>}
          <Slider
            min={0}
            max={100}
            value={pref.playbackVolume}
            onChange={(v) => setPref((p) => ({ ...p, playbackVolume: v }))}
            onAfterChange={(v) => {
              persist({ playbackVolume: v }, { skipRemote: true, silent: true });
              preview();
            }}
          />
        </div>
      </Card>

      {saving && <Paragraph type="secondary">{t('word.common.saving')}</Paragraph>}
    </div>
  );
}
