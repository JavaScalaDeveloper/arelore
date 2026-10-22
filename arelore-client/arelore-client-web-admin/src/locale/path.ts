import { AppLocale, DEFAULT_LOCALE, isAppLocale } from './constants';

export interface ParsedLocalePath {
  locale: AppLocale;
  pathname: string;
  hasExplicitPrefix: boolean;
}

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

export function switchLocalePath(currentPathname: string, nextLocale: AppLocale): string {
  const { pathname } = parseLocalePath(currentPathname);
  return buildLocalePath(nextLocale, pathname);
}

export function getLocaleFromLocation(): AppLocale {
  if (typeof window === 'undefined') {
    return DEFAULT_LOCALE;
  }
  return parseLocalePath(window.location.pathname).locale;
}
