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
    user: null
  },

  onShow() {
    this.refreshUser();
  },

  refreshUser() {
    const token = storage.getToken();
    if (!token) {
      this.setData({ user: null });
      return;
    }
    this.setData({ user: displayUser(storage.getUser()) });
    postWithAuth('/user/auth/current', {}).then((resp) => {
      if (resp && resp.code === 200 && resp.data) {
        storage.setUser(resp.data);
        this.setData({ user: displayUser(resp.data) });
        return;
      }
      if (resp && resp.code === 401) {
        storage.clearSession();
        this.setData({ user: null });
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
            this.setData({ user: null });
            wx.showToast({ title: '已退出', icon: 'success' });
          });
      }
    });
  }
});
