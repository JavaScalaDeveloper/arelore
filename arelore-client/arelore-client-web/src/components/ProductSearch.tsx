import React, { useMemo, useState } from 'react';
import { AutoComplete, Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useLocaleNavigate } from '../locale';
import { kindLabel, searchSiteCatalog, SITE_CATALOG, SiteCatalogItem } from '../data/siteCatalog';

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

function optionLabel(item: SiteCatalogItem) {
  return (
    <div className="product-search-option">
      <div className="product-search-option-title">
        <span>{item.title}</span>
        <em>{kindLabel(item.kind)}</em>
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
  placeholder = '搜索产品、工具或方案…',
  allowListFallback = true,
  initialQuery = '',
}: Props) {
  const navigate = useLocaleNavigate();
  const [value, setValue] = useState(initialQuery);
  const [open, setOpen] = useState(false);

  const options = useMemo(() => {
    const hits = searchSiteCatalog(value, 12);
    return hits.map((item) => ({
      value: item.key,
      label: optionLabel(item),
      item,
    }));
  }, [value]);

  const goItem = (item: SiteCatalogItem) => {
    setOpen(false);
    setValue('');
    navigate(item.path);
  };

  const onSelect = (key: string) => {
    const hit = SITE_CATALOG.find((x) => x.key === key);
    if (hit) {
      goItem(hit);
    }
  };

  const onSearchSubmit = () => {
    const q = value.trim();
    if (!q) {
      if (allowListFallback) navigate('/home/products');
      return;
    }
    const exact = searchSiteCatalog(q, 1);
    if (exact.length === 1 && exact[0].title.toLowerCase() === q.toLowerCase()) {
      goItem(exact[0]);
      return;
    }
    if (exact.length === 1) {
      goItem(exact[0]);
      return;
    }
    if (allowListFallback) {
      setOpen(false);
      navigate(`/home/products?q=${encodeURIComponent(q)}`);
    }
  };

  return (
    <AutoComplete
      className={className}
      style={{ width: '100%' }}
      options={options}
      value={value}
      open={open}
      onFocus={() => setOpen(true)}
      onBlur={() => setTimeout(() => setOpen(false), 150)}
      onSearch={(v) => {
        setValue(v);
        setOpen(true);
      }}
      onSelect={onSelect}
      popupClassName="product-search-dropdown"
      notFoundContent={value.trim() ? '未找到相关产品' : null}
    >
      <Input
        size={size}
        allowClear
        placeholder={placeholder}
        prefix={<SearchOutlined />}
        onPressEnter={onSearchSubmit}
        aria-label="产品搜索"
      />
    </AutoComplete>
  );
}
