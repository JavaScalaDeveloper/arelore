import React, { useEffect, useState } from 'react';
import Taro from '@tarojs/taro';
import { Text, View } from '@tarojs/components';
import './index.scss';

const tabs = [
  { pagePath: '/pages/home/index', text: '首页', icon: '⌂' },
  { pagePath: '/pages/quiz/index', text: '刷题', icon: '📚' },
  { pagePath: '/pages/profile/index', text: '个人', icon: '☺' }
];

const CustomTabBar = () => {
  const [selected, setSelected] = useState(0);

  useEffect(() => {
    const pages = Taro.getCurrentPages();
    const cur = pages[pages.length - 1];
    const route = cur ? `/${cur.route}` : '';
    const idx = tabs.findIndex((t) => t.pagePath === route);
    setSelected(idx >= 0 ? idx : 0);
  });

  const onSwitch = (idx) => {
    const tab = tabs[idx];
    if (!tab) return;
    setSelected(idx);
    Taro.switchTab({ url: tab.pagePath });
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

