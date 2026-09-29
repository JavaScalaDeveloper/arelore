const { post, postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');

Page({
  data: {
    // language | category | book
    step: 'language',
    languages: [],
    categories: [],
    books: [],
    language: null,
    category: null,
    currentCode: '',
    loading: false
  },

  onShow() {
    this.setData({ currentCode: storage.getBookId() || '' });
    this.loadLanguages();
  },

  setNavTitle(title) {
    wx.setNavigationBarTitle({ title });
  },

  loadLanguages(options = {}) {
    const autoEnterSingle = options.autoEnterSingle !== false;
    this.setData({ loading: true, step: 'language', language: null, category: null, categories: [], books: [] });
    this.setNavTitle('选择语种');
    post('/user/word/language/list', {}).then((res) => {
      const languages = res.code === 200 ? (res.data || []) : [];
      this.setData({ languages, loading: false });
      // 仅一个语种时自动进入分类（返回上一步时不跳过，避免无法回到语种页）
      if (autoEnterSingle && languages.length === 1) {
        this.selectLanguage(languages[0]);
      }
    }).catch(() => {
      this.setData({ languages: [], loading: false });
    });
  },

  loadCategories(languageCode) {
    this.setData({ loading: true, step: 'category', category: null, books: [] });
    this.setNavTitle('选择分类');
    post('/user/word/category/list', { languageCode }).then((res) => {
      const categories = res.code === 200 ? (res.data || []) : [];
      this.setData({ categories, loading: false });
    }).catch(() => {
      this.setData({ categories: [], loading: false });
    });
  },

  loadBooks(languageCode, categoryCode) {
    this.setData({ loading: true, step: 'book' });
    this.setNavTitle('选择单词本');
    post('/user/word/book/list', { languageCode, categoryCode }).then((res) => {
      const books = res.code === 200 ? (res.data || []) : [];
      this.setData({
        books: books.map((item) => ({
          ...item,
          subtitle: `${item.wordCount || 0} 词`
        })),
        loading: false
      });
    }).catch(() => {
      this.setData({ books: [], loading: false });
    });
  },

  onSelectLanguage(e) {
    const { code } = e.currentTarget.dataset;
    const language = (this.data.languages || []).find((item) => item.code === code);
    if (!language) {
      return;
    }
    this.selectLanguage(language);
  },

  selectLanguage(language) {
    this.setData({ language });
    this.loadCategories(language.code);
  },

  onSelectCategory(e) {
    const { code } = e.currentTarget.dataset;
    const category = (this.data.categories || []).find((item) => item.code === code);
    if (!category || !this.data.language) {
      return;
    }
    this.setData({ category });
    this.loadBooks(this.data.language.code, category.code);
  },

  onBackStep() {
    const { step, language } = this.data;
    if (step === 'book') {
      this.loadCategories(language.code);
      return;
    }
    if (step === 'category') {
      this.loadLanguages({ autoEnterSingle: false });
    }
  },

  onSelectBook(e) {
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
