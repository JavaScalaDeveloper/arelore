import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Form, Input, Progress, Space, Tabs, Typography, message } from 'antd';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { useSearchParams } from 'react-router-dom';
import { stripLocalePrefix, useLocaleNavigate } from '../../../locale';
import { authApi } from '../api/auth';
import { sha256Hex } from '../../../utils/hash';
import { passwordStrengthView } from '../../../utils/passwordStrength';
import { pushLocalPreference } from '../utils/preference';
import { setToken, setUser, getToken, getUser } from '../utils/storage';
import { shortErrorMessage } from '../utils/request';

const { Title, Paragraph, Text, Link } = Typography;

const CODE_COOLDOWN_SEC = 90;

type Mode = 'password' | 'code' | 'register' | 'reset';

export interface LoginPageProps {
  /** 无 redirect 参数时的登录成功落点；站点 /login 传 /home，背词默认 /word */
  successFallback?: string;
}

function parseMode(raw: string | null): Mode {
  if (raw === 'code' || raw === 'register' || raw === 'reset' || raw === 'password') {
    return raw;
  }
  return 'password';
}

function resolveRedirect(raw: string | null, fallback: string): string {
  if (!raw) {
    return fallback;
  }
  try {
    const decoded = decodeURIComponent(raw);
    const bare = stripLocalePrefix(decoded.split('?')[0].split('#')[0]);
    if (!bare || bare === '/login' || bare.startsWith('/login/') || bare.startsWith('/word/login')) {
      return fallback;
    }
    return decoded.startsWith('/') ? decoded : `/${decoded}`;
  } catch {
    return fallback;
  }
}

export default function LoginPage({ successFallback = '/word' }: LoginPageProps) {
  const navigate = useLocaleNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [form] = Form.useForm();
  const [mode, setMode] = useState<Mode>(() => parseMode(searchParams.get('mode')));
  const [sending, setSending] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [passwordDraft, setPasswordDraft] = useState('');
  const strength = useMemo(() => passwordStrengthView(passwordDraft), [passwordDraft]);

  const successPath = useMemo(
    () => resolveRedirect(searchParams.get('redirect'), successFallback),
    [searchParams, successFallback]
  );

  const switchMode = (next: Mode) => {
    setMode(next);
    setSearchParams(
      (prev) => {
        const nextParams = new URLSearchParams(prev);
        if (next === 'password') {
          nextParams.delete('mode');
        } else {
          nextParams.set('mode', next);
        }
        return nextParams;
      },
      { replace: true }
    );
  };

  const finishLogin = useCallback(
    async (token: string, user: any) => {
      // setToken/setUser 会同步写入 token+userToken、word_current_user+currentUser
      setToken(token);
      setUser(user || {});
      try {
        await pushLocalPreference();
      } catch {
        /* ignore */
      }
      message.success('登录成功');
      navigate(successPath, { replace: true });
    },
    [navigate, successPath]
  );

  useEffect(() => {
    const next = parseMode(searchParams.get('mode'));
    setMode(next);
  }, [searchParams]);

  useEffect(() => {
    // 已登录时允许进入「设置/找回密码」；其它模式跳到目标页
    if (getToken() && mode !== 'reset') {
      navigate(successPath, { replace: true });
    }
  }, [navigate, mode, successPath]);

  useEffect(() => {
    if (countdown <= 0) {
      return undefined;
    }
    const timer = window.setTimeout(() => setCountdown((c) => c - 1), 1000);
    return () => window.clearTimeout(timer);
  }, [countdown]);

  useEffect(() => {
    form.resetFields(['code', 'password', 'newPassword', 'confirmPassword']);
    setPasswordDraft('');
    setCountdown(0);
    if (mode === 'reset') {
      const u: any = getUser();
      const email = (u?.username || u?.email || '').trim();
      if (email.includes('@')) {
        form.setFieldsValue({ email });
      }
    }
  }, [mode, form]);

  const renderPasswordStrength = () => {
    if (!strength) {
      return null;
    }
    return (
      <div className="password-strength" style={{ marginTop: 8 }}>
        <Progress
          percent={strength.percent}
          showInfo={false}
          size="small"
          strokeColor={strength.strokeColor}
          trailColor="#f0f0f0"
        />
        <Text style={{ color: strength.color, fontSize: 12 }}>{strength.label}</Text>
        <div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            仅作提示，不强制大小写或特殊字符；至少 8 位即可提交
          </Text>
        </div>
      </div>
    );
  };

  const onSendCode = async () => {
    try {
      await form.validateFields(['email']);
      const email = String(form.getFieldValue('email') || '').trim();
      setSending(true);
      if (mode === 'register') {
        await authApi.applyEmailRegisterCode(email);
      } else if (mode === 'reset') {
        await authApi.applyEmailResetPasswordCode(email);
      } else {
        await authApi.applyEmailCode(email);
      }
      message.success('验证码已发送，请查收邮件（含垃圾箱）');
      setCountdown(CODE_COOLDOWN_SEC);
    } catch (e: any) {
      if (e?.errorFields) {
        return;
      }
      message.error(shortErrorMessage(e, '发送失败'));
    } finally {
      setSending(false);
    }
  };

  const hashPassword = async (plain: string): Promise<string | null> => {
    try {
      return await sha256Hex(plain);
    } catch {
      message.error('当前环境无法完成密码摘要，请更换浏览器后重试');
      return null;
    }
  };

  const onSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      const email = String(values.email || '').trim();
      if (mode === 'password') {
        const passwordHash = await hashPassword(String(values.password || ''));
        if (!passwordHash) {
          return;
        }
        const res = await authApi.emailPasswordLogin({ email, passwordHash });
        const token = res.data?.token || '';
        if (!token) {
          message.error('登录失败：未返回 token');
          return;
        }
        await finishLogin(token, res.data?.user);
        return;
      }
      if (mode === 'code') {
        const res = await authApi.emailCodeLogin({
          email,
          code: String(values.code || '').trim(),
        });
        const token = res.data?.token || '';
        if (!token) {
          message.error('登录失败：未返回 token');
          return;
        }
        await finishLogin(token, res.data?.user);
        return;
      }
      if (mode === 'register') {
        const passwordHash = await hashPassword(String(values.password || ''));
        if (!passwordHash) {
          return;
        }
        const res = await authApi.confirmEmailRegister({
          email,
          code: String(values.code || '').trim(),
          passwordHash,
        });
        const token = res.data?.token || '';
        if (!token) {
          message.error('注册失败：未返回 token');
          return;
        }
        message.success('注册成功');
        await finishLogin(token, res.data?.user);
        return;
      }
      const newPasswordHash = await hashPassword(String(values.newPassword || ''));
      if (!newPasswordHash) {
        return;
      }
      await authApi.confirmEmailResetPassword({
        email,
        code: String(values.code || '').trim(),
        newPasswordHash,
      });
      message.success(getToken() ? '密码已更新' : '密码已更新，请使用新密码登录');
      form.setFieldsValue({
        password: undefined,
        code: undefined,
        newPassword: undefined,
        confirmPassword: undefined,
      });
      if (getToken()) {
        navigate('/word/profile', { replace: true });
      } else {
        switchMode('password');
      }
    } catch (e: any) {
      message.error(shortErrorMessage(e, mode === 'register' ? '注册失败' : mode === 'reset' ? '改密失败' : '登录失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const titleMap: Record<Mode, string> = {
    password: '邮箱密码登录',
    code: '验证码登录',
    register: '邮箱注册',
    reset: '找回 / 设置密码',
  };

  const tipMap: Record<Mode, string> = {
    password: '使用邮箱作为账号；未设密码请先找回/设置密码',
    code: '验证码登录可自动注册；无密码账号请之后设置密码',
    register: '验证邮箱后设置密码，注册成功即登录',
    reset: '已注册用户（含无密码）可通过验证码设置新密码',
  };

  return (
    <div className="page login-page">
      <div className="login-card">
        <MailOutlined style={{ fontSize: 42, color: '#3b82f6' }} />
        <Title level={3} style={{ marginTop: 12 }}>
          {titleMap[mode]}
        </Title>
        <Paragraph type="secondary">{tipMap[mode]}</Paragraph>

        {(mode === 'password' || mode === 'code') && (
          <Tabs
            activeKey={mode}
            onChange={(key) => switchMode(key as Mode)}
            items={[
              { key: 'password', label: '密码登录' },
              { key: 'code', label: '验证码登录' },
            ]}
            style={{ marginBottom: 8 }}
          />
        )}

        <Form form={form} layout="vertical" onFinish={onSubmit} style={{ textAlign: 'left', marginTop: 8 }}>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '邮箱格式不正确' },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="name@example.com" size="large" autoComplete="email" />
          </Form.Item>

          {mode === 'password' && (
            <Form.Item
              name="password"
              label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码至少 8 位' },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="至少 8 位"
                size="large"
                autoComplete="current-password"
              />
            </Form.Item>
          )}

          {(mode === 'code' || mode === 'register' || mode === 'reset') && (
            <Form.Item label="验证码" required>
              <Space.Compact style={{ width: '100%' }}>
                <Form.Item
                  name="code"
                  noStyle
                  rules={[
                    { required: true, message: '请输入验证码' },
                    { pattern: /^\d{4,8}$/, message: '验证码为 4～8 位数字' },
                  ]}
                >
                  <Input placeholder="邮件中的验证码" size="large" maxLength={8} autoComplete="one-time-code" />
                </Form.Item>
                <Button size="large" loading={sending} disabled={countdown > 0} onClick={onSendCode}>
                  {countdown > 0 ? `${countdown}s` : '获取验证码'}
                </Button>
              </Space.Compact>
            </Form.Item>
          )}

          {mode === 'register' && (
            <Form.Item
              name="password"
              label="设置密码"
              extra={renderPasswordStrength()}
              rules={[
                { required: true, message: '请设置密码' },
                { min: 8, message: '密码至少 8 位' },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="至少 8 位"
                size="large"
                autoComplete="new-password"
                onChange={(e) => setPasswordDraft(e.target.value)}
              />
            </Form.Item>
          )}

          {mode === 'reset' && (
            <>
              <Form.Item
                name="newPassword"
                label="新密码"
                extra={renderPasswordStrength()}
                rules={[
                  { required: true, message: '请输入新密码' },
                  { min: 8, message: '密码至少 8 位' },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="至少 8 位"
                  size="large"
                  autoComplete="new-password"
                  onChange={(e) => setPasswordDraft(e.target.value)}
                />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label="确认新密码"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: '请再次输入新密码' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('两次输入的密码不一致'));
                    },
                  }),
                ]}
              >
                <Input.Password prefix={<LockOutlined />} placeholder="再次输入" size="large" autoComplete="new-password" />
              </Form.Item>
            </>
          )}

          <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
            {mode === 'register' ? '注册并登录' : mode === 'reset' ? '更新密码' : '登录'}
          </Button>
        </Form>

        <div style={{ marginTop: 16, fontSize: 13 }}>
          {mode === 'password' || mode === 'code' ? (
            <Space split={<Text type="secondary">·</Text>}>
              <Link onClick={() => switchMode('register')}>注册账号</Link>
              <Link onClick={() => switchMode('reset')}>忘记密码 / 设置密码</Link>
            </Space>
          ) : mode === 'reset' && getToken() ? (
            <Link onClick={() => navigate('/word/profile')}>返回我的</Link>
          ) : (
            <Link onClick={() => switchMode('password')}>返回登录</Link>
          )}
        </div>

        <Text type="secondary" style={{ display: 'block', marginTop: 12, fontSize: 12 }}>
          验证码 5 分钟内有效；未收到请检查垃圾邮件。
        </Text>
      </div>
    </div>
  );
}
