import React, { useCallback, useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { postWithAuth } from '../../utils/request';
import './index.scss';

const FavoritesPage = () => {
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState('');
  const [list, setList] = useState([]);

  const loadList = useCallback(async () => {
    const currentUser = Taro.getStorageSync('currentUser') || {};
    const userId = String(currentUser?.userId || currentUser?.id || '');
    if (!userId) {
      setErrMsg('请先登录');
      setList([]);
      return;
    }
    try {
      setLoading(true);
      setErrMsg('');
      const resp = await postWithAuth('/user/detection/question/favorite/list-detail', { userId });
      if (resp && resp.code === 200) {
        setList(resp.data || []);
        return;
      }
      setErrMsg(resp?.message || '查询收藏夹失败');
      setList([]);
    } catch (e) {
      setErrMsg(e?.errMsg || '查询收藏夹失败');
      setList([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useDidShow(() => {
    loadList();
  });

  const grouped = useMemo(() => {
    const map = {};
    (list || []).forEach((it) => {
      const typeCode = it?.questionTypeCode || 'UNKNOWN';
      if (!map[typeCode]) map[typeCode] = [];
      map[typeCode].push(it);
    });
    return map;
  }, [list]);

  const typeCodes = useMemo(() => Object.keys(grouped || {}), [grouped]);

  const openDetail = (it) => {
    const tc = encodeURIComponent(it?.questionTypeCode || '');
    const qc = encodeURIComponent(it?.questionCode || '');
    Taro.navigateTo({ url: `/pages/favorite-detail/index?typeCode=${tc}&questionCode=${qc}` });
  };

  return (
    <View className='page'>
      <View className='head'>
        <Text className='title'>收藏夹</Text>
        <Text className='sub'>点击查看题目详情（不做题）</Text>
      </View>
      {loading ? <View className='tip'>加载中...</View> : null}
      {errMsg ? <View className='tip error'>{errMsg}</View> : null}
      {!loading && !errMsg && list.length === 0 ? <View className='tip empty'>暂无收藏</View> : null}

      {typeCodes.map((tc) => (
        <View key={tc} className='group'>
          <View className='groupTitle'>
            <Text className='groupTitleText'>试卷</Text>
            <Text className='groupTitleCode'>{tc}</Text>
          </View>
          <View className='list'>
            {(grouped[tc] || []).map((it) => {
              const key = it?.id || `${it?.questionTypeCode}_${it?.questionCode}`;
              return (
                <View key={key} className='item' onClick={() => openDetail(it)}>
                  <View className='itemTop'>
                    <Text className='itemCode'>{it?.questionCode || '-'}</Text>
                    <Text className='itemHint'>查看详情 ›</Text>
                  </View>
                  <View className='itemTime'>
                    <Text>收藏于 {it?.createTime ? String(it.createTime) : '-'}</Text>
                  </View>
                </View>
              );
            })}
          </View>
        </View>
      ))}
    </View>
  );
};

export default FavoritesPage;
