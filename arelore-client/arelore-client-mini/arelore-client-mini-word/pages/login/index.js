const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const { post, shortErrorMessage } = require('../../utils/request');

Page({
  data: {
    loading: false
  },

  onShow() {
    if (storage.getToken()) {
      wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/words/index' }) });
    }
  },

  /** 静默微信登录：仅 wx.login + code，不强制头像/昵称/手机号授权 */
  onWechatQuickLogin() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          this.failLogin('微信登录失败，请重试');
          return;
        }
        this.submitQuickLogin(loginRes.code);
      },
      fail: (err) => {
        this.failLogin((err && err.errMsg) || '微信登录失败，请重试');
      }
    });
  },

  submitQuickLogin(code) {
    // 头像昵称改为登录后自愿完善；此处用默认值，避免 getUserProfile 触发审核拦截
    const mockOpenid = storage.getOrCreateMockOpenid();
    post('/user/auth/wechat/quick', {
      code,
      userInfo: {
        openid: mockOpenid,
        nickname: '微信用户',
        avatar: '',
        gender: 0,
        country: '',
        province: '',
        city: ''
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
              content: '后端已返回成功，但本地未读取到 token。请检查返回字段是否为 data.token。',
              showCancel: false
            });
            this.setData({ loading: false });
            return;
          }
          preference.pushLocalPreference().catch(() => {});
          wx.showToast({ title: '登录成功', icon: 'success' });
          this.setData({ loading: false });
          setTimeout(() => {
            wx.navigateBack({
              fail: () => wx.switchTab({ url: '/pages/words/index' })
            });
          }, 400);
          return;
        }
        this.failLogin(resp.message || '登录失败');
      })
      .catch((e) => {
        this.failLogin(shortErrorMessage(e, '登录失败，请稍后重试'), e);
      });
  },

  onSkip() {
    wx.navigateBack({
      fail: () => wx.switchTab({ url: '/pages/words/index' })
    });
  },

  failLogin(errMsg, err) {
    this.setData({ loading: false });
    const title = err && err.isNetworkError ? '网络不通' : '登录失败';
    const content = errMsg || '请稍后重试';
    wx.showModal({
      title,
      content,
      showCancel: false
    });
  }
});
