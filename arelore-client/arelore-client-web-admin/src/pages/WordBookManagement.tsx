import React, { useEffect, useState } from 'react';
import { Button, Drawer, Form, Input, InputNumber, Modal, Select, Space, Table, Tooltip, message } from 'antd';
import { DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { BookCoverPreview, EntryMediaPreview } from '../components/WordMediaPreview';
import { adminApi } from '../api/admin';
import { ellipsisColumn } from '../utils/tableCell';
import { resolveBookCover } from '../utils/wordMedia';
import { wordCodeRules } from '../utils/wordCode';

const DEFAULT_PAGE_SIZE = 20;
const ENTRY_PAGE_SIZE = 20;

const WordBookManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [languages, setLanguages] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [form] = Form.useForm();
  const [queryForm] = Form.useForm();

  const [detailBook, setDetailBook] = useState<any | null>(null);
  const [entryList, setEntryList] = useState<any[]>([]);
  const [entryTotal, setEntryTotal] = useState(0);
  const [entryPageNum, setEntryPageNum] = useState(1);
  const [entryPageSize, setEntryPageSize] = useState(ENTRY_PAGE_SIZE);
  const [entryLoading, setEntryLoading] = useState(false);
  const [entryKeyword, setEntryKeyword] = useState('');
  const [entryDetailVisible, setEntryDetailVisible] = useState(false);
  const [entryDetail, setEntryDetail] = useState<any | null>(null);

  const loadMeta = async () => {
    const [catRes, langRes] = await Promise.all([
      adminApi.getWordCategoryList({ pageNum: 1, pageSize: 100 }),
      adminApi.getWordLanguageList({ pageNum: 1, pageSize: 100 })
    ]);
    setCategories(catRes.data?.list || []);
    setLanguages(langRes.data?.list || []);
  };

  const loadData = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    delete filters.pageNum;
    delete filters.pageSize;
    setLoading(true);
    try {
      const bookRes = await adminApi.getWordBookList({
        ...filters,
        pageNum: nextPageNum,
        pageSize: nextPageSize
      });
      setDataSource(bookRes.data?.list || []);
      setTotal(Number(bookRes.data?.total) || 0);
      setPageNum(bookRes.data?.pageNum || nextPageNum);
      setPageSize(bookRes.data?.pageSize || nextPageSize);
    } finally {
      setLoading(false);
    }
  };

  const loadEntries = async (bookCode: string, nextPageNum = 1, nextPageSize = ENTRY_PAGE_SIZE, word = '') => {
    setEntryLoading(true);
    try {
      const res = await adminApi.getWordEntryList({
        bookCode,
        word: word || undefined,
        pageNum: nextPageNum,
        pageSize: nextPageSize
      });
      setEntryList(res.data?.list || []);
      setEntryTotal(Number(res.data?.total) || 0);
      setEntryPageNum(res.data?.pageNum || nextPageNum);
      setEntryPageSize(res.data?.pageSize || nextPageSize);
    } finally {
      setEntryLoading(false);
    }
  };

  const openBookDetail = (book: any) => {
    setDetailBook(book);
    setEntryKeyword('');
    loadEntries(book.code, 1, ENTRY_PAGE_SIZE, '');
  };

  const openEntryDetail = async (record: any) => {
    const res = await adminApi.getWordEntryDetail({ id: record.id });
    setEntryDetail(res.data || record);
    setEntryDetailVisible(true);
  };

  useEffect(() => {
    loadMeta();
    loadData({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
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
    loadData({ pageNum: 1 });
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h1 style={{ margin: 0 }}>单词本</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); form.setFieldsValue({ wordCount: 0, status: 1 }); setModalVisible(true); }}>新增单词本</Button>
      </div>
      <Form form={queryForm} layout="inline" style={{ marginBottom: 16 }} onFinish={(values) => loadData({ pageNum: 1, pageSize, filters: values })}>
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
        size="middle"
        tableLayout="fixed"
        scroll={{ x: 960 }}
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
          { title: 'ID', dataIndex: 'id', width: 80 },
          {
            title: '封面',
            dataIndex: 'cover',
            width: 72,
            render: (_: unknown, record: any) => (
              <BookCoverPreview cover={resolveBookCover(record)} name={record.name} />
            )
          },
          ellipsisColumn('code', 'code', 160),
          ellipsisColumn('名称', 'name', 140),
          ellipsisColumn('语种', 'languageCode', 90),
          ellipsisColumn('分类', 'categoryCode', 110),
          { title: '词数', dataIndex: 'wordCount', width: 72, align: 'center' as const },
          { title: '状态', dataIndex: 'status', width: 72, align: 'center' as const },
          {
            title: '操作',
            width: 120,
            fixed: 'right' as const,
            render: (_, record) => (
              <Space size={0}>
                <Tooltip title="详情"><Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openBookDetail(record)} /></Tooltip>
                <Tooltip title="编辑"><Button type="link" size="small" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalVisible(true); }} /></Tooltip>
                <Tooltip title="删除"><Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => {
                  Modal.confirm({ title: '确认删除', onOk: async () => { await adminApi.deleteWordBook({ id: record.id }); message.success('删除成功'); loadData(); } });
                }} /></Tooltip>
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

      <Drawer
        title={detailBook ? `词条 · ${detailBook.code} · ${detailBook.name}` : '词条'}
        width={860}
        open={!!detailBook}
        onClose={() => setDetailBook(null)}
        destroyOnClose
      >
        {detailBook && (
          <div style={{ marginBottom: 16, display: 'flex', gap: 16, alignItems: 'flex-start' }}>
            <BookCoverPreview cover={resolveBookCover(detailBook)} name={detailBook.name} size={96} />
            <div style={{ lineHeight: 1.7 }}>
              <div><b>{detailBook.name}</b></div>
              <div style={{ color: '#888' }}>{detailBook.code}</div>
              <div style={{ color: '#888' }}>词数 {detailBook.wordCount ?? 0}</div>
              {detailBook.originName && <div style={{ color: '#888' }}>来源 {detailBook.originName}</div>}
            </div>
          </div>
        )}
        <Space style={{ marginBottom: 16 }}>
          <Input
            allowClear
            placeholder="词条原文前缀"
            value={entryKeyword}
            onChange={(e) => setEntryKeyword(e.target.value)}
            onPressEnter={() => detailBook && loadEntries(detailBook.code, 1, entryPageSize, entryKeyword)}
            style={{ width: 220 }}
          />
          <Button
            type="primary"
            icon={<SearchOutlined />}
            onClick={() => detailBook && loadEntries(detailBook.code, 1, entryPageSize, entryKeyword)}
          >
            查询
          </Button>
          <span style={{ color: '#888' }}>共 {entryTotal} 词</span>
        </Space>
        <Table
          rowKey="id"
          loading={entryLoading}
          dataSource={entryList}
          size="middle"
          tableLayout="fixed"
          pagination={{
            current: entryPageNum,
            pageSize: entryPageSize,
            total: entryTotal,
            showSizeChanger: true,
            pageSizeOptions: ['20', '50', '100'],
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page, size) => {
              if (detailBook) {
                loadEntries(detailBook.code, page, size || ENTRY_PAGE_SIZE, entryKeyword);
              }
            }
          }}
          columns={[
            { title: 'ID', dataIndex: 'id', width: 80 },
            ellipsisColumn('单词 code', 'wordCode', 200),
            ellipsisColumn('词条', 'word', 140),
            { title: '排序', dataIndex: 'sortNo', width: 72, align: 'center' as const },
            {
              title: '操作',
              width: 72,
              render: (_, record) => (
                <Tooltip title="查看">
                  <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openEntryDetail(record)} />
                </Tooltip>
              )
            }
          ]}
        />
      </Drawer>

      <Modal
        title={entryDetail ? `词条详情 · ${entryDetail.word}` : '词条详情'}
        open={entryDetailVisible}
        width={720}
        footer={null}
        onCancel={() => { setEntryDetailVisible(false); setEntryDetail(null); }}
      >
        {entryDetail && (
          <div style={{ lineHeight: 1.8 }}>
            <div><b>词本 code</b>：{entryDetail.bookCode}</div>
            <div><b>词本名称</b>：{entryDetail.bookName || '-'}</div>
            <div><b>单词 code</b>：{entryDetail.wordCode}</div>
            <div><b>词条</b>：{entryDetail.word}</div>
            <div><b>排序</b>：{entryDetail.sortNo}</div>
            <div style={{ marginTop: 12 }}>
              <EntryMediaPreview word={entryDetail.word} extInfo={entryDetail.extInfo} />
            </div>
            <div style={{ marginTop: 12 }}><b>拓展 JSON</b></div>
            <pre style={{ background: '#f5f5f5', padding: 12, maxHeight: 360, overflow: 'auto', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
              {(() => {
                try {
                  return JSON.stringify(JSON.parse(entryDetail.extInfo || '{}'), null, 2);
                } catch {
                  return entryDetail.extInfo || '';
                }
              })()}
            </pre>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default WordBookManagement;
