export type PasswordStrengthLevel = 'LOW' | 'MEDIUM' | 'HIGH' | '';

export interface PasswordStrengthView {
  level: PasswordStrengthLevel;
  percent: number;
  strokeColor: string;
  label: string;
  color: string;
}

/**
 * 密码强度仅用于提示，不强制大小写/符号。
 * 规则与 mobile 注册页一致：长度 + 字符类别数。
 */
export function evaluatePasswordStrength(password?: string): PasswordStrengthLevel {
  if (!password) {
    return '';
  }
  const hasLetter = /[A-Za-z]/.test(password);
  const hasNumber = /\d/.test(password);
  const hasSymbol = /[^A-Za-z0-9]/.test(password);
  const categoryCount = [hasLetter, hasNumber, hasSymbol].filter(Boolean).length;
  if (password.length >= 10 && categoryCount >= 3) {
    return 'HIGH';
  }
  if (password.length >= 8 && categoryCount >= 2) {
    return 'MEDIUM';
  }
  return 'LOW';
}

export function passwordStrengthView(password?: string): PasswordStrengthView | null {
  const level = evaluatePasswordStrength(password);
  if (!level) {
    return null;
  }
  if (level === 'HIGH') {
    return {
      level,
      percent: 100,
      strokeColor: '#52c41a',
      label: '密码强度：高（推荐）',
      color: '#389e0d',
    };
  }
  if (level === 'MEDIUM') {
    return {
      level,
      percent: 66,
      strokeColor: '#faad14',
      label: '密码强度：中（建议再加入符号或增加长度）',
      color: '#d48806',
    };
  }
  return {
    level,
    percent: 33,
    strokeColor: '#ff4d4f',
    label: '密码强度：低（建议同时包含字母、数字、符号）',
    color: '#cf1322',
  };
}
