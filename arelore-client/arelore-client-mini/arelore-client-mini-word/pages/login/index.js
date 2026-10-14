const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const { post, shortErrorMessage } = require('../../utils/request');

Page({
  data: {
    loading: false,
    mockOpenid: ''
  },

  onShow() {
    if (storage.getToken()) {
      wx.navigateBack();
      return;
    }
    this.setData({ mockOpenid: storage.getOrCreateMockOpenid() });
  },

  onWechatQuickLogin() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (profileRes) => {
        const userInfo = profileRes.userInfo || {};
        wx.login({
          success: (loginRes) => {
            if (!loginRes.code) {
              this.failLogin('微信登录失败，请重试');
              return;
            }
            this.submitQuickLogin(loginRes.code, userInfo);
          },
          fail: (err) => {
            this.failLogin((err && err.errMsg) || '微信登录失败，请重试');
          }
        });
      },
      fail: (err) => {
        this.failLogin((err && err.errMsg) || '需要授权后才能登录');
      }
    });
  },

  submitQuickLogin(code, userInfo) {
    const mockOpenid = storage.getOrCreateMockOpenid();
    post('/user/auth/wechat/quick', {
      code,
      userInfo: {
        openid: mockOpenid,
        nickname: userInfo.nickName || '微信用户',
        avatar: userInfo.avatarUrl || '',
        gender: userInfo.gender || 0,
        country: userInfo.country || '',
        province: userInfo.province || '',
        city: userInfo.city || ''
      }
    })
      .then((resp) => {
        if (resp.code === 200 && resp.data) {
          const nextToken = resp.data.token || (resp.data.data && resp.data.data.token) || '';
          const nextUser = resp.data.user || (resp.data.data && resp.data.data.user) || {};
          storage.setToken(nextToken);
          storage.setUser(nextUser);
          if (!storage.getToken()) {
            wx.showModal({
              title: '登录态未写入',
              content:
                '后端已返回成功，但本地未读取到 token。请检查返回字段是否为 data.token。',
              showCancel: false
            });
            this.setData({ loading: false });
            return;
          }
          // 登录后把本机偏好推到服务端，避免未登录时改的设置被覆盖
          preference.pushLocalPreference().catch(() => {});
          wx.showToast({ title: '登录成功', icon: 'success' });
          this.setData({ loading: false });
          setTimeout(() => {
            wx.navigateBack();
          }, 400);
          return;
        }
        this.failLogin(resp.message || '登录失败');
      })
      .catch((e) => {
        this.failLogin(shortErrorMessage(e, '登录失败，请稍后重试'), e);
      });
  },

  failLogin(errMsg, err) {
    this.setData({ loading: false });
    const title = err && err.isNetworkError ? '网络不通' : '登录失败';
    const content = errMsg || '请稍后重试';
    wx.showToast({ title: content.slice(0, 20), icon: 'none' });
    wx.showModal({
      title,
      content,
      showCancel: false
    });
  }
});
