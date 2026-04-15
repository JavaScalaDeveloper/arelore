import React from 'react';
import { Card, Descriptions, Empty } from 'antd';

const MobileProfilePage: React.FC = () => {
  const user = JSON.parse(localStorage.getItem('currentUser') || '{}');

  if (!user || Object.keys(user).length === 0) {
    return (
      <div style={{ padding: 16 }}>
        <Card>
          <Empty description="暂无用户信息" />
        </Card>
      </div>
    );
  }

  return (
    <div style={{ padding: 16 }}>
      <Card title="用户主页">
        <Descriptions column={1}>
          <Descriptions.Item label="用户ID">{user.id || '-'}</Descriptions.Item>
          <Descriptions.Item label="用户名">{user.username || '-'}</Descriptions.Item>
          <Descriptions.Item label="昵称">{user.nickname || '-'}</Descriptions.Item>
          <Descriptions.Item label="邮箱">{user.email || '-'}</Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
};

export default MobileProfilePage;
