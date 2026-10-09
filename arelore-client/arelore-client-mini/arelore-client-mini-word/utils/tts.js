/**
 * 发音工具：有道 dictvoice。
 * 单词可整段朗读；整句常返回 500 null audio，因此例句按词排队播放。
 */

function dictVoiceUrl(text, type) {
  const voiceType = type === 1 ? 1 : 2;
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(text)}&type=${voiceType}`;
}

function isSingleWord(text) {
  const t = String(text || '').trim();
  return /^[A-Za-z][A-Za-z'-]*$/.test(t);
}

function splitWords(text) {
  const matched = String(text || '').match(/[A-Za-z']+/g);
  return matched || [];
}

/**
 * @param {WechatMiniprogram.InnerAudioContext} audio
 * @param {string} text
 * @param {1|2} type 1英 2美
 * @param {{ onStart?: Function, onEnd?: Function, onError?: Function }} hooks
 * @returns {{ stop: Function }}
 */
function playText(audio, text, type, hooks) {
  const h = hooks || {};
  let stopped = false;
  let queue = [];
  let queueIndex = 0;

  const clearHandlers = () => {
    if (audio && audio._areloreOnEnded) {
      try {
        audio.offEnded(audio._areloreOnEnded);
      } catch (e) {}
      audio._areloreOnEnded = null;
    }
    if (audio && audio._areloreOnError) {
      try {
        audio.offError(audio._areloreOnError);
      } catch (e) {}
      audio._areloreOnError = null;
    }
  };

  const finishOk = () => {
    clearHandlers();
    if (!stopped && h.onEnd) {
      h.onEnd();
    }
  };

  const finishErr = (err) => {
    clearHandlers();
    if (!stopped && h.onError) {
      h.onError(err);
    }
  };

  const playSrc = (src) => {
    try {
      audio.stop();
    } catch (e) {}
    audio.src = src;
    audio.play();
  };

  const playNextInQueue = () => {
    if (stopped) {
      return;
    }
    if (queueIndex >= queue.length) {
      finishOk();
      return;
    }
    const word = queue[queueIndex++];
    playSrc(dictVoiceUrl(word, type));
  };

  const startWordQueue = (sourceText) => {
    queue = splitWords(sourceText);
    if (!queue.length) {
      finishErr(new Error('empty sentence'));
      return;
    }
    queueIndex = 0;
    clearHandlers();
    const onEnded = () => playNextInQueue();
    const onError = () => playNextInQueue();
    audio._areloreOnEnded = onEnded;
    audio._areloreOnError = onError;
    audio.onEnded(onEnded);
    audio.onError(onError);
    if (h.onStart) {
      h.onStart();
    }
    playNextInQueue();
  };

  const start = () => {
    if (!audio || !text) {
      finishErr(new Error('no audio/text'));
      return;
    }
    const trimmed = String(text).trim();
    if (!trimmed) {
      finishErr(new Error('empty text'));
      return;
    }

    if (isSingleWord(trimmed)) {
      if (h.onStart) {
        h.onStart();
      }
      clearHandlers();
      const onEnded = () => finishOk();
      const onError = () => finishErr(new Error('dictvoice fail'));
      audio._areloreOnEnded = onEnded;
      audio._areloreOnError = onError;
      audio.onEnded(onEnded);
      audio.onError(onError);
      playSrc(dictVoiceUrl(trimmed, type));
      return;
    }

    startWordQueue(trimmed);
  };

  start();

  return {
    stop() {
      stopped = true;
      clearHandlers();
      try {
        audio.stop();
      } catch (e) {}
    }
  };
}

module.exports = {
  dictVoiceUrl,
  isSingleWord,
  playText
};
