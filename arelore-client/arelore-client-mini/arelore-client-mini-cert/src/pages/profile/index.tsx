import React, { useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import { postWithAuth } from '../../utils/request';
import { emitTabBarIndex } from '../../utils/tabBarSync';
import './index.scss';

const ProfilePage = () => {
  const [token, setToken] = useState('');
  const [errMsg, setErrMsg] = useState('');

  useDidShow(() => {
    emitTabBarIndex(2);
    const t = Taro.getStorageSync('token') || '';
    setToken(t);
    setErrMsg('');

    if (!t) return;
    (async () => {
      try {
        const resp = await postWithAuth('/user/auth/current', {});
        if (resp && resp.code === 200 && resp.data) {
          Taro.setStorageSync('currentUser', resp.data);
          return;
        }
        if (resp && resp.code === 401) {
          Taro.removeStorageSync('token');
          Taro.removeStorageSync('currentUser');
          setToken('');
          setErrMsg(resp.message || '未登录或登录已过期');
          return;
        }
        setErrMsg(resp?.message || '登录态校验失败');
      } catch (e) {
        const msg = e && e.errMsg ? e.errMsg : '登录态校验失败';
        setErrMsg(msg);
      }
    })();
  });

  const onGoLogin = () => {
    Taro.navigateTo({ url: '/pages/login/index' });
  };

  const onGoExamHistory = () => {
    Taro.navigateTo({ url: '/pages/exam-history/index' });
  };

  const onGoFavorites = () => {
    Taro.navigateTo({ url: '/pages/favorites/index' });
  };

  return (
    <View className='page'>
      <View className='card'>
        <View className='title'>个人</View>
        {!token ? (
          <>
            <View className='row'>
              <Text>你还未登录。</Text>
            </View>
            {errMsg ? (
              <View className='row'>
                <Text>{errMsg}</Text>
              </View>
            ) : null}
            <Button className='btn' type='primary' onClick={onGoLogin}>
              去登录
            </Button>
          </>
        ) : (
          <>
            <View className='iconGrid'>
              <View className='iconItem' onClick={onGoExamHistory}>
                <View className='iconCircle iconCircleExam'>
                  <Text className='iconEmoji'>📝</Text>
                </View>
                <Text className='iconLabel'>考试记录</Text>
              </View>
              <View className='iconItem' onClick={onGoFavorites}>
                <View className='iconCircle iconCircleFav'>
                  <Text className='iconEmoji'>⭐</Text>
                </View>
                <Text className='iconLabel'>收藏夹</Text>
              </View>
            </View>

            {errMsg ? (
              <View className='row errRow'>
                <Text>{errMsg}</Text>
              </View>
            ) : null}
          </>
        )}
      </View>
    </View>
  );
};

export default ProfilePage;
