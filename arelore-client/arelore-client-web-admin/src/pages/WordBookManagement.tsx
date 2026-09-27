import React, { useEffect, useState } from 'react';
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { wordCodeRules } from '../utils/wordCode';

const WordBookManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [languages, setLanguages] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const loadMeta = async () => {
    const [catRes, langRes] = await Promise.all([
      adminApi.getWordCategoryList({ pageNum: 1, pageSize: 500 }),
      adminApi.getWordLanguageList({ pageNum: 1, pageSize: 200 })
    ]);
    setCategories(catRes.data?.list || []);
    setLanguages(langRes.data?.list || []);
  };

  const loadData = async (filters?: any) => {
    setLoading(true);
    try {
      const bookRes = await adminApi.getWordBookList({ pageNum: 1, pageSize: 500, ...filters });
      setDataSource(bookRes.data?.list || []);
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
    const payload = { ...values, description: values.description || '', wordCount: values.wordCount || 0, status: values.status ?? 1, extInfo: values.extInfo || '' };
    if (editing?.id) {
      await adminApi.updateWordBook({
        ...payload,
        id: editing.id,
        code: editing.code,
        languageCode: editing.languageCode,
        categoryCode: editing.categoryCode
      });
      message.success('更新成功');
    } else {
      await adminApi.createWordBook(payload);
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
        <h1 style={{ margin: 0 }}>单词本</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); form.setFieldsValue({ wordCount: 0, status: 1 }); setModalVisible(true); }}>新增单词本</Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={(values) => loadData(values)}>
        <Form.Item name="code"><Input placeholder="单词本 code" allowClear /></Form.Item>
        <Form.Item name="name"><Input placeholder="名称" allowClear /></Form.Item>
        <Form.Item name="languageCode">
          <Select allowClear placeholder="语种" style={{ width: 140 }} options={languages.map((item) => ({ value: item.code, label: item.code }))} />
        </Form.Item>
        <Form.Item name="categoryCode">
          <Select allowClear placeholder="分类" style={{ width: 180 }} options={categories.map((item) => ({ value: item.code, label: `${item.languageCode}/${item.code}` }))} />
        </Form.Item>
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
          { title: 'code', dataIndex: 'code' },
          { title: '名称', dataIndex: 'name' },
          { title: '语种', dataIndex: 'languageCode', width: 90 },
          { title: '分类', dataIndex: 'categoryCode', width: 110 },
          { title: '词数', dataIndex: 'wordCount', width: 80 },
          { title: '状态', dataIndex: 'status', width: 80 },
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalVisible(true); }}>编辑</Button>
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => {
                  Modal.confirm({ title: '确认删除', onOk: async () => { await adminApi.deleteWordBook({ id: record.id }); message.success('删除成功'); loadData(queryForm.getFieldsValue()); } });
                }}>删除</Button>
              </Space>
            )
          }
        ]}
      />
      <Modal title={editing ? '编辑单词本' : '新增单词本'} open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="单词本 code" rules={wordCodeRules('单词本 code')}>
            <Input disabled={!!editing} />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="languageCode" label="语种" rules={[{ required: true }]}>
            <Select disabled={!!editing} options={languages.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))} />
          </Form.Item>
          <Form.Item name="categoryCode" label="分类" rules={[{ required: true }]}>
            <Select disabled={!!editing} options={categories.map((item) => ({ value: item.code, label: `${item.languageCode}/${item.code} - ${item.name}` }))} />
          </Form.Item>
          <Form.Item name="description" label="描述"><Input /></Form.Item>
          <Form.Item name="status" label="状态"><InputNumber min={0} max={1} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="extInfo" label="拓展 JSON"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WordBookManagement;
