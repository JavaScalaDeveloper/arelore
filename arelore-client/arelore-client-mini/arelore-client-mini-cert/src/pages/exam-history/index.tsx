import React, { useEffect, useState } from 'react';
import Taro from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { postWithAuth } from '../../utils/request';
import './index.scss';

const ExamHistoryPage = () => {
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState('');
  const [list, setList] = useState([]);

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

  return (
    <View className='page'>
      <View className='title'>考试记录</View>
      {loading ? <View className='tip'>加载中...</View> : null}
      {errMsg ? <View className='tip error'>{errMsg}</View> : null}
      {!loading && !errMsg && list.length === 0 ? <View className='tip'>暂无已交卷记录</View> : null}
      <View className='list'>
        {list.map((it) => {
          const id = it?.id || `${it?.userDetectTypeCode}_${it?.createTime || ''}`;
          return (
            <View
              key={id}
              className='item'
              onClick={() => {
                const typeCode = encodeURIComponent(it?.userDetectTypeCode || '');
                const typeName = encodeURIComponent(it?.userDetectTypeCode || '');
                Taro.navigateTo({ url: `/pages/quiz-do/index?typeCode=${typeCode}&typeName=${typeName}` });
              }}
            >
              <View className='row'>
                <Text className='label'>试卷：</Text>
                <Text className='value'>{it?.userDetectTypeCode || '-'}</Text>
              </View>
              <View className='row'>
                <Text className='label'>结果：</Text>
                <Text className='value'>{it?.userDetectResult || '-'}</Text>
              </View>
              <View className='row'>
                <Text className='label'>时间：</Text>
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

