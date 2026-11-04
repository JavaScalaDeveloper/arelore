function buildDictationSlots(word) {
  return Array.from(word || '').map((ch) => {
    if (/[a-zA-Z]/.test(ch)) {
      return { expected: ch.toLowerCase(), fixed: false, display: '' };
    }
    return { expected: ch, fixed: true, display: ch };
  });
}

function nextLetterSlotIndex(slots, filled) {
  for (let i = 0; i < slots.length; i++) {
    if (!slots[i].fixed && !filled[i]) {
      return i;
    }
  }
  return -1;
}

function isDictationComplete(slots, filled) {
  if (!slots.length) {
    return false;
  }
  return slots.every((s, i) => s.fixed || (!!filled[i] && filled[i] === s.expected));
}

function emptyDictationFilled(slots) {
  return slots.map((s) => (s.fixed ? s.expected : ''));
}

function buildDictationViewSlots(slots, filled, currentIndex) {
  return (slots || []).map((s, i) => ({
    expected: s.expected,
    fixed: s.fixed,
    display: s.fixed ? s.expected : (filled[i] || ''),
    filled: !s.fixed && !!filled[i],
    current: !s.fixed && i === currentIndex
  }));
}

module.exports = {
  buildDictationSlots,
  nextLetterSlotIndex,
  isDictationComplete,
  emptyDictationFilled,
  buildDictationViewSlots
};
