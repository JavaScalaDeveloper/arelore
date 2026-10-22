export interface DictationSlot {
  /** 期望字符（字母已小写） */
  expected: string;
  /** 非字母：空格、连字符等，界面固定展示，无需输入 */
  fixed: boolean;
}

export function buildDictationSlots(word: string): DictationSlot[] {
  return Array.from(word || '').map((ch) => {
    if (/[a-zA-Z]/.test(ch)) {
      return { expected: ch.toLowerCase(), fixed: false };
    }
    return { expected: ch, fixed: true };
  });
}

/** 下一个待填的字母槽下标；全部完成返回 -1 */
export function nextLetterSlotIndex(slots: DictationSlot[], filled: string[]): number {
  for (let i = 0; i < slots.length; i++) {
    if (!slots[i].fixed && !filled[i]) {
      return i;
    }
  }
  return -1;
}

export function isDictationComplete(slots: DictationSlot[], filled: string[]): boolean {
  if (!slots.length) {
    return false;
  }
  return slots.every((s, i) => s.fixed || (!!filled[i] && filled[i] === s.expected));
}

export function emptyDictationFilled(slots: DictationSlot[]): string[] {
  return slots.map((s) => (s.fixed ? s.expected : ''));
}
