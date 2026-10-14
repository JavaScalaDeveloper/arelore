/**
 * 与 mini-cert / user 服务环境约定对齐：
 *   dev/pre → http://localhost:8081/api
 *   test    → http://www.arelore.com:8281/api
 *   prd     → http://www.arelore.com:8081/api
 *
 * 解析优先级（高 → 低）：
 *   1. Storage miniApiBaseUrl（完整地址）
 *   2. Storage miniApiEnv（prd|pre|test|dev）
 *   3. utils/env.local.js（由 ./start.sh [env] 生成）
 *   4. 默认 test
 */
const API_BY_ENV = {
  prd: 'http://www.arelore.com:8081/api',
  pre: 'http://localhost:8081/api',
  test: 'http://www.arelore.com:8281/api',
  dev: 'http://localhost:8081/api'
};

const DEFAULT_ENV = 'test';

function loadFileEnv() {
  try {
    // start.sh 生成；不存在时 require 会抛错
    // eslint-disable-next-line import/no-unresolved, global-require
    return require('./env.local.js') || {};
  } catch (e) {
    return {};
  }
}

function normalizeBase(url) {
  return String(url || '').replace(/\/$/, '');
}

function getActiveEnv() {
  const storageEnv = String(wx.getStorageSync('miniApiEnv') || '').toLowerCase();
  if (storageEnv && API_BY_ENV[storageEnv]) {
    return storageEnv;
  }
  const file = loadFileEnv();
  const fileEnv = String(file.env || '').toLowerCase();
  if (fileEnv && API_BY_ENV[fileEnv]) {
    return fileEnv;
  }
  return DEFAULT_ENV;
}

function getApiBaseUrl() {
  const override = wx.getStorageSync('miniApiBaseUrl');
  if (override) {
    return normalizeBase(override);
  }
  const storageEnv = String(wx.getStorageSync('miniApiEnv') || '').toLowerCase();
  if (storageEnv && API_BY_ENV[storageEnv]) {
    return API_BY_ENV[storageEnv];
  }
  const file = loadFileEnv();
  if (file.apiBaseUrl) {
    return normalizeBase(file.apiBaseUrl);
  }
  const fileEnv = String(file.env || '').toLowerCase();
  if (fileEnv && API_BY_ENV[fileEnv]) {
    return API_BY_ENV[fileEnv];
  }
  return API_BY_ENV[DEFAULT_ENV];
}

module.exports = {
  getApiBaseUrl,
  getActiveEnv,
  API_BY_ENV,
  DEFAULT_ENV
};
