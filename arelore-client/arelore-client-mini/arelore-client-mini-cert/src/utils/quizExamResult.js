/** 交卷结果页：题型与对错判定（与题库 extra_info / options.scores 约定一致） */

export function parseQuestionExtraInfo(raw) {
  if (!raw) return {};
  try {
    return JSON.parse(raw) || {};
  } catch (e) {
    return {};
  }
}

export function isSubjectiveQuestion(extra) {
  const t = (extra?.questionType || extra?.题型 || '').trim();
  return /主观|简答|论述|案例分析|问答|填空/.test(t);
}

export function parseUserKeys(selectedOptionKey) {
  if (selectedOptionKey == null || selectedOptionKey === '') return [];
  const s = String(selectedOptionKey).trim();
  if (!s) return [];
  if (/[,，;；]/.test(s)) {
    return s
      .split(/[,，;；\s]+/)
      .map((x) => x.trim().toUpperCase())
      .filter(Boolean);
  }
  if (/^[A-Za-z]{2,}$/.test(s) && s.length <= 12) {
    return s.toUpperCase().split('');
  }
  return [s.toUpperCase()];
}

export function inferCorrectKeys(extra, options) {
  if (extra?.correctOptionKeys && Array.isArray(extra.correctOptionKeys)) {
    return extra.correctOptionKeys.map((k) => String(k).trim().toUpperCase()).filter(Boolean);
  }
  if (extra?.correctOptionKey != null && String(extra.correctOptionKey).trim() !== '') {
    const ck = String(extra.correctOptionKey).trim();
    const qt = extra?.questionType || '';
    if (qt.includes('多选')) {
      return parseUserKeys(ck);
    }
    if (ck.length > 1 && !/[,，;；]/.test(ck)) {
      return parseUserKeys(ck);
    }
    return [ck.toUpperCase()];
  }
  const keys = [];
  const dim = 'score';
  (options || []).forEach((op) => {
    if (!op || !op.key) return;
    const sc = op.scores && (op.scores[dim] ?? op.scores.SCORE);
    const v = Number(sc);
    if (Number.isFinite(v) && v > 0) {
      keys.push(String(op.key).trim().toUpperCase());
    }
  });
  return keys;
}

/**
 * @returns {'correct'|'wrong'|'partial'|'unanswered'|'subjective'}
 */
export function classifyAnswerItem(question, detail) {
  const extra = parseQuestionExtraInfo(question?.extraInfo);
  let options = [];
  try {
    const arr = JSON.parse(question?.options || '[]');
    options = Array.isArray(arr) ? arr : [];
  } catch (e) {
    options = [];
  }

  if (isSubjectiveQuestion(extra)) {
    return 'subjective';
  }

  if (detail?.unanswered === true || detail?.selectedOptionText === '(未作答)') {
    return 'unanswered';
  }
  const sk = detail?.selectedOptionKey;
  if (sk == null || String(sk).trim() === '') {
    return 'unanswered';
  }

  const userKeys = parseUserKeys(sk);
  const correctKeys = inferCorrectKeys(extra, options);

  if (correctKeys.length === 0) {
    return 'subjective';
  }

  const userSet = new Set(userKeys);
  if (userSet.size === 0) return 'unanswered';

  const correctSet = new Set(correctKeys);
  const wrongPicked = userKeys.some((k) => !correctSet.has(k));
  const allCorrectSelected = correctKeys.every((k) => userSet.has(k));

  if (allCorrectSelected && userSet.size === correctSet.size && !wrongPicked) {
    return 'correct';
  }
  const correctCount = userKeys.filter((k) => correctSet.has(k)).length;
  if (correctCount > 0 && (!allCorrectSelected || wrongPicked)) {
    return 'partial';
  }
  return 'wrong';
}

export const RESULT_STATUS_CLASS = {
  correct: 'cellCorrect',
  wrong: 'cellWrong',
  partial: 'cellPartial',
  unanswered: 'cellUnanswered',
  subjective: 'cellSubjective'
};
