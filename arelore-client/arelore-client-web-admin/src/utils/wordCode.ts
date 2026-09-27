export const WORD_CODE_PATTERN = /^[A-Z0-9_]+$/;

export const wordCodeRules = (label = 'code') => [
  { required: true, message: `请输入${label}` },
  { pattern: WORD_CODE_PATTERN, message: `${label} 仅支持大写字母、数字和下划线` }
];
