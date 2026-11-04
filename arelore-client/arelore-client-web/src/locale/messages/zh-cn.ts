import { SiteMessageTree } from './types';

const zhCn: SiteMessageTree = {
  meta: {
    languageSwitcher: '母语',
  },
  common: {
    searchProducts: '搜索产品、工具或方案…',
    browseAll: '浏览全部产品',
    contactUs: '联系我们',
    welcomeBack: '欢迎回来，{name}！',
  },
  nav: {
    home: '首页',
    products: '产品中心',
    foreignLanguage: '外语',
    word: '墨鱼背词',
    creator: '创作',
    aiPrompt: 'AI提示词',
    security: '安全',
    databaseSecurity: '数据库安全',
    databaseClassification: '数据库分类分级',
    databaseAudit: '数据库审计',
    apiSecurity: 'API安全',
    applicationSecurity: '应用安全',
    codeSecurityScan: '代码安全扫描',
    mqSecurity: 'MQ安全',
    mqClassification: 'MQ分类分级',
    tools: '免费工具',
    textMasking: '文本脱敏',
    fileDetection: '文件敏感内容检测',
    documentProcessing: '文档处理',
    pdfToWord: 'PDF转Word',
    solutions: '解决方案',
    scenarioSolutions: '场景解决方案',
    databaseClassificationSolution: '数据库资产分类分级扫描治理方案',
    apiClassificationSolution: 'API分类分级扫描治理方案',
    mqLogClassificationSolution: 'MQ、日志分类分级解决方案',
    fileSensitiveSolution: '文件敏感信息解决方案',
    contact: '联系我们',
  },
  home: {
    heroAria: 'Arelore 首页',
    heroTitle: '把能力做成可上线的产品',
    heroLead: '语言学习、内容创作与数据安全——同一套云端产品体系，直接进入业务。',
    sectionAria: '精选产品',
    sectionKicker: 'Products',
    sectionTitle: '产品线',
    sectionLead: '从核心场景进入，或直接搜索产品。',
  },
  products: {
    title: '产品中心',
    totalCount: '共 {count} 项产品、工具与方案',
    resultCount: '与「{q}」相关的结果（{count}）',
    empty: '没有找到与「{q}」匹配的内容',
  },
  kind: {
    product: '产品',
    tool: '工具',
    solution: '方案',
  },
  catalog: {
    word: {
      title: '墨鱼背词',
      category: '外语',
      blurb: '碎片时间背单词：看词识意、听词摸鱼、听写默写。',
    },
    aiPrompt: {
      title: 'AI 提示词',
      category: '创作',
      blurb: '提示词整理、标签与创作辅助。',
    },
    databaseClassification: {
      title: '数据库分类分级',
      category: '数据库安全',
      blurb: '扫描库表敏感信息，建立分类分级体系。',
    },
    databaseAudit: {
      title: '数据库审计',
      category: '数据库安全',
      blurb: '数据库访问行为审计与风险追踪。',
    },
    apiSecurity: {
      title: 'API 安全',
      category: 'API安全',
      blurb: 'API 资产发现与安全防护。',
    },
    codeSecurityScan: {
      title: '代码安全扫描',
      category: '应用安全',
      blurb: '源码与依赖风险扫描。',
    },
    mqClassification: {
      title: 'MQ 分类分级',
      category: 'MQ安全',
      blurb: '消息队列敏感内容分类分级。',
    },
    textMasking: {
      title: '文本脱敏',
      category: '免费工具',
      blurb: '在线文本敏感信息脱敏。',
    },
    fileDetection: {
      title: '文件敏感内容检测',
      category: '免费工具',
      blurb: '上传文件检测敏感内容。',
    },
    pdfToWord: {
      title: 'PDF 转 Word',
      category: '文档处理',
      blurb: 'PDF 文档转换为 Word。',
    },
    databaseClassificationSolution: {
      title: '数据库资产分类分级扫描治理方案',
      category: '场景解决方案',
      blurb: '数据库资产分类分级落地治理。',
    },
    apiClassificationSolution: {
      title: 'API 分类分级扫描治理方案',
      category: '场景解决方案',
      blurb: 'API 分类分级与合规治理。',
    },
    mqLogClassificationSolution: {
      title: 'MQ、日志分类分级解决方案',
      category: '场景解决方案',
      blurb: '消息与日志链路的分类分级。',
    },
    fileSensitiveSolution: {
      title: '文件敏感信息解决方案',
      category: '场景解决方案',
      blurb: '文件敏感信息发现与防护。',
    },
  },
  footer: {
    createdBy: 'Arelore ©{year} Created by Arelore Team',
    about: '关于我们',
    privacy: '隐私政策',
    terms: '服务条款',
    help: '帮助中心',
    icp: '沪ICP备2026055763号',
  },
};

export default zhCn;
