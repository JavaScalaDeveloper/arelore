import React, { useCallback, useEffect, useState } from 'react';
import { Button, Form, Input, message, Space } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '../../api/auth';
import { reportClientLog } from '../../utils/clientLogger';
import { sha256Hex } from '../../utils/hash';
import './MobileLoginPage.css';

interface LoginFormData {
  username: string;
  password: string;
}

const MobileLoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState<boolean>(false);

  const getRedirectPath = useCallback(() => {
    const params = new URLSearchParams(location.search);
    const redirect = params.get('redirect');
    if (!redirect) {
      return '/mobile/home';
    }
    try {
      const decodedRedirect = decodeURIComponent(redirect);
      if (!decodedRedirect.startsWith('/mobile') || decodedRedirect.startsWith('/mobile/login')) {
        return '/mobile/home';
      }
      return decodedRedirect;
    } catch (error) {
      return '/mobile/home';
    }
  }, [location.search]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      navigate(getRedirectPath(), { replace: true });
    }
  }, [getRedirectPath, navigate]);

  const onFinish = async (values: LoginFormData) => {
    try {
      setLoading(true);
      let passwordHash = '';
      try {
        passwordHash = await sha256Hex(values.password);
      } catch (hashError: any) {
        await reportClientLog('ERROR', 'mobile login hash failed', hashError?.message || '');
        message.error('当前浏览器环境加密能力异常，请升级后重试');
        return;
      }
      const res = await authApi.mobileLogin({
        username: values.username,
        passwordHash
      });
      if (res.code === 200 && res.data) {
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('currentUser', JSON.stringify(res.data.user));
        message.success('登录成功');
        navigate(getRedirectPath(), { replace: true });
      }
    } catch (error: any) {
      await reportClientLog('ERROR', 'mobile login request failed', error?.message || '');
      message.error(error?.message || '登录失败，请检查账号密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mobile-login-page">
      <div className="mobile-login-card">
        <h2 className="mobile-login-title">移动端登录</h2>
        <p className="mobile-login-subtitle">适用于微信公众号内嵌 Web 页面</p>
        <Form layout="vertical" onFinish={onFinish} autoComplete="off">
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" size="large" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="请输入密码" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block size="large" loading={loading}>
            登录
          </Button>
        </Form>
        <Space style={{ marginTop: 12, width: '100%', justifyContent: 'space-between' }}>
          <Button type="link" onClick={() => navigate('/mobile/forgot-password')}>
            找回密码
          </Button>
          <Button type="link" onClick={() => navigate('/mobile/register')}>
            去注册
          </Button>
        </Space>
      </div>
    </div>
  );
};

export default MobileLoginPage;
