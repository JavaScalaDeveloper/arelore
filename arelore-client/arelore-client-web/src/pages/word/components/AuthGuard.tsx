import React from 'react';
import { useLocation } from 'react-router-dom';
import { LocaleNavigate, stripLocalePrefix } from '../../../locale';
import { getAuthToken } from '../../../utils/authSession';

export default function AuthGuard({ children }: { children: React.ReactElement }) {
  const location = useLocation();
  const token = getAuthToken();
  if (!token) {
    return (
      <LocaleNavigate
        to="/word/login"
        replace
        state={{ from: stripLocalePrefix(location.pathname) }}
      />
    );
  }
  return children;
}
