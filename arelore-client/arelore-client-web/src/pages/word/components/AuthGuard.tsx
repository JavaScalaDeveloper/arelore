import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { getToken } from '../utils/storage';

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  if (!getToken()) {
    return <Navigate to="/word/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}
