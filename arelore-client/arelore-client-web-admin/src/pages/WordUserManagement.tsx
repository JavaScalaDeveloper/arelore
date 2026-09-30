import React, { useEffect, useState } from 'react';
import { Button, Drawer, Form, Input, Space, Table, Tabs, Tooltip, Typography, message } from 'antd';
import { EyeOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin';
import { ellipsisColumn } from '../utils/tableCell';

const DEFAULT_PAGE_SIZE = 20;

const parseExt = (raw?: string) => {
  if (!raw) {
    return {};
  }
  try {
    return JSON.parse(raw);
  } catch {
    return {};
  }
};

const resolveUserIdStr = (user: any): string => {
  if (!user) {
    return '';
  }
  if (user.idStr != null && String(user.idStr).trim() !== '') {
    return String(user.idStr).trim();
  }
  return '';
};

const WordUserManagement: React.FC = () => {
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [queryForm] = Form.useForm();

  const [activeUser, setActiveUser] = useState<any | null>(null);
  const [bookLoading, setBookLoading] = useState(false);
  const [currentBooks, setCurrentBooks] = useState<any[]>([]);
  const [studyPlans, setStudyPlans] = useState<any[]>([]);
  const [recordLoading, setRecordLoading] = useState(false);
  const [learnRecords, setLearnRecords] = useState<any[]>([]);
  const [recordTotal, setRecordTotal] = useState(0);
  const [recordPageNum, setRecordPageNum] = useState(1);
  const [recordPageSize, setRecordPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [recordBookCode, setRecordBookCode] = useState('');
  const [recordWordCode, setRecordWordCode] = useState('');

  const loadUsers = async (overrides?: { pageNum?: number; pageSize?: number; filters?: any }) => {
    const nextPageNum = overrides?.pageNum ?? pageNum;
    const nextPageSize = overrides?.pageSize ?? pageSize;
    const filters = { ...(overrides?.filters ?? queryForm.getFieldsValue()) };
    setLoading(true);
    try {
      const res = await adminApi.getWordUserList({
        ...filters,
        pageNum: nextPageNum,
        pageSize: nextPageSize
      });
      setDataSource(res.data?.list || []);
      setTotal(Number(res.data?.total) || 0);
      setPageNum(res.data?.pageNum || nextPageNum);
      setPageSize(res.data?.pageSize || nextPageSize);
    } finally {
      setLoading(false);
    }
  };

  const loadBookLevel = async (idStr: string) => {
    if (!idStr) {
      setCurrentBooks([]);
      setStudyPlans([]);
      return;
    }
    setBookLoading(true);
    try {
      const [currentRes, planRes] = await Promise.all([
        adminApi.getWordUserCurrentBookList({ idStr, pageNum: 1, pageSize: 100 }),
        adminApi.getWordUserStudyPlanList({ idStr, pageNum: 1, pageSize: 100 })
      ]);
      setCurrentBooks(currentRes.data?.list || []);
      setStudyPlans(planRes.data?.list || []);
    } finally {
      setBookLoading(false);
    }
  };

  const loadLearnRecords = async (
    idStr: string,
    nextPageNum = 1,
    nextPageSize = DEFAULT_PAGE_SIZE,
    bookCode = '',
    wordCode = ''
  ) => {
    if (!idStr) {
      setLearnRecords([]);
      setRecordTotal(0);
      return;
    }
    setRecordLoading(true);
    try {
      const res = await adminApi.getWordUserLearnRecordList({
        idStr,
        bookCode: bookCode || undefined,
        wordCode: wordCode || undefined,
        pageNum: nextPageNum,
        pageSize: nextPageSize
      });
      setLearnRecords(res.data?.list || []);
      setRecordTotal(Number(res.data?.total) || 0);
      setRecordPageNum(res.data?.pageNum || nextPageNum);
      setRecordPageSize(res.data?.pageSize || nextPageSize);
    } finally {
      setRecordLoading(false);
    }
  };

  const openUserDetail = async (user: any) => {
    setActiveUser(user);
    setRecordBookCode('');
    setRecordWordCode('');
    const idStr = resolveUserIdStr(user);
    await Promise.all([
      loadBookLevel(idStr),
      loadLearnRecords(idStr, 1, DEFAULT_PAGE_SIZE, '', '')
    ]);
  };

  useEffect(() => {
    loadUsers({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, filters: {} });
  }, []);

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>学习用户</h1>
        <Typography.Text type="secondary">查看注册用户、词书进度与单词学习记录</Typography.Text>
      </div>
      <Form
        form={queryForm}
        layout="inline"
        style={{ marginBottom: 16 }}
        onFinish={(values) => loadUsers({ pageNum: 1, pageSize, filters: values })}
      >
        <Form.Item name="account"><Input placeholder="账号" allowClear /></Form.Item>
        <Form.Item name="accountType"><Input placeholder="账号类型 WECHAT/MOBILE" allowClear /></Form.Item>
        <Form.Item name="idStr"><Input placeholder="用户 ID" allowClear /></Form.Item>
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
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: ['20', '50', '100'],
          showTotal: (t) => `共 ${t} 条`,
          onChange: (nextPage, nextSize) => loadUsers({ pageNum: nextPage, pageSize: nextSize || DEFAULT_PAGE_SIZE })
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          ellipsisColumn('用户 ID', 'idStr', 200),
          ellipsisColumn('账号类型', 'accountType', 100),
          ellipsisColumn('账号', 'account', 160),
          { title: '状态', dataIndex: 'status', width: 72, align: 'center' as const },
          ellipsisColumn('创建时间', 'createTime', 160),
          {
            title: '操作',
            width: 88,
            fixed: 'right' as const,
            render: (_, record) => (
              <Tooltip title="学习记录">
                <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openUserDetail(record)} />
              </Tooltip>
            )
          }
        ]}
      />

      <Drawer
        title={activeUser ? `学习记录 · ${activeUser.account || activeUser.idStr}` : '学习记录'}
        width={920}
        open={!!activeUser}
        onClose={() => setActiveUser(null)}
        destroyOnClose
      >
        <Tabs
          items={[
            {
              key: 'book',
              label: '词书级',
              children: (
                <Space direction="vertical" style={{ width: '100%' }} size={16}>
                  <div>
                    <Typography.Text strong>当前词书进度</Typography.Text>
                    <Table
                      style={{ marginTop: 8 }}
                      rowKey="id"
                      size="small"
                      loading={bookLoading}
                      dataSource={currentBooks}
                      pagination={false}
                      columns={[
                        ellipsisColumn('词本', 'bookCode', 160),
                        { title: '新学完成', dataIndex: 'learnDone', width: 90 },
                        { title: '新学待办', dataIndex: 'learnTodo', width: 90 },
                        { title: '复习完成', dataIndex: 'reviewDone', width: 90 },
                        { title: '复习待办', dataIndex: 'reviewTodo', width: 90 },
                        {
                          title: '操作',
                          width: 100,
                          render: (_, record) => (
                            <Button
                              type="link"
                              size="small"
                              onClick={() => {
                                setRecordBookCode(record.bookCode || '');
                                const idStr = resolveUserIdStr(activeUser);
                                if (idStr) {
                                  loadLearnRecords(idStr, 1, recordPageSize, record.bookCode || '', recordWordCode);
                                  message.info(`已筛选词本 ${record.bookCode}`);
                                }
                              }}
                            >
                              看单词记录
                            </Button>
                          )
                        }
                      ]}
                    />
                  </div>
                  <div>
                    <Typography.Text strong>学习计划</Typography.Text>
                    <Table
                      style={{ marginTop: 8 }}
                      rowKey="id"
                      size="small"
                      loading={bookLoading}
                      dataSource={studyPlans}
                      pagination={false}
                      columns={[
                        ellipsisColumn('词本', 'bookCode', 140),
                        { title: '比例', dataIndex: 'newReviewRatio', width: 90 },
                        { title: '每日新学', dataIndex: 'dailyNewCount', width: 90 },
                        { title: '每日复习', dataIndex: 'dailyReviewCount', width: 90 },
                        { title: '计划天数', dataIndex: 'planDays', width: 90 },
                        {
                          title: '发音',
                          width: 80,
                          render: (_, record) => {
                            const ext = parseExt(record.extInfo);
                            return ext.voiceType === 1 ? '英音' : (ext.voiceType === 2 ? '美音' : '-');
                          }
                        }
                      ]}
                    />
                  </div>
                </Space>
              )
            },
            {
              key: 'word',
              label: '单词级',
              children: (
                <div>
                  <Space style={{ marginBottom: 12 }} wrap>
                    <Input
                      allowClear
                      placeholder="词本 code"
                      value={recordBookCode}
                      onChange={(e) => setRecordBookCode(e.target.value)}
                      style={{ width: 180 }}
                    />
                    <Input
                      allowClear
                      placeholder="单词 code"
                      value={recordWordCode}
                      onChange={(e) => setRecordWordCode(e.target.value)}
                      style={{ width: 180 }}
                    />
                    <Button
                      type="primary"
                      icon={<SearchOutlined />}
                      onClick={() => {
                        const idStr = resolveUserIdStr(activeUser);
                        if (idStr) {
                          loadLearnRecords(idStr, 1, recordPageSize, recordBookCode, recordWordCode);
                        }
                      }}
                    >
                      查询
                    </Button>
                  </Space>
                  <Table
                    rowKey="id"
                    size="small"
                    loading={recordLoading}
                    dataSource={learnRecords}
                    pagination={{
                      current: recordPageNum,
                      pageSize: recordPageSize,
                      total: recordTotal,
                      showSizeChanger: true,
                      pageSizeOptions: ['20', '50', '100'],
                      showTotal: (t) => `共 ${t} 条`,
                      onChange: (page, size) => {
                        const idStr = resolveUserIdStr(activeUser);
                        if (idStr) {
                          loadLearnRecords(idStr, page, size || DEFAULT_PAGE_SIZE, recordBookCode, recordWordCode);
                        }
                      }
                    }}
                    columns={[
                      ellipsisColumn('词本', 'bookCode', 140),
                      ellipsisColumn('单词 code', 'wordCode', 140),
                      {
                        title: '单词',
                        width: 120,
                        ellipsis: true,
                        render: (_, record) => parseExt(record.extInfo).word || '-'
                      },
                      {
                        title: '认识',
                        width: 72,
                        render: (_, record) => {
                          const flag = parseExt(record.extInfo).rememberFlag;
                          if (flag === true || flag === 1 || flag === '1') return '是';
                          if (flag === false || flag === 0 || flag === '0') return '否';
                          return '-';
                        }
                      },
                      {
                        title: '学习次数',
                        width: 88,
                        render: (_, record) => parseExt(record.extInfo).learnCount ?? '-'
                      },
                      {
                        title: '复习次数',
                        width: 88,
                        render: (_, record) => parseExt(record.extInfo).reviewCount ?? '-'
                      },
                      {
                        title: '最近学习',
                        width: 160,
                        ellipsis: true,
                        render: (_, record) => parseExt(record.extInfo).lastLearnTime || '-'
                      }
                    ]}
                  />
                </div>
              )
            }
          ]}
        />
      </Drawer>
    </div>
  );
};

export default WordUserManagement;
