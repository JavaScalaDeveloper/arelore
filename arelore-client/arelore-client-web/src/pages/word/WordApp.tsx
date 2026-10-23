import React from 'react';
import { Route, Routes } from 'react-router-dom';
import AppShell from './components/AppShell';
import AuthGuard from './components/AuthGuard';
import BooksPage from './pages/BooksPage';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import SettingsPage from './pages/SettingsPage';
import StudyPage from './pages/StudyPage';
import WordDocumentSeo from './seo/WordDocumentSeo';
import { LocaleNavigate, useStrippedLocation } from '../../locale';
import './word.css';

/** 墨鱼背词子应用：挂在 /word/*，支持 /en-us/word 等区域前缀 */
export default function WordApp() {
  const strippedLocation = useStrippedLocation();

  return (
    <div className="word-app-root">
      <WordDocumentSeo />
      <Routes location={strippedLocation}>
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
        <Route path="*" element={<LocaleNavigate to="/word" replace />} />
      </Routes>
    </div>
  );
}
