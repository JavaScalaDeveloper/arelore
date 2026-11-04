const { postWithAuth, shortErrorMessage } = require('../../utils/request');
const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const tts = require('../../utils/tts');
const dictationUtil = require('../../utils/dictation');

/** 标准 QWERTY 三行布局 */
const LETTER_PAD_ROWS = [
  'qwertyuiop'.split(''),
  'asdfghjkl'.split(''),
  'zxcvbnm'.split('')
];

function pad2(n) {
  return n < 10 ? `0${n}` : `${n}`;
}

function formatElapsed(ms) {
  const totalSec = Math.max(0, Math.floor(ms / 1000));
  const min = Math.floor(totalSec / 60);
  const sec = totalSec % 60;
  return `${pad2(min)}:${pad2(sec)}`;
}

Page({
  data: {
    // loading | ready | done | error
    status: 'loading',
    errorTitle: '无法开始',
    errorMessage: '',
    needLogin: false,
    bookCode: '',
    cards: [],
    index: 0,
    card: null,
    revealed: false,
    hintShown: false,
    pendingRemember: null,
    answered: false,
    /** 自动下一词倒计时（秒）；0 表示未在倒计时。靠耗时 1s 心跳驱动，不依赖 setTimeout */
    autoNextSec: 0,
    learnTotal: 0,
    reviewTotal: 0,
    learnDone: 0,
    reviewDone: 0,
    voiceType: 2,
    studyMode: storage.STUDY_MODE_SEE_MEANING,
    isListenMode: false,
    isDictationMode: false,
    spellSpeed: storage.DEFAULT_SPELL_SPEED,
    wordRepeatCount: storage.DEFAULT_WORD_REPEAT_COUNT,
    letterVoice: storage.DEFAULT_LETTER_VOICE,
    playingType: 0,
    playingExampleKey: '',
    elapsedText: '00:00',
    submitting: false,
    playing: false,
    pictureError: false,
    /** 听词暂停：不自动切下一词 */
    listenPaused: false,
    dictationSlots: [],
    dictationFilled: [],
    dictationViewSlots: [],
    dictationShake: false,
    letterPadRows: LETTER_PAD_ROWS
  },

  onLoad() {
    this.dictationLock = false;
    this.timer = null;
    this.answeringLock = false;
    this.cardEpoch = 0;
    this.startedAt = 0;
    this.audio = null;
    this.ttsSession = null;
    this.listenAdvanceTimer = null;
    this.listenHoldAdvance = false;
    this.initAudio();
    this.refreshPreference();
    this.loadSession();
  },

  applyPreference(pref) {
    const p = pref || storage.getPreference();
    const isListenMode = p.studyMode === storage.STUDY_MODE_LISTEN_SPELL;
    const isDictationMode = p.studyMode === storage.STUDY_MODE_DICTATION;
    this.setData({
      studyMode: p.studyMode,
      isListenMode,
      isDictationMode,
      spellSpeed: p.spellSpeed,
      wordRepeatCount: p.wordRepeatCount,
      letterVoice: p.letterVoice
    });
  },

  resetDictation(word) {
    const slots = dictationUtil.buildDictationSlots(word || '');
    const filled = dictationUtil.emptyDictationFilled(slots);
    const currentIndex = dictationUtil.nextLetterSlotIndex(slots, filled);
    this.dictationLock = false;
    this.setData({
      dictationSlots: slots,
      dictationFilled: filled,
      dictationViewSlots: dictationUtil.buildDictationViewSlots(slots, filled, currentIndex),
      dictationShake: false
    });
  },

  refreshDictationView(filled) {
    const slots = this.data.dictationSlots || [];
    const nextFilled = filled || this.data.dictationFilled || [];
    const currentIndex = dictationUtil.nextLetterSlotIndex(slots, nextFilled);
    this.setData({
      dictationFilled: nextFilled,
      dictationViewSlots: dictationUtil.buildDictationViewSlots(slots, nextFilled, currentIndex)
    });
  },

  triggerDictationShake() {
    this.setData({ dictationShake: false });
    setTimeout(() => {
      this.setData({ dictationShake: true });
      setTimeout(() => this.setData({ dictationShake: false }), 360);
    }, 16);
  },

  refreshPreference() {
    this.applyPreference(storage.getPreference());
    preference.syncPreference().then((pref) => {
      this.applyPreference(pref);
    }).catch(() => {});
  },

  onHide() {
    // 不停耗时心跳：自动下一词靠 1s interval 递减 autoNextSec；hide 时停掉会失效。
    this.stopAudio();
  },

  onShow() {
    this.refreshPreference();
    this.startTimer();
  },

  onUnload() {
    this.stopTimer();
    this.destroyAudio();
  },

  initAudio() {
    const audio = wx.createInnerAudioContext();
    audio.obeyMuteSwitch = false;
    // 听词队列的错误由 tts.playSrcOnce 自行消化；这里不再挂全局 onError，
    // 避免与逐段 offError 冲突，以及 EncodingError 打断链路。
    // 耳机线控：系统 pause/play 映射到听词暂停/继续（程序主动 stop 会打标记忽略）
    audio.onPause(() => {
      if (tts.isProgrammaticAudioControl(audio)) {
        return;
      }
      if (!audio.__areloreClipActive) {
        return;
      }
      if (!this.isListenMode() || this.data.listenPaused || this.data.submitting) {
        return;
      }
      this.onListenPause();
    });
    audio.onPlay(() => {
      if (tts.isProgrammaticAudioControl(audio)) {
        return;
      }
      if (!this.isListenMode() || !this.data.listenPaused || this.data.submitting) {
        return;
      }
      // 系统恢复的是被暂停的片段；听词语义是整词重播/切词
      tts.softStop(audio);
      this.onListenResume();
    });
    this.audio = audio;
  },

  destroyAudio() {
    this.stopAudio();
    if (this.audio) {
      this.audio.destroy();
      this.audio = null;
    }
  },

  stopAudio() {
    this.clearListenAdvanceTimer();
    if (this.ttsSession) {
      try {
        this.ttsSession.stop();
      } catch (e) {}
      this.ttsSession = null;
    }
    if (this.audio) {
      tts.softStop(this.audio);
    }
  },

  clearListenAdvanceTimer() {
    if (this.listenAdvanceTimer) {
      clearTimeout(this.listenAdvanceTimer);
      this.listenAdvanceTimer = null;
    }
  },

  /**
   * 听词 failsafe：即使 onEnd 丢失，到期也自动「认识」并切词。
   */
  armListenAdvanceFailsafe(expectEpoch, expectWordCode, word, meaning, hasStoredMeaningAudio) {
    this.clearListenAdvanceTimer();
    if (this.data.listenPaused) {
      return;
    }
    const estimated = tts.estimateListenDurationMs(word, {
      wordRepeatCount: this.data.wordRepeatCount,
      spellSpeed: this.data.spellSpeed,
      meaning: meaning || ''
    });
    // 预生成整段释义可能比逐条在线 TTS 更长
    const waitMs = Math.max(10000, hasStoredMeaningAudio ? Math.max(estimated, 120000) : estimated);
    this.listenAdvanceTimer = setTimeout(() => {
      this.listenAdvanceTimer = null;
      this.onListenSequenceFinished(expectEpoch, expectWordCode);
    }, waitMs);
  },

  clearListenPauseState() {
    this.listenHoldAdvance = false;
    if (this.data.listenPaused) {
      this.setData({ listenPaused: false });
    }
  },

  /** 听词：暂停 — 停播且不再自动切词 */
  onListenPause() {
    if (!this.isListenMode() || this.data.submitting || this.data.listenPaused) {
      return;
    }
    this.clearListenAdvanceTimer();
    this.stopAudio();
    this.listenHoldAdvance = false;
    this.setData({ listenPaused: true, playing: false });
  },

  /** 听词：继续 — 退出暂停；若暂停前已播完则立刻切词，否则重播当前词 */
  onListenResume() {
    if (!this.isListenMode() || this.data.submitting || !this.data.listenPaused) {
      return;
    }
    const holdAdvance = this.listenHoldAdvance;
    this.listenHoldAdvance = false;
    this.setData({ listenPaused: false });
    if (holdAdvance) {
      const card = this.data.card;
      this.onListenSequenceFinished(this.cardEpoch, card && card.wordCode);
      return;
    }
    const card = this.data.card;
    if (!card || !card.word || this.data.revealed) {
      return;
    }
    this.playWord(card.word, this.data.voiceType === 1 ? 1 : 2);
  },

  startTimer() {
    if (this.timer) {
      return;
    }
    if (!this.startedAt) {
      this.startedAt = Date.now();
    }
    this.timer = setInterval(() => {
      const patch = {
        elapsedText: formatElapsed(Date.now() - this.startedAt)
      };
      // 自动下一词：每秒减 1，到 0 切词（不依赖 setTimeout）
      const sec = Number(this.data.autoNextSec) || 0;
      if (sec > 1) {
        patch.autoNextSec = sec - 1;
        this.setData(patch);
        return;
      }
      if (sec === 1) {
        patch.autoNextSec = 0;
        const shouldGo = !!(this.data.revealed || this.data.answered);
        this.setData(patch);
        if (shouldGo) {
          this.goNext();
        }
        return;
      }
      this.setData(patch);
    }, 1000);
  },

  stopTimer() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },

  loadSession() {
    const token = storage.getToken();
    if (!token) {
      this.setData({
        status: 'error',
        errorTitle: '需要登录',
        errorMessage: '学习进度需登录后同步。可返回首页先浏览单词书。',
        needLogin: true
      });
      return;
    }
    this.setData({ status: 'loading' });
    postWithAuth('/user/word/study/session', {}).then((res) => {
      if (res.code === 401) {
        storage.clearSession();
        this.setData({ status: 'error', errorMessage: '登录已过期，请重新登录' });
        return;
      }
      if (res.code !== 200 || !res.data) {
        this.setData({ status: 'error', errorMessage: res.message || '加载学习队列失败' });
        return;
      }
      const data = res.data;
      const cards = data.cards || [];
      const learnDone = data.learnDone || 0;
      const reviewDone = data.reviewDone || 0;
      const learnTotal = data.learnTotal || 0;
      const reviewTotal = data.reviewTotal || 0;
      if (!cards.length) {
        this.setData({
          status: 'done',
          bookCode: data.bookCode || '',
          learnTotal,
          reviewTotal,
          learnDone,
          reviewDone
        });
        return;
      }
      this.startedAt = Date.now();
      this.clearAutoNext();
      this.answeringLock = false;
      this.cardEpoch += 1;
      const voiceType = data.voiceType === 1 ? 1 : 2;
      this.setData({
        status: 'ready',
        bookCode: data.bookCode || '',
        cards,
        index: 0,
        card: cards[0],
        revealed: false,
        hintShown: false,
        pendingRemember: null,
        answered: false,
        submitting: false,
        learnTotal,
        reviewTotal,
        learnDone,
        reviewDone,
        voiceType,
        elapsedText: '00:00',
        pictureError: false,
        autoNextSec: 0
      });
      this.resetDictation(cards[0] && cards[0].word);
      this.startTimer();
      this.autoPlayCurrent();
      this.bindStoredPictures(cards[0]);
    }).catch((err) => {
      const isNet = !!(err && err.isNetworkError);
      this.setData({
        status: 'error',
        errorTitle: isNet ? '网络不通' : '无法开始',
        errorMessage: shortErrorMessage(err, '网络不通，加载学习队列失败')
      });
    });
  },

  isListenMode() {
    return this.data.studyMode === storage.STUDY_MODE_LISTEN_SPELL
      || storage.getStudyMode() === storage.STUDY_MODE_LISTEN_SPELL;
  },

  isDictationMode() {
    return this.data.studyMode === storage.STUDY_MODE_DICTATION
      || storage.getStudyMode() === storage.STUDY_MODE_DICTATION;
  },

  autoPlayCurrent() {
    const card = this.data.card;
    if (!card || !card.word) {
      return;
    }
    const type = this.data.voiceType === 1 ? 1 : 2;
    setTimeout(() => this.playWord(card.word, type), 200);
  },

  onPlayVoice(e) {
    const card = this.data.card;
    if (!card || !card.word) {
      return;
    }
    const type = Number(e.currentTarget.dataset.type) === 1 ? 1 : 2;
    this.playWord(card.word, type);
  },

  playWord(word, type) {
    if (!this.audio || !word) {
      return;
    }
    // 注意：stopAudio 会清 failsafe；听词下面会重新 arm
    this.stopAudio();
    const voiceType = type === 1 ? 1 : 2;
    const listenMode = this.isListenMode();
    const playEpoch = this.cardEpoch;
    const playWordCode = this.data.card && this.data.card.wordCode;
    this.setData({ playingType: voiceType, playingExampleKey: '' });

    // 听词模式：整段结束后一律自动「认识」并切词（不再依赖额外开关）
    const triggerListenAdvance = () => {
      this.clearListenAdvanceTimer();
      this.onListenSequenceFinished(playEpoch, playWordCode);
    };

    if (listenMode) {
      const card = this.data.card;
      const meaning = card && card.meaning ? card.meaning : '';
      const startListen = (meaningAudioUrl) => {
        if (this.cardEpoch !== playEpoch) {
          return;
        }
        this.armListenAdvanceFailsafe(playEpoch, playWordCode, word, meaning, !!meaningAudioUrl);
        this.ttsSession = tts.playWordThenSpell(this.audio, word, voiceType, {
          onStart: () => this.setData({ playing: true }),
          onEnd: () => {
            this.setData({ playing: false, playingExampleKey: '' });
            triggerListenAdvance();
          },
          wordRepeatCount: this.data.wordRepeatCount,
          spellSpeed: this.data.spellSpeed,
          letterVoice: this.data.letterVoice,
          meaning,
          meaningAudioUrl: meaningAudioUrl || ''
        });
      };
      const existingUrl = card && card.meaningAudioUrl ? card.meaningAudioUrl : '';
      if (existingUrl) {
        startListen(existingUrl);
        return;
      }
      const audioFileId = card && card.meaningAudioFileId;
      if (audioFileId) {
        this.loadStoredBinary(audioFileId, 'audio').then((path) => {
          if (this.cardEpoch !== playEpoch) {
            return;
          }
          if (path) {
            this.setData({ 'card.meaningAudioUrl': path });
          }
          startListen(path || '');
        });
        return;
      }
      startListen('');
      return;
    }

    this.ttsSession = tts.playText(this.audio, word, voiceType, {
      onStart: () => this.setData({ playing: true }),
      onEnd: () => this.setData({ playing: false, playingExampleKey: '' }),
      onError: () => {
        this.setData({ playing: false, playingExampleKey: '' });
        wx.showToast({ title: '发音播放失败', icon: 'none' });
      }
    });
  },

  /** 听词拼读结束：自动记为认识并立刻切下一词（可被 failsafe 重复调用，内部有防重） */
  onListenSequenceFinished(expectEpoch, expectWordCode) {
    if (!this.isListenMode()) {
      return;
    }
    if (this.cardEpoch !== expectEpoch) {
      return;
    }
    const card = this.data.card;
    if (!card || (expectWordCode && card.wordCode !== expectWordCode)) {
      return;
    }
    // 暂停中：不切词，等「继续」再推进
    if (this.data.listenPaused) {
      this.listenHoldAdvance = true;
      this.clearListenAdvanceTimer();
      this.setData({ playing: false });
      return;
    }
    // 已经在切词/提交中
    if (this.answeringLock || this.data.submitting) {
      return;
    }
    if (!this.data.revealed) {
      this.answerAndReveal(true, { goNextImmediately: true });
      return;
    }
    if (this.data.pendingRemember === true) {
      this.goNext();
    }
  },

  onPictureError() {
    const card = this.data.card;
    const ids = card && Array.isArray(card.pictureFileIds) ? card.pictureFileIds.filter(Boolean) : [];
    if (ids.length && card.picture && String(card.picture).indexOf('word-pic-') < 0) {
      this.bindStoredPictures(card);
      return;
    }
    this.setData({ pictureError: true });
  },

  bindStoredPictures(card) {
    if (!card) {
      return;
    }
    const wordCode = card.wordCode;
    const ids = Array.isArray(card.pictureFileIds) ? card.pictureFileIds.filter(Boolean) : [];
    const meaningAudioFileId = card.meaningAudioFileId;
    Promise.all([
      ids.length
        ? Promise.all(ids.map((id) => this.loadStoredBinary(id, 'pic')))
        : Promise.resolve([]),
      meaningAudioFileId
        ? this.loadStoredBinary(meaningAudioFileId, 'audio')
        : Promise.resolve('')
    ]).then(([paths, meaningAudioUrl]) => {
      const urls = (paths || []).filter(Boolean);
      const current = this.data.card;
      if (!current || current.wordCode !== wordCode) {
        return;
      }
      if (!urls.length && !meaningAudioUrl) {
        return;
      }
      const patch = {};
      if (urls.length) {
        patch['card.picture'] = urls[0];
        patch['card.pictures'] = urls;
        patch.pictureError = false;
      }
      if (meaningAudioUrl) {
        patch['card.meaningAudioUrl'] = meaningAudioUrl;
      }
      this.setData(patch);
    });
  },

  loadStoredBinary(fileId, kind) {
    return postWithAuth('/user/word/picture-content', { id: fileId }).then((res) => {
      const data = res && res.code === 200 ? res.data : null;
      if (!data || !data.base64Data) {
        return '';
      }
      const type = String(data.contentType || '');
      let ext = 'bin';
      if (kind === 'audio' || type.indexOf('audio') >= 0 || type.indexOf('mpeg') >= 0) {
        ext = 'mp3';
      } else if (type.indexOf('png') >= 0) {
        ext = 'png';
      } else if (type.indexOf('webp') >= 0) {
        ext = 'webp';
      } else {
        ext = 'jpg';
      }
      const prefix = kind === 'audio' ? 'word-audio' : 'word-pic';
      const filePath = `${wx.env.USER_DATA_PATH}/${prefix}-${fileId}.${ext}`;
      return new Promise((resolve) => {
        wx.getFileSystemManager().writeFile({
          filePath,
          data: data.base64Data,
          encoding: 'base64',
          success: () => resolve(filePath),
          fail: () => resolve('')
        });
      });
    }).catch(() => '');
  },

  onPreviewPictures() {
    const card = this.data.card;
    if (!card) {
      return;
    }
    const urls = Array.isArray(card.pictures) && card.pictures.length
      ? card.pictures.filter(Boolean)
      : (card.picture ? [card.picture] : []);
    if (!urls.length) {
      return;
    }
    wx.previewImage({
      current: urls[0],
      urls
    });
  },

  onHint() {
    if (!this.data.card || this.data.revealed) {
      return;
    }
    this.setData({ hintShown: true });
  },

  onKnown() {
    this.answerAndReveal(true);
  },

  onUnknown() {
    this.answerAndReveal(false);
  },

  /** 听词：重听当前词整段（单词→拼读→中文） */
  onListenReplay() {
    if (!this.isListenMode() || this.data.submitting || this.data.revealed) {
      return;
    }
    // 重听视为继续学习：退出暂停，播完仍可自动切词
    this.listenHoldAdvance = false;
    if (this.data.listenPaused) {
      this.setData({ listenPaused: false });
    }
    const card = this.data.card;
    if (!card || !card.word) {
      return;
    }
    this.playWord(card.word, this.data.voiceType === 1 ? 1 : 2);
  },

  /** 听词：立刻记为认识并切下一词 */
  onListenSkip() {
    if (!this.isListenMode() || this.answeringLock || this.data.submitting) {
      return;
    }
    const card = this.data.card;
    if (!card) {
      return;
    }
    this.clearListenPauseState();
    this.stopAudio();
    this.setData({ playing: false });
    if (!this.data.revealed) {
      this.answerAndReveal(true, { goNextImmediately: true });
      return;
    }
    if (this.data.pendingRemember === true) {
      this.goNext();
    }
  },

  /** 听词：记为不认识，放回队列稍后复习并继续下一词 */
  onListenUnknown() {
    if (!this.isListenMode() || this.answeringLock || this.data.submitting || this.data.revealed) {
      return;
    }
    this.clearListenPauseState();
    this.stopAudio();
    this.setData({ playing: false });
    this.answerAndReveal(false, { goNextImmediately: true });
  },

  clearAutoNext() {
    if (this.data.autoNextSec) {
      this.setData({ autoNextSec: 0 });
    }
  },

  /** @param {number} sec 倒计时秒数，由 startTimer 的 1s 心跳递减 */
  scheduleAutoNext(sec) {
    const n = Math.max(1, Math.floor(sec == null ? 3 : sec));
    this.setData({ autoNextSec: n });
    this.startTimer();
  },

  answerAndReveal(rememberFlag, options) {
    const opts = options || {};
    const card = this.data.card;
    if (!card || this.answeringLock || this.data.submitting || this.data.revealed) {
      return;
    }
    this.answeringLock = true;
    const answeredWordCode = card.wordCode;
    const answeredEpoch = this.cardEpoch;
    const inDictation = !!(this.data.isDictationMode || this.isDictationMode());
    const dictationUnknown = inDictation && !rememberFlag && !opts.goNextImmediately;
    this.setData({
      submitting: true,
      revealed: true,
      hintShown: false,
      pendingRemember: !!rememberFlag,
      answered: true,
      pictureError: false,
      // 听写「不认识」：立刻开始 3 秒倒计时（界面可见，靠 1s 心跳驱动）
      autoNextSec: dictationUnknown ? 3 : 0
    });
    if (dictationUnknown) {
      this.startTimer();
    }
    postWithAuth('/user/word/study/answer', {
      bookCode: this.data.bookCode,
      wordCode: card.wordCode,
      mode: card.mode,
      rememberFlag
    }).then((res) => {
      this.answeringLock = false;
      // 响应回来时已不在该词：忽略（防重复 answer 的迟到回调）
      if (this.cardEpoch !== answeredEpoch
        || !this.data.card
        || String(this.data.card.wordCode) !== String(answeredWordCode)) {
        this.setData({ submitting: false });
        return;
      }
      if (res.code === 401) {
        storage.clearSession();
        this.setData({ submitting: false, autoNextSec: 0 });
        wx.showToast({ title: '请重新登录', icon: 'none' });
        return;
      }
      if (res.code !== 200) {
        // 提交失败：收回揭示态，允许重试
        this.dictationLock = false;
        this.setData({
          submitting: false,
          revealed: false,
          answered: false,
          pendingRemember: null,
          autoNextSec: 0
        });
        wx.showToast({ title: res.message || '提交失败', icon: 'none' });
        return;
      }
      const patch = { submitting: false };
      // 进度以后端为准：只有「认识」才会计入完成
      if (res.data) {
        if (res.data.learnDone != null) {
          patch.learnDone = res.data.learnDone;
        }
        if (res.data.reviewDone != null) {
          patch.reviewDone = res.data.reviewDone;
        }
      }
      this.setData(patch);
      // 听词：提交后立刻切下一词；听写「认识」立即切；听写「不认识」倒计时已在揭示时启动
      if (opts.goNextImmediately || this.isListenMode()) {
        this.goNext();
        return;
      }
      if (dictationUnknown) {
        return;
      }
      if (rememberFlag) {
        this.scheduleAutoNext(1);
      }
    }).catch((err) => {
      this.answeringLock = false;
      this.dictationLock = false;
      if (this.cardEpoch === answeredEpoch
        && this.data.card
        && String(this.data.card.wordCode) === String(answeredWordCode)) {
        this.setData({
          submitting: false,
          revealed: false,
          answered: false,
          pendingRemember: null,
          autoNextSec: 0
        });
      } else {
        this.setData({ submitting: false, autoNextSec: 0 });
      }
      const msg = shortErrorMessage(err, '网络不通，提交失败');
      wx.showToast({ title: msg.slice(0, 20), icon: 'none' });
    });
  },

  onNextAfterReveal() {
    if (!this.data.revealed || this.data.submitting) {
      return;
    }
    this.clearAutoNext();
    this.goNext();
  },

  finishRound() {
    this.stopTimer();
    this.listenHoldAdvance = false;
    this.setData({
      status: 'done',
      cards: [],
      index: 0,
      card: null,
      revealed: false,
      hintShown: false,
      pendingRemember: null,
      answered: false,
      submitting: false,
      listenPaused: false,
      playing: false,
      autoNextSec: 0
    });
  },

  goNext() {
    this.clearAutoNext();
    this.stopAudio();
    this.answeringLock = false;
    this.listenHoldAdvance = false;
    const remembered = this.data.pendingRemember === true;
    const cards = (this.data.cards || []).slice();
    let index = this.data.index;
    if (!cards.length || index < 0 || index >= cards.length) {
      this.finishRound();
      return;
    }
    // 未作答不得切下一词（防止过期 autoNext / 误触）
    if (!this.data.revealed && !this.data.answered) {
      return;
    }
    const current = cards[index];
    const wasLast = index >= cards.length - 1;
    // 认识：移出队列；不认识：放到队尾，本轮还会再出现
    cards.splice(index, 1);
    if (!remembered && current) {
      cards.push(current);
    }
    if (!cards.length) {
      this.finishRound();
      return;
    }
    // 答完队尾后：从队头继续，避免未记住的词立刻再弹一次
    if (index >= cards.length || (!remembered && wasLast && cards.length > 1)) {
      index = 0;
    }
    this.cardEpoch += 1;
    this.setData({
      cards,
      index,
      card: cards[index],
      revealed: false,
      hintShown: false,
      pendingRemember: null,
      answered: false,
      submitting: false,
      pictureError: false,
      listenPaused: false,
      playing: false,
      autoNextSec: 0
    });
    this.resetDictation(cards[index] && cards[index].word);
    this.autoPlayCurrent();
    this.bindStoredPictures(cards[index]);
  },

  onDictationLetter(e) {
    if (!this.isDictationMode() || this.data.revealed || this.data.submitting || this.dictationLock) {
      return;
    }
    const key = String((e.currentTarget.dataset.letter || '')).toLowerCase();
    if (!/^[a-z]$/.test(key)) {
      return;
    }
    const slots = this.data.dictationSlots || [];
    const filled = (this.data.dictationFilled || []).slice();
    const idx = dictationUtil.nextLetterSlotIndex(slots, filled);
    if (idx < 0) {
      return;
    }
    if (key !== slots[idx].expected) {
      this.triggerDictationShake();
      return;
    }
    filled[idx] = key;
    this.refreshDictationView(filled);
    if (dictationUtil.isDictationComplete(slots, filled)) {
      this.dictationLock = true;
      this.answerAndReveal(true, { goNextImmediately: true });
    }
  },

  onDictationBackspace() {
    if (!this.isDictationMode() || this.data.revealed || this.data.submitting || this.dictationLock) {
      return;
    }
    const slots = this.data.dictationSlots || [];
    const filled = (this.data.dictationFilled || []).slice();
    for (let i = filled.length - 1; i >= 0; i--) {
      if (!slots[i].fixed && filled[i]) {
        filled[i] = '';
        this.refreshDictationView(filled);
        return;
      }
    }
  },

  onDictationUnknown() {
    if (!this.isDictationMode() || this.answeringLock || this.data.submitting || this.data.revealed) {
      return;
    }
    this.stopAudio();
    this.setData({ playing: false });
    // 揭示单词，3 秒自动下一词（也可点「下一个」）
    this.answerAndReveal(false);
  },

  onDictationReplay() {
    if (!this.isDictationMode() || this.data.submitting || this.data.revealed) {
      return;
    }
    const card = this.data.card;
    if (!card || !card.word) {
      return;
    }
    this.playWord(card.word, this.data.voiceType === 1 ? 1 : 2);
  },

  onGoLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },

  onBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({ url: '/pages/words/index' });
      }
    });
  }
});

