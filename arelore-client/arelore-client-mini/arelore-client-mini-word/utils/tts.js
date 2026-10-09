/**
 * 单词发音：有道 dictvoice（type=1 英音，type=2 美音）。
 * 整句接口常返回 500 null audio，例句朗读暂未启用。
 */

function dictVoiceUrl(text, type) {
  const voiceType = type === 1 ? 1 : 2;
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(text)}&type=${voiceType}`;
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

  if (!audio || !text) {
    finishErr(new Error('no audio/text'));
    return {
      stop() {
        stopped = true;
      }
    };
  }

  const trimmed = String(text).trim();
  if (!trimmed) {
    finishErr(new Error('empty text'));
    return {
      stop() {
        stopped = true;
      }
    };
  }

  clearHandlers();
  const onEnded = () => finishOk();
  const onError = () => finishErr(new Error('dictvoice fail'));
  audio._areloreOnEnded = onEnded;
  audio._areloreOnError = onError;
  audio.onEnded(onEnded);
  audio.onError(onError);

  if (h.onStart) {
    h.onStart();
  }
  try {
    audio.stop();
  } catch (e) {}
  audio.src = dictVoiceUrl(trimmed, type);
  audio.play();

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
  playText
};
