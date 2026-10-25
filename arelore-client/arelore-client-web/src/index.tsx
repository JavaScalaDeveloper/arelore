import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import { BrowserRouter } from 'react-router-dom';
import { syncAuthSession } from './utils/authSession';

// 启动即补齐 token/userToken 别名，墨鱼背词与其它模块共用登录态
syncAuthSession();

// 全局捕获未处理异常（微信内嵌页可配合 vConsole 查看；避免「点了按钮报错但看不到堆栈」）
window.addEventListener('error', (event) => {
  // eslint-disable-next-line no-console
  console.error('[window.error]', event.message, event.error?.stack || event.filename, event.lineno, event.colno);
});
window.addEventListener('unhandledrejection', (event) => {
  const err = event.reason;
  // eslint-disable-next-line no-console
  console.error('[unhandledrejection]', err?.stack || err?.message || err);
});

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <BrowserRouter>
    <App />
  </BrowserRouter>
);
