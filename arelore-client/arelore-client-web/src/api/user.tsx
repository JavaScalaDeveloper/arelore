import request from '../utils/request';
import { ApiResponse } from '../types';

export interface UserParams {
  pageNum: number;
  pageSize: number;
  [key: string]: any;
}

/**
 * 用户相关 API
 */
export const userApi = {
  /**
   * 获取用户列表
   */
  getList: (data: UserParams): Promise<ApiResponse<any>> => {
    return request.post('/user/list', data);
  },

  /**
   * 获取用户详情
   */
  getDetail: (data: { userId: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/detail', data);
  },

  /**
   * 创建用户
   */
  create: (data: any): Promise<ApiResponse<any>> => {
    return request.post('/user/create', data);
  },

  /**
   * 更新用户
   */
  update: (data: any): Promise<ApiResponse<any>> => {
    return request.post('/user/update', data);
  },

  /**
   * 删除用户
   */
  delete: (data: { userId: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/delete', data);
  },
};
