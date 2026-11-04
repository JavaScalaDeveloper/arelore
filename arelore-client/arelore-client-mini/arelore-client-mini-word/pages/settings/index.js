const storage = require('../../utils/storage');
const preference = require('../../utils/preference');
const letterVoices = require('../../utils/letterVoices');
const tts = require('../../utils/tts');
const { postWithAuth, shortErrorMessage } = require('../../utils/request');

const SPELL_SPEED_PREVIEW_WORD = 'hello';

Page({
  data: {
    studyMode: storage.STUDY_MODE_SEE_MEANING,
    spellSpeed: storage.DEFAULT_SPELL_SPEED,
    wordRepeatCount: storage.DEFAULT_WORD_REPEAT_COUNT,
    letterVoice: storage.DEFAULT_LETTER_VOICE,
    letterVoiceLabel: letterVoices.letterVoiceLabel(storage.DEFAULT_LETTER_VOICE),
    letterVoiceOptions: letterVoices.LETTER_VOICES,
    playbackVolume: storage.DEFAULT_PLAYBACK_VOLUME,
    saving: false,
    previewing: false
  },

  onLoad() {
    this.initAudio();
  },

  onShow() {
    if (!storage.getToken()) {
      wx.showModal({
        title: '需要登录',
        content: '设置背词模式需要登录后使用',
        confirmText: '去登录',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            wx.redirectTo({ url: '/pages/login/index' });
            return;
          }
          wx.navigateBack({
            fail: () => wx.switchTab({ url: '/pages/words/index' })
          });
        }
      });
      return;
    }
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

  /** 松手后试听：hello → 按当前语速/音色拼读字母 */
  previewSpellSpeed(spellSpeed, letterVoice) {
    this.initAudio();
    this.stopPreview();
    if (!this.audio) {
      return;
    }
    this.setData({ previewing: true });
    this.ttsSession = tts.playWordThenSpell(this.audio, SPELL_SPEED_PREVIEW_WORD, 2, {
      wordRepeatCount: 1,
      spellSpeed: spellSpeed != null ? spellSpeed : this.data.spellSpeed,
      letterVoice: letterVoice != null ? letterVoice : this.data.letterVoice,
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
      wordRepeatCount: p.wordRepeatCount,
      letterVoice: p.letterVoice,
      letterVoiceLabel: letterVoices.letterVoiceLabel(p.letterVoice),
      playbackVolume: p.playbackVolume
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
        wordRepeatCount: res.data && res.data.wordRepeatCount,
        letterVoice: next.letterVoice,
        playbackVolume: next.playbackVolume
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
    if (
      mode !== storage.STUDY_MODE_SEE_MEANING
      && mode !== storage.STUDY_MODE_LISTEN_SPELL
      && mode !== storage.STUDY_MODE_DICTATION
    ) {
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

  onSelectLetterVoice(e) {
    if (this.data.saving) {
      return;
    }
    const letterVoice = letterVoices.normalizeLetterVoice(e.currentTarget.dataset.voice);
    if (letterVoice === this.data.letterVoice) {
      this.previewSpellSpeed(this.data.spellSpeed, letterVoice);
      return;
    }
    // 仅本机保存，不打云端
    this.persist({ letterVoice }, { silent: true, skipRemote: true });
    this.previewSpellSpeed(this.data.spellSpeed, letterVoice);
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
  },

  onPlaybackVolumeChanging(e) {
    this.setData({ playbackVolume: Number(e.detail.value) });
  },

  onPlaybackVolumeChange(e) {
    const playbackVolume = Number(e.detail.value);
    this.setData({ playbackVolume });
    // 仅本机 wx storage，不写 DB；下次启动仍生效
    this.persist({ playbackVolume }, { silent: true, skipRemote: true });
    this.previewSpellSpeed(this.data.spellSpeed, this.data.letterVoice);
  }
});
