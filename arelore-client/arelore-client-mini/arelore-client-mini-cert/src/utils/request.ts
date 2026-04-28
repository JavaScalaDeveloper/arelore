import Taro from '@tarojs/taro';

const DEFAULT_API_BASE_URL = 'http://localhost:8081/api';

function resolveApiBaseUrl() {
  const runtimeOverride = Taro.getStorageSync('miniApiBaseUrl');
  if (runtimeOverride) {
    return runtimeOverride;
  }
  if (typeof process !== 'undefined' && process.env && process.env.TARO_APP_API_BASE_URL) {
    return process.env.TARO_APP_API_BASE_URL;
  }
  return DEFAULT_API_BASE_URL;
}

export async function post(path, data) {
  const token = Taro.getStorageSync('token') || '';
  const apiBaseUrl = resolveApiBaseUrl();
  const requestUrl = `${apiBaseUrl}${path}`;
  try {
    const startAt = Date.now();
    // eslint-disable-next-line no-console
    console.log('[MiniRequest] POST', requestUrl, {
      hasToken: !!token,
      tokenPreview: token ? String(token).slice(0, 16) : ''
    });
    const res = await Taro.request({
      url: requestUrl,
      method: 'POST',
      data: data || {},
      header: {
        'content-type': 'application/json',
        Authorization: token
      }
    });
    // eslint-disable-next-line no-console
    console.log('[MiniRequest] RESP', requestUrl, {
      statusCode: res.statusCode,
      costMs: Date.now() - startAt,
      dataKeys: res.data ? Object.keys(res.data) : []
    });
    return res.data || { code: -1, message: '请求失败', data: null };
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error('[MiniRequest] request failed:', { requestUrl, error: e });
    throw {
      errMsg: `接口连接失败：${requestUrl}（请检查后端是否启动、端口是否可达）`
    };
  }
}

export async function postWithAuth(path, data) {
  const token = Taro.getStorageSync('token') || '';
  if (!token) {
    return { code: 401, message: '未登录或登录已过期', data: null };
  }
  return await post(path, data);
}

