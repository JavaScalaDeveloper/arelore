import React from 'react';
import { Route, Routes } from 'react-router-dom';
import { LocaleNavigate, useStrippedLocation } from '../../locale';
import { getAuthToken, syncAuthSession } from '../../utils/authSession';
import PromptLayout from './PromptLayout';
import PromptListPage from './PromptListPage';
import PromptTagPage from './PromptTagPage';

/** AI 提示词管理子应用：管理端样式，挂在 /home/products/ai-prompt/* */
export default function PromptApp() {
  const strippedLocation = useStrippedLocation();
  syncAuthSession();
  const token = getAuthToken();
  if (!token) {
    const redirect = encodeURIComponent(
      `${strippedLocation.pathname}${strippedLocation.search || ''}`
    );
    return <LocaleNavigate to={`/login?redirect=${redirect}`} replace />;
  }

  return (
    <Routes location={strippedLocation}>
      <Route path="/home/products/ai-prompt" element={<PromptLayout />}>
        <Route index element={<LocaleNavigate to="/home/products/ai-prompt/list" replace />} />
        <Route path="list" element={<PromptListPage />} />
        <Route path="tags" element={<PromptTagPage />} />
      </Route>
      <Route
        path="*"
        element={<LocaleNavigate to="/home/products/ai-prompt/list" replace />}
      />
    </Routes>
  );
}
