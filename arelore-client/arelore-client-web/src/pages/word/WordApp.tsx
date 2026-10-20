import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './components/AppShell';
import AuthGuard from './components/AuthGuard';
import BooksPage from './pages/BooksPage';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import SettingsPage from './pages/SettingsPage';
import StudyPage from './pages/StudyPage';
import './word.css';

/** 背单词子应用：挂在 /word/*（桌面浏览器交互） */
export default function WordApp() {
  return (
    <div className="word-app-root">
      <Routes>
        <Route path="/word" element={<AppShell />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route
            path="books"
            element={
              <AuthGuard>
                <BooksPage />
              </AuthGuard>
            }
          />
          <Route
            path="study"
            element={
              <AuthGuard>
                <StudyPage />
              </AuthGuard>
            }
          />
          <Route
            path="settings"
            element={
              <AuthGuard>
                <SettingsPage />
              </AuthGuard>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/word" replace />} />
      </Routes>
    </div>
  );
}
