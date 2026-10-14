const { BOOKS, DEFAULT_BOOK_ID } = require('./utils/mock');
const storage = require('./utils/storage');
const preference = require('./utils/preference');

App({
  onLaunch() {
    const bookId = storage.getBookId();
    const valid = BOOKS.some((item) => item.id === bookId);
    if (!valid) {
      storage.setBookId(DEFAULT_BOOK_ID);
    }
    // 登录态下预同步背词偏好，学习页/设置页可直接读本地
    preference.syncPreference().catch(() => {});
  }
});
