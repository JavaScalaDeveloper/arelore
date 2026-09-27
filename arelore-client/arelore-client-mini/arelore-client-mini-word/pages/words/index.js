const { post, postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');

function mapCurrent(current, fallbackBook) {
  if (current && current.bookCode) {
    return {
      code: current.bookCode,
      name: current.bookName || current.bookCode,
      learnTodo: current.learnTodo || 0,
      learnDone: current.learnDone || 0,
      reviewTodo: current.reviewTodo || 0,
      reviewDone: current.reviewDone || 0
    };
  }
  if (fallbackBook) {
    return {
      code: fallbackBook.code,
      name: fallbackBook.name,
      learnTodo: fallbackBook.wordCount || 0,
      learnDone: 0,
      reviewTodo: 0,
      reviewDone: 0
    };
  }
  return null;
}

Page({
  data: {
    book: null
  },

  onShow() {
    this.refreshBook();
  },

  refreshBook() {
    const token = storage.getToken();
    const localCode = storage.getBookId();
    post('/user/word/book/list', {}).then((listRes) => {
      const books = listRes.code === 200 ? (listRes.data || []) : [];
      const runCurrent = token
        ? postWithAuth('/user/word/book/current', {})
        : Promise.resolve({ code: 401, data: null });
      return runCurrent.then((curRes) => {
        let current = curRes && curRes.code === 200 ? curRes.data : null;
        let fallback = books.find((item) => item.code === localCode) || books[0] || null;
        const book = mapCurrent(current, fallback);
        if (book && book.code) {
          storage.setBookId(book.code);
        }
        this.setData({ book });
      });
    }).catch(() => {
      this.setData({ book: null });
    });
  },

  onTapBook() {
    wx.navigateTo({ url: '/pages/books/index' });
  },

  onTapLearn() {
    wx.showToast({ title: '新学流程稍后接入', icon: 'none' });
  },

  onTapReview() {
    wx.showToast({ title: '复习流程稍后接入', icon: 'none' });
  }
});
