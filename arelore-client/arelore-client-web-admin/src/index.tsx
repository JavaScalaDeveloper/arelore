import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import { ApiRequestError } from './utils/request';

// 接口错误已在 axios 拦截器中 message 提示，阻止 CRA 开发态全屏报错层
window.addEventListener('unhandledrejection', (event) => {
  const reason = event.reason;
  if (reason instanceof ApiRequestError || (reason && (reason as any).notified)) {
    event.preventDefault();
  }
});

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
