const { postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');

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
    elapsedText: '00:00',
    submitting: false,
    playing: false
  },

  timer: null,
  autoNextTimer: null,
  startedAt: 0,
  audio: null,

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
    audio.onPlay(() => this.setData({ playing: true }));
    audio.onEnded(() => this.setData({ playing: false }));
    audio.onStop(() => this.setData({ playing: false }));
    audio.onError(() => {
      this.setData({ playing: false });
      wx.showToast({ title: '发音播放失败', icon: 'none' });
    });
    this.audio = audio;
  },

  destroyAudio() {
    if (this.audio) {
      this.audio.destroy();
      this.audio = null;
    }
  },

  stopAudio() {
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
        learnTotal,
        reviewTotal,
        learnDone,
        reviewDone,
        voiceType,
        elapsedText: '00:00'
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
    const voiceType = type === 1 ? 1 : 2;
    const src = `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(word)}&type=${voiceType}`;
    this.setData({ playingType: voiceType });
    try {
      this.audio.stop();
    } catch (e) {}
    this.audio.src = src;
    this.audio.play();
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

  answerAndReveal(rememberFlag) {
    const card = this.data.card;
    if (!card || this.data.submitting || this.data.revealed) {
      return;
    }
    this.clearAutoNext();
    this.setData({
      submitting: true,
      revealed: true,
      hintShown: false,
      pendingRemember: !!rememberFlag,
      answered: true
    });
    postWithAuth('/user/word/study/answer', {
      bookCode: this.data.bookCode,
      wordCode: card.wordCode,
      mode: card.mode,
      rememberFlag
    }).then((res) => {
      this.setData({ submitting: false });
      if (res.code === 401) {
        storage.clearSession();
        wx.showToast({ title: '请重新登录', icon: 'none' });
        return;
      }
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '提交失败', icon: 'none' });
        return;
      }
      const patch = {};
      if (card.mode === 'review') {
        patch.reviewDone = this.data.reviewDone + 1;
      } else {
        patch.learnDone = this.data.learnDone + 1;
      }
      this.setData(patch);
      if (rememberFlag) {
        this.autoNextTimer = setTimeout(() => {
          this.goNext();
        }, 1000);
      }
    }).catch((err) => {
      this.setData({ submitting: false });
      wx.showToast({ title: (err && err.errMsg) || '提交失败', icon: 'none' });
    });
  },

  onNextAfterReveal() {
    this.clearAutoNext();
    this.goNext();
  },

  goNext() {
    this.clearAutoNext();
    this.stopAudio();
    const nextIndex = this.data.index + 1;
    const cards = this.data.cards || [];
    if (nextIndex >= cards.length) {
      this.stopTimer();
      this.setData({
        status: 'done',
        card: null,
        revealed: false,
        hintShown: false,
        pendingRemember: null,
        answered: false,
        elapsedText: formatElapsed(Date.now() - this.startedAt)
      });
      return;
    }
    this.setData({
      index: nextIndex,
      card: cards[nextIndex],
      revealed: false,
      hintShown: false,
      pendingRemember: null,
      answered: false,
      submitting: false
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
