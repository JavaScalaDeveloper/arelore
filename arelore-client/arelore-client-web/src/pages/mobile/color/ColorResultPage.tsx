import React from 'react';
import { Button, Card, Space, Typography } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';

const { Title, Paragraph, Text } = Typography;

interface ResultMeaningDetail {
  summary?: string;
  strengths?: string;
  risks?: string;
  suggestedRoles?: string;
  communicationTips?: string;
}

const ColorResultPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const result = (location.state as any)?.result || '';
  const meaning = (location.state as any)?.meaning as string | ResultMeaningDetail | undefined;

  return (
    <div style={{ minHeight: 'calc(100vh - 56px)', background: '#f5f7fb', padding: 16 }}>
      <Card style={{ maxWidth: 760, margin: '0 auto', background: '#f6ffed', borderColor: '#b7eb8f' }}>
        <Title level={3} style={{ marginTop: 0 }}>性格色彩测试结果</Title>
        <Text>你的性格主色是：</Text>
        <Title level={2} style={{ margin: '8px 0 0' }}>{result || '未知'}</Title>
        <Paragraph style={{ marginTop: 8, marginBottom: 0, color: '#666' }}>
          {typeof meaning === 'string' ? (meaning || '暂未配置该结果的含义说明。') : (meaning?.summary || '暂未配置该结果的含义说明。')}
        </Paragraph>
        {meaning && typeof meaning !== 'string' ? (
          <Space direction="vertical" size={4} style={{ marginTop: 8 }}>
            {meaning.strengths ? <Text style={{ color: '#389e0d' }}>优势：{meaning.strengths}</Text> : null}
            {meaning.risks ? <Text style={{ color: '#cf1322' }}>风险点：{meaning.risks}</Text> : null}
            {meaning.suggestedRoles ? <Text>建议方向：{meaning.suggestedRoles}</Text> : null}
            {meaning.communicationTips ? <Text>沟通建议：{meaning.communicationTips}</Text> : null}
          </Space>
        ) : null}
        <Space style={{ marginTop: 20 }}>
          <Button type="primary" onClick={() => navigate('/mobile/color-test')}>返回测试</Button>
          <Button onClick={() => navigate('/mobile/home')}>返回首页</Button>
        </Space>
      </Card>
    </div>
  );
};

export default ColorResultPage;

