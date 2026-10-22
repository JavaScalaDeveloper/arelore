import { AppLocale, DEFAULT_LOCALE, isAppLocale } from './constants';

export interface ParsedLocalePath {
  locale: AppLocale;
  /** 去掉区域前缀后的路径，始终以 / 开头（根为 /） */
  pathname: string;
  /** URL 是否显式带了 /zh-cn 或 /en-us */
  hasExplicitPrefix: boolean;
}

/** 从完整 pathname 解析区域与业务路径 */
export function parseLocalePath(pathname: string): ParsedLocalePath {
  const raw = pathname || '/';
  const parts = raw.split('/').filter(Boolean);
  if (parts.length > 0 && isAppLocale(parts[0])) {
    const rest = '/' + parts.slice(1).join('/');
    return {
      locale: parts[0],
      pathname: rest === '/' ? '/' : rest.replace(/\/+$/, '') || '/',
      hasExplicitPrefix: true,
    };
  }
  return {
    locale: DEFAULT_LOCALE,
    pathname: raw === '/' ? '/' : raw.replace(/\/+$/, '') || '/',
    hasExplicitPrefix: false,
  };
}

export function stripLocalePrefix(pathname: string): string {
  return parseLocalePath(pathname).pathname;
}

/**
 * 拼出带区域策略的路径：
 * - zh-cn → 无前缀（默认）
 * - en-us → /en-us/...
 */
export function buildLocalePath(locale: AppLocale, pathWithoutLocale: string): string {
  let path = pathWithoutLocale || '/';
  if (!path.startsWith('/')) {
    path = '/' + path;
  }
  if (path.length > 1 && path.endsWith('/')) {
    path = path.slice(0, -1);
  }
  if (locale === DEFAULT_LOCALE) {
    return path || '/';
  }
  if (path === '/') {
    return `/${locale}`;
  }
  return `/${locale}${path}`;
}

/** 切换区域时，保留当前业务路径 */
export function switchLocalePath(currentPathname: string, nextLocale: AppLocale): string {
  const { pathname } = parseLocalePath(currentPathname);
  return buildLocalePath(nextLocale, pathname);
}

/** 从当前浏览器地址解析 locale（供 axios 等非 React 场景） */
export function getLocaleFromLocation(): AppLocale {
  if (typeof window === 'undefined') {
    return DEFAULT_LOCALE;
  }
  return parseLocalePath(window.location.pathname).locale;
}
