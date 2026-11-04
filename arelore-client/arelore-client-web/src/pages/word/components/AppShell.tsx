import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { BookOutlined, SettingOutlined, UserOutlined, HomeOutlined } from '@ant-design/icons';
import { RegionSwitcher, stripLocalePrefix, useI18n, useLocale } from '../../../locale';

export default function AppShell() {
  const { localePath, pathnameWithoutLocale } = useLocale();
  const { t } = useI18n();
  const isLogin = pathnameWithoutLocale.startsWith('/word/login')
    || stripLocalePrefix(window.location.pathname).startsWith('/word/login');

  return (
    <div className="app-shell">
      <header className="word-header">
        <div className="word-header-inner">
          <NavLink to={localePath('/word')} className="word-brand" end>
            {t('word.brand')}
          </NavLink>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginLeft: 'auto' }}>
            {!isLogin && (
              <nav className="word-nav">
                <NavLink to={localePath('/word')} end className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  <HomeOutlined />
                  <span>{t('word.nav.home')}</span>
                </NavLink>
                <NavLink to={localePath('/word/books')} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  <BookOutlined />
                  <span>{t('word.nav.books')}</span>
                </NavLink>
                <NavLink to={localePath('/word/settings')} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  <SettingOutlined />
                  <span>{t('word.nav.settings')}</span>
                </NavLink>
                <NavLink to={localePath('/word/profile')} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  <UserOutlined />
                  <span>{t('word.nav.profile')}</span>
                </NavLink>
              </nav>
            )}
            <RegionSwitcher size="small" />
          </div>
        </div>
      </header>
      <main className="app-body">
        <Outlet />
      </main>
    </div>
  );
}
