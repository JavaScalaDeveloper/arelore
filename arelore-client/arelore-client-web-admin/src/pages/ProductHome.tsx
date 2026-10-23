import React from 'react';
import { Card, Col, Row, Typography } from 'antd';
import { BookOutlined, DatabaseOutlined, FileSearchOutlined } from '@ant-design/icons';
import { RegionSwitcher, useLocaleNavigate } from '../locale';

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
  },
  {
    key: 'common',
    name: '公共基础',
    code: 'common',
    desc: '跨产品公共资源：二进制文件库、C 端日访问统计',
    icon: <DatabaseOutlined style={{ fontSize: 36, color: '#08979c' }} />,
    path: '/common/binary-files'
  }
];

const ProductHome: React.FC = () => {
  const navigate = useLocaleNavigate();

  return (
    <div style={{ minHeight: '100vh', background: '#f5f5f5', padding: '64px 24px', position: 'relative' }}>
      <div style={{ position: 'absolute', top: 24, right: 24 }}>
        <RegionSwitcher />
      </div>
      <div style={{ maxWidth: 960, margin: '0 auto' }}>
        <Title level={2} style={{ marginBottom: 8 }}>选择产品</Title>
        <Paragraph type="secondary">进入后地址栏会带上产品标识（cert / word / common），菜单随产品切换。</Paragraph>
        <Row gutter={24}>
          {products.map((item) => (
            <Col xs={24} md={12} key={item.key} style={{ marginBottom: 24 }}>
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
