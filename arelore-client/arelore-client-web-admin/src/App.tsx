import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useParams } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
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

function ProductIndex() {
  const { product } = useParams();
  return <Navigate to={product === 'word' ? 'languages' : 'dashboard'} replace />;
}

function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{
      token: {
        colorPrimary: '#1890ff',
      },
    }}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProductHome />} />
          <Route path="/:product" element={<Layout />}>
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="users" element={<UserManagement />} />
            <Route path="settings" element={<SystemSettings />} />
            <Route path="detection-types" element={<DetectionTypeManagement />} />
            <Route path="detection-questions" element={<DetectionQuestionManagement />} />
            <Route path="languages" element={<WordLanguageManagement />} />
            <Route path="categories" element={<WordCategoryManagement />} />
            <Route path="books" element={<WordBookManagement />} />
            <Route path="entries" element={<WordEntryManagement />} />
            <Route index element={<ProductIndex />} />
          </Route>
        </Routes>
      </Router>
    </ConfigProvider>
  );
}

export default App;
