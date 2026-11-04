import { PromptMessages, WordMessages } from './wordTypes';

/** 官网壳文案（不含子应用） */
export type SiteMessageTree = {
  meta: {
    languageSwitcher: string;
  };
  common: {
    searchProducts: string;
    browseAll: string;
    contactUs: string;
    welcomeBack: string;
  };
  nav: {
    home: string;
    products: string;
    foreignLanguage: string;
    word: string;
    creator: string;
    aiPrompt: string;
    security: string;
    databaseSecurity: string;
    databaseClassification: string;
    databaseAudit: string;
    apiSecurity: string;
    applicationSecurity: string;
    codeSecurityScan: string;
    mqSecurity: string;
    mqClassification: string;
    tools: string;
    textMasking: string;
    fileDetection: string;
    documentProcessing: string;
    pdfToWord: string;
    solutions: string;
    scenarioSolutions: string;
    databaseClassificationSolution: string;
    apiClassificationSolution: string;
    mqLogClassificationSolution: string;
    fileSensitiveSolution: string;
    contact: string;
  };
  home: {
    heroAria: string;
    heroTitle: string;
    heroLead: string;
    sectionAria: string;
    sectionKicker: string;
    sectionTitle: string;
    sectionLead: string;
  };
  products: {
    title: string;
    totalCount: string;
    resultCount: string;
    empty: string;
  };
  kind: {
    product: string;
    tool: string;
    solution: string;
  };
  catalog: Record<
    string,
    {
      title: string;
      category: string;
      blurb: string;
    }
  >;
  footer: {
    createdBy: string;
    about: string;
    privacy: string;
    terms: string;
    help: string;
    icp: string;
  };
};

/** 站点 + 子应用完整文案树 */
export type MessageTree = SiteMessageTree & {
  word: WordMessages;
  prompt: PromptMessages;
};

export type { WordMessages, PromptMessages };
