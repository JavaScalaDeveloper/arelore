import React, { useRef, useState } from 'react';
import Taro, { useDidShow, useLoad } from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { post } from '../../utils/request';
import { mergeWeappRouteParams } from '../../utils/weappRouteParams';
import { softExamSubjectByPaperIndex } from '../../constants/softExamPaperNav';
import { ncreSubjectByPaperIndex } from '../../constants/ncreExamPaperNav';
import './index.scss';

/** 与 pages/quiz/index 约定：无 paperIdx 时从本地读取 */
const QUIZ_PAPER_LIST_EXAM_KEY = 'quizPaperListExamCategory';
const QUIZ_PAPER_LIST_SUBJECT_KEY = 'quizPaperListSubject';

const QuizPaperListPage = () => {
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [subjectName, setSubjectName] = useState('软件设计师');
  const loadQueryRef = useRef({});

  useLoad((query) => {
    loadQueryRef.current = { ...(query || {}) };
  });

  useDidShow(() => {
    const params = { ...loadQueryRef.current, ...mergeWeappRouteParams() };
    let examCategory = (params.examCategory || '').trim();

    let subject = '';
    const rawIdx = params.paperIdx;
    if (rawIdx !== undefined && rawIdx !== null && String(rawIdx).trim() !== '') {
      const i = parseInt(String(rawIdx), 10);
      if (examCategory === 'NCRE') {
        const resolved = ncreSubjectByPaperIndex(i);
        if (resolved) {
          subject = resolved;
        }
      } else {
        const resolved = softExamSubjectByPaperIndex(i);
        if (resolved) {
          subject = resolved;
          if (!examCategory) {
            examCategory = '软考';
          }
        }
      }
    }
    if (!subject) {
      let s = (params.subject || '').trim();
      if (s) {
        try {
          s = decodeURIComponent(s);
      } catch (_e) {
        /* 已是明文 */
      }
        subject = s;
      }
    }

    if (!subject) {
      try {
        const storedSub = (Taro.getStorageSync(QUIZ_PAPER_LIST_SUBJECT_KEY) || '').trim();
        const storedExam = (Taro.getStorageSync(QUIZ_PAPER_LIST_EXAM_KEY) || '').trim();
        if (storedSub) {
          subject = storedSub;
          if (!examCategory) {
            examCategory = storedExam || '软考';
          }
        }
      } catch (_e) {
        /* ignore */
      }
    }
    if (!examCategory) {
      examCategory = '软考';
    }
    if (!subject) {
      subject = examCategory === 'NCRE' ? 'Python语言程序设计' : '软件设计师';
    }
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
            <Text className='paperAction'>{'开始做题 >'}</Text>
          </View>
        ))}
      </View>
    </View>
  );
};

export default QuizPaperListPage;

