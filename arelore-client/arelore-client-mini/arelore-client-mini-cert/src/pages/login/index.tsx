import React, { useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Button, Text, View } from '@tarojs/components';
import { post } from '../../utils/request';
import './index.scss';

const LoginPage = () => {
  const [loading, setLoading] = useState(false);

  useDidShow(() => {
    const token = Taro.getStorageSync('token');
    if (token) {
      Taro.switchTab({ url: '/pages/home/index' });
    }
  });

  const getOrCreateMockOpenid = () => {
    const cached = Taro.getStorageSync('miniOpenid');
    if (cached) return cached;
    // 本地开发/未接入 code2session 时，用设备维度的稳定 openid，避免每次登录都创建新账号
    const rnd = Math.random().toString(36).slice(2, 10);
    const value = `mini_dev_${Date.now()}_${rnd}`;
    Taro.setStorageSync('miniOpenid', value);
    return value;
  };

  const mockOpenid = useMemo(() => getOrCreateMockOpenid(), []);

  const onWechatQuickLogin = async () => {
    try {
      // eslint-disable-next-line no-console
      console.log('[MiniLogin] tap login button');
      setLoading(true);
      // 必须在用户点击手势链路中尽早调用，否则会触发
      // "can only be invoked by user TAP gesture"
      const profileRes = await Taro.getUserProfile({
        desc: '用于完善用户资料'
      });
      const userInfo = profileRes.userInfo;
      const loginRes = await Taro.login();
      if (!loginRes.code) {
        Taro.showToast({ title: '微信登录失败，请重试', icon: 'none' });
        return;
      }

      const resp = await post('/user/auth/wechat/quick', {
        code: loginRes.code,
        userInfo: {
          openid: mockOpenid,
          nickname: userInfo?.nickName || '微信用户',
          avatar: userInfo?.avatarUrl || '',
          gender: userInfo?.gender || 0,
          country: userInfo?.country || '',
          province: userInfo?.province || '',
          city: userInfo?.city || ''
        }
      });

      if (resp.code === 200 && resp.data) {
        const nextToken = resp.data?.token || resp.data?.data?.token || '';
        const nextUser = resp.data?.user || resp.data?.data?.user || {};

        Taro.setStorageSync('token', nextToken);
        Taro.setStorageSync('currentUser', nextUser);

        const savedToken = Taro.getStorageSync('token') || '';
        // eslint-disable-next-line no-console
        console.log('[MiniLogin] token saved', {
          nextTokenPreview: nextToken ? String(nextToken).slice(0, 16) : '',
          savedTokenPreview: savedToken ? String(savedToken).slice(0, 16) : ''
        });
        if (!savedToken) {
          Taro.showModal({
            title: '登录态未写入',
            content:
              '后端已返回成功，但本地未读取到 token。请检查后端返回字段是否为 data.token，以及 DevTools 是否清理了数据。',
            showCancel: false
          });
          return;
        }
        Taro.showToast({ title: '登录成功', icon: 'success' });
        Taro.switchTab({ url: '/pages/home/index' });
        return;
      }
      Taro.showToast({ title: resp.message || '登录失败', icon: 'none' });
    } catch (e) {
      // 微信开发者工具里方便直接定位错误
      // eslint-disable-next-line no-console
      console.error('[MiniLogin] wechat quick login error:', e);
      const errMsg = e && e.errMsg ? e.errMsg : '登录失败，请稍后重试';
      Taro.showToast({ title: errMsg, icon: 'none' });
      Taro.showModal({
        title: '登录失败',
        content: errMsg,
        showCancel: false
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <View className='page'>
      <View className='card'>
        <View className='title'>考证宝</View>
        <View className='sub'>
          <Text>微信一键登录</Text>
        </View>
        <View className='hint'>
          <Text>本机 OpenID（本地模拟）：{mockOpenid}</Text>
        </View>
        <Button className='btn' type='primary' loading={loading} onClick={onWechatQuickLogin}>
          微信授权登录
        </Button>
      </View>
    </View>
  );
};

export default LoginPage;

