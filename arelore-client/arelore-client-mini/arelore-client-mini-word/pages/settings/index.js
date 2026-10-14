const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const tts = require('../../utils/tts');
const { postWithAuth, shortErrorMessage } = require('../../utils/request');

const SPELL_SPEED_PREVIEW_WORD = 'hello';

Page({
  data: {
    studyMode: storage.STUDY_MODE_SEE_MEANING,
    spellSpeed: storage.DEFAULT_SPELL_SPEED,
    wordRepeatCount: storage.DEFAULT_WORD_REPEAT_COUNT,
    saving: false,
    previewing: false
  },

  onLoad() {
    this.initAudio();
  },

  onShow() {
    this.loadPreference();
  },

  onHide() {
    this.stopPreview();
  },

  onUnload() {
    this.destroyAudio();
  },

  initAudio() {
    if (this.audio) {
      return;
    }
    const audio = wx.createInnerAudioContext();
    audio.obeyMuteSwitch = false;
    this.audio = audio;
  },

  destroyAudio() {
    this.stopPreview();
    if (this.audio) {
      try {
        this.audio.destroy();
      } catch (e) {}
      this.audio = null;
    }
  },

  stopPreview() {
    if (this.ttsSession) {
      try {
        this.ttsSession.stop();
      } catch (e) {}
      this.ttsSession = null;
    }
    if (this.audio) {
      try {
        this.audio.stop();
      } catch (e) {}
    }
    if (this.data.previewing) {
      this.setData({ previewing: false });
    }
  },

  /** 松手后试听：hello → 按当前语速拼读字母 */
  previewSpellSpeed(spellSpeed) {
    this.initAudio();
    this.stopPreview();
    if (!this.audio) {
      return;
    }
    this.setData({ previewing: true });
    this.ttsSession = tts.playWordThenSpell(this.audio, SPELL_SPEED_PREVIEW_WORD, 2, {
      wordRepeatCount: 1,
      spellSpeed,
      meaning: '',
      onEnd: () => {
        this.ttsSession = null;
        this.setData({ previewing: false });
      }
    });
  },

  applyLocal(pref) {
    const p = pref || storage.getPreference();
    this.setData({
      studyMode: p.studyMode,
      spellSpeed: p.spellSpeed,
      wordRepeatCount: p.wordRepeatCount
    });
  },

  loadPreference() {
    this.applyLocal(storage.getPreference());
    preference.syncPreference().then((pref) => {
      this.applyLocal(pref);
    }).catch(() => {});
  },

  persist(partial, options) {
    const opts = options || {};
    const prev = storage.getPreference();
    const next = storage.setPreference(partial);
    this.applyLocal(next);
    if (opts.skipRemote) {
      return;
    }

    const token = storage.getToken();
    if (!token) {
      if (!opts.silent) {
        wx.showToast({ title: '已保存到本机', icon: 'success' });
      }
      return;
    }

    this.setData({ saving: true });
    postWithAuth('/user/word/preference/update', {
      studyMode: next.studyMode,
      spellSpeed: next.spellSpeed,
      wordRepeatCount: next.wordRepeatCount
    }).then((res) => {
      this.setData({ saving: false });
      if (res.code === 401) {
        storage.clearSession();
        wx.showToast({ title: '请重新登录', icon: 'none' });
        return;
      }
      if (res.code !== 200) {
        storage.setPreference(prev);
        this.applyLocal(prev);
        wx.showToast({ title: res.message || '保存失败', icon: 'none' });
        return;
      }
      const saved = storage.setPreference({
        studyMode: res.data && res.data.studyMode,
        spellSpeed: res.data && res.data.spellSpeed,
        wordRepeatCount: res.data && res.data.wordRepeatCount
      });
      this.applyLocal(saved);
      if (!opts.silent) {
        wx.showToast({ title: '已保存', icon: 'success' });
      }
    }).catch((err) => {
      storage.setPreference(prev);
      this.applyLocal(prev);
      this.setData({ saving: false });
      const msg = shortErrorMessage(err, '网络不通，保存失败');
      wx.showToast({ title: msg.slice(0, 20), icon: 'none' });
      if (err && err.isNetworkError) {
        wx.showModal({
          title: '网络不通',
          content: msg,
          showCancel: false
        });
      }
    });
  },

  onSelectMode(e) {
    const mode = e.currentTarget.dataset.mode;
    if (mode === storage.STUDY_MODE_SEE_PICK_IMAGE) {
      wx.showToast({ title: '看词选图暂未开放', icon: 'none' });
      return;
    }
    if (mode !== storage.STUDY_MODE_SEE_MEANING && mode !== storage.STUDY_MODE_LISTEN_SPELL) {
      return;
    }
    if (mode === this.data.studyMode || this.data.saving) {
      return;
    }
    this.persist({ studyMode: mode });
  },

  onSpellSpeedChanging(e) {
    const spellSpeed = Number(e.detail.value);
    this.setData({ spellSpeed });
  },

  onSpellSpeedChange(e) {
    if (this.data.saving) {
      return;
    }
    const spellSpeed = Number(e.detail.value);
    this.setData({ spellSpeed });
    this.persist({ spellSpeed }, { silent: true });
    this.previewSpellSpeed(spellSpeed);
  },

  onSelectRepeat(e) {
    if (this.data.saving) {
      return;
    }
    const wordRepeatCount = Number(e.currentTarget.dataset.count);
    if (!(wordRepeatCount >= 1 && wordRepeatCount <= 3) || wordRepeatCount === this.data.wordRepeatCount) {
      return;
    }
    this.persist({ wordRepeatCount }, { silent: true });
  }
});
