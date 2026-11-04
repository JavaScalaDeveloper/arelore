const letterVoices = require('./letterVoices');

const KEY_BOOK = 'word_current_book_id';
/** 游客态选中的词书摘要（未登录也可在首页展示） */
const KEY_GUEST_BOOK = 'word_guest_book';
const KEY_USER = 'word_current_user';
const KEY_TOKEN = 'token';
const KEY_OPENID = 'miniOpenid';
const KEY_PREFERENCE = 'word_preference';
/** @deprecated 已迁移到 KEY_PREFERENCE */
const KEY_STUDY_MODE_LEGACY = 'word_study_mode';

/** 看词识意（默认） */
const STUDY_MODE_SEE_MEANING = 'see_meaning';
/** 听词（摸鱼） */
const STUDY_MODE_LISTEN_SPELL = 'listen_spell';
/** 听写 */
const STUDY_MODE_DICTATION = 'dictation';
/** 看词选图（暂未开放） */
const STUDY_MODE_SEE_PICK_IMAGE = 'see_pick_image';

const DEFAULT_SPELL_SPEED = 5;
const DEFAULT_WORD_REPEAT_COUNT = 1;
const DEFAULT_LETTER_VOICE = letterVoices.LETTER_VOICE_RANDOM;
/** 播放音量 0–100，仅本机（wx storage），不同步服务端 */
const DEFAULT_PLAYBACK_VOLUME = 100;

function clampInt(n, min, max, fallback) {
  const v = Number(n);
  if (!Number.isFinite(v)) {
    return fallback;
  }
  return Math.max(min, Math.min(max, Math.round(v)));
}

function normalizePreference(raw) {
  const src = raw && typeof raw === 'object' ? raw : {};
  let studyMode = src.studyMode;
  if (
    studyMode !== STUDY_MODE_LISTEN_SPELL
    && studyMode !== STUDY_MODE_DICTATION
    && studyMode !== STUDY_MODE_SEE_PICK_IMAGE
  ) {
    studyMode = STUDY_MODE_SEE_MEANING;
  }
  return {
    studyMode,
    spellSpeed: clampInt(src.spellSpeed, 0, 10, DEFAULT_SPELL_SPEED),
    wordRepeatCount: clampInt(src.wordRepeatCount, 1, 3, DEFAULT_WORD_REPEAT_COUNT),
    /** 拼读音色：random | jenny | guy | sonia | ryan（仅本机，不同步服务端） */
    letterVoice: letterVoices.normalizeLetterVoice(
      src.letterVoice != null ? src.letterVoice : DEFAULT_LETTER_VOICE
    ),
    /** 播放音量 0–100（仅本机，不同步服务端；下次启动仍生效） */
    playbackVolume: clampInt(src.playbackVolume, 0, 100, DEFAULT_PLAYBACK_VOLUME)
  };
}

function getBookId() {
  return wx.getStorageSync(KEY_BOOK) || '';
}

function setBookId(id) {
  wx.setStorageSync(KEY_BOOK, id);
}

function getGuestBook() {
  const raw = wx.getStorageSync(KEY_GUEST_BOOK);
  if (!raw || typeof raw !== 'object' || !raw.code) {
    return null;
  }
  return {
    code: String(raw.code),
    name: String(raw.name || raw.code),
    wordCount: Number(raw.wordCount) || 0
  };
}

function setGuestBook(book) {
  if (!book || !book.code) {
    wx.removeStorageSync(KEY_GUEST_BOOK);
    return null;
  }
  const next = {
    code: String(book.code),
    name: String(book.name || book.code),
    wordCount: Number(book.wordCount) || 0
  };
  wx.setStorageSync(KEY_GUEST_BOOK, next);
  setBookId(next.code);
  return next;
}

function clearGuestBook() {
  wx.removeStorageSync(KEY_GUEST_BOOK);
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
  // 保留游客词书选择，退出后仍可浏览
}

function getPreference() {
  const cached = wx.getStorageSync(KEY_PREFERENCE);
  if (cached && typeof cached === 'object') {
    return normalizePreference(cached);
  }
  // 兼容旧版只存 studyMode 字符串
  const legacyMode = wx.getStorageSync(KEY_STUDY_MODE_LEGACY);
  if (legacyMode) {
    const migrated = normalizePreference({ studyMode: legacyMode });
    wx.setStorageSync(KEY_PREFERENCE, migrated);
    try {
      wx.removeStorageSync(KEY_STUDY_MODE_LEGACY);
    } catch (e) {}
    return migrated;
  }
  return normalizePreference({});
}

function setPreference(partial) {
  const next = normalizePreference({
    ...getPreference(),
    ...(partial && typeof partial === 'object' ? partial : {})
  });
  wx.setStorageSync(KEY_PREFERENCE, next);
  return next;
}

function getStudyMode() {
  return getPreference().studyMode;
}

function setStudyMode(mode) {
  return setPreference({ studyMode: mode }).studyMode;
}

module.exports = {
  STUDY_MODE_SEE_MEANING,
  STUDY_MODE_LISTEN_SPELL,
  STUDY_MODE_DICTATION,
  STUDY_MODE_SEE_PICK_IMAGE,
  DEFAULT_SPELL_SPEED,
  DEFAULT_WORD_REPEAT_COUNT,
  DEFAULT_LETTER_VOICE,
  DEFAULT_PLAYBACK_VOLUME,
  getBookId,
  setBookId,
  getGuestBook,
  setGuestBook,
  clearGuestBook,
  getToken,
  setToken,
  getUser,
  setUser,
  getOrCreateMockOpenid,
  clearSession,
  getPreference,
  setPreference,
  getStudyMode,
  setStudyMode
};
