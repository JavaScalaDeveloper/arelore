import React, { useEffect, useState } from 'react';
import { Card, Empty, List, Spin, Tabs, Typography } from 'antd';
import { userApi } from '../api/user';

const { Title, Paragraph, Text } = Typography;

const MbtiQuestionBankPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [types, setTypes] = useState<any[]>([]);
  const [questions, setQuestions] = useState<any[]>([]);
  const [activeTypeCode, setActiveTypeCode] = useState<string>('');

  const loadTypes = async () => {
    const typeRes = await userApi.getDetectionTypeAll();
    const typeList = typeRes.data || [];
    setTypes(typeList);
    if (typeList.length > 0) {
      setActiveTypeCode(typeList[0].typeCode);
      return typeList[0].typeCode;
    }
    return '';
  };

  const loadQuestions = async (typeCode?: string) => {
    const questionRes = await userApi.getDetectionQuestionAll(typeCode ? { typeCode } : {});
    setQuestions(questionRes.data || []);
  };

  useEffect(() => {
    const init = async () => {
      setLoading(true);
      try {
        const firstTypeCode = await loadTypes();
        await loadQuestions(firstTypeCode);
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  const onTabChange = async (key: string) => {
    setActiveTypeCode(key);
    setLoading(true);
    try {
      await loadQuestions(key);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-content-wrapper">
      <div style={{ padding: '24px', maxWidth: 1100, margin: '0 auto' }}>
        <Title level={2}>MBTI 题库</Title>
        <Paragraph type="secondary">该页面仅展示后台配置的题目类型和题目内容，不提供新增或编辑操作。</Paragraph>
        <Card>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px 0' }}>
              <Spin />
            </div>
          ) : types.length === 0 ? (
            <Empty description="暂无题目类型数据" />
          ) : (
            <Tabs
              activeKey={activeTypeCode}
              onChange={onTabChange}
              items={types.map((item) => ({
                key: item.typeCode,
                label: item.typeName || item.typeCode
              }))}
            />
          )}

          {!loading && questions.length > 0 && (
            <List
              itemLayout="vertical"
              dataSource={questions}
              renderItem={(item: any) => (
                <List.Item key={item.id}>
                  <Text strong>{item.questionOrder}. {item.questionName}</Text>
                  <div style={{ color: '#666', marginTop: 8 }}>{item.questionDescription || '无描述'}</div>
                  <div style={{ marginTop: 8 }}>
                    <Text type="secondary">选项: </Text>
                    <Text code>{item.options || '[]'}</Text>
                  </div>
                </List.Item>
              )}
            />
          )}
        </Card>
      </div>
    </div>
  );
};

export default MbtiQuestionBankPage;
