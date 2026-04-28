import React, { useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import './index.scss';

const softExamData = [
  { level: '高级', items: ['信息系统项目管理师', '系统分析师', '系统架构设计师'] },
  { level: '中级', items: ['软件设计师', '网络工程师', '信息系统管理工程师', '系统集成项目管理工程师'] },
  { level: '初级', items: ['程序员', '网络管理员', '信息处理技术员'] }
];

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
                {softExamData.map((g) => (
                  <View key={g.level} className='group'>
                    <View className='groupTitle'>{g.level}</View>
                    <View className='list'>
                      {g.items.map((it) => (
                        <View key={it} className='listItem'>
                          <Text className='bullet'>•</Text>
                          <Text
                            className='listText clickable'
                            onClick={() => {
                              if (it.indexOf('软件设计师') >= 0) {
                                Taro.navigateTo({
                                  url: '/pages/quiz-paper-list/index?examCategory=软考&subject=软件设计师'
                                });
                              }
                            }}
                          >
                            {it}
                          </Text>
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

