import { AppLocale, DEFAULT_LOCALE, translate } from '../../../locale';
import request from './request';
import {
  getPreference,
  getToken,
  setPreference,
  STUDY_MODE_DICTATION,
  STUDY_MODE_LISTEN_SPELL,
  STUDY_MODE_SEE_MEANING,
  STUDY_MODE_SEE_PICK_IMAGE,
  type StudyMode,
  type WordPreference,
} from './storage';

const MODE_PATH: Record<string, string> = {
  [STUDY_MODE_LISTEN_SPELL]: 'word.mode.listenSpell',
  [STUDY_MODE_SEE_MEANING]: 'word.mode.seeMeaning',
  [STUDY_MODE_DICTATION]: 'word.mode.dictation',
  [STUDY_MODE_SEE_PICK_IMAGE]: 'word.mode.seePickImage',
};

export function modeLabel(mode?: string, locale: AppLocale = DEFAULT_LOCALE): string {
  const path = MODE_PATH[mode || ''] || MODE_PATH[STUDY_MODE_SEE_MEANING];
  return translate(locale, path);
}

export async function syncPreference(): Promise<WordPreference> {
  const local = getPreference();
  if (!getToken()) {
    return local;
  }
  try {
    const res: any = await request.post('/user/word/preference/get', {});
    if (!res?.data) {
      return local;
    }
    return setPreference({
      studyMode: res.data.studyMode,
      spellSpeed: res.data.spellSpeed,
      wordRepeatCount: res.data.wordRepeatCount,
      letterVoice: local.letterVoice,
      playbackVolume: local.playbackVolume,
    });
  } catch {
    return local;
  }
}

export async function pushLocalPreference(): Promise<WordPreference> {
  const local = getPreference();
  if (!getToken()) {
    return local;
  }
  try {
    const res: any = await request.post('/user/word/preference/update', {
      studyMode: local.studyMode,
      spellSpeed: local.spellSpeed,
      wordRepeatCount: local.wordRepeatCount,
    });
    if (!res?.data) {
      return local;
    }
    return setPreference({
      studyMode: res.data.studyMode,
      spellSpeed: res.data.spellSpeed,
      wordRepeatCount: res.data.wordRepeatCount,
      letterVoice: local.letterVoice,
      playbackVolume: local.playbackVolume,
    });
  } catch {
    return local;
  }
}

export type { StudyMode };
