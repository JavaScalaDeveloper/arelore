import React from 'react';
import { Card, Col, Row, Typography } from 'antd';
import { BookOutlined, FileSearchOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph } = Typography;

const products = [
  {
    key: 'cert',
    name: '考证宝',
    code: 'cert',
    desc: '题目类型、试卷与题目管理',
    icon: <FileSearchOutlined style={{ fontSize: 36, color: '#1677ff' }} />,
    path: '/cert/dashboard'
  },
  {
    key: 'word',
    name: '背单词',
    code: 'word',
    desc: '语种、分类、单词本与词条管理',
    icon: <BookOutlined style={{ fontSize: 36, color: '#722ed1' }} />,
    path: '/word/languages'
  }
];

const ProductHome: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div style={{ minHeight: '100vh', background: '#f5f5f5', padding: '64px 24px' }}>
      <div style={{ maxWidth: 960, margin: '0 auto' }}>
        <Title level={2} style={{ marginBottom: 8 }}>选择产品</Title>
        <Paragraph type="secondary">进入后地址栏会带上产品标识（cert / word），菜单随产品切换。</Paragraph>
        <Row gutter={24}>
          {products.map((item) => (
            <Col xs={24} md={12} key={item.key}>
              <Card
                hoverable
                onClick={() => navigate(item.path)}
                style={{ borderRadius: 12, minHeight: 180 }}
              >
                <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
                  {item.icon}
                  <div>
                    <Title level={4} style={{ margin: 0 }}>{item.name}</Title>
                    <Paragraph type="secondary" style={{ margin: '8px 0 0' }}>
                      {item.code} · {item.desc}
                    </Paragraph>
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  );
};

export default ProductHome;
