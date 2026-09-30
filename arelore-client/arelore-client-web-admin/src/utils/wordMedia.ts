/** 解析词条/词本 extInfo，并生成配图与英美音地址 */

export function parseExtInfo(raw?: string | null): Record<string, any> {
  if (!raw) {
    return {};
  }
  try {
    const obj = JSON.parse(raw);
    return obj && typeof obj === 'object' ? obj : {};
  } catch {
    return {};
  }
}

export function resolveBookCover(record: any): string {
  if (!record) {
    return '';
  }
  if (record.cover) {
    return String(record.cover).trim();
  }
  const ext = parseExtInfo(record.extInfo);
  return (ext.cover || '').trim();
}

/** http → https，避免管理端混合内容被拦截 */
export function toHttpsUrl(url?: string | null): string {
  if (!url) {
    return '';
  }
  const s = String(url).trim();
  if (s.startsWith('http://')) {
    return `https://${s.slice('http://'.length)}`;
  }
  return s;
}

export function resolveEntryPicture(extOrRecord: any, extInfoRaw?: string): string {
  const ext = extOrRecord?.picture != null || extOrRecord?.ukphone != null
    ? extOrRecord
    : parseExtInfo(extInfoRaw ?? extOrRecord?.extInfo);
  return toHttpsUrl(ext?.picture || '');
}

/**
 * 有道发音：type=1 英音，type=2 美音。
 * 优先用词条 ukspeech/usspeech；否则用单词原文。
 */
export function resolveDictVoiceUrl(word: string, voice: 'uk' | 'us', ext?: Record<string, any>): string {
  const speech = voice === 'uk' ? ext?.ukspeech : ext?.usspeech;
  if (speech && String(speech).trim()) {
    const raw = String(speech).trim();
    if (raw.startsWith('http')) {
      return toHttpsUrl(raw);
    }
    return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(raw)}`;
  }
  const audio = (word || '').trim();
  if (!audio) {
    return '';
  }
  const type = voice === 'uk' ? 1 : 2;
  return `https://dict.youdao.com/dictvoice?audio=${encodeURIComponent(audio)}&type=${type}`;
}
