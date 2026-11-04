import React, { useMemo } from 'react';
import { Empty, Tag } from 'antd';
import { useSearchParams } from 'react-router-dom';
import { useI18n, useLocaleNavigate } from '../locale';
import ProductSearch from '../components/ProductSearch';
import {
  searchSiteCatalog,
  SITE_CATALOG,
  SiteCatalogKind,
} from '../data/siteCatalog';

const KIND_ORDER: SiteCatalogKind[] = ['product', 'tool', 'solution'];

export default function ProductsPage() {
  const navigate = useLocaleNavigate();
  const { t, locale } = useI18n();
  const [params] = useSearchParams();
  const q = params.get('q') || '';

  const results = useMemo(() => searchSiteCatalog(q, 100, locale), [q, locale]);
  const grouped = useMemo(() => {
    const map: Record<SiteCatalogKind, typeof results> = {
      product: [],
      tool: [],
      solution: [],
    };
    results.forEach((item) => map[item.kind].push(item));
    return map;
  }, [results]);

  return (
    <div className="products-page">
      <div className="products-page-head">
        <h1>{t('products.title')}</h1>
        <p>
          {q
            ? t('products.resultCount', { q, count: results.length })
            : t('products.totalCount', { count: SITE_CATALOG.length })}
        </p>
        <div className="products-page-search">
          <ProductSearch key={`${locale}-${q}`} size="large" initialQuery={q} />
        </div>
      </div>

      {results.length === 0 ? (
        <Empty description={t('products.empty', { q })} />
      ) : (
        KIND_ORDER.map((kind) => {
          const list = grouped[kind];
          if (!list.length) return null;
          return (
            <section key={kind} className="products-group">
              <h2>{t(`kind.${kind}`)}</h2>
              <div className="products-grid">
                {list.map((item) => (
                  <button
                    key={item.key}
                    type="button"
                    className="product-tile"
                    onClick={() => navigate(item.path)}
                  >
                    <div className="product-tile-top">
                      <strong>{item.title}</strong>
                      <Tag>{item.category}</Tag>
                    </div>
                    <p>{item.blurb}</p>
                  </button>
                ))}
              </div>
            </section>
          );
        })
      )}
    </div>
  );
}
