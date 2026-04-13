import React from 'react';
import { Button } from 'antd';
import { useNavigate } from 'react-router-dom';

const MobileHomePage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#f5f7fb',
        color: '#333',
        fontSize: '18px',
        gap: '14px'
      }}
    >
      移动端首页（功能建设中）
      <Button type="primary" onClick={() => navigate('/mobile/mbti')}>
        MBTI 测试
      </Button>
    </div>
  );
};

export default MobileHomePage;
