const { BOOKS, DEFAULT_BOOK_ID } = require('./utils/mock');
const storage = require('./utils/storage');

App({
  onLaunch() {
    const bookId = storage.getBookId();
    const valid = BOOKS.some((item) => item.id === bookId);
    if (!valid) {
      storage.setBookId(DEFAULT_BOOK_ID);
    }
  }
});
