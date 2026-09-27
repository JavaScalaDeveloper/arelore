import React, { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Space, Table, message } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi, DetectionTypePayload } from '../api/admin';

const DetectionTypeManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const loadData = async (filters?: any) => {
    setLoading(true);
    try {
      const res = await adminApi.getDetectionTypeList({ pageNum: 1, pageSize: 200, ...filters });
      setDataSource(res.data?.list || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: DetectionTypePayload = {
      ...values,
      typeDescription: values.typeDescription || '',
      extraInfo: values.extraInfo || ''
    };
    if (editing?.id) {
      await adminApi.updateDetectionType({ ...payload, id: editing.id });
      message.success('更新成功');
    } else {
      await adminApi.createDetectionType(payload);
      message.success('创建成功');
    }
    setModalVisible(false);
    setEditing(null);
    form.resetFields();
    loadData(queryForm.getFieldsValue());
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      onOk: async () => {
        await adminApi.deleteDetectionType({ id });
        message.success('删除成功');
        loadData(queryForm.getFieldsValue());
      }
    });
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h1 style={{ margin: 0 }}>题目类型管理</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); setModalVisible(true); }}>
          新增类型
        </Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={(values) => loadData(values)}>
        <Form.Item name="typeCode"><Input placeholder="类型编码" allowClear /></Form.Item>
        <Form.Item name="typeName"><Input placeholder="类型名称" allowClear /></Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>查询</Button>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading}
        dataSource={dataSource}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '类型编码', dataIndex: 'typeCode' },
          { title: '类型名称', dataIndex: 'typeName' },
          { title: '描述', dataIndex: 'typeDescription' },
          { title: '修改人', dataIndex: 'modifier', width: 120 },
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalVisible(true); }}>编辑</Button>
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
              </Space>
            )
          }
        ]}
      />
      <Modal title={editing ? '编辑类型' : '新增类型'} open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="typeCode" label="类型编码" rules={[{ required: true, message: '请输入类型编码' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="typeName" label="类型名称" rules={[{ required: true, message: '请输入类型名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="typeDescription" label="描述">
            <Input />
          </Form.Item>
          <Form.Item name="extraInfo" label="扩展信息(JSON)">
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DetectionTypeManagement;
