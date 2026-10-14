/**
 * 单词发音：有道 dictvoice（type=1 英音，type=2 美音）。
 * 字母拼读：有道远程；拼读语速只调字母间隔，每个字母必须播完。
 * 中文释义：有道 le=zh；有释义时必须等播完（或失败）再 onEnd 切词。
 */

const AFTER_WORD_PAUSE_MS = 400;
const AFTER_SPELL_PAUSE_MS = 280;
const BETWEEN_CLIP_GAP_MS = 40;
const BETWEEN_WORD_REPEAT_MS = 220;
const REMOTE_AUDIO_TIMEOUT_MS = 5000;
const LETTER_AUDIO_TIMEOUT_MS = 5000;
/** 估算单字母完整时长（供 failsafe，须不短于真实播放） */
const LETTER_AUDIO_ESTIMATE_MS = 900;
const ZH_AUDIO_TIMEOUT_MS = 12000;

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

function letterVoiceUrl(ch, type) {
  const c = String(ch || '').toUpperCase();
  if (c < 'A' || c > 'Z') {
    return '';
  }
  return dictVoiceUrl(c, type);
}

/** 字母间隔（拼读语速唯一影响项）：0→420ms，10→0 */
function letterPauseMs(spellSpeed) {
  const s = clamp(spellSpeed, 0, 10, 5);
  return Math.round(420 - s * 42);
}

function extractLetters(word) {
  return String(word || '')
    .toLowerCase()
    .replace(/[^a-z]/g, '')
    .split('');
}

function meaningForSpeech(meaning) {
  let s = String(meaning || '').trim();
  if (!s) {
    return '';
  }
  s = s.replace(/(^|[；;])\s*[a-z]+\.\s*/gi, '$1');
  s = s.replace(/[；;]+/g, '，');
  s = s.replace(/\s+/g, ' ').trim();
  const first = s.split(/[，,]/)[0] || s;
  return first.length > 24 ? first.slice(0, 24) : first;
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
  const hasMeaning = !!meaningForSpeech(opts.meaning);
  // 字母按完整播完估算，语速只缩短字母间隔
  return wordRepeatCount * 1600
    + AFTER_WORD_PAUSE_MS
    + letters.length * (LETTER_AUDIO_ESTIMATE_MS + BETWEEN_CLIP_GAP_MS + pauseMs)
    + AFTER_SPELL_PAUSE_MS
    + (hasMeaning ? ZH_AUDIO_TIMEOUT_MS + 500 : 500)
    + 2000;
}

/**
 * 播放一段音频；end/error/timeout 都会 resolve。
 */
function playSrcOnce(audio, src, timeoutMs) {
  return new Promise((resolve) => {
    if (!audio || !src) {
      resolve('error');
      return;
    }

    let settled = false;
    let watchdog = null;
    let onEnded = null;
    let onError = null;

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
    };

    onEnded = function onEnded() {
      settle('end');
    };
    onError = function onError() {
      settle('error');
    };

    audio.onEnded(onEnded);
    audio.onError(onError);

    try {
      audio.src = src;
      audio.play();
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
  playSrcOnce(audio, dictVoiceUrl(trimmed, type), REMOTE_AUDIO_TIMEOUT_MS).then((reason) => {
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
      await playSrcOnce(audio, dictVoiceUrl(String(word).trim(), type), REMOTE_AUDIO_TIMEOUT_MS);
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

    // 2) 逐字母拼读：等当前字母完整播完，再按语速间隔播下一个
    for (let i = 0; i < letters.length; i += 1) {
      if (stopped) {
        return;
      }
      const src = letterVoiceUrl(letters[i], type);
      if (src) {
        try {
          audio.stop();
        } catch (e) {}
        await sleep(BETWEEN_CLIP_GAP_MS);
        if (stopped) {
          return;
        }
        await playSrcOnce(audio, src, LETTER_AUDIO_TIMEOUT_MS);
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

    // 3) 有中文释义则等播完（长超时），再 finish 切词
    const speech = meaningForSpeech(meaning);
    if (speech && !stopped) {
      try {
        audio.stop();
      } catch (e) {}
      await sleep(BETWEEN_CLIP_GAP_MS);
      if (!stopped) {
        await playSrcOnce(audio, dictVoiceZhUrl(speech), ZH_AUDIO_TIMEOUT_MS);
      }
    }

    if (!stopped) {
      finish(speech ? 'after_meaning' : 'after_spell');
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
  letterPauseMs,
  estimateListenDurationMs,
  playText,
  playWordThenSpell
};
