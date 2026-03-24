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
import { Routes, Route, useNavigate, Navigate } from 'react-router-dom';
import { authApi } from './api/auth';
import WechatLoginModal from './pages/LoginPage';
import TextMaskingPage from './pages/TextMaskingPage';
import FileDetectionPage from './pages/FileDetectionPage';
import { UserInfo } from './types';
import './App.css';

const { Header, Content, Footer } = Layout;

type MenuItem = Required<MenuProps>['items'][number];

// 导航菜单配置
const navigationItems: any[] = [
  { key: 'home', icon: <HomeOutlined />, label: '首页', path: '/home' },
  {
    key: 'products',
    icon: <SolutionOutlined />,
    label: '产品中心',
    children: [
      {
        key: 'databaseSecurity',
        label: '数据库安全',
        children: [
          {
            key: 'databaseClassification',
            label: '数据库分类分级',
            path: '/home/products/database-classification'
          }
        ]
      },
      {
        key: 'apiSecurity',
        label: 'API安全',
        path: '/home/products/api-security'
      }
    ]
  },
  {
    key: 'tools',
    icon: <EyeInvisibleOutlined />,
    label: '免费工具',
    children: [
      {
        key: 'textMasking',
        label: '文本脱敏',
        path: '/home/tools'
      },
      {
        key: 'fileDetection',
        label: '文件敏感内容检测',
        path: '/home/tools/file-detection'
      }
    ]
  },
  { key: 'solutions', icon: <CustomerServiceOutlined />, label: '解决方案', path: '/home/solutions' },
  { key: 'contact', icon: <PhoneOutlined />, label: '联系我们', path: '/home/contact' },
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
    
    // 查找菜单项（包括子菜单）
    const findMenuItem = (items: any[], key: string): any => {
      for (const item of items) {
        if (item.key === key) {
          return item;
        }
        if (item.children) {
          const found = findMenuItem(item.children, key);
          if (found) {
            return found;
          }
        }
      }
      return null;
    };
    
    const item = findMenuItem(navigationItems, e.key);
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
          <div className="logo" onClick={() => navigate('/home')}>
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
            {navigationItems.map(item => {
              if (item.type === 'sub' || ('children' in item && item.children)) {
                return (
                  <Menu.SubMenu key={item.key} icon={item.icon} title={item.label}>
                    {(item.children || []).map((child: any) => {
                      if (child.children) {
                        return (
                          <Menu.SubMenu key={child.key} title={child.label}>
                            {child.children.map((grandchild: any) => (
                              <Menu.Item key={grandchild.key}>
                                {grandchild.label}
                              </Menu.Item>
                            ))}
                          </Menu.SubMenu>
                        );
                      }
                      return (
                        <Menu.Item key={child.key}>
                          {child.label}
                        </Menu.Item>
                      );
                    })}
                  </Menu.SubMenu>
                );
              }
              return (
                <Menu.Item key={item.key} icon={item.icon}>
                  {item.label}
                </Menu.Item>
              );
            })}
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
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/home" element={
            <div className="app-content-wrapper">
              <div className="hero-section">
                <h1 className="hero-title">欢迎来到 Arelore</h1>
                <p className="hero-subtitle">
                  创新技术，引领未来<br/>
                  为您提供最优质的产品和服务
                </p>
                <Space size="large" className="hero-buttons">
                  <Button type="primary" size="large" onClick={() => navigate('/home/products')}>
                    探索产品
                  </Button>
                  <Button size="large" onClick={() => navigate('/home/contact')}>
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
          <Route path="/home/tools" element={<TextMaskingPage />} />
          <Route path="/home/tools/file-detection" element={<FileDetectionPage />} />
          <Route path="/home/products/database-classification" element={
            <div className="app-content-wrapper">
              <div style={{ padding: '24px' }}>
                <h1>数据库分类分级</h1>
                <p style={{ fontSize: '16px', lineHeight: '1.8', marginBottom: '24px' }}>
                  数据库分类分级是一款专业的数据安全管理工具，能够对数据库进行全面扫描，自动检测并识别敏感信息，帮助企业建立完善的数据分类分级体系。
                </p>
                <h2>主要功能</h2>
                <ul style={{ fontSize: '16px', lineHeight: '1.8', marginBottom: '24px' }}>
                  <li><strong>智能扫描：</strong>自动扫描数据库，检测各类敏感信息</li>
                  <li><strong>分类分级：</strong>支持自定义分类分级策略，灵活配置敏感数据级别</li>
                  <li><strong>风险评估：</strong>提供数据安全风险评估报告</li>
                  <li><strong>合规管理：</strong>满足数据安全合规要求</li>
                  <li><strong>可视化展示：</strong>直观展示数据库敏感信息分布情况</li>
                </ul>
                <h2>核心优势</h2>
                <ul style={{ fontSize: '16px', lineHeight: '1.8' }}>
                  <li><strong>高效准确：</strong>快速扫描大型数据库，准确率高</li>
                  <li><strong>灵活配置：</strong>支持自定义规则和策略</li>
                  <li><strong>易于部署：</strong>简单的部署流程，低运维成本</li>
                  <li><strong>安全可靠：</strong>采用先进的数据处理技术，确保数据安全</li>
                </ul>
              </div>
            </div>
          } />
          <Route path="/home/products/api-security" element={
            <div className="app-content-wrapper">
              <div style={{ padding: '24px' }}>
                <h1>API安全</h1>
                <p style={{ fontSize: '16px', lineHeight: '1.8' }}>
                  API安全产品正在开发中，敬请期待...
                </p>
              </div>
            </div>
          } />
          <Route path="/home/contact" element={
            <div className="app-content-wrapper">
              <div style={{ padding: '24px', maxWidth: '800px', margin: '0 auto' }}>
                <h1 style={{ textAlign: 'center', marginBottom: '32px' }}>联系我们</h1>
                <div style={{ 
                  backgroundColor: '#f0f5ff', 
                  padding: '24px', 
                  borderRadius: '8px',
                  textAlign: 'center'
                }}>
                  <p style={{ fontSize: '18px', marginBottom: '16px' }}>
                    <strong>QQ：</strong>544789628
                  </p>
                  <p style={{ fontSize: '18px' }}>
                    <strong>微信：</strong>AiDeepCore
                  </p>
                </div>
              </div>
            </div>
          } />
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
