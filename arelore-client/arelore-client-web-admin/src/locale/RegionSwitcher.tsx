import React from 'react';
import { Select } from 'antd';
import { GlobalOutlined } from '@ant-design/icons';
import { AppLocale, LOCALE_LABELS, SUPPORTED_LOCALES } from './constants';
import { useLocaleOptional } from './LocaleContext';

type Props = {
  size?: 'small' | 'middle' | 'large';
  style?: React.CSSProperties;
};

export default function RegionSwitcher({ size = 'middle', style }: Props) {
  const { locale, setLocale } = useLocaleOptional();

  return (
    <Select
      size={size}
      value={locale}
      onChange={(v: AppLocale) => setLocale(v)}
      style={{ minWidth: 140, ...style }}
      suffixIcon={<GlobalOutlined />}
      options={SUPPORTED_LOCALES.map((code) => ({
        value: code,
        label: LOCALE_LABELS[code],
      }))}
      aria-label="Country / Region"
    />
  );
}
