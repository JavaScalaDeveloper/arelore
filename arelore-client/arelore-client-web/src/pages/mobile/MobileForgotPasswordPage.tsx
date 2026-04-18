import React, { useState } from 'react';
import { Button, Card, Form, Input, Progress, Space, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../../api/user';
import { sha256Hex } from '../../utils/hash';

const { Title, Paragraph } = Typography;

interface ForgotPasswordForm {
  mobile: string;
  verifyCode: string;
  newPassword: string;
}

const MobileForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm<ForgotPasswordForm>();
  const [applyLoading, setApplyLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
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

  const handleApplyCode = async () => {
    try {
      const values = await form.validateFields(['mobile']);
      setApplyLoading(true);
      await userApi.mobileResetPasswordApply({ mobile: values.mobile });
      setCodeSent(true);
      message.success('验证码已发送');
    } catch (error: any) {
      message.error(error?.message || '发送失败');
    } finally {
      setApplyLoading(false);
    }
  };

  const handleConfirmReset = async (values: ForgotPasswordForm) => {
    try {
      setSubmitLoading(true);
      const newPasswordHash = await sha256Hex(values.newPassword);
      await userApi.mobileResetPasswordConfirm({
        mobile: values.mobile,
        verifyCode: values.verifyCode,
        newPassword: newPasswordHash
      });
      message.success('密码重置成功，请重新登录');
      navigate('/mobile/login', { replace: true });
    } catch (error: any) {
      message.error(error?.message || '重置失败');
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fb', padding: 16 }}>
      <div style={{ maxWidth: 520, margin: '0 auto' }}>
        <Card>
          <Title level={3} style={{ marginTop: 0 }}>找回密码</Title>
          <Paragraph type="secondary">通过手机号验证码重置登录密码。</Paragraph>
          <Form form={form} layout="vertical" onFinish={handleConfirmReset}>
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
              label="新密码"
              name="newPassword"
              rules={[{ required: true, message: '请输入新密码' }, { min: 8, message: '至少8位' }]}
            >
              <Input.Password
                placeholder="请输入新密码"
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
                <Button type="primary" loading={applyLoading} onClick={handleApplyCode}>
                  获取验证码
                </Button>
              </Space.Compact>
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={submitLoading} disabled={!codeSent} block>
              确认重置
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

export default MobileForgotPasswordPage;
