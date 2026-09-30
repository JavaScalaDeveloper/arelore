const { post, postWithAuth } = require('../../utils/request');
const storage = require('../../utils/storage');

const RATIO_OPTIONS = [
  { label: '1:1', value: '1:1', reviewPart: 1 },
  { label: '1:2', value: '1:2', reviewPart: 2 },
  { label: '1:3', value: '1:3', reviewPart: 3 }
];

const NEW_COUNT_OPTIONS = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];

// 有道 dictvoice type：1 英音，2 美音
const VOICE_OPTIONS = [
  { label: '美音', value: 2 },
  { label: '英音', value: 1 }
];

function calcPlan(wordCount, ratioValue, dailyNewCount) {
  const ratio = RATIO_OPTIONS.find((item) => item.value === ratioValue) || RATIO_OPTIONS[0];
  const dailyReviewCount = dailyNewCount * ratio.reviewPart;
  const total = wordCount || 0;
  const planDays = dailyNewCount > 0 ? Math.ceil(total / dailyNewCount) : 0;
  return { dailyReviewCount, planDays };
}

function parseBookExt(extInfo) {
  if (!extInfo) {
    return { cover: '', originName: '' };
  }
  let raw = extInfo;
  if (typeof extInfo === 'string') {
    try {
      raw = JSON.parse(extInfo);
    } catch (e) {
      return { cover: '', originName: '' };
    }
  }
  if (!raw || typeof raw !== 'object') {
    return { cover: '', originName: '' };
  }
  const cover = typeof raw.cover === 'string' ? raw.cover.trim() : '';
  const origin = raw.bookOrigin;
  let originName = '';
  if (origin && typeof origin === 'object') {
    originName = (origin.originName || '').trim();
  } else if (typeof origin === 'string') {
    originName = origin.trim();
  }
  return { cover, originName };
}

function mapBookItem(item) {
  const parsed = parseBookExt(item.extInfo);
  const cover = (item.cover || parsed.cover || '').trim();
  const originName = (item.originName || parsed.originName || '').trim();
  const parts = [`${item.wordCount || 0} 词`];
  if (originName) {
    parts.push(originName);
  }
  return {
    ...item,
    cover,
    originName,
    subtitle: parts.join(' · ')
  };
}

Page({
  data: {
    // language | category | book | plan
    step: 'language',
    languages: [],
    categories: [],
    books: [],
    language: null,
    category: null,
    selectedBook: null,
    currentCode: '',
    loading: false,
    saving: false,
    ratioOptions: RATIO_OPTIONS,
    ratioIndex: 0,
    newCountOptions: NEW_COUNT_OPTIONS,
    newCountIndex: 0,
    voiceOptions: VOICE_OPTIONS,
    voiceIndex: 0,
    showVoiceOption: false,
    dailyNewCount: 10,
    dailyReviewCount: 10,
    planDays: 0
  },

  onShow() {
    this.setData({ currentCode: storage.getBookId() || '' });
    if (this.data.step === 'plan' && this.data.selectedBook) {
      return;
    }
    this.loadLanguages();
  },

  setNavTitle(title) {
    wx.setNavigationBarTitle({ title });
  },

  refreshPlanDerived() {
    const book = this.data.selectedBook;
    if (!book) {
      return;
    }
    const ratioValue = this.data.ratioOptions[this.data.ratioIndex].value;
    const dailyNewCount = this.data.newCountOptions[this.data.newCountIndex];
    const derived = calcPlan(book.wordCount || 0, ratioValue, dailyNewCount);
    this.setData({
      dailyNewCount,
      dailyReviewCount: derived.dailyReviewCount,
      planDays: derived.planDays
    });
  },

  loadLanguages(options = {}) {
    const autoEnterSingle = options.autoEnterSingle !== false;
    this.setData({
      loading: true,
      step: 'language',
      language: null,
      category: null,
      selectedBook: null,
      categories: [],
      books: []
    });
    this.setNavTitle('选择语种');
    post('/user/word/language/list', {}).then((res) => {
      const languages = res.code === 200 ? (res.data || []) : [];
      this.setData({ languages, loading: false });
      if (autoEnterSingle && languages.length === 1) {
        this.selectLanguage(languages[0]);
      }
    }).catch(() => {
      this.setData({ languages: [], loading: false });
    });
  },

  loadCategories(languageCode) {
    this.setData({ loading: true, step: 'category', category: null, selectedBook: null, books: [] });
    this.setNavTitle('选择分类');
    post('/user/word/category/list', { languageCode }).then((res) => {
      const categories = res.code === 200 ? (res.data || []) : [];
      this.setData({ categories, loading: false });
    }).catch(() => {
      this.setData({ categories: [], loading: false });
    });
  },

  loadBooks(languageCode, categoryCode) {
    this.setData({ loading: true, step: 'book', selectedBook: null });
    this.setNavTitle('选择单词本');
    post('/user/word/book/list', { languageCode, categoryCode }).then((res) => {
      const books = res.code === 200 ? (res.data || []) : [];
      this.setData({
        books: books.map(mapBookItem),
        loading: false
      });
    }).catch(() => {
      this.setData({ books: [], loading: false });
    });
  },

  openPlan(book) {
    const showVoiceOption = !!(this.data.language && this.data.language.code === 'EN');
    this.setData({
      step: 'plan',
      selectedBook: book,
      loading: false,
      ratioIndex: 0,
      newCountIndex: 0,
      voiceIndex: 0,
      showVoiceOption,
      dailyNewCount: 10
    });
    this.setNavTitle('学习计划');
    this.refreshPlanDerived();

    const token = storage.getToken();
    if (!token || !book.code) {
      return;
    }
    postWithAuth('/user/word/plan/get', { bookCode: book.code }).then((res) => {
      if (res.code !== 200 || !res.data) {
        return;
      }
      const plan = res.data;
      const ratioIndex = Math.max(0, RATIO_OPTIONS.findIndex((item) => item.value === plan.newReviewRatio));
      const newCountIndex = Math.max(0, NEW_COUNT_OPTIONS.findIndex((item) => item === plan.dailyNewCount));
      let voiceIndex = 0;
      if (showVoiceOption && plan.extInfo) {
        try {
          const ext = typeof plan.extInfo === 'string' ? JSON.parse(plan.extInfo) : plan.extInfo;
          const voiceType = ext && ext.voiceType;
          const found = VOICE_OPTIONS.findIndex((item) => item.value === voiceType);
          if (found >= 0) {
            voiceIndex = found;
          }
        } catch (e) {}
      }
      this.setData({
        ratioIndex: ratioIndex < 0 ? 0 : ratioIndex,
        newCountIndex: newCountIndex < 0 ? 0 : newCountIndex,
        voiceIndex
      });
      this.refreshPlanDerived();
    }).catch(() => {});
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
    if (step === 'plan') {
      this.loadBooks(language.code, this.data.category.code);
      return;
    }
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
    const book = (this.data.books || []).find((item) => item.code === code);
    if (!book) {
      return;
    }
    this.openPlan(book);
  },

  onRatioChange(e) {
    this.setData({ ratioIndex: Number(e.detail.value) || 0 });
    this.refreshPlanDerived();
  },

  onNewCountChange(e) {
    this.setData({ newCountIndex: Number(e.detail.value) || 0 });
    this.refreshPlanDerived();
  },

  onVoiceChange(e) {
    this.setData({ voiceIndex: Number(e.detail.value) || 0 });
  },

  onConfirmPlan() {
    const book = this.data.selectedBook;
    if (!book || !book.code) {
      return;
    }
    if (this.data.saving) {
      return;
    }
    const ratioValue = this.data.ratioOptions[this.data.ratioIndex].value;
    const dailyNewCount = this.data.newCountOptions[this.data.newCountIndex];
    const payload = {
      bookCode: book.code,
      newReviewRatio: ratioValue,
      dailyNewCount
    };
    if (this.data.showVoiceOption) {
      const voiceType = this.data.voiceOptions[this.data.voiceIndex].value;
      payload.extInfo = JSON.stringify({ voiceType });
    }
    const finishLocal = () => {
      storage.setBookId(book.code);
      this.setData({ currentCode: book.code, saving: false });
      wx.showToast({ title: '计划已保存', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 400);
    };
    const token = storage.getToken();
    if (!token) {
      wx.showToast({ title: '请先登录后再保存计划', icon: 'none' });
      storage.setBookId(book.code);
      this.setData({ currentCode: book.code });
      setTimeout(() => {
        wx.navigateBack();
      }, 500);
      return;
    }
    this.setData({ saving: true });
    postWithAuth('/user/word/plan/confirm', payload).then((res) => {
      if (res.code === 401) {
        this.setData({ saving: false });
        wx.showToast({ title: '请先登录后再保存计划', icon: 'none' });
        return;
      }
      if (res.code !== 200) {
        this.setData({ saving: false });
        wx.showToast({ title: res.message || '保存失败', icon: 'none' });
        return;
      }
      finishLocal();
    }).catch((err) => {
      this.setData({ saving: false });
      wx.showToast({ title: (err && err.errMsg) || '保存失败', icon: 'none' });
    });
  }
});
