import React, { useEffect, useRef, useState } from 'react';
import { Button, Card, Slider, Space, Tag, Typography, message } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { LETTER_VOICES, normalizeLetterVoice } from '../utils/letterVoices';
import { modeLabel } from '../utils/preference';
import request, { shortErrorMessage } from '../utils/request';
import {
  getPreference,
  getToken,
  setPreference,
  STUDY_MODE_LISTEN_SPELL,
  STUDY_MODE_SEE_MEANING,
  STUDY_MODE_SEE_PICK_IMAGE,
  type WordPreference,
} from '../utils/storage';
import { playWordThenSpell, type TtsSession } from '../utils/tts';

const { Title, Text, Paragraph } = Typography;

export default function SettingsPage() {
  const navigate = useNavigate();
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
      if (!opts?.silent) message.success('已保存到本机');
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
      });
      setPref(saved);
      if (!opts?.silent) message.success('已保存');
    } catch (e: any) {
      setPreference(prev);
      setPref(prev);
      message.error(shortErrorMessage(e, '保存失败'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <Title level={3} style={{ margin: 0 }}>
          学习设置
        </Title>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/word')}>
          返回首页
        </Button>
      </div>

      <Card title="背词模式" style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          {[
            { mode: STUDY_MODE_LISTEN_SPELL, tip: '单词 → 拼读 → 中文，结束后自动下一词' },
            { mode: STUDY_MODE_SEE_MEANING, tip: '看单词，判断是否认识含义' },
            { mode: STUDY_MODE_SEE_PICK_IMAGE, tip: '暂未开放', disabled: true },
          ].map((item) => (
            <div
              key={item.mode}
              className={`option-item ${pref.studyMode === item.mode ? 'active' : ''} ${item.disabled ? 'disabled' : ''}`}
              onClick={() => {
                if (item.disabled) {
                  message.info('看词选图暂未开放');
                  return;
                }
                if (item.mode !== pref.studyMode) persist({ studyMode: item.mode as any });
              }}
            >
              <div>
                <Text strong>{modeLabel(item.mode)}</Text>
                <Paragraph type="secondary" style={{ margin: 0 }}>
                  {item.tip}
                </Paragraph>
              </div>
              {pref.studyMode === item.mode && <Tag color="blue">当前</Tag>}
            </div>
          ))}
        </Space>
      </Card>

      {pref.studyMode === STUDY_MODE_LISTEN_SPELL && (
        <Card title="听词选项">
          <div className="field">
            <div className="field-head">
              <Text>拼读语速</Text>
              <Text>{pref.spellSpeed}</Text>
            </div>
            <Paragraph type="secondary">0 最慢，10 最快；松手后用 hello 试听</Paragraph>
            {previewing && <Paragraph type="secondary">试听中：hello → 拼读…</Paragraph>}
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
              <Text>拼读音色</Text>
            </div>
            <Space wrap>
              <Button
                type={pref.letterVoice === 'random' ? 'primary' : 'default'}
                onClick={() => {
                  persist({ letterVoice: 'random' }, { skipRemote: true, silent: true });
                  preview({ letterVoice: 'random' });
                }}
              >
                随机
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
              <Text>连读次数</Text>
              <Text>{pref.wordRepeatCount} 次</Text>
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

      {saving && <Paragraph type="secondary">保存中…</Paragraph>}
    </div>
  );
}
