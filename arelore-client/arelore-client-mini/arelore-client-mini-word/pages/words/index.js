const { postWithAuth, shortErrorMessage } = require('../../utils/request');
const { getApiBaseUrl, getActiveEnv } = require('../../utils/config');
const storage = require('../../utils/storage');
const preference = require('../../utils/preference');

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
    // loading | guest | noBook | networkError | ready
    status: 'loading',
    book: null,
    guestBook: null,
    studyModeLabel: preference.modeLabel(storage.STUDY_MODE_SEE_MEANING),
    isListenMode: false,
    networkErrorMessage: '',
    apiBaseUrl: '',
    apiEnv: ''
  },

  onShow() {
    this.refreshPreferenceChip();
    this.refreshHome();
  },

  refreshPreferenceChip() {
    const local = storage.getPreference();
    this.setData({
      studyModeLabel: preference.modeLabel(local.studyMode),
      isListenMode: local.studyMode === storage.STUDY_MODE_LISTEN_SPELL
    });
    preference.syncPreference().then((pref) => {
      this.setData({
        studyModeLabel: preference.modeLabel(pref.studyMode),
        isListenMode: pref.studyMode === storage.STUDY_MODE_LISTEN_SPELL
      });
    }).catch(() => {});
  },

  /** 游客首页：可浏览词书，不强制登录 */
  showGuestHome() {
    const guestBook = storage.getGuestBook();
    this.setData({
      status: 'guest',
      book: null,
      guestBook,
      networkErrorMessage: ''
    });
  },

  refreshHome() {
    const token = storage.getToken();
    if (!token) {
      this.showGuestHome();
      return;
    }

    this.setData({
      status: 'loading',
      networkErrorMessage: '',
      apiBaseUrl: getApiBaseUrl(),
      apiEnv: getActiveEnv()
    });
    postWithAuth('/user/word/book/current', {})
      .then((res) => {
        if (res.code === 401) {
          storage.clearSession();
          this.showGuestHome();
          return;
        }
        if (res.code === -1001 || res.isNetworkError) {
          this.setData({
            status: 'networkError',
            book: null,
            networkErrorMessage: res.message || '网络不通',
            apiBaseUrl: getApiBaseUrl(),
            apiEnv: getActiveEnv()
          });
          return;
        }
        if (res.code !== 200) {
          wx.showToast({ title: res.message || '加载失败', icon: 'none' });
          this.setData({ status: 'noBook', book: null, guestBook: null });
          return;
        }
        const book = mapCurrent(res.data);
        if (!book) {
          // 已登录但未选词书：仍可进词书列表
          this.setData({
            status: 'noBook',
            book: null,
            guestBook: storage.getGuestBook()
          });
          return;
        }
        storage.setBookId(book.code);
        storage.clearGuestBook();
        this.setData({ status: 'ready', book, guestBook: null });
      })
      .catch((err) => {
        this.setData({
          status: 'networkError',
          book: null,
          networkErrorMessage: shortErrorMessage(err, '网络不通，请稍后重试'),
          apiBaseUrl: getApiBaseUrl(),
          apiEnv: getActiveEnv()
        });
      });
  },

  onRetryHome() {
    this.refreshHome();
  },

  onLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },

  onTapBook() {
    wx.navigateTo({ url: '/pages/books/index' });
  },

  onStartStudy() {
    if (!storage.getToken()) {
      this.promptLogin('登录后可保存进度并开始学习');
      return;
    }
    if (!this.ensureReady()) {
      return;
    }
    wx.navigateTo({ url: '/pages/study/index' });
  },

  onOpenSettings() {
    if (!storage.getToken()) {
      this.promptLogin('设置背词模式需要登录后使用');
      return;
    }
    wx.navigateTo({ url: '/pages/settings/index' });
  },

  ensureReady() {
    if (!storage.getToken()) {
      this.promptLogin('登录后可同步单词本与学习进度');
      return false;
    }
    if (this.data.status !== 'ready' || !this.data.book) {
      wx.showToast({ title: '请先选择单词本', icon: 'none' });
      return false;
    }
    return true;
  },

  promptLogin(content) {
    wx.showModal({
      title: '登录后继续',
      content: content || '登录后可同步单词本与学习进度',
      confirmText: '去登录',
      cancelText: '再看看',
      success: (res) => {
        if (res.confirm) {
          this.onLogin();
        }
      }
    });
  }
});
