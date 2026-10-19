/**
 * 单词发音：有道 dictvoice（type=1 英音，type=2 美音）；失败时用 dictionaryapi.dev 的发音兜底。
 * 字母拼读：本地静态音色包 assets/letters/<pack>/a.mp3…z.mp3（不按字母打外网）；
 * 可配置固定音色或 random；整词字母共用同一 pack。语速只调字母间隔，每个字母必须播完。
 * 中文释义：逐条朗读全部释义。词性用本地 assets/pos 整段音频（n&a 读成「名词」「形容词」）。有道 dictvoice 对部分中文/标点会 HTTP 500，
 * 失败则改走百度 gettts；全部释义播完（或失败兜底结束）再 onEnd 切词。
 */

const letterVoices = require('./letterVoices');

const AFTER_WORD_PAUSE_MS = 400;
const AFTER_SPELL_PAUSE_MS = 280;
const BETWEEN_CLIP_GAP_MS = 40;
const BETWEEN_WORD_REPEAT_MS = 220;
const REMOTE_AUDIO_TIMEOUT_MS = 5000;
const LETTER_AUDIO_TIMEOUT_MS = 4000;
/** 估算单字母时长（本地包已用更快 TTS；高语速还会截尾静音） */
const LETTER_AUDIO_ESTIMATE_MS = 520;
const ZH_AUDIO_TIMEOUT_MS = 12000;
const DICT_API_TIMEOUT_MS = 8000;
/** word|type → dictionaryapi.dev 音频地址 */
const dictApiAudioCache = {};

function clamp(n, min, max, fallback) {
  const v = Number(n);
  if (!Number.isFinite(v)) {
    return fallback;
  }
  return Math.max(min, Math.min(max, Math.round(v)));
}

function dictVoiceUrl(text, type) {
  const voiceType = type === 1 ? 1 : 2;
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(text)}&type=${voiceType}`;
}

function dictVoiceZhUrl(text) {
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(text)}&le=zh`;
}

function requestJson(url, timeoutMs) {
  return new Promise((resolve, reject) => {
    if (typeof wx === 'undefined' || !wx.request) {
      reject(new Error('wx.request unavailable'));
      return;
    }
    wx.request({
      url,
      method: 'GET',
      timeout: timeoutMs || DICT_API_TIMEOUT_MS,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
          return;
        }
        reject(new Error('http ' + res.statusCode));
      },
      fail(err) {
        reject(err || new Error('request fail'));
      }
    });
  });
}

function pickDictionaryAudio(entries, type) {
  const audios = [];
  const list = Array.isArray(entries) ? entries : [];
  list.forEach((entry) => {
    const phonetics = entry && entry.phonetics;
    if (!Array.isArray(phonetics)) {
      return;
    }
    phonetics.forEach((item) => {
      const audioUrl = item && item.audio ? String(item.audio).trim() : '';
      if (audioUrl) {
        audios.push(audioUrl);
      }
    });
  });
  if (!audios.length) {
    return '';
  }
  const mark = type === 1 ? '-uk' : '-us';
  const matched = audios.find((item) => item.indexOf(mark) >= 0);
  return matched || audios[0];
}

/** 有道单词音频失败时，从 dictionaryapi.dev 取一条发音 */
function dictionaryAudioUrl(word, type) {
  const text = String(word || '').trim();
  if (!text || /\s/.test(text)) {
    return Promise.resolve('');
  }
  const voiceType = type === 1 ? 1 : 2;
  const cacheKey = `${text.toLowerCase()}|${voiceType}`;
  if (dictApiAudioCache[cacheKey]) {
    return Promise.resolve(dictApiAudioCache[cacheKey]);
  }
  const url = `https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(text)}`;
  return requestJson(url, DICT_API_TIMEOUT_MS).then((data) => {
    const src = pickDictionaryAudio(data, voiceType);
    if (src) {
      dictApiAudioCache[cacheKey] = src;
    }
    return src;
  }).catch(() => '');
}

/** 先播有道单词音，未正常结束再播 dictionaryapi.dev */
function playWordAudio(audio, word, type) {
  return playSrcOnce(audio, dictVoiceUrl(String(word).trim(), type), REMOTE_AUDIO_TIMEOUT_MS)
    .then((reason) => {
      if (reason === 'end') {
        return reason;
      }
      try {
        audio.stop();
      } catch (e) {}
      return dictionaryAudioUrl(word, type).then((src) => {
        if (!src) {
          return reason;
        }
        return playSrcOnce(audio, src, REMOTE_AUDIO_TIMEOUT_MS);
      });
    });
}

/** 有道中文合成对部分词/标点返回 HTTP 500 时的备用 */
function baiduZhUrl(text) {
  return `https://fanyi.baidu.com/gettts?lan=zh&text=${encodeURIComponent(text)}&spd=5&source=web`;
}

/** 本地字母音频路径；packId 为 jenny/guy/sonia/ryan */
function letterVoiceUrl(ch, packId) {
  return letterVoices.localLetterSrc(packId, ch);
}

/**
 * 字母间隔：0→160ms，10→0。
 */
function letterPauseMs(spellSpeed) {
  const s = clamp(spellSpeed, 0, 10, 5);
  return Math.round(160 - s * 16);
}

/** 每个字母播前空隙：0→20ms，5 及以上为 0 */
function letterClipGapMs(spellSpeed) {
  const s = clamp(spellSpeed, 0, 10, 5);
  if (s >= 5) {
    return 0;
  }
  return Math.round(20 - s * 4);
}

/**
 * 单字母最长播放（墙钟 ms）。语速 0 不截断，播完原始音频；语速升高才裁尾静音。
 * 0→2400，10→700。
 */
function letterClipMaxMs(spellSpeed) {
  const s = clamp(spellSpeed, 0, 10, 5);
  if (s <= 0) {
    return 2400;
  }
  return Math.round(1600 - s * 90);
}

/**
 * 字母播放倍速。语速 0 固定 1 倍（原始语速）；10→1.4。
 * 必须在设置 src 之后再写入，否则微信会重置为 1。
 */
function letterPlaybackRate(spellSpeed) {
  const s = clamp(spellSpeed, 0, 10, 5);
  if (s <= 0) {
    return 1;
  }
  return Math.round((1 + s * 0.04) * 100) / 100;
}

function extractLetters(word) {
  return String(word || '')
    .toLowerCase()
    .replace(/[^a-z]/g, '')
    .split('');
}

/**
 * 词典词性缩写 → 中文名 + 本地整段音频（assets/pos/<file>.mp3）。
 * 组合词性如 n&a、vt.&vi. 会拆成多段，各自播完整句，不再按字母或逐字读。
 */
const POS_SPEECH = {
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
  aux: { label: '助动词', file: 'aux' },
  vaux: { label: '助动词', file: 'aux' },
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
  idiom: { label: '习语', file: 'idiom' }
};

function posVoiceUrl(file) {
  if (!file) {
    return '';
  }
  return `/assets/pos/${file}.mp3`;
}

/** n&a. / vt.&vi. / n./v. → ['n','a'] */
function splitPosKeys(pos) {
  return String(pos || '')
    .split(/[&＆/／+＋、,，]/)
    .map((part) => part.replace(/[.．。\s]/g, '').toLowerCase())
    .filter(Boolean);
}

function posEntries(pos) {
  const keys = splitPosKeys(pos);
  const entries = [];
  const seen = {};
  keys.forEach((key) => {
    const item = POS_SPEECH[key];
    if (!item || seen[item.file]) {
      return;
    }
    seen[item.file] = true;
    entries.push(item);
  });
  return entries;
}

/**
 * 义项开头若全是已知词性缩写（可含 & / .），拆出来；否则整句当释义。
 * 「n&a. 中心(的)」→ 名词 + 形容词，释义「中心的」
 */
function peelLeadingPos(sense) {
  const text = String(sense || '').trim();
  const bracket = text.match(/^(【[^】]+】)\s*(.*)$/);
  if (bracket) {
    return { entries: [], glossLead: bracket[1], body: (bracket[2] || '').trim() };
  }
  const headMatch = text.match(/^([^一-龥（(]+)/);
  if (!headMatch) {
    return { entries: [], glossLead: '', body: text };
  }
  const head = headMatch[1].trim();
  const entries = posEntries(head);
  const keys = splitPosKeys(head);
  if (!entries.length || entries.length !== keys.length) {
    return { entries: [], glossLead: '', body: text };
  }
  return {
    entries,
    glossLead: '',
    body: text.slice(headMatch[1].length).trim()
  };
}

function tidyGloss(gloss) {
  return String(gloss || '')
    .replace(/[()（）]/g, '')
    .trim();
}

/**
 * 拆成待朗读的释义句：每个义项都读，词性用本地整段音频。
 * 例：「n&a. 中心(的)；v. 居中，使集中」
 * → {labels:名词/形容词, gloss:中心的}、{labels:动词, gloss:居中}、{gloss:使集中}
 */
function meaningsForSpeech(meaning) {
  const source = String(meaning || '').trim();
  if (!source) {
    return [];
  }
  const senses = source.split(/[；;]+/).map((part) => part.trim()).filter(Boolean);
  const lines = [];
  senses.forEach((sense) => {
    const peeled = peelLeadingPos(sense);
    const body = tidyGloss(peeled.body);
    const glosses = body.split(/[，,、]+/).map((part) => tidyGloss(part)).filter(Boolean);
    const labels = peeled.entries.map((item) => item.label);
    const files = peeled.entries.map((item) => item.file);
    if (peeled.glossLead) {
      labels.unshift(peeled.glossLead.replace(/[【】]/g, ''));
    }
    if (!glosses.length) {
      if (labels.length) {
        lines.push({ labels, files, gloss: '' });
      }
      return;
    }
    glosses.forEach((gloss, index) => {
      lines.push({
        labels: index === 0 ? labels : [],
        files: index === 0 ? files : [],
        gloss
      });
    });
  });
  return lines;
}

function meaningLineText(line) {
  if (!line) {
    return '';
  }
  if (typeof line === 'string') {
    return line;
  }
  const labels = (line.labels || []).filter(Boolean).join('，');
  return [labels, line.gloss].filter(Boolean).join('，');
}

/** 兼容旧调用：是否有可朗读释义 */
function meaningForSpeech(meaning) {
  const lines = meaningsForSpeech(meaning);
  return lines.map(meaningLineText).filter(Boolean).join('，');
}

function sleep(ms) {
  if (ms <= 0) {
    return Promise.resolve();
  }
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * 估算听词总时长（含可选中文），供页面 failsafe，须不短于真实播放。
 */
function estimateListenDurationMs(word, options) {
  const opts = options || {};
  const wordRepeatCount = clamp(opts.wordRepeatCount, 1, 3, 1);
  const spellSpeed = clamp(opts.spellSpeed, 0, 10, 5);
  const letters = extractLetters(word);
  const pauseMs = letterPauseMs(spellSpeed);
  const gapMs = letterClipGapMs(spellSpeed);
  const letterRate = letterPlaybackRate(spellSpeed);
  const clipMax = letterClipMaxMs(spellSpeed);
  const perLetterMs = Math.min(
    Math.round(LETTER_AUDIO_ESTIMATE_MS / letterRate),
    clipMax
  );
  const meaningLines = meaningsForSpeech(opts.meaning);
  const posClips = meaningLines.reduce((sum, line) => sum + ((line.files && line.files.length) || 0), 0);
  return wordRepeatCount * 1600
    + AFTER_WORD_PAUSE_MS
    + letters.length * (perLetterMs + gapMs + pauseMs)
    + AFTER_SPELL_PAUSE_MS
    + posClips * 1500
    + meaningLines.length * (ZH_AUDIO_TIMEOUT_MS + 400)
    + 2000;
}

/**
 * 播放一段音频；end/error/timeout 都会 resolve。
 * playbackRate 在赋值 src 之后设置（微信改 src 会把倍速重置为 1）。
 */
function playSrcOnce(audio, src, timeoutMs, playbackRate) {
  return new Promise((resolve) => {
    if (!audio || !src) {
      resolve('error');
      return;
    }

    const rate = playbackRate > 0 ? playbackRate : 1;
    let settled = false;
    let watchdog = null;
    let onEnded = null;
    let onError = null;
    let onCanplay = null;

    const applyRate = () => {
      try {
        audio.playbackRate = rate;
      } catch (e) {}
    };

    const settle = (reason) => {
      if (settled) {
        return;
      }
      settled = true;
      if (watchdog) {
        clearTimeout(watchdog);
        watchdog = null;
      }
      resolve(reason);
      try {
        if (onEnded) {
          audio.offEnded(onEnded);
        }
      } catch (e) {}
      try {
        if (onError) {
          audio.offError(onError);
        }
      } catch (e) {}
      try {
        if (onCanplay) {
          audio.offCanplay(onCanplay);
        }
      } catch (e) {}
    };

    onEnded = function onEnded() {
      settle('end');
    };
    onError = function onError() {
      settle('error');
    };
    onCanplay = function onCanplay() {
      applyRate();
    };

    audio.onEnded(onEnded);
    audio.onError(onError);
    try {
      audio.onCanplay(onCanplay);
    } catch (e) {}

    try {
      audio.src = src;
      applyRate();
      audio.play();
      applyRate();
    } catch (e) {
      settle('error');
      return;
    }

    watchdog = setTimeout(
      () => settle('timeout'),
      timeoutMs != null ? timeoutMs : REMOTE_AUDIO_TIMEOUT_MS
    );
  });
}

/** 有道对「动词，变清澈」这类带标点整句，以及部分词（如「清澈」）会 HTTP 500 */
function zhSpeechChunks(text) {
  return String(text || '')
    .split(/[，,、。；;！!？?\s]+/)
    .map((part) => part.trim())
    .filter(Boolean);
}

/**
 * 朗读一小段中文：先有道，未正常结束再百度。
 * 整段仍失败且多于一字时，再逐字重试（单字有道/百度更容易成功）。
 */
async function playZhChunk(audio, text) {
  const value = String(text || '').trim();
  if (!value || !audio) {
    return;
  }
  const youdaoReason = await playSrcOnce(audio, dictVoiceZhUrl(value), ZH_AUDIO_TIMEOUT_MS);
  if (youdaoReason === 'end') {
    return;
  }
  try {
    audio.stop();
  } catch (e) {}
  const baiduReason = await playSrcOnce(audio, baiduZhUrl(value), ZH_AUDIO_TIMEOUT_MS);
  if (baiduReason === 'end') {
    return;
  }
  try {
    audio.stop();
  } catch (e) {}
  if (value.length <= 1) {
    return;
  }
  const chars = value.split('');
  for (let i = 0; i < chars.length; i += 1) {
    await playZhChunk(audio, chars[i]);
  }
}

/**
 * 朗读一句中文释义。先按标点拆开，避免把「动词，变清澈」整句丢给有道（必 500）。
 * dictionaryapi.dev 只有英文单词发音，不能读中文释义，因此中文失败走百度，再不行逐字。
 */
async function playZhLine(audio, text) {
  const value = String(text || '').trim();
  if (!value || !audio) {
    return;
  }
  const chunks = zhSpeechChunks(value);
  const parts = chunks.length ? chunks : [value];
  for (let i = 0; i < parts.length; i += 1) {
    await playZhChunk(audio, parts[i]);
  }
}

/** 词性走本地整段音频；没有对应文件时整词朗读，不逐字拆开 */
async function playPosLabel(audio, file, label) {
  const src = posVoiceUrl(file);
  if (src) {
    const reason = await playSrcOnce(audio, src, 4000);
    if (reason === 'end') {
      return;
    }
    try {
      audio.stop();
    } catch (e) {}
  }
  const value = String(label || '').trim();
  if (!value) {
    return;
  }
  await playSrcOnce(audio, dictVoiceZhUrl(value), ZH_AUDIO_TIMEOUT_MS);
}

async function playMeaningLine(audio, line) {
  const item = typeof line === 'string' ? { labels: [], files: [], gloss: line } : (line || {});
  const files = item.files || [];
  const labels = item.labels || [];
  for (let i = 0; i < Math.max(files.length, labels.length); i += 1) {
    try {
      audio.stop();
    } catch (e) {}
    await playPosLabel(audio, files[i], labels[i]);
    await sleep(BETWEEN_CLIP_GAP_MS);
  }
  if (item.gloss) {
    try {
      audio.stop();
    } catch (e) {}
    await playZhLine(audio, item.gloss);
  }
}

function playText(audio, text, type, hooks) {
  const h = hooks || {};
  let stopped = false;
  const trimmed = String(text || '').trim();
  if (!trimmed) {
    if (h.onError) {
      h.onError(new Error('empty text'));
    }
    return {
      stop() {
        stopped = true;
      }
    };
  }
  if (h.onStart) {
    h.onStart();
  }
  playWordAudio(audio, trimmed, type).then((reason) => {
    if (stopped) {
      return;
    }
    if (reason === 'error') {
      if (h.onError) {
        h.onError(new Error('dictvoice fail'));
      }
      return;
    }
    if (h.onEnd) {
      h.onEnd();
    }
  });
  return {
    stop() {
      stopped = true;
      try {
        audio.stop();
      } catch (e) {}
    }
  };
}

/**
 * 听词摸鱼：单词连读 → 字母拼读 → 中文释义（有则等播完）→ onEnd。
 */
function playWordThenSpell(audio, word, type, options) {
  const opts = options || {};
  const wordRepeatCount = clamp(opts.wordRepeatCount, 1, 3, 1);
  const spellSpeed = clamp(opts.spellSpeed, 0, 10, 5);
  const meaning = opts.meaning || '';
  const letters = extractLetters(word);
  // 语速只影响字母间隔；每个字母等播完再下一个，全部拼完才 onEnd 切词
  const pauseMs = letterPauseMs(spellSpeed);
  const letterGapMs = letterClipGapMs(spellSpeed);
  const letterRate = letterPlaybackRate(spellSpeed);
  const letterMaxMs = letterClipMaxMs(spellSpeed);
  // 本词拼读锁定一个本地音色包（配置 random 则现场抽一个）
  const letterPackId = letterVoices.resolveLetterPackId(opts.letterVoice);

  let stopped = false;
  let finished = false;

  const finish = (tag) => {
    if (finished) {
      return;
    }
    finished = true;
    if (opts.onEnd) {
      try {
        opts.onEnd(tag || 'done');
      } catch (e) {}
    }
  };

  const stopAll = () => {
    stopped = true;
    finished = true;
    try {
      audio.stop();
    } catch (e) {}
  };

  const run = async () => {
    if (opts.onStart) {
      try {
        opts.onStart();
      } catch (e) {}
    }

    // 1) 单词连读（完整听完）
    for (let i = 0; i < wordRepeatCount; i += 1) {
      if (stopped) {
        return;
      }
      try {
        audio.stop();
      } catch (e) {}
      await sleep(BETWEEN_CLIP_GAP_MS);
      if (stopped) {
        return;
      }
      await playWordAudio(audio, String(word).trim(), type);
      if (stopped) {
        return;
      }
      if (i + 1 < wordRepeatCount) {
        await sleep(BETWEEN_WORD_REPEAT_MS);
      }
    }

    if (stopped) {
      return;
    }
    await sleep(AFTER_WORD_PAUSE_MS);

    // 2) 逐字母拼读：缩短间隔 + 加快倍速 + 高语速截尾静音
    for (let i = 0; i < letters.length; i += 1) {
      if (stopped) {
        return;
      }
      const src = letterVoiceUrl(letters[i], letterPackId);
      if (src) {
        try {
          audio.stop();
        } catch (e) {}
        await sleep(letterGapMs);
        if (stopped) {
          return;
        }
        const playBudget = Math.min(LETTER_AUDIO_TIMEOUT_MS, letterMaxMs);
        await playSrcOnce(audio, src, playBudget, letterRate);
        // 超时截尾后立刻停，避免与下一字母叠音
        if (!stopped) {
          try {
            audio.stop();
          } catch (e) {}
        }
      }
      if (stopped) {
        return;
      }
      if (i + 1 < letters.length) {
        await sleep(pauseMs);
      }
    }
    if (stopped) {
      return;
    }
    await sleep(AFTER_SPELL_PAUSE_MS);

    // 3) 逐条朗读全部释义（词性 + 释义），全部结束再切词
    const lines = meaningsForSpeech(meaning);
    for (let i = 0; i < lines.length; i += 1) {
      if (stopped) {
        return;
      }
      try {
        audio.stop();
      } catch (e) {}
      await sleep(BETWEEN_CLIP_GAP_MS);
      if (stopped) {
        return;
      }
      await playMeaningLine(audio, lines[i]);
    }

    if (!stopped) {
      finish(lines.length ? 'after_meaning' : 'after_spell');
    }
  };

  run().catch(() => {
    if (!stopped) {
      finish('error');
    }
  });

  return { stop: stopAll };
}

module.exports = {
  dictVoiceUrl,
  dictVoiceZhUrl,
  letterVoiceUrl,
  extractLetters,
  meaningForSpeech,
  meaningsForSpeech,
  letterPauseMs,
  estimateListenDurationMs,
  playText,
  playWordThenSpell
};
