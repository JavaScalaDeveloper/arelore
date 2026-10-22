import React from 'react';
import { useLocation } from 'react-router-dom';
import { LocaleNavigate, stripLocalePrefix } from '../../../locale';

export default function AuthGuard({ children }: { children: React.ReactElement }) {
  const location = useLocation();
  const token = localStorage.getItem('token');
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
