import React, { useCallback, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { post, postWithAuth } from '../../utils/request';
import './index.scss';

const stripImageMarkers = (text) => {
  if (!text) return '';
  return text
    .replace(/【img:[0-9a-fA-F]{64}】/g, '')
    .replace(/\(img:[0-9a-fA-F]{64}\)/g, '')
    .trim();
};

const parseOptions = (raw) => {
  if (!raw) return [];
  try {
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return [];
  }
};

const resolveStemAndDesc = (q) => {
  let stem = q?.questionName || '';
  let desc = q?.questionDescription || '';
  try {
    const ext = q?.extraInfo ? JSON.parse(q.extraInfo) : {};
    if (ext?.fullQuestionName) stem = ext.fullQuestionName;
    if (ext?.fullQuestionDescription) desc = ext.fullQuestionDescription;
  } catch (e) {
    // ignore
  }
  return { stem: stripImageMarkers(stem), desc: stripImageMarkers(desc) };
};

const FavoriteDetailPage = () => {
  const [typeCode, setTypeCode] = useState('');
  const [questionCode, setQuestionCode] = useState('');
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState('');

  const getUserId = () => {
    const u = Taro.getStorageSync('currentUser') || {};
    return String(u?.userId || u?.id || '');
  };

  const loadQuestion = useCallback(async (tc, qc) => {
    if (!tc || !qc) {
      setErrMsg('缺少题目参数');
      return;
    }
    try {
      setLoading(true);
      setErrMsg('');
      const resp = await post('/user/detection/question/one', { typeCode: tc, questionCode: qc });
      if (resp && resp.code === 200 && resp.data) {
        setQuestion(resp.data);
        return;
      }
      setQuestion(null);
      setErrMsg(resp?.message || '题目不存在或已下架');
    } catch (e) {
      setErrMsg(e?.errMsg || '加载失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useDidShow(() => {
    const instance = Taro.getCurrentInstance();
    const tc = decodeURIComponent(instance?.router?.params?.typeCode || '');
    const qc = decodeURIComponent(instance?.router?.params?.questionCode || '');
    setTypeCode(tc);
    setQuestionCode(qc);
    loadQuestion(tc, qc);
  });

  const onUnfavorite = async () => {
    const userId = getUserId();
    if (!userId || !typeCode || !questionCode) {
      Taro.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    const confirm = await Taro.showModal({
      title: '取消收藏',
      content: '确定从收藏夹中移除本题吗？'
    });
    if (!confirm.confirm) return;
    const resp = await postWithAuth('/user/detection/question/favorite/toggle', {
      userId,
      questionTypeCode: typeCode,
      questionCode,
      favorite: false
    });
    if (!resp || resp.code !== 200) {
      Taro.showToast({ title: resp?.message || '操作失败', icon: 'none' });
      return;
    }
    Taro.showToast({ title: '已取消收藏', icon: 'success' });
    setTimeout(() => {
      try {
        const pages = Taro.getCurrentPages();
        if (pages.length > 1) {
          Taro.navigateBack({
            fail: () => {
              Taro.switchTab({ url: '/pages/profile/index' });
            }
          });
        } else {
          Taro.switchTab({ url: '/pages/profile/index' });
        }
      } catch (_e) {
        Taro.switchTab({ url: '/pages/profile/index' });
      }
    }, 400);
  };

  const { stem, desc } = question ? resolveStemAndDesc(question) : { stem: '', desc: '' };
  const options = question ? parseOptions(question.options) : [];

  return (
    <View className='page'>
      {loading ? <View className='tip'>加载中...</View> : null}
      {errMsg ? <View className='tip error'>{errMsg}</View> : null}
      {!loading && !errMsg && question ? (
        <View className='card'>
          <View className='meta'>
            {typeCode} · {questionCode}
          </View>
          <View className='stem'>{stem || '（无题干）'}</View>
          {desc ? <View className='desc'>{desc}</View> : null}
          <View className='sectionTitle'>选项</View>
          {options.length === 0 ? (
            <View className='tip'>暂无选项数据</View>
          ) : (
            options.map((op) => (
              <View key={op.key || op.text} className='option'>
                <Text className='optionKey'>{op.key}.</Text>
                <Text className='optionText'>{stripImageMarkers(op.text)}</Text>
              </View>
            ))
          )}
        </View>
      ) : null}

      {!loading && !errMsg && question ? (
        <View className='footer'>
          <View className='unfavBtn' onClick={onUnfavorite}>
            <Text>取消收藏</Text>
          </View>
        </View>
      ) : null}
    </View>
  );
};

export default FavoriteDetailPage;
