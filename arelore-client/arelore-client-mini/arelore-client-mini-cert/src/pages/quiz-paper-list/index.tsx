import React, { useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { post } from '../../utils/request';
import './index.scss';

const QuizPaperListPage = () => {
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [subjectName, setSubjectName] = useState('软件设计师');

  useDidShow(() => {
    const instance = Taro.getCurrentInstance();
    const examCategory = instance?.router?.params?.examCategory || '软考';
    const subject = instance?.router?.params?.subject || '软件设计师';
    setSubjectName(subject);

    (async () => {
      try {
        setLoading(true);
        setError('');
        const resp = await post('/user/detection/paper/list', { examCategory, subject });
        if (resp && resp.code === 200) {
          setPapers(resp.data || []);
          return;
        }
        setPapers([]);
        setError(resp?.message || '查询试卷列表失败');
      } catch (e) {
        setPapers([]);
        setError(e?.errMsg || '查询试卷列表失败');
      } finally {
        setLoading(false);
      }
    })();
  });

  const onGoDoPaper = (paper) => {
    Taro.navigateTo({
      url: `/pages/quiz-do/index?typeCode=${encodeURIComponent(paper.typeCode || '')}&typeName=${encodeURIComponent(
        paper.typeName || ''
      )}`
    });
  };

  return (
    <View className='page'>
      <View className='card'>
        <View className='title'>{subjectName} - 试卷列表</View>
        {loading ? <View className='tip'>加载中...</View> : null}
        {error ? <View className='tip error'>{error}</View> : null}
        {!loading && !error && papers.length === 0 ? <View className='tip'>暂无可用试卷</View> : null}
        {papers.map((p) => (
          <View key={p.typeCode} className='paperItem' onClick={() => onGoDoPaper(p)}>
            <Text className='paperName'>{p.typeName || p.typeCode}</Text>
            <Text className='paperCode'>{p.typeCode}</Text>
            <Text className='paperAction'>开始做题 ></Text>
          </View>
        ))}
      </View>
    </View>
  );
};

export default QuizPaperListPage;

