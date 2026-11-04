import React, { useEffect, useState } from 'react';
import { Avatar, Button, Card, Space, Typography, message } from 'antd';
import { useI18n, useLocaleNavigate } from '../../../locale';
import { authApi } from '../api/auth';
import { wordApi } from '../api/word';
import { modeLabel, syncPreference } from '../utils/preference';
import { clearSession, getPreference, getToken, getUser, setUser } from '../utils/storage';
import { shortErrorMessage } from '../utils/request';

const { Title, Text, Paragraph } = Typography;

export default function ProfilePage() {
  const navigate = useLocaleNavigate();
  const { t, locale } = useI18n();

  const displayUser = (raw: any) => {
    if (!raw) return null;
    return {
      nickName: raw.nickname || raw.nickName || raw.username || t('word.common.emailUser'),
      email: raw.username || raw.email || '',
      avatarUrl: raw.avatar || raw.avatarUrl || '',
    };
  };

  const [user, setUserState] = useState(displayUser(getUser()));
  const [stats, setStats] = useState({ totalStudyDays: 0, consecutiveStudyDays: 0 });
  const [studyModeLabel, setStudyModeLabel] = useState(modeLabel(getPreference().studyMode, locale));

  const refresh = async () => {
    setStudyModeLabel(modeLabel(getPreference().studyMode, locale));
    syncPreference()
      .then((p) => setStudyModeLabel(modeLabel(p.studyMode, locale)))
      .catch(() => undefined);

    if (!getToken()) {
      setUserState(null);
      setStats({ totalStudyDays: 0, consecutiveStudyDays: 0 });
      return;
    }
    try {
      const res = await authApi.current();
      if (res.data) {
        setUser(res.data);
        setUserState(displayUser(res.data));
      }
      const st = await wordApi.stats();
      setStats({
        totalStudyDays: st.data?.totalStudyDays || 0,
        consecutiveStudyDays: st.data?.consecutiveStudyDays || 0,
      });
    } catch (e: any) {
      message.error(shortErrorMessage(e));
    }
  };

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [locale]);

  const logout = async () => {
    try {
      await authApi.logout();
    } catch {
      /* ignore */
    }
    clearSession();
    setUserState(null);
    setStats({ totalStudyDays: 0, consecutiveStudyDays: 0 });
    message.success(t('word.profile.loggedOut'));
    navigate('/word/login');
  };

  return (
    <div className="page">
      <Title level={3}>{t('word.profile.title')}</Title>
      {!user ? (
        <Card>
          <Paragraph type="secondary">{t('word.profile.loginHint')}</Paragraph>
          <Button type="primary" onClick={() => navigate('/word/login')}>
            {t('word.profile.emailLogin')}
          </Button>
        </Card>
      ) : (
        <Card>
          <Space>
            <Avatar size={56} src={user.avatarUrl || undefined}>
              {(user.nickName || 'U').slice(0, 1)}
            </Avatar>
            <div>
              <Text strong>{user.nickName}</Text>
              {user.email ? (
                <Paragraph type="secondary" style={{ margin: 0 }}>
                  {user.email}
                </Paragraph>
              ) : null}
              <Paragraph type="secondary" style={{ margin: 0 }}>
                {t('word.profile.currentMode', { mode: studyModeLabel })}
              </Paragraph>
            </div>
          </Space>
          <div className="profile-stats">
            <div>
              <div className="stat-num">{stats.totalStudyDays}</div>
              <Text type="secondary">{t('word.profile.totalDays')}</Text>
            </div>
            <div>
              <div className="stat-num">{stats.consecutiveStudyDays}</div>
              <Text type="secondary">{t('word.profile.streakDays')}</Text>
            </div>
          </div>
        </Card>
      )}

      <div className="home-actions" style={{ marginTop: 20 }}>
        <Button onClick={() => navigate('/word/settings')}>{t('word.profile.studySettings')}</Button>
        <Button onClick={() => navigate('/word/books')}>{t('word.profile.chooseBook')}</Button>
        {user && <Button onClick={() => navigate('/word/feedback')}>{t('word.profile.feedback')}</Button>}
        {user && (
          <Button onClick={() => navigate('/word/login?mode=reset')}>{t('word.profile.setPassword')}</Button>
        )}
        {user && (
          <Button danger onClick={logout}>
            {t('word.profile.logout')}
          </Button>
        )}
      </div>
    </div>
  );
}
