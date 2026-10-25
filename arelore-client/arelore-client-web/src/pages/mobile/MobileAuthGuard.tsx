import React from 'react';
import { useLocation } from 'react-router-dom';
import { LocaleNavigate } from '../../locale';
import { getAuthToken } from '../../utils/authSession';

interface MobileAuthGuardProps {
  children: React.ReactElement;
}

const MobileAuthGuard: React.FC<MobileAuthGuardProps> = ({ children }) => {
  const location = useLocation();
  const token = getAuthToken();

  if (!token) {
    const redirect = encodeURIComponent(`${location.pathname}${location.search}${location.hash}`);
    return <LocaleNavigate to={`/mobile/login?redirect=${redirect}`} replace />;
  }

  return children;
};

export default MobileAuthGuard;
