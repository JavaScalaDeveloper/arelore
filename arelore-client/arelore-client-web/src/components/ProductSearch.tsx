import React, { useMemo, useState } from 'react';
import { AutoComplete, Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useI18n, useLocaleNavigate } from '../locale';
import {
  LocalizedSiteCatalogItem,
  searchSiteCatalog,
  SITE_CATALOG,
} from '../data/siteCatalog';

type Props = {
  /** hero 大搜索 / header 紧凑 */
  size?: 'large' | 'middle';
  className?: string;
  placeholder?: string;
  /** 回车无精确项时跳转产品列表并带 q */
  allowListFallback?: boolean;
  /** 列表页带入 URL ?q= */
  initialQuery?: string;
};

function optionLabel(item: LocalizedSiteCatalogItem, kindText: string) {
  return (
    <div className="product-search-option">
      <div className="product-search-option-title">
        <span>{item.title}</span>
        <em>{kindText}</em>
      </div>
      <div className="product-search-option-meta">
        {item.category} · {item.blurb}
      </div>
    </div>
  );
}

export default function ProductSearch({
  size = 'large',
  className,
  placeholder,
  allowListFallback = true,
  initialQuery = '',
}: Props) {
  const navigate = useLocaleNavigate();
  const { t, locale } = useI18n();
  const [value, setValue] = useState(initialQuery);
  const [open, setOpen] = useState(false);
  const ph = placeholder || t('common.searchProducts');

  const options = useMemo(() => {
    const hits = searchSiteCatalog(value, 12, locale);
    return hits.map((item) => ({
      value: item.key,
      label: optionLabel(item, t(`kind.${item.kind}`)),
      item,
    }));
  }, [value, locale, t]);

  const goItem = (item: LocalizedSiteCatalogItem) => {
    setOpen(false);
    setValue('');
    navigate(item.path);
  };

  const onSelect = (key: string) => {
    const hit = searchSiteCatalog('', 100, locale).find((x) => x.key === key)
      || searchSiteCatalog(key, 1, locale)[0];
    const raw = SITE_CATALOG.find((x) => x.key === key);
    if (hit) {
      goItem(hit);
    } else if (raw) {
      navigate(raw.path);
    }
  };

  const onSearchSubmit = () => {
    const q = value.trim();
    if (!q) {
      if (allowListFallback) navigate('/home/products');
      return;
    }
    const exact = searchSiteCatalog(q, 1, locale);
    if (exact.length === 1 && exact[0].title.toLowerCase() === q.toLowerCase()) {
      goItem(exact[0]);
      return;
    }
    if (exact.length === 1) {
      goItem(exact[0]);
      return;
    }
    if (allowListFallback) {
      navigate(`/home/products?q=${encodeURIComponent(q)}`);
    }
  };

  return (
    <AutoComplete
      className={className}
      style={{ width: '100%' }}
      options={options}
      open={open}
      onDropdownVisibleChange={setOpen}
      value={value}
      onChange={setValue}
      onSelect={onSelect}
      onFocus={() => setOpen(true)}
    >
      <Input
        size={size}
        allowClear
        placeholder={ph}
        prefix={<SearchOutlined />}
        onPressEnter={onSearchSubmit}
      />
    </AutoComplete>
  );
}
