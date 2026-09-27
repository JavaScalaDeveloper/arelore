/** 与考证宝本地默认一致；可在开发者工具 Storage 写入 miniApiBaseUrl 覆盖 */
const DEFAULT_API_BASE_URL = 'http://localhost:8081/api';

function getApiBaseUrl() {
  const override = wx.getStorageSync('miniApiBaseUrl');
  if (override) return override;
  return DEFAULT_API_BASE_URL;
}

module.exports = {
  getApiBaseUrl
};
