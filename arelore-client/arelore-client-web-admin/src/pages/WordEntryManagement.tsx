import React, { useEffect, useState } from 'react';
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { wordCodeRules } from '../utils/wordCode';

const WordEntryManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [books, setBooks] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const loadMeta = async () => {
    const bookRes = await adminApi.getWordBookList({ pageNum: 1, pageSize: 500 });
    setBooks(bookRes.data?.list || []);
  };

  const loadData = async (filters?: any) => {
    setLoading(true);
    try {
      const entryRes = await adminApi.getWordEntryList({ pageNum: 1, pageSize: 500, ...filters });
      setDataSource(entryRes.data?.list || []);
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
    const payload = { ...values, sortNo: values.sortNo || 0, extInfo: values.extInfo || '' };
    if (editing?.id) {
      await adminApi.updateWordEntry({
        ...payload,
        id: editing.id,
        bookCode: editing.bookCode,
        wordCode: editing.wordCode
      });
      message.success('更新成功');
    } else {
      await adminApi.createWordEntry(payload);
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
        <h1 style={{ margin: 0 }}>单词本词条</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); form.setFieldsValue({ sortNo: 0 }); setModalVisible(true); }}>新增词条</Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={(values) => loadData(values)}>
        <Form.Item name="bookCode">
          <Select allowClear placeholder="单词本" style={{ width: 200 }} options={books.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))} />
        </Form.Item>
        <Form.Item name="wordCode"><Input placeholder="单词 code" allowClear /></Form.Item>
        <Form.Item name="word"><Input placeholder="词条原文" allowClear /></Form.Item>
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
          { title: '词本', dataIndex: 'bookCode', width: 140 },
          { title: '单词 code', dataIndex: 'wordCode' },
          { title: '词条', dataIndex: 'word' },
          { title: '排序', dataIndex: 'sortNo', width: 80 },
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalVisible(true); }}>编辑</Button>
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => {
                  Modal.confirm({ title: '确认删除', onOk: async () => { await adminApi.deleteWordEntry({ id: record.id }); message.success('删除成功'); loadData(queryForm.getFieldsValue()); } });
                }}>删除</Button>
              </Space>
            )
          }
        ]}
      />
      <Modal title={editing ? '编辑词条' : '新增词条'} open={modalVisible} width={720} onOk={handleSubmit} onCancel={() => setModalVisible(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="bookCode" label="单词本" rules={[{ required: true }]}>
            <Select disabled={!!editing} options={books.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))} />
          </Form.Item>
          <Form.Item name="wordCode" label="单词 code" rules={wordCodeRules('单词 code')}>
            <Input placeholder="词本内唯一，如 APPLE" disabled={!!editing} />
          </Form.Item>
          <Form.Item name="word" label="词条原文" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="sortNo" label="排序"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="extInfo" label="拓展 JSON（phonetic / partOfSpeech / meaning / example / imageIds）">
            <Input.TextArea rows={6} placeholder='{"phonetic":"/ˈæpl/","meaning":"苹果","example":"..."}' />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WordEntryManagement;
