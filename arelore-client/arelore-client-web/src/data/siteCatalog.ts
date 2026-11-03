/** 官网可搜索的产品 / 工具 / 方案（与导航 path 对齐） */
export type SiteCatalogKind = 'product' | 'tool' | 'solution';

export interface SiteCatalogItem {
  key: string;
  title: string;
  kind: SiteCatalogKind;
  category: string;
  path: string;
  blurb: string;
  keywords: string[];
}

export const SITE_CATALOG: SiteCatalogItem[] = [
  {
    key: 'word',
    title: '墨鱼背词',
    kind: 'product',
    category: '外语',
    path: '/word',
    blurb: '碎片时间背单词：看词识意、听词摸鱼、听写默写。',
    keywords: ['背词', '单词', '英语', '听写', '外语', '墨鱼'],
  },
  {
    key: 'aiPrompt',
    title: 'AI 提示词',
    kind: 'product',
    category: '创作',
    path: '/home/products/ai-prompt/list',
    blurb: '提示词整理、标签与创作辅助。',
    keywords: ['提示词', 'prompt', 'AI', '创作'],
  },
  {
    key: 'databaseClassification',
    title: '数据库分类分级',
    kind: 'product',
    category: '数据库安全',
    path: '/home/products/database-classification',
    blurb: '扫描库表敏感信息，建立分类分级体系。',
    keywords: ['数据库', '分类分级', '敏感数据', '安全'],
  },
  {
    key: 'databaseAudit',
    title: '数据库审计',
    kind: 'product',
    category: '数据库安全',
    path: '/home/products/database-audit',
    blurb: '数据库访问行为审计与风险追踪。',
    keywords: ['审计', '数据库', '安全'],
  },
  {
    key: 'apiSecurity',
    title: 'API 安全',
    kind: 'product',
    category: 'API安全',
    path: '/home/products/api-security',
    blurb: 'API 资产发现与安全防护。',
    keywords: ['API', '接口', '安全'],
  },
  {
    key: 'codeSecurityScan',
    title: '代码安全扫描',
    kind: 'product',
    category: '应用安全',
    path: '/home/products/application-security/code-security-scan',
    blurb: '源码与依赖风险扫描。',
    keywords: ['代码', '扫描', 'SAST', '安全'],
  },
  {
    key: 'mqClassification',
    title: 'MQ 分类分级',
    kind: 'product',
    category: 'MQ安全',
    path: '/home/products/mq-classification',
    blurb: '消息队列敏感内容分类分级。',
    keywords: ['MQ', '消息队列', '分类分级'],
  },
  {
    key: 'textMasking',
    title: '文本脱敏',
    kind: 'tool',
    category: '免费工具',
    path: '/home/tools/text-masking',
    blurb: '在线文本敏感信息脱敏。',
    keywords: ['脱敏', '文本', '工具', '隐私'],
  },
  {
    key: 'fileDetection',
    title: '文件敏感内容检测',
    kind: 'tool',
    category: '免费工具',
    path: '/home/tools/file-detection',
    blurb: '上传文件检测敏感内容。',
    keywords: ['文件', '检测', '敏感', '工具'],
  },
  {
    key: 'pdfToWord',
    title: 'PDF 转 Word',
    kind: 'tool',
    category: '文档处理',
    path: '/home/tools/pdf-to-word',
    blurb: 'PDF 文档转换为 Word。',
    keywords: ['PDF', 'Word', '转换', '文档'],
  },
  {
    key: 'databaseClassificationSolution',
    title: '数据库资产分类分级扫描治理方案',
    kind: 'solution',
    category: '场景解决方案',
    path: '/home/solutions/database-classification',
    blurb: '数据库资产分类分级落地治理。',
    keywords: ['方案', '数据库', '治理'],
  },
  {
    key: 'apiClassificationSolution',
    title: 'API 分类分级扫描治理方案',
    kind: 'solution',
    category: '场景解决方案',
    path: '/home/solutions/api-classification',
    blurb: 'API 分类分级与合规治理。',
    keywords: ['方案', 'API', '治理'],
  },
  {
    key: 'mqLogClassificationSolution',
    title: 'MQ、日志分类分级解决方案',
    kind: 'solution',
    category: '场景解决方案',
    path: '/home/solutions/mq-log-classification',
    blurb: '消息与日志链路的分类分级。',
    keywords: ['方案', 'MQ', '日志'],
  },
  {
    key: 'fileSensitiveSolution',
    title: '文件敏感信息解决方案',
    kind: 'solution',
    category: '场景解决方案',
    path: '/home/solutions/file-sensitive',
    blurb: '文件敏感信息发现与防护。',
    keywords: ['方案', '文件', '敏感'],
  },
];

const KIND_LABEL: Record<SiteCatalogKind, string> = {
  product: '产品',
  tool: '工具',
  solution: '方案',
};

export function kindLabel(kind: SiteCatalogKind): string {
  return KIND_LABEL[kind];
}

/** 本地关键词搜索；空查询返回全部 */
export function searchSiteCatalog(query: string, limit = 20): SiteCatalogItem[] {
  const q = String(query || '').trim().toLowerCase();
  if (!q) {
    return SITE_CATALOG.slice(0, limit);
  }
  const scored = SITE_CATALOG.map((item) => {
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
