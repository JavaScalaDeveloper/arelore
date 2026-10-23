import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { absoluteWordUrl, buildWordJsonLd, WORD_SEO } from './wordSeo';

const META_KEYS = ['description', 'keywords', 'robots'] as const;
const OG_KEYS = [
  'og:type',
  'og:title',
  'og:description',
  'og:url',
  'og:site_name',
  'og:locale',
] as const;
const TW_KEYS = ['twitter:card', 'twitter:title', 'twitter:description'] as const;
const JSON_LD_ID = 'word-seo-jsonld';
const CANONICAL_ID = 'word-seo-canonical';

function upsertMeta(attr: 'name' | 'property', key: string, content: string) {
  let el = document.head.querySelector(`meta[${attr}="${key}"]`) as HTMLMetaElement | null;
  if (!el) {
    el = document.createElement('meta');
    el.setAttribute(attr, key);
    document.head.appendChild(el);
  }
  el.setAttribute('content', content);
}

function upsertLink(rel: string, id: string, href: string) {
  let el = document.getElementById(id) as HTMLLinkElement | null;
  if (!el) {
    el = document.createElement('link');
    el.id = id;
    el.rel = rel;
    document.head.appendChild(el);
  }
  el.href = href;
}

function upsertJsonLd(data: object) {
  let el = document.getElementById(JSON_LD_ID) as HTMLScriptElement | null;
  if (!el) {
    el = document.createElement('script');
    el.id = JSON_LD_ID;
    el.type = 'application/ld+json';
    document.head.appendChild(el);
  }
  el.textContent = JSON.stringify(data);
}

/**
 * 进入 /word 时写入 SEO head；离开时尽量恢复站点默认（不依赖 react-helmet）。
 */
export default function WordDocumentSeo() {
  const location = useLocation();

  useEffect(() => {
    const prevTitle = document.title;
    const origin = window.location.origin;
    const pageUrl = absoluteWordUrl(origin) + (location.search || '');

    document.title = WORD_SEO.title;
    document.documentElement.lang = 'zh-CN';

    upsertMeta('name', 'description', WORD_SEO.description);
    upsertMeta('name', 'keywords', WORD_SEO.keywords);
    upsertMeta('name', 'robots', 'index,follow,max-image-preview:large,max-snippet:-1');

    upsertMeta('property', 'og:type', 'website');
    upsertMeta('property', 'og:title', WORD_SEO.title);
    upsertMeta('property', 'og:description', WORD_SEO.description);
    upsertMeta('property', 'og:url', pageUrl);
    upsertMeta('property', 'og:site_name', WORD_SEO.shortTitle);
    upsertMeta('property', 'og:locale', 'zh_CN');

    upsertMeta('name', 'twitter:card', 'summary');
    upsertMeta('name', 'twitter:title', WORD_SEO.title);
    upsertMeta('name', 'twitter:description', WORD_SEO.description);

    upsertLink('canonical', CANONICAL_ID, absoluteWordUrl(origin));
    upsertJsonLd(buildWordJsonLd(origin));

    return () => {
      document.title = prevTitle || 'Arelore - 官方网站';
      const jsonLd = document.getElementById(JSON_LD_ID);
      if (jsonLd) {
        jsonLd.remove();
      }
      const canonical = document.getElementById(CANONICAL_ID);
      if (canonical) {
        canonical.remove();
      }
      // description / keywords 交回站点默认，避免污染官网其他页
      upsertMeta('name', 'description', 'Arelore - 创新技术，引领未来');
      const kw = document.head.querySelector('meta[name="keywords"]');
      if (kw) {
        kw.remove();
      }
      [...META_KEYS.filter((k) => k === 'robots'), ...OG_KEYS, ...TW_KEYS].forEach((key) => {
        const isOg = String(key).startsWith('og:');
        const el = document.head.querySelector(
          isOg ? `meta[property="${key}"]` : `meta[name="${key}"]`
        );
        if (el && (isOg || key.startsWith('twitter:') || key === 'robots')) {
          el.remove();
        }
      });
    };
  }, [location.pathname, location.search]);

  return null;
}
