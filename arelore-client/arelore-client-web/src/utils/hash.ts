import CryptoJS from 'crypto-js';

/**
 * 对字符串做 SHA-256 十六进制摘要。
 *
 * 说明：微信/部分内嵌 WebView 中 `crypto.subtle` 可能不完整或调用时抛错
 *（典型报错：Cannot read properties of undefined (reading 'digest')），
 * 因此本函数**统一使用 crypto-js 纯 JS 实现**，避免依赖 Web Crypto API。
 */
export const sha256Hex = async (text: string): Promise<string> => {
  return CryptoJS.SHA256(text).toString(CryptoJS.enc.Hex);
};
