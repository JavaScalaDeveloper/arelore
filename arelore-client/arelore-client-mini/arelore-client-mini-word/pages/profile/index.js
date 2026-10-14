const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const { postWithAuth, shortErrorMessage } = require('../../utils/request');
const { getApiBaseUrl, getActiveEnv } = require('../../utils/config');

function displayUser(raw) {
  if (!raw) return null;
  return {
    nickName: raw.nickname || raw.nickName || raw.username || '微信用户',
    avatarUrl: raw.avatar || raw.avatarUrl || ''
  };
}

Page({
  data: {
    user: null,
    totalStudyDays: 0,
    consecutiveStudyDays: 0,
    studyModeLabel: preference.modeLabel(storage.STUDY_MODE_SEE_MEANING),
    networkError: false,
    networkErrorMessage: '',
    apiBaseUrl: '',
    apiEnv: ''
  },

  onShow() {
    this.refreshPreferenceSummary();
    this.refreshUser();
  },

  clearNetworkError() {
    this.setData({
      networkError: false,
      networkErrorMessage: '',
      apiBaseUrl: '',
      apiEnv: ''
    });
  },

  showNetworkError(err) {
    this.setData({
      networkError: true,
      networkErrorMessage: shortErrorMessage(err, '网络不通，请稍后重试'),
      apiBaseUrl: getApiBaseUrl(),
      apiEnv: getActiveEnv()
    });
  },

  onRetryNetwork() {
    this.clearNetworkError();
    this.refreshPreferenceSummary();
    this.refreshUser();
  },

  refreshPreferenceSummary() {
    const local = storage.getPreference();
    this.setData({ studyModeLabel: preference.modeLabel(local.studyMode) });
    preference.syncPreference().then((pref) => {
      this.setData({ studyModeLabel: preference.modeLabel(pref.studyMode) });
    }).catch(() => {});
  },

  refreshUser() {
    const token = storage.getToken();
    if (!token) {
      this.clearNetworkError();
      this.setData({
        user: null,
        totalStudyDays: 0,
        consecutiveStudyDays: 0
      });
      return;
    }
    this.setData({ user: displayUser(storage.getUser()) });
    postWithAuth('/user/auth/current', {}).then((resp) => {
      if (resp && resp.code === 200 && resp.data) {
        this.clearNetworkError();
        storage.setUser(resp.data);
        this.setData({ user: displayUser(resp.data) });
        this.loadStudyStats();
        return;
      }
      if (resp && resp.code === 401) {
        storage.clearSession();
        this.clearNetworkError();
        this.setData({
          user: null,
          totalStudyDays: 0,
          consecutiveStudyDays: 0
        });
      }
    }).catch((err) => {
      this.showNetworkError(err);
    });
  },

  loadStudyStats() {
    postWithAuth('/user/word/study/stats', {}).then((resp) => {
      if (resp && resp.code === 200 && resp.data) {
        this.setData({
          totalStudyDays: resp.data.totalStudyDays || 0,
          consecutiveStudyDays: resp.data.consecutiveStudyDays || 0
        });
      }
    }).catch((err) => {
      this.showNetworkError(err);
    });
  },

  onLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },

  onSettings() {
    wx.navigateTo({ url: '/pages/settings/index' });
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '退出后需重新微信授权登录',
      success: (res) => {
        if (!res.confirm) return;
        postWithAuth('/user/auth/logout', {})
          .catch(() => {})
          .then(() => {
            storage.clearSession();
            this.setData({
              user: null,
              totalStudyDays: 0,
              consecutiveStudyDays: 0
            });
            wx.showToast({ title: '已退出', icon: 'success' });
          });
      }
    });
  }
});
