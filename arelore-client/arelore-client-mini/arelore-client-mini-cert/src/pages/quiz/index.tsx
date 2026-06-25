import React, { useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import {
  SOFT_EXAM_CATALOG,
  softExamPaperIndexOfSubject
} from '../../constants/softExamPaperNav';
import {
  NCRE_EXAM_CATALOG,
  ncrePaperIndexOfSubject
} from '../../constants/ncreExamPaperNav';
import { emitTabBarIndex } from '../../utils/tabBarSync';
import './index.scss';

/** 与 quiz-paper-list 约定：无 paperIdx 时的回退 */
const QUIZ_PAPER_LIST_EXAM_KEY = 'quizPaperListExamCategory';
const QUIZ_PAPER_LIST_SUBJECT_KEY = 'quizPaperListSubject';

const QuizPage = () => {
  const [token, setToken] = useState('');
  const [activeRoot, setActiveRoot] = useState('SOFT_EXAM');
  const [openNcre, setOpenNcre] = useState({ 一级: true, 二级: false, 三级: false, 四级: false });

  useDidShow(() => {
    setToken(Taro.getStorageSync('token') || '');
    emitTabBarIndex(1);
  });

  const onGoLogin = () => {
    Taro.navigateTo({ url: '/pages/login/index' });
  };

  const syncPaperListStorage = (examCategory, subject) => {
    try {
      Taro.setStorageSync(QUIZ_PAPER_LIST_EXAM_KEY, examCategory);
      Taro.setStorageSync(QUIZ_PAPER_LIST_SUBJECT_KEY, subject);
    } catch (_e) {
      /* 试卷列表页仍可从 URL 参数解析，缓存失败不阻断导航 */
    }
  };

  const goPaperList = (examCategory, paperIdx, subject) => {
    syncPaperListStorage(examCategory, subject);
    const url =
      examCategory === 'NCRE'
        ? `/pages/quiz-paper-list/index?examCategory=NCRE&paperIdx=${paperIdx}`
        : `/pages/quiz-paper-list/index?paperIdx=${paperIdx}`;
    Taro.navigateTo({
      url,
      fail: (err) => {
        void Taro.showToast({
          title: err?.errMsg || '无法打开试卷列表',
          icon: 'none'
        });
      }
    });
  };

  /** tab 页上不用 navigator（易与 React 事件冲突）；整行 View + 同步 navigateTo */
  const goQuizPaperList = (subject) => {
    const paperIdx = softExamPaperIndexOfSubject(subject);
    if (paperIdx < 0) {
      void Taro.showToast({ title: '未知科目', icon: 'none' });
      return;
    }
    goPaperList('软考', paperIdx, subject);
  };

  const goNcrePaperList = (subject) => {
    const paperIdx = ncrePaperIndexOfSubject(subject);
    if (paperIdx < 0) {
      void Taro.showToast({ title: '未知科目', icon: 'none' });
      return;
    }
    goPaperList('NCRE', paperIdx, subject);
  };

  const rightTitle = useMemo(() => {
    if (activeRoot === 'SOFT_EXAM') return '软考分类';
    if (activeRoot === 'NCRE') return 'NCRE（全国计算机等级考试）';
    return '';
  }, [activeRoot]);

  return (
    <View className='page'>
      <View className='header'>
        <View className='title'>刷题</View>
        {!token ? (
          <View className='loginTip'>
            <Text>未登录也可以浏览目录；登录后可同步记录。</Text>
            <Button className='loginBtn' type='primary' size='mini' onClick={onGoLogin}>
              去登录
            </Button>
          </View>
        ) : null}
      </View>

      <View className='layout'>
        <View className='left'>
          <View
            className={`rootItem ${activeRoot === 'SOFT_EXAM' ? 'active' : ''}`}
            onClick={() => setActiveRoot('SOFT_EXAM')}
          >
            <View className='rootTitle'>软考</View>
            <View className='rootSub'>计算机技术与软件资格</View>
          </View>
          <View
            className={`rootItem ${activeRoot === 'NCRE' ? 'active' : ''}`}
            onClick={() => setActiveRoot('NCRE')}
          >
            <View className='rootTitle'>NCRE</View>
            <View className='rootSub'>全国计算机等级考试</View>
          </View>
        </View>

        <View className='right'>
          <View className='panel'>
            <View className='panelTitle'>{rightTitle}</View>

            {activeRoot === 'SOFT_EXAM' ? (
              <View className='groups'>
                {SOFT_EXAM_CATALOG.map((g) => (
                  <View key={g.level} className='group'>
                    <View className='groupTitle'>{g.level}</View>
                    <View className='list'>
                      {g.items.map((it) => (
                        <View
                          key={it}
                          className='softPaperRow'
                          hoverClass='softPaperRowHover'
                          hoverStayTime={70}
                          onClick={() => goQuizPaperList(it)}
                        >
                          <View className='bullet'>•</View>
                          <View className='listText clickable'>{it}</View>
                        </View>
                      ))}
                    </View>
                  </View>
                ))}
              </View>
            ) : null}

            {activeRoot === 'NCRE' ? (
              <View className='groups'>
                {NCRE_EXAM_CATALOG.map((g) => (
                  <View key={g.level} className='group'>
                    <View
                      className='accordionTitle'
                      onClick={() => setOpenNcre((prev) => ({ ...prev, [g.level]: !prev[g.level] }))}
                    >
                      <Text className='accordionText'>{g.level}</Text>
                      <Text className='accordionArrow'>{openNcre[g.level] ? '▾' : '▸'}</Text>
                    </View>
                    {openNcre[g.level] ? (
                      <View className='list'>
                        {g.items.map((it) => (
                          <View
                            key={it}
                            className='softPaperRow'
                            hoverClass='softPaperRowHover'
                            hoverStayTime={70}
                            onClick={() => goNcrePaperList(it)}
                          >
                            <View className='bullet'>•</View>
                            <View className='listText clickable'>{it}</View>
                          </View>
                        ))}
                      </View>
                    ) : null}
                  </View>
                ))}
              </View>
            ) : null}
          </View>
        </View>
      </View>
    </View>
  );
};

export default QuizPage;

