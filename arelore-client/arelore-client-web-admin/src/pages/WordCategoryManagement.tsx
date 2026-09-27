import React, { useEffect, useState } from 'react';
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { wordCodeRules } from '../utils/wordCode';

const WordCategoryManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [languages, setLanguages] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const loadMeta = async () => {
    const langRes = await adminApi.getWordLanguageList({ pageNum: 1, pageSize: 200 });
    setLanguages(langRes.data?.list || []);
  };

  const loadData = async (filters?: any) => {
    setLoading(true);
    try {
      const catRes = await adminApi.getWordCategoryList({ pageNum: 1, pageSize: 500, ...filters });
      setDataSource(catRes.data?.list || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMeta();
    loadData();
  }, []);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = { ...values, description: values.description || '', sortNo: values.sortNo || 0, status: values.status ?? 1 };
    if (editing?.id) {
      await adminApi.updateWordCategory({
        ...payload,
        id: editing.id,
        code: editing.code,
        languageCode: editing.languageCode
      });
      message.success('更新成功');
    } else {
      await adminApi.createWordCategory(payload);
      message.success('创建成功');
    }
    setModalVisible(false);
    setEditing(null);
    form.resetFields();
    loadData(queryForm.getFieldsValue());
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h1 style={{ margin: 0 }}>单词本分类</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); form.setFieldsValue({ sortNo: 0, status: 1 }); setModalVisible(true); }}>新增分类</Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={(values) => loadData(values)}>
        <Form.Item name="languageCode">
          <Select allowClear placeholder="语种" style={{ width: 160 }} options={languages.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))} />
        </Form.Item>
        <Form.Item name="code"><Input placeholder="分类 code" allowClear /></Form.Item>
        <Form.Item name="name"><Input placeholder="名称" allowClear /></Form.Item>
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
          { title: '语种', dataIndex: 'languageCode', width: 100 },
          { title: 'code', dataIndex: 'code' },
          { title: '名称', dataIndex: 'name' },
          { title: '排序', dataIndex: 'sortNo', width: 80 },
          { title: '状态', dataIndex: 'status', width: 80 },
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalVisible(true); }}>编辑</Button>
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => {
                  Modal.confirm({ title: '确认删除', onOk: async () => { await adminApi.deleteWordCategory({ id: record.id }); message.success('删除成功'); loadData(queryForm.getFieldsValue()); } });
                }}>删除</Button>
              </Space>
            )
          }
        ]}
      />
      <Modal title={editing ? '编辑分类' : '新增分类'} open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="languageCode" label="语种" rules={[{ required: true }]}>
            <Select disabled={!!editing} options={languages.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))} />
          </Form.Item>
          <Form.Item name="code" label="分类 code" rules={wordCodeRules('分类 code')}>
            <Input placeholder="如 JUNIOR、IELTS" disabled={!!editing} />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="description" label="描述"><Input /></Form.Item>
          <Form.Item name="sortNo" label="排序"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="status" label="状态"><InputNumber min={0} max={1} style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WordCategoryManagement;
