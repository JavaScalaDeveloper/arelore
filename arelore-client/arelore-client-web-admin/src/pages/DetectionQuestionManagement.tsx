import React, { useEffect, useState } from 'react';
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi, DetectionQuestionPayload } from '../api/admin';

const DetectionQuestionManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [typeList, setTypeList] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const loadData = async (filters?: any) => {
    setLoading(true);
    try {
      const [questionRes, typeRes] = await Promise.all([
        adminApi.getDetectionQuestionList({ pageNum: 1, pageSize: 500, ...filters }),
        adminApi.getDetectionTypeList({ pageNum: 1, pageSize: 500 })
      ]);
      setDataSource(questionRes.data?.list || []);
      setTypeList(typeRes.data?.list || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: DetectionQuestionPayload = {
      ...values,
      questionOrder: values.questionOrder || 0,
      questionDescription: values.questionDescription || '',
      options: values.options || '',
      extraInfo: values.extraInfo || ''
    };
    if (editing?.id) {
      await adminApi.updateDetectionQuestion({ ...payload, id: editing.id });
      message.success('更新成功');
    } else {
      await adminApi.createDetectionQuestion(payload);
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
        await adminApi.deleteDetectionQuestion({ id });
        message.success('删除成功');
        loadData(queryForm.getFieldsValue());
      }
    });
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h1 style={{ margin: 0 }}>题目管理</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); setModalVisible(true); }}>
          新增题目
        </Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={(values) => loadData(values)}>
        <Form.Item name="typeCode">
          <Select allowClear placeholder="类型编码" style={{ width: 200 }} options={typeList.map((item) => ({ value: item.typeCode, label: `${item.typeCode} - ${item.typeName}` }))} />
        </Form.Item>
        <Form.Item name="questionCode"><Input placeholder="题目编码" allowClear /></Form.Item>
        <Form.Item name="questionName"><Input placeholder="题目名称" allowClear /></Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>查询</Button>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading}
        dataSource={dataSource}
        scroll={{ x: 1200 }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '类型编码', dataIndex: 'typeCode', width: 140 },
          { title: '题目编码', dataIndex: 'questionCode', width: 140 },
          { title: '题目名称', dataIndex: 'questionName', width: 220 },
          { title: '排序', dataIndex: 'questionOrder', width: 90 },
          { title: '修改人', dataIndex: 'modifier', width: 120 },
          { title: '描述', dataIndex: 'questionDescription', width: 220 },
          {
            title: '操作',
            fixed: 'right',
            width: 140,
            render: (_, record) => (
              <Space>
                <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalVisible(true); }}>编辑</Button>
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
              </Space>
            )
          }
        ]}
      />
      <Modal title={editing ? '编辑题目' : '新增题目'} open={modalVisible} width={720} onOk={handleSubmit} onCancel={() => setModalVisible(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="typeCode" label="类型编码" rules={[{ required: true, message: '请选择类型' }]}>
            <Select options={typeList.map((item) => ({ value: item.typeCode, label: `${item.typeCode} - ${item.typeName}` }))} />
          </Form.Item>
          <Form.Item name="questionCode" label="题目编码" rules={[{ required: true, message: '请输入题目编码' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="questionName" label="题目名称" rules={[{ required: true, message: '请输入题目名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="questionOrder" label="排序">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="questionDescription" label="描述">
            <Input />
          </Form.Item>
          <Form.Item name="options" label="选项(JSON)">
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item name="extraInfo" label="扩展信息(JSON)">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DetectionQuestionManagement;
