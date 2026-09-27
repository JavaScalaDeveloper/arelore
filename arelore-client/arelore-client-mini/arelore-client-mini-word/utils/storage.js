const KEY_BOOK = 'word_current_book_id';
const KEY_USER = 'word_current_user';
const KEY_TOKEN = 'token';
const KEY_OPENID = 'miniOpenid';

function getBookId() {
  return wx.getStorageSync(KEY_BOOK) || '';
}

function setBookId(id) {
  wx.setStorageSync(KEY_BOOK, id);
}

function getToken() {
  return wx.getStorageSync(KEY_TOKEN) || '';
}

function setToken(token) {
  wx.setStorageSync(KEY_TOKEN, token);
}

function getUser() {
  return wx.getStorageSync(KEY_USER) || null;
}

function setUser(user) {
  wx.setStorageSync(KEY_USER, user);
}

function getOrCreateMockOpenid() {
  const cached = wx.getStorageSync(KEY_OPENID);
  if (cached) return cached;
  const rnd = Math.random().toString(36).slice(2, 10);
  const value = `mini_dev_${Date.now()}_${rnd}`;
  wx.setStorageSync(KEY_OPENID, value);
  return value;
}

function clearSession() {
  wx.removeStorageSync(KEY_USER);
  wx.removeStorageSync(KEY_TOKEN);
}

module.exports = {
  getBookId,
  setBookId,
  getToken,
  setToken,
  getUser,
  setUser,
  getOrCreateMockOpenid,
  clearSession
};
