const { post, postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');

Page({
  data: {
    books: [],
    currentCode: ''
  },

  onShow() {
    const currentCode = storage.getBookId();
    post('/user/word/book/list', {}).then((res) => {
      const books = res.code === 200 ? (res.data || []) : [];
      this.setData({
        books: books.map((item) => ({
          ...item,
          subtitle: `${item.languageCode || ''} / ${item.categoryCode || ''} · ${item.wordCount || 0} 词`
        })),
        currentCode
      });
    }).catch(() => {
      this.setData({ books: [], currentCode });
    });
  },

  onSelect(e) {
    const { code } = e.currentTarget.dataset;
    const token = storage.getToken();
    const finish = () => {
      storage.setBookId(code);
      this.setData({ currentCode: code });
      wx.showToast({ title: '已切换单词本', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 400);
    };
    if (!token) {
      finish();
      return;
    }
    postWithAuth('/user/word/book/switch', { bookCode: code }).then((res) => {
      if (res.code === 401) {
        wx.showToast({ title: '请先登录后再同步进度', icon: 'none' });
        finish();
        return;
      }
      if (res.code !== 200) {
        wx.showToast({ title: res.message || '切换失败', icon: 'none' });
        return;
      }
      finish();
    }).catch((err) => {
      wx.showToast({ title: (err && err.errMsg) || '切换失败', icon: 'none' });
    });
  }
});
