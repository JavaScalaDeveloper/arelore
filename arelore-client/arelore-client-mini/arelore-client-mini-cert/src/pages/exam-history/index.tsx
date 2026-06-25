import React, { useEffect, useState } from 'react';
import Taro from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { postWithAuth } from '../../utils/request';
import './index.scss';

function parseHistoryExtra(raw) {
  if (raw == null || raw === '') return {};
  if (typeof raw === 'object') return raw || {};
  try {
    return JSON.parse(raw) || {};
  } catch (e) {
    return {};
  }
}

const ExamHistoryPage = () => {
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState('');
  const [list, setList] = useState([]);
  const [typeNameMap, setTypeNameMap] = useState({});

  useEffect(() => {
    (async () => {
      try {
        const resp = await postWithAuth('/user/detection/type/all', {});
        if (resp && resp.code === 200 && Array.isArray(resp.data)) {
          const m = {};
          resp.data.forEach((t) => {
            const code = t?.typeCode;
            if (code) {
              m[code] = t?.typeName || t?.typeDescription || code;
            }
          });
          setTypeNameMap(m);
        }
      } catch (e) {
        /* 试卷名称映射失败时仍展示 typeCode */
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      const currentUser = Taro.getStorageSync('currentUser') || {};
      const userId = String(currentUser?.userId || currentUser?.id || '');
      if (!userId) {
        setErrMsg('请先登录');
        return;
      }
      try {
        setLoading(true);
        setErrMsg('');
        const resp = await postWithAuth('/user/detection/result/history/submitted', { userId });
        if (resp && resp.code === 200) {
          setList(resp.data || []);
          return;
        }
        setErrMsg(resp?.message || '查询考试记录失败');
      } catch (e) {
        setErrMsg(e?.errMsg || '查询考试记录失败');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const openResultDetail = (it) => {
    const tc = it?.userDetectTypeCode || '';
    if (!tc) {
      Taro.showToast({ title: '记录缺少试卷编号', icon: 'none' });
      return;
    }
    const extraParsed = parseHistoryExtra(it?.extraInfo);
    const displayName = typeNameMap[tc] || tc;
    Taro.setStorageSync('quizLastResult', {
      typeCode: tc,
      typeName: displayName,
      detectResult: it?.userDetectResult || '',
      extraInfo: extraParsed,
      savedAt: Date.now(),
      source: 'exam-history'
    });
    Taro.navigateTo({
      url: `/pages/quiz-result/index?typeCode=${encodeURIComponent(tc)}&typeName=${encodeURIComponent(displayName)}`
    });
  };

  return (
    <View className='page'>
      <View className='pageHead'>
        <Text className='title'>考试记录</Text>
        <Text className='sub'>点击查看得分与答题卡（仅解析，不可作答）</Text>
      </View>
      {loading ? <View className='tip'>加载中...</View> : null}
      {errMsg ? <View className='tip error'>{errMsg}</View> : null}
      {!loading && !errMsg && list.length === 0 ? <View className='tip empty'>暂无已交卷记录</View> : null}
      <View className='list'>
        {list.map((it) => {
          const id = it?.id || `${it?.userDetectTypeCode}_${it?.createTime || ''}`;
          const tc = it?.userDetectTypeCode || '-';
          const paperTitle = typeNameMap[tc] || tc;
          return (
            <View key={id} className='item' onClick={() => openResultDetail(it)}>
              <View className='itemTitleRow'>
                <Text className='paperName' numberOfLines={2}>
                  {paperTitle}
                </Text>
                <Text className='chevron'>›</Text>
              </View>
              <View className='codeRow'>
                <Text className='codeLabel'>试卷编号</Text>
                <Text className='codeValue'>{tc}</Text>
              </View>
              <View className='row'>
                <Text className='label'>得分</Text>
                <Text className='value score'>{it?.userDetectResult || '-'}</Text>
              </View>
              <View className='row'>
                <Text className='label'>交卷时间</Text>
                <Text className='value'>{it?.createTime ? String(it.createTime) : '-'}</Text>
              </View>
            </View>
          );
        })}
      </View>
    </View>
  );
};

export default ExamHistoryPage;
