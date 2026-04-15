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

  getDetectionTypeAll: (): Promise<ApiResponse<any>> => {
    return request.post('/user/detection/type/all', {});
  },

  getDetectionQuestionAll: (data?: { typeCode?: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/detection/question/all', data || {});
  },

  saveDetectionResult: (data: {
    userId: string;
    userDetectTypeCode: string;
    answeredQuestions: Array<{
      questionId?: number | string;
      questionCode?: string;
      selectedOptionKey: string;
    }>;
  }): Promise<ApiResponse<any>> => {
    return request.post('/user/detection/result/save', data);
  },

  getCurrentDetectionResult: (data: { userId: string; typeCode: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/detection/result/current', data);
  },

  getDetectionResultHistory: (data: { userId: string; typeCode: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/detection/result/history', data);
  },

  mobileRegisterApply: (data: { mobile: string; password: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/register/mobile/apply', data);
  },

  mobileRegisterVerify: (data: { mobile: string; verifyCode: string }): Promise<ApiResponse<any>> => {
    return request.post('/user/register/mobile/verify', data);
  },
};
