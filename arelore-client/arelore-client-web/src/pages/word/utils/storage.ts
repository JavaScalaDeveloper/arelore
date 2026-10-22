import { normalizeLetterVoice, LETTER_VOICE_RANDOM, type LetterVoiceId } from './letterVoices';

const KEY_BOOK = 'word_current_book_id';
const KEY_USER = 'word_current_user';
const KEY_TOKEN = 'token';
const KEY_PREFERENCE = 'word_preference';

export const STUDY_MODE_SEE_MEANING = 'see_meaning';
export const STUDY_MODE_LISTEN_SPELL = 'listen_spell';
export const STUDY_MODE_DICTATION = 'dictation';
export const STUDY_MODE_SEE_PICK_IMAGE = 'see_pick_image';

export type StudyMode =
  | typeof STUDY_MODE_SEE_MEANING
  | typeof STUDY_MODE_LISTEN_SPELL
  | typeof STUDY_MODE_DICTATION
  | typeof STUDY_MODE_SEE_PICK_IMAGE;

export interface WordPreference {
  studyMode: StudyMode;
  spellSpeed: number;
  wordRepeatCount: number;
  letterVoice: LetterVoiceId;
}

const DEFAULT_SPELL_SPEED = 5;
const DEFAULT_WORD_REPEAT_COUNT = 1;

function clampInt(n: unknown, min: number, max: number, fallback: number): number {
  const v = Number(n);
  if (!Number.isFinite(v)) {
    return fallback;
  }
  return Math.max(min, Math.min(max, Math.round(v)));
}

export function normalizePreference(raw: any): WordPreference {
  const src = raw && typeof raw === 'object' ? raw : {};
  let studyMode = src.studyMode as StudyMode;
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
    letterVoice: normalizeLetterVoice(
      src.letterVoice != null ? src.letterVoice : LETTER_VOICE_RANDOM
    ),
  };
}

export function getBookId(): string {
  return localStorage.getItem(KEY_BOOK) || '';
}

export function setBookId(id: string): void {
  localStorage.setItem(KEY_BOOK, id || '');
}

export function getToken(): string {
  return localStorage.getItem(KEY_TOKEN) || '';
}

export function setToken(token: string): void {
  if (token) {
    localStorage.setItem(KEY_TOKEN, token);
  } else {
    localStorage.removeItem(KEY_TOKEN);
  }
}

export function getUser<T = any>(): T | null {
  const raw = localStorage.getItem(KEY_USER);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as T;
  } catch {
    return null;
  }
}

export function setUser(user: unknown): void {
  if (user == null) {
    localStorage.removeItem(KEY_USER);
    return;
  }
  localStorage.setItem(KEY_USER, JSON.stringify(user));
}

export function clearSession(): void {
  localStorage.removeItem(KEY_USER);
  localStorage.removeItem(KEY_TOKEN);
}

export function getPreference(): WordPreference {
  const raw = localStorage.getItem(KEY_PREFERENCE);
  if (!raw) {
    return normalizePreference({});
  }
  try {
    return normalizePreference(JSON.parse(raw));
  } catch {
    return normalizePreference({});
  }
}

export function setPreference(partial: Partial<WordPreference>): WordPreference {
  const next = normalizePreference({ ...getPreference(), ...partial });
  localStorage.setItem(KEY_PREFERENCE, JSON.stringify(next));
  return next;
}

export function getStudyMode(): StudyMode {
  return getPreference().studyMode;
}
