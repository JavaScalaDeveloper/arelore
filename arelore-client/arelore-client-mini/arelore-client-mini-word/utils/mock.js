const BOOKS = [
  {
    id: 'cet4',
    name: '四级核心词汇',
    subtitle: 'CET-4 · 约 1200 词',
    learnTodo: 20,
    learnDone: 8,
    reviewTodo: 15,
    reviewDone: 12
  },
  {
    id: 'cet6',
    name: '六级核心词汇',
    subtitle: 'CET-6 · 约 1500 词',
    learnTodo: 18,
    learnDone: 3,
    reviewTodo: 10,
    reviewDone: 4
  },
  {
    id: 'kaoyan',
    name: '考研英语词汇',
    subtitle: '考研 · 约 5500 词',
    learnTodo: 25,
    learnDone: 0,
    reviewTodo: 8,
    reviewDone: 0
  },
  {
    id: 'ielts',
    name: '雅思核心词汇',
    subtitle: 'IELTS · 约 3500 词',
    learnTodo: 16,
    learnDone: 5,
    reviewTodo: 12,
    reviewDone: 7
  }
];

const DEFAULT_BOOK_ID = 'cet4';

function getBookById(id) {
  return BOOKS.find((item) => item.id === id) || BOOKS[0];
}

module.exports = {
  BOOKS,
  DEFAULT_BOOK_ID,
  getBookById
};
