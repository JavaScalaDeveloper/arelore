import React from 'react';
import { Button } from 'antd';
import { useLocaleNavigate } from '../locale';
import ProductSearch from '../components/ProductSearch';
import { SITE_CATALOG, kindLabel } from '../data/siteCatalog';

const highlights = SITE_CATALOG.filter((x) =>
  ['word', 'aiPrompt', 'textMasking', 'databaseClassification'].includes(x.key)
);

export default function SiteHomePage() {
  const navigate = useLocaleNavigate();

  return (
    <div className="site-home">
      <section className="site-hero" aria-label="Arelore 首页">
        <div className="site-hero-glow" aria-hidden />
        <div className="site-hero-grid" aria-hidden />
        <div className="site-hero-inner">
          <p className="site-brand">Arelore</p>
          <h1 className="site-hero-title">用产品把复杂事变简单</h1>
          <p className="site-hero-lead">
            外语学习、创作辅助与数据安全工具，一处入口即可搜索直达。
          </p>
          <div className="site-hero-search">
            <ProductSearch size="large" />
          </div>
          <div className="site-hero-cta">
            <Button
              type="primary"
              size="large"
              className="site-btn-primary"
              onClick={() => navigate('/home/products')}
            >
              浏览全部产品
            </Button>
            <Button
              size="large"
              className="site-btn-ghost"
              onClick={() => navigate('/home/contact')}
            >
              联系我们
            </Button>
          </div>
        </div>
      </section>

      <section className="site-section" aria-label="精选产品">
        <div className="site-section-head">
          <h2>精选入口</h2>
          <p>从高频场景开始，搜索框也能直接跳转。</p>
        </div>
        <div className="site-highlight-row">
          {highlights.map((item) => (
            <button
              key={item.key}
              type="button"
              className="site-highlight"
              onClick={() => navigate(item.path)}
            >
              <span className="site-highlight-kind">{kindLabel(item.kind)}</span>
              <strong>{item.title}</strong>
              <span className="site-highlight-blurb">{item.blurb}</span>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}
