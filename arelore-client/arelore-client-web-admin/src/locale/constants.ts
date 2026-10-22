export const SUPPORTED_LOCALES = ['zh-cn', 'en-us'] as const;

export type AppLocale = (typeof SUPPORTED_LOCALES)[number];

export const DEFAULT_LOCALE: AppLocale = 'zh-cn';

export const LOCALE_STORAGE_KEY = 'arelore_admin_locale';

export const LOCALE_LABELS: Record<AppLocale, string> = {
  'zh-cn': '中国',
  'en-us': 'United States',
};

export function isAppLocale(value: string | null | undefined): value is AppLocale {
  return !!value && (SUPPORTED_LOCALES as readonly string[]).includes(value);
}
