import Taro from '@tarojs/taro';

/**
 * 微信端 router.params 常为 {}，真实 query 多在当前页 options 里。
 * 使用 .js 避免 babel 对部分 .ts 未启用 TypeScript 解析。
 */
export function mergeWeappRouteParams() {
  const inst = Taro.getCurrentInstance();
  const fromRouter = inst?.router?.params || {};
  let fromPage = {};
  try {
    const pages = Taro.getCurrentPages();
    const cur = pages[pages.length - 1];
    fromPage = (cur && cur.options) || {};
  } catch (e) {
    /* ignore */
  }
  return { ...fromRouter, ...fromPage };
}
