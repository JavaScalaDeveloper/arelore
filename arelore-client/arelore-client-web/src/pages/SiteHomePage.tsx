import React from 'react';
import { Button } from 'antd';
import { ArrowRightOutlined } from '@ant-design/icons';
import { useI18n, useLocaleNavigate } from '../locale';
import ProductSearch from '../components/ProductSearch';
import { localizedSiteCatalog } from '../data/siteCatalog';

const HIGHLIGHT_KEYS = ['word', 'aiPrompt', 'textMasking', 'databaseClassification'];

export default function SiteHomePage() {
  const navigate = useLocaleNavigate();
  const { t, locale } = useI18n();
  const highlights = localizedSiteCatalog(locale).filter((x) => HIGHLIGHT_KEYS.includes(x.key));

  return (
    <div className="site-home">
      <section className="site-hero" aria-label={t('home.heroAria')}>
        <div className="site-hero-bg" aria-hidden>
          <div className="site-hero-mesh" />
          <div className="site-hero-beam" />
          <div className="site-hero-panel">
            <div className="site-hero-panel-glow" />
            <div className="site-hero-code">
              <span>arelore.cloud</span>
              <span>products.ship()</span>
              <span className="site-hero-code-accent">status: online</span>
            </div>
          </div>
        </div>

        <div className="site-hero-inner">
          <p className="site-brand">Arelore</p>
          <h1 className="site-hero-title">{t('home.heroTitle')}</h1>
          <p className="site-hero-lead">{t('home.heroLead')}</p>
          <div className="site-hero-cta">
            <Button
              type="primary"
              size="large"
              className="site-btn-primary"
              onClick={() => navigate('/home/products')}
            >
              {t('common.browseAll')}
              <ArrowRightOutlined />
            </Button>
            <Button
              size="large"
              className="site-btn-ghost"
              onClick={() => navigate('/home/contact')}
            >
              {t('common.contactUs')}
            </Button>
          </div>
          <div className="site-hero-search">
            <ProductSearch size="large" />
          </div>
        </div>
      </section>

      <section className="site-section" aria-label={t('home.sectionAria')}>
        <div className="site-section-inner">
          <div className="site-section-head">
            <p className="site-section-kicker">{t('home.sectionKicker')}</p>
            <h2>{t('home.sectionTitle')}</h2>
            <p>{t('home.sectionLead')}</p>
          </div>
          <div className="site-highlight-row">
            {highlights.map((item, index) => (
              <button
                key={item.key}
                type="button"
                className="site-highlight"
                onClick={() => navigate(item.path)}
              >
                <span className="site-highlight-index">
                  {String(index + 1).padStart(2, '0')}
                </span>
                <span className="site-highlight-body">
                  <span className="site-highlight-kind">{t(`kind.${item.kind}`)}</span>
                  <strong>{item.title}</strong>
                  <span className="site-highlight-blurb">{item.blurb}</span>
                </span>
                <ArrowRightOutlined className="site-highlight-arrow" />
              </button>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
