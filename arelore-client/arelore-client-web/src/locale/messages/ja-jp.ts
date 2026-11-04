import { SiteMessageTree } from './types';

const jaJp: SiteMessageTree = {
  meta: {
    languageSwitcher: "母語"
  },
  common: {
    searchProducts: "製品・ツール・ソリューションを検索…",
    browseAll: "すべての製品を見る",
    contactUs: "お問い合わせ",
    welcomeBack: "おかえりなさい、{name}さん！"
  },
  nav: {
    home: "ホーム",
    products: "製品",
    foreignLanguage: "語学",
    word: "墨魚背詞",
    creator: "創作",
    aiPrompt: "AIプロンプト",
    security: "セキュリティ",
    databaseSecurity: "データベースセキュリティ",
    databaseClassification: "データ分類",
    databaseAudit: "データベース監査",
    apiSecurity: "APIセキュリティ",
    applicationSecurity: "アプリセキュリティ",
    codeSecurityScan: "コードセキュリティスキャン",
    mqSecurity: "MQセキュリティ",
    mqClassification: "MQ分類",
    tools: "無料ツール",
    textMasking: "テキストマスキング",
    fileDetection: "ファイル機密検出",
    documentProcessing: "ドキュメント",
    pdfToWord: "PDFをWordへ",
    solutions: "ソリューション",
    scenarioSolutions: "シナリオソリューション",
    databaseClassificationSolution: "データベース分類・ガバナンス",
    apiClassificationSolution: "API分類・ガバナンス",
    mqLogClassificationSolution: "MQ・ログ分類",
    fileSensitiveSolution: "ファイル機密データ対策",
    contact: "お問い合わせ"
  },
  home: {
    heroAria: "Arelore ホーム",
    heroTitle: "能力を製品として届ける",
    heroLead: "語学学習、創作、データセキュリティ——実務に直結するクラウド製品スタック。",
    sectionAria: "注目製品",
    sectionKicker: "Products",
    sectionTitle: "プロダクトライン",
    sectionLead: "主要シナリオから入るか、カタログを検索。"
  },
  products: {
    title: "製品",
    totalCount: "{count} 件の製品・ツール・ソリューション",
    resultCount: "「{q}」の検索結果（{count}）",
    empty: "「{q}」に一致する結果はありません"
  },
  kind: {
    product: "製品",
    tool: "ツール",
    solution: "ソリューション"
  },
  catalog: {
    word: {
      title: "墨魚背詞",
      category: "語学",
      blurb: "短時間で単語を学ぶ：見る・聞く・書き取り。"
    },
    aiPrompt: {
      title: "AIプロンプト",
      category: "創作",
      blurb: "プロンプト、タグ、創作補助を整理。"
    },
    databaseClassification: {
      title: "データ分類",
      category: "データベースセキュリティ",
      blurb: "テーブルの機密データをスキャンし分類体系を構築。"
    },
    databaseAudit: {
      title: "データベース監査",
      category: "データベースセキュリティ",
      blurb: "アクセス監査とリスク追跡。"
    },
    apiSecurity: {
      title: "APIセキュリティ",
      category: "APIセキュリティ",
      blurb: "API資産の発見と保護。"
    },
    codeSecurityScan: {
      title: "コードセキュリティスキャン",
      category: "アプリセキュリティ",
      blurb: "ソースと依存関係のリスクをスキャン。"
    },
    mqClassification: {
      title: "MQ分類",
      category: "MQセキュリティ",
      blurb: "メッセージキューの機密コンテンツを分類。"
    },
    textMasking: {
      title: "テキストマスキング",
      category: "無料ツール",
      blurb: "オンラインで機密テキストをマスク。"
    },
    fileDetection: {
      title: "ファイル機密検出",
      category: "無料ツール",
      blurb: "ファイルをアップロードして機密内容を検出。"
    },
    pdfToWord: {
      title: "PDFをWordへ",
      category: "ドキュメント",
      blurb: "PDFをWordに変換。"
    },
    databaseClassificationSolution: {
      title: "データベース分類・ガバナンス",
      category: "シナリオソリューション",
      blurb: "データベース分類を本番ガバナンスへ。"
    },
    apiClassificationSolution: {
      title: "API分類・ガバナンス",
      category: "シナリオソリューション",
      blurb: "API分類とコンプライアンス管理。"
    },
    mqLogClassificationSolution: {
      title: "MQ・ログ分類",
      category: "シナリオソリューション",
      blurb: "MQとログ横断の機密分類。"
    },
    fileSensitiveSolution: {
      title: "ファイル機密データ対策",
      category: "シナリオソリューション",
      blurb: "機密ファイルの発見と保護。"
    }
  },
  footer: {
    createdBy: "Arelore ©{year} Created by Arelore Team",
    about: "会社概要",
    privacy: "プライバシーポリシー",
    terms: "利用規約",
    help: "ヘルプセンター",
    icp: "沪ICP备2026055763号"
  }
};

export default jaJp;
