import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Form, Input, Progress, Space, Tabs, Typography, message } from 'antd';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { useSearchParams } from 'react-router-dom';
import { stripLocalePrefix, useI18n, useLocaleNavigate } from '../../../locale';
import { authApi } from '../api/auth';
import { sha256Hex } from '../../../utils/hash';
import { passwordStrengthView } from '../../../utils/passwordStrength';
import { pushLocalPreference } from '../utils/preference';
import { setToken, setUser, getToken, getUser } from '../utils/storage';
import { shortErrorMessage } from '../utils/request';

const { Title, Paragraph, Text, Link } = Typography;

const CODE_COOLDOWN_SEC = 60;
const CODE_COOLDOWN_STORAGE_PREFIX = 'word_email_code_cd:';

type Mode = 'password' | 'code' | 'register' | 'reset';

function cooldownStorageKey(mode: Mode, email: string): string {
  return `${CODE_COOLDOWN_STORAGE_PREFIX}${mode}:${email.trim().toLowerCase()}`;
}

function readCooldownLeft(mode: Mode, email: string): number {
  if (!email.trim()) {
    return 0;
  }
  try {
    const raw = sessionStorage.getItem(cooldownStorageKey(mode, email));
    const until = Number(raw);
    if (!Number.isFinite(until)) {
      return 0;
    }
    return Math.max(0, Math.ceil((until - Date.now()) / 1000));
  } catch {
    return 0;
  }
}

function writeCooldown(mode: Mode, email: string, seconds: number): void {
  try {
    const until = Date.now() + seconds * 1000;
    sessionStorage.setItem(cooldownStorageKey(mode, email), String(until));
  } catch {
    /* ignore */
  }
}

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
  const { t } = useI18n();
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
      message.success(t('word.login.loginOk'));
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
    if (mode === 'reset') {
      const u: any = getUser();
      const email = (u?.username || u?.email || '').trim();
      if (email.includes('@')) {
        form.setFieldsValue({ email });
        setCountdown(readCooldownLeft(mode, email));
        return;
      }
    }
    const email = String(form.getFieldValue('email') || '').trim();
    setCountdown(readCooldownLeft(mode, email));
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
            {t('word.login.passwordHint')}
          </Text>
        </div>
      </div>
    );
  };

  const onSendCode = async () => {
    if (countdown > 0 || sending) {
      return;
    }
    try {
      await form.validateFields(['email']);
      const email = String(form.getFieldValue('email') || '').trim();
      const left = readCooldownLeft(mode, email);
      if (left > 0) {
        setCountdown(left);
        message.warning(t('word.login.codeWait', { n: left }));
        return;
      }
      setSending(true);
      if (mode === 'register') {
        await authApi.applyEmailRegisterCode(email);
      } else if (mode === 'reset') {
        await authApi.applyEmailResetPasswordCode(email);
      } else {
        await authApi.applyEmailCode(email);
      }
      message.success(t('word.login.codeSent'));
      writeCooldown(mode, email, CODE_COOLDOWN_SEC);
      setCountdown(CODE_COOLDOWN_SEC);
    } catch (e: any) {
      if (e?.errorFields) {
        return;
      }
      message.error(shortErrorMessage(e, t('word.login.sendFailed')));
    } finally {
      setSending(false);
    }
  };

  const hashPassword = async (plain: string): Promise<string | null> => {
    try {
      return await sha256Hex(plain);
    } catch {
      message.error(t('word.login.hashFailed'));
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
          message.error(t('word.login.noTokenLogin'));
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
          message.error(t('word.login.noTokenLogin'));
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
          message.error(t('word.login.noTokenRegister'));
          return;
        }
        message.success(t('word.login.registerOk'));
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
      message.success(getToken() ? t('word.login.passwordUpdated') : t('word.login.passwordUpdatedLogin'));
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
      message.error(shortErrorMessage(e, mode === 'register' ? t('word.login.registerFailed') : mode === 'reset' ? t('word.login.resetFailed') : t('word.login.loginFailed')));
    } finally {
      setSubmitting(false);
    }
  };

  const titleMap: Record<Mode, string> = {
    password: t('word.login.titlePassword'),
    code: t('word.login.titleCode'),
    register: t('word.login.titleRegister'),
    reset: t('word.login.titleReset'),
  };

  const tipMap: Record<Mode, string> = {
    password: t('word.login.tipPassword'),
    code: t('word.login.tipCode'),
    register: t('word.login.tipRegister'),
    reset: t('word.login.tipReset'),
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
              { key: 'password', label: t('word.login.tabPassword') },
              { key: 'code', label: t('word.login.tabCode') },
            ]}
            style={{ marginBottom: 8 }}
          />
        )}

        <Form form={form} layout="vertical" onFinish={onSubmit} style={{ textAlign: 'left', marginTop: 8 }}>
          <Form.Item
            name="email"
            label={t('word.login.email')}
            rules={[
              { required: true, message: t('word.login.emailRequired') },
              { type: 'email', message: t('word.login.emailInvalid') },
            ]}
          >
            <Input
              prefix={<MailOutlined />}
              placeholder="name@example.com"
              size="large"
              autoComplete="email"
              onBlur={() => {
                const email = String(form.getFieldValue('email') || '').trim();
                setCountdown(readCooldownLeft(mode, email));
              }}
            />
          </Form.Item>

          {mode === 'password' && (
            <Form.Item
              name="password"
              label={t('word.login.password')}
              rules={[
                { required: true, message: t('word.login.passwordRequired') },
                { min: 8, message: t('word.login.passwordMin') },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder={t('word.login.passwordPlaceholder')}
                size="large"
                autoComplete="current-password"
              />
            </Form.Item>
          )}

          {(mode === 'code' || mode === 'register' || mode === 'reset') && (
            <Form.Item label={t('word.login.code')} required>
              <Space.Compact style={{ width: '100%' }}>
                <Form.Item
                  name="code"
                  noStyle
                  rules={[
                    { required: true, message: t('word.login.codeRequired') },
                    { pattern: /^\d{4,8}$/, message: t('word.login.codePattern') },
                  ]}
                >
                  <Input placeholder={t('word.login.codePlaceholder')} size="large" maxLength={8} autoComplete="one-time-code" />
                </Form.Item>
                <Button size="large" loading={sending} disabled={countdown > 0 || sending} onClick={onSendCode}>
                  {countdown > 0 ? t('word.login.retryIn', { n: countdown }) : t('word.login.getCode')}
                </Button>
              </Space.Compact>
            </Form.Item>
          )}

          {mode === 'register' && (
            <Form.Item
              name="password"
              label={t('word.login.setPassword')}
              extra={renderPasswordStrength()}
              rules={[
                { required: true, message: t('word.login.passwordRequired') },
                { min: 8, message: t('word.login.passwordMin') },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder={t('word.login.passwordPlaceholder')}
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
                label={t('word.login.newPassword')}
                extra={renderPasswordStrength()}
                rules={[
                  { required: true, message: t('word.login.passwordRequired') },
                  { min: 8, message: t('word.login.passwordMin') },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder={t('word.login.passwordPlaceholder')}
                  size="large"
                  autoComplete="new-password"
                  onChange={(e) => setPasswordDraft(e.target.value)}
                />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label={t('word.login.confirmPassword')}
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: t('word.login.confirmRequired') },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error(t('word.login.confirmMismatch')));
                    },
                  }),
                ]}
              >
                <Input.Password prefix={<LockOutlined />} placeholder={t('word.login.confirmPlaceholder')} size="large" autoComplete="new-password" />
              </Form.Item>
            </>
          )}

          <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
            {mode === 'register' ? t('word.login.submitRegister') : mode === 'reset' ? t('word.login.submitReset') : t('word.login.submitLogin')}
          </Button>
        </Form>

        <div style={{ marginTop: 16, fontSize: 13 }}>
          {mode === 'password' || mode === 'code' ? (
            <Space split={<Text type="secondary">·</Text>}>
              <Link onClick={() => switchMode('register')}>{t('word.login.linkRegister')}</Link>
              <Link onClick={() => switchMode('reset')}>{t('word.login.linkForgot')}</Link>
            </Space>
          ) : mode === 'reset' && getToken() ? (
            <Link onClick={() => navigate('/word/profile')}>{t('word.login.backProfile')}</Link>
          ) : (
            <Link onClick={() => switchMode('password')}>{t('word.login.backLogin')}</Link>
          )}
        </div>

        <Text type="secondary" style={{ display: 'block', marginTop: 12, fontSize: 12 }}>
          {t('word.login.codeFooter')}
        </Text>
      </div>
    </div>
  );
}
