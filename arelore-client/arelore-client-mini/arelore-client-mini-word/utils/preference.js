const storage = require('./storage');
const { postWithAuth } = require('./request');

const MODE_LABELS = {
  [storage.STUDY_MODE_LISTEN_SPELL]: '听词（摸鱼）',
  [storage.STUDY_MODE_SEE_MEANING]: '看词识意',
  [storage.STUDY_MODE_SEE_PICK_IMAGE]: '看词选图'
};

function modeLabel(mode) {
  return MODE_LABELS[mode] || MODE_LABELS[storage.STUDY_MODE_SEE_MEANING];
}

/**
 * 拉取服务端偏好并写入本地；未登录则只返回本地。
 * @returns {Promise<object>} normalize 后的 preference
 */
function syncPreference() {
  const local = storage.getPreference();
  const token = storage.getToken();
  if (!token) {
    return Promise.resolve(local);
  }
  return postWithAuth('/user/word/preference/get', {}).then((res) => {
    if (res.code === 401) {
      storage.clearSession();
      return local;
    }
    if (res.code !== 200 || !res.data) {
      return local;
    }
    // letterVoice 仅本机，同步云端偏好时保留本地值
    return storage.setPreference({
      studyMode: res.data.studyMode,
      spellSpeed: res.data.spellSpeed,
      wordRepeatCount: res.data.wordRepeatCount,
      letterVoice: local.letterVoice
    });
  }).catch(() => local);
}

/**
 * 把本地偏好推到服务端（登录后保留未登录时改过的设置）。
 * @returns {Promise<object>}
 */
function pushLocalPreference() {
  const local = storage.getPreference();
  const token = storage.getToken();
  if (!token) {
    return Promise.resolve(local);
  }
  return postWithAuth('/user/word/preference/update', {
    studyMode: local.studyMode,
    spellSpeed: local.spellSpeed,
    wordRepeatCount: local.wordRepeatCount
  }).then((res) => {
    if (res.code === 401) {
      storage.clearSession();
      return local;
    }
    if (res.code !== 200 || !res.data) {
      return local;
    }
    return storage.setPreference({
      studyMode: res.data.studyMode,
      spellSpeed: res.data.spellSpeed,
      wordRepeatCount: res.data.wordRepeatCount,
      letterVoice: local.letterVoice
    });
  }).catch(() => local);
}

module.exports = {
  MODE_LABELS,
  modeLabel,
  syncPreference,
  pushLocalPreference
};
