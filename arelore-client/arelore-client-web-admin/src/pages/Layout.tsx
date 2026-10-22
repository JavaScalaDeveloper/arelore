import React, { useEffect, useMemo, useState } from 'react';
import { Outlet, useLocation, useParams } from 'react-router-dom';
import { Layout as AntLayout, Menu } from 'antd';
import type { MenuProps } from 'antd';
import {
  DashboardOutlined,
  UserOutlined,
  SettingOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  AppstoreOutlined,
  BookOutlined,
  DatabaseOutlined,
} from '@ant-design/icons';
import { RegionSwitcher, stripLocalePrefix, useLocaleNavigate } from '../locale';

type MenuItem = Required<MenuProps>['items'][number];

const { Header, Sider, Content, Footer } = AntLayout;

const PRODUCT_KEYS = ['cert', 'word', 'common'] as const;
type ProductKey = (typeof PRODUCT_KEYS)[number];

const PRODUCT_LABEL: Record<ProductKey, string> = {
  cert: '考证宝',
  word: '背单词',
  common: '公共基础',
};

const CERT_MENUS: MenuItem[] = [
  { key: '/cert/dashboard', icon: <DashboardOutlined />, label: '控制台' },
  { key: '/cert/users', icon: <UserOutlined />, label: '用户管理' },
  { key: '/cert/settings', icon: <SettingOutlined />, label: '系统设置' },
  { key: '/cert/detection-types', icon: <FileTextOutlined />, label: '题目类型管理' },
  { key: '/cert/detection-questions', icon: <FileTextOutlined />, label: '题目管理' },
];

const WORD_MENUS: MenuItem[] = [
  { key: '/word/languages', icon: <BookOutlined />, label: '单词语种' },
  { key: '/word/categories', icon: <FileTextOutlined />, label: '单词本分类' },
  { key: '/word/books', icon: <FileTextOutlined />, label: '单词本' },
  { key: '/word/entries', icon: <FileTextOutlined />, label: '单词本词条' },
  { key: '/word/base-info', icon: <FileTextOutlined />, label: '单词基础信息' },
  { key: '/word/users', icon: <UserOutlined />, label: '学习用户' },
];

const COMMON_MENUS: MenuItem[] = [
  { key: '/common/binary-files', icon: <DatabaseOutlined />, label: '二进制文件' },
];

const MENUS_BY_PRODUCT: Record<ProductKey, MenuItem[]> = {
  cert: CERT_MENUS,
  word: WORD_MENUS,
  common: COMMON_MENUS,
};

function isProductKey(value: string | undefined): value is ProductKey {
  return !!value && (PRODUCT_KEYS as readonly string[]).includes(value);
}

const Layout: React.FC = () => {
  const [collapsed, setCollapsed] = useState<boolean>(false);
  const navigate = useLocaleNavigate();
  const location = useLocation();
  const { product } = useParams<{ product: string }>();
  const currentProduct = isProductKey(product) ? product : null;
  const menuItems = currentProduct ? MENUS_BY_PRODUCT[currentProduct] : [];
  const selectedKey = useMemo(
    () => stripLocalePrefix(location.pathname),
    [location.pathname]
  );

  useEffect(() => {
    if (!isProductKey(product)) {
      navigate('/', { replace: true });
    }
  }, [product, navigate]);

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark">
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: collapsed ? 16 : 18,
            fontWeight: 'bold',
            borderBottom: '1px solid rgba(255,255,255,0.1)',
            cursor: 'pointer',
          }}
          onClick={() => navigate('/')}
        >
          {collapsed ? 'A' : `Arelore · ${currentProduct ? PRODUCT_LABEL[currentProduct] : ''}`}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={({ key }) => navigate(String(key))}
        />
      </Sider>
      <AntLayout>
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,21,41,.08)',
          }}
        >
          {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
            className: 'trigger',
            onClick: () => setCollapsed(!collapsed),
            style: { fontSize: 18, cursor: 'pointer' },
          })}
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <RegionSwitcher size="middle" />
            <span
              style={{ color: '#1677ff', cursor: 'pointer' }}
              onClick={() => navigate('/')}
            >
              <AppstoreOutlined /> 切换产品
            </span>
            <span style={{ color: '#666' }}>管理员</span>
            <LogoutOutlined
              style={{ cursor: 'pointer', color: '#ff4d4f' }}
              onClick={() => {
                localStorage.removeItem('adminToken');
                localStorage.removeItem('adminUser');
                navigate('/login');
              }}
            />
          </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            background: '#fff',
            borderRadius: 4,
            minHeight: 280,
          }}
        >
          <Outlet />
        </Content>
        <Footer style={{ textAlign: 'center', color: '#999' }}>
          Arelore 管理后台 ©{new Date().getFullYear()} Created by Arelore Team
        </Footer>
      </AntLayout>
    </AntLayout>
  );
};

export default Layout;
