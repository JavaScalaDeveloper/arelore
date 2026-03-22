import request from '../utils/request';
import { ApiResponse } from '../types';

export interface LoginParams {
  username: string;
  password: string;
}

export interface UserListParams {
  pageNum: number;
  pageSize: number;
  [key: string]: any;
}

/**
 * 管理员相关 API
 */
export const adminApi = {
  /**
   * 管理员登录
   */
  login: (data: LoginParams): Promise<ApiResponse<any>> => {
    return request.post('/admin/login', data);
  },

  /**
   * 获取仪表盘数据
   */
  getDashboard: (): Promise<ApiResponse<any>> => {
    return request.post('/admin/dashboard');
  },

  /**
   * 获取用户列表
   */
  getUserList: (data: UserListParams): Promise<ApiResponse<any>> => {
    return request.post('/admin/users', data);
  },

  /**
   * 获取系统设置
   */
  getSettings: (): Promise<ApiResponse<any>> => {
    return request.post('/admin/settings/get');
  },

  /**
   * 更新系统设置
   */
  updateSettings: (data: any): Promise<ApiResponse<any>> => {
    return request.post('/admin/settings/update', data);
  },
};
