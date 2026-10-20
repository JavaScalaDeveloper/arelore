import React, { useEffect, useState } from 'react';
import { Avatar, Button, Card, Space, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { wordApi } from '../api/word';
import { modeLabel, syncPreference } from '../utils/preference';
import { clearSession, getPreference, getToken, getUser, setUser } from '../utils/storage';
import { shortErrorMessage } from '../utils/request';

const { Title, Text, Paragraph } = Typography;

function displayUser(raw: any) {
  if (!raw) return null;
  return {
    nickName: raw.nickname || raw.nickName || raw.username || '邮箱用户',
    email: raw.username || raw.email || '',
    avatarUrl: raw.avatar || raw.avatarUrl || '',
  };
}

export default function ProfilePage() {
  const navigate = useNavigate();
  const [user, setUserState] = useState(displayUser(getUser()));
  const [stats, setStats] = useState({ totalStudyDays: 0, consecutiveStudyDays: 0 });
  const [studyModeLabel, setStudyModeLabel] = useState(modeLabel(getPreference().studyMode));

  const refresh = async () => {
    setStudyModeLabel(modeLabel(getPreference().studyMode));
    syncPreference()
      .then((p) => setStudyModeLabel(modeLabel(p.studyMode)))
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
  }, []);

  const logout = async () => {
    try {
      await authApi.logout();
    } catch {
      /* ignore */
    }
    clearSession();
    setUserState(null);
    setStats({ totalStudyDays: 0, consecutiveStudyDays: 0 });
    message.success('已退出');
    navigate('/word/login');
  };

  return (
    <div className="page">
      <Title level={3}>我的</Title>
      {!user ? (
        <Card>
          <Paragraph type="secondary">登录后可同步学习进度</Paragraph>
          <Button type="primary" onClick={() => navigate('/word/login')}>
            邮箱登录
          </Button>
        </Card>
      ) : (
        <Card>
          <Space>
            <Avatar size={56} src={user.avatarUrl || undefined}>
              {(user.nickName || '用').slice(0, 1)}
            </Avatar>
            <div>
              <Text strong>{user.nickName}</Text>
              {user.email ? (
                <Paragraph type="secondary" style={{ margin: 0 }}>
                  {user.email}
                </Paragraph>
              ) : null}
              <Paragraph type="secondary" style={{ margin: 0 }}>
                当前模式：{studyModeLabel}
              </Paragraph>
            </div>
          </Space>
          <div className="profile-stats">
            <div>
              <div className="stat-num">{stats.totalStudyDays}</div>
              <Text type="secondary">累计学习天</Text>
            </div>
            <div>
              <div className="stat-num">{stats.consecutiveStudyDays}</div>
              <Text type="secondary">连续学习天</Text>
            </div>
          </div>
        </Card>
      )}

      <div className="home-actions" style={{ marginTop: 20 }}>
        <Button onClick={() => navigate('/word/settings')}>学习设置</Button>
        <Button onClick={() => navigate('/word/books')}>选择单词书</Button>
        {user && (
          <Button onClick={() => navigate('/word/login?mode=reset')}>设置 / 修改密码</Button>
        )}
        {user && (
          <Button danger onClick={logout}>
            退出登录
          </Button>
        )}
      </div>
    </div>
  );
}
