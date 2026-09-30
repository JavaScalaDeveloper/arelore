import React, { useEffect, useState } from 'react';
import { Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Table, Tooltip, Typography, message } from 'antd';
import { DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { EntryMediaPreview } from '../components/WordMediaPreview';
import { ellipsisColumn } from '../utils/tableCell';
import { wordCodeRules } from '../utils/wordCode';

const DEFAULT_PAGE_SIZE = 20;

const formatExtInfo = (raw?: string) => {
  if (!raw) {
    return '';
  }
  try {
    return JSON.stringify(JSON.parse(raw), null, 2);
  } catch {
    return raw;
  }
};

const WordEntryManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [books, setBooks] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [detail, setDetail] = useState<any | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any; bookList?: any[] }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    delete filters.pageNum;
    delete filters.pageSize;
    if (!filters?.bookCode) {
      message.warning('请先选择单词本再查询');
      setDataSource([]);
      setTotal(0);
      return;
    }
    setLoading(true);
    try {
      const entryRes = await adminApi.getWordEntryList({
        ...filters,
        pageNum: nextPageNum,
        pageSize: nextPageSize
      });
      const sourceBooks = overrides?.bookList ?? books;
      const bookNameMap = Object.fromEntries(sourceBooks.map((b) => [b.code, b.name]));
      const list = (entryRes.data?.list || []).map((item: any) => ({
        ...item,
        bookName: item.bookName || bookNameMap[item.bookCode] || ''
      }));
      setDataSource(list);
      setTotal(Number(entryRes.data?.total) || 0);
      setPageNum(entryRes.data?.pageNum || nextPageNum);
      setPageSize(entryRes.data?.pageSize || nextPageSize);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      const bookRes = await adminApi.getWordBookList({ pageNum: 1, pageSize: 100 });
      const bookList = bookRes.data?.list || [];
      setBooks(bookList);
      if (bookList.length > 0) {
        const firstCode = bookList[0].code;
        queryForm.setFieldsValue({ bookCode: firstCode });
        loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: { bookCode: firstCode }, bookList });
      }
    })();
  }, []);

  const handleSearch = (values: any) => {
    loadData({ pageNum: 1, pageSize, filters: values });
  };

  const openEdit = async (record: any) => {
    const detailRes = await adminApi.getWordEntryDetail({ id: record.id });
    const detailData = detailRes.data || record;
    setEditing(detailData);
    form.setFieldsValue(detailData);
    setModalVisible(true);
  };

  const openDetail = async (record: any) => {
    setDetailVisible(true);
    setDetailLoading(true);
    try {
      const detailRes = await adminApi.getWordEntryDetail({ id: record.id });
      setDetail(detailRes.data || record);
    } finally {
      setDetailLoading(false);
    }
  };

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
    loadData({ pageNum: 1 });
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h1 style={{ margin: 0 }}>单词本词条</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); form.setFieldsValue({ sortNo: 0, bookCode: queryForm.getFieldValue('bookCode') }); setModalVisible(true); }}>新增词条</Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={handleSearch}>
        <Form.Item name="bookCode" rules={[{ required: true, message: '请选择单词本' }]}>
          <Select
            showSearch
            optionFilterProp="label"
            placeholder="单词本（必选）"
            style={{ width: 260 }}
            options={books.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))}
            onChange={(bookCode) => loadData({ pageNum: 1, pageSize, filters: { ...queryForm.getFieldsValue(), bookCode } })}
          />
        </Form.Item>
        <Form.Item name="wordCode"><Input placeholder="单词 code" allowClear /></Form.Item>
        <Form.Item name="word"><Input placeholder="词条原文（前缀匹配）" allowClear /></Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>查询</Button>
        </Form.Item>
      </Form>
      <Table
        rowKey="id"
        loading={loading}
        dataSource={dataSource}
        size="middle"
        tableLayout="fixed"
        scroll={{ x: 900 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: ['20', '50', '100'],
          showTotal: (t) => `共 ${t} 条`,
          onChange: (nextPage, nextSize) => {
            loadData({ pageNum: nextPage, pageSize: nextSize || DEFAULT_PAGE_SIZE });
          }
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 88 },
          {
            title: '词本',
            key: 'book',
            width: 168,
            ellipsis: { showTitle: false },
            render: (_: unknown, record: any) => {
              const label = record.bookName || record.bookCode || '-';
              const tip = [record.bookCode, record.bookName].filter(Boolean).join(' · ');
              return (
                <Tooltip placement="topLeft" title={tip || undefined}>
                  <span style={{ display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {label}
                  </span>
                </Tooltip>
              );
            }
          },
          ellipsisColumn('单词 code', 'wordCode', 180),
          ellipsisColumn('词条', 'word', 140),
          { title: '排序', dataIndex: 'sortNo', width: 72, align: 'center' as const },
          {
            title: '操作',
            key: 'actions',
            width: 120,
            fixed: 'right' as const,
            render: (_: unknown, record: any) => (
              <Space size={0}>
                <Tooltip title="查看详情">
                  <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(record)} />
                </Tooltip>
                <Tooltip title="编辑">
                  <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(record)} />
                </Tooltip>
                <Tooltip title="删除">
                  <Button
                    type="link"
                    size="small"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => {
                      Modal.confirm({
                        title: '确认删除',
                        onOk: async () => {
                          await adminApi.deleteWordEntry({ id: record.id });
                          message.success('删除成功');
                          loadData();
                        }
                      });
                    }}
                  />
                </Tooltip>
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
      <Modal
        title={detail ? `词条详情 · ${detail.word}` : '词条详情'}
        open={detailVisible}
        width={760}
        footer={null}
        confirmLoading={detailLoading}
        onCancel={() => { setDetailVisible(false); setDetail(null); }}
      >
        {detail && (
          <>
            <EntryMediaPreview word={detail.word} extInfo={detail.extInfo} />
            <Descriptions column={1} size="small" bordered style={{ marginBottom: 16 }}>
              <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="词本 code">{detail.bookCode}</Descriptions.Item>
              <Descriptions.Item label="词本名称">{detail.bookName || '-'}</Descriptions.Item>
              <Descriptions.Item label="单词 code">{detail.wordCode}</Descriptions.Item>
              <Descriptions.Item label="词条">{detail.word}</Descriptions.Item>
              <Descriptions.Item label="排序">{detail.sortNo}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{detail.createTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="修改时间">{detail.modifyTime || '-'}</Descriptions.Item>
            </Descriptions>
            <Typography.Text strong>拓展配置（ext_info）</Typography.Text>
            <pre style={{
              marginTop: 8,
              maxHeight: 360,
              overflow: 'auto',
              padding: 12,
              background: '#f6f8fa',
              borderRadius: 6,
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word'
            }}>{formatExtInfo(detail.extInfo) || '（空）'}</pre>
          </>
        )}
      </Modal>
    </div>
  );
};

export default WordEntryManagement;
