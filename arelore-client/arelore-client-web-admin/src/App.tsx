import React from 'react';
import { BrowserRouter as Router, Routes, Route, useParams } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import enUS from 'antd/locale/en_US';
import Layout from './pages/Layout';
import ProductHome from './pages/ProductHome';
import Dashboard from './pages/Dashboard';
import UserManagement from './pages/UserManagement';
import SystemSettings from './pages/SystemSettings';
import Login from './pages/Login';
import DetectionTypeManagement from './pages/DetectionTypeManagement';
import DetectionQuestionManagement from './pages/DetectionQuestionManagement';
import WordLanguageManagement from './pages/WordLanguageManagement';
import WordCategoryManagement from './pages/WordCategoryManagement';
import WordBookManagement from './pages/WordBookManagement';
import WordEntryManagement from './pages/WordEntryManagement';
import WordBaseInfoManagement from './pages/WordBaseInfoManagement';
import CommonBinaryFileManagement from './pages/CommonBinaryFileManagement';
import WordUserManagement from './pages/WordUserManagement';
import {
  LocaleGate,
  LocaleNavigate,
  useLocale,
  useStrippedLocation,
} from './locale';

function ProductIndex() {
  const { product } = useParams();
  // 相对路径，由当前 /:product 解析，勿加区域前缀
  if (product === 'word') {
    return <LocaleNavigate to="languages" replace />;
  }
  if (product === 'common') {
    return <LocaleNavigate to="binary-files" replace />;
  }
  return <LocaleNavigate to="dashboard" replace />;
}

function ProductUsers() {
  const { product } = useParams();
  return product === 'word' ? <WordUserManagement /> : <UserManagement />;
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
          <Route index element={<ProductIndex />} />
        </Route>
      </Routes>
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
