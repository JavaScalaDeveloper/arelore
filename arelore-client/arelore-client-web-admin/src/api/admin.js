import request from '../utils/request';

/**
 * 管理员相关 API
 */
export const adminApi = {
  /**
   * 管理员登录
   */
  login: (data) => {
    return request.post('/admin/login', data);
  },

  /**
   * 获取仪表盘数据
   */
  getDashboard: () => {
    return request.post('/admin/dashboard');
  },

  /**
   * 获取用户列表
   */
  getUserList: (data) => {
    return request.post('/admin/users', data);
  },

  /**
   * 获取系统设置
   */
  getSettings: () => {
    return request.post('/admin/settings/get');
  },

  /**
   * 更新系统设置
   */
  updateSettings: (data) => {
    return request.post('/admin/settings/update', data);
  },
};
