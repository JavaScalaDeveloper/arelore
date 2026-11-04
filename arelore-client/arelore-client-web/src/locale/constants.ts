/**
 * 右上角切换的是学习者「母语」（看英文词条时的释义语种），默认简体中文。
 * 词典词头目前是英语；母语决定展示有道中文义 / wordTranslations 对应语种等。
 *
 * URL 前缀与 X-Locale 使用 BCP47 短码（zh-cn、ja-jp…）。
 * 站点壳文案：下拉框全部母语均有完整 MessageTree；词条释义仍按 glossLang 走。
 */
export const SUPPORTED_LOCALES = [
  'zh-cn',
  'en-us',
  'ja-jp',
  'ko-kr',
  'es-es',
  'fr-fr',
  'de-de',
  'pt-br',
  'ru-ru',
  'vi-vn',
  'th-th',
  'id-id',
  'ar-sa',
  'hi-in',
  'it-it',
  'nl-nl',
  'tr-tr',
  'pl-pl',
  'sv-se',
  'uk-ua',
  'ms-my',
] as const;

export type AppLocale = (typeof SUPPORTED_LOCALES)[number];

export const DEFAULT_LOCALE: AppLocale = 'zh-cn';

export const LOCALE_STORAGE_KEY = 'arelore_locale';

/** 切换器展示名：语言自身名称（母语名） */
export const LOCALE_LABELS: Record<AppLocale, string> = {
  'zh-cn': '简体中文',
  'en-us': 'English',
  'ja-jp': '日本語',
  'ko-kr': '한국어',
  'es-es': 'Español',
  'fr-fr': 'Français',
  'de-de': 'Deutsch',
  'pt-br': 'Português',
  'ru-ru': 'Русский',
  'vi-vn': 'Tiếng Việt',
  'th-th': 'ไทย',
  'id-id': 'Bahasa Indonesia',
  'ar-sa': 'العربية',
  'hi-in': 'हिन्दी',
  'it-it': 'Italiano',
  'nl-nl': 'Nederlands',
  'tr-tr': 'Türkçe',
  'pl-pl': 'Polski',
  'sv-se': 'Svenska',
  'uk-ua': 'Українська',
  'ms-my': 'Bahasa Melayu',
};

/**
 * 母语 → ISO 3166-1 alpha-2（用于 /flags/{code}.png）。
 * 不用 emoji：Windows 会把区域指示符渲染成 CN/US/JP 字母。
 */
export const LOCALE_FLAG_ISO: Record<AppLocale, string> = {
  'zh-cn': 'cn',
  'en-us': 'us',
  'ja-jp': 'jp',
  'ko-kr': 'kr',
  'es-es': 'es',
  'fr-fr': 'fr',
  'de-de': 'de',
  'pt-br': 'br',
  'ru-ru': 'ru',
  'vi-vn': 'vn',
  'th-th': 'th',
  'id-id': 'id',
  'ar-sa': 'sa',
  'hi-in': 'in',
  'it-it': 'it',
  'nl-nl': 'nl',
  'tr-tr': 'tr',
  'pl-pl': 'pl',
  'sv-se': 'se',
  'uk-ua': 'ua',
  'ms-my': 'my',
};

export function localeFlagSrc(locale: AppLocale): string {
  return `${process.env.PUBLIC_URL || ''}/flags/${LOCALE_FLAG_ISO[locale]}.png`;
}

/** 母语 → 词库对译 / TTS 短码（对齐服务端 I18nGlossLang） */
export const LOCALE_GLOSS_LANG: Record<AppLocale, string> = {
  'zh-cn': 'zh',
  'en-us': 'en',
  'ja-jp': 'ja',
  'ko-kr': 'ko',
  'es-es': 'es',
  'fr-fr': 'fr',
  'de-de': 'de',
  'pt-br': 'pt',
  'ru-ru': 'ru',
  'vi-vn': 'vi',
  'th-th': 'th',
  'id-id': 'id',
  'ar-sa': 'ar',
  'hi-in': 'hi',
  'it-it': 'it',
  'nl-nl': 'nl',
  'tr-tr': 'tr',
  'pl-pl': 'pl',
  'sv-se': 'sv',
  'uk-ua': 'uk',
  'ms-my': 'ms',
};

/** 非默认母语（URL 需要 /ja-jp 等前缀） */
export const PREFIXED_LOCALES = SUPPORTED_LOCALES.filter((l) => l !== DEFAULT_LOCALE);

export function isAppLocale(value: string | null | undefined): value is AppLocale {
  return !!value && (SUPPORTED_LOCALES as readonly string[]).includes(value);
}

export function glossLangOf(locale: AppLocale): string {
  return LOCALE_GLOSS_LANG[locale] || 'zh';
}
