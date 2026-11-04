import { SiteMessageTree } from './types';

const enUs: SiteMessageTree = {
  meta: {
    languageSwitcher: 'Native language',
  },
  common: {
    searchProducts: 'Search products, tools, or solutions…',
    browseAll: 'Browse all products',
    contactUs: 'Contact us',
    welcomeBack: 'Welcome back, {name}!',
  },
  nav: {
    home: 'Home',
    products: 'Products',
    foreignLanguage: 'Languages',
    word: 'Moyu Words',
    creator: 'Create',
    aiPrompt: 'AI Prompts',
    security: 'Security',
    databaseSecurity: 'Database security',
    databaseClassification: 'Data classification',
    databaseAudit: 'Database audit',
    apiSecurity: 'API security',
    applicationSecurity: 'App security',
    codeSecurityScan: 'Code security scan',
    mqSecurity: 'MQ security',
    mqClassification: 'MQ classification',
    tools: 'Free tools',
    textMasking: 'Text masking',
    fileDetection: 'File sensitive-content scan',
    documentProcessing: 'Documents',
    pdfToWord: 'PDF to Word',
    solutions: 'Solutions',
    scenarioSolutions: 'Scenario solutions',
    databaseClassificationSolution: 'Database classification & governance',
    apiClassificationSolution: 'API classification & governance',
    mqLogClassificationSolution: 'MQ & log classification',
    fileSensitiveSolution: 'File sensitive-data solution',
    contact: 'Contact',
  },
  home: {
    heroAria: 'Arelore home',
    heroTitle: 'Ship capabilities as products',
    heroLead: 'Language learning, creation, and data security — one cloud product stack for real workflows.',
    sectionAria: 'Featured products',
    sectionKicker: 'Products',
    sectionTitle: 'Product line',
    sectionLead: 'Enter from a core scenario, or search the catalog.',
  },
  products: {
    title: 'Products',
    totalCount: '{count} products, tools, and solutions',
    resultCount: 'Results for “{q}” ({count})',
    empty: 'No matches for “{q}”',
  },
  kind: {
    product: 'Product',
    tool: 'Tool',
    solution: 'Solution',
  },
  catalog: {
    word: {
      title: 'Moyu Words',
      category: 'Languages',
      blurb: 'Learn words in short sessions: see, hear, and dictate.',
    },
    aiPrompt: {
      title: 'AI Prompts',
      category: 'Create',
      blurb: 'Organize prompts, tags, and creative helpers.',
    },
    databaseClassification: {
      title: 'Data classification',
      category: 'Database security',
      blurb: 'Scan tables for sensitive data and build a classification scheme.',
    },
    databaseAudit: {
      title: 'Database audit',
      category: 'Database security',
      blurb: 'Audit database access and track risk.',
    },
    apiSecurity: {
      title: 'API security',
      category: 'API security',
      blurb: 'Discover API assets and protect them.',
    },
    codeSecurityScan: {
      title: 'Code security scan',
      category: 'App security',
      blurb: 'Scan source and dependency risks.',
    },
    mqClassification: {
      title: 'MQ classification',
      category: 'MQ security',
      blurb: 'Classify sensitive content in message queues.',
    },
    textMasking: {
      title: 'Text masking',
      category: 'Free tools',
      blurb: 'Mask sensitive text online.',
    },
    fileDetection: {
      title: 'File sensitive-content scan',
      category: 'Free tools',
      blurb: 'Upload files to detect sensitive content.',
    },
    pdfToWord: {
      title: 'PDF to Word',
      category: 'Documents',
      blurb: 'Convert PDF documents to Word.',
    },
    databaseClassificationSolution: {
      title: 'Database classification & governance',
      category: 'Scenario solutions',
      blurb: 'Put database classification into production governance.',
    },
    apiClassificationSolution: {
      title: 'API classification & governance',
      category: 'Scenario solutions',
      blurb: 'API classification with compliance controls.',
    },
    mqLogClassificationSolution: {
      title: 'MQ & log classification',
      category: 'Scenario solutions',
      blurb: 'Classify sensitive data across MQ and logs.',
    },
    fileSensitiveSolution: {
      title: 'File sensitive-data solution',
      category: 'Scenario solutions',
      blurb: 'Discover and protect sensitive files.',
    },
  },
  footer: {
    createdBy: 'Arelore ©{year} Created by Arelore Team',
    about: 'About us',
    privacy: 'Privacy policy',
    terms: 'Terms of service',
    help: 'Help center',
    icp: '沪ICP备2026055763号',
  },
};

export default enUs;
