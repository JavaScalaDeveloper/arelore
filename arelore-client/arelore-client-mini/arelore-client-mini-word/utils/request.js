const { getApiBaseUrl } = require('./config');

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
        resolve(res.data || { code: -1, message: '请求失败', data: null });
      },
      fail(err) {
        reject({
          errMsg: `接口连接失败：${requestUrl}（请检查后端是否启动、端口是否可达）`,
          raw: err
        });
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

module.exports = {
  post,
  postWithAuth
};
