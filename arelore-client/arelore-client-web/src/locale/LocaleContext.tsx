import React, { createContext, useCallback, useContext, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  AppLocale,
  DEFAULT_LOCALE,
  LOCALE_STORAGE_KEY,
  isAppLocale,
} from './constants';
import { buildLocalePath, parseLocalePath, switchLocalePath } from './path';

interface LocaleContextValue {
  locale: AppLocale;
  /** 业务路径（无区域前缀） */
  pathnameWithoutLocale: string;
  setLocale: (next: AppLocale) => void;
  /** 把业务绝对路径转成带当前区域策略的完整路径 */
  localePath: (pathWithoutLocale: string) => string;
}

const LocaleContext = createContext<LocaleContextValue | null>(null);

export function LocaleProvider({
  locale: localeProp,
  children,
}: {
  locale: AppLocale;
  children: React.ReactNode;
}) {
  const navigate = useNavigate();
  const location = useLocation();
  const parsed = parseLocalePath(location.pathname);
  const locale = localeProp || parsed.locale || DEFAULT_LOCALE;
  const pathnameWithoutLocale = parsed.pathname;

  const setLocale = useCallback(
    (next: AppLocale) => {
      if (!isAppLocale(next)) {
        return;
      }
      try {
        localStorage.setItem(LOCALE_STORAGE_KEY, next);
      } catch {
        // ignore
      }
      const target = switchLocalePath(location.pathname, next);
      if (target !== location.pathname) {
        navigate(target + location.search + location.hash);
      }
    },
    [location.pathname, location.search, location.hash, navigate]
  );

  const localePath = useCallback(
    (pathWithoutLocale: string) => buildLocalePath(locale, pathWithoutLocale),
    [locale]
  );

  const value = useMemo(
    () => ({ locale, pathnameWithoutLocale, setLocale, localePath }),
    [locale, pathnameWithoutLocale, setLocale, localePath]
  );

  return <LocaleContext.Provider value={value}>{children}</LocaleContext.Provider>;
}

export function useLocale(): LocaleContextValue {
  const ctx = useContext(LocaleContext);
  if (!ctx) {
    throw new Error('useLocale must be used within LocaleProvider');
  }
  return ctx;
}

/** 安全版：无 Provider 时退回默认中国 */
export function useLocaleOptional(): LocaleContextValue {
  const ctx = useContext(LocaleContext);
  const location = useLocation();
  const navigate = useNavigate();
  const parsed = parseLocalePath(location.pathname);

  const setLocale = useCallback(
    (next: AppLocale) => {
      try {
        localStorage.setItem(LOCALE_STORAGE_KEY, next);
      } catch {
        // ignore
      }
      navigate(switchLocalePath(location.pathname, next) + location.search + location.hash);
    },
    [location.pathname, location.search, location.hash, navigate]
  );

  if (ctx) {
    return ctx;
  }

  return {
    locale: parsed.locale,
    pathnameWithoutLocale: parsed.pathname,
    setLocale,
    localePath: (p) => buildLocalePath(parsed.locale, p),
  };
}
