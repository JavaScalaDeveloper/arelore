import React, { useCallback, useEffect, useState } from 'react';
import Taro from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import { subscribeTabBarIndex } from '../utils/tabBarSync';
import './index.scss';

const tabs = [
  { pagePath: '/pages/home/index', text: '首页', icon: '⌂' },
  { pagePath: '/pages/quiz/index', text: '刷题', icon: '📚' },
  { pagePath: '/pages/profile/index', text: '个人', icon: '☺' }
];

function indexFromRoute() {
  try {
    const pages = Taro.getCurrentPages();
    const cur = pages[pages.length - 1];
    const route = cur?.route ? `/${cur.route}` : '';
    const idx = tabs.findIndex((t) => t.pagePath === route);
    return idx >= 0 ? idx : 0;
  } catch (_e) {
    return 0;
  }
}

const CustomTabBar = () => {
  const [selected, setSelected] = useState(0);

  const applyIndex = useCallback((idx) => {
    setSelected((prev) => (prev === idx ? prev : idx));
  }, []);

  useEffect(() => {
    applyIndex(indexFromRoute());
    return subscribeTabBarIndex(applyIndex);
  }, [applyIndex]);

  const onSwitch = (idx) => {
    const tab = tabs[idx];
    if (!tab) return;
    if (selected === idx) {
      return;
    }
    Taro.switchTab({
      url: tab.pagePath,
      success: () => applyIndex(idx)
    });
  };

  return (
    <View className='tabbar'>
      {tabs.map((t, idx) => {
        const active = idx === selected;
        return (
          <View
            key={t.pagePath}
            className='tabbar-item'
            onClick={() => onSwitch(idx)}
          >
            <Text className={`tabbar-icon ${active ? 'active' : ''}`}>{t.icon}</Text>
            <Text className={`tabbar-text ${active ? 'active' : ''}`}>{t.text}</Text>
          </View>
        );
      })}
    </View>
  );
};

export default CustomTabBar;

