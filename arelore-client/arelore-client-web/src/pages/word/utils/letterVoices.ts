export const LETTER_VOICE_RANDOM = 'random';

export const LETTER_VOICES = [
  { id: 'jenny', label: 'Jenny·美女' },
  { id: 'guy', label: 'Guy·美男' },
  { id: 'sonia', label: 'Sonia·英女' },
  { id: 'ryan', label: 'Ryan·英男' },
] as const;

export type LetterVoiceId = (typeof LETTER_VOICES)[number]['id'] | typeof LETTER_VOICE_RANDOM;

const LETTER_VOICE_IDS = LETTER_VOICES.map((v) => v.id);

export function isKnownLetterVoice(id: string): boolean {
  return LETTER_VOICE_IDS.indexOf(id as any) >= 0;
}

export function normalizeLetterVoice(raw: unknown): LetterVoiceId {
  const value = raw == null ? '' : String(raw).trim();
  if (!value || value === LETTER_VOICE_RANDOM) {
    return LETTER_VOICE_RANDOM;
  }
  return isKnownLetterVoice(value) ? (value as LetterVoiceId) : LETTER_VOICE_RANDOM;
}

export function resolveLetterPackId(letterVoice: unknown): string {
  const pref = normalizeLetterVoice(letterVoice);
  if (pref !== LETTER_VOICE_RANDOM && isKnownLetterVoice(pref)) {
    return pref;
  }
  if (!LETTER_VOICE_IDS.length) {
    return 'jenny';
  }
  return LETTER_VOICE_IDS[Math.floor(Math.random() * LETTER_VOICE_IDS.length)];
}

export function localLetterSrc(packId: string, letter: string): string {
  const c = String(letter || '').toLowerCase();
  if (!/^[a-z]$/.test(c)) {
    return '';
  }
  return `${process.env.PUBLIC_URL || ''}/assets/letters/${packId}/${c}.mp3`;
}
