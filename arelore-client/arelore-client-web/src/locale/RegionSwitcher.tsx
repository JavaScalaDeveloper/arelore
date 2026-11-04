import React from 'react';
import { Select, Space } from 'antd';
import { GlobalOutlined } from '@ant-design/icons';
import { AppLocale, LOCALE_LABELS, SUPPORTED_LOCALES, localeFlagSrc } from './constants';
import { useLocaleOptional } from './LocaleContext';
import { useI18n } from './useI18n';

type Props = {
  size?: 'small' | 'middle' | 'large';
  style?: React.CSSProperties;
};

function LocaleOptionLabel({ code }: { code: AppLocale }) {
  return (
    <Space size={8} className="locale-option">
      <img
        className="locale-flag-img"
        src={localeFlagSrc(code)}
        alt=""
        width={20}
        height={15}
        loading="lazy"
        decoding="async"
      />
      <span>{LOCALE_LABELS[code]}</span>
    </Space>
  );
}

/** 切换学习者母语（释义用语）；默认简体中文 */
export default function RegionSwitcher({ size = 'middle', style }: Props) {
  const { locale, setLocale } = useLocaleOptional();
  const { t } = useI18n();

  return (
    <Select
      size={size}
      value={locale}
      onChange={(v: AppLocale) => setLocale(v)}
      style={{ minWidth: 168, ...style }}
      suffixIcon={<GlobalOutlined />}
      showSearch
      optionFilterProp="searchLabel"
      labelRender={({ value }) =>
        value && (SUPPORTED_LOCALES as readonly string[]).includes(String(value)) ? (
          <LocaleOptionLabel code={value as AppLocale} />
        ) : null
      }
      options={SUPPORTED_LOCALES.map((code) => ({
        value: code,
        searchLabel: `${LOCALE_LABELS[code]} ${code}`,
        label: <LocaleOptionLabel code={code} />,
      }))}
      aria-label={t('meta.languageSwitcher')}
      popupClassName="locale-switcher-dropdown"
    />
  );
}
