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
    return currentUser?.nickname || currentUser?.username || '微信用户';
  }, [currentUser]);

  const onGoLogin = () => {
    Taro.navigateTo({ url: '/pages/login/index' });
  };

  const onLogout = () => {
    Taro.removeStorageSync('token');
    Taro.removeStorageSync('currentUser');
    setToken('');
    setCurrentUser({});
    Taro.showToast({ title: '已退出登录', icon: 'success' });
  };

  return (
    <View className='page'>
      <View className='card'>
        <View className='title'>考证宝</View>
        {!token ? (
          <>
            <View className='user'>
              <Text>当前状态：未登录</Text>
            </View>
            <Button className='btn' type='primary' onClick={onGoLogin}>
              去登录
            </Button>
          </>
        ) : (
          <>
            <View className='user'>
              <Text>当前用户：{currentUserName}</Text>
            </View>
            <Button className='btn' onClick={onLogout}>
              退出登录
            </Button>
          </>
        )}
      </View>
    </View>
  );
};

export default HomePage;

