import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Button, message, Spin } from 'antd';
import {
  UserOutlined,
  MessageOutlined,
  DashboardOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import { adminApi } from '../api/admin';

const Dashboard = () => {
  const [loading, setLoading] = useState(false);
  const [dashboardData, setDashboardData] = useState(null);
  const [userList, setUserList] = useState([]);

  // 加载仪表盘数据
  const loadDashboard = async () => {
    try {
      setLoading(true);
      const response = await adminApi.getDashboard();
      
      if (response.code === 200 && response.data) {
        setDashboardData(response.data);
        message.success('仪表盘数据加载成功');
      }
    } catch (error) {
      console.error('加载仪表盘失败:', error);
      message.error('加载失败，请检查后端服务是否启动');
    } finally {
      setLoading(false);
    }
  };

  // 加载用户列表
  const loadUserList = async () => {
    try {
      const response = await adminApi.getUserList({
        pageNum: 1,
        pageSize: 5
      });
      
      if (response.code === 200 && response.data) {
        setUserList(response.data.list || []);
      }
    } catch (error) {
      console.error('加载用户列表失败:', error);
    }
  };

  useEffect(() => {
    loadDashboard();
    loadUserList();
  }, []);

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
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
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h1 style={{ margin: 0 }}>控制台</h1>
        <Button 
          type="primary" 
          onClick={loadDashboard} 
          loading={loading}
        >
          刷新数据
        </Button>
      </div>
      
      {/* 统计卡片 */}
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card bordered={false}>
              <Statistic
                title="总用户数"
                value={dashboardData?.totalUsers || 0}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card bordered={false}>
              <Statistic
                title="今日新增"
                value={dashboardData?.todayNewUsers || 0}
                prefix={<RiseOutlined />}
                valueStyle={{ color: '#52c41a' }}
                suffix="人"
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card bordered={false}>
              <Statistic
                title="消息总数"
                value={dashboardData?.totalMessages || 0}
                prefix={<MessageOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card bordered={false}>
              <Statistic
                title="系统状态"
                value={dashboardData?.systemStatus || '正常'}
                prefix={<DashboardOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
        </Row>
      </Spin>

      {/* 用户列表 */}
      <Card 
        title="最新用户（来自后端接口）" 
        style={{ marginTop: 24 }}
        bordered={false}
      >
        <Table
          columns={columns}
          dataSource={userList}
          rowKey={(record) => record.id}
          pagination={false}
          locale={{ emptyText: '暂无数据' }}
        />
      </Card>

      {/* API 测试说明 */}
      <Card title="📡 API 连接测试" style={{ marginTop: 24 }} bordered={false}>
        <div style={{ padding: '16px', background: '#f6ffed', border: '1px solid #b7eb8f', borderRadius: 4 }}>
          <h3>✅ 管理端已成功连接到后端服务</h3>
          <p><strong>当前配置：</strong></p>
          <ul>
            <li>前端端口：3001</li>
            <li>后端服务：arelore-server-admin</li>
            <li>后端端口：8082</li>
            <li>API 地址：http://localhost:8082/api</li>
          </ul>
          <p><strong>数据来源：</strong></p>
          <ul>
            <li>统计卡片：<code>POST /api/admin/dashboard</code></li>
            <li>用户列表：<code>POST /api/admin/users</code></li>
          </ul>
          <p style={{ margin: 0, color: '#52c41a', fontWeight: 'bold' }}>
            💡 如果看到数据，说明前后端已打通！
          </p>
        </div>
      </Card>
    </div>
  );
};

export default Dashboard;
