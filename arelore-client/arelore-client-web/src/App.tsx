import React, { useState, useEffect } from 'react';
import { Layout, Menu, Button, Avatar, Dropdown, Space, Badge, message } from 'antd';
import type { MenuProps } from 'antd';
import { 
  UserOutlined, 
  SettingOutlined, 
  LogoutOutlined,
  HomeOutlined,
  SolutionOutlined,
  CustomerServiceOutlined,
  PhoneOutlined,
  BellOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons';
import { Routes, Route, useNavigate } from 'react-router-dom';
import { authApi } from './api/auth';
import WechatLoginModal from './pages/LoginPage';
import TextMaskingPage from './pages/TextMaskingPage';
import { UserInfo } from './types';
import './App.css';

const { Header, Content, Footer } = Layout;

type MenuItem = Required<MenuProps>['items'][number];

// 导航菜单配置
const navigationItems: { key: string; icon: React.ReactNode; label: string; path: string }[] = [
  { key: 'home', icon: <HomeOutlined />, label: '首页', path: '/' },
  { key: 'products', icon: <SolutionOutlined />, label: '产品中心', path: '/products' },
  { key: 'tools', icon: <EyeInvisibleOutlined />, label: '工具', path: '/tools' },
  { key: 'solutions', icon: <CustomerServiceOutlined />, label: '解决方案', path: '/solutions' },
  { key: 'contact', icon: <PhoneOutlined />, label: '联系我们', path: '/contact' },
];

// 用户菜单配置（动态）
const getUserMenuItems = (onLogout: () => void): MenuItem[] => [
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
  } as any,
  {
    key: 'logout',
    icon: <LogoutOutlined />,
    label: '退出登录',
    onClick: onLogout
  }
];

function App() {
  const [current, setCurrent] = useState<string>('home');
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<UserInfo | null>(null);
  const [showLoginModal, setShowLoginModal] = useState<boolean>(false);
  const navigate = useNavigate();

  // 初始化时检查登录状态
  useEffect(() => {
    checkLoginStatus();
  }, []);

  // 检查登录状态
  const checkLoginStatus = () => {
    const token = localStorage.getItem('userToken');
    const userStr = localStorage.getItem('currentUser');
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        setCurrentUser(user);
        setIsLoggedIn(true);
      } catch (error) {
        console.error('解析用户信息失败:', error);
        localStorage.removeItem('userToken');
        localStorage.removeItem('currentUser');
        setIsLoggedIn(false);
      }
    }
  };

  // 处理导航点击
  const handleNavClick = (e: { key: string }) => {
    setCurrent(e.key);
    const item = navigationItems.find(i => i.key === e.key);
    if (item && item.path) {
      navigate(item.path);
    }
  };

  // 处理用户菜单点击
  const handleUserMenuClick = ({ key }: { key: string }) => {
    const items = getUserMenuItems(handleLogout);
    const item = items?.find(i => i && 'key' in i && i.key === key) as any;
    if (item && item.path) {
      navigate(item.path);
    } else if (item && item.onClick) {
      item.onClick();
    }
  };

  // 打开登录弹窗
  const handleOpenLogin = () => {
    setShowLoginModal(true);
  };

  // 关闭登录弹窗
  const handleCloseLogin = () => {
    setShowLoginModal(false);
  };

  // 处理登录成功
  const handleLoginSuccess = (user: UserInfo) => {
    setCurrentUser(user);
    setIsLoggedIn(true);
    message.success(`欢迎回来，${user.nickname || user.username}！`);
  };

  // 处理退出登录
  const handleLogout = async () => {
    try {
      // 调用后端退出接口
      await authApi.logout();
      
      // 清除本地数据
      localStorage.removeItem('userToken');
      localStorage.removeItem('currentUser');
      
      setCurrentUser(null);
      setIsLoggedIn(false);
      message.success('已退出登录');
      
      // 跳转到首页
      navigate('/');
    } catch (error) {
      console.error('退出登录失败:', error);
      // 即使后端失败，也清除本地数据
      localStorage.removeItem('userToken');
      localStorage.removeItem('currentUser');
      setCurrentUser(null);
      setIsLoggedIn(false);
      message.success('已退出登录');
    }
  };

  // 用户头像下拉菜单
  const userDropdown = (
    <Dropdown menu={{ items: getUserMenuItems(handleLogout), onClick: handleUserMenuClick }} trigger={['click']}>
      <Space style={{ cursor: 'pointer' }}>
        <Badge count={3} size="small">
          <Avatar 
            src={currentUser?.avatar} 
            style={{ backgroundColor: currentUser?.avatar ? '#fff' : '#1890ff' }} 
            icon={!currentUser?.avatar && <UserOutlined />}
          />
        </Badge>
        <span style={{ color: '#fff' }}>{currentUser?.nickname || currentUser?.username || '用户'}</span>
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
            {/* 通知图标 - 暂时隐藏 */}
            {/* <Badge count={5} size="small" className="notification-badge">
              <BellOutlined className="notification-icon" />
            </Badge> */}

            {/* 登录/用户中心 - 暂时隐藏 */}
            {/* {isLoggedIn ? (
              userDropdown
            ) : (
              <Space>
                <Button 
                  type="primary" 
                  ghost
                  onClick={handleOpenLogin}
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
            )} */}
          </div>
        </div>
      </Header>

      <Content className="app-content">
        <Routes>
          <Route path="/" element={
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
          } />
          <Route path="/tools" element={<TextMaskingPage />} />
        </Routes>
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

      {/* 微信登录弹窗 */}
      <WechatLoginModal 
        visible={showLoginModal}
        onCancel={handleCloseLogin}
        onLoginSuccess={handleLoginSuccess}
      />
    </Layout>
  );
}

export default App;
