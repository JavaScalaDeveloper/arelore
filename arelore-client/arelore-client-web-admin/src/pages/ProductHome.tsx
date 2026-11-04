import React from 'react';
import { Card, Col, Row, Typography } from 'antd';
import { BookOutlined, DatabaseOutlined, FileSearchOutlined, HighlightOutlined } from '@ant-design/icons';
import { RegionSwitcher, useLocaleNavigate } from '../locale';

const { Title, Paragraph } = Typography;

const products = [
  {
    key: 'cert',
    name: '考证宝',
    code: 'cert',
    desc: '题目类型、试卷与题目管理',
    iconColor: '#1677ff',
    Icon: FileSearchOutlined,
    path: '/cert/dashboard'
  },
  {
    key: 'word',
    name: '背单词',
    code: 'word',
    desc: '语种、分类、单词本与词条管理',
    iconColor: '#722ed1',
    Icon: BookOutlined,
    path: '/word/languages'
  },
  {
    key: 'common',
    name: '公共基础',
    code: 'common',
    desc: '跨产品公共资源：二进制文件库、C 端日访问统计',
    iconColor: '#08979c',
    Icon: DatabaseOutlined,
    path: '/common/binary-files'
  },
  {
    key: 'prompt',
    name: 'AI提示词',
    code: 'prompt',
    desc: '用户偏好、App 访问 PV/UV、用户配置的提示词',
    iconColor: '#d46b08',
    Icon: HighlightOutlined,
    path: '/prompt/prefs'
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
        <Paragraph type="secondary">进入后地址栏会带上产品标识（cert / word / common / prompt），菜单随产品切换。</Paragraph>
        <Row gutter={24}>
          {products.map((item) => (
            <Col xs={24} md={12} key={item.key} style={{ marginBottom: 24 }}>
              <Card
                hoverable
                onClick={() => navigate(item.path)}
                style={{ borderRadius: 12, minHeight: 180 }}
              >
                <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
                  <item.Icon style={{ fontSize: 36, color: item.iconColor }} />
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
