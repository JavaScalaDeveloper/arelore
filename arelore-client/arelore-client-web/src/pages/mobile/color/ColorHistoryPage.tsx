import React, { useEffect, useState } from 'react';
import { Button, Card, Empty, List, Modal, Space, Spin, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../../../api/user';

const { Title, Text } = Typography;
const COLOR_TYPE_CODE = 'COLOR4_INTL_V1';

const ColorHistoryPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(false);
  const [historyList, setHistoryList] = useState<any[]>([]);
  const [selectedHistory, setSelectedHistory] = useState<any | null>(null);
  const [detailVisible, setDetailVisible] = useState<boolean>(false);

  useEffect(() => {
    const loadHistory = async () => {
      const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
      if (!currentUser?.id) {
        setHistoryList([]);
        return;
      }

      try {
        setLoading(true);
        const res = await userApi.getDetectionResultHistory({
          userId: currentUser.id,
          typeCode: COLOR_TYPE_CODE
        });
        setHistoryList(res.data || []);
      } finally {
        setLoading(false);
      }
    };
    loadHistory();
  }, []);

  const getAnsweredQuestions = () => {
    if (!selectedHistory?.extraInfo) {
      return [];
    }
    try {
      const parsed = JSON.parse(selectedHistory.extraInfo);
      return parsed.answeredQuestions || [];
    } catch (error) {
      return [];
    }
  };

  const answeredQuestions = getAnsweredQuestions();

  return (
    <div style={{ minHeight: 'calc(100vh - 56px)', background: '#f5f7fb', padding: 16 }}>
      <Card style={{ maxWidth: 900, margin: '0 auto' }}>
        <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
          <Title level={4} style={{ margin: 0 }}>性格色彩历史检测结果</Title>
          <Button onClick={() => navigate('/mobile/color-test')}>返回测试页</Button>
        </Space>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0' }}><Spin /></div>
        ) : historyList.length === 0 ? (
          <Empty description="暂无历史检测结果" />
        ) : (
          <Card size="small" title="历史记录">
            <List
              dataSource={historyList}
              renderItem={(item: any) => (
                <List.Item
                  style={{ cursor: 'pointer' }}
                  onClick={() => {
                    setSelectedHistory(item);
                    setDetailVisible(true);
                  }}
                >
                  <div style={{ width: '100%' }}>
                    <div><Text strong>{item.userDetectResult}</Text></div>
                    <div style={{ color: '#888', fontSize: 12 }}>{item.createTime}</div>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        )}
      </Card>
      <Modal
        title="历史结果详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width="95vw"
      >
        {selectedHistory ? (
          <>
            <div style={{ marginBottom: 10 }}>
              <Text strong>检测结果：</Text>
              <Text>{selectedHistory.userDetectResult}</Text>
            </div>
            <div style={{ marginBottom: 10 }}>
              <Text strong>检测时间：</Text>
              <Text>{selectedHistory.createTime}</Text>
            </div>
            <List
              header={<Text strong>答题详情</Text>}
              dataSource={answeredQuestions}
              locale={{ emptyText: '该条记录没有答题详情' }}
              renderItem={(item: any, index: number) => (
                <List.Item>
                  <div>
                    <div><Text strong>{index + 1}. {item.questionTitle}</Text></div>
                    <div style={{ color: '#666' }}>
                      选择：{item.selectedOptionKey} - {item.selectedOptionText}
                    </div>
                  </div>
                </List.Item>
              )}
            />
          </>
        ) : (
          <Empty description="请选择历史记录" />
        )}
      </Modal>
    </div>
  );
};

export default ColorHistoryPage;

