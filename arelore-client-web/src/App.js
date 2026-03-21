import React, { useState } from 'react';
import { Layout, Menu, Button, Avatar, Dropdown, Space, Badge } from 'antd';
import { 
  UserOutlined, 
  SettingOutlined, 
  LogoutOutlined,
  HomeOutlined,
  SolutionOutlined,
  CustomerServiceOutlined,
  PhoneOutlined,
  BellOutlined
} from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import './App.css';

const { Header, Content, Footer } = Layout;

// 导航菜单配置
const navigationItems = [
  { key: 'home', icon: <HomeOutlined />, label: '首页', path: '/' },
  { key: 'products', icon: <SolutionOutlined />, label: '产品中心', path: '/products' },
  { key: 'solutions', icon: <CustomerServiceOutlined />, label: '解决方案', path: '/solutions' },
  { key: 'contact', icon: <PhoneOutlined />, label: '联系我们', path: '/contact' },
];

// 用户菜单配置
const userMenuItems = [
  {
    key: 'profile',
    icon: <UserOutlined />,
    label: '个人中心',
    path: '/user/profile'
  },
  {
    key: 'settings',
    icon: <SettingOutlined />,
    label: '账号设置',
    path: '/user/settings'
  },
  {
    type: 'divider',
  },
  {
    key: 'logout',
    icon: <LogoutOutlined />,
    label: '退出登录',
    onClick: () => console.log('退出登录')
  }
];

function App() {
  const [current, setCurrent] = useState('home');
  const [isLoggedIn, setIsLoggedIn] = useState(false); // 模拟登录状态
  const navigate = useNavigate();

  // 处理导航点击
  const handleNavClick = (e) => {
    setCurrent(e.key);
    const item = navigationItems.find(i => i.key === e.key);
    if (item && item.path) {
      navigate(item.path);
    }
  };

  // 处理用户菜单点击
  const handleUserMenuClick = ({ key }) => {
    const item = userMenuItems.find(i => i.key === key);
    if (item && item.path) {
      navigate(item.path);
    } else if (item && item.onClick) {
      item.onClick();
    }
  };

  // 用户头像下拉菜单
  const userDropdown = (
    <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} trigger={['click']}>
      <Space style={{ cursor: 'pointer' }}>
        <Badge count={3} size="small">
          <Avatar style={{ backgroundColor: '#1890ff' }} icon={<UserOutlined />} />
        </Badge>
        <span style={{ color: '#fff' }}>张三</span>
      </Space>
    </Dropdown>
  );

  return (
    <Layout className="app-layout">
      <Header className="app-header">
        <div className="header-container">
          {/* Logo */}
          <div className="logo" onClick={() => navigate('/')}>
            <span>Arelore</span>
          </div>

          {/* 主导航 */}
          <Menu
            theme="dark"
            mode="horizontal"
            selectedKeys={[current]}
            onClick={handleNavClick}
            className="nav-menu"
          >
            {navigationItems.map(item => (
              <Menu.Item key={item.key} icon={item.icon}>
                {item.label}
              </Menu.Item>
            ))}
          </Menu>

          {/* 右侧功能区 */}
          <div className="header-right">
            {/* 通知图标 */}
            <Badge count={5} size="small" className="notification-badge">
              <BellOutlined className="notification-icon" />
            </Badge>

            {/* 登录/用户中心 */}
            {isLoggedIn ? (
              userDropdown
            ) : (
              <Space>
                <Button 
                  type="primary" 
                  ghost
                  onClick={() => navigate('/login')}
                >
                  登录
                </Button>
                <Button 
                  type="default"
                  onClick={() => navigate('/register')}
                >
                  注册
                </Button>
              </Space>
            )}
          </div>
        </div>
      </Header>

      <Content className="app-content">
        <div className="app-content-wrapper">
          <div className="hero-section">
            <h1 className="hero-title">欢迎来到 Arelore</h1>
            <p className="hero-subtitle">
              创新技术，引领未来<br/>
              为您提供最优质的产品和服务
            </p>
            <Space size="large" className="hero-buttons">
              <Button type="primary" size="large" onClick={() => navigate('/products')}>
                探索产品
              </Button>
              <Button size="large" onClick={() => navigate('/contact')}>
                联系我们
              </Button>
            </Space>
          </div>

          {/* 特性展示区 */}
          <div className="features-section">
            <div className="feature-card">
              <SolutionOutlined className="feature-icon" />
              <h3>丰富的产品线</h3>
              <p>提供多样化的产品选择，满足不同需求</p>
            </div>
            <div className="feature-card">
              <CustomerServiceOutlined className="feature-icon" />
              <h3>专业的服务</h3>
              <p>7x24 小时专业技术支持与服务</p>
            </div>
            <div className="feature-card">
              <PhoneOutlined className="feature-icon" />
              <h3>便捷的联系</h3>
              <p>多渠道沟通，快速响应您的需求</p>
            </div>
          </div>
        </div>
      </Content>

      <Footer className="app-footer">
        <div className="footer-content">
          <p>Arelore ©{new Date().getFullYear()} Created by Arelore Team</p>
          <div className="footer-links">
            <a href="#">关于我们</a>
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
            <a href="#">帮助中心</a>
          </div>
        </div>
      </Footer>
    </Layout>
  );
}

export default App;
