import request from '../utils/request';

/**
 * 用户相关 API
 */
export const userApi = {
  /**
   * 获取用户列表
   */
  getList: (data) => {
    return request.post('/user/list', data);
  },

  /**
   * 获取用户详情
   */
  getDetail: (data) => {
    return request.post('/user/detail', data);
  },

  /**
   * 创建用户
   */
  create: (data) => {
    return request.post('/user/create', data);
  },

  /**
   * 更新用户
   */
  update: (data) => {
    return request.post('/user/update', data);
  },

  /**
   * 删除用户
   */
  delete: (data) => {
    return request.post('/user/delete', data);
  },
};
