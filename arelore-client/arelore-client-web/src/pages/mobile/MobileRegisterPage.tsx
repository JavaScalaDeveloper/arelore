import React, { useState } from 'react';
import { Button, Card, Form, Input, message, Space, Typography } from 'antd';
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
              rules={[{ required: true, message: '请输入密码' }, { min: 6, message: '至少6位' }]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
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

