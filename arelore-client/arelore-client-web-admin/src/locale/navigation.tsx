import React, { useCallback } from 'react';
import { Navigate, NavigateProps, useNavigate } from 'react-router-dom';
import type { NavigateOptions, To } from 'react-router-dom';
import { useLocaleOptional } from './LocaleContext';
import { isAppLocale } from './constants';

function withLocalePrefix(localePath: (p: string) => string, to: string): string {
  if (!to.startsWith('/')) {
    return to;
  }
  const q = to.indexOf('?');
  const h = to.indexOf('#');
  let cut = to.length;
  if (q >= 0) cut = Math.min(cut, q);
  if (h >= 0) cut = Math.min(cut, h);
  const path = to.slice(0, cut);
  const suffix = to.slice(cut);

  const parts = path.split('/').filter(Boolean);
  if (parts.length > 0 && isAppLocale(parts[0])) {
    return to;
  }
  return localePath(path) + suffix;
}

export function useLocaleNavigate() {
  const navigate = useNavigate();
  const { localePath } = useLocaleOptional();

  return useCallback(
    (to: To | number, options?: NavigateOptions) => {
      if (typeof to === 'number') {
        navigate(to);
        return;
      }
      if (typeof to === 'string') {
        navigate(withLocalePrefix(localePath, to), options);
        return;
      }
      if (to && typeof to === 'object' && 'pathname' in to && typeof to.pathname === 'string') {
        navigate(
          {
            ...to,
            pathname: withLocalePrefix(localePath, to.pathname),
          },
          options
        );
        return;
      }
      navigate(to as To, options);
    },
    [navigate, localePath]
  );
}

export function LocaleNavigate({ to, ...rest }: NavigateProps) {
  const { localePath } = useLocaleOptional();
  const resolved =
    typeof to === 'string'
      ? withLocalePrefix(localePath, to)
      : to && typeof to === 'object' && 'pathname' in to && typeof to.pathname === 'string'
        ? { ...to, pathname: withLocalePrefix(localePath, to.pathname) }
        : to;
  return <Navigate to={resolved} {...rest} />;
}
