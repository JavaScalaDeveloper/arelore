import React, { useState } from 'react';
import { Button, Card, Form, Input, Progress, message, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../../api/user';

const { Title, Paragraph } = Typography;

interface RegisterForm {
  mobile: string;
  password: string;
  verifyCode: string;
}

const MobileRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm<RegisterForm>();
  const [applyLoading, setApplyLoading] = useState(false);
  const [verifyLoading, setVerifyLoading] = useState(false);
  const [codeSent, setCodeSent] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState<'LOW' | 'MEDIUM' | 'HIGH' | ''>('');

  const getPasswordStrength = (password?: string): 'LOW' | 'MEDIUM' | 'HIGH' | '' => {
    if (!password) {
      return '';
    }
    const hasLetter = /[A-Za-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSymbol = /[^A-Za-z0-9]/.test(password);
    const categoryCount = [hasLetter, hasNumber, hasSymbol].filter(Boolean).length;
    if (password.length >= 10 && categoryCount >= 3) {
      return 'HIGH';
    }
    if (password.length >= 8 && categoryCount >= 2) {
      return 'MEDIUM';
    }
    return 'LOW';
  };

  const renderPasswordStrengthText = () => {
    if (!passwordStrength) {
      return null;
    }
    if (passwordStrength === 'HIGH') {
      return <span style={{ color: '#389e0d' }}>密码强度：高（推荐）</span>;
    }
    if (passwordStrength === 'MEDIUM') {
      return <span style={{ color: '#d48806' }}>密码强度：中（建议再加入符号或增加长度）</span>;
    }
    return <span style={{ color: '#cf1322' }}>密码强度：低（建议同时包含字母、数字、符号）</span>;
  };

  const getPasswordStrengthProgress = () => {
    if (!passwordStrength) {
      return null;
    }
    if (passwordStrength === 'HIGH') {
      return { percent: 100, strokeColor: '#52c41a' };
    }
    if (passwordStrength === 'MEDIUM') {
      return { percent: 66, strokeColor: '#faad14' };
    }
    return { percent: 33, strokeColor: '#ff4d4f' };
  };

  const handleApply = async () => {
    try {
      const values = await form.validateFields(['mobile', 'password']);
      setApplyLoading(true);
      await userApi.mobileRegisterApply({
        mobile: values.mobile,
        password: values.password
      });
      setCodeSent(true);
      message.success('验证码已发送');
    } catch (error: any) {
      message.error(error?.message || '发送失败');
    } finally {
      setApplyLoading(false);
    }
  };

  const handleVerify = async (values: RegisterForm) => {
    try {
      setVerifyLoading(true);
      const res = await userApi.mobileRegisterVerify({
        mobile: values.mobile,
        verifyCode: values.verifyCode
      });
      message.success(`注册成功，userId=${res.data?.userId || ''}`);
      navigate('/mobile/login', { replace: true });
    } catch (error: any) {
      message.error(error?.message || '校验失败');
    } finally {
      setVerifyLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fb', padding: 16 }}>
      <div style={{ maxWidth: 520, margin: '0 auto' }}>
        <Card>
          <Title level={3} style={{ marginTop: 0 }}>手机号注册</Title>
          <Paragraph type="secondary">输入手机号与密码，获取验证码后完成注册。</Paragraph>

          <Form form={form} layout="vertical" onFinish={handleVerify} initialValues={{ verifyCode: '' }}>
            <Form.Item
              label="手机号"
              name="mobile"
              rules={[
                { required: true, message: '请输入手机号' },
                { pattern: /^1\d{10}$/, message: '手机号格式不正确' }
              ]}
            >
              <Input placeholder="请输入手机号" maxLength={11} />
            </Form.Item>
            <Form.Item
              label="密码"
              name="password"
              extra={renderPasswordStrengthText()}
              rules={[{ required: true, message: '请输入密码' }, { min: 8, message: '至少8位' }]}
            >
              <Input.Password
                placeholder="请输入密码"
                onChange={(e) => setPasswordStrength(getPasswordStrength(e.target.value))}
              />
            </Form.Item>
            {passwordStrength ? (
              <Progress
                percent={getPasswordStrengthProgress()?.percent}
                showInfo={false}
                strokeColor={getPasswordStrengthProgress()?.strokeColor}
                size="small"
                style={{ marginTop: -6, marginBottom: 12 }}
              />
            ) : null}
            <Form.Item
              label="验证码"
              name="verifyCode"
              rules={[{ required: true, message: '请输入验证码' }]}
            >
              <Space.Compact style={{ display: 'flex', width: '100%' }}>
                <Input placeholder="请输入验证码" maxLength={8} style={{ flex: 1 }} />
                <Button type="primary" onClick={handleApply} loading={applyLoading}>
                  获取验证码
                </Button>
              </Space.Compact>
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={verifyLoading} disabled={!codeSent} block>
              提交注册
            </Button>
          </Form>

          <Space style={{ marginTop: 12 }}>
            <Button type="link" onClick={() => navigate('/mobile/login')}>返回登录</Button>
          </Space>
        </Card>
      </div>
    </div>
  );
};

export default MobileRegisterPage;

