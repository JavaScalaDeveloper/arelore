import React from 'react';
import { useLocation } from 'react-router-dom';
import { AppLocale } from './constants';
import { LocaleProvider } from './LocaleContext';
import { parseLocalePath } from './path';

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

export function useStrippedLocation() {
  const location = useLocation();
  const pathname = useLocaleMatchedPathname();
  return React.useMemo(
    () => ({ ...location, pathname }),
    [location, pathname]
  );
}
