const storage = require('../../utils/storage');
const { postWithAuth } = require('../../utils/request');

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
    consecutiveStudyDays: 0
  },

  onShow() {
    this.refreshUser();
  },

  refreshUser() {
    const token = storage.getToken();
    if (!token) {
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
        storage.setUser(resp.data);
        this.setData({ user: displayUser(resp.data) });
        this.loadStudyStats();
        return;
      }
      if (resp && resp.code === 401) {
        storage.clearSession();
        this.setData({
          user: null,
          totalStudyDays: 0,
          consecutiveStudyDays: 0
        });
      }
    }).catch(() => {});
  },

  loadStudyStats() {
    postWithAuth('/user/word/study/stats', {}).then((resp) => {
      if (resp && resp.code === 200 && resp.data) {
        this.setData({
          totalStudyDays: resp.data.totalStudyDays || 0,
          consecutiveStudyDays: resp.data.consecutiveStudyDays || 0
        });
      }
    }).catch(() => {});
  },

  onLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
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
