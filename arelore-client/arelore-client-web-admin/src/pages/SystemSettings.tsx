import React from 'react';
import { Form, Input, Button, Card, Switch, message } from 'antd';
import type { FormProps } from 'antd';
import { SaveOutlined } from '@ant-design/icons';

interface SettingsFormValues {
  siteName: string;
  siteDescription?: string;
  allowRegister: boolean;
  maintenanceMode: boolean;
  smtpHost?: string;
  smtpPort?: string;
  smtpUsername?: string;
  smtpPassword?: string;
}

const SystemSettings: React.FC = () => {
  const [form] = Form.useForm();

  const onFinish: FormProps<SettingsFormValues>['onFinish'] = async (values) => {
    try {
      console.log('系统设置:', values);
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
    }
  };

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>系统设置</h1>

      <Card title="基础设置" bordered={false} style={{ marginBottom: 24 }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{
            siteName: 'Arelore',
            siteDescription: 'Arelore 平台',
            allowRegister: true,
            maintenanceMode: false,
          }}
        >
          <Form.Item
            name="siteName"
            label="站点名称"
            rules={[{ required: true, message: '请输入站点名称' }]}
          >
            <Input placeholder="请输入站点名称" />
          </Form.Item>

          <Form.Item
            name="siteDescription"
            label="站点描述"
          >
            <Input.TextArea 
              placeholder="请输入站点描述" 
              rows={3}
            />
          </Form.Item>

          <Form.Item
            name="allowRegister"
            label="注册功能"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>

          <Form.Item
            name="maintenanceMode"
            label="维护模式"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" icon={<SaveOutlined />}>
              保存设置
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="邮件设置" bordered={false}>
        <Form layout="vertical">
          <Form.Item
            label="SMTP 服务器"
            name="smtpHost"
            rules={[{ required: true, message: '请输入 SMTP 服务器' }]}
          >
            <Input placeholder="smtp.example.com" />
          </Form.Item>

          <Form.Item
            label="端口"
            name="smtpPort"
            rules={[{ required: true, message: '请输入端口' }]}
          >
            <Input placeholder="587" />
          </Form.Item>

          <Form.Item
            label="用户名"
            name="smtpUsername"
          >
            <Input placeholder="邮箱地址" />
          </Form.Item>

          <Form.Item
            label="密码"
            name="smtpPassword"
          >
            <Input.Password placeholder="SMTP 密码" />
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default SystemSettings;
