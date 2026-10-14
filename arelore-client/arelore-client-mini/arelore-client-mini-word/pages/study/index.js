const { postWithAuth, shortErrorMessage } = require('../../utils/request');
const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const tts = require('../../utils/tts');

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
    bookCode: '',
    cards: [],
    index: 0,
    card: null,
    revealed: false,
    hintShown: false,
    pendingRemember: null,
    learnTotal: 0,
    reviewTotal: 0,
    learnDone: 0,
    reviewDone: 0,
    voiceType: 2,
    studyMode: storage.STUDY_MODE_SEE_MEANING,
    isListenMode: false,
    spellSpeed: storage.DEFAULT_SPELL_SPEED,
    wordRepeatCount: storage.DEFAULT_WORD_REPEAT_COUNT,
    playingType: 0,
    playingExampleKey: '',
    elapsedText: '00:00',
    submitting: false,
    playing: false,
    pictureError: false,
    /** 听词暂停：不自动切下一词 */
    listenPaused: false
  },

  timer: null,
  autoNextTimer: null,
  /** 同步锁：防止连点「认识/不认识」在 setData 生效前重复提交 */
  answeringLock: false,
  /** 当前卡片世代，用于作废过期的自动下一词定时器 */
  cardEpoch: 0,
  startedAt: 0,
  audio: null,
  ttsSession: null,
  /** 听词 failsafe 切词定时器 */
  listenAdvanceTimer: null,
  /** 暂停期间整段已播完，继续时立刻切下一词 */
  listenHoldAdvance: false,

  onLoad() {
    this.initAudio();
    this.refreshPreference();
    this.loadSession();
  },

  onShow() {
    this.refreshPreference();
    this.startTimer();
  },

  applyPreference(pref) {
    const p = pref || storage.getPreference();
    const isListenMode = p.studyMode === storage.STUDY_MODE_LISTEN_SPELL;
    this.setData({
      studyMode: p.studyMode,
      isListenMode,
      spellSpeed: p.spellSpeed,
      wordRepeatCount: p.wordRepeatCount
    });
  },

  refreshPreference() {
    this.applyPreference(storage.getPreference());
    preference.syncPreference().then((pref) => {
      this.applyPreference(pref);
    }).catch(() => {});
  },

  onHide() {
    this.stopTimer();
    this.clearAutoNext();
    this.stopAudio();
  },

  onUnload() {
    this.stopTimer();
    this.clearAutoNext();
    this.destroyAudio();
  },

  initAudio() {
    const audio = wx.createInnerAudioContext();
    audio.obeyMuteSwitch = false;
    // 听词队列的错误由 tts.playSrcOnce 自行消化；这里不再挂全局 onError，
    // 避免与逐段 offError 冲突，以及 EncodingError 打断链路。
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
      try {
        this.audio.stop();
      } catch (e) {}
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
  armListenAdvanceFailsafe(expectEpoch, expectWordCode, word, meaning) {
    this.clearListenAdvanceTimer();
    if (this.data.listenPaused) {
      return;
    }
    const waitMs = Math.max(
      10000,
      tts.estimateListenDurationMs(word, {
        wordRepeatCount: this.data.wordRepeatCount,
        spellSpeed: this.data.spellSpeed,
        meaning: meaning || ''
      })
    );
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
      this.setData({ elapsedText: formatElapsed(Date.now() - this.startedAt) });
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
      this.setData({ status: 'error', errorMessage: '请先登录后再学习' });
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
        pictureError: false
      });
      this.startTimer();
      this.autoPlayCurrent();
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
      // failsafe 须覆盖「中文释义播完」；正常路径仍以 onEnd（等中文结束）切词
      this.armListenAdvanceFailsafe(playEpoch, playWordCode, word, meaning);
      this.ttsSession = tts.playWordThenSpell(this.audio, word, voiceType, {
        onStart: () => this.setData({ playing: true }),
        onEnd: () => {
          this.setData({ playing: false, playingExampleKey: '' });
          triggerListenAdvance();
        },
        wordRepeatCount: this.data.wordRepeatCount,
        spellSpeed: this.data.spellSpeed,
        meaning
      });
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
    this.setData({ pictureError: true });
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
    if (this.autoNextTimer) {
      clearTimeout(this.autoNextTimer);
      this.autoNextTimer = null;
    }
  },

  scheduleAutoNext(expectEpoch, expectWordCode, delayMs) {
    this.clearAutoNext();
    const delay = delayMs == null ? 1000 : delayMs;
    this.autoNextTimer = setTimeout(() => {
      this.autoNextTimer = null;
      // 已切到别的词 / 用户已手动下一词：丢弃过期定时器
      if (this.cardEpoch !== expectEpoch) {
        return;
      }
      const card = this.data.card;
      if (!card || card.wordCode !== expectWordCode || !this.data.revealed) {
        return;
      }
      this.goNext();
    }, delay);
  },

  answerAndReveal(rememberFlag, options) {
    const opts = options || {};
    const card = this.data.card;
    if (!card || this.answeringLock || this.data.submitting || this.data.revealed) {
      return;
    }
    this.answeringLock = true;
    this.clearAutoNext();
    const answeredWordCode = card.wordCode;
    const answeredEpoch = this.cardEpoch;
    this.setData({
      submitting: true,
      revealed: true,
      hintShown: false,
      pendingRemember: !!rememberFlag,
      answered: true,
      pictureError: false
    });
    postWithAuth('/user/word/study/answer', {
      bookCode: this.data.bookCode,
      wordCode: card.wordCode,
      mode: card.mode,
      rememberFlag
    }).then((res) => {
      this.answeringLock = false;
      this.setData({ submitting: false });
      // 响应回来时已不在该词：忽略（防重复 answer 的迟到回调）
      if (this.cardEpoch !== answeredEpoch
        || !this.data.card
        || this.data.card.wordCode !== answeredWordCode) {
        return;
      }
      if (res.code === 401) {
        storage.clearSession();
        wx.showToast({ title: '请重新登录', icon: 'none' });
        return;
      }
      if (res.code !== 200) {
        // 提交失败：收回揭示态，允许重试
        this.setData({
          revealed: false,
          answered: false,
          pendingRemember: null
        });
        wx.showToast({ title: res.message || '提交失败', icon: 'none' });
        return;
      }
      const patch = {};
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
      // 听词：认识/稍后复习 提交后都立刻切下一词
      if (opts.goNextImmediately || (rememberFlag && this.isListenMode())) {
        this.goNext();
        return;
      }
      if (rememberFlag) {
        this.scheduleAutoNext(answeredEpoch, answeredWordCode, 1000);
      }
    }).catch((err) => {
      this.answeringLock = false;
      if (this.cardEpoch === answeredEpoch
        && this.data.card
        && this.data.card.wordCode === answeredWordCode) {
        this.setData({
          submitting: false,
          revealed: false,
          answered: false,
          pendingRemember: null
        });
      } else {
        this.setData({ submitting: false });
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
      playing: false
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
      playing: false
    });
    this.autoPlayCurrent();
  },

  onBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({ url: '/pages/words/index' });
      }
    });
  }
});
