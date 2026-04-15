import React from 'react';
import { Avatar, Button, Dropdown, Space } from 'antd';
import type { MenuProps } from 'antd';
import { LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { Outlet, useNavigate } from 'react-router-dom';

const MobileLayout: React.FC = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('currentUser') || '{}');

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'profile') {
      navigate('/mobile/profile');
      return;
    }

    if (key === 'logout') {
      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
      navigate('/mobile/login', { replace: true });
    }
  };

  const items: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '用户主页'
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录'
    }
  ];

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fb' }}>
      <div
        style={{
          height: 56,
          padding: '0 16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: '#fff',
          borderBottom: '1px solid #f0f0f0'
        }}
      >
        <Button type="link" style={{ padding: 0, fontWeight: 600 }} onClick={() => navigate('/mobile/home')}>
          首页
        </Button>
        <Dropdown menu={{ items, onClick: handleMenuClick }} trigger={['click']}>
          <Space style={{ cursor: 'pointer' }}>
            <Avatar src={user.avatar} icon={!user.avatar && <UserOutlined />} />
          </Space>
        </Dropdown>
      </div>
      <Outlet />
    </div>
  );
};

export default MobileLayout;
