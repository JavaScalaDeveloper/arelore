/**
 * 与 user 服务同机多环境端口约定一致：
 *   prd=8081  pre=8181  test=8281  dev=8381
 *
 * 覆盖方式（开发者工具 Storage）：
 *   - miniApiBaseUrl：完整根地址，如 http://127.0.0.1:8281/api
 *   - miniApiEnv：prd | pre | test | dev（默认 test，对应本地 spring.profiles.active）
 */
const API_BY_ENV = {
  prd: 'http://localhost:8081/api',
  pre: 'http://localhost:8181/api',
  test: 'http://localhost:8281/api',
  dev: 'http://localhost:8381/api'
};

const DEFAULT_ENV = 'test';

function getApiBaseUrl() {
  const override = wx.getStorageSync('miniApiBaseUrl');
  if (override) {
    return String(override).replace(/\/$/, '');
  }
  const env = String(wx.getStorageSync('miniApiEnv') || DEFAULT_ENV).toLowerCase();
  return API_BY_ENV[env] || API_BY_ENV[DEFAULT_ENV];
}

module.exports = {
  getApiBaseUrl,
  API_BY_ENV,
  DEFAULT_ENV
};
