import React, { useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import { emitTabBarIndex } from '../../utils/tabBarSync';
import './index.scss';

const HomePage = () => {
  const [token, setToken] = useState('');

  useDidShow(() => {
    setToken(Taro.getStorageSync('token') || '');
    emitTabBarIndex(0);
  });

  const onGoLogin = () => {
    Taro.navigateTo({ url: '/pages/login/index' });
  };

  const goQuiz = () => {
    Taro.switchTab({ url: '/pages/quiz/index' });
  };

  const goProfile = () => {
    Taro.switchTab({ url: '/pages/profile/index' });
  };

  const goExamHistory = () => {
    Taro.navigateTo({ url: '/pages/exam-history/index' });
  };

  const goFavorites = () => {
    Taro.navigateTo({ url: '/pages/favorites/index' });
  };

  return (
    <View className='page'>
      <View className='hero'>
        <Text className='heroBrand'>考证宝</Text>
        <Text className='heroTagline'>软考真题练习 · 答题进度云端同步</Text>
      </View>

      <View className='ctaCard' onClick={goQuiz}>
        <View className='ctaInner'>
          <Text className='ctaTitle'>开始刷题</Text>
          <Text className='ctaDesc'>选择科目与试卷，随时继续上次进度</Text>
        </View>
        <Text className='ctaArrow'>›</Text>
      </View>

      <View className='section'>
        <Text className='sectionTitle'>常用入口</Text>
        <View className='quickGrid'>
          <View className='quickItem' onClick={goExamHistory}>
            <Text className='quickIcon'>📝</Text>
            <Text className='quickLabel'>考试记录</Text>
          </View>
          <View className='quickItem' onClick={goFavorites}>
            <Text className='quickIcon'>⭐</Text>
            <Text className='quickLabel'>收藏夹</Text>
          </View>
          <View className='quickItem' onClick={goProfile}>
            <Text className='quickIcon'>👤</Text>
            <Text className='quickLabel'>个人中心</Text>
          </View>
        </View>
      </View>

      {!token ? (
        <View className='accountCard'>
          <Text className='accountLine'>登录后可同步做题进度、查看考试记录与收藏</Text>
          <Button className='loginBtn' type='primary' onClick={onGoLogin}>
            微信登录
          </Button>
        </View>
      ) : null}
    </View>
  );
};

export default HomePage;
