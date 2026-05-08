import React, { useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import './index.scss';

const HomePage = () => {
  const [token, setToken] = useState('');
  const [currentUser, setCurrentUser] = useState({});

  useDidShow(() => {
    const t = Taro.getStorageSync('token') || '';
    const u = Taro.getStorageSync('currentUser') || {};
    setToken(t);
    setCurrentUser(u);
  });

  const currentUserName = useMemo(() => {
    if (!currentUser || typeof currentUser !== 'object') return '微信用户';
    const nick = Reflect.get(currentUser, 'nickname');
    const uname = Reflect.get(currentUser, 'username');
    return (typeof nick === 'string' && nick) || (typeof uname === 'string' && uname) || '微信用户';
  }, [currentUser]);

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

  const onLogout = async () => {
    const res = await Taro.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？'
    });
    if (!res.confirm) return;
    Taro.removeStorageSync('token');
    Taro.removeStorageSync('currentUser');
    setToken('');
    setCurrentUser({});
    Taro.showToast({ title: '已退出登录', icon: 'success' });
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

      <View className='accountCard'>
        {!token ? (
          <>
            <Text className='accountLine'>登录后可同步做题进度、查看考试记录与收藏</Text>
            <Button className='loginBtn' type='primary' onClick={onGoLogin}>
              微信登录
            </Button>
          </>
        ) : (
          <>
            <View className='accountRow'>
              <Text className='accountLabel'>当前账号</Text>
              <Text className='accountName'>{currentUserName}</Text>
            </View>
            <View className='accountActions'>
              <Text className='linkMuted' onClick={goProfile}>
                账号与安全 ›
              </Text>
              <Text className='linkLogout' onClick={onLogout}>
                退出登录
              </Text>
            </View>
          </>
        )}
      </View>
    </View>
  );
};

export default HomePage;
