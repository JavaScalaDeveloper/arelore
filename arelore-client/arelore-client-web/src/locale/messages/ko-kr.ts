import { SiteMessageTree } from './types';

const koKr: SiteMessageTree = {
  meta: {
    languageSwitcher: "모국어"
  },
  common: {
    searchProducts: "제품, 도구, 솔루션 검색…",
    browseAll: "모든 제품 보기",
    contactUs: "문의하기",
    welcomeBack: "다시 오신 것을 환영합니다, {name}님!"
  },
  nav: {
    home: "홈",
    products: "제품",
    foreignLanguage: "언어",
    word: "묵어배사",
    creator: "창작",
    aiPrompt: "AI 프롬프트",
    security: "보안",
    databaseSecurity: "데이터베이스 보안",
    databaseClassification: "데이터 분류",
    databaseAudit: "데이터베이스 감사",
    apiSecurity: "API 보안",
    applicationSecurity: "앱 보안",
    codeSecurityScan: "코드 보안 스캔",
    mqSecurity: "MQ 보안",
    mqClassification: "MQ 분류",
    tools: "무료 도구",
    textMasking: "텍스트 마스킹",
    fileDetection: "파일 민감정보 탐지",
    documentProcessing: "문서",
    pdfToWord: "PDF를 Word로",
    solutions: "솔루션",
    scenarioSolutions: "시나리오 솔루션",
    databaseClassificationSolution: "데이터베이스 분류·거버넌스",
    apiClassificationSolution: "API 분류·거버넌스",
    mqLogClassificationSolution: "MQ·로그 분류",
    fileSensitiveSolution: "파일 민감데이터 솔루션",
    contact: "문의"
  },
  home: {
    heroAria: "Arelore 홈",
    heroTitle: "역량을 제품으로 출시하세요",
    heroLead: "언어 학습, 창작, 데이터 보안 — 실제 업무를 위한 하나의 클라우드 제품 스택.",
    sectionAria: "주요 제품",
    sectionKicker: "Products",
    sectionTitle: "제품 라인",
    sectionLead: "핵심 시나리오에서 시작하거나 카탈로그를 검색하세요."
  },
  products: {
    title: "제품",
    totalCount: "제품·도구·솔루션 {count}개",
    resultCount: "“{q}” 검색 결과 ({count})",
    empty: "“{q}”와 일치하는 결과가 없습니다"
  },
  kind: {
    product: "제품",
    tool: "도구",
    solution: "솔루션"
  },
  catalog: {
    word: {
      title: "묵어배사",
      category: "언어",
      blurb: "짧은 세션으로 단어 학습: 보고, 듣고, 받아쓰기."
    },
    aiPrompt: {
      title: "AI 프롬프트",
      category: "창작",
      blurb: "프롬프트, 태그, 창작 도우미 정리."
    },
    databaseClassification: {
      title: "데이터 분류",
      category: "데이터베이스 보안",
      blurb: "테이블의 민감 데이터를 스캔하고 분류 체계를 구축."
    },
    databaseAudit: {
      title: "데이터베이스 감사",
      category: "데이터베이스 보안",
      blurb: "접근 감사와 리스크 추적."
    },
    apiSecurity: {
      title: "API 보안",
      category: "API 보안",
      blurb: "API 자산 발견과 보호."
    },
    codeSecurityScan: {
      title: "코드 보안 스캔",
      category: "앱 보안",
      blurb: "소스와 의존성 리스크 스캔."
    },
    mqClassification: {
      title: "MQ 분류",
      category: "MQ 보안",
      blurb: "메시지 큐의 민감 콘텐츠 분류."
    },
    textMasking: {
      title: "텍스트 마스킹",
      category: "무료 도구",
      blurb: "온라인에서 민감 텍스트 마스킹."
    },
    fileDetection: {
      title: "파일 민감정보 탐지",
      category: "무료 도구",
      blurb: "파일 업로드로 민감 내용 탐지."
    },
    pdfToWord: {
      title: "PDF를 Word로",
      category: "문서",
      blurb: "PDF를 Word로 변환."
    },
    databaseClassificationSolution: {
      title: "데이터베이스 분류·거버넌스",
      category: "시나리오 솔루션",
      blurb: "데이터베이스 분류를 운영 거버넌스로."
    },
    apiClassificationSolution: {
      title: "API 분류·거버넌스",
      category: "시나리오 솔루션",
      blurb: "API 분류와 컴플라이언스 관리."
    },
    mqLogClassificationSolution: {
      title: "MQ·로그 분류",
      category: "시나리오 솔루션",
      blurb: "MQ와 로그 전반의 민감 데이터 분류."
    },
    fileSensitiveSolution: {
      title: "파일 민감데이터 솔루션",
      category: "시나리오 솔루션",
      blurb: "민감 파일 발견과 보호."
    }
  },
  footer: {
    createdBy: "Arelore ©{year} Created by Arelore Team",
    about: "회사 소개",
    privacy: "개인정보 처리방침",
    terms: "이용약관",
    help: "고객센터",
    icp: "沪ICP备2026055763号"
  }
};

export default koKr;
