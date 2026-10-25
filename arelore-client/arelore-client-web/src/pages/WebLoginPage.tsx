import React from 'react';
import WordLoginPage from './word/pages/LoginPage';
import './word/word.css';

/**
 * 站点统一登录页：复用墨鱼背词邮箱密码/验证码登录界面。
 * 支持 ?redirect=/path 登录成功后跳转；默认回 /home。
 */
const WebLoginPage: React.FC = () => {
  return (
    <div className="word-app-root" style={{ minHeight: 'calc(100vh - 64px)' }}>
      <WordLoginPage successFallback="/home" />
    </div>
  );
};

export default WebLoginPage;
