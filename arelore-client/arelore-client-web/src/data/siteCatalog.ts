import { AppLocale, DEFAULT_LOCALE } from '../locale/constants';
import { getMessages } from '../locale/messages';
import { translate } from '../locale/useI18n';

/** 官网可搜索的产品 / 工具 / 方案（与导航 path 对齐） */
export type SiteCatalogKind = 'product' | 'tool' | 'solution';

export interface SiteCatalogItem {
  key: string;
  kind: SiteCatalogKind;
  path: string;
  /** 双语检索用；展示文案走 locale messages */
  keywords: string[];
}

export interface LocalizedSiteCatalogItem extends SiteCatalogItem {
  title: string;
  category: string;
  blurb: string;
}

export const SITE_CATALOG: SiteCatalogItem[] = [
  {
    key: 'word',
    kind: 'product',
    path: '/word',
    keywords: ['背词', '单词', '英语', '听写', '外语', '墨鱼', 'word', 'vocab', 'moyu'],
  },
  {
    key: 'aiPrompt',
    kind: 'product',
    path: '/home/products/ai-prompt/list',
    keywords: ['提示词', 'prompt', 'AI', '创作', 'create'],
  },
  {
    key: 'databaseClassification',
    kind: 'product',
    path: '/home/products/database-classification',
    keywords: ['数据库', '分类分级', '敏感数据', '安全', 'database', 'classification'],
  },
  {
    key: 'databaseAudit',
    kind: 'product',
    path: '/home/products/database-audit',
    keywords: ['审计', '数据库', '安全', 'audit'],
  },
  {
    key: 'apiSecurity',
    kind: 'product',
    path: '/home/products/api-security',
    keywords: ['API', '接口', '安全', 'api'],
  },
  {
    key: 'codeSecurityScan',
    kind: 'product',
    path: '/home/products/application-security/code-security-scan',
    keywords: ['代码', '扫描', 'SAST', '安全', 'code', 'scan'],
  },
  {
    key: 'mqClassification',
    kind: 'product',
    path: '/home/products/mq-classification',
    keywords: ['MQ', '消息队列', '分类分级', 'queue'],
  },
  {
    key: 'textMasking',
    kind: 'tool',
    path: '/home/tools/text-masking',
    keywords: ['脱敏', '文本', '工具', '隐私', 'masking', 'privacy'],
  },
  {
    key: 'fileDetection',
    kind: 'tool',
    path: '/home/tools/file-detection',
    keywords: ['文件', '检测', '敏感', '工具', 'file', 'detection'],
  },
  {
    key: 'pdfToWord',
    kind: 'tool',
    path: '/home/tools/pdf-to-word',
    keywords: ['PDF', 'Word', '转换', '文档', 'convert'],
  },
  {
    key: 'databaseClassificationSolution',
    kind: 'solution',
    path: '/home/solutions/database-classification',
    keywords: ['方案', '数据库', '治理', 'governance'],
  },
  {
    key: 'apiClassificationSolution',
    kind: 'solution',
    path: '/home/solutions/api-classification',
    keywords: ['方案', 'API', '治理'],
  },
  {
    key: 'mqLogClassificationSolution',
    kind: 'solution',
    path: '/home/solutions/mq-log-classification',
    keywords: ['方案', 'MQ', '日志', 'log'],
  },
  {
    key: 'fileSensitiveSolution',
    kind: 'solution',
    path: '/home/solutions/file-sensitive',
    keywords: ['方案', '文件', '敏感', 'file'],
  },
];

export function kindLabel(kind: SiteCatalogKind, locale: AppLocale = DEFAULT_LOCALE): string {
  return translate(locale, `kind.${kind}`);
}

export function localizeCatalogItem(
  item: SiteCatalogItem,
  locale: AppLocale = DEFAULT_LOCALE
): LocalizedSiteCatalogItem {
  const pack = getMessages(locale).catalog[item.key];
  return {
    ...item,
    title: pack?.title || item.key,
    category: pack?.category || '',
    blurb: pack?.blurb || '',
  };
}

export function localizedSiteCatalog(locale: AppLocale = DEFAULT_LOCALE): LocalizedSiteCatalogItem[] {
  return SITE_CATALOG.map((item) => localizeCatalogItem(item, locale));
}

/** 本地关键词搜索；空查询返回全部（按当前语言文案 + 双语 keywords） */
export function searchSiteCatalog(
  query: string,
  limit = 20,
  locale: AppLocale = DEFAULT_LOCALE
): LocalizedSiteCatalogItem[] {
  const all = localizedSiteCatalog(locale);
  const q = String(query || '').trim().toLowerCase();
  if (!q) {
    return all.slice(0, limit);
  }
  const scored = all.map((item) => {
    const hay = [item.title, item.category, item.blurb, ...item.keywords]
      .join(' ')
      .toLowerCase();
    let score = 0;
    if (item.title.toLowerCase().includes(q)) score += 40;
    if (item.keywords.some((k) => k.toLowerCase().includes(q) || q.includes(k.toLowerCase()))) {
      score += 25;
    }
    if (item.category.toLowerCase().includes(q)) score += 15;
    if (hay.includes(q)) score += 10;
    q.split(/\s+/).forEach((part) => {
      if (part && hay.includes(part)) score += 5;
    });
    return { item, score };
  })
    .filter((x) => x.score > 0)
    .sort((a, b) => b.score - a.score);
  return scored.slice(0, limit).map((x) => x.item);
}
