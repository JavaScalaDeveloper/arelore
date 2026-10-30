import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, useParams } from 'react-router-dom';
import { ConfigProvider, Spin } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import enUS from 'antd/locale/en_US';
import {
  LocaleGate,
  LocaleNavigate,
  useLocale,
  useStrippedLocation,
} from './locale';

const Layout = lazy(() => import('./pages/Layout'));
const ProductHome = lazy(() => import('./pages/ProductHome'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const UserManagement = lazy(() => import('./pages/UserManagement'));
const SystemSettings = lazy(() => import('./pages/SystemSettings'));
const Login = lazy(() => import('./pages/Login'));
const DetectionTypeManagement = lazy(() => import('./pages/DetectionTypeManagement'));
const DetectionQuestionManagement = lazy(() => import('./pages/DetectionQuestionManagement'));
const WordLanguageManagement = lazy(() => import('./pages/WordLanguageManagement'));
const WordCategoryManagement = lazy(() => import('./pages/WordCategoryManagement'));
const WordBookManagement = lazy(() => import('./pages/WordBookManagement'));
const WordEntryManagement = lazy(() => import('./pages/WordEntryManagement'));
const WordBaseInfoManagement = lazy(() => import('./pages/WordBaseInfoManagement'));
const CommonBinaryFileManagement = lazy(() => import('./pages/CommonBinaryFileManagement'));
const CommonUserDailyVisitManagement = lazy(() => import('./pages/CommonUserDailyVisitManagement'));
const WordUserManagement = lazy(() => import('./pages/WordUserManagement'));
const PromptPrefManagement = lazy(() => import('./pages/PromptPrefManagement'));
const PromptVisitManagement = lazy(() => import('./pages/PromptVisitManagement'));
const PromptUserPromptManagement = lazy(() => import('./pages/PromptUserPromptManagement'));

function ProductIndex() {
  const { product } = useParams();
  if (product === 'word') {
    return <LocaleNavigate to="languages" replace />;
  }
  if (product === 'common') {
    return <LocaleNavigate to="binary-files" replace />;
  }
  if (product === 'prompt') {
    return <LocaleNavigate to="prefs" replace />;
  }
  return <LocaleNavigate to="dashboard" replace />;
}

function ProductUsers() {
  const { product } = useParams();
  return product === 'word' ? <WordUserManagement /> : <UserManagement />;
}

function RouteFallback() {
  return (
    <div style={{ padding: 48, textAlign: 'center' }}>
      <Spin size="large" />
    </div>
  );
}

function AdminRoutes() {
  const strippedLocation = useStrippedLocation();
  const { locale } = useLocale();
  const antdLocale = locale === 'en-us' ? enUS : zhCN;

  return (
    <ConfigProvider
      locale={antdLocale}
      theme={{
        token: {
          colorPrimary: '#1890ff',
        },
      }}
    >
      <Suspense fallback={<RouteFallback />}>
        <Routes location={strippedLocation}>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProductHome />} />
          <Route path="/:product" element={<Layout />}>
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="users" element={<ProductUsers />} />
            <Route path="settings" element={<SystemSettings />} />
            <Route path="detection-types" element={<DetectionTypeManagement />} />
            <Route path="detection-questions" element={<DetectionQuestionManagement />} />
            <Route path="languages" element={<WordLanguageManagement />} />
            <Route path="categories" element={<WordCategoryManagement />} />
            <Route path="books" element={<WordBookManagement />} />
            <Route path="entries" element={<WordEntryManagement />} />
            <Route path="base-info" element={<WordBaseInfoManagement />} />
            <Route path="binary-files" element={<CommonBinaryFileManagement />} />
            <Route path="user-daily-visits" element={<CommonUserDailyVisitManagement />} />
            <Route path="prefs" element={<PromptPrefManagement />} />
            <Route path="visits" element={<PromptVisitManagement />} />
            <Route path="user-prompts" element={<PromptUserPromptManagement />} />
            <Route index element={<ProductIndex />} />
          </Route>
        </Routes>
      </Suspense>
    </ConfigProvider>
  );
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/en-us/*" element={<LocaleGate locale="en-us"><AdminRoutes /></LocaleGate>} />
        <Route path="/zh-cn/*" element={<LocaleGate locale="zh-cn"><AdminRoutes /></LocaleGate>} />
        <Route path="*" element={<LocaleGate locale="zh-cn"><AdminRoutes /></LocaleGate>} />
      </Routes>
    </Router>
  );
}

export default App;
