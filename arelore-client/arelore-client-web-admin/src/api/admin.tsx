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

export interface DetectionTypePayload {
  id?: number;
  typeCode: string;
  typeName: string;
  typeDescription?: string;
  extraInfo?: string;
}

export interface DetectionQuestionPayload {
  id?: number;
  typeCode: string;
  questionCode: string;
  questionName: string;
  questionOrder?: number;
  questionDescription?: string;
  options?: string;
  extraInfo?: string;
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

  getDetectionTypeList: (data: any): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/type/list', data);
  },

  createDetectionType: (data: DetectionTypePayload): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/type/create', data);
  },

  updateDetectionType: (data: DetectionTypePayload): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/type/update', data);
  },

  deleteDetectionType: (data: { id: number }): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/type/delete', data);
  },

  getDetectionQuestionList: (data: any): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/question/list', data);
  },

  createDetectionQuestion: (data: DetectionQuestionPayload): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/question/create', data);
  },

  updateDetectionQuestion: (data: DetectionQuestionPayload): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/question/update', data);
  },

  deleteDetectionQuestion: (data: { id: number }): Promise<ApiResponse<any>> => {
    return request.post('/admin/detection/question/delete', data);
  },

  getWordLanguageList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/language/list', data),
  createWordLanguage: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/language/create', data),
  updateWordLanguage: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/language/update', data),
  deleteWordLanguage: (data: { id: number }): Promise<ApiResponse<any>> => request.post('/admin/word/language/delete', data),

  getWordCategoryList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/category/list', data),
  createWordCategory: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/category/create', data),
  updateWordCategory: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/category/update', data),
  deleteWordCategory: (data: { id: number }): Promise<ApiResponse<any>> => request.post('/admin/word/category/delete', data),

  getWordBookList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/book/list', data),
  createWordBook: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/book/create', data),
  updateWordBook: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/book/update', data),
  deleteWordBook: (data: { id: number }): Promise<ApiResponse<any>> => request.post('/admin/word/book/delete', data),

  getWordEntryList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/entry/list', data),
  createWordEntry: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/entry/create', data),
  updateWordEntry: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/entry/update', data),
  deleteWordEntry: (data: { id: number }): Promise<ApiResponse<any>> => request.post('/admin/word/entry/delete', data),
};
