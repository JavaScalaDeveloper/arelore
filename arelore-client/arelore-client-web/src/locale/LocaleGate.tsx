import React from 'react';
import { ConfigProvider } from 'antd';
import arEG from 'antd/locale/ar_EG';
import deDE from 'antd/locale/de_DE';
import enUS from 'antd/locale/en_US';
import esES from 'antd/locale/es_ES';
import frFR from 'antd/locale/fr_FR';
import hiIN from 'antd/locale/hi_IN';
import idID from 'antd/locale/id_ID';
import itIT from 'antd/locale/it_IT';
import jaJP from 'antd/locale/ja_JP';
import koKR from 'antd/locale/ko_KR';
import msMY from 'antd/locale/ms_MY';
import nlNL from 'antd/locale/nl_NL';
import plPL from 'antd/locale/pl_PL';
import ptBR from 'antd/locale/pt_BR';
import ruRU from 'antd/locale/ru_RU';
import svSE from 'antd/locale/sv_SE';
import thTH from 'antd/locale/th_TH';
import trTR from 'antd/locale/tr_TR';
import ukUA from 'antd/locale/uk_UA';
import viVN from 'antd/locale/vi_VN';
import zhCN from 'antd/locale/zh_CN';
import type { Locale as AntdLocale } from 'antd/es/locale';
import { useLocation } from 'react-router-dom';
import { AppLocale } from './constants';
import { LocaleProvider } from './LocaleContext';
import { parseLocalePath } from './path';

const ANTD_BY_LOCALE: Partial<Record<AppLocale, AntdLocale>> = {
  'zh-cn': zhCN,
  'en-us': enUS,
  'ja-jp': jaJP,
  'ko-kr': koKR,
  'es-es': esES,
  'fr-fr': frFR,
  'de-de': deDE,
  'pt-br': ptBR,
  'ru-ru': ruRU,
  'vi-vn': viVN,
  'th-th': thTH,
  'id-id': idID,
  'ar-sa': arEG,
  'hi-in': hiIN,
  'it-it': itIT,
  'nl-nl': nlNL,
  'tr-tr': trTR,
  'pl-pl': plPL,
  'sv-se': svSE,
  'uk-ua': ukUA,
  'ms-my': msMY,
};

/**
 * 挂在 /ja-jp/*、/en-us/*、* 下：把母语前缀从 pathname 剥掉后再渲染子树。
 */
export default function LocaleGate({
  locale,
  children,
}: {
  locale: AppLocale;
  children: React.ReactNode;
}) {
  const location = useLocation();
  const { pathname } = parseLocalePath(location.pathname);
  const antdLocale = ANTD_BY_LOCALE[locale] || enUS;

  return (
    <LocaleProvider locale={locale}>
      <ConfigProvider locale={antdLocale}>
        <LocalePathLocation pathname={pathname}>{children}</LocalePathLocation>
      </ConfigProvider>
    </LocaleProvider>
  );
}

const PathCtx = React.createContext<string>('/');

export function useLocaleMatchedPathname(): string {
  return React.useContext(PathCtx);
}

function LocalePathLocation({
  pathname,
  children,
}: {
  pathname: string;
  children: React.ReactNode;
}) {
  return <PathCtx.Provider value={pathname}>{children}</PathCtx.Provider>;
}

/**
 * 供内部 <Routes location={...}> 使用。
 * RR v6 要求 location.pathname 必须以父路由匹配前缀开头（如 /ja-jp）。
 */
export function useStrippedLocation() {
  const location = useLocation();
  const businessPath = useLocaleMatchedPathname();
  const parsed = parseLocalePath(location.pathname);
  const pathname = parsed.hasExplicitPrefix
    ? `/${parsed.locale}${businessPath === '/' ? '' : businessPath}`
    : businessPath;
  return React.useMemo(
    () => ({ ...location, pathname }),
    [location, pathname]
  );
}
