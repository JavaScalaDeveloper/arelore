import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Table, Button, message, Spin } from 'antd';
import { UserOutlined, SolutionOutlined, CustomerServiceOutlined } from '@ant-design/icons';
import { userApi } from './api/user';

const HomePage = () => {
  const [loading, setLoading] = useState(false);
  const [userList, setUserList] = useState([]);
  const [stats, setStats] = useState({
    totalUsers: 0,
    todayNew: 0,
    totalProducts: 0
  });

  // 加载用户列表
  const loadUserList = async () => {
    try {
      setLoading(true);
      const response = await userApi.getList({
        pageNum: 1,
        pageSize: 5
      });
      
      if (response.code === 200 && response.data) {
        setUserList(response.data.list || []);
        setStats({
          totalUsers: response.data.total || 0,
          todayNew: Math.floor(Math.random() * 100), // 模拟数据
          totalProducts: Math.floor(Math.random() * 50) // 模拟数据
        });
        message.success('数据加载成功');
      }
    } catch (error) {
      console.error('加载数据失败:', error);
      message.error('加载数据失败，请检查后端服务是否启动');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUserList();
  }, []);

  const columns = [
    {
      title: '序号',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      render: (_, __, index) => index + 1
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username'
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => status ? (
        <span style={{ color: '#52c41a' }}>启用</span>
      ) : (
        <span style={{ color: '#ff4d4f' }}>禁用</span>
      )
    }
  ];

  return (
    <div style={{ padding: '24px' }}>
      <h1 style={{ marginBottom: 24 }}>Arelore 用户平台</h1>
      
      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={8}>
          <Card bordered={false}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <p style={{ margin: 0, color: '#666', fontSize: 14 }}>总用户数</p>
                <p style={{ margin: '8px 0 0 0', fontSize: 32, fontWeight: 'bold', color: '#1890ff' }}>
                  {stats.totalUsers}
                </p>
              </div>
              <UserOutlined style={{ fontSize: 48, color: '#1890ff', opacity: 0.3 }} />
            </div>
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={8}>
          <Card bordered={false}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <p style={{ margin: 0, color: '#666', fontSize: 14 }}>今日新增</p>
                <p style={{ margin: '8px 0 0 0', fontSize: 32, fontWeight: 'bold', color: '#52c41a' }}>
                  {stats.todayNew}
                </p>
              </div>
              <SolutionOutlined style={{ fontSize: 48, color: '#52c41a', opacity: 0.3 }} />
            </div>
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={8}>
          <Card bordered={false}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <p style={{ margin: 0, color: '#666', fontSize: 14 }}>产品数量</p>
                <p style={{ margin: '8px 0 0 0', fontSize: 32, fontWeight: 'bold', color: '#722ed1' }}>
                  {stats.totalProducts}
                </p>
              </div>
              <CustomerServiceOutlined style={{ fontSize: 48, color: '#722ed1', opacity: 0.3 }} />
            </div>
          </Card>
        </Col>
      </Row>

      {/* 用户列表 */}
      <Card 
        title="最新用户" 
        extra={
          <Button type="primary" onClick={loadUserList} loading={loading}>
            刷新数据
          </Button>
        }
      >
        <Spin spinning={loading}>
          <Table
            columns={columns}
            dataSource={userList}
            rowKey={(record) => record.id || Math.random()}
            pagination={false}
            locale={{ emptyText: '暂无数据' }}
          />
        </Spin>
      </Card>

      {/* API 测试说明 */}
      <Card title="📡 API 连接测试" style={{ marginTop: 24 }} bordered={false}>
        <div style={{ padding: '16px', background: '#f6ffed', border: '1px solid #b7eb8f', borderRadius: 4 }}>
          <h3>✅ 前端已成功连接到后端服务</h3>
          <p><strong>当前配置：</strong></p>
          <ul>
            <li>前端端口：3000</li>
            <li>后端服务：arelore-server-user</li>
            <li>后端端口：8081</li>
            <li>API 地址：http://localhost:8081/api</li>
          </ul>
          <p><strong>数据来源：</strong> 上方用户列表数据来自后端接口 <code>POST /api/user/list</code></p>
          <p style={{ margin: 0, color: '#52c41a', fontWeight: 'bold' }}>
            💡 如果看到用户数据，说明前后端已打通！
          </p>
        </div>
      </Card>
    </div>
  );
};

export default HomePage;
