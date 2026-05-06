import React, { useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import {
  SOFT_EXAM_CATALOG,
  softExamPaperIndexOfSubject
} from '../../constants/softExamPaperNav';
import './index.scss';

/** 与 quiz-paper-list 约定：无 paperIdx 时的回退 */
const QUIZ_PAPER_LIST_EXAM_KEY = 'quizPaperListExamCategory';
const QUIZ_PAPER_LIST_SUBJECT_KEY = 'quizPaperListSubject';

const ncreData = [
  {
    level: '一级',
    items: ['计算机基础及WPS Office应用', '计算机基础及MS Office应用']
  },
  {
    level: '二级',
    items: ['C语言程序设计', 'Java语言程序设计', 'Python语言程序设计', 'Web程序设计', 'MS Office高级应用']
  },
  {
    level: '三级',
    items: ['网络技术', '数据库技术', '信息安全技术', '嵌入式系统开发技术']
  },
  {
    level: '四级',
    items: ['网络工程师', '数据库工程师', '信息安全工程师', '嵌入式系统开发工程师']
  }
];

const QuizPage = () => {
  const [token, setToken] = useState('');
  const [activeRoot, setActiveRoot] = useState('SOFT_EXAM');
  const [openNcre, setOpenNcre] = useState({ 一级: true, 二级: false, 三级: false, 四级: false });

  useDidShow(() => {
    setToken(Taro.getStorageSync('token') || '');
  });

  const onGoLogin = () => {
    Taro.navigateTo({ url: '/pages/login/index' });
  };

  const syncPaperListStorage = (subject) => {
    try {
      Taro.setStorageSync(QUIZ_PAPER_LIST_EXAM_KEY, '软考');
      Taro.setStorageSync(QUIZ_PAPER_LIST_SUBJECT_KEY, subject);
    } catch (_e) {
      /* 试卷列表页仍可从 URL 参数解析，缓存失败不阻断导航 */
    }
  };

  /** tab 页上不用 navigator（易与 React 事件冲突）；整行 View + 同步 navigateTo，URL 仅用 paperIdx */
  const goQuizPaperList = (subject) => {
    const paperIdx = softExamPaperIndexOfSubject(subject);
    if (paperIdx < 0) {
      void Taro.showToast({ title: '未知科目', icon: 'none' });
      return;
    }
    syncPaperListStorage(subject);
    const url = `/pages/quiz-paper-list/index?paperIdx=${paperIdx}`;
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
                {ncreData.map((g) => (
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
                          <View key={it} className='listItem'>
                            <Text className='bullet'>•</Text>
                            <Text className='listText'>{it}</Text>
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

