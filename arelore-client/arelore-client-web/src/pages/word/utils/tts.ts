import { localLetterSrc, resolveLetterPackId } from './letterVoices';

const AFTER_WORD_PAUSE_MS = 400;
const AFTER_SPELL_PAUSE_MS = 280;
const BETWEEN_CLIP_GAP_MS = 40;
const BETWEEN_WORD_REPEAT_MS = 220;
const REMOTE_AUDIO_TIMEOUT_MS = 5000;
const LETTER_AUDIO_TIMEOUT_MS = 4000;
const ZH_AUDIO_TIMEOUT_MS = 12000;
/** 预生成整段释义 mp3，可能较长 */
const STORED_MEANING_AUDIO_TIMEOUT_MS = 90000;

const dictApiAudioCache: Record<string, string> = {};

function clamp(n: unknown, min: number, max: number, fallback: number): number {
  const v = Number(n);
  if (!Number.isFinite(v)) return fallback;
  return Math.max(min, Math.min(max, Math.round(v)));
}

function sleep(ms: number): Promise<void> {
  if (ms <= 0) return Promise.resolve();
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

function dictVoiceUrl(text: string, type: number): string {
  const voiceType = type === 1 ? 1 : 2;
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(text)}&type=${voiceType}`;
}

function dictVoiceZhUrl(text: string): string {
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(text)}&le=zh`;
}

function baiduZhUrl(text: string): string {
  return `https://fanyi.baidu.com/gettts?lan=zh&text=${encodeURIComponent(text)}&spd=5&source=web`;
}

function letterPauseMs(spellSpeed: number): number {
  const s = clamp(spellSpeed, 0, 10, 5);
  return Math.round(160 - s * 16);
}

function letterClipGapMs(spellSpeed: number): number {
  const s = clamp(spellSpeed, 0, 10, 5);
  if (s >= 5) return 0;
  return Math.round(20 - s * 4);
}

function letterClipMaxMs(spellSpeed: number): number {
  const s = clamp(spellSpeed, 0, 10, 5);
  if (s <= 0) return 2400;
  return Math.round(1600 - s * 90);
}

function letterPlaybackRate(spellSpeed: number): number {
  const s = clamp(spellSpeed, 0, 10, 5);
  if (s <= 0) return 1;
  return Math.round((1 + s * 0.04) * 100) / 100;
}

function extractLetters(word: string): string[] {
  return String(word || '').toLowerCase().replace(/[^a-z]/g, '').split('');
}

const POS_SPEECH: Record<string, { label: string; file: string }> = {
  n: { label: '名词', file: 'n' },
  v: { label: '动词', file: 'v' },
  vt: { label: '及物动词', file: 'vt' },
  vi: { label: '不及物动词', file: 'vi' },
  adj: { label: '形容词', file: 'adj' },
  a: { label: '形容词', file: 'adj' },
  adv: { label: '副词', file: 'adv' },
  ad: { label: '副词', file: 'adv' },
  prep: { label: '介词', file: 'prep' },
  conj: { label: '连词', file: 'conj' },
  pron: { label: '代词', file: 'pron' },
  art: { label: '冠词', file: 'art' },
  num: { label: '数词', file: 'num' },
  int: { label: '感叹词', file: 'int' },
  interj: { label: '感叹词', file: 'int' },
  exclam: { label: '感叹词', file: 'int' },
  aux: { label: '助动词', file: 'aux_verb' },
  vaux: { label: '助动词', file: 'aux_verb' },
  modal: { label: '情态动词', file: 'modal' },
  det: { label: '限定词', file: 'det' },
  abbr: { label: '缩写', file: 'abbr' },
  pl: { label: '复数', file: 'pl' },
  phrase: { label: '短语', file: 'phrase' },
  phr: { label: '短语', file: 'phrase' },
  pref: { label: '前缀', file: 'pref' },
  prefix: { label: '前缀', file: 'pref' },
  suf: { label: '后缀', file: 'suf' },
  suffix: { label: '后缀', file: 'suf' },
  u: { label: '不可数名词', file: 'uncount' },
  un: { label: '不可数名词', file: 'uncount' },
  c: { label: '可数名词', file: 'count' },
  cn: { label: '可数名词', file: 'count' },
  link: { label: '系动词', file: 'link' },
  linking: { label: '系动词', file: 'link' },
  ger: { label: '动名词', file: 'ger' },
  gerund: { label: '动名词', file: 'ger' },
  part: { label: '分词', file: 'part' },
  inf: { label: '不定式', file: 'inf' },
  ord: { label: '序数词', file: 'ord' },
  quant: { label: '量词', file: 'quant' },
  idiom: { label: '习语', file: 'idiom' },
};

function posVoiceUrl(file: string): string {
  if (!file) return '';
  const name = file === 'aux' ? 'aux_verb' : file;
  return `${process.env.PUBLIC_URL || ''}/assets/pos/${name}.mp3`;
}

function splitPosKeys(pos: string): string[] {
  return String(pos || '')
    .split(/[&＆/／+＋、,，]/)
    .map((part) => part.replace(/[.．。\s]/g, '').toLowerCase())
    .filter(Boolean);
}

function posEntries(pos: string): { label: string; file: string }[] {
  const keys = splitPosKeys(pos);
  const entries: { label: string; file: string }[] = [];
  const seen: Record<string, boolean> = {};
  keys.forEach((key) => {
    const item = POS_SPEECH[key];
    if (!item || seen[item.file]) return;
    seen[item.file] = true;
    entries.push(item);
  });
  return entries;
}

function peelLeadingPos(sense: string): { entries: { label: string; file: string }[]; body: string } {
  const text = String(sense || '').trim();
  const bracket = text.match(/^(【[^】]+】)\s*(.*)$/);
  if (bracket) {
    return { entries: [], body: text };
  }
  const headMatch = text.match(/^([^一-龥（(]+)/);
  if (!headMatch) return { entries: [], body: text };
  const head = headMatch[1].trim();
  const entries = posEntries(head);
  const keys = splitPosKeys(head);
  if (!entries.length || entries.length !== keys.length) {
    return { entries: [], body: text };
  }
  return { entries, body: text.slice(headMatch[1].length).trim() };
}

function tidyGloss(gloss: string): string {
  return String(gloss || '').replace(/[()（）]/g, '').trim();
}

interface MeaningLine {
  files: string[];
  labels: string[];
  gloss: string;
}

function meaningsForSpeech(meaning: string): MeaningLine[] {
  const source = String(meaning || '').trim();
  if (!source) return [];
  const senses = source.split(/[；;]+/).map((part) => part.trim()).filter(Boolean);
  const lines: MeaningLine[] = [];
  senses.forEach((sense) => {
    const peeled = peelLeadingPos(sense);
    const body = tidyGloss(peeled.body);
    const glosses = body.split(/[，,、]+/).map((part) => tidyGloss(part)).filter(Boolean);
    const labels = peeled.entries.map((item) => item.label);
    const files = peeled.entries.map((item) => item.file);
    if (!glosses.length) {
      if (labels.length) lines.push({ labels, files, gloss: '' });
      return;
    }
    glosses.forEach((gloss, index) => {
      lines.push({
        labels: index === 0 ? labels : [],
        files: index === 0 ? files : [],
        gloss,
      });
    });
  });
  return lines;
}

type PlayReason = 'end' | 'error' | 'timeout';

function playSrcOnce(
  audio: HTMLAudioElement,
  src: string,
  timeoutMs: number,
  playbackRate = 1,
  signal?: { stopped: boolean }
): Promise<PlayReason> {
  return new Promise((resolve) => {
    if (!audio || !src || signal?.stopped) {
      resolve('error');
      return;
    }
    const rate = playbackRate > 0 ? playbackRate : 1;
    let settled = false;
    let timer = 0;
    const onEnded = () => settle('end');
    const onError = () => settle('error');
    const applyRate = () => {
      try { audio.playbackRate = rate; } catch { /* ignore */ }
    };
    const cleanup = () => {
      audio.removeEventListener('ended', onEnded);
      audio.removeEventListener('error', onError);
      audio.removeEventListener('canplay', applyRate);
      if (timer) window.clearTimeout(timer);
    };
    const settle = (reason: PlayReason) => {
      if (settled) return;
      settled = true;
      cleanup();
      resolve(reason);
    };
    audio.addEventListener('ended', onEnded);
    audio.addEventListener('error', onError);
    audio.addEventListener('canplay', applyRate);
    try {
      audio.src = src;
      applyRate();
      const played = audio.play();
      if (played && typeof played.then === 'function') {
        played.then(applyRate).catch(() => settle('error'));
      }
    } catch {
      settle('error');
      return;
    }
    timer = window.setTimeout(() => settle('timeout'), timeoutMs);
  });
}

async function dictionaryAudioUrl(word: string, type: number): Promise<string> {
  const text = String(word || '').trim();
  if (!text || /\s/.test(text)) return '';
  const voiceType = type === 1 ? 1 : 2;
  const cacheKey = `${text.toLowerCase()}|${voiceType}`;
  if (dictApiAudioCache[cacheKey]) return dictApiAudioCache[cacheKey];
  try {
    const res = await fetch(`https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(text)}`);
    if (!res.ok) return '';
    const data = await res.json();
    const audios: string[] = [];
    (Array.isArray(data) ? data : []).forEach((entry: any) => {
      (entry?.phonetics || []).forEach((item: any) => {
        if (item?.audio) audios.push(String(item.audio));
      });
    });
    const mark = voiceType === 1 ? '-uk' : '-us';
    const src = audios.find((item) => item.indexOf(mark) >= 0) || audios[0] || '';
    if (src) dictApiAudioCache[cacheKey] = src;
    return src;
  } catch {
    return '';
  }
}

async function playWordAudio(audio: HTMLAudioElement, word: string, type: number, signal: { stopped: boolean }) {
  const reason = await playSrcOnce(audio, dictVoiceUrl(word.trim(), type), REMOTE_AUDIO_TIMEOUT_MS, 1, signal);
  if (reason === 'end' || signal.stopped) return;
  try { audio.pause(); } catch { /* ignore */ }
  const src = await dictionaryAudioUrl(word, type);
  if (src && !signal.stopped) {
    await playSrcOnce(audio, src, REMOTE_AUDIO_TIMEOUT_MS, 1, signal);
  }
}

function zhSpeechChunks(text: string): string[] {
  return String(text || '').split(/[，,、。；;！!？?\s]+/).map((part) => part.trim()).filter(Boolean);
}

async function playZhChunk(audio: HTMLAudioElement, text: string, signal: { stopped: boolean }) {
  const value = String(text || '').trim();
  if (!value || signal.stopped) return;
  const youdao = await playSrcOnce(audio, dictVoiceZhUrl(value), ZH_AUDIO_TIMEOUT_MS, 1, signal);
  if (youdao === 'end' || signal.stopped) return;
  try { audio.pause(); } catch { /* ignore */ }
  const baidu = await playSrcOnce(audio, baiduZhUrl(value), ZH_AUDIO_TIMEOUT_MS, 1, signal);
  if (baidu === 'end' || signal.stopped || value.length <= 1) return;
  for (const ch of value.split('')) {
    if (signal.stopped) return;
    await playZhChunk(audio, ch, signal);
  }
}

async function playZhLine(audio: HTMLAudioElement, text: string, signal: { stopped: boolean }) {
  const chunks = zhSpeechChunks(text);
  const parts = chunks.length ? chunks : [text];
  for (const part of parts) {
    if (signal.stopped) return;
    await playZhChunk(audio, part, signal);
  }
}

async function playPosLabel(audio: HTMLAudioElement, file: string, label: string, signal: { stopped: boolean }) {
  const src = posVoiceUrl(file);
  if (src) {
    const reason = await playSrcOnce(audio, src, 4000, 1, signal);
    if (reason === 'end' || signal.stopped) return;
    try { audio.pause(); } catch { /* ignore */ }
  }
  if (label) {
    await playSrcOnce(audio, dictVoiceZhUrl(label), ZH_AUDIO_TIMEOUT_MS, 1, signal);
  }
}

async function playMeaningLine(audio: HTMLAudioElement, line: MeaningLine, signal: { stopped: boolean }) {
  const count = Math.max(line.files.length, line.labels.length);
  for (let i = 0; i < count; i += 1) {
    if (signal.stopped) return;
    try { audio.pause(); } catch { /* ignore */ }
    await playPosLabel(audio, line.files[i], line.labels[i], signal);
    await sleep(BETWEEN_CLIP_GAP_MS);
  }
  if (line.gloss && !signal.stopped) {
    try { audio.pause(); } catch { /* ignore */ }
    await playZhLine(audio, line.gloss, signal);
  }
}

export interface TtsSession {
  stop: () => void;
}

export function playWordThenSpell(
  audio: HTMLAudioElement,
  word: string,
  type: number,
  options?: {
    wordRepeatCount?: number;
    spellSpeed?: number;
    letterVoice?: unknown;
    meaning?: string;
    /** 预生成释义语音 data URL / blob URL；播失败再走 meaning 在线 TTS */
    meaningAudioUrl?: string;
    onStart?: () => void;
    onEnd?: () => void;
  }
): TtsSession {
  const opts = options || {};
  const signal = { stopped: false };
  const wordRepeatCount = clamp(opts.wordRepeatCount, 1, 3, 1);
  const spellSpeed = clamp(opts.spellSpeed, 0, 10, 5);
  const pauseMs = letterPauseMs(spellSpeed);
  const letterGapMs = letterClipGapMs(spellSpeed);
  const letterMaxMs = letterClipMaxMs(spellSpeed);
  const letterRate = letterPlaybackRate(spellSpeed);
  const packId = resolveLetterPackId(opts.letterVoice);
  const letters = extractLetters(word);

  const finish = () => {
    if (!signal.stopped && opts.onEnd) {
      try { opts.onEnd(); } catch { /* ignore */ }
    }
  };

  const run = async () => {
    if (opts.onStart) {
      try { opts.onStart(); } catch { /* ignore */ }
    }
    for (let i = 0; i < wordRepeatCount; i += 1) {
      if (signal.stopped) return;
      try { audio.pause(); } catch { /* ignore */ }
      await sleep(BETWEEN_CLIP_GAP_MS);
      if (signal.stopped) return;
      await playWordAudio(audio, String(word).trim(), type, signal);
      if (i + 1 < wordRepeatCount) await sleep(BETWEEN_WORD_REPEAT_MS);
    }
    if (signal.stopped) return;
    await sleep(AFTER_WORD_PAUSE_MS);
    for (let i = 0; i < letters.length; i += 1) {
      if (signal.stopped) return;
      const src = localLetterSrc(packId, letters[i]);
      if (src) {
        try { audio.pause(); } catch { /* ignore */ }
        await sleep(letterGapMs);
        if (signal.stopped) return;
        await playSrcOnce(audio, src, Math.min(LETTER_AUDIO_TIMEOUT_MS, letterMaxMs), letterRate, signal);
        try { audio.pause(); } catch { /* ignore */ }
      }
      if (i + 1 < letters.length) await sleep(pauseMs);
    }
    if (signal.stopped) return;
    await sleep(AFTER_SPELL_PAUSE_MS);

    const storedMeaning = String(opts.meaningAudioUrl || '').trim();
    if (storedMeaning) {
      try { audio.pause(); } catch { /* ignore */ }
      await sleep(BETWEEN_CLIP_GAP_MS);
      if (!signal.stopped) {
        const reason = await playSrcOnce(
          audio,
          storedMeaning,
          STORED_MEANING_AUDIO_TIMEOUT_MS,
          1,
          signal
        );
        if (reason === 'end' || signal.stopped) {
          if (!signal.stopped) finish();
          return;
        }
      }
    }

    const lines = meaningsForSpeech(opts.meaning || '');
    for (const line of lines) {
      if (signal.stopped) return;
      try { audio.pause(); } catch { /* ignore */ }
      await sleep(BETWEEN_CLIP_GAP_MS);
      await playMeaningLine(audio, line, signal);
    }
    if (!signal.stopped) finish();
  };

  run().catch(() => {
    if (!signal.stopped) finish();
  });

  return {
    stop: () => {
      signal.stopped = true;
      try {
        audio.pause();
        audio.removeAttribute('src');
        audio.load();
      } catch { /* ignore */ }
    },
  };
}

export function playWordOnly(audio: HTMLAudioElement, word: string, type: number): TtsSession {
  const signal = { stopped: false };
  playWordAudio(audio, word, type, signal).catch(() => undefined);
  return {
    stop: () => {
      signal.stopped = true;
      try {
        audio.pause();
        audio.removeAttribute('src');
      } catch { /* ignore */ }
    },
  };
}
