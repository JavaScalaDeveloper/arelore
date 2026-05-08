import React, { useCallback, useMemo, useRef, useState } from 'react';
import Taro, { useDidShow, useReachBottom } from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { postWithAuth } from '../../utils/request';
import './index.scss';

const PAGE_SIZE = 20;

function stripImageMarkers(text) {
  if (!text) return '';
  return String(text)
    .replace(/【img:[0-9a-fA-F]{64}】/g, '')
    .replace(/\(img:[0-9a-fA-F]{64}\)/g, '')
    .replace(/\s+/g, ' ')
    .trim();
}

/** 列表区只做字符级缩略，配合样式多行省略，避免题干占满屏 */
function previewStem(name, maxLen = 72) {
  const s = stripImageMarkers(name);
  if (!s) return '（题目已删除或暂无题干）';
  if (s.length <= maxLen) return s;
  return `${s.slice(0, maxLen)}…`;
}

const FavoritesPage = () => {
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [errMsg, setErrMsg] = useState('');
  const [list, setList] = useState([]);
  const [pageNum, setPageNum] = useState(1);
  const [total, setTotal] = useState(0);
  const loadingRef = useRef(false);

  const hasMore = list.length < total;

  const fetchPage = useCallback(async (nextPage, append) => {
    const currentUser = Taro.getStorageSync('currentUser') || {};
    const userId = String(currentUser?.userId || currentUser?.id || '');
    if (!userId) {
      setErrMsg('请先登录');
      setList([]);
      setTotal(0);
      return;
    }
    if (loadingRef.current) return;
    loadingRef.current = true;
    try {
      if (append) {
        setLoadingMore(true);
      } else {
        setLoading(true);
      }
      setErrMsg('');
      const resp = await postWithAuth('/user/detection/question/favorite/page', {
        userId,
        pageNum: nextPage,
        pageSize: PAGE_SIZE
      });
      if (resp && resp.code === 200 && resp.data) {
        const records = Array.isArray(resp.data.records) ? resp.data.records : [];
        const t = Number(resp.data.total) || 0;
        setTotal(t);
        setPageNum(nextPage);
        setList((prev) => (append ? [...prev, ...records] : records));
        return;
      }
      setErrMsg(resp?.message || '查询收藏夹失败');
      if (!append) {
        setList([]);
        setTotal(0);
      }
    } catch (e) {
      setErrMsg(e?.errMsg || '查询收藏夹失败');
      if (!append) {
        setList([]);
        setTotal(0);
      }
    } finally {
      loadingRef.current = false;
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  const loadFirstPage = useCallback(() => {
    setPageNum(1);
    fetchPage(1, false);
  }, [fetchPage]);

  const loadMore = useCallback(() => {
    if (!hasMore || loading || loadingMore || errMsg) return;
    fetchPage(pageNum + 1, true);
  }, [fetchPage, hasMore, loading, loadingMore, errMsg, pageNum]);

  useDidShow(() => {
    loadFirstPage();
  });

  useReachBottom(() => {
    loadMore();
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
        <Text className='sub'>上拉加载更多 · 点击查看题目详情</Text>
      </View>
      {loading ? <View className='tip'>加载中...</View> : null}
      {errMsg ? <View className='tip error'>{errMsg}</View> : null}
      {!loading && !errMsg && list.length === 0 ? <View className='tip empty'>暂无收藏</View> : null}

      {typeCodes.map((tc) => {
        const rows = grouped[tc] || [];
        const groupLabel = rows[0]?.typeName || rows[0]?.questionTypeCode || tc;
        return (
          <View key={tc} className='group'>
            <View className='groupTitle'>
              <Text className='groupTitleText'>{groupLabel}</Text>
            </View>
            <View className='list'>
              {rows.map((it) => {
                const key = it?.id || `${it?.questionTypeCode}_${it?.questionCode}`;
                return (
                  <View key={key} className='item' onClick={() => openDetail(it)}>
                    <View className='itemTop'>
                      <Text className='itemStem' numberOfLines={3}>
                        {previewStem(it?.questionName)}
                      </Text>
                      <Text className='itemHint'>详情 ›</Text>
                    </View>
                    <View className='itemTime'>
                      <Text>收藏于 {it?.createTime ? String(it.createTime) : '-'}</Text>
                    </View>
                  </View>
                );
              })}
            </View>
          </View>
        );
      })}

      {loadingMore ? <View className='tip footerTip'>加载更多…</View> : null}
      {!loading && !loadingMore && list.length > 0 && !hasMore ? (
        <View className='tip footerTip'>已加载全部 {total} 条</View>
      ) : null}
    </View>
  );
};

export default FavoritesPage;
