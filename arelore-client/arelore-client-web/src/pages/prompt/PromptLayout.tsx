import React, { useMemo, useState } from 'react';
import { Avatar, Dropdown, Layout as AntLayout, Menu, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import {
  FileTextOutlined,
  TagsOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  HomeOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Outlet } from 'react-router-dom';
import {
  RegionSwitcher,
  stripLocalePrefix,
  useLocaleNavigate,
  useStrippedLocation,
} from '../../locale';
import { clearAuthSession, getAuthUserRaw } from '../../utils/authSession';

const { Header, Sider, Content } = AntLayout;

const MENU_ITEMS: MenuProps['items'] = [
  {
    key: '/home/products/ai-prompt/list',
    icon: <FileTextOutlined />,
    label: '提示词列表',
  },
  {
    key: '/home/products/ai-prompt/tags',
    icon: <TagsOutlined />,
    label: '标签管理',
  },
];

function readDisplayUser(): { name: string; email: string } {
  try {
    const raw = getAuthUserRaw();
    if (!raw) {
      return { name: '未登录', email: '' };
    }
    const u = JSON.parse(raw);
    const name =
      u.nickname || u.nickName || u.username || u.email || u.mobile || '用户';
    const email = u.email || u.username || u.mobile || '';
    return { name: String(name), email: email && email !== name ? String(email) : '' };
  } catch {
    return { name: '用户', email: '' };
  }
}

const PromptLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useLocaleNavigate();
  const location = useStrippedLocation();
  const user = useMemo(() => readDisplayUser(), []);
  const selectedKey = useMemo(() => {
    const path = stripLocalePrefix(location.pathname);
    if (path.startsWith('/home/products/ai-prompt/tags')) {
      return '/home/products/ai-prompt/tags';
    }
    return '/home/products/ai-prompt/list';
  }, [location.pathname]);

  const onLogout = () => {
    clearAuthSession();
    navigate('/login?redirect=/home/products/ai-prompt/list', { replace: true });
  };

  const userMenu: MenuProps['items'] = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: onLogout,
    },
  ];

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark" width={220}>
        <div
          style={{
            height: 64,
            margin: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 700,
            fontSize: collapsed ? 14 : 16,
            borderBottom: '1px solid rgba(255,255,255,0.1)',
            cursor: 'pointer',
            padding: '0 12px',
            textAlign: 'center',
          }}
          onClick={() => navigate('/home/products/ai-prompt/list')}
        >
          {collapsed ? 'AI' : 'AI 提示词管理'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={MENU_ITEMS}
          onClick={({ key }) => navigate(String(key))}
        />
      </Sider>
      <AntLayout>
        <Header
          style={{
            padding: '0 20px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,21,41,.08)',
          }}
        >
          {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
            onClick: () => setCollapsed(!collapsed),
            style: { fontSize: 18, cursor: 'pointer' },
          })}
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <RegionSwitcher size="middle" />
            <span
              style={{ color: '#1677ff', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 4 }}
              onClick={() => navigate('/home')}
            >
              <HomeOutlined /> 返回官网
            </span>
            <Dropdown menu={{ items: userMenu }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }} size={8}>
                <Avatar size="small" icon={<UserOutlined />} style={{ background: '#1677ff' }} />
                <span style={{ display: 'inline-flex', flexDirection: 'column', lineHeight: 1.2 }}>
                  <Typography.Text strong style={{ fontSize: 13 }}>
                    {user.name}
                  </Typography.Text>
                  {user.email ? (
                    <Typography.Text type="secondary" style={{ fontSize: 11 }}>
                      {user.email}
                    </Typography.Text>
                  ) : null}
                </span>
              </Space>
            </Dropdown>
          </div>
        </Header>
        <Content
          style={{
            margin: '16px',
            padding: 24,
            background: '#fff',
            borderRadius: 4,
            minHeight: 360,
          }}
        >
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
};

export default PromptLayout;
