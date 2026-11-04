export * from './constants';
export * from './path';
export { LocaleProvider, useLocale, useLocaleOptional } from './LocaleContext';
export { default as RegionSwitcher } from './RegionSwitcher';
export { useLocaleNavigate, LocaleNavigate } from './navigation';
export { default as LocaleGate, useStrippedLocation } from './LocaleGate';
export { useI18n, translate } from './useI18n';
export { getMessages } from './messages';

/** @deprecated 兼容旧注释：母语切换器即 RegionSwitcher */
export { default as NativeLanguageSwitcher } from './RegionSwitcher';