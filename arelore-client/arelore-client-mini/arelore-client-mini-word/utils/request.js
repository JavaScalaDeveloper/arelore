const { getApiBaseUrl } = require('./config');

function isNetworkFail(err) {
  if (!err) {
    return true;
  }
  const msg = String(err.errMsg || err.message || '');
  return /fail|timeout|TIMED_OUT|CONNECTION|ERR_|network|中断|拒绝|unreachable/i.test(msg)
    || !msg;
}

function networkError(requestUrl, err) {
  const short = '网络不通，请检查后端是否启动、手机/模拟器网络，以及接口域名是否正确';
  return {
    code: -1001,
    message: short,
    isNetworkError: true,
    errMsg: `${short}\n${requestUrl}`,
    requestUrl,
    raw: err || null
  };
}

function post(path, data) {
  const token = wx.getStorageSync('token') || '';
  const requestUrl = `${getApiBaseUrl()}${path}`;
  return new Promise((resolve, reject) => {
    wx.request({
      url: requestUrl,
      method: 'POST',
      data: data || {},
      timeout: 20000,
      header: {
        'content-type': 'application/json',
        Authorization: token
      },
      success(res) {
        // 无 HTTP 层成功但 body 异常时仍交给业务页处理
        if (res.statusCode >= 400) {
          reject(networkError(requestUrl, {
            errMsg: `HTTP ${res.statusCode}`,
            statusCode: res.statusCode
          }));
          return;
        }
        resolve(res.data || { code: -1, message: '请求失败', data: null });
      },
      fail(err) {
        reject(networkError(requestUrl, err));
      }
    });
  });
}

function postWithAuth(path, data) {
  const token = wx.getStorageSync('token') || '';
  if (!token) {
    return Promise.resolve({ code: 401, message: '未登录或登录已过期', data: null });
  }
  return post(path, data);
}

/** 给页面展示用的短文案（避免 toast/空白页被超长 errMsg 搞坏） */
function shortErrorMessage(err, fallback) {
  if (err && err.isNetworkError && err.message) {
    return err.message;
  }
  if (err && err.message) {
    return String(err.message);
  }
  if (err && err.errMsg) {
    const raw = String(err.errMsg);
    // 只要第一行，去掉 URL 堆叠
    return raw.split('\n')[0] || fallback || '请求失败';
  }
  return fallback || '请求失败';
}

module.exports = {
  post,
  postWithAuth,
  isNetworkFail,
  networkError,
  shortErrorMessage
};
