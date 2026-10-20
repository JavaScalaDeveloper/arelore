import request from './request';
import {
  getPreference,
  getToken,
  setPreference,
  STUDY_MODE_LISTEN_SPELL,
  STUDY_MODE_SEE_MEANING,
  STUDY_MODE_SEE_PICK_IMAGE,
  type StudyMode,
  type WordPreference,
} from './storage';

const MODE_LABELS: Record<string, string> = {
  [STUDY_MODE_LISTEN_SPELL]: '听词（摸鱼）',
  [STUDY_MODE_SEE_MEANING]: '看词识意',
  [STUDY_MODE_SEE_PICK_IMAGE]: '看词选图',
};

export function modeLabel(mode?: string): string {
  return MODE_LABELS[mode || ''] || MODE_LABELS[STUDY_MODE_SEE_MEANING];
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
    });
  } catch {
    return local;
  }
}

export type { StudyMode };
