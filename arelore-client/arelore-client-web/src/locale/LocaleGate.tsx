import React from 'react';
import { useLocation } from 'react-router-dom';
import { AppLocale } from './constants';
import { LocaleProvider } from './LocaleContext';
import { parseLocalePath } from './path';

/**
 * 挂在 /en-us/*、/zh-cn/*、* 下：把区域前缀从 pathname 剥掉后再渲染子树，
 * 这样内部仍可用绝对路径 /mobile/home、/word 等。
 */
export default function LocaleGate({
  locale,
  children,
}: {
  locale: AppLocale;
  children: React.ReactNode;
}) {
  const location = useLocation();
  const { pathname } = parseLocalePath(location.pathname);

  return (
    <LocaleProvider locale={locale}>
      {/* 通过克隆 children 并注入改写后的 location 不现实；由调用方用 Routes location= 传入 */}
      <LocalePathLocation pathname={pathname}>{children}</LocalePathLocation>
    </LocaleProvider>
  );
}

const PathCtx = React.createContext<string>('/');

export function useLocaleMatchedPathname(): string {
  return React.useContext(PathCtx);
}

function LocalePathLocation({
  pathname,
  children,
}: {
  pathname: string;
  children: React.ReactNode;
}) {
  return <PathCtx.Provider value={pathname}>{children}</PathCtx.Provider>;
}

/** 供内部 <Routes location={...}> 使用的 location 对象 */
export function useStrippedLocation() {
  const location = useLocation();
  const pathname = useLocaleMatchedPathname();
  return React.useMemo(
    () => ({ ...location, pathname }),
    [location, pathname]
  );
}
