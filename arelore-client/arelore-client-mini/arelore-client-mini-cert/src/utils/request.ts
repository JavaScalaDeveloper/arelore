import Taro from '@tarojs/taro';

const DEFAULT_API_BASE_URL = 'http://localhost:8081/api';

function resolveApiBaseUrl() {
  // 本地调试若曾写入过该 key，会一直优先于构建注入的地址（换环境后记得清 Storage）
  const runtimeOverride = Taro.getStorageSync('miniApiBaseUrl');
  if (runtimeOverride) {
    return runtimeOverride;
  }
  // 小程序端没有 Node 的 process；此处在构建时由 webpack DefinePlugin 替换为字符串字面量，勿再包一层 typeof process 判断，否则会永远走默认 8081
  const buildTimeBase = process.env.TARO_APP_API_BASE_URL;
  if (buildTimeBase) {
    return buildTimeBase;
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
      timeout: 20000,
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
    if (res.statusCode >= 400) {
      throw {
        code: -1001,
        isNetworkError: true,
        message: '网络不通，请检查后端是否启动、手机/模拟器网络，以及接口域名是否正确',
        errMsg: `网络不通 HTTP ${res.statusCode}\n${requestUrl}`,
        requestUrl,
        statusCode: res.statusCode,
        raw: res.data || null
      };
    }
    return res.data || { code: -1, message: '请求失败', data: null };
  } catch (e) {
    if (e && e.isNetworkError) {
      throw e;
    }
    // eslint-disable-next-line no-console
    console.error('[MiniRequest] request failed:', { requestUrl, error: e });
    throw {
      code: -1001,
      isNetworkError: true,
      message: '网络不通，请检查后端是否启动、手机/模拟器网络，以及接口域名是否正确',
      errMsg: `网络不通\n${requestUrl}`,
      requestUrl,
      raw: e || null
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

/** 页面展示用短文案 */
export function shortErrorMessage(err, fallback = '请求失败') {
  if (err && err.isNetworkError && err.message) {
    return err.message;
  }
  if (err && err.message) {
    return String(err.message);
  }
  if (err && err.errMsg) {
    return String(err.errMsg).split('\n')[0] || fallback;
  }
  return fallback;
}

