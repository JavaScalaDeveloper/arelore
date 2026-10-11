const { postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');
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
    playingType: 0,
    playingExampleKey: '',
    elapsedText: '00:00',
    submitting: false,
    playing: false,
    pictureError: false
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

  onLoad() {
    this.initAudio();
    this.loadSession();
  },

  onShow() {
    this.startTimer();
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
    // playing / playingExampleKey 由 tts.playText 的 onStart/onEnd/onError 维护；
    // 例句按词排队时中间会 stop+play，不能在 onStop 里清空状态。
    audio.onError(() => {
      // 排队播词时个别词失败由 tts 继续下一词；整段失败才 toast
      if (!this.ttsSession) {
        this.setData({ playing: false, playingExampleKey: '' });
        wx.showToast({ title: '发音播放失败', icon: 'none' });
      }
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
      this.setData({
        status: 'error',
        errorMessage: (err && err.errMsg) || '加载学习队列失败'
      });
    });
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
    this.stopAudio();
    this.setData({ playingType: type === 1 ? 1 : 2, playingExampleKey: '' });
    this.ttsSession = tts.playText(this.audio, word, type === 1 ? 1 : 2, {
      onStart: () => this.setData({ playing: true }),
      onEnd: () => this.setData({ playing: false, playingExampleKey: '' }),
      onError: () => {
        this.setData({ playing: false, playingExampleKey: '' });
        wx.showToast({ title: '发音播放失败', icon: 'none' });
      }
    });
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

  clearAutoNext() {
    if (this.autoNextTimer) {
      clearTimeout(this.autoNextTimer);
      this.autoNextTimer = null;
    }
  },

  scheduleAutoNext(expectEpoch, expectWordCode) {
    this.clearAutoNext();
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
    }, 1000);
  },

  answerAndReveal(rememberFlag) {
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
      if (rememberFlag) {
        this.scheduleAutoNext(answeredEpoch, answeredWordCode);
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
      wx.showToast({ title: (err && err.errMsg) || '提交失败', icon: 'none' });
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
    this.setData({
      status: 'done',
      cards: [],
      index: 0,
      card: null,
      revealed: false,
      hintShown: false,
      pendingRemember: null,
      answered: false,
      submitting: false
    });
  },

  goNext() {
    this.clearAutoNext();
    this.stopAudio();
    this.answeringLock = false;
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
      pictureError: false
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
