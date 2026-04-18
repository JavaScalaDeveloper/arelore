import CryptoJS from 'crypto-js';

const toHex = (buffer: ArrayBuffer): string => {
  const bytes = Array.from(new Uint8Array(buffer));
  return bytes.map((b) => b.toString(16).padStart(2, '0')).join('');
};

/**
 * 优先使用 Web Crypto；在不支持 subtle.digest 的运行环境（如部分内嵌 WebView）回退到 crypto-js。
 */
export const sha256Hex = async (text: string): Promise<string> => {
  const globalCrypto = (globalThis as { crypto?: Crypto }).crypto;
  if (globalCrypto?.subtle && typeof globalCrypto.subtle.digest === 'function') {
    try {
      const data = new TextEncoder().encode(text);
      const hashBuffer = await globalCrypto.subtle.digest('SHA-256', data);
      return toHex(hashBuffer);
    } catch (error) {
      // WebView 对 subtle.digest 实现不完整时回退到 crypto-js。
    }
  }
  return CryptoJS.SHA256(text).toString(CryptoJS.enc.Hex);
};
