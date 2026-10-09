const { postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');

function mapCurrent(current) {
  if (!current || !current.bookCode) {
    return null;
  }
  const wordCount = current.wordCount || 0;
  const learnedCount = current.learnedCount || 0;
  return {
    code: current.bookCode,
    name: current.bookName || current.bookCode,
    learnTodo: current.learnTodo || 0,
    learnDone: current.learnDone || 0,
    reviewTodo: current.reviewTodo || 0,
    reviewDone: current.reviewDone || 0,
    wordCount,
    learnedCount,
    planDays: current.planDays || 0,
    remainingDays: current.remainingDays != null ? current.remainingDays : 0,
    progressPercent: current.progressPercent != null
      ? current.progressPercent
      : (wordCount <= 0 ? 0 : Math.min(100, Math.round((learnedCount * 100) / wordCount)))
  };
}

Page({
  data: {
    // loading | needLogin | noBook | ready
    status: 'loading',
    book: null
  },

  onShow() {
    this.refreshHome();
  },

  refreshHome() {
    const token = storage.getToken();
    if (!token) {
      this.setData({ status: 'needLogin', book: null });
      return;
    }

    this.setData({ status: 'loading' });
    postWithAuth('/user/word/book/current', {})
      .then((res) => {
        if (res.code === 401) {
          storage.clearSession();
          this.setData({ status: 'needLogin', book: null });
          return;
        }
        if (res.code !== 200) {
          wx.showToast({ title: res.message || '加载失败', icon: 'none' });
          this.setData({ status: 'noBook', book: null });
          return;
        }
        const book = mapCurrent(res.data);
        if (!book) {
          this.setData({ status: 'noBook', book: null });
          return;
        }
        storage.setBookId(book.code);
        this.setData({ status: 'ready', book });
      })
      .catch((err) => {
        wx.showToast({ title: (err && err.errMsg) || '加载失败', icon: 'none' });
        this.setData({ status: 'noBook', book: null });
      });
  },

  onLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },

  onTapBook() {
    if (this.data.status === 'needLogin') {
      this.promptLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/books/index' });
  },

  onStartStudy() {
    if (!this.ensureReady()) {
      return;
    }
    wx.navigateTo({ url: '/pages/study/index' });
  },

  ensureReady() {
    if (this.data.status === 'needLogin') {
      this.promptLogin();
      return false;
    }
    if (this.data.status !== 'ready' || !this.data.book) {
      wx.showToast({ title: '请先选择单词本', icon: 'none' });
      return false;
    }
    return true;
  },

  promptLogin() {
    wx.showModal({
      title: '需要登录',
      content: '登录后可同步单词本与学习进度',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          this.onLogin();
        }
      }
    });
  }
});
