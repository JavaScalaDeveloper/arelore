import React, { useMemo } from 'react';
import { Empty, Tag } from 'antd';
import { useSearchParams } from 'react-router-dom';
import { useLocaleNavigate } from '../locale';
import ProductSearch from '../components/ProductSearch';
import {
  kindLabel,
  searchSiteCatalog,
  SITE_CATALOG,
  SiteCatalogKind,
} from '../data/siteCatalog';

const KIND_ORDER: SiteCatalogKind[] = ['product', 'tool', 'solution'];

export default function ProductsPage() {
  const navigate = useLocaleNavigate();
  const [params] = useSearchParams();
  const q = params.get('q') || '';

  const results = useMemo(() => searchSiteCatalog(q, 100), [q]);
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
        <h1>产品中心</h1>
        <p>
          {q
            ? `与「${q}」相关的结果（${results.length}）`
            : `共 ${SITE_CATALOG.length} 项产品、工具与方案`}
        </p>
        <div className="products-page-search">
          <ProductSearch key={q} size="large" initialQuery={q} />
        </div>
      </div>

      {results.length === 0 ? (
        <Empty description={`没有找到与「${q}」匹配的内容`} />
      ) : (
        KIND_ORDER.map((kind) => {
          const list = grouped[kind];
          if (!list.length) return null;
          return (
            <section key={kind} className="products-group">
              <h2>{kindLabel(kind)}</h2>
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
