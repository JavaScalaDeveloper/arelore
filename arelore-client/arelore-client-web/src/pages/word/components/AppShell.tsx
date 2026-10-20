import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { BookOutlined, SettingOutlined, UserOutlined, HomeOutlined } from '@ant-design/icons';

export default function AppShell() {
  const location = useLocation();
  const isLogin = location.pathname.startsWith('/word/login');

  return (
    <div className="app-shell">
      <header className="word-header">
        <div className="word-header-inner">
          <NavLink to="/word" className="word-brand" end>
            Arelore 背单词
          </NavLink>
          {!isLogin && (
            <nav className="word-nav">
              <NavLink to="/word" end className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <HomeOutlined />
                <span>首页</span>
              </NavLink>
              <NavLink to="/word/books" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <BookOutlined />
                <span>词书</span>
              </NavLink>
              <NavLink to="/word/settings" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <SettingOutlined />
                <span>设置</span>
              </NavLink>
              <NavLink to="/word/profile" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <UserOutlined />
                <span>我的</span>
              </NavLink>
            </nav>
          )}
        </div>
      </header>
      <main className="app-body">
        <Outlet />
      </main>
    </div>
  );
}
