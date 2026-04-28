import React, { useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import { postWithAuth } from '../../utils/request';
import './index.scss';

const ProfilePage = () => {
  const [token, setToken] = useState('');
  const [currentUser, setCurrentUser] = useState({});
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState('');

  useDidShow(() => {
    const t = Taro.getStorageSync('token') || '';
    setToken(t);
    setCurrentUser(Taro.getStorageSync('currentUser') || {});
    setErrMsg('');

    if (!t) return;
    (async () => {
      try {
        setLoading(true);
        const resp = await postWithAuth('/user/auth/current', {});
        if (resp && resp.code === 200 && resp.data) {
          setCurrentUser(resp.data);
          Taro.setStorageSync('currentUser', resp.data);
          return;
        }
        if (resp && resp.code === 401) {
          Taro.removeStorageSync('token');
          Taro.removeStorageSync('currentUser');
          setToken('');
          setCurrentUser({});
          setErrMsg(resp.message || '未登录或登录已过期');
          return;
        }
        setErrMsg(resp?.message || '获取用户信息失败');
      } catch (e) {
        const msg = e && e.errMsg ? e.errMsg : '获取用户信息失败';
        setErrMsg(msg);
      } finally {
        setLoading(false);
      }
    })();
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
    Taro.reLaunch({ url: '/pages/login/index' });
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
            <View className='row'>
              <Text>昵称：{currentUserName}</Text>
            </View>
            <View className='row'>
              <Text>登录态：{loading ? '校验中…' : '已登录'}</Text>
            </View>
            {errMsg ? (
              <View className='row'>
                <Text>{errMsg}</Text>
              </View>
            ) : null}
            <Button className='btn' onClick={onLogout}>
              退出登录
            </Button>
          </>
        )}
      </View>
    </View>
  );
};

export default ProfilePage;

