/**
 * 本地字母拼读音色包（assets/letters/<id>/a.mp3 … z.mp3）。
 * 由 scripts/generate-letter-voices.ps1 用 edge-tts 生成。
 */

const LETTER_VOICE_RANDOM = 'random';

const LETTER_VOICES = [
  { id: 'jenny', label: 'Jenny·美女' },
  { id: 'guy', label: 'Guy·美男' },
  { id: 'sonia', label: 'Sonia·英女' },
  { id: 'ryan', label: 'Ryan·英男' }
];

const LETTER_VOICE_IDS = LETTER_VOICES.map((v) => v.id);

function isKnownLetterVoice(id) {
  return LETTER_VOICE_IDS.indexOf(id) >= 0;
}

function normalizeLetterVoice(raw) {
  const value = raw == null ? '' : String(raw).trim();
  if (!value || value === LETTER_VOICE_RANDOM) {
    return LETTER_VOICE_RANDOM;
  }
  return isKnownLetterVoice(value) ? value : LETTER_VOICE_RANDOM;
}

/** 解析本次拼读实际使用的音色包 id（random 则现场抽一个） */
function resolveLetterPackId(letterVoice) {
  const pref = normalizeLetterVoice(letterVoice);
  if (pref !== LETTER_VOICE_RANDOM && isKnownLetterVoice(pref)) {
    return pref;
  }
  if (!LETTER_VOICE_IDS.length) {
    return 'jenny';
  }
  return LETTER_VOICE_IDS[Math.floor(Math.random() * LETTER_VOICE_IDS.length)];
}

/** 小程序包内路径，供 InnerAudioContext.src */
function localLetterSrc(packId, ch) {
  const c = String(ch || '').toLowerCase();
  if (c < 'a' || c > 'z' || !isKnownLetterVoice(packId)) {
    return '';
  }
  return `/assets/letters/${packId}/${c}.mp3`;
}

function letterVoiceLabel(id) {
  const pref = normalizeLetterVoice(id);
  if (pref === LETTER_VOICE_RANDOM) {
    return '随机';
  }
  const found = LETTER_VOICES.find((v) => v.id === pref);
  return found ? found.label : '随机';
}

module.exports = {
  LETTER_VOICE_RANDOM,
  LETTER_VOICES,
  LETTER_VOICE_IDS,
  isKnownLetterVoice,
  normalizeLetterVoice,
  resolveLetterPackId,
  localLetterSrc,
  letterVoiceLabel
};
