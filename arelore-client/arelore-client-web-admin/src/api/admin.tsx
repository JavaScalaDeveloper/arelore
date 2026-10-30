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
  getWordEntryDetail: (data: { id: number }): Promise<ApiResponse<any>> => request.post('/admin/word/entry/detail', data),
  createWordEntry: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/entry/create', data),
  updateWordEntry: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/entry/update', data),
  deleteWordEntry: (data: { id: number }): Promise<ApiResponse<any>> => request.post('/admin/word/entry/delete', data),

  getWordUserList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/user/list', data),
  getWordUserCurrentBookList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/user/current-book/list', data),
  getWordUserStudyPlanList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/user/study-plan/list', data),
  getWordUserLearnRecordList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/user/learn-record/list', data),

  getWordBaseInfoList: (data: any): Promise<ApiResponse<any>> => request.post('/admin/word/base-info/list', data),
  getWordBaseInfoDetail: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/admin/word/base-info/detail', data),
  testWordBaseInfoYoudao: (data: { word: string }): Promise<ApiResponse<any>> =>
    request.post('/admin/word/base-info/test-youdao', data, { timeout: 30000 }),
  searchWordBaseInfoPictures: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/admin/word/base-info/search-pictures', data, { timeout: 30000 }),
  /** 批量搜图：按当前筛选分页扫描；source=PIXABAY|YOUDAO；limit 不传则全部 */
  batchSearchWordBaseInfoPictures: (data?: {
    languageCode?: string;
    word?: string;
    tag?: string;
    tags?: string[];
    hasPicture?: boolean;
    pictureConfirmed?: boolean;
    source?: 'PIXABAY' | 'YOUDAO';
    /** 最多处理词数；不传/0 = 全部 */
    limit?: number;
    picturesPerWord?: number;
  }): Promise<ApiResponse<any>> =>
    request.post('/admin/word/base-info/batch-search-pictures', data || {}, { timeout: 60 * 60 * 1000 }),
  /** 批量用 Edge TTS 生成释义语音；force=true 强制重生成 */
  batchGenerateWordBaseInfoMeaningAudio: (data?: {
    languageCode?: string;
    word?: string;
    tag?: string;
    tags?: string[];
    hasPicture?: boolean;
    pictureConfirmed?: boolean;
    limit?: number;
    force?: boolean;
  }): Promise<ApiResponse<any>> =>
    request.post('/admin/word/base-info/batch-generate-meaning-audio', data || {}, { timeout: 6 * 60 * 60 * 1000 }),
  adoptWordBaseInfoPictures: (data: {
    id: number;
    pictureUrls: string[];
    pictureSource?: string;
  }): Promise<ApiResponse<any>> => request.post('/admin/word/base-info/adopt-pictures', data),
  /** 拉取已落库配图/语音；缺文件属常见情况，默认不弹全局「文件不存在」toast */
  getWordBaseInfoPictureContent: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/admin/word/base-info/picture-content', data, { skipErrorToast: true } as any),
  /** 从词条同步语种+词形（不查有道） */
  syncWordBaseInfo: (data?: any): Promise<ApiResponse<any>> =>
    // 含有道补释义，词量大时可能超过半小时
    request.post('/admin/word/base-info/sync', data || {}, { timeout: 2 * 60 * 60 * 1000 }),

  /** 公共二进制文件列表（不含 file_data） */
  getCommonBinaryFileList: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/common/binary-file/list', data),
  /** 详情含 base64，可能较大 */
  getCommonBinaryFileDetail: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/admin/common/binary-file/detail', data, { timeout: 120000 }),
  deleteCommonBinaryFile: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/admin/common/binary-file/delete', data),
  batchDeleteCommonBinaryFiles: (data: { ids: number[] }): Promise<ApiResponse<any>> =>
    request.post('/admin/common/binary-file/batch-delete', data),

  /** C 端日访问统计 */
  getCommonUserDailyVisitList: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/common/user-daily-visit/list', data),
  deleteCommonUserDailyVisit: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/admin/common/user-daily-visit/delete', data),
  batchDeleteCommonUserDailyVisits: (data: { ids: number[] }): Promise<ApiResponse<any>> =>
    request.post('/admin/common/user-daily-visit/batch-delete', data),

  /** 提示词产品：用户偏好 / 访问 / 用户提示词 */
  getPromptPrefPage: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/prompt/pref/page', data),
  getPromptPrefDetail: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/prompt/pref/detail', data),
  getPromptVisitPage: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/prompt/visit/page', data),
  getPromptVisitStats: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/prompt/visit/stats', data),
  getPromptUserPromptPage: (data: any): Promise<ApiResponse<any>> =>
    request.post('/admin/prompt/user-prompt/page', data),
};
